# Changelog

本文档记录项目的重要变更，格式参考 Keep a Changelog。

## [Unreleased]

### 新增
- 新增武大教育邮箱验证码注册、用户名密码登录和 Bearer Token 会话。
- 新增 `user_credentials` 表以及 Redis 验证码、会话存储。
- 新增前端登录页、注册页、路由守卫和退出登录能力。

### 变更
- 业务接口不再依赖固定演示用户，统一改为真实登录用户上下文。
- 发帖接口兼容 `HOME/ALUMNI` 与中文发布范围。
- 部署配置新增 SMTP 与认证时效参数。
- 修复 SMTP 发信时 `From` 地址可能为空的问题，默认回退到 `spring.mail.username`。
- 补充 QQ SMTP 的 STARTTLS/SSL 参数与超时配置，便于排查验证码发送失败。
- 后端运行配置改为支持 Docker 环境变量注入数据库、Redis、SMTP 与 SQL 初始化策略。

## [0.1.0] - 2026-04-08

### 新增
- 初始化武大树洞 Spring Boot 多模块后端工程。
- 新增首页、话题页、校友圈、个人主页和消息中心接口。
- 新增 MySQL 建表与种子数据脚本，并接入 Redis 页面缓存。
