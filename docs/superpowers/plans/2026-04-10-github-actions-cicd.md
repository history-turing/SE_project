# GitHub Actions CI/CD Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为当前仓库补齐可工作的 Docker 部署配置，并新增基于 `push` 触发的 GitHub Actions 流程，在所有分支执行 CI、仅 `main` 自动 SSH 部署到 `/root/SE_project`。

**Architecture:** 先恢复前端、后端和 Compose 的部署契约，使 `docker compose up -d --build` 成为稳定入口；再新增单个 `ci-cd.yml` 工作流，在 GitHub Runner 上分别执行前后端校验，并在 `main` 分支通过 SSH 连接线上服务器执行拉取与重建。

**Tech Stack:** GitHub Actions, Docker Compose, Vite, React, TypeScript, Maven, Spring Boot 3, MySQL 8, Redis 7, OpenSSH

---

### Task 1: 恢复前端 Docker 镜像构建

**Files:**
- Modify: `frontend/Dockerfile`
- Test: `frontend/Dockerfile`

- [ ] **Step 1: 运行当前前端镜像构建，确认它因产物目录错误而失败**

Run: `docker build -t se-frontend:test frontend`
Expected: FAIL，错误接近 `"/app/build" not found` 或 `COPY failed`，说明当前 Dockerfile 仍在复制不存在的 Vite 构建目录。

- [ ] **Step 2: 将前端 Dockerfile 改回与 Vite 项目匹配的实现**

```dockerfile
FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN npm run build

FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

- [ ] **Step 3: 重新构建前端镜像，确认 Dockerfile 已恢复可用**

Run: `docker build -t se-frontend:test frontend`
Expected: PASS，日志中出现 `npm run build` 成功，并完成向 Nginx 镜像复制 `/app/dist`。

- [ ] **Step 4: 提交前端 Docker 修复**

```bash
git add frontend/Dockerfile
git commit -m "fix: restore frontend deployment image"
```

### Task 2: 恢复后端 Maven 多模块 Docker 镜像构建

**Files:**
- Modify: `backend/Dockerfile`
- Test: `backend/Dockerfile`

- [ ] **Step 1: 运行当前后端镜像构建，确认单模块写法无法适配当前仓库**

Run: `docker build -t se-backend:test backend`
Expected: FAIL，错误接近 `COPY src ./src` 找不到目录、`target/*.jar` 不存在，或 Maven 无法找到多模块产物。

- [ ] **Step 2: 将后端 Dockerfile 改为 Maven 多模块构建**

```dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY pom.xml settings.xml ./
COPY whu-treehole-common/pom.xml whu-treehole-common/pom.xml
COPY whu-treehole-domain/pom.xml whu-treehole-domain/pom.xml
COPY whu-treehole-infra/pom.xml whu-treehole-infra/pom.xml
COPY whu-treehole-server/pom.xml whu-treehole-server/pom.xml

RUN mvn -q -s settings.xml -pl whu-treehole-server -am dependency:go-offline

COPY whu-treehole-common whu-treehole-common
COPY whu-treehole-domain whu-treehole-domain
COPY whu-treehole-infra whu-treehole-infra
COPY whu-treehole-server whu-treehole-server

RUN mvn -q -s settings.xml -pl whu-treehole-server -am clean package -DskipTests
RUN find /workspace/whu-treehole-server/target -maxdepth 1 -name "whu-treehole-server-*.jar" ! -name "*.jar.original" -exec cp {} /workspace/app.jar \;

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache tzdata \
    && addgroup -S spring \
    && adduser -S spring -G spring

COPY --from=builder /workspace/app.jar /app/app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

- [ ] **Step 3: 重新构建后端镜像，确认可执行 jar 能正确产出**

Run: `docker build -t se-backend:test backend`
Expected: PASS，日志中出现 `-pl whu-treehole-server -am clean package` 成功，并在运行镜像中复制 `/workspace/app.jar`。

- [ ] **Step 4: 提交后端 Docker 修复**

```bash
git add backend/Dockerfile
git commit -m "fix: restore backend deployment image"
```

### Task 3: 恢复 Compose 部署契约与环境变量映射

**Files:**
- Modify: `docker-compose.yml`
- Test: `docker-compose.yml`

- [ ] **Step 1: 验证当前 Compose 配置没有暴露后端期望的 `TREEHOLE_*` 环境变量**

Run: `docker compose config | Select-String "TREEHOLE_DB_HOST"`
Expected: no match，说明当前 Compose 文件已经偏离 `application.yml` 中的运行时配置约定。

- [ ] **Step 2: 将 `docker-compose.yml` 改回支持一键部署的版本**

```yaml
version: "2.4"

services:
  mysql:
    image: mysql:8.0.41
    restart: unless-stopped
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
    environment:
      TZ: Asia/Shanghai
      MYSQL_DATABASE: ${MYSQL_DATABASE:-se_project}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-jwdyszm0220}
    volumes:
      - mysql-data:/var/lib/mysql
      - ./docker/se_project.sql:/docker-entrypoint-initdb.d/01-se_project.sql:ro
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h 127.0.0.1 -uroot -p$$MYSQL_ROOT_PASSWORD --silent"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 20s

  redis:
    image: redis:7.4-alpine
    restart: unless-stopped
    command:
      - redis-server
      - --appendonly
      - "yes"
      - --requirepass
      - ${REDIS_PASSWORD:-jwdyszm0220}
    environment:
      TZ: Asia/Shanghai
      REDIS_PASSWORD: ${REDIS_PASSWORD:-jwdyszm0220}
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD-SHELL", "redis-cli -a \"$$REDIS_PASSWORD\" ping | grep PONG"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 10s

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    restart: unless-stopped
    environment:
      TZ: Asia/Shanghai
      TREEHOLE_DB_HOST: mysql
      TREEHOLE_DB_PORT: 3306
      TREEHOLE_DB_NAME: ${MYSQL_DATABASE:-se_project}
      TREEHOLE_DB_USERNAME: root
      TREEHOLE_DB_PASSWORD: ${MYSQL_ROOT_PASSWORD:-jwdyszm0220}
      TREEHOLE_REDIS_HOST: redis
      TREEHOLE_REDIS_PORT: 6379
      TREEHOLE_REDIS_PASSWORD: ${REDIS_PASSWORD:-jwdyszm0220}
      TREEHOLE_SERVER_PORT: 8080
      TREEHOLE_SQL_INIT_MODE: never
      TREEHOLE_AUTH_MOCK_EMAIL_ENABLED: ${TREEHOLE_AUTH_MOCK_EMAIL_ENABLED:-false}
      TREEHOLE_MAIL_HOST: ${TREEHOLE_MAIL_HOST:-smtp.qq.com}
      TREEHOLE_MAIL_PORT: ${TREEHOLE_MAIL_PORT:-587}
      TREEHOLE_MAIL_USERNAME: ${TREEHOLE_MAIL_USERNAME:-2953695254@qq.com}
      TREEHOLE_MAIL_PASSWORD: ${TREEHOLE_MAIL_PASSWORD:-xpmdhbwnkrkidfei}
      TREEHOLE_MAIL_SMTP_AUTH: ${TREEHOLE_MAIL_SMTP_AUTH:-true}
      TREEHOLE_MAIL_SMTP_STARTTLS: ${TREEHOLE_MAIL_SMTP_STARTTLS:-true}
      TREEHOLE_MAIL_SMTP_SSL_TRUST: ${TREEHOLE_MAIL_SMTP_SSL_TRUST:-smtp.qq.com}
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    expose:
      - "8080"

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
      args:
        VITE_API_BASE_URL: /api/v1
    restart: unless-stopped
    depends_on:
      - backend
    ports:
      - "${FRONTEND_PORT:-80}:80"

volumes:
  mysql-data:
  redis-data:
```

- [ ] **Step 3: 重新解析 Compose 配置，确认部署契约已恢复**

Run: `docker compose config`
Expected: PASS，输出中包含 `TREEHOLE_DB_HOST: mysql`、`TREEHOLE_REDIS_HOST: redis`、`./docker/se_project.sql:/docker-entrypoint-initdb.d/01-se_project.sql:ro` 和两个命名卷 `mysql-data`、`redis-data`。

- [ ] **Step 4: 提交 Compose 修复**

```bash
git add docker-compose.yml
git commit -m "fix: restore compose deployment contract"
```

### Task 4: 新增 GitHub Actions CI/CD 工作流

**Files:**
- Create: `.github/workflows/ci-cd.yml`
- Test: `.github/workflows/ci-cd.yml`

- [ ] **Step 1: 先确认仓库内还没有现成 workflow，确保这是新增而不是覆盖**

Run: `Get-ChildItem -Recurse .github`
Expected: FAIL 或目录不存在，说明仓库当前尚未配置 GitHub Actions。

- [ ] **Step 2: 创建 `ci-cd.yml`，实现 push 触发的 CI 和 main 分支部署**

```yaml
name: CI/CD

on:
  push:

jobs:
  frontend-ci:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: "20"
          cache: npm
          cache-dependency-path: frontend/package-lock.json

      - name: Install frontend dependencies
        working-directory: frontend
        run: npm ci

      - name: Build frontend
        working-directory: frontend
        run: npm run build

  backend-ci:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"
          cache: maven

      - name: Test backend modules
        working-directory: backend
        run: mvn -q -s settings.xml -pl whu-treehole-server -am test

  deploy:
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    needs:
      - frontend-ci
      - backend-ci
    steps:
      - name: Configure SSH key
        run: |
          mkdir -p ~/.ssh
          printf '%s\n' "${{ secrets.DEPLOY_SSH_KEY }}" > ~/.ssh/id_rsa
          chmod 600 ~/.ssh/id_rsa
          ssh-keyscan -p "${{ secrets.DEPLOY_PORT }}" "${{ secrets.DEPLOY_HOST }}" >> ~/.ssh/known_hosts

      - name: Deploy on server
        run: |
          ssh -p "${{ secrets.DEPLOY_PORT }}" "${{ secrets.DEPLOY_USER }}@${{ secrets.DEPLOY_HOST }}" <<'EOF'
          set -e
          cd /root/SE_project
          git pull
          docker compose up -d --build
          EOF
```

- [ ] **Step 3: 使用 `actionlint` 校验 workflow 语法**

Run: `docker run --rm -v "${PWD}:/repo" -w /repo rhysd/actionlint:latest`
Expected: PASS，输出为空或只包含非阻塞提示，确认 `.github/workflows/ci-cd.yml` 没有语法和表达式错误。

- [ ] **Step 4: 提交 GitHub Actions 工作流**

```bash
git add .github/workflows/ci-cd.yml
git commit -m "ci: add push-based github actions pipeline"
```

### Task 5: 本地总验证、GitHub Secrets 准备与推送

**Files:**
- Modify: `.github/workflows/ci-cd.yml`（如验证后需微调）
- Test: `frontend/package.json`
- Test: `backend/pom.xml`
- Test: `docker-compose.yml`

- [ ] **Step 1: 运行前端构建，确认 CI 命令与本地一致**

Run: `npm ci`
Working directory: `frontend`
Expected: PASS，依赖安装完成且无锁文件冲突。

- [ ] **Step 2: 运行前端构建**

Run: `npm run build`
Working directory: `frontend`
Expected: PASS，生成 Vite 构建产物。

- [ ] **Step 3: 运行后端测试，确认 CI 命令可通过**

Run: `mvn -q -s settings.xml -pl whu-treehole-server -am test`
Working directory: `backend`
Expected: PASS，至少 `DomainSmokeTest` 通过，Maven 多模块测试完成。

- [ ] **Step 4: 再次检查 Compose 最终配置**

Run: `docker compose config`
Expected: PASS，输出为最终部署配置，不出现 YAML 解析错误。

- [ ] **Step 5: 确认 GitHub 仓库 Secrets 已准备好**

需要在 GitHub 仓库中配置以下 Secrets：

```text
DEPLOY_HOST=43.134.116.122
DEPLOY_USER=root
DEPLOY_PORT=22
DEPLOY_SSH_KEY=<用于登录服务器的私钥内容>
```

- [ ] **Step 6: 推送到 GitHub 触发首轮 CI/CD**

Run: `git push origin main`
Expected: PASS，GitHub 上出现名为 `CI/CD` 的 workflow；`frontend-ci` 与 `backend-ci` 先执行，随后 `deploy` 在 `main` 分支触发。

- [ ] **Step 7: 如果需要收尾修正，再提交一次**

```bash
git add frontend/Dockerfile backend/Dockerfile docker-compose.yml .github/workflows/ci-cd.yml
git commit -m "chore: finalize github actions deployment flow"
```
