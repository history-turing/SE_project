# RBAC And Moderation Design

## 目标

为当前树洞项目补齐并扩展以下核心能力：

- 用户可以删除自己发布的帖子
- 评论作者可以删除自己的评论
- 被回复目标用户可以删除回复自己的评论
- 管理员可以删除任意帖子和评论
- 引入可扩展的表驱动 RBAC 权限体系
- 将现有账号 `xiewei` 初始化为 `SUPER_ADMIN`
- 支持普通管理员、超级管理员的后台管理能力
- 增加举报、审计、恢复能力，为后续持续扩展打底

本次设计的首要原则不是“最快做完”，而是“后续能持续扩展而不推翻底层设计”。

## 设计原则

- 可扩展优先：角色、权限、举报类型、审计动作均以数据模型驱动，不在业务代码中写死整套规则
- 业务与权限分层：RBAC 负责“是否具备某类能力”，业务层负责“当前用户与资源是什么关系”
- 删除默认软删：帖子、评论、举报单等业务记录优先保留状态，不直接物理删除
- 后台能力前后端闭环：不仅提供后端接口，还提供超管与管理员可操作的前端入口
- 审计优先：所有高风险管理动作必须记录操作日志
- 线上验证优先：功能是否完成，以远程服务器部署后的真实结果为准
- 后续改进显式化：如果后面发现更优设计，应优先通过新增角色、权限、状态、审计动作扩展，而不是重写认证链路

## 当前项目上下文

当前系统已经具备：

- Bearer token + Redis session 的登录态恢复链路
- 帖子创建、点赞、收藏能力
- 评论查询、创建、回复能力
- 前端评论面板基础交互
- MySQL、Redis、Docker、CI/CD 部署链路

当前系统的明显缺口：

- 没有删除帖子接口
- 没有删除评论接口
- 没有管理员、角色、权限、封禁概念
- 没有举报能力
- 没有审计日志
- 没有恢复已删除内容能力
- 当前鉴权仅识别“是否登录”，不识别“拥有哪些权限”

## 方案选型

### 方案 A：角色硬编码

做法：

- 代码中直接分支判断 `SUPER_ADMIN / ADMIN / USER`
- 不引入权限表

优点：

- 上手快，改动面小

缺点：

- 后续新增角色或细粒度权限时成本高
- 容易把业务判断和权限判断缠在一起
- 后续演进到运营、审核、学院管理员等角色时会返工

### 方案 B：表驱动 RBAC

做法：

- 新增 `roles / permissions / user_roles / role_permissions`
- 角色由数据库驱动
- 权限使用明确的 `permission code`
- 业务层再结合资源关系判定是否允许操作

优点：

- 扩展性最好
- 可支持未来更多角色和权限点
- 后台管理、封禁、举报、审计都可共用同一权限底座

缺点：

- 本次实现复杂度更高
- 需要同步改造鉴权、数据库、后端接口、前端当前用户上下文

### 方案 C：角色 + 自定义状态字段混合

做法：

- 保留固定角色
- 再给用户加一些额外状态和操作开关

优点：

- 比纯硬编码灵活

缺点：

- 复杂度接近表驱动 RBAC
- 规范性又不如标准 RBAC
- 后面仍容易走向混乱

## 选定方案

采用方案 B：表驱动 RBAC。

原因：

- 用户明确要求后续所有方案都要保证可扩展性
- 当前要补的不只是删除，还有管理员管理、封禁、举报、审计、恢复
- 这些能力若没有统一权限底座，后面维护成本会快速失控

## 权限模型设计

### 角色模型

本期内置三个系统角色：

- `SUPER_ADMIN`
- `ADMIN`
- `USER`

说明：

- 这三个角色是初始角色，不代表以后角色集合固定
- 用户允许拥有多个角色
- 系统角色通过 `system_flag` 标记，防止误删

### 权限模型

权限以字符串编码形式定义，示例：

- `post.create`
- `post.delete.own`
- `post.delete.any`
- `post.restore.any`
- `comment.create`
- `comment.reply`
- `comment.delete.own`
- `comment.delete.target`
- `comment.delete.any`
- `comment.restore.any`
- `report.create`
- `report.read.any`
- `report.assign`
- `report.resolve`
- `audit.read.moderation`
- `audit.read.all`
- `user.ban`
- `user.unban`
- `role.read.any`
- `role.assign.admin`
- `role.revoke.admin`

权限设计约束：

- 资源关系权限单独拆分，如 `own`、`target`、`any`
- 同类动作优先统一命名，便于后续查询、展示、批量授权
- 权限码面向业务能力，不直接编码实现细节

### 资源关系判断

RBAC 不直接表达“这是不是你的帖子”或“你是不是被回复的人”，这类判断由业务服务负责。

判定流程统一为：

1. 识别当前资源与当前用户的关系
2. 计算本次操作所需权限码
3. 判断当前用户是否拥有该权限
4. 不通过则返回 `403`

示例：

- 删除自己的帖子，需要 `post.delete.own`
- 删除任意帖子，需要 `post.delete.any`
- 删除回复给自己的评论，需要 `comment.delete.target`

## 删除与恢复策略

### 软删与物理删除

软删：

- 不真正删除数据库记录
- 通过 `deleted_flag`、`deleted_at`、`deleted_by` 等字段标记已删除
- 正常查询时过滤已删除记录
- 保留数据用于举报追溯、审计、恢复、统计

物理删除：

- 直接执行数据库删除操作
- 数据不可直接恢复
- 容易破坏引用关系与审计链

本项目所有业务主流程默认采用软删，不在主链路中使用物理删除。

### 帖子删除规则

- 帖子作者可以删除自己的帖子
- `ADMIN` 和 `SUPER_ADMIN` 可以删除任意帖子
- 删除帖子后帖子不再出现在常规查询中
- 帖子删除必须记录操作者和时间
- 被删帖子允许后续由管理员恢复

### 评论删除规则

- 评论作者可以删除自己的评论
- 被回复目标用户可以删除回复自己的评论
- `ADMIN` 和 `SUPER_ADMIN` 可以删除任意评论
- 评论删除采用软删
- 被删评论本期从列表中过滤，不展示“该评论已删除”占位

### 恢复规则

- 普通用户没有恢复权限
- `ADMIN` 和 `SUPER_ADMIN` 可以恢复帖子和评论
- 恢复行为必须记录审计日志
- 恢复后内容重新回到正常查询结果中

## 数据库设计

### 新增 RBAC 表

#### `roles`

字段建议：

- `id`
- `code`
- `name`
- `description`
- `system_flag`
- `created_at`
- `updated_at`

约束：

- `code` 唯一

#### `permissions`

字段建议：

- `id`
- `code`
- `name`
- `description`
- `module`
- `created_at`
- `updated_at`

约束：

- `code` 唯一

#### `user_roles`

字段建议：

- `id`
- `user_id`
- `role_id`
- `created_at`
- `created_by`

约束：

- `UNIQUE(user_id, role_id)`

#### `role_permissions`

字段建议：

- `id`
- `role_id`
- `permission_id`
- `created_at`
- `created_by`

约束：

- `UNIQUE(role_id, permission_id)`

### 用户状态扩展

在用户主信息或认证关联表中增加：

- `account_status`
- `status_reason`
- `status_updated_at`
- `status_updated_by`

推荐状态值：

- `ACTIVE`
- `BANNED`

说明：

- 后续若要增加 `PENDING_REVIEW`、`DISABLED`、`DELETED`，沿用同一状态模型扩展

### 帖子表扩展

在 `posts` 上增加：

- `deleted_flag`
- `deleted_at`
- `deleted_by`

### 评论表扩展

在 `post_comments` 上补充：

- `deleted_at`
- `deleted_by`

### 举报表

新增 `reports` 表。

字段建议：

- `id`
- `report_code`
- `target_type`
- `target_id`
- `reporter_user_id`
- `reason_code`
- `reason_detail`
- `status`
- `assignee_user_id`
- `resolution_code`
- `resolution_note`
- `created_at`
- `updated_at`
- `resolved_at`
- `deleted_flag`

推荐枚举：

- `target_type`: `POST / COMMENT / USER`
- `status`: `PENDING / IN_REVIEW / RESOLVED / REJECTED`

### 审计表

新增 `audit_logs` 表。

字段建议：

- `id`
- `actor_user_id`
- `actor_role_snapshot`
- `action_type`
- `resource_type`
- `resource_id`
- `resource_code`
- `detail_json`
- `created_at`

说明：

- `detail_json` 用于记录变化前后状态、处理说明、上下文信息
- 后续新增审计动作时无需频繁改表

## 初始化与迁移设计

在 `schema.sql` 中新增：

- RBAC 相关表
- 举报表
- 审计表
- 用户状态字段
- 帖子软删字段
- 评论删除补充字段

在 `data.sql` 中新增：

- 系统角色
- 核心权限码
- 角色与权限映射
- 将 `xiewei` 绑定为 `SUPER_ADMIN`
- 为现有用户赋予 `USER` 角色

迁移要求：

- 设计为幂等执行，避免 CI/CD 重复同步数据库时报错
- 不能依赖一次性人工执行
- 线上初始化后应允许继续追加角色、权限和映射

## 权限分配

### `USER`

- `post.create`
- `post.delete.own`
- `comment.create`
- `comment.reply`
- `comment.delete.own`
- `comment.delete.target`
- `report.create`

### `ADMIN`

继承 `USER` 基础能力，并增加：

- `post.delete.any`
- `post.restore.any`
- `comment.delete.any`
- `comment.restore.any`
- `report.read.any`
- `report.assign`
- `report.resolve`
- `audit.read.moderation`
- `user.ban`
- `user.unban`

限制：

- 不能任命管理员
- 不能撤销管理员
- 不能管理 `SUPER_ADMIN`

### `SUPER_ADMIN`

拥有 `ADMIN` 全部能力，并增加：

- `role.read.any`
- `role.assign.admin`
- `role.revoke.admin`
- `audit.read.all`

说明：

- 后续如需开放权限管理页面，可再增加 `permission.read.any`、`permission.manage`
- 本期不做自定义角色编辑器，但底层结构允许后续加入

## 用户管理规则

### 角色管理

- 只有 `SUPER_ADMIN` 可以任命普通管理员
- 只有 `SUPER_ADMIN` 可以撤销普通管理员
- `ADMIN` 不能任命或撤销任何管理员角色

### 封禁规则

- `ADMIN` 和 `SUPER_ADMIN` 都可以封禁普通用户
- `SUPER_ADMIN` 可以封禁 `ADMIN`
- `ADMIN` 不能封禁 `ADMIN`
- 任意角色都不能封禁 `SUPER_ADMIN`

### 封禁效果

被封禁用户不能执行写操作，包括：

- 发帖
- 评论
- 回复
- 点赞
- 收藏
- 删除自己的帖子或评论
- 发起举报

说明：

- 已有内容默认保留
- 封禁与内容删除不是同一动作

## 后端接口设计

### 用户侧接口

- `DELETE /api/v1/posts/{postCode}`
- `DELETE /api/v1/posts/{postCode}/comments/{commentCode}`
- `POST /api/v1/reports`

`POST /api/v1/reports` 请求体建议包含：

- `targetType`
- `targetCode`
- `reasonCode`
- `reasonDetail`

### 管理侧接口

- `GET /api/v1/admin/reports`
- `GET /api/v1/admin/reports/{reportCode}`
- `POST /api/v1/admin/reports/{reportCode}/assign`
- `POST /api/v1/admin/reports/{reportCode}/resolve`
- `POST /api/v1/admin/users/{userCode}/ban`
- `POST /api/v1/admin/users/{userCode}/unban`
- `GET /api/v1/admin/users`
- `GET /api/v1/admin/roles`
- `POST /api/v1/admin/users/{userCode}/roles`
- `DELETE /api/v1/admin/users/{userCode}/roles/{roleCode}`
- `GET /api/v1/admin/audit-logs`
- `GET /api/v1/admin/audit-logs/{id}`

### 举报处理动作

举报处理动作建议支持：

- `NO_ACTION`
- `DELETE_POST`
- `DELETE_COMMENT`
- `BAN_USER`
- `RESTORE_POST`
- `RESTORE_COMMENT`

后续可扩展：

- `ESCALATE`
- `MARK_DUPLICATE`
- `WARN_USER`

## 鉴权链路设计

### 登录态与权限上下文

登录成功后，当前用户信息中增加：

- `roles`
- `permissions`
- `accountStatus`

### 鉴权实现分层

- `AuthInterceptor` 继续负责认证，识别用户身份
- 新增统一的授权服务负责角色、权限、状态判断
- 业务服务不直接散落写角色字符串判断

推荐职责划分：

- 认证层：解析 token、加载 session、识别当前用户
- 授权层：判断某个用户是否拥有权限码
- 业务层：判断当前资源关系并调用授权层

### 被封禁用户处理

- 被封禁用户的写操作统一在鉴权流程中拦截
- 被封禁用户允许完成登录并读取基础信息及公开内容，用于展示封禁状态与申诉提示
- 只要命中写操作接口，一律返回 `USER_BANNED`

## 前端设计

### 用户侧入口

帖子卡片：

- 有权限时显示“删除”
- 所有用户都可见“举报”

评论区：

- 满足权限条件时显示“删除”
- 评论和回复都可见“举报”
- 保留现有超过三条根评论折叠逻辑

### 管理侧入口

新增管理页或管理面板，至少包含：

- 举报处理
- 用户管理
- 角色管理
- 审计日志

显示规则：

- `ADMIN` 可见举报处理、用户管理、审计日志
- `SUPER_ADMIN` 额外可见角色管理

### 当前用户上下文

前端当前用户模型需要增加：

- `roles`
- `permissions`
- `accountStatus`

前端按钮显示逻辑依赖权限，不依赖前端硬编码角色分支作为唯一依据。

## 审计设计

以下动作必须写入 `audit_logs`：

- 删除帖子
- 恢复帖子
- 删除评论
- 恢复评论
- 封禁用户
- 解封用户
- 任命管理员
- 撤销管理员
- 举报分配
- 举报处理

审计读取范围：

- `ADMIN` 仅可查看 moderation 相关审计
- `SUPER_ADMIN` 可查看全量审计

## 错误处理

统一状态码建议：

- 未登录：`401`
- 权限不足：`403`
- 资源不存在或不可见：`404`
- 状态冲突：`409`

建议新增业务错误码：

- `AUTH_FORBIDDEN`
- `USER_BANNED`
- `POST_ALREADY_DELETED`
- `COMMENT_ALREADY_DELETED`
- `POST_ALREADY_RESTORED`
- `COMMENT_ALREADY_RESTORED`
- `REPORT_ALREADY_RESOLVED`
- `ROLE_ASSIGNMENT_FORBIDDEN`

## 测试设计

### 后端测试

- 用户删除自己的帖子
- 用户删除自己的评论
- 被回复对象删除回复自己的评论
- 普通用户删除别人内容失败
- 管理员删除任意内容成功
- 管理员恢复帖子和评论成功
- 普通用户恢复内容失败
- 超管任命管理员成功
- 管理员任命管理员失败
- 管理员封禁普通用户成功
- 管理员封禁管理员失败
- 超管封禁管理员成功
- 任意角色封禁超管失败
- 举报创建、分配、处理、恢复链路正确
- 审计日志正确落库
- 软删后查询过滤，恢复后重新可见
- 角色与权限关联查询正确

### 前端测试

- 有权限时显示删除按钮
- 无权限时不显示删除按钮
- 举报按钮提交流程
- 管理后台角色管理、封禁、举报处理交互
- 评论超过三条折叠逻辑不受影响

### 线上验证

完成实现后按以下顺序验证：

1. 本地测试通过
2. 提交到 `main`
3. 等待 CI/CD 完成
4. SSH 到远程服务器验证最新 commit 已部署
5. 检查容器状态、后端日志、数据库结构
6. 直接调用远程接口验证 RBAC、删除、举报、恢复、审计
7. 在远程页面实际操作验证前端入口与权限显示

## 代码修改范围

后端预计涉及：

- `backend/whu-treehole-server/src/main/resources/db/schema.sql`
- `backend/whu-treehole-server/src/main/resources/db/data.sql`
- `backend/whu-treehole-server/src/main/java/.../controller`
- `backend/whu-treehole-server/src/main/java/.../service`
- `backend/whu-treehole-server/src/main/java/.../support`
- `backend/whu-treehole-infra/src/main/java/.../mapper`
- `backend/whu-treehole-infra/src/main/resources/mapper/*.xml`

前端预计涉及：

- `frontend/src/components/PostCard.tsx`
- `frontend/src/components/PostCommentsPanel.tsx`
- `frontend/src/pages/*`
- `frontend/src/services/api.ts`
- `frontend/src/context/*`
- `frontend/src/types.ts`

## 非目标

本期不包含以下内容：

- 自定义角色可视化编排器
- 权限拖拽配置界面
- 复杂审批流
- 用户申诉中心
- 举报消息通知系统
- 审计日志导出
- 长期归档清理任务

这些能力后续都可以在本设计基础上扩展，而无需重写 RBAC 主链路。

## 实施顺序

1. 扩展数据库 schema 与种子数据
2. 接入 RBAC 基础查询与授权服务
3. 完成帖子、评论删除与恢复后端能力
4. 完成封禁、角色管理、举报、审计后端接口
5. 扩展前端当前用户上下文与权限判断
6. 接入帖子、评论删除与举报入口
7. 接入管理后台页面
8. 完成测试、CI/CD 部署与远程验证
