# deployment

## 目的
用最少的手工步骤完成远程服务器上的全栈部署。

## 模块概述
- **职责:** 定义镜像构建方式、容器编排方式、初始化数据与运行时配置
- **状态:** ✅稳定
- **最后更新:** 2026-04-09

## 规范

### 需求: 远程服务器一键部署
**模块:** deployment
部署方案应支持通过一条命令完成前后端、MySQL 与 Redis 的拉起，并提供可覆盖的环境变量入口。

#### 场景: 新服务器首次部署
运维人员在仓库根目录执行 Compose 命令后，应能得到可访问的完整服务。
- 自动构建前后端镜像
- MySQL 首次启动自动导入 `docker/se_project.sql`
- 前端对外暴露单个 HTTP 端口
- 运行密码、端口和 SMTP 参数可通过 `.env` 覆盖

## API接口
### docker compose up -d --build
**描述:** 统一构建并启动所有服务
**输入:** 可选 `.env` 配置
**输出:** 前后端、MySQL、Redis 容器

## 数据模型
### Compose 服务
| 字段 | 类型 | 说明 |
|------|------|------|
| frontend | service | 前端静态资源与 API 代理 |
| backend | service | Spring Boot 应用 |
| mysql | service | 业务数据库 |
| redis | service | 缓存与会话 |

## 依赖
- frontend
- backend
- docker/se_project.sql

## 变更历史
- [202604091701_remote_docker_deploy](../../history/2026-04/202604091701_remote_docker_deploy/) - 新增远程部署所需 Docker 编排与运行约定
