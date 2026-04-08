# API 手册

## 概述
当前后端包含页面聚合接口、互动接口以及完整的账号认证接口。除登录、注册、发送验证码外，其余 `/api/v1/**` 接口默认要求携带 `Authorization: Bearer <token>`。

## 认证方式
- 首次注册: `@whu.edu.cn` 邮箱 + 验证码 + 用户名 + 密码
- 后续登录: 用户名 + 密码
- 登录态: Redis 存储的 Bearer Token 会话，默认 7 天

---

## 认证接口

### [POST] /api/v1/auth/email-code
- 描述: 向武大教育邮箱发送 6 位注册验证码
- 请求体: `email`
- 约束: 仅允许 `@whu.edu.cn`

### [POST] /api/v1/auth/register
- 描述: 校验邮箱验证码并创建账号
- 请求体: `email` `code` `username` `password`
- 响应: `token` + 当前用户摘要

### [POST] /api/v1/auth/login
- 描述: 使用用户名和密码登录
- 请求体: `username` `password`
- 响应: `token` + 当前用户摘要

### [GET] /api/v1/auth/me
- 描述: 获取当前登录用户摘要
- 请求头: `Authorization: Bearer <token>`

### [POST] /api/v1/auth/logout
- 描述: 删除当前登录会话
- 请求头: `Authorization: Bearer <token>`

---

## 页面聚合接口

### [GET] /api/v1/pages/home
- 描述: 获取首页统计、话题预览、热榜、公告和帖子列表

### [GET] /api/v1/pages/topics
- 描述: 获取话题广场数据，支持 `scope=ALL|CAMPUS|ALUMNI`

### [GET] /api/v1/pages/alumni
- 描述: 获取校友圈动态、故事卡片和人脉联系人

### [GET] /api/v1/pages/profile
- 描述: 获取个人主页信息、我的帖子、收藏和消息会话

---

## 互动接口

### [POST] /api/v1/posts
- 描述: 发布树洞帖子
- 请求体: `title` `content` `topic` `audience` `anonymous`
- 说明: `audience` 同时支持 `HOME|ALUMNI` 和中文标签

### [POST] /api/v1/posts/{postCode}/likes/toggle
- 描述: 切换点赞状态并返回最新计数

### [POST] /api/v1/posts/{postCode}/saves/toggle
- 描述: 切换收藏状态并返回最新计数

### [POST] /api/v1/alumni/contacts/{contactCode}/follow/toggle
- 描述: 切换校友联系人的关注状态

### [GET] /api/v1/conversations/{conversationCode}
- 描述: 查询单个会话详情

### [POST] /api/v1/conversations/{conversationCode}/messages
- 描述: 发送站内消息

### [POST] /api/v1/conversations/{conversationCode}/read
- 描述: 将会话未读数清零
