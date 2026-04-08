# infra

## 目的
统一管理 MyBatis Mapper、SQL 映射和数据库读写对象。

## 模块概览
- 职责: 页面查询、互动写入、认证凭证读写、默认用户资料初始化
- 状态: 稳定
- 最后更新: 2026-04-08

## 当前职责
- `PortalQueryMapper` / `PortalCommandMapper`: 页面聚合与互动 SQL
- `AuthMapper`: 用户凭证、默认徽章、默认统计初始化
- `mapper/AuthMapper.xml`: `user_credentials` 相关 SQL 映射

## 变更历史
- [202604081956_whu_treehole_backend](../../history/2026-04/202604081956_whu_treehole_backend/) - 初始化页面查询与写入 Mapper
- [202604082220_auth_login](../../history/2026-04/202604082220_auth_login/) - 新增认证 Mapper 与凭证模型
