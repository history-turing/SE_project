# 武大树洞后端

> 本文档包含项目级核心信息，模块细节见 `modules/`。

---

## 1. 项目概览
- 目标: 为现有武大树洞前端提供可运行的 Spring Boot 后端，并补齐真实账号体系。
- 当前范围: 页面聚合查询、发帖互动、消息中心、武大邮箱注册、用户名密码登录、Redis 会话。
- 当前不含: 评论系统、文件上传、后台管理、细粒度权限角色。

## 2. 模块索引
| 模块 | 职责 | 状态 | 文档 |
|------|------|------|------|
| common | 通用响应与异常 | 稳定 | [common](modules/common.md) |
| domain | DTO、请求体、枚举 | 稳定 | [domain](modules/domain.md) |
| infra | MyBatis Mapper 与数据对象 | 稳定 | [infra](modules/infra.md) |
| server | 控制器、服务、认证拦截器、缓存配置 | 稳定 | [server](modules/server.md) |

## 3. 快速链接
- [项目技术约定](../project.md)
- [架构设计](arch.md)
- [API 手册](api.md)
- [数据模型](data.md)
- [变更历史](../history/index.md)
