# Messaging Platform Phase C Design

## 目标

为当前武大树洞项目设计一套可长期演进的私信系统，满足以下目标：

- 用户可从用户主页、帖子作者区域、评论作者区域发起私信
- 支持真实单聊会话，不再依赖当前展示型假会话结构
- 支持文本消息发送、实时推送、已读状态、未读聚合
- 支持消息撤回、单侧删除会话、拉黑、举报、审计
- 引入 `api-gateway`、`nacos`、`redis`、`rabbitmq`
- 私信域按独立 `message-service` 设计，为后续图片、表情包、群聊、多端同步预留空间
- 保持现有 Web 端整体 UI 布局不被大改，只升级数据与交互能力
- 兼容未来以学习为目的的 K8s 集群部署

本次设计优先级不是“最快实现”，而是“先把边界和模型做对，后续扩展不推翻”。

## 当前项目上下文

当前项目已经具备：

- 基于 Bearer token + Redis session 的认证链路
- 帖子、评论、点赞、收藏、举报、审核、审计、公告、热议话题等基础能力
- 面向演示用途的消息页、会话列表和发送消息接口
- Docker Compose 远程部署、GitHub Actions、远程 `.env.production` 基线
- `SUPER_ADMIN / ADMIN / USER` 的 RBAC 框架

当前私信能力的主要问题：

- 当前 `conversations(owner_user_id, peer_name...)` 更偏页面展示模型，不是产品级会话模型
- 当前 `messages(sender_type, text_content...)` 无法优雅支持撤回、表情包、图片、系统消息
- 没有真实的双边参与者模型、已读位置模型、单侧删除模型
- 没有 Redis 在线态、未读聚合、幂等控制
- 没有 MQ 事件链路，后续推送、审计联动、离线补偿都不稳
- 没有 Gateway/Nacos 微服务治理能力

## 设计原则

- 可扩展优先：会话、参与者、消息、事件、举报、审计解耦建模
- 事实存储与高频状态分离：MySQL 存事实，Redis 存状态，RabbitMQ 传事件
- 微服务边界先行：即使初期部署在同一服务器，也按独立服务职责设计
- 前端布局稳定优先：尽量不破坏现有页面结构和视觉层次
- 审计优先：所有高风险后台动作必须落审计日志
- 合理收敛复杂度：本期聚焦文本消息与实时链路，不提前实现图片上传对象存储
- 对 K8s 友好：服务无状态化、配置外置、健康检查标准化、可横向扩容

## 方案选型

### 方案 A：继续补丁式增强当前私信实现

做法：

- 保留当前 `conversations` 与 `messages` 的页面导向模型
- 继续追加字段和接口

优点：

- 改动最小
- 短期可见效果快

缺点：

- 数据模型会越来越别扭
- 撤回、未读、拉黑、事件流、表情包扩展都容易堆成补丁
- 后续拆微服务成本高

### 方案 B：标准消息域 + 单体内模块化 + Redis/RabbitMQ

做法：

- 在当前后端内重建消息域模型
- 接入 Redis、RabbitMQ、WebSocket
- 服务职责仍在同一 Spring Boot 工程内

优点：

- 比方案 A 稳健很多
- 上手成本低

缺点：

- 学习不到完整网关、注册发现、服务治理
- 后续拆服务仍要继续迁移

### 方案 C：独立消息服务 + Gateway + Nacos + Redis + RabbitMQ

做法：

- 引入 `api-gateway`
- 引入 `message-service`
- 使用 `nacos` 做注册发现与配置中心
- 使用 `redis` 管理在线状态、未读聚合、幂等键、WebSocket 会话态
- 使用 `rabbitmq` 做消息事件分发、离线补偿、审计联动

优点：

- 边界清晰，最接近成熟社区产品的演进方向
- 便于后续扩展图片、表情包、群聊、站内通知、推送
- 便于后续接入 K8s

缺点：

- 本次实现复杂度最高
- 部署、配置、调试链路都会扩展

## 选定方案

采用方案 C：独立消息服务 + Gateway + Nacos + Redis + RabbitMQ。

原因：

- 用户明确希望按成熟项目而不是低配实现推进
- 用户明确希望学习微服务、MQ、网关、注册中心和 K8s
- 该方案已经具备长期演进到图片消息、表情包、系统消息的结构基础
- 当前项目已有远程 Docker 与 CI/CD 基线，具备继续扩展的操作基础

## 目标架构

### 服务拆分

- `api-gateway`
  - 统一 HTTP 与 WebSocket 入口
  - 统一鉴权透传、路由、限流、跨域策略
- `user-service`
  - 认证、用户、RBAC、用户状态
- `content-service`
  - 帖子、评论、公告、话题等现有内容域
- `message-service`
  - 会话、参与者、消息、撤回、拉黑、未读、WebSocket 实时推送
- `moderation-service`
  - 本期允许逻辑上独立、物理上暂不拆出单独容器
  - 接收消息举报、审计查询、管理动作

### 基础设施

- `mysql`
  - 事实数据存储
- `redis`
  - 在线状态、会话未读聚合、WebSocket 连接态、消息幂等键
- `rabbitmq`
  - 消息事件总线、离线补偿、审计联动
- `nacos`
  - 注册中心与配置中心

### K8s 预留

- 所有业务服务保持无状态
- 配置全部外置，不写死在仓库
- 所有服务都提供健康检查接口
- 日志统一输出到 stdout/stderr
- 服务间调用不把注册发现逻辑散落在业务代码中

## 私信领域模型

当前已有的 `conversations` / `messages` 结构不适合作为未来主模型。新模型建议采用以下命名与职责：

### `dm_conversations`

表示一段真实会话。

建议字段：

- `id`
- `conversation_code`
- `conversation_type`，本期固定为 `SINGLE`
- `status`，如 `ACTIVE / FROZEN / CLOSED`
- `created_by`
- `last_message_id`
- `last_message_at`
- `created_at`
- `updated_at`

说明：

- 这张表不保存某个用户视角下的 `peer_name` 等展示字段
- 展示信息通过参与者与用户资料拼装
- 预留群聊扩展口，不在本期启用

### `dm_conversation_participants`

表示用户参与某段会话以及该用户视角下的状态。

建议字段：

- `id`
- `conversation_id`
- `user_id`
- `participant_role`
- `joined_at`
- `left_at`
- `last_read_message_id`
- `last_read_at`
- `unread_count`
- `pinned_flag`
- `muted_flag`
- `cleared_at`
- `deleted_at`
- `created_at`
- `updated_at`

说明：

- 单侧删除会话通过 `deleted_at / cleared_at` 实现
- 已读位置与未读聚合统一从该表衍生
- 后续支持置顶、免打扰、会话归档不需要改主模型

### `dm_messages`

表示消息本体。

建议字段：

- `id`
- `message_code`
- `conversation_id`
- `sender_user_id`
- `message_type`
- `content_payload`
- `status`
- `seq_no`
- `client_message_id`
- `sent_at`
- `created_at`
- `updated_at`

消息类型建议：

- `TEXT`
- `SYSTEM`
- 预留 `STICKER`
- 预留 `IMAGE`

说明：

- `content_payload` 使用 JSON 文本存储结构化内容
- `TEXT` 本期 payload 只需包含纯文本
- `STICKER / IMAGE` 后续直接扩展 payload 结构即可
- `client_message_id` 用于客户端重试幂等
- `seq_no` 用于会话内排序、已读、撤回、事件定位

### `dm_message_recalls`

表示撤回动作流水。

建议字段：

- `id`
- `message_id`
- `operator_user_id`
- `reason_code`
- `created_at`

说明：

- 撤回不做物理删除
- 消息主表状态置为 `RECALLED`
- 审计仍可还原原文

### `dm_user_blocks`

表示用户拉黑关系。

建议字段：

- `id`
- `user_id`
- `blocked_user_id`
- `created_at`

说明：

- A 拉黑 B 后，B 不可继续向 A 发送新消息
- 会话历史仍保留

### `dm_conversation_events`

表示私信域事件流水。

建议字段：

- `id`
- `event_type`
- `conversation_id`
- `message_id`
- `operator_user_id`
- `payload_json`
- `created_at`

说明：

- 记录发消息、撤回、已读、删除会话、冻结会话等动作
- 便于审计、排错、补偿

### 举报与审计模型

消息举报不单独发明第二套体系，复用已有审核/审计设计，只扩展目标类型：

- `reports.target_type` 增加 `MESSAGE / CONVERSATION`
- `audit_logs.action_type` 增加私信域动作类型

## 接口设计

### 用户侧 HTTP 接口

- `POST /api/messages/conversations/single`
  - 创建或获取单聊会话
- `GET /api/messages/conversations`
  - 获取会话列表，按最近消息排序
- `GET /api/messages/conversations/{conversationCode}`
  - 获取会话详情与历史消息，支持分页
- `POST /api/messages/conversations/{conversationCode}/messages`
  - 发送文本消息
- `POST /api/messages/messages/{messageCode}/recall`
  - 撤回消息
- `POST /api/messages/conversations/{conversationCode}/read`
  - 标记已读
- `DELETE /api/messages/conversations/{conversationCode}`
  - 单侧删除会话
- `POST /api/messages/blocks`
  - 拉黑用户
- `DELETE /api/messages/blocks/{userCode}`
  - 取消拉黑
- `POST /api/messages/reports`
  - 举报消息或会话

### 管理侧接口

- `GET /api/admin/messages/reports`
- `GET /api/admin/messages/reports/{reportCode}`
- `POST /api/admin/messages/reports/{reportCode}/resolve`
- `GET /api/admin/messages/audit-logs`
- `POST /api/admin/messages/conversations/{conversationCode}/freeze`
- `POST /api/admin/messages/conversations/{conversationCode}/unfreeze`
- `POST /api/admin/messages/messages/{messageCode}/recall`

说明：

- 普通管理员默认只可见进入审核流程的私信
- 超级管理员具备全局审计能力
- 所有后台读取私信内容行为必须写审计日志

## WebSocket 设计

### 建连方式

- 前端通过 `api-gateway` 访问 `/ws/messages`
- `api-gateway` 完成路由与基础鉴权透传
- `message-service` 校验用户身份并建立用户连接会话

### 推送事件类型

- `message.created`
- `message.recalled`
- `conversation.read`
- `conversation.deleted`
- `conversation.unread.changed`
- `conversation.frozen`
- `user.block.created`

说明：

- 推送结构统一为事件模型，不推页面片段
- 前端按事件类型更新当前消息页与会话列表
- 后续表情包、图片、系统消息只需增加事件数据解析

## RabbitMQ 设计

### 使用目的

- 新消息事件广播
- 会话摘要刷新
- 未读聚合更新
- 审计联动
- 举报联动
- 离线补偿

### 关键原则

- 发消息成功以 MySQL 持久化成功为准
- MQ 失败不影响消息事实写入，但需要补偿
- RabbitMQ 是事件总线，不是消息事实库

### 典型事件

- `dm.message.created`
- `dm.message.recalled`
- `dm.conversation.read`
- `dm.conversation.deleted`
- `dm.report.created`

## Redis 设计

### 使用目的

- 在线用户状态
- WebSocket 连接映射
- 会话未读数缓存
- 最近会话摘要缓存
- `client_message_id` 防重
- 撤回窗口辅助校验

### 关键原则

- Redis 不保存长期历史消息
- Redis 数据丢失后，允许通过 MySQL 重建状态
- 以 MySQL 为最终事实来源

## 私信业务规则

### 发起私信入口

- 用户主页
- 帖子作者区域
- 评论作者区域

### 发起会话规则

- 统一调用“创建或获取单聊”接口
- 相同两人之间复用已有单聊会话，不重复创建
- 被对方拉黑时禁止创建或继续发送

### 撤回规则

- 普通用户只能撤回自己发送的消息
- 默认撤回窗口为 `2 分钟`
- 超时后普通用户不可撤回
- 超级管理员可基于审核动作执行管理型撤回
- 撤回只改状态，不物理删除内容

### 删除规则

- 用户只允许单侧删除自己的会话视图
- 删除不影响对方会话
- 删除后再次收到新消息可重新出现在列表中

### 拉黑规则

- 用户可拉黑其他用户
- 被拉黑方不能继续发送新消息
- 历史消息保留
- 管理员不能绕过用户拉黑关系代发消息

### 举报与审计规则

- 用户可举报单条消息或整段会话
- 管理员默认只在举报流程中查看相关上下文
- 超级管理员可进行全局审计
- 所有后台查看私信内容行为都写入 `audit_logs`

## RBAC 权限扩展

新增私信域权限建议如下：

- `dm.conversation.create`
- `dm.conversation.read.self`
- `dm.conversation.delete.self`
- `dm.message.send`
- `dm.message.recall.self`
- `dm.message.report`
- `dm.user.block.self`
- `dm.report.review`
- `dm.message.audit`
- `dm.message.recall.any`
- `dm.conversation.freeze`
- `dm.user.block.manage`

### 角色分配建议

`USER`

- 创建单聊
- 查看自己的会话
- 发送文本消息
- 在规则内撤回自己的消息
- 单侧删除会话
- 拉黑用户
- 举报消息/会话

`ADMIN`

- 处理私信举报
- 查看举报上下文
- 冻结会话
- 对违规用户执行限制

`SUPER_ADMIN`

- 拥有全部 `ADMIN` 权限
- 可执行全局审计查询
- 可执行管理型撤回
- 可恢复冻结会话
- 可配置敏感词、撤回策略、审计策略

## 前端设计

### 总体要求

- 尽量不改变现有前端布局
- 保留现有消息中心页的大框架
- 通过真实接口和事件替换演示数据

### 入口改造

- 用户主页增加“发私信”按钮
- 帖子作者区域增加“发私信”入口
- 评论作者区域增加“发私信”入口

### 会话页能力

- 会话列表按最近消息时间排序
- 展示未读数
- 当前会话支持文本发送
- 当前会话支持撤回消息
- 会话支持删除、举报
- 后续可平滑增加表情包消息类型

## Gateway 与 Nacos 设计说明

### Gateway 价值

引入 `api-gateway` 的目的不是简单提高裸性能，而是：

- 统一入口
- 路由转发
- JWT 鉴权透传
- 限流与黑名单策略
- WebSocket 入口统一
- 后续灰度与管理端隔离

### Nacos 价值

引入 `nacos` 的目的在于：

- 服务注册发现
- 配置中心
- 本地与远程 Docker 环境中的微服务治理

对未来 K8s 的影响：

- 现在允许使用 Nacos 加快微服务学习与落地
- 后续进入 K8s 时，服务发现可迁移到 `Service DNS`
- 配置可迁移到 `ConfigMap / Secret`
- 业务代码不要强耦合 Nacos SDK 细节

## K8s 预留设计

为了后续学习 K8s，本期设计需满足：

- 每个服务一个独立 Dockerfile
- 健康检查接口标准化
- 配置和密钥全部外置
- WebSocket 不依赖单机内存态
- 在线态与会话态落 Redis
- 服务间调用尽量走服务名或统一客户端抽象
- 所有服务可独立横向扩容

## 测试设计

### 单元测试

- 会话创建幂等
- 相同两人重复发起单聊不重复建会话
- 被拉黑后发送拦截
- 撤回窗口校验
- 单侧删除不影响对方
- 消息防重成功

### 集成测试

- `api-gateway -> message-service -> mysql/redis/rabbitmq`
- WebSocket 建连、收消息、撤回事件同步
- MQ 消费异常后的补偿
- Redis 丢失后状态重建
- Nacos 配置下发后的服务存活

### 端到端测试

- 从用户主页发起私信
- 从帖子作者区域发起私信
- 从评论作者区域发起私信
- 双端发送消息后会话排序与未读更新正确
- 撤回后双方视图一致
- 举报后管理端可见并可审计

### 远程环境验证

功能验收以远程 Docker 部署环境为准，不以本地通过代替：

1. 本地测试通过
2. 推送代码并触发部署
3. 远程检查容器、日志、配置与注册状态
4. 远程检查 Gateway、Message WebSocket 与 HTTP 接口
5. 远程页面实际操作验证发送、撤回、拉黑、举报、审计

## 部署与演进顺序

建议实现顺序如下：

1. 重构私信领域模型，淘汰展示型会话模型
2. 抽出 `message-service`，打通 MySQL
3. 接入 `api-gateway` 与 `nacos`
4. 接入 Redis 在线态、未读聚合、幂等控制
5. 接入 RabbitMQ 事件流与补偿
6. 接入 WebSocket 实时推送
7. 接入举报、审计、冻结能力
8. 最后预埋 `STICKER / IMAGE` 消息类型扩展点

## 风险与控制

### 风险：模型重构会影响当前消息页

控制：

- 前端保留现有布局
- 新老接口切换时做适配层，避免页面整体推倒

### 风险：Gateway + WebSocket 鉴权容易出错

控制：

- 建立专门的握手与透传集成测试
- 明确 JWT 在 Gateway 和 Message Service 的职责边界

### 风险：Redis 与 MySQL 状态不一致

控制：

- MySQL 永远作为事实源
- Redis 缓存允许失效重建

### 风险：RabbitMQ 堆积或失败导致推送延迟

控制：

- 用户成功发送以持久化成功为准
- WebSocket 推送失败不影响历史消息拉取
- 建立补偿消费与死信处理策略

### 风险：管理员滥用审计能力

控制：

- 默认仅举报进入流程的私信可见
- 所有查看私信内容的后台动作写审计日志

### 风险：未来切 K8s 时被 Nacos 绑定

控制：

- 注册发现和配置访问集中封装
- 业务层不直接依赖 Nacos 细节

## 非目标

本期不包含以下内容：

- 图片消息落地
- 对象存储接入
- 表情包资源管理后台
- 群聊
- 消息漫游到移动端推送
- 音视频消息
- 全文检索与消息搜索
- 真正生产级别的多机房灾备

这些能力都应在本设计基础上扩展，而不是推翻当前消息模型。
