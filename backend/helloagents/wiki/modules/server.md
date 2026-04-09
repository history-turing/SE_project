# server

## 目的
作为启动模块，负责暴露 REST API、业务编排、认证拦截、缓存和跨域配置。

## 模块概览
- 职责: 控制器、服务、认证上下文、拦截器、缓存配置、邮件发送
- 状态: 稳定
- 最后更新: 2026-04-08

## 当前职责
- 页面与互动接口: `PageController`、`PostController`、`ConversationController`、`AlumniController`
- 认证接口: `AuthController`
- 认证服务: `AuthService` 负责验证码、注册、登录、会话与当前用户查询
- 鉴权入口: `AuthInterceptor` + `AuthContextHolder`
- 配置: `AuthProperties`、`AuthSupportConfig`、`WebConfig`

## 运行说明
- `/api/v1/auth/email-code`、`/api/v1/auth/register`、`/api/v1/auth/login` 为匿名接口
- 其余 `/api/v1/**` 接口默认需要 Bearer Token
- 本地没有 SMTP 时可开启 `TREEHOLE_AUTH_MOCK_EMAIL_ENABLED=true` 用日志模拟验证码
- Docker 部署时，数据库、Redis 和 SMTP 连接信息统一通过 `TREEHOLE_DB_*`、`TREEHOLE_REDIS_*`、`TREEHOLE_MAIL_*` 环境变量注入
- 远程部署默认通过 `TREEHOLE_SQL_INIT_MODE=never` 禁用 Spring SQL 初始化，避免与 MySQL 容器导入脚本重复执行
- 真实 SMTP 发信时，`AuthService` 会优先使用 `treehole.auth.mail-from`，未配置时自动回退到 `spring.mail.username`
- QQ 邮箱若使用 `587` 端口，建议开启 STARTTLS；若使用 `465` 端口，建议关闭 STARTTLS 并开启 SSL

## 变更历史
- [202604081956_whu_treehole_backend](../../history/2026-04/202604081956_whu_treehole_backend/) - 初始化控制器、服务与缓存配置
- [202604082220_auth_login](../../history/2026-04/202604082220_auth_login/) - 新增邮箱注册、登录态与鉴权拦截
- [202604091701_remote_docker_deploy](../../history/2026-04/202604091701_remote_docker_deploy/) - 调整容器化运行配置，支持远程服务器部署
