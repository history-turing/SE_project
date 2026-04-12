# 部署说明

## 1. 设计原则
- 仓库中不保存任何真实密码、SMTP 授权码、数据库口令或 Redis 口令。
- 本地开发使用根目录 `.env.local`，服务器部署使用根目录 `.env.production`。
- GitHub Actions 只负责拉代码并执行仓库内的 `scripts/deploy.sh`，真实密钥只保留在服务器本地环境文件和 GitHub Secrets。

## 2. 关键文件
- 根目录 `.env.example`
  - 环境变量模板，只放占位符，不放真实凭据。
- 根目录 `docker-compose.yml`
  - 统一读取环境变量并启动 MySQL、Redis、后端、前端。
- 根目录 `scripts/deploy.sh`
  - 统一的远程部署入口，负责构建、迁移、健康检查。
- `backend/whu-treehole-server/src/main/resources/application.yml`
  - 应用配置，只保留非敏感默认值，敏感值必须从环境变量注入。
- `backend/whu-treehole-server/src/main/resources/application-local.yml`
  - 仅控制本地 SQL 初始化策略。

## 3. 常用环境变量
| 变量名 | 说明 |
|--------|------|
| `MYSQL_DATABASE` | MySQL 数据库名 |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码，必填 |
| `REDIS_PASSWORD` | Redis 密码，必填 |
| `FRONTEND_PORT` | 前端映射端口 |
| `VITE_API_BASE_URL` | 前端 API 基础路径 |
| `TREEHOLE_SERVER_PORT` | 后端服务端口 |
| `TREEHOLE_SQL_INIT_MODE` | SQL 初始化模式，本地可 `always`，Docker/生产建议 `never` |
| `TREEHOLE_AUTH_MOCK_EMAIL_ENABLED` | 本地无 SMTP 时可设为 `true` |
| `TREEHOLE_MAIL_HOST` / `TREEHOLE_MAIL_PORT` | SMTP 地址与端口 |
| `TREEHOLE_MAIL_USERNAME` / `TREEHOLE_MAIL_PASSWORD` | SMTP 账号与密码 |
| `TREEHOLE_MAIL_SMTP_AUTH` | 是否开启 SMTP 认证 |
| `TREEHOLE_MAIL_SMTP_STARTTLS` / `TREEHOLE_MAIL_SMTP_STARTTLS_REQUIRED` | STARTTLS 配置 |
| `TREEHOLE_MAIL_SMTP_SSL_ENABLE` / `TREEHOLE_MAIL_SMTP_SSL_TRUST` | SSL 配置 |
| `TREEHOLE_AUTH_MAIL_FROM` | 发件人地址 |
| `TREEHOLE_AUTH_EMAIL_SUFFIX` | 允许注册邮箱后缀 |
| `TREEHOLE_AUTH_EMAIL_CODE_TTL` | 验证码有效期 |
| `TREEHOLE_AUTH_EMAIL_SEND_COOLDOWN` | 发码冷却时间 |
| `TREEHOLE_AUTH_SESSION_TTL` | 登录会话有效期 |
| `TREEHOLE_DEPLOY_ENV_FILE` | 部署脚本使用的 env 文件路径 |
| `TREEHOLE_COMPOSE_PROJECT` | Docker Compose project name |

## 4. 本地 Docker 运行
1. 从根目录复制环境模板：
   ```bash
   cp .env.example .env.local
   ```
2. 修改 `.env.local` 中的真实密码与本地配置。
3. 启动容器：
   ```bash
   docker compose --env-file .env.local up -d --build
   ```
4. 如需在本地自动初始化 SQL，可将 `.env.local` 中的 `TREEHOLE_SQL_INIT_MODE=always`。

## 5. 服务器部署
1. 在服务器项目根目录创建 `.env.production`：
   ```bash
   cp .env.example .env.production
   ```
2. 填入真实的 MySQL、Redis、SMTP 配置。
3. 执行部署：
   ```bash
   TREEHOLE_DEPLOY_ENV_FILE=.env.production TREEHOLE_COMPOSE_PROJECT=se_project bash scripts/deploy.sh
   ```

## 6. GitHub Actions 部署约定
- `main` 分支 push 后才执行生产部署。
- Workflow 通过 SSH 登录服务器，只执行：
  - `git pull --ff-only origin main`
  - `bash scripts/deploy.sh`
- 如果服务器缺失 `.env.production` 或配置不完整，部署会直接失败，而不是偷偷回退到源码里的默认密码。

## 7. 安全建议
- 不要把 `.env.local`、`.env.production`、数据库导出文件、Redis dump 提交到 Git。
- MySQL、Redis、SMTP 使用不同随机密码，不要共用同一串口令。
- 生产 SMTP 建议使用专门的发信账号，不要直接复用个人主邮箱。
- 如果后续项目继续扩展，下一步可考虑把服务器 `.env.production` 再迁到云厂商 Secret Manager 或 Vault。
