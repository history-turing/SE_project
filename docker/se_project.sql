-- MySQL dump 10.13  Distrib 8.4.7, for Win64 (x86_64)
--
-- Host: localhost    Database: se_project
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `alumni_contacts`
--

DROP TABLE IF EXISTS `alumni_contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alumni_contacts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contact_code` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `meta` varchar(128) NOT NULL,
  `focus` varchar(128) NOT NULL,
  `avatar_url` varchar(512) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `contact_code` (`contact_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alumni_contacts`
--

LOCK TABLES `alumni_contacts` WRITE;
/*!40000 ALTER TABLE `alumni_contacts` DISABLE KEYS */;
INSERT INTO `alumni_contacts` VALUES (1,'wang','王博士','2010 级 · 物理学院','材料与科研合作','https://example.com/contacts/wang.jpg',1),(2,'zhao','赵设计师','2019 级 · 艺术学院','品牌与视觉设计','https://example.com/contacts/zhao.jpg',2),(3,'zhou','周律师','2008 级 · 法学院','法律咨询与职业路径','https://example.com/contacts/zhou.jpg',3);
/*!40000 ALTER TABLE `alumni_contacts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `alumni_stories`
--

DROP TABLE IF EXISTS `alumni_stories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alumni_stories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `story_code` varchar(64) NOT NULL,
  `title` varchar(255) NOT NULL,
  `meta` varchar(255) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `story_code` (`story_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alumni_stories`
--

LOCK TABLES `alumni_stories` WRITE;
/*!40000 ALTER TABLE `alumni_stories` DISABLE KEYS */;
INSERT INTO `alumni_stories` VALUES (1,'story-1','从珞珈山到硅谷','2012 级 计算机学院 · 张校友',1),(2,'story-2','支教归来的这一年','2018 级 文学院 · 李校友',2),(3,'story-3','转行做纪录片导演之后','2014 级 新闻学院 · 吴校友',3);
/*!40000 ALTER TABLE `alumni_stories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conversations`
--

DROP TABLE IF EXISTS `conversations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_code` varchar(64) NOT NULL,
  `owner_user_id` bigint NOT NULL,
  `peer_name` varchar(64) NOT NULL,
  `peer_subtitle` varchar(128) NOT NULL,
  `peer_avatar_url` varchar(512) NOT NULL,
  `last_message` varchar(512) NOT NULL,
  `display_time` varchar(64) NOT NULL,
  `unread_count` int NOT NULL DEFAULT '0',
  `sort_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `conversation_code` (`conversation_code`),
  KEY `fk_conversations_user` (`owner_user_id`),
  CONSTRAINT `fk_conversations_user` FOREIGN KEY (`owner_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conversations`
--

LOCK TABLES `conversations` WRITE;
/*!40000 ALTER TABLE `conversations` DISABLE KEYS */;
INSERT INTO `conversations` VALUES (1,'fox',1,'珞珈山下的小狐狸','学业互助伙伴','https://example.com/conversations/fox.jpg','你好','22:10',0,'2026-04-08 22:10:59'),(2,'museum',1,'信管男神（自封）','周末逛展搭子','https://example.com/conversations/museum.jpg','下次一起去万林博物馆看展吗？','昨天',0,'2026-04-07 20:00:00'),(3,'seat',1,'图书馆占座狂魔','自习室情报官','https://example.com/conversations/seat.jpg','不好意思，那个座位已经有人了。','星期一',0,'2026-04-06 09:00:00');
/*!40000 ALTER TABLE `conversations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_code` varchar(64) NOT NULL,
  `conversation_id` bigint NOT NULL,
  `sender_type` varchar(16) NOT NULL,
  `text_content` varchar(1000) NOT NULL,
  `display_time` varchar(64) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `message_code` (`message_code`),
  KEY `fk_messages_conversation` (`conversation_id`),
  CONSTRAINT `fk_messages_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (1,'fox-1',1,'THEM','你好！我在树洞看到你发的关于考研资料的帖子，请问数学三的笔记还在吗？','昨天 18:30','2026-04-07 18:30:00'),(2,'fox-2',1,'ME','在的，还没被领走。如果你需要的话，明天中午我们可以约在信息学部食堂门口。','昨天 19:02','2026-04-07 19:02:00'),(3,'fox-3',1,'THEM','太好了！那明天 12:30 可以吗？谢谢你的学业互助，真的很有用！','14:18','2026-04-08 14:18:00'),(4,'museum-1',2,'THEM','这周万林的新展我已经想去三次了。','星期一','2026-04-06 18:00:00'),(5,'museum-2',2,'ME','如果周末天气好，我们可以顺便去东湖边走走。','星期一','2026-04-06 18:20:00'),(6,'seat-1',3,'THEM','今天总馆二楼靠窗的位置开放得比平时早。','星期一','2026-04-06 07:40:00'),(7,'seat-2',3,'ME','收到，我下次试试看提前一点去。','星期一','2026-04-06 07:45:00'),(8,'fox-1775657459082',1,'ME','你好','22:10','2026-04-08 22:10:59');
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notices`
--

DROP TABLE IF EXISTS `notices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `notice_code` varchar(64) NOT NULL,
  `title` varchar(255) NOT NULL,
  `meta` varchar(64) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `notice_code` (`notice_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notices`
--

LOCK TABLES `notices` WRITE;
/*!40000 ALTER TABLE `notices` DISABLE KEYS */;
INSERT INTO `notices` VALUES (1,'notice-1','樱花开放期间校园管理措施更新','置顶公告',1),(2,'notice-2','图书馆预约系统今晚 23:00 维护','系统通知',2),(3,'notice-3','东湖夜跑社团本周五集合','社团活动',3);
/*!40000 ALTER TABLE `notices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_interactions`
--

DROP TABLE IF EXISTS `post_interactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_interactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `liked` tinyint(1) NOT NULL DEFAULT '0',
  `saved` tinyint(1) NOT NULL DEFAULT '0',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_interactions_user_post` (`user_id`,`post_id`),
  KEY `fk_post_interactions_post` (`post_id`),
  CONSTRAINT `fk_post_interactions_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
  CONSTRAINT `fk_post_interactions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_interactions`
--

LOCK TABLES `post_interactions` WRITE;
/*!40000 ALTER TABLE `post_interactions` DISABLE KEYS */;
INSERT INTO `post_interactions` VALUES (1,1,1,1,0,'2026-04-08 12:00:00'),(2,1,2,0,1,'2026-04-08 12:00:00'),(3,1,5,1,1,'2026-04-08 12:00:00'),(4,1,8,0,1,'2026-04-08 12:00:00'),(5,1,13,1,0,'2026-04-08 22:10:48');
/*!40000 ALTER TABLE `post_interactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `posts`
--

DROP TABLE IF EXISTS `posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `posts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_code` varchar(64) NOT NULL,
  `creator_user_id` bigint NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `content` text NOT NULL,
  `author_name` varchar(64) NOT NULL,
  `author_handle` varchar(128) NOT NULL,
  `topic_name` varchar(64) NOT NULL,
  `audience_type` varchar(16) NOT NULL,
  `display_time` varchar(64) NOT NULL,
  `like_count` int NOT NULL DEFAULT '0',
  `comment_count` int NOT NULL DEFAULT '0',
  `save_count` int NOT NULL DEFAULT '0',
  `accent_tone` varchar(16) NOT NULL,
  `badge` varchar(64) DEFAULT NULL,
  `image_url` varchar(512) DEFAULT NULL,
  `anonymous_flag` tinyint(1) NOT NULL DEFAULT '0',
  `location` varchar(64) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `post_code` (`post_code`),
  KEY `fk_posts_user` (`creator_user_id`),
  CONSTRAINT `fk_posts_user` FOREIGN KEY (`creator_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `posts`
--

LOCK TABLES `posts` WRITE;
/*!40000 ALTER TABLE `posts` DISABLE KEYS */;
INSERT INTO `posts` VALUES (1,'home-1',1,'今早的老斋舍，樱花落了一地','时间好像在这里走得很慢。看到花瓣落在台阶上，突然觉得三年前第一次进校门的那个人还在眼前。','小狐狸树洞','匿名珞珈人','校园日常','HOME','2 小时前',421,56,18,'rose',NULL,'https://example.com/posts/home-1.jpg',1,NULL,'2026-04-08 08:00:00'),(2,'home-2',1,'有没有跨专业考研到法学院的前辈？','本科是理科生，最近开始认真查资料了。求经验、求书单，哪怕一点建议也很珍贵。','匿名用户','学业互助','学业交流','HOME','今天 11:20',89,24,37,'gold',NULL,NULL,1,NULL,'2026-04-08 11:20:00'),(3,'home-3',1,NULL,'东湖的晚霞真的很适合给焦虑按下暂停键。今晚风很大，心却很静。','追夕阳的人','湖边散步计划','校园日常','HOME','昨天',842,45,61,'ink',NULL,'https://example.com/posts/home-3.jpg',0,NULL,'2026-04-07 18:30:00'),(4,'home-4',1,'终于在工学部食堂吃到了今天最满意的一碗热干面','如果有人也在做武大食堂巡礼，真心推荐这家。辣油香得刚刚好，面也够劲道。','干饭人小张','食堂雷达','校园日常','HOME','昨天',3200,128,205,'jade',NULL,'https://example.com/posts/home-4.jpg',0,NULL,'2026-04-07 12:00:00'),(5,'alumni-1',1,'回母校走走，樱花大道依旧，只是少年已不再','今天趁着出差回了趟武大，看到图书馆里埋头苦读的学弟学妹，仿佛看到了当年的自己。','陈先生','2015 级校友','校友故事','ALUMNI','深圳 · 2 小时前',1200,86,53,'rose','返校日记','https://example.com/posts/alumni-1.jpg',0,'深圳','2026-04-08 09:00:00'),(6,'alumni-2',1,'【字节跳动】产品经理 / 研发校招社招内推','部门直招，校友内推简历直达 HR。感兴趣的同学或校友可以直接私信我。','林学姐','2018 级校友','职场内推','ALUMNI','上海 · 5 小时前',452,120,141,'jade','机会速递',NULL,0,'上海','2026-04-08 06:30:00'),(7,'alumni-3',1,'支教归来的这一年，重新理解了成长','离开校园后才发现，很多答案不是在课堂里得到的，而是在与真实世界的相遇里慢慢长出来的。','李校友','2018 级 · 文学院','校友故事','ALUMNI','成都 · 昨天',284,41,29,'gold','成长故事',NULL,0,'成都','2026-04-07 19:30:00'),(8,'me-1',1,'想给第一次来武大的朋友做一份散步地图','从凌波门日出到东湖绿道，如果你只能在武大待一天，我很想把这条线送给你。','樱花味猫奴','我的树洞','校园日常','HOME','3 天前',96,14,22,'rose',NULL,NULL,0,NULL,'2026-04-05 08:00:00'),(9,'me-2',1,'如果你也在准备春招，我整理了一份时间线','把最近看到的笔试、投递和面试时间都汇总到了便签里。希望能帮到正在赶路的人。','樱花味猫奴','我的树洞','职场内推','ALUMNI','1 周前',143,27,49,'jade',NULL,NULL,0,NULL,'2026-04-01 20:00:00'),(10,'post-1775657263615',1,'联调测试树洞','这是一条前后端联调测试数据','匿名珞珈人','低语模式','校园日常','HOME','刚刚',0,0,0,'rose',NULL,NULL,1,NULL,'2026-04-08 22:07:44'),(11,'post-1775657289151',1,'联调测试树洞','这是一条前后端联调测试数据','匿名珞珈人','低语模式','校园日常','HOME','刚刚',0,0,0,'rose',NULL,NULL,1,NULL,'2026-04-08 22:08:09'),(12,'post-1775657300908',1,'联调页面测试树洞','这是一条页面联调验证数据','匿名珞珈人','低语模式','校园日常','HOME','刚刚',0,0,0,'rose',NULL,NULL,1,NULL,'2026-04-08 22:08:21'),(13,'post-1775657400907',1,'test','我已经实现树洞的基础功能啦','匿名珞珈人','低语模式','校园日常','HOME','刚刚',1,0,0,'rose',NULL,NULL,1,NULL,'2026-04-08 22:10:01'),(14,'post-1775660883410',6,'??????','???????????????','user1775660878','待完善 · 新用户','表白墙','HOME','刚刚',0,0,0,'rose',NULL,NULL,0,NULL,'2026-04-08 23:08:03'),(15,'post-1775674036504',7,'email-test','已经完善登陆时邮箱发送功能','匿名珞珈人','低语模式','学业交流','HOME','刚刚',0,0,0,'rose',NULL,NULL,1,NULL,'2026-04-09 02:47:17');
/*!40000 ALTER TABLE `posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topic_rankings`
--

DROP TABLE IF EXISTS `topic_rankings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `topic_rankings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ranking_code` varchar(64) NOT NULL,
  `label` varchar(64) NOT NULL,
  `heat_text` varchar(64) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ranking_code` (`ranking_code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic_rankings`
--

LOCK TABLES `topic_rankings` WRITE;
/*!40000 ALTER TABLE `topic_rankings` DISABLE KEYS */;
INSERT INTO `topic_rankings` VALUES (1,'rank-1','#樱花季预约','45.2w 热度',1),(2,'rank-2','#图书馆占座','32.8w 热度',2),(3,'rank-3','#梅园食堂新品','28.5w 热度',3),(4,'rank-4','#珞珈山猫咪图鉴','15.1w 热度',4),(5,'rank-5','#春招提前批','12.3w 热度',5);
/*!40000 ALTER TABLE `topic_rankings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topic_tags`
--

DROP TABLE IF EXISTS `topic_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `topic_tags` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `topic_id` bigint NOT NULL,
  `tag_name` varchar(64) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_topic_tags_topic` (`topic_id`),
  CONSTRAINT `fk_topic_tags_topic` FOREIGN KEY (`topic_id`) REFERENCES `topics` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic_tags`
--

LOCK TABLES `topic_tags` WRITE;
/*!40000 ALTER TABLE `topic_tags` DISABLE KEYS */;
INSERT INTO `topic_tags` VALUES (1,1,'心动瞬间',1),(2,1,'暗恋日记',2),(3,1,'春日樱花',3),(4,2,'校园卡',1),(5,2,'钥匙',2),(6,2,'雨伞',3),(7,3,'期末周',1),(8,3,'考研经验',2),(9,3,'课程互助',3),(10,4,'食堂测评',1),(11,4,'东湖日落',2),(12,4,'宿舍闲聊',3),(13,5,'产品经理',1),(14,5,'算法岗',2),(15,5,'春招',3),(16,6,'行业成长',1),(17,6,'返校记忆',2),(18,6,'人生选择',3);
/*!40000 ALTER TABLE `topic_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topics`
--

DROP TABLE IF EXISTS `topics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `topics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `topic_code` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `description` varchar(255) NOT NULL,
  `heat_text` varchar(64) NOT NULL,
  `destination_type` varchar(16) NOT NULL,
  `accent_tone` varchar(16) NOT NULL,
  `emoji` varchar(16) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `topic_code` (`topic_code`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topics`
--

LOCK TABLES `topics` WRITE;
/*!40000 ALTER TABLE `topics` DISABLE KEYS */;
INSERT INTO `topics` VALUES (1,'confession','表白墙','把没说出口的话，放进珞珈山的风里。','1.2k 正在热议','CAMPUS','rose','💗',1),(2,'lost-found','失物招领','连接遗落的时光，帮物品重新找到主人。','450+ 待认领','CAMPUS','jade','🔎',2),(3,'study','学业交流','课程、考研、复习与经验都在这里汇流。','学术研讨中','CAMPUS','gold','📚',3),(4,'campus-chat','校园日常','吐槽食堂、分享晚霞、记录普通却动人的一天。','深夜食堂','CAMPUS','ink','☕',4),(5,'career','职场内推','把校友网络织进求职旅程，给下一位武大人一束光。','158 条机会','ALUMNI','jade','💼',5),(6,'alumni-stories','校友故事','看见从珞珈到世界各地的人，如何讲述自己的路。','持续更新','ALUMNI','rose','🌸',6);
/*!40000 ALTER TABLE `topics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_badges`
--

DROP TABLE IF EXISTS `user_badges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_badges` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `badge_name` varchar(64) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_user_badges_user` (`user_id`),
  CONSTRAINT `fk_user_badges_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_badges`
--

LOCK TABLES `user_badges` WRITE;
/*!40000 ALTER TABLE `user_badges` DISABLE KEYS */;
INSERT INTO `user_badges` VALUES (1,1,'树洞记录者',1),(2,1,'春招互助',2),(3,1,'东湖散步搭子',3),(4,2,'邮箱已认证',1),(5,3,'邮箱已认证',1),(6,4,'邮箱已认证',1),(7,5,'邮箱已认证',1),(8,6,'邮箱已认证',1),(9,7,'邮箱已认证',1);
/*!40000 ALTER TABLE `user_badges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_credentials`
--

DROP TABLE IF EXISTS `user_credentials`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_credentials` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `email` varchar(128) NOT NULL,
  `username` varchar(32) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `email_verified_at` datetime NOT NULL,
  `last_login_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `username` (`username`),
  KEY `fk_user_credentials_user` (`user_id`),
  CONSTRAINT `fk_user_credentials_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_credentials`
--

LOCK TABLES `user_credentials` WRITE;
/*!40000 ALTER TABLE `user_credentials` DISABLE KEYS */;
INSERT INTO `user_credentials` VALUES (1,2,'test1775660204@whu.edu.cn','user1775660204','$2a$10$CzpWQJAlckWa4UowYKCNs.fKZdM9PiCFQmSP1oklnE/hrsee4V/Sy','2026-04-08 22:56:48','2026-04-08 22:56:49','2026-04-08 22:56:48','2026-04-08 22:56:49'),(2,3,'test1775660326@whu.edu.cn','user1775660326','$2a$10$9DfehgtWx8s/5z6BR.X2UuB2OR353oUt06CSstNGFLXOKTtE8sM/G','2026-04-08 22:58:50','2026-04-08 22:58:51','2026-04-08 22:58:50','2026-04-08 22:58:51'),(3,4,'test1775660430@whu.edu.cn','user1775660430','$2a$10$mCFK2189fXwfwrNxR5csVOmcqcmmd5qbbSHEu5NbYoyPoYyrSIOsy','2026-04-08 23:00:34','2026-04-08 23:00:35','2026-04-08 23:00:34','2026-04-08 23:00:35'),(4,5,'test1775660720@whu.edu.cn','user1775660720','$2a$10$4p//s5mJVOp1CrTqhg0dU.RwY6pcamsxDg.lsyDjh8loWbHZB4eoe','2026-04-08 23:05:25','2026-04-08 23:05:25','2026-04-08 23:05:25','2026-04-08 23:05:25'),(5,6,'test1775660878@whu.edu.cn','user1775660878','$2a$10$36MMHD1nPz1D81x6Lxxxv./WjdnfZ/NmTTXV8JQrxIgeMaIXe4gkK','2026-04-08 23:08:02','2026-04-08 23:08:03','2026-04-08 23:08:02','2026-04-08 23:08:03'),(6,7,'2024302112009@whu.edu.cn','xiewei','$2a$10$d.k/FFc3ZMuF982MBoG0d.2HtL2sCSBDdMP5gi21HMN1wdJ9Vitlq','2026-04-09 02:44:30','2026-04-09 02:44:30','2026-04-09 02:44:30','2026-04-09 02:44:30');
/*!40000 ALTER TABLE `user_credentials` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_follow_contacts`
--

DROP TABLE IF EXISTS `user_follow_contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follow_contacts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `contact_id` bigint NOT NULL,
  `followed` tinyint(1) NOT NULL DEFAULT '0',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow_contact` (`user_id`,`contact_id`),
  KEY `fk_follow_contact` (`contact_id`),
  CONSTRAINT `fk_follow_contact` FOREIGN KEY (`contact_id`) REFERENCES `alumni_contacts` (`id`),
  CONSTRAINT `fk_follow_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_follow_contacts`
--

LOCK TABLES `user_follow_contacts` WRITE;
/*!40000 ALTER TABLE `user_follow_contacts` DISABLE KEYS */;
INSERT INTO `user_follow_contacts` VALUES (1,1,1,1,'2026-04-08 12:00:00');
/*!40000 ALTER TABLE `user_follow_contacts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_profile_stats`
--

DROP TABLE IF EXISTS `user_profile_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_profile_stats` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `stat_label` varchar(64) NOT NULL,
  `stat_value` varchar(64) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_user_stats_user` (`user_id`),
  CONSTRAINT `fk_user_stats_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_profile_stats`
--

LOCK TABLES `user_profile_stats` WRITE;
/*!40000 ALTER TABLE `user_profile_stats` DISABLE KEYS */;
INSERT INTO `user_profile_stats` VALUES (1,1,'已发树洞','18',1),(2,1,'收藏内容','27',2),(3,1,'已建立私信','9',3),(4,2,'已发树洞','0',1),(5,2,'收藏内容','0',2),(6,2,'已建立私信','0',3),(7,3,'已发树洞','0',1),(8,3,'收藏内容','0',2),(9,3,'已建立私信','0',3),(10,4,'已发树洞','0',1),(11,4,'收藏内容','0',2),(12,4,'已建立私信','0',3),(13,5,'已发树洞','0',1),(14,5,'收藏内容','0',2),(15,5,'已建立私信','0',3),(16,6,'已发树洞','0',1),(17,6,'收藏内容','0',2),(18,6,'已建立私信','0',3),(19,7,'已发树洞','0',1),(20,7,'收藏内容','0',2),(21,7,'已建立私信','0',3);
/*!40000 ALTER TABLE `user_profile_stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_code` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `tagline` varchar(255) NOT NULL,
  `college` varchar(64) NOT NULL,
  `grade_year` varchar(32) NOT NULL,
  `bio` varchar(512) NOT NULL,
  `avatar_url` varchar(512) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_code` (`user_code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'me','樱花味猫奴','把校园生活慢慢写成一册柔软的日志。','信息管理学院','2022 级','喜欢晚霞、热干面、图书馆靠窗位置，也喜欢把看似平凡的瞬间认真记下来。','https://example.com/avatar/me.jpg','2026-04-08 10:00:00'),(2,'user-775d3158881f4ab294a4b07a74bec394','user1775660204','刚完成武大邮箱认证，准备在树洞留下第一条记录。','待完善','新用户','这个用户还没有填写个人简介。','data:image/svg+xml,%3Csvg+xmlns%3D%27http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%27+width%3D%27160%27+height%3D%27160%27+viewBox%3D%270+0+160+160%27%3E%3Crect+width%3D%27160%27+height%3D%27160%27+rx%3D%2736%27+fill%3D%27%23B85A73%27%2F%3E%3Ctext+x%3D%2750%25%27+y%3D%2754%25%27+text-anchor%3D%27middle%27+font-size%3D%2752%27+font-family%3D%27Arial%27+fill%3D%27white%27%3EUS%3C%2Ftext%3E%3C%2Fsvg%3E','2026-04-08 22:56:48'),(3,'user-bfe48b050d5b4bbcb7a71f7108063d34','user1775660326','刚完成武大邮箱认证，准备在树洞留下第一条记录。','待完善','新用户','这个用户还没有填写个人简介。','data:image/svg+xml,%3Csvg+xmlns%3D%27http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%27+width%3D%27160%27+height%3D%27160%27+viewBox%3D%270+0+160+160%27%3E%3Crect+width%3D%27160%27+height%3D%27160%27+rx%3D%2736%27+fill%3D%27%23B85A73%27%2F%3E%3Ctext+x%3D%2750%25%27+y%3D%2754%25%27+text-anchor%3D%27middle%27+font-size%3D%2752%27+font-family%3D%27Arial%27+fill%3D%27white%27%3EUS%3C%2Ftext%3E%3C%2Fsvg%3E','2026-04-08 22:58:50'),(4,'user-79cdd461386e4fdebd398927e7e56329','user1775660430','刚完成武大邮箱认证，准备在树洞留下第一条记录。','待完善','新用户','这个用户还没有填写个人简介。','data:image/svg+xml,%3Csvg+xmlns%3D%27http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%27+width%3D%27160%27+height%3D%27160%27+viewBox%3D%270+0+160+160%27%3E%3Crect+width%3D%27160%27+height%3D%27160%27+rx%3D%2736%27+fill%3D%27%23B85A73%27%2F%3E%3Ctext+x%3D%2750%25%27+y%3D%2754%25%27+text-anchor%3D%27middle%27+font-size%3D%2752%27+font-family%3D%27Arial%27+fill%3D%27white%27%3EUS%3C%2Ftext%3E%3C%2Fsvg%3E','2026-04-08 23:00:34'),(5,'user-66dff1cd119345c2a7f449ef638263a7','user1775660720','刚完成武大邮箱认证，准备在树洞留下第一条记录。','待完善','新用户','这个用户还没有填写个人简介。','data:image/svg+xml,%3Csvg+xmlns%3D%27http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%27+width%3D%27160%27+height%3D%27160%27+viewBox%3D%270+0+160+160%27%3E%3Crect+width%3D%27160%27+height%3D%27160%27+rx%3D%2736%27+fill%3D%27%23B85A73%27%2F%3E%3Ctext+x%3D%2750%25%27+y%3D%2754%25%27+text-anchor%3D%27middle%27+font-size%3D%2752%27+font-family%3D%27Arial%27+fill%3D%27white%27%3EUS%3C%2Ftext%3E%3C%2Fsvg%3E','2026-04-08 23:05:25'),(6,'user-2814c311a4d84ef8a5c35e0fa366fbe8','user1775660878','刚完成武大邮箱认证，准备在树洞留下第一条记录。','待完善','新用户','这个用户还没有填写个人简介。','data:image/svg+xml,%3Csvg+xmlns%3D%27http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%27+width%3D%27160%27+height%3D%27160%27+viewBox%3D%270+0+160+160%27%3E%3Crect+width%3D%27160%27+height%3D%27160%27+rx%3D%2736%27+fill%3D%27%23B85A73%27%2F%3E%3Ctext+x%3D%2750%25%27+y%3D%2754%25%27+text-anchor%3D%27middle%27+font-size%3D%2752%27+font-family%3D%27Arial%27+fill%3D%27white%27%3EUS%3C%2Ftext%3E%3C%2Fsvg%3E','2026-04-08 23:08:02'),(7,'user-64b6ad361da84e4592ba11a3ac048bb0','xiewei','刚完成武大邮箱认证，准备在树洞留下第一条记录。','待完善','新用户','这个用户还没有填写个人简介。','data:image/svg+xml,%3Csvg+xmlns%3D%27http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%27+width%3D%27160%27+height%3D%27160%27+viewBox%3D%270+0+160+160%27%3E%3Crect+width%3D%27160%27+height%3D%27160%27+rx%3D%2736%27+fill%3D%27%23B85A73%27%2F%3E%3Ctext+x%3D%2750%25%27+y%3D%2754%25%27+text-anchor%3D%27middle%27+font-size%3D%2752%27+font-family%3D%27Arial%27+fill%3D%27white%27%3EXI%3C%2Ftext%3E%3C%2Fsvg%3E','2026-04-09 02:44:30');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-09 15:27:27
