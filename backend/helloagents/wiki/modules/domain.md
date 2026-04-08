# domain

## 目的
承载页面 DTO、认证请求体和领域枚举，保持接口契约与持久层解耦。

## 模块概览
- 职责: 页面聚合响应、互动请求体、认证请求/响应 DTO、发布范围枚举
- 状态: 稳定
- 最后更新: 2026-04-08

## 当前职责
- `PostCreateRequest`、`MessageCreateRequest` 等业务请求体
- `EmailCodeRequest`、`RegisterRequest`、`LoginRequest`、`AuthResponse` 等认证契约
- `AudienceType` 支持 `HOME/ALUMNI` 与中文标签双向兼容

## 变更历史
- [202604081956_whu_treehole_backend](../../history/2026-04/202604081956_whu_treehole_backend/) - 初始化页面 DTO 与领域枚举
- [202604082220_auth_login](../../history/2026-04/202604082220_auth_login/) - 新增认证 DTO 与发布范围兼容逻辑
