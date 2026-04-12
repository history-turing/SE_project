INSERT IGNORE INTO users (id, user_code, name, tagline, college, grade_year, bio, avatar_url, created_at) VALUES
(1, 'me', '樱花味猫奴', '把校园生活慢慢写成一册柔软的日志。', '信息管理学院', '2022 级', '喜欢晚霞、热干面、图书馆靠窗位置，也喜欢把看似平凡的瞬间认真记下来。', 'https://example.com/avatar/me.jpg', '2026-04-08 10:00:00');

INSERT INTO users (user_code, name, tagline, college, grade_year, bio, avatar_url, created_at)
SELECT 'xiewei',
       'xiewei',
       '维护树洞系统，也认真记录校园生活。',
       '信息管理学院',
       '2022 级',
       '系统初始化的超级管理员账号。',
       'https://example.com/avatar/xiewei.jpg',
       '2026-04-08 10:05:00'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE user_code = 'xiewei'
);

INSERT IGNORE INTO user_badges (id, user_id, badge_name, sort_order) VALUES
(1, 1, '树洞记录者', 1),
(2, 1, '春招互助', 2),
(3, 1, '东湖散步搭子', 3);

INSERT IGNORE INTO user_profile_stats (id, user_id, stat_label, stat_value, sort_order) VALUES
(1, 1, '已发树洞', '18', 1),
(2, 1, '收藏内容', '27', 2),
(3, 1, '已建立私信', '9', 3);

INSERT INTO user_badges (user_id, badge_name, sort_order)
SELECT u.id, '邮箱已认证', 1
FROM users u
WHERE u.user_code = 'xiewei'
  AND NOT EXISTS (
      SELECT 1
      FROM user_badges ub
      WHERE ub.user_id = u.id
        AND ub.badge_name = '邮箱已认证'
  );

INSERT INTO user_profile_stats (user_id, stat_label, stat_value, sort_order)
SELECT u.id, '已发树洞', '0', 1
FROM users u
WHERE u.user_code = 'xiewei'
  AND NOT EXISTS (
      SELECT 1
      FROM user_profile_stats ups
      WHERE ups.user_id = u.id
        AND ups.stat_label = '已发树洞'
  );

INSERT INTO user_profile_stats (user_id, stat_label, stat_value, sort_order)
SELECT u.id, '收藏内容', '0', 2
FROM users u
WHERE u.user_code = 'xiewei'
  AND NOT EXISTS (
      SELECT 1
      FROM user_profile_stats ups
      WHERE ups.user_id = u.id
        AND ups.stat_label = '收藏内容'
  );

INSERT INTO user_profile_stats (user_id, stat_label, stat_value, sort_order)
SELECT u.id, '已建立私信', '0', 3
FROM users u
WHERE u.user_code = 'xiewei'
  AND NOT EXISTS (
      SELECT 1
      FROM user_profile_stats ups
      WHERE ups.user_id = u.id
        AND ups.stat_label = '已建立私信'
  );

INSERT INTO user_credentials (
    user_id,
    email,
    username,
    password_hash,
    email_verified_at,
    last_login_at,
    created_at,
    updated_at
)
SELECT u.id,
       'xiewei@whu.edu.cn',
       'xiewei',
       '$2a$10$Mdu24keixSODrG.puvozUOpLv0tTVtLG7F7xmSfPkvXEgkggNEq.2',
       '2026-04-08 10:05:00',
       '2026-04-08 10:05:00',
       '2026-04-08 10:05:00',
       '2026-04-08 10:05:00'
FROM users u
WHERE u.user_code = 'xiewei'
  AND NOT EXISTS (
      SELECT 1
      FROM user_credentials uc
      WHERE uc.username = 'xiewei'
         OR uc.email = 'xiewei@whu.edu.cn'
  );

-- Stable test accounts for remote acceptance and RBAC verification.
INSERT INTO users (user_code, name, tagline, college, grade_year, bio, avatar_url, created_at)
SELECT 'user-codex-super',
       'codex-super',
       '用于超级管理员验收的稳定测试账号。',
       '测试账号',
       '系统种子',
       '保留用于超级管理员权限、公告投放与全局治理验收。',
       'https://example.com/avatar/codex-super.jpg',
       '2026-04-13 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM users u
    WHERE u.user_code = 'user-codex-super'
)
  AND NOT EXISTS (
    SELECT 1
    FROM user_credentials uc
    WHERE uc.username = 'codex-super'
       OR uc.email = 'codex-super@whu.edu.cn'
  );

INSERT INTO users (user_code, name, tagline, college, grade_year, bio, avatar_url, created_at)
SELECT 'user-codex-user',
       'codex-user',
       '用于普通用户验收的稳定测试账号。',
       '测试账号',
       '系统种子',
       '保留用于普通发帖、评论、举报和私信流程验收。',
       'https://example.com/avatar/codex-user.jpg',
       '2026-04-13 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM users u
    WHERE u.user_code = 'user-codex-user'
)
  AND NOT EXISTS (
    SELECT 1
    FROM user_credentials uc
    WHERE uc.username = 'codex-user'
       OR uc.email = 'codex-user@whu.edu.cn'
  );

INSERT INTO users (user_code, name, tagline, college, grade_year, bio, avatar_url, created_at)
SELECT 'user-codex-promote',
       'codex-promote',
       '用于角色提升验收的稳定测试账号。',
       '测试账号',
       '系统种子',
       '保留用于从普通用户提拔为管理员、再回收权限的验收。',
       'https://example.com/avatar/codex-promote.jpg',
       '2026-04-13 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM users u
    WHERE u.user_code = 'user-codex-promote'
)
  AND NOT EXISTS (
    SELECT 1
    FROM user_credentials uc
    WHERE uc.username = 'codex-promote'
       OR uc.email = 'codex-promote@whu.edu.cn'
  );

INSERT INTO user_credentials (
    user_id,
    email,
    username,
    password_hash,
    email_verified_at,
    last_login_at,
    created_at,
    updated_at
)
SELECT u.id,
       'codex-super@whu.edu.cn',
       'codex-super',
       '$2a$10$F6NFYwJ4okPAS4BcNi5eYOB2ZZtCYgr4XBdpA3Ov3XQ5Q21ihChZK',
       '2026-04-13 09:00:00',
       '2026-04-13 09:00:00',
       '2026-04-13 09:00:00',
       '2026-04-13 09:00:00'
FROM users u
WHERE u.user_code = 'user-codex-super'
  AND NOT EXISTS (
      SELECT 1
      FROM user_credentials uc
      WHERE uc.username = 'codex-super'
         OR uc.email = 'codex-super@whu.edu.cn'
  );

INSERT INTO user_credentials (
    user_id,
    email,
    username,
    password_hash,
    email_verified_at,
    last_login_at,
    created_at,
    updated_at
)
SELECT u.id,
       'codex-user@whu.edu.cn',
       'codex-user',
       '$2a$10$F6NFYwJ4okPAS4BcNi5eYOB2ZZtCYgr4XBdpA3Ov3XQ5Q21ihChZK',
       '2026-04-13 09:00:00',
       '2026-04-13 09:00:00',
       '2026-04-13 09:00:00',
       '2026-04-13 09:00:00'
FROM users u
WHERE u.user_code = 'user-codex-user'
  AND NOT EXISTS (
      SELECT 1
      FROM user_credentials uc
      WHERE uc.username = 'codex-user'
         OR uc.email = 'codex-user@whu.edu.cn'
  );

INSERT INTO user_credentials (
    user_id,
    email,
    username,
    password_hash,
    email_verified_at,
    last_login_at,
    created_at,
    updated_at
)
SELECT u.id,
       'codex-promote@whu.edu.cn',
       'codex-promote',
       '$2a$10$F6NFYwJ4okPAS4BcNi5eYOB2ZZtCYgr4XBdpA3Ov3XQ5Q21ihChZK',
       '2026-04-13 09:00:00',
       '2026-04-13 09:00:00',
       '2026-04-13 09:00:00',
       '2026-04-13 09:00:00'
FROM users u
WHERE u.user_code = 'user-codex-promote'
  AND NOT EXISTS (
      SELECT 1
      FROM user_credentials uc
      WHERE uc.username = 'codex-promote'
         OR uc.email = 'codex-promote@whu.edu.cn'
  );

INSERT INTO user_badges (user_id, badge_name, sort_order)
SELECT u.id, '测试账号', 1
FROM users u
WHERE u.user_code IN ('user-codex-super', 'user-codex-user', 'user-codex-promote')
  AND NOT EXISTS (
      SELECT 1
      FROM user_badges ub
      WHERE ub.user_id = u.id
        AND ub.badge_name = '测试账号'
  );

INSERT INTO user_profile_stats (user_id, stat_label, stat_value, sort_order)
SELECT u.id, '已发树洞', '0', 1
FROM users u
WHERE u.user_code IN ('user-codex-super', 'user-codex-user', 'user-codex-promote')
  AND NOT EXISTS (
      SELECT 1
      FROM user_profile_stats ups
      WHERE ups.user_id = u.id
        AND ups.stat_label = '已发树洞'
  );

INSERT INTO user_profile_stats (user_id, stat_label, stat_value, sort_order)
SELECT u.id, '收藏内容', '0', 2
FROM users u
WHERE u.user_code IN ('user-codex-super', 'user-codex-user', 'user-codex-promote')
  AND NOT EXISTS (
      SELECT 1
      FROM user_profile_stats ups
      WHERE ups.user_id = u.id
        AND ups.stat_label = '收藏内容'
  );

INSERT INTO user_profile_stats (user_id, stat_label, stat_value, sort_order)
SELECT u.id, '已建立私信', '0', 3
FROM users u
WHERE u.user_code IN ('user-codex-super', 'user-codex-user', 'user-codex-promote')
  AND NOT EXISTS (
      SELECT 1
      FROM user_profile_stats ups
      WHERE ups.user_id = u.id
        AND ups.stat_label = '已建立私信'
  );

INSERT IGNORE INTO topics (id, topic_code, name, description, heat_text, destination_type, accent_tone, emoji, sort_order) VALUES
(1, 'confession', '表白墙', '把没说出口的话，放进珞珈山的风里。', '1.2k 正在热议', 'CAMPUS', 'rose', '💗', 1),
(2, 'lost-found', '失物招领', '连接遗落的时光，帮物品重新找到主人。', '450+ 待认领', 'CAMPUS', 'jade', '🔎', 2),
(3, 'study', '学业交流', '课程、考研、复习与经验都在这里汇流。', '学术研讨中', 'CAMPUS', 'gold', '📚', 3),
(4, 'campus-chat', '校园日常', '吐槽食堂、分享晚霞、记录普通却动人的一天。', '深夜食堂', 'CAMPUS', 'ink', '☕', 4),
(5, 'career', '职场内推', '把校友网络织进求职旅程，给下一位武大人一束光。', '158 条机会', 'ALUMNI', 'jade', '💼', 5),
(6, 'alumni-stories', '校友故事', '看见从珞珈到世界各地的人，如何讲述自己的路。', '持续更新', 'ALUMNI', 'rose', '🌸', 6);

INSERT IGNORE INTO topic_tags (id, topic_id, tag_name, sort_order) VALUES
(1, 1, '心动瞬间', 1),
(2, 1, '暗恋日记', 2),
(3, 1, '春日樱花', 3),
(4, 2, '校园卡', 1),
(5, 2, '钥匙', 2),
(6, 2, '雨伞', 3),
(7, 3, '期末周', 1),
(8, 3, '考研经验', 2),
(9, 3, '课程互助', 3),
(10, 4, '食堂测评', 1),
(11, 4, '东湖日落', 2),
(12, 4, '宿舍闲聊', 3),
(13, 5, '产品经理', 1),
(14, 5, '算法岗', 2),
(15, 5, '春招', 3),
(16, 6, '行业成长', 1),
(17, 6, '返校记忆', 2),
(18, 6, '人生选择', 3);

INSERT IGNORE INTO topic_rankings (id, ranking_code, label, heat_text, sort_order) VALUES
(1, 'rank-1', '#樱花季预约', '45.2w 热度', 1),
(2, 'rank-2', '#图书馆占座', '32.8w 热度', 2),
(3, 'rank-3', '#梅园食堂新品', '28.5w 热度', 3),
(4, 'rank-4', '#珞珈山猫咪图鉴', '15.1w 热度', 4),
(5, 'rank-5', '#春招提前批', '12.3w 热度', 5);

INSERT IGNORE INTO notices (id, notice_code, title, meta, sort_order) VALUES
(1, 'notice-1', '樱花开放期间校园管理措施更新', '置顶公告', 1),
(2, 'notice-2', '图书馆预约系统今晚 23:00 维护', '系统通知', 2),
(3, 'notice-3', '东湖夜跑社团本周五集合', '社团活动', 3);

INSERT IGNORE INTO posts (id, post_code, creator_user_id, title, content, author_name, author_handle, topic_name, audience_type, display_time, like_count, comment_count, save_count, accent_tone, badge, image_url, anonymous_flag, location, created_at) VALUES
(1, 'home-1', 1, '今早的老斋舍，樱花落了一地', '时间好像在这里走得很慢。看到花瓣落在台阶上，突然觉得三年前第一次进校门的那个人还在眼前。', '小狐狸树洞', '匿名珞珈人', '校园日常', 'HOME', '2 小时前', 421, 56, 18, 'rose', NULL, 'https://example.com/posts/home-1.jpg', 1, NULL, '2026-04-08 08:00:00'),
(2, 'home-2', 1, '有没有跨专业考研到法学院的前辈？', '本科是理科生，最近开始认真查资料了。求经验、求书单，哪怕一点建议也很珍贵。', '匿名用户', '学业互助', '学业交流', 'HOME', '今天 11:20', 89, 24, 37, 'gold', NULL, NULL, 1, NULL, '2026-04-08 11:20:00'),
(3, 'home-3', 1, NULL, '东湖的晚霞真的很适合给焦虑按下暂停键。今晚风很大，心却很静。', '追夕阳的人', '湖边散步计划', '校园日常', 'HOME', '昨天', 842, 45, 61, 'ink', NULL, 'https://example.com/posts/home-3.jpg', 0, NULL, '2026-04-07 18:30:00'),
(4, 'home-4', 1, '终于在工学部食堂吃到了今天最满意的一碗热干面', '如果有人也在做武大食堂巡礼，真心推荐这家。辣油香得刚刚好，面也够劲道。', '干饭人小张', '食堂雷达', '校园日常', 'HOME', '昨天', 3200, 128, 205, 'jade', NULL, 'https://example.com/posts/home-4.jpg', 0, NULL, '2026-04-07 12:00:00'),
(5, 'alumni-1', 1, '回母校走走，樱花大道依旧，只是少年已不再', '今天趁着出差回了趟武大，看到图书馆里埋头苦读的学弟学妹，仿佛看到了当年的自己。', '陈先生', '2015 级校友', '校友故事', 'ALUMNI', '深圳 · 2 小时前', 1200, 86, 53, 'rose', '返校日记', 'https://example.com/posts/alumni-1.jpg', 0, '深圳', '2026-04-08 09:00:00'),
(6, 'alumni-2', 1, '【字节跳动】产品经理 / 研发校招社招内推', '部门直招，校友内推简历直达 HR。感兴趣的同学或校友可以直接私信我。', '林学姐', '2018 级校友', '职场内推', 'ALUMNI', '上海 · 5 小时前', 452, 120, 141, 'jade', '机会速递', NULL, 0, '上海', '2026-04-08 06:30:00'),
(7, 'alumni-3', 1, '支教归来的这一年，重新理解了成长', '离开校园后才发现，很多答案不是在课堂里得到的，而是在与真实世界的相遇里慢慢长出来的。', '李校友', '2018 级 · 文学院', '校友故事', 'ALUMNI', '成都 · 昨天', 284, 41, 29, 'gold', '成长故事', NULL, 0, '成都', '2026-04-07 19:30:00'),
(8, 'me-1', 1, '想给第一次来武大的朋友做一份散步地图', '从凌波门日出到东湖绿道，如果你只能在武大待一天，我很想把这条线送给你。', '樱花味猫奴', '我的树洞', '校园日常', 'HOME', '3 天前', 96, 14, 22, 'rose', NULL, NULL, 0, NULL, '2026-04-05 08:00:00'),
(9, 'me-2', 1, '如果你也在准备春招，我整理了一份时间线', '把最近看到的笔试、投递和面试时间都汇总到了便签里。希望能帮到正在赶路的人。', '樱花味猫奴', '我的树洞', '职场内推', 'ALUMNI', '1 周前', 143, 27, 49, 'jade', NULL, NULL, 0, NULL, '2026-04-01 20:00:00');

INSERT IGNORE INTO post_interactions (id, user_id, post_id, liked, saved, updated_at) VALUES
(1, 1, 1, 1, 0, '2026-04-08 12:00:00'),
(2, 1, 2, 0, 1, '2026-04-08 12:00:00'),
(3, 1, 5, 1, 1, '2026-04-08 12:00:00'),
(4, 1, 8, 0, 1, '2026-04-08 12:00:00');

INSERT IGNORE INTO post_comments (
    id, comment_code, post_id, user_id, parent_comment_id, root_comment_id, reply_to_user_id,
    author_name, author_handle, content, deleted_flag, created_at, updated_at
) VALUES
(1, 'comment-seed-root', 1, 1, NULL, NULL, NULL, '樱花味猫咪', '信息管理学院 · 2022', '春天的老斋舍真的很适合发呆。', 0, '2026-04-08 09:10:00', '2026-04-08 09:10:00'),
(2, 'comment-seed-reply', 1, 1, 1, 1, 1, '樱花味猫咪', '信息管理学院 · 2022', '而且早上的光线特别好。', 0, '2026-04-08 09:15:00', '2026-04-08 09:15:00');

INSERT IGNORE INTO alumni_stories (id, story_code, title, meta, sort_order) VALUES
(1, 'story-1', '从珞珈山到硅谷', '2012 级 计算机学院 · 张校友', 1),
(2, 'story-2', '支教归来的这一年', '2018 级 文学院 · 李校友', 2),
(3, 'story-3', '转行做纪录片导演之后', '2014 级 新闻学院 · 吴校友', 3);

INSERT IGNORE INTO alumni_contacts (id, contact_code, name, meta, focus, avatar_url, sort_order) VALUES
(1, 'wang', '王博士', '2010 级 · 物理学院', '材料与科研合作', 'https://example.com/contacts/wang.jpg', 1),
(2, 'zhao', '赵设计师', '2019 级 · 艺术学院', '品牌与视觉设计', 'https://example.com/contacts/zhao.jpg', 2),
(3, 'zhou', '周律师', '2008 级 · 法学院', '法律咨询与职业路径', 'https://example.com/contacts/zhou.jpg', 3);

INSERT IGNORE INTO user_follow_contacts (id, user_id, contact_id, followed, updated_at) VALUES
(1, 1, 1, 1, '2026-04-08 12:00:00');

-- RBAC seed data
INSERT IGNORE INTO roles (code, name, description, system_flag) VALUES
('SUPER_ADMIN', 'Super Admin', 'System super administrator', 1),
('ADMIN', 'Admin', 'Moderator administrator', 1),
('USER', 'User', 'Normal authenticated user', 1);

INSERT IGNORE INTO permissions (code, name, description, module) VALUES
('post.create', 'Create Post', 'Create post content', 'POST'),
('post.delete.own', 'Delete Own Post', 'Delete own post', 'POST'),
('post.delete.any', 'Delete Any Post', 'Delete any post', 'POST'),
('post.restore.any', 'Restore Any Post', 'Restore deleted post', 'POST'),
('comment.create', 'Create Comment', 'Create post comment', 'COMMENT'),
('comment.reply', 'Reply Comment', 'Reply to comment', 'COMMENT'),
('comment.delete.own', 'Delete Own Comment', 'Delete own comment', 'COMMENT'),
('comment.delete.target', 'Delete Target Comment', 'Delete comment on own content', 'COMMENT'),
('comment.delete.any', 'Delete Any Comment', 'Delete any comment', 'COMMENT'),
('comment.restore.any', 'Restore Any Comment', 'Restore deleted comment', 'COMMENT'),
('report.create', 'Create Report', 'Create report for content', 'REPORT'),
('report.read.any', 'Read Reports', 'Read all reports', 'REPORT'),
('report.assign', 'Assign Report', 'Assign report to moderator', 'REPORT'),
('report.resolve', 'Resolve Report', 'Resolve report result', 'REPORT'),
('audit.read.moderation', 'Read Moderation Audit', 'Read moderation audit logs', 'AUDIT'),
('audit.read.all', 'Read All Audit', 'Read all audit logs', 'AUDIT'),
('user.ban', 'Ban User', 'Ban target user', 'USER'),
('user.unban', 'Unban User', 'Unban target user', 'USER'),
('role.read.any', 'Read Roles', 'Read role data', 'ROLE'),
('role.assign.admin', 'Assign Admin Role', 'Assign admin role', 'ROLE'),
('role.revoke.admin', 'Revoke Admin Role', 'Revoke admin role', 'ROLE'),
('trending.read.any', 'Read Trending Topics', 'Read trending topic candidates', 'TRENDING'),
('trending.hide', 'Hide Trending Topic', 'Hide abnormal trending topic', 'TRENDING'),
('trending.curate', 'Curate Trending Topic', 'Pin, merge or rename trending topic', 'TRENDING'),
('announcement.read.any', 'Read Announcements', 'Read announcement management data', 'ANNOUNCEMENT'),
('announcement.create', 'Create Announcement', 'Create or edit announcements', 'ANNOUNCEMENT'),
('announcement.publish', 'Publish Announcement', 'Publish or offline announcements', 'ANNOUNCEMENT'),
('announcement.popup.manage', 'Manage Popup Announcement', 'Manage popup or pinned announcements', 'ANNOUNCEMENT');

INSERT IGNORE INTO role_permissions (role_id, permission_id, created_by)
SELECT r.id, p.id, NULL
FROM roles r
         INNER JOIN permissions p ON p.code IN (
    'post.create',
    'post.delete.own',
    'comment.create',
    'comment.reply',
    'comment.delete.own',
    'comment.delete.target',
    'report.create'
)
WHERE r.code = 'USER';

INSERT IGNORE INTO role_permissions (role_id, permission_id, created_by)
SELECT r.id, p.id, NULL
FROM roles r
         INNER JOIN permissions p ON p.code IN (
    'post.create',
    'post.delete.own',
    'post.delete.any',
    'post.restore.any',
    'comment.create',
    'comment.reply',
    'comment.delete.own',
    'comment.delete.target',
    'comment.delete.any',
    'comment.restore.any',
    'report.create',
    'report.read.any',
    'report.assign',
    'report.resolve',
    'audit.read.moderation',
    'user.ban',
    'user.unban',
    'trending.read.any',
    'trending.hide',
    'announcement.read.any',
    'announcement.create',
    'announcement.publish'
)
WHERE r.code = 'ADMIN';

INSERT IGNORE INTO role_permissions (role_id, permission_id, created_by)
SELECT r.id, p.id, NULL
FROM roles r
         INNER JOIN permissions p ON p.code IN (
    'post.create',
    'post.delete.own',
    'post.delete.any',
    'post.restore.any',
    'comment.create',
    'comment.reply',
    'comment.delete.own',
    'comment.delete.target',
    'comment.delete.any',
    'comment.restore.any',
    'report.create',
    'report.read.any',
    'report.assign',
    'report.resolve',
    'audit.read.moderation',
    'audit.read.all',
    'user.ban',
    'user.unban',
    'role.read.any',
    'role.assign.admin',
    'role.revoke.admin',
    'trending.read.any',
    'trending.hide',
    'trending.curate',
    'announcement.read.any',
    'announcement.create',
    'announcement.publish',
    'announcement.popup.manage'
)
WHERE r.code = 'SUPER_ADMIN';

INSERT IGNORE INTO user_roles (user_id, role_id, created_by)
SELECT u.id, r.id, NULL
FROM users u
         INNER JOIN roles r ON r.code = 'USER';

INSERT IGNORE INTO user_roles (user_id, role_id, created_by)
SELECT uc.user_id, r.id, uc.user_id
FROM user_credentials uc
         INNER JOIN roles r ON r.code = 'SUPER_ADMIN'
WHERE uc.username = 'xiewei';

INSERT IGNORE INTO user_roles (user_id, role_id, created_by)
SELECT uc.user_id, r.id, uc.user_id
FROM user_credentials uc
         INNER JOIN roles r ON r.code = 'SUPER_ADMIN'
WHERE uc.username = 'codex-super';

UPDATE users
SET account_status = 'ACTIVE'
WHERE account_status IS NULL OR account_status = '';

INSERT IGNORE INTO conversations (id, conversation_code, owner_user_id, peer_name, peer_subtitle, peer_avatar_url, last_message, display_time, unread_count, sort_time) VALUES
(1, 'fox', 1, '珞珈山下的小狐狸', '学业互助伙伴', 'https://example.com/conversations/fox.jpg', '谢谢你的学业互助，真的很有用！', '14:20', 1, '2026-04-08 14:20:00'),
(2, 'museum', 1, '信管男神（自封）', '周末逛展搭子', 'https://example.com/conversations/museum.jpg', '下次一起去万林博物馆看展吗？', '昨天', 0, '2026-04-07 20:00:00'),
(3, 'seat', 1, '图书馆占座狂魔', '自习室情报官', 'https://example.com/conversations/seat.jpg', '不好意思，那个座位已经有人了。', '星期一', 0, '2026-04-06 09:00:00');

INSERT IGNORE INTO dm_conversations (id, conversation_code, conversation_type, status, created_by, last_message_id, last_message_at, created_at, updated_at) VALUES
(1, 'dm-1001', 'SINGLE', 'ACTIVE', 1, 2, '2026-04-08 10:12:00', '2026-04-08 10:00:00', '2026-04-08 10:12:00');

INSERT IGNORE INTO dm_conversation_participants (
    id, conversation_id, user_id, last_read_message_id, last_read_at, unread_count,
    pinned_flag, muted_flag, cleared_at, deleted_at, created_at, updated_at
) VALUES
(1, 1, 1, 2, '2026-04-08 10:12:00', 0, 0, 0, NULL, NULL, '2026-04-08 10:00:00', '2026-04-08 10:12:00');

INSERT INTO dm_conversation_participants (
    id, conversation_id, user_id, last_read_message_id, last_read_at, unread_count,
    pinned_flag, muted_flag, cleared_at, deleted_at, created_at, updated_at
)
SELECT 2, 1, u.id, 1, '2026-04-08 10:05:00', 1, 0, 0, NULL, NULL, '2026-04-08 10:00:00', '2026-04-08 10:12:00'
FROM users u
WHERE u.user_code = 'xiewei'
  AND NOT EXISTS (
      SELECT 1
      FROM dm_conversation_participants p
      WHERE p.id = 2
  );

INSERT INTO dm_messages (
    id, message_code, client_message_id, conversation_id, sender_user_id, message_type, status,
    content_payload, sent_at, recalled_at, created_at, updated_at
)
SELECT 1, 'dm-1001-msg-1', 'seed-client-1', 1, 1, 'TEXT', 'SENT',
       '你好，今天你在树洞的帖子我看到了。', '2026-04-08 10:05:00', NULL, '2026-04-08 10:05:00', '2026-04-08 10:05:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM dm_messages
    WHERE id = 1
);

INSERT INTO dm_messages (
    id, message_code, client_message_id, conversation_id, sender_user_id, message_type, status,
    content_payload, sent_at, recalled_at, created_at, updated_at
)
SELECT 2, 'dm-1001-msg-2', 'seed-client-2', 1, u.id, 'TEXT', 'SENT',
       '晚上好，我想和你私信聊一下。', '2026-04-08 10:12:00', NULL, '2026-04-08 10:12:00', '2026-04-08 10:12:00'
FROM users u
WHERE u.user_code = 'xiewei'
  AND NOT EXISTS (
      SELECT 1
      FROM dm_messages
      WHERE id = 2
  );

INSERT IGNORE INTO messages (id, message_code, conversation_id, sender_type, text_content, display_time, created_at) VALUES
(1, 'fox-1', 1, 'THEM', '你好！我在树洞看到你发的关于考研资料的帖子，请问数学三的笔记还在吗？', '昨天 18:30', '2026-04-07 18:30:00'),
(2, 'fox-2', 1, 'ME', '在的，还没被领走。如果你需要的话，明天中午我们可以约在信息学部食堂门口。', '昨天 19:02', '2026-04-07 19:02:00'),
(3, 'fox-3', 1, 'THEM', '太好了！那明天 12:30 可以吗？谢谢你的学业互助，真的很有用！', '14:18', '2026-04-08 14:18:00'),
(4, 'museum-1', 2, 'THEM', '这周万林的新展我已经想去三次了。', '星期一', '2026-04-06 18:00:00'),
(5, 'museum-2', 2, 'ME', '如果周末天气好，我们可以顺便去东湖边走走。', '星期一', '2026-04-06 18:20:00'),
(6, 'seat-1', 3, 'THEM', '今天总馆二楼靠窗的位置开放得比平时早。', '星期一', '2026-04-06 07:40:00'),
(7, 'seat-2', 3, 'ME', '收到，我下次试试看提前一点去。', '星期一', '2026-04-06 07:45:00');
