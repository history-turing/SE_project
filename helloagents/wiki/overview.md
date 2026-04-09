# WHU Treehole 全栈项目

> 本文件包含项目级别的核心信息。详细的模块文档见 `modules/` 目录。

---

## 1. 项目概述

### 目标与背景
本项目包含武大树洞的前端单页应用、Spring Boot 多模块后端以及 MySQL、Redis 基础设施。当前目标是补齐远程服务器的一键部署能力，降低环境搭建成本。

### 范围
- **范围内:** 前后端镜像构建、容器编排、容器内网络连通、数据库初始化、远程部署运行约定
- **范围外:** HTTPS 证书托管、CI/CD 平台接入、云厂商专属负载均衡配置

### 干系人
- **负责人:** 当前仓库维护者

---

## 2. 模块索引

| 模块名称 | 职责 | 状态 | 文档 |
|---------|------|------|------|
| frontend | React 页面渲染与浏览器交互 | 稳定 | [modules/frontend.md](modules/frontend.md) |
| backend | 提供 `/api/v1` 接口与认证、页面聚合能力 | 稳定 | [modules/backend.md](modules/backend.md) |
| deployment | 提供 Docker 镜像、Compose 编排与远程部署约定 | 稳定 | [modules/deployment.md](modules/deployment.md) |

---

## 3. 快速链接
- [技术约定](../project.md)
- [架构设计](arch.md)
- [API 手册](api.md)
- [数据模型](data.md)
- [变更历史](../history/index.md)
