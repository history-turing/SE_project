# 数据模型

## 概述
业务数据持久化在 MySQL，缓存、验证码和会话存储在 Redis。容器编排默认通过 `docker/se_project.sql` 初始化 MySQL 中的业务表与示例数据。

---

## 数据表/集合

### se_project

**描述:** MySQL 业务库，承载用户、帖子、会话、话题等核心表结构。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| MYSQL_DATABASE | string | 非空 | Compose 中默认创建的数据库名 |
| docker/se_project.sql | SQL dump | 初始化脚本 | 容器首次启动时自动导入 |

**索引:**
- 业务索引定义见 `docker/se_project.sql`

**关联关系:**
- 后端通过 `spring.datasource.*` 连接该数据库

### Redis

**描述:** 作为页面缓存、邮箱验证码和登录会话存储。

| 字段名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| TREEHOLE_REDIS_HOST | string | 非空 | Redis 主机名，容器环境下为 `redis` |
| TREEHOLE_REDIS_PORT | integer | 非空 | Redis 端口，默认 `6379` |
| TREEHOLE_REDIS_PASSWORD | string | 可空/可配置 | Redis 访问密码 |

**索引:**
- 不适用

**关联关系:**
- 后端通过 `spring.data.redis.*` 连接 Redis
