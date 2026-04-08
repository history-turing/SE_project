# 任务清单: 武大树洞 React 化重构

目录: `helloagents/history/2026-04/202604081846_whu_treehole_react/`

---

## 1. 工程骨架
- [√] 1.1 在 `package.json`、`vite.config.ts`、`tsconfig*.json` 中建立 React + TypeScript + Vite 工程，验证 why.md#需求-首页树洞体验-场景-浏览首页内容
- [√] 1.2 在 `index.html` 与 `src/main.tsx` 中创建应用挂载入口，验证 why.md#需求-首页树洞体验-场景-浏览首页内容

## 2. 共享布局与状态
- [√] 2.1 在 `src/components/AppShell.tsx` 中实现共享导航、页脚与移动端入口，验证 why.md#需求-话题入口聚合-场景-进入指定话题
- [√] 2.2 在 `src/context/AppContext.tsx` 中实现帖子、收藏、关注与会话状态，验证 why.md#需求-首页树洞体验-场景-浏览首页内容，依赖任务2.1
- [√] 2.3 在 `src/components/ComposerModal.tsx` 与 `src/components/PostCard.tsx` 中实现发帖和互动组件，验证 why.md#需求-首页树洞体验-场景-浏览首页内容，依赖任务2.2

## 3. 页面迁移
- [√] 3.1 在 `src/pages/HomePage.tsx` 中迁移首页树洞流，验证 why.md#需求-首页树洞体验-场景-浏览首页内容
- [√] 3.2 在 `src/pages/TopicsPage.tsx` 中迁移话题页并实现跳转，验证 why.md#需求-话题入口聚合-场景-进入指定话题
- [√] 3.3 在 `src/pages/AlumniPage.tsx` 中迁移校友圈并实现筛选与关注，验证 why.md#需求-校友圈内容浏览-场景-浏览校友圈
- [√] 3.4 在 `src/pages/ProfilePage.tsx` 中迁移个人主页与消息中心，验证 why.md#需求-个人主页与消息-场景-发送站内消息

## 4. 安全检查
- [√] 4.1 执行安全检查（按G9: 不引入敏感信息、不连接外部生产服务、不写入密钥）

## 5. 文档更新
- [√] 5.1 更新 `helloagents/wiki/*.md`、`helloagents/project.md`、`helloagents/CHANGELOG.md`

## 6. 测试
- [√] 6.1 执行构建验证，确保新工程可被 Vite 正常编译


