# 数据模型

## 概述
MySQL 负责持久化用户资料、账号凭证、帖子、互动、消息等主数据；Redis 负责页面缓存、邮箱验证码和登录会话。

---

## MySQL 主要表

### users
- 描述: 用户资料主表，保存昵称、学院、年级、简介、头像等展示信息

### user_credentials
- 描述: 账号凭证表，保存 `user_id`、教育邮箱、用户名、密码散列、邮箱验证时间、最近登录时间
- 约束:
  - `email` 唯一
  - `username` 唯一
  - `user_id` 外键关联 `users`

### posts
- 描述: 首页、校友圈和个人页共用的帖子实体

### post_interactions
- 描述: 用户对帖子点赞、收藏的状态表

### alumni_contacts / user_follow_contacts
- 描述: 校友联系人与关注关系

### conversations / messages
- 描述: 站内会话和消息正文

---

## Redis Key 设计
- `whu-treehole::homePage::*` 等: 页面聚合缓存
- `auth:email-code:{email}`: 邮箱验证码，默认 5 分钟
- `auth:email-send-lock:{email}`: 发码冷却锁，默认 60 秒
- `auth:session:{token}`: 登录会话，默认 7 天

---

## 认证数据流
1. 用户提交邮箱后，验证码写入 Redis，不落 MySQL。
2. 用户校验验证码注册成功后，资料写入 `users`，凭证写入 `user_credentials`。
3. 登录成功后，token 写入 Redis，会话关联到 `user_id`。
4. 业务接口通过拦截器读取 Redis 会话，再反查 MySQL 中的用户资料和互动数据。
