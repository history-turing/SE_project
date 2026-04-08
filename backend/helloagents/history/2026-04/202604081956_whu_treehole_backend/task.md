# 任务清单: 武大树洞后端实现

目录: `helloagents/plan/202604081956_whu_treehole_backend/`

---

## 1. 工程骨架
- [√] 1.1 在 `backend/pom.xml` 和各子模块 `pom.xml` 中搭建 Maven 多模块工程，验证 why.md#需求-页面聚合接口-场景-首页与校友圈查询
- [√] 1.2 在 `backend/whu-treehole-server/src/main/resources` 中补充运行配置，验证 why.md#需求-页面聚合接口-场景-首页与校友圈查询

## 2. 数据与持久层
- [√] 2.1 在 `backend/whu-treehole-server/src/main/resources/db` 中实现 MySQL 建表与种子数据脚本，验证 why.md#需求-页面聚合接口-场景-首页与校友圈查询
- [√] 2.2 在 `backend/whu-treehole-infra` 中实现 Mapper 与 XML 查询，验证 why.md#需求-内容互动-场景-发帖与互动回写，依赖任务2.1

## 3. 应用服务与接口
- [√] 3.1 在 `backend/whu-treehole-server/src/main/java` 中实现页面聚合查询服务和控制器，验证 why.md#需求-页面聚合接口-场景-首页与校友圈查询，依赖任务2.2
- [√] 3.2 在 `backend/whu-treehole-server/src/main/java` 中实现发帖、点赞、收藏、关注和消息接口，验证 why.md#需求-内容互动-场景-发帖与互动回写，依赖任务3.1
- [√] 3.3 在 `backend/whu-treehole-server/src/main/java` 中实现消息会话读写逻辑，验证 why.md#需求-消息中心-场景-站内消息发送，依赖任务3.1

## 4. 安全检查
- [√] 4.1 执行安全检查（按G9: 输入验证、敏感信息处理、权限控制、EHRB风险规避）

## 5. 文档更新
- [√] 5.1 更新 `backend/helloagents/wiki/*.md` 与 `backend/helloagents/project.md`

## 6. 测试
- [√] 6.1 执行 Maven 构建验证，确认多模块工程可编译启动
