# 音乐播放器（vue_musicplayer）

基于 **Vue 3 + Spring Boot** 的全栈音乐播放器。支持歌曲上传/管理、歌单、播放队列、歌词、播放模式（顺序/单曲/随机）、用户系统（注册/登录/个人资料/登出），后端使用 JWT + Redis 黑名单实现安全且可持久化的登出失效。

> 最后一次大规模重构：**2026-08-09**。完整重构历程见文末 [重构历程](#重构历程)。

---

## 技术栈

| 端 | 技术 |
|---|---|
| 前端 `web/` | Vue 3 · Vite · TypeScript · Pinia · Element Plus · Tailwind CSS · pnpm |
| 后端 `backend/` | Java 21 · Spring Boot · MyBatis-Plus · JWT（Redis 登出黑名单） · Maven |
| 基础设施 | MySQL 8 · Redis 7 · Nginx · Docker Compose |

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
│   └── src/main/java/com/kyf/mp/server/
│       ├── common/      # 统一异常、JWT 过滤器、Token 黑名单
│       ├── config/      # 安全配置、静态资源映射（/static/** 兜底）
│       └── modules/     # 按业务分包（Song / Playlist / Queue / User）
├── mysql/               # MySQL 数据卷
├── nginx/               # Nginx 反向代理（只跟踪 Dockerfile + conf）
├── static/              # 用户上传资源（歌曲/封面，不入库，由 nginx 直接读取）
└── compose.yml   # MySQL + 后端 + Nginx 全栈编排
```

## 快速开始

### 开发环境

```bash
# 1. 起基础设施（MySQL + 后端 + nginx 走 docker，nginx 暴露在宿主机 8080）
#    首次启动或改过 Dockerfile/nginx.conf 后加 --build；镜像会自行构建前端与后端。
docker compose up -d --build

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
> `/static/**` 由后端 [StaticResourceConfig](backend/src/main/java/com/kyf/mp/server/config/StaticResourceConfig.java) 兜底提供（同样支持 Range 断点续传）。

### 生产部署

```bash
docker compose up -d --build
```

- nginx 镜像在构建阶段自动执行前端构建，并内置产物；无需手工复制 `web/dist`。
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

## 注意事项

### 包管理
- 前端用 **pnpm**（`packageManager` 已锁定 `pnpm@11.17.0`），**不要用 npm** 安装依赖。esbuild 的构建脚本已在 `pnpm-workspace.yaml`（`allowBuilds`）里放行。

### 后端部署
- 后端 Dockerfile 使用多阶段构建：Docker 会在构建阶段执行 Maven 打包，无需在宿主机预先生成 `target/*.jar`。
- 后端不提供数据库密码或 JWT 密钥默认值；裸跑时复制 `backend/.env.example` 为 `backend/.env` 并填写，Docker 部署时在根 `.env` 中填写 `MYSQL_ROOT_PASSWORD`、`MYSQL_APP_PASSWORD` 和 `JWT_SECRET`。

### 端口
- docker 的 nginx 占用宿主机 **8080**；裸跑后端（Spring Boot 默认也是 8080）会**端口冲突**。裸跑调试时改 `SERVER_PORT` 环境变量，或停掉 nginx 容器。

### 静态资源
- `static/` 是用户上传数据，**不入库**。首次部署克隆后为空目录：docker 会自动创建挂载目录，上传时后端用 `mkdirs()` 自动建 `songs/` 等子目录。
- 修改 `nginx/conf/nginx.conf` 后，运行中的容器仍是旧配置，必须**重建镜像**才生效：`docker compose up -d --build nginx`。

### 数据库迁移
- 表结构由 Flyway 管理；初始结构为 `backend/src/main/resources/db/migration/V1__initial_schema.sql`。
- 已有数据库首次启动会自动 baseline 到 V1，不会重复建表。后续结构变更必须新增 `V2__说明.sql`、`V3__说明.sql` 等迁移文件，禁止修改已执行的版本文件。
- Docker 仅通过 `MYSQL_DATABASE` 创建空数据库；所有表结构和增量变更均由 Flyway 管理。
- `cd backend && ./mvnw test` 会通过 Testcontainers 在临时 MySQL 8 实例上验证全部迁移；需要本机 Docker 运行中。

### 开发工具
- `CodeGenerator` 需要环境变量 `GENERATOR_DB_URL`、`GENERATOR_DB_USERNAME` 和 `GENERATOR_DB_PASSWORD`；可选 `GENERATOR_OUTPUT_DIR`，默认 `src/main/java`。凭据不再写入源码。
- `DbdataInit <songs-directory> <song-covers-directory>` 目前仅校验并扫描媒体目录，供后续实现本地文件元数据导入使用；它不会启动服务或写入数据库。

### 前端开发
- dev 下 Vite 把 `/api`、`/static` 代理到 `127.0.0.1:8080`（docker nginx）。**docker 没启动时接口会 502**。
- `VITE_API_URL` 留空 = 相对路径，依赖 nginx / Vite 代理；如果前端裸跑（无代理），图片与接口会 404。
- 生产镜像会自动构建并托管前端 SPA，部署仅需 `docker compose up -d --build`。


## 重构历程

> Note: commit hashes were intentionally omitted because repository history was rewritten during the security cleanup.

### 2026-02-28 · Project origin
-  最初版：Vue3 + Flask + SQLite
-  用户模块（Pinia + userApi + SHA-256 加密）

### 2026-03 · 前端功能迭代
-  前端迁移到 TypeScript
-  重做 axios 拦截器错误处理
-  调整项目结构
-  用户认证 + 歌曲管理
-  队列/歌单逻辑 + 播放状态持久化
-  标准化数据库列名 + 编写 DDL
-  注册 / 登录 / 登出
-  歌曲搜索与展示
-  歌曲上传
-  后端 Flask → Node（Express）
-  解耦播放控制 + 优化 Element Plus 导入
-  用 el-menu 优化播放栏
-  优化播放进度条（含缓冲进度）
-  歌曲呼吸感、歌单信息编辑
-  歌词上传、后端取词、滚动歌词
-  配置 nginx 代理

### 2026-04 · 后端迁移 Spring Boot + 模块化
-  重构为 Spring Boot，完成 user / song 模块
-  全局错误处理器 + 歌单模块
-  按模块重构（mapper / service / controller 分层）
-  歌曲编辑 + 个人资料模块
-  从 Git 追踪移除环境变量文件

### 2026-04-21 · Docker 部署
-  实现 docker 部署（MySQL + 后端 + Nginx）

### 2026-05-20
-  加注释并小幅优化冗余逻辑

### 2026-07-18
-  更新修改建议

### 2026-08-09 · 大规模工业化重构（23 个提交）

**后端安全加固**
-  升级 JWT 认证依赖
-  改进全局异常响应
-  统一安全错误响应
-  加固 JWT 认证过滤器
-  实现安全登出 + JWT 内存黑名单失效

**前端架构对齐工业化**
-  提交待处理的本地改动
-  错误处理集中化：axios 拦截器统一弹错，组件只弹成功提示（silent 标志）
-  分层架构：新增 `common/` 组件与 composables 层
-  拆解 playlist / user 页面模块
-  每个页面独立成模块文件夹（index.vue + const.ts + components/ + composables/）
-  统一命名规范
-  播放器组件拆分为子模块 + 恢复 auto-import
-  后台请求走 silent，消除未处理的 Promise 拒绝
-  player store（约 800 行）拆分为音频引擎 / 歌词 / 队列变更三个 composable
-  playlist 全面类型化 + 抽取 usePlaylistDetail + 合并 utils
-  player 队列同步类型化 + 复用 getImageUrl

**基础设施与工程化**
-  修复未入库文件（application.yml、docker-compose、nginx Dockerfile 曾被 gitignore 误伤）+ 重写 .gitignore
-  前端配置精简（移除死测试脚手架与死依赖）、npm → pnpm、修复 dev/prod 后端访问与 Vite 代理
-  只保留 Java 后端（删除 Flask / Node 旧后端），目录重构（`client`→`web`、`javaserver`→`backend` 扁平化、`nginx-1.26.3`→`nginx`），静态资源移至根 `static/`，后端增加 `/static/**` 兜底映射
-  重写 README（完整重构历程 + 运行说明），nginx `/static` 改回 alias 直接读 static/ 卷（性能更好），Spring 静态路由仅作兜底
-  删除无主的根 package-lock.json（对应 package.json 早已删除）
-  README 补充注意事项（后端需先 `./mvnw package`、改 nginx 配置需重建镜像、8080 端口冲突、静态资源不入库等）

**样式规范**
-  前端样式重构：模板 Tailwind 原子类 → 语义化 BEM 类名，样式收进 `<style scoped>`（顶部 `@reference` + `@apply` 迁移原原子类），`hover`/`focus`/`group-hover` 等变体改写为纯 CSS 选择器，动态 `:class` 原子条件改为 `is-active` 等语义类。涉及 31 个 `.vue` 文件

> Docker Compose creates the `music_app` database user only when MySQL initializes an empty data directory. For an existing volume, provision that user manually or recreate the empty development volume before switching the backend credentials.
