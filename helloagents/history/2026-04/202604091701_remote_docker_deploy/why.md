# 变更提案: 远程服务器 Docker 一键部署

## 需求背景
当前仓库已具备前端、后端和数据库初始化脚本，但 Docker 相关文件为空，且后端默认依赖 `localhost` 与硬编码敏感配置，无法直接部署到远程服务器。

## 变更内容
1. 补齐前端、后端镜像构建文件和根目录 Compose 编排文件。
2. 将后端运行依赖切换为环境变量注入。
3. 新增远程部署所需的前端 Nginx 代理配置、示例环境变量和构建忽略规则。

## 影响范围
- **模块:** frontend、backend、deployment
- **文件:** `docker-compose.yml`、`frontend/Dockerfile`、`backend/Dockerfile`、`backend/whu-treehole-server/src/main/resources/application.yml` 等
- **API:** 无新增业务 API，新增前端代理 `/api/v1` 和 `/actuator`
- **数据:** 继续使用 `docker/se_project.sql` 初始化 MySQL

## 核心场景

### 需求: 远程服务器一键部署
**模块:** deployment
仓库在远程 Linux 服务器上应可通过单条 Docker Compose 命令完成首次部署。

#### 场景: 首次拉起完整环境
服务器安装 Docker 与 Compose 后，在仓库根目录执行命令即可完成部署。
- 自动构建前后端镜像
- 自动启动 MySQL 和 Redis
- 前端可通过公网端口访问，后端通过反向代理访问

### 需求: 容器内依赖解耦
**模块:** backend
后端不应再依赖仓库中的本地地址和明文凭据。

#### 场景: 容器启动连接依赖
后端在 Docker 网络中应可使用服务名连接依赖。
- 数据库主机、端口、账号与密码可由环境变量覆盖
- Redis 和 SMTP 配置可由环境变量覆盖
- SQL 初始化模式可切换，避免重复导入

## 风险评估
- **风险:** MySQL 初始化脚本与 Spring SQL 初始化可能重复执行；远程服务器未配置 SMTP 时认证邮件发送失败。
- **缓解:** Compose 默认关闭 Spring SQL 初始化，并默认启用邮箱模拟发送；实际生产可通过 `.env` 覆盖。
