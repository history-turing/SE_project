# 数据模型

## 概述
当前版本采用前端内存数据模型，并将交互快照序列化到 `localStorage`。模型以页面驱动为主，便于静态稿快速迁移到 React 结构。

---

## 数据表/集合

### FeedPost

**描述:** 首页、校友圈和个人页共用的帖子实体。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | string | 主键 | 帖子唯一标识 |
| title | string | 可空 | 帖子标题 |
| content | string | 非空 | 正文内容 |
| author | string | 非空 | 作者展示名 |
| handle | string | 非空 | 作者副标题 |
| topic | string | 非空 | 归属话题 |
| audience | string | 非空 | 发布范围，如首页或校友圈 |
| likes | number | 非空 | 点赞数 |
| comments | number | 非空 | 评论数 |
| saves | number | 非空 | 收藏数 |
| image | string | 可空 | 卡片配图 |

### TopicCard

**描述:** 话题分类与推荐模块的聚合卡片。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | string | 主键 | 话题标识 |
| name | string | 非空 | 话题名称 |
| description | string | 非空 | 话题简介 |
| heat | string | 非空 | 热度展示值 |
| tags | string[] | 非空 | 关联标签 |

### Conversation

**描述:** 个人主页消息中心使用的会话数据。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | string | 主键 | 会话唯一标识 |
| name | string | 非空 | 对方名称 |
| lastMessage | string | 非空 | 会话摘要 |
| unreadCount | number | 非空 | 未读数 |
| messages | Message[] | 非空 | 消息明细 |

### Message

**描述:** 单条站内消息。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | string | 主键 | 消息唯一标识 |
| sender | string | 非空 | 发送方标识 |
| text | string | 非空 | 消息正文 |
| time | string | 非空 | 展示时间 |
