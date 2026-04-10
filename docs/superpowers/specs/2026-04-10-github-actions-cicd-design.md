# GitHub Actions CI/CD Design

## 目标

为当前仓库增加一套基于 GitHub Actions 的 CI/CD 流程，满足以下要求：

- 代码推送到 GitHub 后自动触发工作流
- 所有 `push` 先执行 CI 校验
- 仅 `main` 分支 `push` 时自动部署到服务器 `43.134.116.122`
- 部署目标目录为 `/root/SE_project`
- 部署方式为服务器拉取最新代码后执行 `docker compose up -d --build`

## 当前项目上下文

仓库当前包含以下关键部分：

- `frontend/`：Vite + TypeScript + React 前端
- `backend/`：Spring Boot 3 + Maven 多模块后端
- `docker-compose.yml`：用于前后端、MySQL、Redis 的容器编排
- `frontend/Dockerfile` 与 `backend/Dockerfile`：当前存在与项目实际结构不匹配的改动，若不修正会直接导致构建或部署失败

Git 远程仓库当前为：

- `origin = git@github.com:history-turing/SE_project.git`

## 方案选型

### 方案 A：任意分支 `push` 后直接部署

优点：

- 配置最简单

缺点：

- 任意分支变更都会上线，风险过高
- 不利于后续多人协作

### 方案 B：所有 `push` 触发 CI，仅 `main` 分支自动部署

优点：

- 每次推送都有构建校验
- 线上部署只由主分支驱动，风险可控
- 与当前项目规模和协作方式最匹配

缺点：

- 需要对分支条件做一次明确约定

### 方案 C：Actions 构建镜像并推送镜像仓库，服务器拉镜像部署

优点：

- 部署链路更标准
- 服务器无需本地构建应用代码

缺点：

- 需要额外配置 GHCR 或 Docker Hub
- 当前需求没有要求引入镜像仓库，复杂度偏高

## 选定方案

采用方案 B：

- 所有 `push` 触发 CI
- 仅 `main` 分支 `push` 触发 CD

## 架构设计

### CI 工作流

CI 工作流负责在 GitHub Runner 上完成基础质量校验。

前端步骤：

- 安装 Node.js
- 在 `frontend/` 目录执行 `npm ci`
- 执行 `npm run build`

后端步骤：

- 安装 JDK 17
- 在 `backend/` 目录执行 Maven 多模块测试或打包校验
- 推荐命令为 `mvn -q -pl whu-treehole-server -am test`

设计原则：

- 先保证仓库在 GitHub 上每次推送都能被自动验证
- 不在 CI 阶段依赖远程服务器状态
- 尽量复用仓库现有构建方式

### CD 工作流

CD 工作流在 `main` 分支 `push` 后执行。

部署步骤：

1. GitHub Actions 连接服务器 `43.134.116.122`
2. 进入 `/root/SE_project`
3. 执行 `git pull`
4. 执行 `docker compose up -d --build`
5. 清理无用镜像可作为可选步骤，不作为首版强制要求

认证方式：

- 使用 GitHub Secrets 保存 SSH 私钥、主机、用户、端口
- 工作流通过 SSH Action 或原生命令连接服务器

### Secrets 设计

首版至少需要以下 GitHub Secrets：

- `DEPLOY_HOST=43.134.116.122`
- `DEPLOY_USER=root`
- `DEPLOY_PORT`
- `DEPLOY_SSH_KEY`

如果服务器 `git pull` 使用 SSH 拉仓库，则服务器本机仍需具备拉取该 GitHub 仓库的权限。

## 代码修改范围

### 新增文件

- `.github/workflows/ci-cd.yml`
- `docs/superpowers/plans/2026-04-10-github-actions-cicd.md`

### 修改文件

- `frontend/Dockerfile`
- `backend/Dockerfile`
- `docker-compose.yml`
- 视情况补充 `.gitignore` 或部署文档

## Docker 与部署兼容性修正

在正式接入 Actions 前，需要先修正当前仓库里会阻塞部署的配置问题。

### 前端 Dockerfile

当前风险：

- 当前文件把 Vite 构建产物从 `/app/build` 复制到 Nginx
- 但 Vite 默认产物目录是 `/app/dist`

预期修正：

- 改回复制 `/app/dist`
- 保留或恢复现有 `nginx.conf` 接入方式，避免静态资源或前端路由异常

### 后端 Dockerfile

当前风险：

- 当前文件按单模块 Maven 项目写法构建
- 实际仓库是 Maven 多模块结构，直接构建会失败

预期修正：

- 恢复为多模块 Maven 构建方式
- 定位 `whu-treehole-server` 产出的可执行 jar 并复制到运行镜像

### docker-compose.yml

当前风险：

- 当前改动移除了部分原有环境变量覆盖能力和初始化约定
- 现有版本与部署文档存在偏差

预期修正：

- 保持 `docker compose up -d --build` 可直接部署
- 明确保留 MySQL、Redis、前后端依赖关系
- 尽量保留 `.env` 覆盖能力
- 保证服务器端无需手工编辑大量配置即可上线

## 错误处理

### CI 失败

- 任意前端或后端构建失败都应直接使工作流失败
- 不触发部署

### CD 失败

- SSH 连接失败时直接失败
- `git pull` 或 `docker compose up -d --build` 任一步失败都直接失败
- 保留日志用于排查，不在首版加入自动回滚

## 测试与验证

实现完成后至少验证以下内容：

1. 本地前端 `npm run build` 能通过
2. 本地后端 Maven 校验命令能通过
3. Dockerfile 修正后，本地或服务器上 `docker compose config` 能通过
4. GitHub Actions YAML 语法正确
5. 提交到 GitHub 后，非 `main` 分支仅执行 CI
6. 提交到 `main` 分支后，CI 完成后执行部署

## 非目标

本次不包含以下内容：

- 蓝绿发布或滚动发布
- 自动回滚
- 镜像仓库接入
- 多环境部署（dev/staging/prod）
- 复杂监控、告警、审批流

## 实施顺序

1. 修正 Docker 与部署配置，使仓库具备稳定部署基础
2. 新增 GitHub Actions 工作流并接入 CI
3. 增加基于 SSH 的 CD 步骤
4. 本地验证关键构建命令
5. 提交并推送到 GitHub
6. 在 GitHub 仓库中补充 Secrets 后完成首次部署验证
