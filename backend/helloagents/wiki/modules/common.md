# common

## 目的
提供统一响应体和统一业务异常，避免控制器重复处理返回格式。

## 模块概述
- **职责:** 封装 `ApiResponse` 与 `BusinessException`
- **状态:** ✅稳定
- **最后更新:** 2026-04-08

## 规范
### 需求: 统一 API 返回结构
**模块:** common
所有控制器接口都返回一致的 `code/message/data` 结构。

#### 场景: 正常返回
控制器成功处理请求后：
- 返回 `code = 0`
- `message = success`

#### 场景: 业务异常
发生业务错误时：
- 返回非零错误码
- 输出清晰错误信息

## 依赖
- domain

## 变更历史
- [202604081956_whu_treehole_backend](../../history/2026-04/202604081956_whu_treehole_backend/) - 初始化统一响应与异常机制
