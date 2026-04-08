# 任务清单: 武大教育邮箱注册与账号登录

目录: `backend/helloagents/plan/202604082220_auth_login/`

---

## 1. 数据与配置
- [√] 1.1 更新 `backend/whu-treehole-server/src/main/resources/db/schema.sql`，补充认证相关表结构并兼容现有用户表
- [-] 1.2 更新 `backend/whu-treehole-server/src/main/resources/db/data.sql`，补充演示账号的认证数据
> 备注: 认证设计不提供默认账号密码，首次进入应通过邮箱注册生成真实凭证，避免在仓库中落明文演示密码。
- [√] 1.3 更新 `backend/whu-treehole-server/src/main/resources/application*.yml`，补充 SMTP 与认证配置

## 2. 后端认证链路
- [√] 2.1 在 `backend/whu-treehole-domain` 中新增认证请求/响应 DTO
- [√] 2.2 在 `backend/whu-treehole-infra` 中新增认证数据模型、Mapper 接口与 XML
- [√] 2.3 在 `backend/whu-treehole-server` 中实现验证码发送、注册、登录、查询当前用户、退出登录服务
- [√] 2.4 在 `backend/whu-treehole-server` 中实现认证拦截器与上下文，替换固定 `DemoUserContext`

## 3. 前端认证改造
- [√] 3.1 更新 `frontend/src/services/api.ts`，接入认证接口与 token 自动注入
- [√] 3.2 新增登录页、注册页和认证上下文
- [√] 3.3 更新路由与现有页面初始化逻辑，未登录时跳转认证页面

## 4. 文档与知识库
- [√] 4.1 更新 `backend/docs/deployment.md`，补充邮箱服务与联调步骤
- [√] 4.2 更新 `backend/helloagents/wiki/*.md`、`backend/helloagents/project.md`、`backend/helloagents/CHANGELOG.md`

## 5. 验证
- [√] 5.1 运行 Maven 测试或至少完成后端编译验证
- [√] 5.2 运行前端构建验证
- [√] 5.3 检查注册、登录与发帖链路是否指向真实用户
