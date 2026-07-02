# SE_project

武汉大学树洞项目的全栈代码仓库，包含 React 前端、Spring Boot 多模块后端，以及基于 Docker Compose 的一键部署配置。

## 项目概览

- 前端提供树洞首页、话题页、校友圈、个人主页、消息等页面。
- 后端提供 `/api/v1` 接口、邮箱验证码认证、内容聚合与基础互动能力。
- 基础设施使用 MySQL 8 和 Redis 7。
- 部署方式以 Docker / Docker Compose 为主，也支持前后端本地分开启动。

## 技术栈

### 前端
- React 18
- TypeScript 5
- Vite 5
- React Router 6
- Nginx（生产环境静态资源托管与反向代理）

### 后端
- Java 17
- Spring Boot 3.3
- Maven 3.9+
- MyBatis

### 基础设施与部署
- MySQL 8
- Redis 7
- Docker
- Docker Compose

## 目录结构

```text
.
├─ frontend/                 # React + Vite 前端
├─ backend/                  # Spring Boot 多模块后端
│  ├─ whu-treehole-common/   # 通用响应体、异常定义
│  ├─ whu-treehole-domain/   # DTO、请求体、领域模型
│  ├─ whu-treehole-infra/    # MyBatis Mapper、DO、SQL 映射
│  └─ whu-treehole-server/   # 启动模块、Controller、Service、配置
├─ docker/                   # 数据库初始化脚本等部署资源
├─ docs/                     # 设计文档与实现计划
├─ helloagents/              # 项目知识与过程文档
└─ docker-compose.yml        # 一键部署编排文件
```

## 核心能力

- 邮箱验证码注册与登录
- 首页、话题、校友圈、个人主页等页面聚合接口
- 发帖、点赞、收藏等基础互动
- 会话消息读取与发送
- 评论搜索设计与 RBAC / moderation 设计文档沉淀

## 快速开始

### 方式一：Docker Compose 一键部署

这是当前最直接的启动方式。

#### 1. 准备环境

- Docker
- Docker Compose

#### 2. 配置环境变量

在仓库根目录创建 `.env`，可直接参考 `.env.example`：

```bash
cp .env.example .env
```

建议至少修改以下配置，避免直接使用示例值：

- `MYSQL_ROOT_PASSWORD`
- `REDIS_PASSWORD`
- `TREEHOLE_MAIL_USERNAME`
- `TREEHOLE_MAIL_PASSWORD`
- `FRONTEND_PORT`

如果本地只做联调，不需要真实发信，可设置：

```env
TREEHOLE_AUTH_MOCK_EMAIL_ENABLED=true
```

#### 3. 启动服务

```bash
docker compose up -d --build
```

#### 4. 访问项目

- 前端首页：`http://localhost:${FRONTEND_PORT}`
- 后端健康检查：`http://localhost:${FRONTEND_PORT}/actuator/health`

如果未修改 `.env`，默认前端端口为 `80`，即：

- `http://localhost`

#### 5. 查看日志

```bash
docker compose logs -f
```

#### 6. 停止服务

```bash
docker compose down
```

如需连同数据卷一起删除：

```bash
docker compose down -v
```

## 本地开发

### 环境要求

- Node.js 20+
- npm
- JDK 17
- Maven 3.9+
- MySQL 8
- Redis 7

### 启动后端

1. 启动本地 MySQL 和 Redis。
2. 按需设置环境变量，常用项包括：
   - `TREEHOLE_DB_HOST`
   - `TREEHOLE_DB_PORT`
   - `TREEHOLE_DB_NAME`
   - `TREEHOLE_DB_USERNAME`
   - `TREEHOLE_DB_PASSWORD`
   - `TREEHOLE_REDIS_HOST`
   - `TREEHOLE_REDIS_PORT`
   - `TREEHOLE_REDIS_PASSWORD`
   - `TREEHOLE_AUTH_MOCK_EMAIL_ENABLED`
   - `TREEHOLE_SQL_INIT_MODE`
3. 首次本地启动建议使用：

```env
TREEHOLE_SQL_INIT_MODE=always
TREEHOLE_AUTH_MOCK_EMAIL_ENABLED=true
```

4. 在 `backend/` 目录运行：

```bash
mvn -q -s settings.xml -pl whu-treehole-server -am spring-boot:run
```

说明：

- 默认使用 `local` profile。
- `application-local.yml` 会在 `TREEHOLE_SQL_INIT_MODE=always` 时自动加载 `db/schema.sql` 和 `db/data.sql`。
- 若启用 mock 邮件模式，验证码会打印在后端日志中。

### 启动前端

在 `frontend/` 目录运行：

```bash
npm install
npm run dev
```

常用命令：

```bash
npm run build
npm run test
```

前端默认通过构建时注入的 `VITE_API_BASE_URL` 访问后端，生产容器中默认值为 `/api/v1`。

## 生产部署说明

- `frontend` 容器基于 Nginx 提供静态资源，并将 `/api/v1` 和 `/actuator` 代理到 `backend:8080`
- `backend` 容器启动时通过环境变量注入数据库、Redis、SMTP 配置
- `mysql` 容器首次启动时会自动执行 `docker/se_project.sql`
- `redis` 容器启用了 AOF 持久化

部署入口文件：

- 根目录 [docker-compose.yml](/D:/Desktop/SE_task/docker-compose.yml)
- 后端 Dockerfile: [backend/Dockerfile](/D:/Desktop/SE_task/backend/Dockerfile)
- 前端 Dockerfile: [frontend/Dockerfile](/D:/Desktop/SE_task/frontend/Dockerfile)

## 常用接口

- `POST /api/v1/auth/email-code`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `GET /api/v1/pages/home`
- `GET /api/v1/pages/topics`
- `GET /api/v1/pages/alumni`
- `GET /api/v1/pages/profile`
- `POST /api/v1/posts`
- `GET /api/v1/conversations/{conversationCode}`
- `POST /api/v1/conversations/{conversationCode}/messages`

## 测试与构建

### 前端

```bash
cd frontend
npm run build
npm run test
```

### 后端

```bash
cd backend
mvn -q -s settings.xml -pl whu-treehole-server -am test
```

## 相关文档

- 后端说明：[backend/README.md](/D:/Desktop/SE_task/backend/README.md)
- 后端部署说明：[backend/docs/deployment.md](/D:/Desktop/SE_task/backend/docs/deployment.md)
- 项目知识库：[helloagents/wiki/overview.md](/D:/Desktop/SE_task/helloagents/wiki/overview.md)
- 设计文档目录：[docs/superpowers](/D:/Desktop/SE_task/docs/superpowers)

## 注意事项

- 仓库中的 `.env.example` 仅作为示例，不应直接用于生产环境。
- `frontend/dist` 和 `frontend/node_modules` 属于构建产物或依赖目录，不建议作为日常手工变更提交。
- 若 GitHub 首页需要显示“刚刚更新”，必须产生一个新的 commit；单纯重新推送旧 commit，不会改变页面上的“3 months ago”显示。
