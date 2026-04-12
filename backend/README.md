# 武大树洞后端

这是面向现有前端页面的 Spring Boot 多模块后端，覆盖首页、话题页、校友圈、个人主页、消息中心、RBAC 与内容治理相关接口。

## 模块说明
- `whu-treehole-common`: 通用响应体与异常定义
- `whu-treehole-domain`: DTO、请求体与领域枚举
- `whu-treehole-infra`: MyBatis Mapper、数据模型与 SQL 映射
- `whu-treehole-server`: Spring Boot 启动模块、控制器、服务、缓存与配置

## 技术栈
- Java 17
- Spring Boot 3.3
- Maven 多模块
- MyBatis
- MySQL 8
- Redis 7

## 本地启动
1. 准备 MySQL 与 Redis。
2. 根目录复制环境模板：
   ```bash
   cp .env.example .env.local
   ```
3. 根据本地环境修改 `.env.local`。
4. 在 `backend/` 目录执行：
   ```bash
   mvn -q -s settings.xml -pl whu-treehole-server -am spring-boot:run
   ```

## Docker 启动
推荐在仓库根目录执行：

```bash
docker compose --env-file .env.local up -d --build
```

## 配置原则
- `application.yml` 中只保留非敏感默认值。
- 所有密码、SMTP 授权码、Redis 口令必须通过环境变量注入。
- 生产部署统一使用根目录 `scripts/deploy.sh`。

## 主要接口
- `GET /api/v1/pages/home`
- `GET /api/v1/pages/topics`
- `GET /api/v1/pages/alumni`
- `GET /api/v1/pages/profile`
- `GET /api/v1/topics/trending`
- `GET /api/v1/announcements`
- `GET /api/v1/announcements/popup`
- `POST /api/v1/posts`
- `POST /api/v1/posts/{postCode}/likes/toggle`
- `POST /api/v1/posts/{postCode}/saves/toggle`
- `POST /api/v1/alumni/contacts/{contactCode}/follow/toggle`
- `GET /api/v1/conversations/{conversationCode}`
- `POST /api/v1/conversations/{conversationCode}/messages`
- `POST /api/v1/conversations/{conversationCode}/read`
