# 音乐播放器（vue_musicplayer）

基于 **Vue 3 + Spring Boot** 的全栈音乐播放器。支持歌曲上传/管理、歌单、播放队列、歌词、播放模式（顺序/单曲/随机）、用户系统（注册/登录/个人资料/登出），后端使用 JWT + 内存黑名单实现安全的登出失效。

> 最后一次大规模重构：**2026-08-09**。完整重构历程见文末 [重构历程](#重构历程)。

---

## 技术栈

| 端 | 技术 |
|---|---|
| 前端 `web/` | Vue 3 · Vite · TypeScript · Pinia · Element Plus · Tailwind CSS · pnpm |
| 后端 `backend/` | Java 21 · Spring Boot · MyBatis-Plus · JWT（登出黑名单） · Maven |
| 基础设施 | MySQL 8 · Nginx · Docker Compose |

## 目录结构

```
vue_musicplayer/
├── web/                 # 前端
│   ├── src/
│   │   ├── api/         # axios 封装（去重/错误集中处理）+ 领域 API
│   │   ├── common/      # 公共组件（PageContainer 等）+ composables（useAsyncTask 等）
│   │   ├── components/  # 播放器外壳（导航/播放栏/队列抽屉/歌曲详情）
│   │   ├── composables/ # player 拆分出的音频引擎/歌词/队列变更
│   │   ├── stores/      # Pinia（player / user / song）
│   │   ├── utils/       # 工具函数（format / crypto / storage / lrcParser）
│   │   ├── views/       # 页面（每页独立 index.vue + const.ts + components/ + composables/）
│   │   └── router/
│   ├── .env.development / .env.production / .env.example
│   └── vite.config.ts   # 开发代理：/api、/static → 127.0.0.1:8080
├── backend/             # Spring Boot 后端（Java 21）
│   └── src/main/java/com/kyf/mp/javaserver/
│       ├── common/      # 统一异常、JWT 过滤器、Token 黑名单
│       ├── config/      # 安全配置、静态资源映射（/static/** 兜底）
│       └── modules/     # 按业务分包（Song / Playlist / Queue / User）
├── mysql/               # MySQL 初始化 SQL（init.sql）与数据卷
├── nginx/               # Nginx 反向代理（只跟踪 Dockerfile + conf）
├── static/              # 用户上传资源（歌曲/封面，不入库，由 nginx 直接读取）
└── Docker-compose.yml   # MySQL + 后端 + Nginx 全栈编排
```

## 快速开始

### 开发环境

```bash
# 1. 起基础设施（MySQL + 后端 + nginx 走 docker，nginx 暴露在宿主机 8080）
docker compose up -d

# 2. 前端 dev server（vite，端口 5173）
cd web
pnpm install
pnpm dev
```

访问 `http://localhost:5173`。开发时代理链路：

```
浏览器(5173) ──> Vite 代理 ──> 127.0.0.1:8080（docker nginx）
                  /api/*        → proxy_pass 到 backend-server
                  /static/*     → nginx 直接读 static/ 卷（性能更好）
```

> 裸跑后端（不用 nginx）时：`cd backend && ./mvnw spring-boot:run`，
> `/static/**` 由后端 [StaticResourceConfig](backend/src/main/java/com/kyf/mp/javaserver/config/StaticResourceConfig.java) 兜底提供（同样支持 Range 断点续传）。

### 生产部署

```bash
docker compose up -d --build
```

- 前端构建产物放入 `nginx/html/`（Nginx 服务 SPA 站点根目录）
- 访问 `http://<host>:8080`

## 环境变量

| 位置 | 文件 | 说明 |
|---|---|---|
| 前端 | [web/.env.example](web/.env.example) | `VITE_API_URL` 留空 = 相对路径（接口走同源 `/api`，静态走同源 `/static`，由 nginx/Vite 代理处理） |
| 后端 | [backend/.../application.example.yml](backend/src/main/resources/application.example.yml) | 全部敏感配置用 `${VAR:default}` 占位，可被环境变量覆盖（`MYSQL_PASSWORD`、`JWT_SECRET` 等） |

## 静态资源链路

```
上传：前端 ──> /api ──> Spring ──> 写入 static/{songs,song_covers,playlist_covers,user_covers}
读取：/static/** ──> nginx alias 直接读取 static/ 卷（主链路，性能好）
      裸跑后端时 ──> Spring /static/** resource handler（兜底）
```

## 重构历程

### 2026-02-28 · 项目诞生
- `d8e8b24` 最初版：Vue3 + Flask + SQLite
- `d916c1c` 用户模块（Pinia + userApi + SHA-256 加密）

### 2026-03 · 前端功能迭代
- `6f6d900` 前端迁移到 TypeScript
- `b22400a` 重做 axios 拦截器错误处理
- `2bc035a` / `c5537de` 调整项目结构
- `0b98597` 用户认证 + 歌曲管理
- `a35c775` 队列/歌单逻辑 + 播放状态持久化
- `c7c4bc2` 标准化数据库列名 + 编写 DDL
- `24f3655` 注册 / 登录 / 登出
- `499f800` 歌曲搜索与展示
- `898d14f` 歌曲上传
- `31d3d4a` 后端 Flask → Node（Express）
- `d3a1efe` 解耦播放控制 + 优化 Element Plus 导入
- `f4a41ff` 用 el-menu 优化播放栏
- `d29df67` 优化播放进度条（含缓冲进度）
- `8cc57b5` / `5891bd5` 歌曲呼吸感、歌单信息编辑
- `4fa6683` / `b80ae03` / `ee7d3fe` 歌词上传、后端取词、滚动歌词
- `8f59485` 配置 nginx 代理

### 2026-04 · 后端迁移 Spring Boot + 模块化
- `e6cbda0` 重构为 Spring Boot，完成 user / song 模块
- `49127dd` 全局错误处理器 + 歌单模块
- `677d030` 按模块重构（mapper / service / controller 分层）
- `a8c192d` 歌曲编辑 + 个人资料模块
- `91e39e7` 从 Git 追踪移除环境变量文件

### 2026-04-21 · Docker 部署
- `a3032ac` 实现 docker 部署（MySQL + 后端 + Nginx）

### 2026-05-20
- `27ae97c` 加注释并小幅优化冗余逻辑

### 2026-07-18
- `9d3e811` 更新修改建议

### 2026-08-09 · 大规模工业化重构（19 个提交）

**后端安全加固**
- `1771f76` 升级 JWT 认证依赖
- `dd35a70` 改进全局异常响应
- `76290bf` 统一安全错误响应
- `4e20ba2` 加固 JWT 认证过滤器
- `90e16f1` 实现安全登出 + JWT 内存黑名单失效

**前端架构对齐工业化**
- `6b29629` 提交待处理的本地改动
- `da59267` 错误处理集中化：axios 拦截器统一弹错，组件只弹成功提示（silent 标志）
- `b77a5de` 分层架构：新增 `common/` 组件与 composables 层
- `74dceb5` 拆解 playlist / user 页面模块
- `c6ef480` 每个页面独立成模块文件夹（index.vue + const.ts + components/ + composables/）
- `7427ee2` 统一命名规范
- `32e998b` 播放器组件拆分为子模块 + 恢复 auto-import
- `889a3fb` 后台请求走 silent，消除未处理的 Promise 拒绝
- `884d276` player store（约 800 行）拆分为音频引擎 / 歌词 / 队列变更三个 composable
- `1ee7e24` playlist 全面类型化 + 抽取 usePlaylistDetail + 合并 utils
- `e83a4c5` player 队列同步类型化 + 复用 getImageUrl

**基础设施与工程化**
- `5597174` 修复未入库文件（application.yml、docker-compose、nginx Dockerfile 曾被 gitignore 误伤）+ 重写 .gitignore
- `1646e8a` 前端配置精简（移除死测试脚手架与死依赖）、npm → pnpm、修复 dev/prod 后端访问与 Vite 代理
- `325b370` 只保留 Java 后端（删除 Flask / Node 旧后端），目录重构（`client`→`web`、`javaserver`→`backend` 扁平化、`nginx-1.26.3`→`nginx`），静态资源移至根 `static/`，后端增加 `/static/**` 兜底映射
