import os
from flask_sqlalchemy import SQLAlchemy
from flask_login import UserMixin
from datetime import datetime, timezone, UTC
import jwt
import time
from werkzeug.security import generate_password_hash, check_password_hash

db = SQLAlchemy()


class User(UserMixin,db.Model):
    __tablename__ = 'users'

    id = db.Column(db.Integer, primary_key=True)
    user_name = db.Column(db.String(40), unique=True, nullable=False, index=True)
    email = db.Column(db.String(30), unique=True, nullable=False, index=True)
    password_hash = db.Column(db.String(225))
    created_at = db.Column(db.DateTime, default=datetime.now(UTC))
    is_active = db.Column(db.Boolean, default=True)
    last_login = db.Column(db.DateTime)

    playlists = db.relationship('Playlist', backref='user', lazy='dynamic', cascade='all,delete-orphan')
    uploaded_songs = db.relationship('Song', backref='uploader', lazy='dynamic')
    liked_playlists = db.relationship('PlaylistLike', backref='user', lazy='dynamic', cascade='all,delete-orphan')

    def set_password(self , password):
        self.password_hash = generate_password_hash(password)


    def check_password(self,password):
        return check_password_hash(self.password_hash,password)


    def get_reset_token(self,expires_sec=1800):
        return jwt.encode(
            {
                'user_id':self.id,'exp':time.time()+expires_sec
            },
            os.environ.get('SECRET_KEY') or 'secret-key',
            algorithm='HS256'
        )


    @staticmethod
    def verify_reset_token(token):
        try:
            user_id = jwt.decode(
                token,
                os.environ.get('SECRET_KEY') or 'secret-key',
                algorithm='HS256'
            )['user_id']
        except:
            return None
        return User.query.get(user_id)


    __table_args__ = (
        db.Index('idx_user_created','created_at'),
     db.Index('idx_user_active','is_active'),
    )


class Song(db.Model):
    __tablename__ = 'songs'
    id = db.Column(db.Integer,primary_key=True)
    title=db.Column(db.String(50),nullable=False,index=True)
    artist=db.Column(db.String(50),index=True)
    album=db.Column(db.String(50),index=True)
    duration=db.Column(db.Integer)
    file_path=db.Column(db.String(200),nullable=False)
    file_name=db.Column(db.String(100),nullable=False)
    file_size=db.Column(db.BigInteger)
    file_format=db.Column(db.String(50))
    bitrate=db.Column(db.Integer)
    genre=db.Column(db.String(100))
    year=db.Column(db.Integer)
    track_number=db.Column(db.Integer)
    uploader_id=db.Column(db.Integer,db.ForeignKey('users.id',ondelete='SET NULL'))
    date_added=db.Column(db.DateTime,default=datetime.now(timezone.utc),index=True)
    play_count=db.Column(db.Integer,default=0,index=True)
    last_played=db.Column(db.DateTime)
    is_public=db.Column(db.Boolean,default=True,index=True)
    cover_url = db.Column(db.String(500))
    lyrics = db.Column(db.Text, nullable=True)

    playlists = db.relationship('PlaylistSong', backref='song', lazy='dynamic', cascade='all, delete-orphan')

    __table_args__ = (
        db.Index('idx_song_title_artist', 'title', 'artist'),
        db.Index('idx_song_upload_date', 'date_added'),
        db.Index('idx_song_public', 'is_public'),
        db.Index('idx_song_popular', 'play_count', 'date_added'),
    )


class Playlist(db.Model):
    __tablename__ = 'playlists'

    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(255), nullable=False, index=True)
    description = db.Column(db.Text)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), nullable=False, index=True)
    created_date = db.Column(db.DateTime, default=datetime.now(timezone.utc), index=True)
    updated_date = db.Column(db.DateTime, default=datetime.now(timezone.utc), onupdate=datetime.now(timezone.utc))
    is_public = db.Column(db.Boolean, default=False, index=True)
    cover_image = db.Column(db.String(500))
    play_count = db.Column(db.Integer, default=0, index=True)
    like_count = db.Column(db.Integer, default=0, index=True)
        # 关系
    songs = db.relationship('PlaylistSong', backref='playlist', lazy='dynamic', cascade='all, delete-orphan')
    tags = db.relationship('Tag', secondary='playlist_tags', backref='playlists', lazy='dynamic')
    likes = db.relationship('PlaylistLike', backref='playlist', lazy='dynamic', cascade='all, delete-orphan')

    __table_args__ = (
        db.Index('idx_playlist_user', 'user_id'),
        db.Index('idx_playlist_public', 'is_public'),
        db.Index('idx_playlist_popular', 'play_count', 'like_count'),
        db.Index('idx_playlist_updated', 'updated_date'),
    )


class PlaylistSong(db.Model):
    __tablename__ = 'playlist_songs'

    playlist_id = db.Column(db.Integer, db.ForeignKey('playlists.id', ondelete='CASCADE'), primary_key=True)
    song_id = db.Column(db.Integer, db.ForeignKey('songs.id', ondelete='CASCADE'), primary_key=True)
    position = db.Column(db.Integer, nullable=False)
    added_date = db.Column(db.DateTime, default=datetime.now(timezone.utc))
    added_by = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='SET NULL'))

    __table_args__ = (
        db.Index('idx_playlist_song_position', 'playlist_id', 'position'),
        db.Index('idx_playlist_song_added', 'added_date'),
    )


class Tag(db.Model):
    __tablename__ = 'tags'

    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(50), unique=True, nullable=False, index=True)
    created_date = db.Column(db.DateTime, default=datetime.now(timezone.utc))

    __table_args__ = (
        db.Index('idx_tag_name', 'name'),
    )


class PlaylistTag(db.Model):
    __tablename__ = 'playlist_tags'

    playlist_id = db.Column(db.Integer, db.ForeignKey('playlists.id', ondelete='CASCADE'), primary_key=True)
    tag_id = db.Column(db.Integer, db.ForeignKey('tags.id', ondelete='CASCADE'), primary_key=True)


class PlaylistLike(db.Model):
    __tablename__ = 'playlist_likes'

    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), primary_key=True)
    playlist_id = db.Column(db.Integer, db.ForeignKey('playlists.id', ondelete='CASCADE'), primary_key=True)
    created_date = db.Column(db.DateTime, default=datetime.now(timezone.utc)) ###实际上是liked_date但是要改的地方比较多就改成了这个

    __table_args__ = (
        db.Index('idx_like_user', 'user_id'),
        db.Index('idx_like_playlist', 'playlist_id'),
    )


class UserSession(db.Model):
    __tablename__ = 'user_sessions'

    id = db.Column(db.String(128), primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), index=True)
    created_at = db.Column(db.DateTime, default=datetime.now(timezone.utc))
    last_activity = db.Column(db.DateTime, default=datetime.now(timezone.utc))
    ip_address = db.Column(db.String(45))
    user_agent = db.Column(db.Text)

    __table_args__ = (
        db.Index('idx_session_user', 'user_id'),
        db.Index('idx_session_activity', 'last_activity'),
    )


# 删除原有的PlayQueue和UserQueueState模型，替换为以下模型

class PlayQueue(db.Model):
    """播放队列"""
    __tablename__ = 'play_queues'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    name = db.Column(db.String(255), default='默认队列')
    is_saved = db.Column(db.Boolean, default=False)  # 是否为已保存的队列
    created_date = db.Column(db.DateTime, default=datetime.now(timezone.utc))
    updated_date = db.Column(db.DateTime, default=datetime.now(timezone.utc), onupdate=datetime.now(timezone.utc))

    # 关系
    user = db.relationship('User', backref='play_queues')
    queue_items = db.relationship('PlayQueueItem', backref='play_queue',
                                  order_by='PlayQueueItem.position',
                                  cascade='all, delete-orphan')


class PlayQueueItem(db.Model):
    __tablename__ = 'play_queue_items'
    id = db.Column(db.Integer, primary_key=True)
    queue_id = db.Column(db.Integer, db.ForeignKey('play_queues.id'), nullable=False)
    song_id = db.Column(db.Integer, db.ForeignKey('songs.id'), nullable=False)
    position = db.Column(db.Integer, nullable=False)
    added_date = db.Column(db.DateTime, default=datetime.now(timezone.utc))

    # 关系
    song = db.relationship('Song')


class UserPlayState(db.Model):
    """用户播放状态（每个用户只有一条记录）"""
    __tablename__ = 'user_play_states'
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), primary_key=True)
    current_queue_id = db.Column(db.Integer, db.ForeignKey('play_queues.id'))
    current_song_id = db.Column(db.Integer, db.ForeignKey('songs.id'))
    current_position = db.Column(db.Integer, default=0)  # 当前在队列中的位置
    progress = db.Column(db.Integer, default=0)  # 当前歌曲播放进度（秒）
    is_playing = db.Column(db.Boolean, default=False)
    play_mode = db.Column(db.String(20), default='sequential')  # sequ-ential, shuffle, repeat_one, repeat_all
    updated_date = db.Column(db.DateTime, default=datetime.now(timezone.utc), onupdate=datetime.now(timezone.utc))

    # 关系
    user = db.relationship('User', backref=db.backref('play_state', uselist=False))
    current_queue = db.relationship('PlayQueue')
    current_song = db.relationship('Song')






