# 任务清单: 远程服务器 Docker 一键部署

目录: `helloagents/plan/202604091701_remote_docker_deploy/`

---

## 1. 部署知识库与方案包
- [√] 1.1 在 `helloagents/` 下补齐根目录知识库与本次方案包，验证 why.md#需求-远程服务器一键部署-场景-首次拉起完整环境

## 2. 后端容器化配置
- [√] 2.1 在 `backend/whu-treehole-server/src/main/resources/application.yml` 中改为环境变量读取 MySQL、Redis、SMTP 与敏感配置，验证 why.md#需求-容器内依赖解耦-场景-容器启动连接依赖
- [√] 2.2 在 `backend/Dockerfile` 和 `backend/.dockerignore` 中补齐后端镜像构建与运行配置，验证 why.md#需求-远程服务器一键部署-场景-首次拉起完整环境

## 3. 前端容器化配置
- [√] 3.1 在 `frontend/Dockerfile`、`frontend/nginx.conf` 和 `frontend/.dockerignore` 中补齐前端镜像与代理配置，验证 why.md#需求-远程服务器一键部署-场景-首次拉起完整环境

## 4. Compose 编排与部署入口
- [√] 4.1 在 `docker-compose.yml` 与 `.env.example` 中编排完整服务，验证 why.md#需求-远程服务器一键部署-场景-首次拉起完整环境

## 5. 安全检查
- [√] 5.1 执行安全检查（按G9: 输入验证、敏感信息处理、权限控制、EHRB风险规避）

## 6. 文档更新
- [√] 6.1 更新 `helloagents/project.md`、`helloagents/wiki/arch.md`、`backend/helloagents/project.md`、`frontend/helloagents/project.md`

## 7. 测试
- [√] 7.1 执行 `npm run build` 验证前端构建
- [√] 7.2 执行 `mvn -q -s settings.xml -pl whu-treehole-server -am test` 验证后端构建与测试

## 执行备注
- `docker compose config` 已通过，说明 Compose 语法和变量替换有效。
- `docker compose build frontend backend` 在当前环境因 Docker Desktop Linux 引擎未启动而无法完成，属于环境限制，不是配置语法错误。
