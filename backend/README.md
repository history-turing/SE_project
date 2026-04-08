# 武大树洞后端

这是一个面向现有前端页面的 Spring Boot 多模块后端，覆盖首页、话题页、校友圈、个人主页和消息中心所需的核心接口。

## 模块说明

- `whu-treehole-common`: 通用响应体、异常定义。
- `whu-treehole-domain`: 页面 DTO、请求体和领域枚举。
- `whu-treehole-infra`: MyBatis Mapper、数据对象与 SQL 映射。
- `whu-treehole-server`: Spring Boot 启动模块、控制器、服务、缓存与配置。

## 技术栈

- Java 17
- Spring Boot 3.3
- Maven 多模块
- MyBatis
- MySQL 8
- Redis 7

## 快速启动

1. 准备 MySQL 和 Redis。
2. 根据 `whu-treehole-server/src/main/resources/application-local.yml` 配置环境变量。
3. 首次启动可使用本地 profile 自动执行 `db/schema.sql` 与 `db/data.sql`。
4. 在 `backend/` 目录执行:

```bash
mvn -q -pl whu-treehole-server -am spring-boot:run
```

## 主要接口

- `GET /api/v1/pages/home`
- `GET /api/v1/pages/topics`
- `GET /api/v1/pages/alumni`
- `GET /api/v1/pages/profile`
- `POST /api/v1/posts`
- `POST /api/v1/posts/{postCode}/likes/toggle`
- `POST /api/v1/posts/{postCode}/saves/toggle`
- `POST /api/v1/alumni/contacts/{contactCode}/follow/toggle`
- `GET /api/v1/conversations/{conversationCode}`
- `POST /api/v1/conversations/{conversationCode}/messages`
- `POST /api/v1/conversations/{conversationCode}/read`
