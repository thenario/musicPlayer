# music-starter

前后端分离的全栈脚手架模板：**Spring Boot 3 + MyBatis-Plus**（后端）· **Vue 3 + Vite + TypeScript + Pinia + Element Plus**（前端）· MySQL + Nginx + Docker Compose 部署。

内置一个最小可运行的 `user` 模块（注册 / 登录 / JWT / 个人资料）作为「如何新增模块」的样板。

## 特性

**后端**
- 分层：`Controller → Service(接口+实现) → Business(接口+实现) → Mapper`，适合复杂业务
- 通用 CRUD 基类 `BaseBusiness` / `BaseBusinessImpl`（继承 MyBatis-Plus `IService`）
- 全局异常处理 + 统一返回体 `ResultModel`
- Bean Validation 参数校验（注解 + message）
- JWT 认证 + Token 黑名单（登出失效）
- 雪花 ID（`IdType.ASSIGN_ID`），`Long` 全局序列化为字符串（防 JS 精度丢失）
- 本地 `.env` 读取（`DotenvEnvironmentPostProcessor`），密码/密钥不入库

**前端**
- axios 拦截器封装、Pinia、路由守卫
- 通用组件（`AppPagination` / `PageContainer` / `CopyText`）与 composables（`use-async-task` 等）
- `sameId` 等工具（雪花 ID 安全比较）
- 命名规范：`.vue` 大驼峰，`.ts` / 文件夹 kebab-case

## 快速开始

### 1. 生成项目

```bash
./scaffold.sh my-app com.example.myapp
# 生成到 ../my-app，自动替换包名、数据库名、项目名
```

### 2. 配置环境变量

```bash
# 本地裸跑后端（工作目录 = backend/）
cp backend/.env.example backend/.env   # 填 MYSQL_PASSWORD、JWT_SECRET
# Docker 部署（与 Docker-compose.yml 同目录）
cp .env.example .env                    # 填 MYSQL_ROOT_PASSWORD、JWT_SECRET
```

### 3. 启动

```bash
# 本地：先起 MySQL（或 docker compose up db），再
cd backend && mvn spring-boot:run      # 后端 :8080
cd web && pnpm install && pnpm dev     # 前端 :5173（代理 /api → 8080）

# 或 Docker 一键
docker compose up -d --build           # 前端 nginx :8080
```

## 目录结构

```
.
├── backend/                  # Spring Boot 后端
│   └── src/main/java/<pkg>/
│       ├── ServerApplication.java    # 主类（根包，勿移动）
│       ├── common/            # 通用：异常、ResultModel、JWT、BaseBusiness
│       ├── config/            # 配置：Jackson/MyBatisPlus/Security/Redis/Dotenv...
│       ├── modules/user/      # 业务模块（样板）：entity/mapper/service/business/controller/dto/vo/xml
│       ├── tools/             # 开发工具：CodeGenerator（代码生成器）
│       └── utils/             # 运行时工具：JwtUtils...
├── web/                       # Vue3 前端
│   └── src/{api,common,components,composables,router,stores,types,utils,views}
├── mysql/init/init.sql        # 建库建表（雪花 BIGINT 主键）
├── nginx/                     # 反代配置
├── Docker-compose.yml         # db + backend + nginx
├── .env.example               # docker-compose 环境变量模板
└── scaffold.sh                # 脚手架实例化脚本
```

## 后端分层约定

| 层 | 职责 | 示例（user 模块） |
|---|---|---|
| `controller` | 参数校验、打包 `ResultModel` | `UsersController` |
| `service` | 业务编排（接口 + `imp` 实现） | `UsersService` / `UsersServiceImpl` |
| `business` | 数据访问（接口 + `imp` 实现，继承 `BaseBusiness`） | `UsersBusiness` / `UsersBusinessImpl` |
| `mapper` | MyBatis-Plus 接口 + 自定义 XML | `UsersMapper` / `UsersMapper.xml` |

接口不写 `I` 前缀（非 Java 标准），实现类放 `imp` 子包。异常统一抛 `BusinessException`，由全局异常处理器捕获。

## 如何新增一个业务模块

以「文章 article」为例：

1. **建表**：在 `mysql/init/init.sql` 增加 `articles` 表（主键 `article_id` bigint，雪花）
2. **生成代码**：改 `backend/.../tools/CodeGenerator.java` 的 `addInclude("articles")`，运行 main 生成 entity/mapper/service/controller
3. **补分层**：按 `user` 模块的样子补齐 `business`（接口 + `imp`）与 `service` 实现，Controller 里用 `@Validated` + 校验注解，Service 里编排、Business 里访问数据
4. **前端**：在 `web/src/api` 加 `article-api.ts`，`views/article/` 建页面 + `composables`，`router` 注册路由，`types` 加类型

## 配置说明

- **`.env` 优先级**：真实环境变量 > `.env` 文件 > `application.yml` 默认值。本地裸跑用 `backend/.env`，Docker 用 compose 注入环境变量。
- **雪花 ID**：后端 `Long` 统一序列化为字符串；前端 ID 类型用 `number | string`，比较用 `sameId()`。
- **静态资源**：开发走 Vite 代理 `/api`、`/static`；生产走 Nginx 同源转发。

## 部署

```bash
cp .env.example .env   # 填密码
docker compose up -d --build
```
