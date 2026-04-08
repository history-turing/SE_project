# 武大树洞

> 本文件包含项目级别的核心信息。详细的模块文档见 `modules/` 目录。

---

## 1. 项目概述

### 目标与背景
武大树洞是一个面向武汉大学学生与校友的树洞式 Web 社区。当前版本将原先互相独立的四个静态 HTML 页面整合为一个 React 单页应用，统一导航、视觉语言、数据结构和交互逻辑。

### 范围
- **范围内:** 首页信息流、话题聚合、校友圈、个人主页、站内消息、本地发布与互动
- **范围外:** 登录鉴权、后端 API、真实数据库、线上部署

### 干系人
- **负责人:** 当前仓库维护者

---

## 2. 模块索引

| 模块名称 | 职责 | 状态 | 文档 |
|---------|------|------|------|
| app-shell | 站点布局、导航、全局发布入口 | 🚧开发中 | [modules/app-shell.md](modules/app-shell.md) |
| home-feed | 首页树洞流、趋势区、筛选 | 🚧开发中 | [modules/home-feed.md](modules/home-feed.md) |
| topic-explorer | 话题分类与热点入口 | 🚧开发中 | [modules/topic-explorer.md](modules/topic-explorer.md) |
| alumni-circle | 校友内容、关系与筛选 | 🚧开发中 | [modules/alumni-circle.md](modules/alumni-circle.md) |
| profile-center | 个人资料、收藏、消息中心 | 🚧开发中 | [modules/profile-center.md](modules/profile-center.md) |

---

## 3. 快速链接
- [技术约定](../project.md)
- [架构设计](arch.md)
- [API 手册](api.md)
- [数据模型](data.md)
- [变更历史](../history/index.md)
