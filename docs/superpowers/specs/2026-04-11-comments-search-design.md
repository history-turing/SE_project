# Comments And Search Design

## 目标

为当前树洞项目补齐两项缺失能力：

- 帖子评论功能：支持一级评论和二级回复
- 全站搜索功能：支持帖子、校友故事、联系人统一搜索

本次设计要求同时满足以下约束：

- 评论数据以 MySQL 为真源，Redis 仅用于缓存与热点数据
- 搜索必须提供真实后端能力，不能继续停留在前端静态过滤
- 前端风格保持与现有项目一致，不重做视觉体系
- 搜索结果使用独立结果页展示

## 当前项目上下文

后端当前已具备：

- 首页接口：`GET /api/v1/pages/home?topic&keyword`
- 校友圈接口：`GET /api/v1/pages/alumni?topic&keyword`
- 帖子点赞、收藏写接口
- MySQL + Redis 基础设施
- `posts.comment_count` 字段

前端当前已具备：

- 首页、校友圈页面与帖子卡片组件
- 顶部搜索框与页面内本地过滤逻辑
- 帖子点赞、收藏交互
- 统一 API 封装和类型定义

当前缺口：

- 没有评论表，也没有评论接口
- 搜索框没有接后端统一搜索
- 没有搜索结果页
- 帖子评论数不能展开评论区

## 方案选型

### 方案 A：评论与搜索都只放 Redis

优点：

- 读写快

缺点：

- 评论属于核心业务数据，不适合只存缓存
- 搜索结果与评论关系复杂，持久化与恢复成本高
- 一旦 Redis 丢失会直接丢业务数据

### 方案 B：MySQL 为真源，Redis 做评论与搜索缓存

优点：

- 数据一致性最好
- 与当前项目基础设施匹配
- 便于做缓存失效和后续扩展

缺点：

- 需要多一层缓存失效设计

### 方案 C：MySQL 评论，搜索先完全不缓存

优点：

- 实现简单

缺点：

- 搜索接口后续容易成为热点
- 失去 Redis 的现有价值

## 选定方案

采用方案 B：

- 评论与搜索主数据均来自 MySQL
- Redis 仅缓存帖子评论列表、搜索结果和热门搜索词

## 架构设计

### 评论能力

新增评论表 `post_comments`，用于承载一级评论和二级回复。

字段建议：

- `id`
- `comment_code`
- `post_id`
- `user_id`
- `parent_comment_id`
- `root_comment_id`
- `content`
- `reply_to_user_id`
- `deleted_flag`
- `created_at`
- `updated_at`

约束设计：

- `parent_comment_id` 为空表示一级评论
- `parent_comment_id` 非空表示二级回复
- 二级回复必须指向同一帖子的一级评论或该一级评论下的回复
- 本期只支持两级，不支持无限嵌套

新增接口：

- `GET /api/v1/posts/{postCode}/comments`
- `POST /api/v1/posts/{postCode}/comments`
- `POST /api/v1/posts/{postCode}/comments/{commentCode}/replies`

返回结构：

- 一级评论列表
- 每条一级评论下挂载回复列表
- 每条评论包含作者、内容、时间、是否本人、回复对象等前端必要字段
- 一级评论按 `created_at ASC` 排序，回复按 `created_at ASC` 排序

帖子评论计数：

- 新增评论或回复时同步更新 `posts.comment_count`
- 首页、校友圈、个人页继续直接复用该字段展示评论数

### 搜索能力

新增统一搜索接口：

- `GET /api/v1/search?q=...`

搜索范围：

- 帖子：标题、正文、话题、作者
- 校友故事：标题、meta
- 联系人：姓名、meta、focus

返回结构按分组组织：

- `posts`
- `stories`
- `contacts`
- `keyword`
- `total`

搜索结果页：

- 前端路由新增 `/search?q=...`
- 顶部搜索框提交后跳转搜索页
- 搜索页按“帖子 / 校友故事 / 人脉联系人”分组展示
- 无结果时展示统一空状态，不改变现有视觉语言

## 前端交互设计

### 评论区

评论入口：

- 继续使用帖子卡片底部评论数按钮区域
- 点击后在当前帖子区域内展开评论区，不跳离当前页

评论区结构：

- 顶部评论输入框
- 一级评论列表
- 每条一级评论下展示二级回复
- 每条一级评论提供“回复”入口
- 二级回复使用更轻量缩进样式

风格约束：

- 沿用当前浅色渐变、卡片、圆角、按钮和字重体系
- 不新增与当前站点冲突的深色区块、重边框或全新组件体系

### 搜索页

保留现有顶部搜索框入口，但改为真实请求驱动。

页面结构：

- 搜索栏和当前关键词
- 结果总数
- 帖子结果分组
- 校友故事结果分组
- 联系人结果分组

展示要求：

- 帖子结果沿用现有帖子卡片风格
- 校友故事和联系人沿用现有卡片风格
- 仅补充搜索结果页的布局，不重构首页和校友圈整体外观

## 数据流设计

### 评论数据流

1. 前端提交评论或回复
2. 后端校验帖子、父评论、回复层级和内容长度
3. 写入 `post_comments`
4. 更新 `posts.comment_count`
5. 删除 Redis 中该帖评论缓存
6. `POST` 接口返回新创建的评论 DTO，前端按需要局部插入或重新拉取列表

读取评论时：

1. 优先读取 `post-comments:{postCode}`
2. 缓存未命中则查询 MySQL
3. 组装两级评论树
4. 回写 Redis

### 搜索数据流

1. 前端访问 `/search?q=keyword`
2. 后端清洗关键词并校验长度
3. 优先读取 `search:{keyword}`
4. 未命中时分别查询帖子、校友故事、联系人
5. 聚合为统一响应
6. 写入 Redis 并增加热门搜索词计数

## 缓存设计与失效策略

评论缓存键：

- `post-comments:{postCode}`

搜索缓存键：

- `search:{keyword}`

热点统计键：

- `search-hot:{keyword}`

失效策略：

- 新增评论或回复后删除对应 `post-comments:{postCode}`
- 新增帖子后清理与该帖子可能相关的搜索缓存不现实，因此本期采用短 TTL 搜索缓存策略
- 搜索缓存设置较短 TTL，保证实现复杂度与结果新鲜度平衡
- 热门搜索词计数使用单独递增，不随普通搜索结果缓存一起删除

本期建议：

- 评论缓存 TTL 5-10 分钟
- 搜索缓存 TTL 2-5 分钟

## 错误处理

评论接口：

- 帖子不存在：返回 404 类业务错误
- 父评论不存在或不属于该帖子：返回参数错误
- 回复层级超过两级：返回参数错误
- 评论内容为空或超长：返回参数错误

搜索接口：

- 关键词为空：直接返回参数错误，不执行查询
- 关键词超长：直接拦截
- 查询异常：返回统一错误响应，不暴露底层 SQL 细节

前端交互：

- 沿用现有轻提示/错误文案风格
- 评论提交失败时保留输入内容，避免用户重复输入
- 搜索失败时提供重试入口，但不改动站点整体结构

## 测试方案

后端测试：

- 评论 service 测试：创建一级评论、创建二级回复、非法父评论、层级校验、评论数更新
- 评论 controller 测试：接口参数校验与响应结构
- 搜索 service 测试：帖子、故事、联系人聚合查询与空结果场景
- Redis 缓存相关测试：缓存命中与缓存失效行为

前端测试：

- 评论区展开、提交一级评论、提交回复、失败提示
- 搜索框提交跳转 `/search?q=...`
- 搜索结果页分组渲染和空状态渲染
- 样式回归只做局部补充，不改现有页面主结构

集成验证：

1. 发帖后帖子卡片评论数仍正常显示
2. 新增评论后帖子评论数同步增加
3. 评论区展开后可看到一级评论和二级回复
4. 搜索关键词能同时返回帖子、故事、联系人结果
5. 搜索页视觉风格与现有首页、校友圈保持一致

## 代码修改范围

后端预计涉及：

- `backend/whu-treehole-server/src/main/resources/db/schema.sql`
- `backend/whu-treehole-server/src/main/resources/db/data.sql`
- `backend/whu-treehole-server/src/main/java/.../controller`
- `backend/whu-treehole-server/src/main/java/.../service`
- `backend/whu-treehole-infra/src/main/java/.../mapper`
- `backend/whu-treehole-infra/src/main/resources/mapper/*.xml`

前端预计涉及：

- `frontend/src/components/PostCard.tsx`
- `frontend/src/pages/HomePage.tsx`
- `frontend/src/pages/AlumniPage.tsx`
- `frontend/src/services/api.ts`
- `frontend/src/types.ts`
- 新增搜索结果页组件与必要样式

## 非目标

本期不包含以下内容：

- 评论点赞
- 评论删除与审核台
- 全文检索引擎接入
- 搜索高亮、联想词、拼写纠错
- 多级无限嵌套评论
- 全站视觉重设计

## 实施顺序

1. 扩展数据库 schema 和测试数据
2. 完成评论后端接口与缓存失效
3. 完成统一搜索后端接口与缓存
4. 接入前端评论区交互
5. 接入前端搜索结果页
6. 做后端测试、前端验证和样式收口
