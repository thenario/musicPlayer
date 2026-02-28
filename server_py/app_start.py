# 标准库导入
import json
import logging
import os
import threading
import uuid
from collections import defaultdict
from datetime import datetime, timedelta, timezone
from functools import wraps
from urllib.parse import quote

# 第三方库导入
import mutagen
from flask import (Flask, current_app, jsonify, request,
                   send_file, session)
from flask_cors import CORS
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from flask_login import (LoginManager, current_user, login_required,
                         login_user, logout_user)
from mutagen.easyid3 import EasyID3
from mutagen.id3 import APIC
from mutagen.mp3 import MP3
import redis
from sqlalchemy import text
from sqlalchemy.orm import joinedload
from werkzeug.utils import secure_filename

# 本地应用/模块导入
from db_model import (PlayQueue, PlayQueueItem, Playlist, PlaylistLike,
                      PlaylistSong, PlaylistTag, Song, Tag, User,
                      UserPlayState, UserSession, db)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)


class Config:
    # SQLALCHEMY_DATABASE_URI = os.environ.get('DATABASE_URL') or \
    # 'mysql+pymysql://music_user:password@localhost/music_player'
    SQLALCHEMY_DATABASE_URI = os.environ.get('DATABASE_URL') or \
                              'sqlite:///music_player.db'

    SQLALCHEMY_TRACK_MODIFICATIONS = False
    SQLALCHEMY_ENGINE_OPTIONS = {
        'pool_size': 5,
        'max_overflow': 10,
        'pool_recycle': 3600,
        'pool_pre_ping': True,
        'pool_timeout': 30,
        'isolation_level': None,
        # 'isolation_level': 'READ COMMITTED'
    }

    REDIS_URL = os.environ.get('REDIS_URL') or 'redis://localhost:6379/0'

    SECRET_KEY = os.environ.get('SECRET_KEY') or 'your-secret-key-change-in-production'
    UPLOAD_FOLDER = 'static/uploader/songs'
    COVERS_FOLDER = 'static/uploader/covers'

    os.makedirs(UPLOAD_FOLDER, exist_ok=True)
    os.makedirs(COVERS_FOLDER, exist_ok=True)


app.config.from_object(Config)

SESSION_COOKIE_SECURE = False
SESSION_COOKIE_SAMESITE = 'Lax'
SESSION_COOKIE_PATH = '/'



# 反向代理的配置没写

db.init_app(app)
CORS(app, supports_credentials=True)

redis_client = redis.Redis.from_url(app.config['REDIS_URL'], decode_responses=True)

limiter = Limiter(
    app=app,
    key_func=get_remote_address,
    storage_uri=app.config['REDIS_URL'],
    # default_limits=["200 per day", "50 per hour"],
    strategy='moving-window'
)

login_manager = LoginManager()

login_manager.init_app(app)
login_manager.login_view = 'login'
login_manager.session_protection = 'strong'

# =========================================================================
# 辅助工具与装饰器 (Helpers & Decorators)
# =========================================================================


ALLOWED_AUDIO_EXT = {'mp3', 'wav', 'flac', 'm4a', 'ogg'}
ALLOWED_IMG_EXT = {'png', 'jpg', 'jpeg', 'gif'}
SERVER_URL = "http://127.0.0.1:5000"


def allowed_file(filename, file_type='audio'):
    if '.' not in filename: return False
    ext = filename.rsplit('.', 1)[1].lower()
    return ext in (ALLOWED_AUDIO_EXT if file_type == 'audio' else ALLOWED_IMG_EXT)


def get_full_url(path):
    """将相对路径转换为完整的前端可用 URL (处理封面图)"""
    if not path: return None
    if path.startswith('http'): return path

    # 统一路径分隔符并清理
    clean_path = path.replace('\\', '/').lstrip('/')

    # 对文件名进行 URL 编码 (处理中文)
    if '/' in clean_path:
        folder, filename = clean_path.rsplit('/', 1)
        clean_path = f"{folder}/{quote(filename)}"
    else:
        clean_path = quote(clean_path)

    # 智能补全路径 (如果数据库存的是文件名)
    if 'static' not in clean_path:
        return f"{SERVER_URL}/static/uploader/covers/{clean_path}"

    return f"{SERVER_URL}/{clean_path}"


def extract_and_save_cover(audio_path, covers_folder):
    """从音频文件提取封面并保存"""
    try:
        audio = mutagen.File(audio_path)
        data = None
        ext = 'jpg'

        # MP3 (ID3)
        if hasattr(audio, 'tags') and audio.tags:
            for tag in audio.tags.values():
                if isinstance(tag, mutagen.id3.APIC):
                    data = tag.data
                    if 'png' in tag.mime: ext = 'png'
                    break
        # FLAC
        elif hasattr(audio, 'pictures') and audio.pictures:
            data = audio.pictures[0].data
            if 'png' in audio.pictures[0].mime: ext = 'png'

        if data:
            fname = f"{uuid.uuid4().hex}.{ext}"
            save_path = os.path.join(covers_folder, fname)
            with open(save_path, 'wb') as f:
                f.write(data)
            return f"/static/uploader/covers/{fname}"

    except Exception as e:
        logger.warning(f"Failed to extract cover from {audio_path}: {e}")

    return None


def extract_metadata(file_path):
    """提取音频元数据"""
    try:
        audio = mutagen.File(file_path, easy=True)
        if not audio: return None

        info = audio.info
        filename_without_ext = os.path.splitext(os.path.basename(file_path))[0]

        def get_tag(name, default):
            return audio.get(name, [default])[0]

        return {
            'title': get_tag('title', filename_without_ext),
            'artist': get_tag('artist', 'Unknown Artist'),
            'album': get_tag('album', 'Unknown Album'),
            'duration': int(info.length) if info else 0,
            'file_size': os.path.getsize(file_path),
            'file_format': os.path.splitext(file_path)[1][1:].upper(),
            'bitrate': getattr(info, 'bitrate', 0) // 1000 if info and hasattr(info, 'bitrate') else 0,
            'genre': get_tag('genre', ''),
            'year': get_tag('date', ''),
            'track_number': 0
        }
    except Exception as e:
        logger.error(f"Metadata extraction error for {file_path}: {e}")
        return None


# --- B. 数据序列化 ---

def song_to_dict(song):
    """Song 模型转字典"""
    return {
        'id': song.id,
        'title': song.title,
        'artist': song.artist or "Unknown Artist",
        'album': song.album or "Unknown Album",
        'duration': song.duration,
        'file_format': song.file_format,
        'file_size': song.file_size,
        'bitrate': getattr(song, 'bitrate', 0),
        'play_count': song.play_count,
        'date_added': song.date_added.isoformat() if song.date_added else None,
        'last_played': song.last_played.isoformat() if song.last_played else None,
        'uploader_id': song.uploader_id,
        'uploader_name': song.uploader.user_name if song.uploader else 'Unknown',
        'cover_url': get_full_url(song.cover_url),
        'lyrics': song.lyrics or ""
    }


def queue_to_dict(queue, include_items=True):
    """PlayQueue 模型转字典"""
    res = {
        'id': queue.id,
        'name': queue.name,
        'is_saved': queue.is_saved,
        'user_id': queue.user_id,
        'item_count': len(queue.queue_items) if queue.queue_items else 0,
        'created_date': queue.created_date.isoformat() if queue.created_date else None,
        'updated_date': queue.updated_date.isoformat() if queue.updated_date else None
    }
    if include_items and queue.queue_items:
        res['items'] = [{
            'id': item.id,
            'position': item.position,
            'song': song_to_dict(item.song),
            'added_date': item.added_date.isoformat() if item.added_date else None
        } for item in queue.queue_items]
    return res


def play_state_to_dict(play_state):
    """UserPlayState 模型转字典"""
    if not play_state: return None
    return {
        'current_queue_id': play_state.current_queue_id,
        'current_song_id': play_state.current_song_id,
        'current_position': play_state.current_position,
        'progress': play_state.progress,
        'is_playing': play_state.is_playing,
        'play_mode': play_state.play_mode,
        'updated_date': play_state.updated_date.isoformat() if play_state.updated_date else None
    }


# --- C. 播放状态管理 ---

def get_or_create_user_play_state(user_id):
    """获取或创建用户播放状态"""
    state = UserPlayState.query.filter_by(user_id=user_id).first()
    if not state:
        state = UserPlayState(user_id=user_id)
        db.session.add(state)
        db.session.commit()
    return state


def get_user_current_queue(user_id):
    """获取用户当前的播放队列"""
    state = get_or_create_user_play_state(user_id)
    if state.current_queue_id:
        return PlayQueue.query.filter_by(id=state.current_queue_id, user_id=user_id).first()
    return None


def set_user_current_queue(user_id, queue_id):
    """设置用户当前播放队列"""
    queue = PlayQueue.query.filter_by(id=queue_id, user_id=user_id).first()
    if not queue: return False

    state = get_or_create_user_play_state(user_id)
    state.current_queue_id = queue_id
    state.current_position = 0
    state.progress = 0
    db.session.commit()
    return True


@app.after_request
def update_user_last_activity(response):
    """更新用户最后活跃时间 (After Request)"""
    if response.status_code < 400 and current_user.is_authenticated:
        sid = session.get('user_session_id')
        if sid:
            try:
                # 简单优化：不每次都查库，这里只演示逻辑
                # 实际生产中建议用 Redis 缓存活跃时间，定期写库
                UserSession.query.filter_by(id=sid).update({'last_activity': datetime.now(timezone.utc)})
                db.session.commit()
            except Exception:
                db.session.rollback()
    return response


# --- D. 装饰器 ---

def cache_response(time_out=300, key_prefix='view'):
    """响应缓存装饰器"""

    def decorator(f):
        @wraps(f)
        def wrapper(*args, **kwargs):
            # 如果 Redis 不可用，直接跳过缓存逻辑
            if not redis_client: return f(*args, **kwargs)

            cache_key = f"{key_prefix}:{request.path}:{hash(frozenset(request.args.items()))}"
            cached = redis_client.get(cache_key)

            if cached:
                logger.debug(f"Cache hit for key: {cache_key}")
                return jsonify(json.loads(cached))

            response = f(*args, **kwargs)

            if response.status_code == 200 and response.is_json:
                redis_client.setex(cache_key, time_out, response.get_data(as_text=True))
            return response

        return wrapper

    return decorator


def concurrent_control(key_func, limit=1, time_out=10):
    """并发控制装饰器 (分布式锁)"""

    def decorator(f):
        @wraps(f)
        def wrapper(*args, **kwargs):
            if not redis_client: return f(*args, **kwargs)

            if key_func:
                lock_key = f"lock:{key_func(*args, **kwargs)}"
            else:
                lock_key = f"lock:{request.endpoint}:{current_user.id if current_user.is_authenticated else get_remote_address()}"

            # 尝试获取锁
            if not redis_client.set(lock_key, '1', nx=True, ex=time_out):
                return jsonify({"error": "操作太快，请稍候"}), 429

            try:
                return f(*args, **kwargs)
            finally:
                redis_client.delete(lock_key)

        return wrapper

    return decorator


# =========================================================================
# 歌曲路由 (Song Routes)
# =========================================================================


@login_manager.unauthorized_handler
def unauthorized():
    return jsonify({"error": "用户未登录或会话已过期，请重新登录"}), 401


@login_manager.user_loader
def load_user(user_id):
    cache_key = f"user:{user_id}"

    # 1. 尝试从 Redis 缓存加载
    if redis_client:
        user_data = redis_client.get(cache_key)
        if user_data:
            try:
                d = json.loads(user_data)
                user = User()
                user.id = d['id']
                user.user_name = d['user_name']
                user.email = d['email']
                user.is_active = d['is_active']
                return user
            except Exception as e:
                logger.warning(f"Cache data parse error for user {user_id}: {e}")

    # 2. 从数据库加载
    user = db.session.get(User, int(user_id))
    if user and redis_client:
        # 写入缓存
        redis_client.setex(
            cache_key,
            300,
            json.dumps({
                'id': user.id,
                'user_name': user.user_name,
                'email': user.email,
                'is_active': user.is_active
            })
        )
    return user


@app.route('/api/register', methods=['POST'])
def register():
    """用户注册"""
    logger.info("Received registration request")

    data = request.get_json() or {}

    # 1. 字段验证
    if not all(k in data for k in ('user_name', 'email', 'password')):
        logger.warning("Registration failed: Missing fields")
        return jsonify({'error': '缺少必要字段'}), 400

    username = data['user_name'].strip()
    email = data['email'].strip()
    password = data['password']

    if not username or not email or not password:
        logger.warning("Registration failed: Empty values")
        return jsonify({'error': '字段不能为空'}), 400

    # 2. 唯一性检查
    if User.query.filter_by(user_name=username).first():
        logger.warning(f"Registration failed: Username '{username}' exists")
        return jsonify({'error': '用户名已存在'}), 400

    if User.query.filter_by(email=email).first():
        logger.warning(f"Registration failed: Email '{email}' exists")
        return jsonify({'error': '邮箱已存在'}), 400

    # 3. 创建用户
    try:
        user = User(user_name=username, email=email)
        user.set_password(password)

        db.session.add(user)
        db.session.commit()

        if redis_client:
            redis_client.delete('users:count')

        logger.info(f"User registered successfully: ID={user.id}, Name={user.user_name}")

        return jsonify({
            'message': '用户注册成功',
            'user_id': user.id,
            'user_name': user.user_name,
            'email': user.email
        })

    except Exception as e:
        db.session.rollback()
        logger.critical("Database error during registration", exc_info=True)
        return jsonify({'error': '服务器内部错误'}), 500


@app.route('/api/login', methods=['POST'])
def login():
    """用户登录"""
    data = request.get_json() or {}
    username = data.get('user_name', '').strip()
    password = data.get('password', '')

    if not username or not password:
        logger.warning("Login failed: Missing credentials")
        return jsonify({"error": "用户名和密码不能为空"}), 400

    logger.debug(f"Attempting login for user: {username}")

    try:
        user = User.query.filter_by(user_name=username).first()

        # 验证用户和密码
        if user and user.check_password(password):
            # 1. 执行登录
            login_user(user, remember=True)
            session.permanent = True

            # 2. 记录 Session (UserSession)
            user_session_id = str(uuid.uuid4())
            session['user_session_id'] = user_session_id

            user.last_login = datetime.now(timezone.utc)

            user_session = UserSession(
                id=user_session_id,
                user_id=user.id,
                ip_address=request.remote_addr,
                user_agent=request.headers.get('User-Agent', ''),
                last_activity=datetime.now(timezone.utc)
            )
            db.session.add(user_session)
            db.session.commit()

            logger.info(f"Login successful: User={user.user_name} (ID={user.id})")

            return jsonify({
                "message": "登录成功",
                "user_id": user.id,
                "user_name": user.user_name,
                "email": user.email
            })

        else:
            logger.warning(f"Login failed: Invalid credentials for '{username}'")
            return jsonify({"error": "用户名或密码错误"}), 401

    except Exception as e:
        db.session.rollback()
        logger.critical("Login process error", exc_info=True)
        return jsonify({"error": "登录过程出错"}), 500


@app.route('/api/logout', methods=['POST'])
@login_required
def logout():
    """用户登出"""
    try:
        user_name = current_user.user_name
        logger.info(f"Logout request from user: {user_name} (ID={current_user.id})")

        logout_user()
        session.clear()

        response = jsonify({'message': "退出成功"})

        # 清除 Cookies
        response.set_cookie('session', '', expires=0, path='/')
        response.set_cookie('remember_token', '', expires=0, path='/')

        logger.info(f"User {user_name} logged out successfully")
        return response

    except Exception as e:
        logger.error("Error during logout", exc_info=True)
        return jsonify({'error': '退出时发生错误'}), 500


@app.route('/api/getuser')
@login_required
def get_user():
    """获取当前用户信息"""
    return jsonify({
        'id': current_user.id,
        'user_name': current_user.user_name,
        'email': current_user.email
    })


#  ======================================================歌曲操作相关=================================================


@app.route('/api/songs')
@cache_response(time_out=60)
def get_songs():
    """获取歌曲列表"""
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 20, type=int)
    search = request.args.get('search', '')

    logger.debug(f"API Request: get_songs - page={page}, per_page={per_page}, search='{search}'")

    query = Song.query.filter_by(is_public=True)

    if search:
        query = query.filter(
            db.or_(
                Song.title.ilike(f"%{search}%"),
                Song.artist.ilike(f"%{search}%"),
                Song.album.ilike(f"%{search}%")
            )
        )

    try:
        pagination = query.order_by(Song.date_added.desc()).paginate(
            page=page, per_page=per_page, error_out=False
        )
        songs = pagination.items
        logger.debug(f"Query result: total={pagination.total}, pages={pagination.pages}, current_count={len(songs)}")

    except Exception as e:
        logger.error(f"Pagination query error: {str(e)}")
        return jsonify({'error': 'Internal server error', 'message': str(e)}), 500

    # 使用 song_to_dict 统一格式
    return jsonify({
        'songs': [song_to_dict(song) for song in songs],
        'total': pagination.total,
        'pages': pagination.pages,
        'current_page': page,
        'per_page': per_page
    })


@app.route('/api/songs/<int:song_id>/play', methods=['POST'])
@login_required
def play_song(song_id):
    """播放歌曲并更新队列状态"""
    song = Song.query.get_or_404(song_id)

    # 获取当前队列
    queue = get_user_current_queue(current_user.id)

    # 查找歌曲在队列中的位置
    queue_item = PlayQueueItem.query.filter_by(queue_id=queue.id, song_id=song_id).first()
    position = queue_item.position - 1 if queue_item else 0

    # 更新播放状态
    play_state = get_or_create_user_play_state(current_user.id)
    play_state.current_song_id = song_id
    play_state.current_queue_id = queue.id
    play_state.current_position = position
    play_state.is_playing = True
    play_state.progress = 0
    play_state.updated_date = datetime.now(timezone.utc)

    # 更新播放计数
    song.play_count += 1
    song.last_played = datetime.now(timezone.utc)

    db.session.commit()

    return jsonify({
        "message": "开始播放",
        "song": song_to_dict(song),  # 使用统一函数
        "queue_id": queue.id
    })


@app.route('/api/songs/refresh_server_songs', methods=["POST"])
@login_required
@concurrent_control(lambda: f"{current_user.id}:refresh_server_songs")
def refresh_server_songs():
    """扫描服务器歌曲文件夹"""
    logger.info(f"Start refreshing server songs for user: {current_user.id}")

    try:
        folder_path = Config.UPLOAD_FOLDER
        covers_folder = Config.COVERS_FOLDER  # 建议使用 Config 变量

        if not os.path.exists(folder_path):
            return jsonify({"status": "error", "message": f"目录不存在: {folder_path}"}), 404

        if not os.path.exists(covers_folder):
            os.makedirs(covers_folder, exist_ok=True)

        added_count = 0
        error_files = []
        skipped_files = 0

        files = os.listdir(folder_path)
        logger.debug(f"Found {len(files)} files in folder")

        for filename in files:
            file_path = os.path.join(folder_path, filename)

            if not os.path.isfile(file_path): continue
            if not filename.lower().endswith(tuple(ALLOWED_AUDIO_EXT)):
                skipped_files += 1
                continue

            try:
                # 查重
                if Song.query.filter_by(file_path=file_path).first():
                    skipped_files += 1
                    continue

                # 解析文件名
                if '_' in filename:
                    name_part = os.path.splitext(filename)[0]
                    parts = name_part.split('_', 1)
                    title = parts[0].strip()
                    artist = parts[1].strip() if len(parts) > 1 else "未知艺术家"
                else:
                    title = os.path.splitext(filename)[0].strip()
                    artist = "未知艺术家"

                # 初始化元数据
                album = "未知专辑"
                cover_url = None
                duration = 0

                # 读取元数据
                try:
                    if filename.lower().endswith('.mp3'):
                        try:
                            audio = MP3(file_path, ID3=EasyID3)
                            if 'album' in audio: album = audio['album'][0]
                        except:
                            audio = MP3(file_path)
                    else:
                        audio = mutagen.File(file_path, easy=True)
                        if audio and 'album' in audio: album = audio['album'][0]

                    duration = int(audio.info.length) if audio else 0
                except Exception as e:
                    logger.warning(f"Metadata read error for {filename}: {e}")

                file_size = os.path.getsize(file_path)

                # 查找同名封面
                base_name = os.path.splitext(filename)[0]
                for ext in ['.jpg', '.jpeg', '.png', '.gif', '.bmp']:
                    for case_ext in [ext, ext.upper()]:
                        pot_cover = f"{base_name}{case_ext}"
                        if os.path.exists(os.path.join(covers_folder, pot_cover)):
                            # 构建 Web 路径
                            cover_url = f"/static/uploader/covers/{pot_cover}"
                            break
                    if cover_url: break

                # 默认封面
                if not cover_url:
                    default_cover = "default_cover.jpg"
                    if os.path.exists(os.path.join(covers_folder, default_cover)):
                        cover_url = f"/static/uploader/covers/{default_cover}"

                # 创建记录
                song = Song(
                    file_path=file_path, title=title, artist=artist, album=album,
                    cover_url=cover_url, duration=duration, file_size=file_size,
                    file_name=filename, uploader_id=current_user.id
                )
                db.session.add(song)
                added_count += 1

            except Exception as e:
                logger.error(f"Error processing file {filename}: {e}")
                error_files.append({'filename': filename, 'error': str(e)})
                continue

        db.session.commit()
        if redis_client: redis_client.delete("stats:global")

        logger.info(f"Refresh completed: added {added_count}, skipped {skipped_files}, errors {len(error_files)}")

        return jsonify({
            "status": "success", "message": f"成功添加 {added_count} 首新歌",
            "added_count": added_count, "errors": error_files,
            "error_count": len(error_files), "skipped_count": skipped_files
        })

    except Exception as e:
        db.session.rollback()
        logger.critical(f"Refresh process error: {e}", exc_info=True)
        return jsonify({"status": "error", "message": str(e)}), 500


@app.route('/api/songs/upload', methods=['POST'])
@login_required
@concurrent_control(lambda: f"upload:{current_user.id}")
def upload_songs():
    """上传歌曲"""
    if 'file' not in request.files: return jsonify({'error': "没有选择文件"}), 400
    file = request.files['file']
    if file.filename == '': return jsonify({'error': "未选择文件"}), 400

    if allowed_file(file.filename, 'audio'):
        try:
            # 1. 保存文件
            original_filename = secure_filename(file.filename)
            ext = original_filename.rsplit('.', 1)[1].lower()
            uuid_filename = f"{uuid.uuid4().hex}.{ext}"

            save_path = os.path.join(app.config['UPLOAD_FOLDER'], uuid_filename)
            file.save(save_path)

            # 2. 获取表单数据
            form = request.form
            title = form.get('title', '').strip()
            artist = form.get('artist', '').strip()
            album = form.get('album', '').strip()

            # 3. 提取元数据
            meta_title, meta_artist, meta_album = None, None, None
            duration, bitrate = 0, 0

            try:
                audio = mutagen.File(save_path, easy=True)
                if audio:
                    meta_title = audio.get('title', [None])[0]
                    meta_artist = audio.get('artist', [None])[0]
                    meta_album = audio.get('album', [None])[0]
                    duration = int(audio.info.length)
                    bitrate = int(getattr(audio.info, 'bitrate', 0) / 1000)
            except Exception as e:
                logger.warning(f"Metadata extract warning: {e}")

            # 4. 确定最终字段 (用户输入 > 元数据 > 默认)
            final_title = title or meta_title or os.path.splitext(original_filename)[0]
            final_artist = artist or meta_artist or "Unknown Artist"
            final_album = album or meta_album or "Unknown Album"

            # 5. 处理封面
            final_cover = None
            cover_file = request.files.get('cover_image')

            # A: 用户上传
            if cover_file and cover_file.filename and allowed_file(cover_file.filename, 'cover'):
                cext = cover_file.filename.rsplit('.', 1)[1].lower()
                cname = f"{uuid.uuid4().hex}.{cext}"
                cover_file.save(os.path.join(app.config['COVERS_FOLDER'], cname))
                final_cover = f"/static/uploader/covers/{cname}"
            # B: 自动提取
            else:
                final_cover = extract_and_save_cover(save_path, app.config['COVERS_FOLDER'])

            # 6. 入库
            song = Song(
                title=final_title, artist=final_artist, album=final_album,
                duration=duration, bitrate=bitrate,
                file_path=save_path, file_name=uuid_filename,
                file_size=os.path.getsize(save_path), file_format=ext.upper(),
                cover_url=final_cover, uploader_id=current_user.id, is_public=True
            )

            db.session.add(song)
            db.session.commit()

            if redis_client:
                redis_client.delete("songs:popular")
                redis_client.delete("stats:global")

            return jsonify({"message": "上传成功"})

        except Exception as e:
            if os.path.exists(save_path): os.remove(save_path)
            db.session.rollback()
            logger.error(f"Upload failed: {e}", exc_info=True)
            return jsonify({"error": f"上传处理失败: {str(e)}"}), 500

    return jsonify({"error": "不支持的文件类型"}), 400


@app.route('/api/songs/<int:song_id>/stream')
@limiter.exempt
def stream_song(song_id):
    """流媒体播放"""
    song = db.session.get(Song, song_id)
    if not song: return jsonify({"error": "歌曲不存在"}), 404

    # 异步更新播放数
    app_instance = current_app._get_current_object()

    def update_count(app_obj):
        with app_obj.app_context():
            try:
                target_song = db.session.get(Song, song_id)
                if target_song:
                    target_song.play_count += 1
                    target_song.last_played = datetime.now(timezone.utc)
                    db.session.commit()
                    if redis_client: redis_client.delete("songs:popular")
            except Exception as e:
                logger.error(f"Play count update error: {e}")

    threading.Thread(target=update_count, args=(app_instance,)).start()

    range_header = request.headers.get('Range', None)
    return send_file(song.file_path, conditional=True) if range_header else send_file(song.file_path)


# =========================================================================
#  歌单路由 (Playlist Routes)
# =========================================================================

@app.route('/api/playlists/my')
@login_required
def get_my_playlists():
    playlists = Playlist.query.filter_by(user_id=current_user.id).all()
    return jsonify({
        'playlists': [{
            'id': p.id,
            'name': p.name,
            'description': p.description,
            'cover_image': get_full_url(p.cover_image),
            'song_count': p.songs.count(),
            'like_count': p.like_count,
            'is_public': p.is_public,
            'created_date': p.created_date.isoformat() if p.created_date else None,
            'updated_date': p.updated_date.isoformat() if p.updated_date else None
        } for p in playlists]
    })


@app.route('/api/playlists/<int:pid>')
def get_playlist(pid):
    p = db.session.get(Playlist, pid)
    if not p: return jsonify({"error": "歌单不存在"}), 404

    if not p.is_public and (not current_user.is_authenticated or p.user_id != current_user.id):
        return jsonify({"error": "无权访问"}), 403

    # 异步更新访问数
    app_instance = current_app._get_current_object()

    def update_count(app_obj):
        with app_obj.app_context():
            try:
                pl = db.session.get(Playlist, pid)
                if pl:
                    pl.play_count += 1
                    db.session.commit()
            except Exception as e:
                logger.error(f"Update playlist play count error: {e}")

    threading.Thread(target=update_count, args=(app_instance,)).start()

    songs = PlaylistSong.query.options(joinedload(PlaylistSong.song)) \
        .filter_by(playlist_id=pid).order_by(PlaylistSong.position).all()

    is_liked = False
    if current_user.is_authenticated:
        is_liked = PlaylistLike.query.filter_by(user_id=current_user.id, playlist_id=pid).count() > 0

    return jsonify({
        'id': p.id, 'name': p.name, 'description': p.description,
        'cover_image': get_full_url(p.cover_image),
        'user': {'id': p.user.id, 'user_name': p.user.user_name},
        'play_count': p.play_count, 'like_count': p.like_count, 'song_count': len(songs),
        'is_liked': is_liked, 'is_public': p.is_public,
        'created_date': p.created_date.isoformat() if p.created_date else None,
        'songs': [{
            'id': ps.song.id, 'title': ps.song.title, 'artist': ps.song.artist, 'album': ps.song.album,
            'duration': ps.song.duration, 'cover_url': get_full_url(ps.song.cover_url), 'position': ps.position
        } for ps in songs]
    })


@app.route('/api/playlists', methods=['POST'])
@login_required
@concurrent_control(lambda: f"create_pl:{current_user.id}")
def create_playlist():
    name = request.form.get('name')
    if not name: return jsonify({"error": "名称必填"}), 400

    cover_path = ""
    if 'cover_image' in request.files:
        f = request.files['cover_image']
        if f and f.filename:
            fname = secure_filename(f.filename)
            ext = fname.rsplit('.', 1)[1].lower() if '.' in fname else 'jpg'
            new_name = f"{uuid.uuid4().hex}.{ext}"
            f.save(os.path.join(app.config['COVERS_FOLDER'], new_name))
            cover_path = f"/static/uploader/covers/{new_name}"

    pl = Playlist(
        name=name, description=request.form.get('description', ''),
        user_id=current_user.id, cover_image=cover_path,
        is_public=request.form.get('is_public') == 'true'
    )

    # 处理 Tags (JSON string)
    tags_str = request.form.get('tags')
    if tags_str:
        try:
            tags_list = json.loads(tags_str)
            for tname in tags_list:
                tag = Tag.query.filter_by(name=tname).first()
                if not tag:
                    tag = Tag(name=tname)
                    db.session.add(tag)
                pl.tags.append(tag)
        except:
            pass

    try:
        db.session.add(pl)
        db.session.commit()
        if redis_client:
            redis_client.delete("playlists:recent")
            redis_client.delete(f"user:{current_user.id}:playlists")
        return jsonify({"message": "创建成功", "playlist_id": pl.id, "cover_image": cover_path})
    except Exception as e:
        db.session.rollback()
        logger.error(f"Create playlist failed: {e}")
        return jsonify({"error": str(e)}), 500


@app.route('/api/playlists/<int:pid>', methods=['DELETE'])
@login_required
def delete_playlist(pid):
    pl = Playlist.query.filter_by(id=pid, user_id=current_user.id).first()
    if not pl: return jsonify({"error": "歌单不存在或无权删除"}), 404

    # 记录封面路径以便后续删除
    cover_path = pl.cover_image

    try:
        # 手动清理关联表 (虽然有 cascade，但手动更保险)
        PlaylistSong.query.filter_by(playlist_id=pid).delete()
        PlaylistTag.query.filter_by(playlist_id=pid).delete()
        PlaylistLike.query.filter_by(playlist_id=pid).delete()

        db.session.delete(pl)
        db.session.commit()

        # 删除封面文件
        if cover_path and 'static' in cover_path:
            try:
                fname = os.path.basename(cover_path)
                fpath = os.path.join(app.config['COVERS_FOLDER'], fname)
                if os.path.exists(fpath): os.remove(fpath)
            except Exception as e:
                logger.warning(f"Delete cover file failed: {e}")

        # 清除缓存
        if redis_client:
            redis_client.delete(f"playlist:{pid}")
            redis_client.delete("playlists:recent")
            redis_client.delete("playlists:popular")
            redis_client.delete(f"user:{current_user.id}:playlists")

        logger.info(f"User {current_user.id} deleted playlist {pid}")
        return jsonify({"message": "删除成功"})

    except Exception as e:
        db.session.rollback()
        logger.error(f"Delete playlist failed: {e}")
        return jsonify({"error": "删除失败"}), 500


@app.route('/api/playlists/<int:pid>/songs', methods=['POST'])
@login_required
@concurrent_control(lambda pid: f"pl_add:{pid}")
def add_song_to_playlist(pid):
    pl = Playlist.query.get(pid)
    if not pl: return jsonify({"error": "歌单不存在"}), 404
    if pl.user_id != current_user.id: return jsonify({"error": "无权操作"}), 403

    sid = request.json.get('song_id')
    if not sid: return jsonify({"error": "缺少歌曲ID"}), 400

    if PlaylistSong.query.filter_by(playlist_id=pid, song_id=sid).first():
        return jsonify({"error": "歌曲已存在"}), 400

    try:
        max_pos = db.session.query(db.func.max(PlaylistSong.position)).filter_by(playlist_id=pid).scalar() or 0
        item = PlaylistSong(playlist_id=pid, song_id=sid, position=max_pos + 1, added_by=current_user.id)

        db.session.add(item)
        pl.updated_date = datetime.now(timezone.utc)
        # 如果模型有 song_count 字段则更新
        if hasattr(pl, 'song_count'): pl.song_count = (pl.song_count or 0) + 1

        db.session.commit()
        if redis_client: redis_client.delete(f"playlist:{pid}")

        return jsonify({"message": "添加成功", "position": max_pos + 1})
    except Exception as e:
        db.session.rollback()
        return jsonify({"error": str(e)}), 500


@app.route('/api/playlists/<int:pid>/songs/<int:sid>', methods=['DELETE'])
@login_required
def remove_song_from_playlist(pid, sid):
    pl = db.session.get(Playlist, pid)
    if not pl or pl.user_id != current_user.id: return jsonify({"error": "无权操作"}), 403

    item = PlaylistSong.query.filter_by(playlist_id=pid, song_id=sid).first()
    if not item: return jsonify({"error": "歌曲不在歌单中"}), 404

    try:
        pos = item.position
        db.session.delete(item)

        # 重排后续位置
        PlaylistSong.query.filter(PlaylistSong.playlist_id == pid, PlaylistSong.position > pos) \
            .update({PlaylistSong.position: PlaylistSong.position - 1})

        pl.updated_date = datetime.now(timezone.utc)
        if hasattr(pl, 'song_count'): pl.song_count = max(0, (pl.song_count or 0) - 1)

        db.session.commit()
        if redis_client: redis_client.delete(f"playlist:{pid}")

        return jsonify({"message": "移除成功"})
    except Exception as e:
        db.session.rollback()
        return jsonify({"error": str(e)}), 500


@app.route('/api/playlists/<int:pid>/like', methods=['POST'])
@login_required
def like_playlist(pid):
    pl = db.session.get(Playlist, pid)
    if not pl: return jsonify({"error": "不存在"}), 404

    if PlaylistLike.query.filter_by(user_id=current_user.id, playlist_id=pid).first():
        return jsonify({"message": "已点赞"}), 200

    try:
        db.session.add(PlaylistLike(user_id=current_user.id, playlist_id=pid))
        pl.like_count += 1
        db.session.commit()

        if redis_client:
            redis_client.delete(f"playlist:{pid}")
            redis_client.delete(f"user:{current_user.id}:liked_playlists")

        return jsonify({"message": "OK", "like_count": pl.like_count})
    except Exception as e:
        db.session.rollback()
        return jsonify({"error": str(e)}), 500


@app.route('/api/playlists/<int:pid>/unlike', methods=['POST'])
@login_required
def unlike_playlist(pid):
    pl = db.session.get(Playlist, pid)
    if not pl: return jsonify({"error": "不存在"}), 404

    like = PlaylistLike.query.filter_by(user_id=current_user.id, playlist_id=pid).first()
    if like:
        try:
            db.session.delete(like)
            pl.like_count = max(0, pl.like_count - 1)
            db.session.commit()

            if redis_client:
                redis_client.delete(f"playlist:{pid}")
                redis_client.delete(f"user:{current_user.id}:liked_playlists")

            return jsonify({"message": "OK", "like_count": pl.like_count})
        except Exception as e:
            db.session.rollback()
            return jsonify({"error": str(e)}), 500

    return jsonify({"error": "未点赞"}), 400


# =========================================================================
# 队列管理路由 (Queue Routes)
# =========================================================================

@app.route('/api/queues')
@login_required
def get_user_queues():
    """获取用户的所有队列"""
    queues = PlayQueue.query.filter_by(user_id=current_user.id).order_by(
        PlayQueue.updated_date.desc()
    ).all()

    state = get_or_create_user_play_state(current_user.id)
    current_id = state.current_queue_id

    return jsonify([{
        'id': q.id,
        'name': q.name,
        'is_saved': q.is_saved,
        'is_current': q.id == current_id,
        'item_count': len(q.queue_items),
        'created_date': q.created_date.isoformat(),
        'updated_date': q.updated_date.isoformat()
    } for q in queues])


@app.route('/api/queues/<int:queue_id>')
@login_required
def get_queue_detail(queue_id):
    """获取指定队列详情 (用于预览)"""
    queue = PlayQueue.query.filter_by(id=queue_id, user_id=current_user.id).first_or_404()
    return jsonify({'queue': queue_to_dict(queue, include_items=True)})


@app.route('/api/queue/current')
@login_required
@limiter.exempt
def get_current_queue_info():
    """获取当前正在播放的队列"""
    state = get_or_create_user_play_state(current_user.id)
    queue = None

    # 1. 尝试从状态中获取
    if state.current_queue_id:
        queue = db.session.get(PlayQueue, state.current_queue_id)

    # 2. 兜底：如果状态里的队列不存在了，找最近更新的一个
    if not queue:
        queue = PlayQueue.query.filter_by(user_id=current_user.id).order_by(PlayQueue.updated_date.desc()).first()
        if queue:
            logger.info(f"Auto-restored current queue to {queue.id} for user {current_user.id}")
            state.current_queue_id = queue.id
            db.session.commit()

    if not queue:
        # 如果连备胎都没有，返回空结构
        return jsonify({'queue': None, 'state': play_state_to_dict(state)})

    return jsonify({
        'queue': queue_to_dict(queue, include_items=True),
        'state': play_state_to_dict(state)
    })


@app.route('/api/queues/<int:queue_id>/load', methods=['POST'])
@login_required
def load_queue_route(queue_id):
    """切换当前播放队列"""
    queue = PlayQueue.query.filter_by(id=queue_id, user_id=current_user.id).first_or_404()

    state = get_or_create_user_play_state(current_user.id)
    state.current_queue_id = queue.id
    state.current_position = 0
    state.progress = 0
    # 注意：这里不设 is_playing=True，让前端控制是否自动播放

    db.session.commit()
    logger.info(f"User {current_user.id} switched to queue {queue_id}")
    return jsonify({"message": "队列已加载"})


@app.route('/api/queues/<int:queue_id>', methods=['DELETE'])
@login_required
def delete_queue(queue_id):
    """删除指定队列"""
    queue = PlayQueue.query.filter_by(id=queue_id, user_id=current_user.id).first_or_404()

    # 如果删除的是当前队列，重置状态
    state = get_or_create_user_play_state(current_user.id)
    if state.current_queue_id == queue.id:
        state.current_queue_id = None
        state.current_song_id = None
        state.current_position = 0
        state.is_playing = False

    db.session.delete(queue)
    db.session.commit()

    if redis_client:
        redis_client.delete(f"queue:{queue_id}")

    logger.info(f"User {current_user.id} deleted queue {queue_id}")
    return jsonify({"message": "队列已删除"})


@app.route('/api/queue/clear', methods=['POST'])
@login_required
@concurrent_control(lambda: f"queue:{current_user.id}")
def clear_queue():
    """清空队列内容 (不删除队列本身)"""
    data = request.json or {}
    qid = data.get('queue_id')

    # 默认为当前队列
    state = get_or_create_user_play_state(current_user.id)
    if not qid:
        qid = state.current_queue_id

    if not qid:
        return jsonify({"error": "无活动队列"}), 400

    # 验证权限
    queue = PlayQueue.query.filter_by(id=qid, user_id=current_user.id).first()
    if not queue:
        return jsonify({"error": "队列不存在"}), 404

    # 清空项目
    count = PlayQueueItem.query.filter_by(queue_id=qid).delete()

    # 重置状态
    if state.current_queue_id == qid:
        state.current_song_id = None
        state.current_position = 0
        state.progress = 0
        state.is_playing = False

    queue.updated_date = datetime.now(timezone.utc)
    db.session.commit()

    logger.info(f"User {current_user.id} cleared queue {qid} ({count} items)")
    return jsonify({"message": "队列已清空", "deleted_count": count})


@app.route('/api/queues/<int:queue_id>/song', methods=['POST'])
@login_required
@limiter.exempt
@concurrent_control(lambda queue_id: f"queue:{current_user.id}")
def add_song_to_queue(queue_id):
    """添加歌曲到队列 (核心逻辑)"""
    data = request.json or {}
    song_id = data.get('song_id')
    position = data.get('position')  # 前端传入的目标位置 (通常是插队位置)

    if not song_id: return jsonify({"error": "缺少歌曲ID"}), 400

    song = db.session.get(Song, song_id)
    if not song: return jsonify({"error": "歌曲不存在"}), 404

    # 1. 确定目标队列
    target_queue = None
    if queue_id == 0:
        # 0 代表当前队列
        state = get_or_create_user_play_state(current_user.id)
        if state.current_queue_id:
            target_queue = db.session.get(PlayQueue, state.current_queue_id)
    else:
        target_queue = PlayQueue.query.filter_by(id=queue_id, user_id=current_user.id).first()

    # 2. 如果没有队列，自动创建 (含数量限制保护)
    if not target_queue:
        user_queues = PlayQueue.query.filter_by(user_id=current_user.id).all()
        state = get_or_create_user_play_state(current_user.id)

        # 数量限制 Max 5
        if len(user_queues) >= 5:
            # 保护正在播放的队列不被删除
            current_playing_id = state.current_queue_id
            candidates = [q for q in user_queues if q.id != current_playing_id]

            # 如果所有队列都在忙(理论不可能)或为空，则用全集
            target_list = candidates if candidates else user_queues

            if target_list:
                oldest = min(target_list, key=lambda q: q.created_date)
                logger.info(f"Auto-deleting oldest queue: {oldest.id}")
                db.session.delete(oldest)
                db.session.flush()

        # 创建新队列
        target_queue = PlayQueue(
            user_id=current_user.id,
            name=data.get('new_queue_name', '默认播放队列'),
            is_saved=True
        )
        db.session.add(target_queue)
        db.session.flush()

        # 设为当前
        state.current_queue_id = target_queue.id
        state.current_position = 0
        state.progress = 0

    # 3. 查重跳转逻辑
    # 检查这首歌是否已经在队列里了
    existing_item = PlayQueueItem.query.filter_by(queue_id=target_queue.id, song_id=song_id).first()

    if existing_item:
        # 如果存在，直接跳转播放，不再添加
        state = get_or_create_user_play_state(current_user.id)
        state.current_queue_id = target_queue.id
        state.current_song_id = song_id
        # 修正索引：DB(1-based) -> Array(0-based)
        state.current_position = max(0, existing_item.position - 1)
        state.is_playing = True
        state.updated_date = datetime.now(timezone.utc)

        db.session.commit()
        logger.info(f"Song {song_id} exists, jumping to pos {existing_item.position}")

        return jsonify({
            "action": "jump_to_existing",
            "position": existing_item.position,
            "queue_id": target_queue.id
        })

    # 4. 插入新歌
    # 计算最大位置
    max_pos = db.session.query(db.func.max(PlayQueueItem.position)).filter_by(
        queue_id=target_queue.id
    ).scalar() or 0

    # 确定插入位置：如果没传 position，默认插到最后
    target_pos = max_pos + 1 if position is None else min(position, max_pos + 1)

    # 如果是插队(插在中间)，需要把后面的都往后挪
    if target_pos <= max_pos:
        PlayQueueItem.query.filter(
            PlayQueueItem.queue_id == target_queue.id,
            PlayQueueItem.position >= target_pos
        ).update(
            {PlayQueueItem.position: PlayQueueItem.position + 1},
            synchronize_session=False
        )

    item = PlayQueueItem(
        queue_id=target_queue.id,
        song_id=song_id,
        position=target_pos
    )
    db.session.add(item)
    target_queue.updated_date = datetime.now(timezone.utc)

    db.session.commit()

    if redis_client:
        redis_client.delete(f"queue:{target_queue.id}")

    return jsonify({
        "action": "added",
        "item_id": item.id,
        "queue_id": target_queue.id,
        "song_info": song_to_dict(song)  # 关键：返回完整信息供前端更新
    })


@app.route('/api/queue/items/<int:item_id>', methods=['DELETE'])
@login_required
def remove_queue_item(item_id):
    """移除队列中的单首歌曲"""
    item = db.session.get(PlayQueueItem, item_id)
    if not item or item.play_queue.user_id != current_user.id:
        return jsonify({"error": "无效或无权操作"}), 404

    qid = item.queue_id
    pos = item.position

    db.session.delete(item)

    # 调整后续位置 (填补空缺)
    PlayQueueItem.query.filter(
        PlayQueueItem.queue_id == qid,
        PlayQueueItem.position > pos
    ).update(
        {PlayQueueItem.position: PlayQueueItem.position - 1},
        synchronize_session=False
    )

    db.session.commit()
    return jsonify({"message": "已删除"})


@app.route('/api/queue/items/reorder', methods=['POST'])
@login_required
def reorder_queue():
    """拖拽排序更新"""
    data = request.json or {}
    song_ids = data.get('song_ids', [])
    qid = data.get('queue_id')

    if not song_ids or not qid: return jsonify({"error": "参数错误"}), 400

    queue = PlayQueue.query.filter_by(id=qid, user_id=current_user.id).first_or_404()

    # 批量更新位置逻辑 (使用库存池处理重复歌曲)
    current_items = PlayQueueItem.query.filter_by(queue_id=qid).order_by(PlayQueueItem.position).all()

    # 构建池子 {song_id: [item1, item2]}
    pool = defaultdict(list)
    for i in current_items: pool[i.song_id].append(i)

    new_pos = 1
    # 1. 先分配前端传来的顺序
    for sid in song_ids:
        if pool[sid]:
            item = pool[sid].pop(0)
            item.position = new_pos
            new_pos += 1

    # 2. 处理剩余未匹配的 (防止数据丢失)
    for items in pool.values():
        for item in items:
            item.position = new_pos
            new_pos += 1

    queue.updated_date = datetime.now(timezone.utc)
    db.session.commit()

    if redis_client:
        redis_client.delete(f"queue:{qid}")

    return jsonify({"message": "OK"})


@app.route('/api/queues/from-playlist/<int:playlist_id>', methods=['POST'])
@login_required
def create_queue_from_playlist(playlist_id):
    """播放歌单 (创建新队列)"""
    pl = db.session.get(Playlist, playlist_id)
    if not pl: return jsonify({"error": "歌单不存在"}), 404

    if not pl.is_public and pl.user_id != current_user.id:
        return jsonify({"error": "无权访问"}), 403

    # 1. 检查上限 & 保护当前播放
    state = get_or_create_user_play_state(current_user.id)
    user_queues = PlayQueue.query.filter_by(user_id=current_user.id).all()

    if len(user_queues) >= 5:
        candidates = [q for q in user_queues if q.id != state.current_queue_id]
        # 兜底
        targets = candidates if candidates else user_queues
        if targets:
            oldest = min(targets, key=lambda x: x.created_date)
            logger.info(f"Auto-deleting oldest queue for playlist play: {oldest.id}")
            db.session.delete(oldest)
            db.session.flush()

    # 2. 创建新队列
    nq = PlayQueue(
        user_id=current_user.id,
        name=f"播放: {pl.name}",
        is_saved=True
    )
    db.session.add(nq)
    db.session.flush()

    # 3. 批量复制歌曲
    pl_songs = PlaylistSong.query.filter_by(playlist_id=playlist_id).order_by(PlaylistSong.position).all()
    for i, ps in enumerate(pl_songs):
        db.session.add(PlayQueueItem(
            queue_id=nq.id,
            song_id=ps.song_id,
            position=i + 1
        ))

    # 4. 更新状态
    state.current_queue_id = nq.id
    state.current_position = 0
    state.is_playing = True
    state.updated_date = datetime.now(timezone.utc)

    if pl_songs:
        state.current_song_id = pl_songs[0].song_id

    db.session.commit()

    logger.info(f"Created queue {nq.id} from playlist {playlist_id}")
    return jsonify({
        "message": "OK",
        "queue_id": nq.id,
        "song_count": len(pl_songs)
    })


@app.route('/api/queue/state', methods=['PUT'])
@login_required
@limiter.exempt  # 豁免高频同步
def update_queue_state_route():
    """同步播放状态 (进度、播放/暂停)"""
    d = request.json or {}
    s = get_or_create_user_play_state(current_user.id)

    if 'current_song_id' in d: s.current_song_id = d['current_song_id']
    if 'current_position' in d: s.current_position = d['current_position']
    if 'progress' in d: s.progress = d['progress']
    if 'is_playing' in d: s.is_playing = d['is_playing']
    if 'current_queue_id' in d and d['current_queue_id']: s.current_queue_id = d['current_queue_id']
    if 'play_mode' in d: s.play_mode = d['play_mode']

    s.updated_date = datetime.now(timezone.utc)
    db.session.commit()
    return jsonify({"message": "OK"})


@app.route('/api/queue/playmode', methods=['PUT'])
@login_required
def set_play_mode_route():
    """设置播放模式"""
    mode = request.json.get('play_mode', 'sequential')
    s = get_or_create_user_play_state(current_user.id)
    s.play_mode = mode
    db.session.commit()
    return jsonify({"message": "OK"})


# =========================================================================
# 其他路由与系统初始化 (Misc & Initialization)
# =========================================================================

@app.errorhandler(429)
def ratelimit_handler(e):
    return jsonify({"error": "请求过于频繁，请稍后再试"}), 429


@app.errorhandler(404)
def not_found_error(error):
    return jsonify({"error": "资源未找到"}), 404


@app.errorhandler(500)
def internal_error(error):
    db.session.rollback()
    logger.error(f"Server Error: {error}", exc_info=True)
    return jsonify({"error": "服务器内部错误"}), 500


@app.route('/health')
def health_check():
    """系统健康检查"""
    try:
        # 检查数据库连接
        db.session.execute(text('SELECT 1'))

        # 检查 Redis 连接
        redis_status = False
        if redis_client:
            redis_status = redis_client.ping()

        return jsonify({
            "status": "healthy",
            "database": "connected",
            "cache": "connected" if redis_status else "disconnected"
        })
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        return jsonify({"status": "unhealthy", "error": str(e)}), 503


@app.route('/api/stats')
@cache_response(time_out=300)  # 交给装饰器自动缓存 5 分钟
def get_stats():
    """获取系统统计信息 (首页仪表盘)"""

    total_songs = Song.query.filter_by(is_public=True).count()
    total_plays = db.session.query(db.func.sum(Song.play_count)).scalar() or 0
    total_users = User.query.count()

    # 获取在线用户数（最近 5 分钟有活动的用户）
    online_threshold = datetime.now(timezone.utc) - timedelta(minutes=5)
    online_users = UserSession.query.filter(
        UserSession.last_activity >= online_threshold
    ).distinct(UserSession.user_id).count()

    # 热门歌曲 TOP 10
    popular_songs = Song.query.filter_by(is_public=True).order_by(
        Song.play_count.desc()
    ).limit(10).all()

    return jsonify({
        'total_songs': total_songs,
        'total_plays': total_plays,
        'total_users': total_users,
        'online_users': online_users,
        'popular_songs': [{
            'id': s.id,
            'title': s.title,
            'artist': s.artist,
            'play_count': s.play_count,
            'cover_url': get_full_url(s.cover_url)  # 首页也需要封面
        } for s in popular_songs]
    })


# =========================================================================
# Main Entry
# =========================================================================

def init_db():
    """初始化数据库表"""
    with app.app_context():
        try:
            db.create_all()
            logger.info("Database tables created successfully")
        except Exception as e:
            logger.error(f"Error creating database tables: {e}")


if __name__ == '__main__':
    init_db()
    app.run(debug=True, host='127.0.0.1', port=5000)

