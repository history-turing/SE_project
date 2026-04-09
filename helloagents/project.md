# 项目技术约定

---

## 技术栈
- **前端:** TypeScript / React / Vite / Nginx
- **后端:** Java 17 / Spring Boot 3.3 / Maven 多模块
- **数据:** MySQL 8 / Redis 7
- **部署:** Docker / Docker Compose

---

## 开发约定
- 前端通过 Nginx 提供静态资源，并反向代理 `/api/v1` 到后端容器。
- 后端所有外部依赖地址、账号和密码均通过环境变量注入，不在仓库中保存可直接使用的敏感值。
- 数据库初始化优先使用容器启动时挂载的 SQL 文件，避免与应用内初始化脚本重复执行。

---

## 错误与日志
- **策略:** 容器日志通过 `docker compose logs` 统一查看，应用内部保留默认控制台日志。
- **健康检查:** MySQL 与 Redis 使用容器健康检查，后端通过重启策略等待依赖就绪。

---

## 测试与流程
- 后端修改后至少执行 `mvn -q -s settings.xml -pl whu-treehole-server -am test`。
- 前端修改后至少执行 `npm run build`。
- 部署配置修改后需保证 `docker compose up -d --build` 可以完成远程服务器一键启动。
