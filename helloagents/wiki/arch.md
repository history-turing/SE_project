# 架构设计

## 总体架构
```mermaid
flowchart LR
    Browser[浏览器] --> Nginx[frontend 容器 / Nginx]
    Nginx -->|/api/v1| Backend[backend 容器 / Spring Boot]
    Backend --> MySQL[(mysql 容器)]
    Backend --> Redis[(redis 容器)]
    MySQL --> InitSQL[docker/se_project.sql]
```

## 技术栈
- **后端:** Java 17 / Spring Boot / Maven / MyBatis
- **前端:** React / Vite / Nginx
- **数据:** MySQL 8 / Redis 7
- **部署:** Docker Compose

## 核心流程
```mermaid
sequenceDiagram
    participant User as 用户浏览器
    participant FE as frontend(Nginx)
    participant BE as backend(Spring Boot)
    participant DB as MySQL
    participant Cache as Redis

    User->>FE: 访问页面
    FE-->>User: 返回静态资源
    User->>FE: 请求 /api/v1/*
    FE->>BE: 反向代理接口请求
    BE->>DB: 查询/写入业务数据
    BE->>Cache: 读写缓存/验证码/会话
    BE-->>FE: 返回 JSON
    FE-->>User: 渲染结果
```

## 重大架构决策
完整的 ADR 存储在各变更的 how.md 中，本章节提供索引。

| adr_id | title | date | status | affected_modules | details |
|--------|-------|------|--------|------------------|---------|
| ADR-20260409-01 | 使用 Nginx 统一承载前端静态资源与 API 代理 | 2026-04-09 | ✅已采纳 | frontend,deployment | [../history/2026-04/202604091701_remote_docker_deploy/how.md#adr-20260409-01-使用-nginx-统一承载前端静态资源与-api-代理](../history/2026-04/202604091701_remote_docker_deploy/how.md#adr-20260409-01-使用-nginx-统一承载前端静态资源与-api-代理) |
| ADR-20260409-02 | 使用环境变量替代仓库中的容器运行敏感配置 | 2026-04-09 | ✅已采纳 | backend,deployment | [../history/2026-04/202604091701_remote_docker_deploy/how.md#adr-20260409-02-使用环境变量替代仓库中的容器运行敏感配置](../history/2026-04/202604091701_remote_docker_deploy/how.md#adr-20260409-02-使用环境变量替代仓库中的容器运行敏感配置) |
