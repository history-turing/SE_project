# 项目技术约定

---

## 技术栈
- 核心: Java 17 / Spring Boot 3.3 / Maven 多模块
- 数据访问: MyBatis + MySQL 8
- 缓存与会话: Redis 7
- 邮件: Spring Mail / SMTP

---

## 开发约定
- Controller 负责参数接收与响应包装，业务编排进入 Service，SQL 统一进入 Mapper XML。
- 认证采用 Bearer Token，首次注册要求 `@whu.edu.cn` 邮箱验证码，后续使用用户名和密码登录。
- 密码必须使用 BCrypt 散列，不允许明文入库。
- Redis 同时承担页面缓存、邮箱验证码和登录会话存储。

---

## 配置约定
- MySQL 与 Redis 连接配置位于 `backend/whu-treehole-server/src/main/resources/application.yml`。
- 本地 SQL 初始化位于 `backend/whu-treehole-server/src/main/resources/application-local.yml`。
- 邮箱 SMTP 与认证时效配置同样位于 `application.yml` 的 `spring.mail` 和 `treehole.auth` 节点。
- 本地没有 SMTP 时，可通过 `TREEHOLE_AUTH_MOCK_EMAIL_ENABLED=true` 使用日志模拟验证码发送。

---

## 测试与验证
- 后端至少保证 `mvn -q -s settings.xml -pl whu-treehole-server -am test` 通过。
- 前端至少保证 `npm run build` 通过。
- 认证改动需要验证邮箱验证码、注册、登录、带 token 查询和带 token 发帖链路。
