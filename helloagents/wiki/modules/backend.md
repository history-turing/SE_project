# backend

## 目的
提供武大树洞业务接口，并通过环境变量适配不同部署环境。

## 模块概述
- **职责:** 提供 `/api/v1` 接口、连接 MySQL 和 Redis、管理认证和页面聚合逻辑
- **状态:** ✅稳定
- **最后更新:** 2026-04-09

## 规范

### 需求: 远程服务器一键部署
**模块:** backend
后端容器在远程服务器启动时应直接连接 Compose 网络中的 MySQL、Redis，并避免重复初始化数据库。

#### 场景: Docker Compose 启动后端
后端容器应通过环境变量读取依赖配置，而不是依赖容器内 `localhost`。
- MySQL 与 Redis 地址可通过环境变量覆盖
- SQL 初始化模式可由环境变量关闭
- SMTP 配置支持环境变量注入，未配置时可启用模拟邮箱

## API接口
### [GET] /actuator/health
**描述:** 提供服务健康状态查询
**输入:** 无
**输出:** Spring Boot 健康状态

## 数据模型
### 运行时配置
| 字段 | 类型 | 说明 |
|------|------|------|
| TREEHOLE_DB_HOST | string | 数据库主机名 |
| TREEHOLE_DB_PORT | integer | 数据库端口 |
| TREEHOLE_DB_NAME | string | 数据库名 |
| TREEHOLE_DB_USERNAME | string | 数据库用户名 |
| TREEHOLE_DB_PASSWORD | string | 数据库密码 |

## 依赖
- mysql
- redis

## 变更历史
- [202604091701_remote_docker_deploy](../../history/2026-04/202604091701_remote_docker_deploy/) - 将容器运行配置改为环境变量注入
