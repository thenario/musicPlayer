-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: musicPlayer
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Current Database: `musicPlayer`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `musicPlayer` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `musicPlayer`;

--
-- Table structure for table `albums`
--

DROP TABLE IF EXISTS `albums`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `albums` (
  `album_id` bigint NOT NULL COMMENT '专辑ID(雪花)',
  `album_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '专辑名称',
  `artist_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '艺术家名(未建 artists 表)',
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专辑封面',
  `release_date` date DEFAULT NULL COMMENT '发行日期',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`album_id`),
  KEY `idx_album_name` (`album_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `albums`
--

LOCK TABLES `albums` WRITE;
/*!40000 ALTER TABLE `albums` DISABLE KEYS */;
/*!40000 ALTER TABLE `albums` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comments` (
  `comment_id` bigint NOT NULL COMMENT '评论ID(雪花)',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `target_type` enum('song','playlist') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论目标类型',
  `target_id` bigint NOT NULL COMMENT '评论目标ID(歌曲/歌单, 多态无外键)',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID(支持回复)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  KEY `idx_target` (`target_type`,`target_id`,`created_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`comment_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comments`
--

LOCK TABLES `comments` WRITE;
/*!40000 ALTER TABLE `comments` DISABLE KEYS */;
/*!40000 ALTER TABLE `comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','initial schema','SQL','V1__initial_schema.sql',462934151,'root','2026-08-16 06:55:04',1097,1),(2,'2','add relationship foreign keys','SQL','V2__add_relationship_foreign_keys.sql',1357106163,'root','2026-08-16 06:55:04',633,1),(3,'3','prevent duplicate queue songs','SQL','V3__prevent_duplicate_queue_songs.sql',-543997721,'root','2026-08-16 06:55:04',15,1),(4,'4','prevent duplicate playlist positions','SQL','V4__prevent_duplicate_playlist_positions.sql',-428061984,'root','2026-08-16 06:55:04',15,1),(5,'5','enforce queue item positions','SQL','V5__enforce_queue_item_positions.sql',-1946151204,'root','2026-08-16 06:55:04',15,1),(6,'6','normalize playmode values','SQL','V6__normalize_playmode_values.sql',904773230,'root','2026-08-16 06:55:04',11,1),(7,'7','store song file size as bigint','SQL','V7__store_song_file_size_as_bigint.sql',-1133867066,'root','2026-08-16 06:55:05',153,1),(8,'8','remove redundant queue created at','SQL','V8__remove_redundant_queue_created_at.sql',-521260488,'root','2026-08-16 06:55:05',66,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `play_history`
--

DROP TABLE IF EXISTS `play_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `play_history` (
  `history_id` bigint NOT NULL COMMENT '播放历史ID(雪花)',
  `user_id` bigint NOT NULL,
  `song_id` bigint NOT NULL,
  `played_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`history_id`),
  KEY `idx_user_played` (`user_id`,`played_date`),
  KEY `idx_song_id` (`song_id`),
  CONSTRAINT `fk_play_history_song` FOREIGN KEY (`song_id`) REFERENCES `songs` (`song_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_play_history_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `play_history`
--

LOCK TABLES `play_history` WRITE;
/*!40000 ALTER TABLE `play_history` DISABLE KEYS */;
INSERT INTO `play_history` VALUES (2097686831548309506,2088884371316674561,2088899211435347970,'2026-09-10 11:41:37'),(2097686838896730113,2088884371316674561,2088899228808155137,'2026-09-10 11:38:16'),(2097686847356641281,2088884371316674561,2088899232574640129,'2026-09-10 11:52:09'),(2097687484551110658,2088884371316674561,2088899229458272258,'2026-09-10 11:45:13'),(2097688007052337154,2088884371316674561,2088899232385896449,'2026-09-09 22:11:18'),(2097688643340836865,2088884371316674561,2088899228422279169,'2026-09-10 11:55:32'),(2097689283538427905,2088884371316674561,2088899232385896449,'2026-09-09 22:11:13'),(2097689304203763713,2088884371316674561,2088899232385896449,'2026-09-09 22:11:18'),(2097689339020681218,2088884371316674561,2088899232255873026,'2026-09-10 11:49:41');
/*!40000 ALTER TABLE `play_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `play_state`
--

DROP TABLE IF EXISTS `play_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `play_state` (
  `user_id` bigint NOT NULL COMMENT '用户ID(雪花)',
  `current_queue_id` bigint DEFAULT NULL,
  `current_song_id` bigint DEFAULT NULL,
  `current_position` int NOT NULL DEFAULT '0',
  `current_progress` int NOT NULL DEFAULT '0',
  `playmode` varchar(20) DEFAULT 'sequential',
  `updated_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  KEY `idx_current_queue` (`current_queue_id`),
  CONSTRAINT `fk_play_state_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `play_state`
--

LOCK TABLES `play_state` WRITE;
/*!40000 ALTER TABLE `play_state` DISABLE KEYS */;
INSERT INTO `play_state` VALUES (2088884371316674561,2097687484161040385,2088899228422279169,3,202,'repeat_all','2026-09-10 11:55:32');
/*!40000 ALTER TABLE `play_state` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `playlists`
--

DROP TABLE IF EXISTS `playlists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `playlists` (
  `playlist_id` bigint NOT NULL COMMENT '歌单ID(雪花)',
  `playlist_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `creator_id` bigint NOT NULL,
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `playlist_cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `song_count` int NOT NULL DEFAULT '0',
  `like_count` int NOT NULL DEFAULT '0',
  `play_count` int NOT NULL DEFAULT '0',
  `is_public` tinyint(1) NOT NULL DEFAULT '1',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`playlist_id`),
  KEY `idx_creator_id` (`creator_id`),
  CONSTRAINT `fk_playlists_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `playlists`
--

LOCK TABLES `playlists` WRITE;
/*!40000 ALTER TABLE `playlists` DISABLE KEYS */;
/*!40000 ALTER TABLE `playlists` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `queue_items`
--

DROP TABLE IF EXISTS `queue_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `queue_items` (
  `queue_item_id` bigint NOT NULL COMMENT '队列项ID(雪花)',
  `queue_id` bigint NOT NULL,
  `song_id` bigint NOT NULL,
  `queue_item_position` int NOT NULL DEFAULT '0',
  `added_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`queue_item_id`),
  UNIQUE KEY `uq_queue_items_queue_song` (`queue_id`,`song_id`),
  UNIQUE KEY `uq_queue_item_position` (`queue_id`,`queue_item_position`),
  KEY `idx_queue_song` (`queue_id`,`song_id`),
  KEY `idx_queue_position` (`queue_id`,`queue_item_position`),
  KEY `fk_queue_items_song` (`song_id`),
  CONSTRAINT `fk_queue_items_queue` FOREIGN KEY (`queue_id`) REFERENCES `queues` (`queue_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_queue_items_song` FOREIGN KEY (`song_id`) REFERENCES `songs` (`song_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `queue_items`
--

LOCK TABLES `queue_items` WRITE;
/*!40000 ALTER TABLE `queue_items` DISABLE KEYS */;
INSERT INTO `queue_items` VALUES (2092850272948908033,2088900286640979970,2088899229458272258,1,'2026-08-27 13:42:43'),(2092850339592204289,2088900286640979970,2088899228422279169,3,'2026-08-27 13:42:59'),(2092853353317068802,2088900286640979970,2088899211435347970,4,'2026-08-27 13:54:57'),(2092869785383854082,2088900286640979970,2092869288631459841,8,'2026-08-27 15:00:15'),(2092869953462198274,2088900286640979970,2088899215524794369,9,'2026-08-27 15:00:55'),(2097686838645071874,2088900286640979970,2088899228808155137,5,'2026-09-09 22:01:30'),(2097686847226617857,2088900286640979970,2088899232574640129,6,'2026-09-09 22:01:32'),(2097686857976619009,2088900286640979970,2088899232255873026,7,'2026-09-09 22:01:35'),(2097687484228149249,2097687484161040385,2088899232255873026,1,'2026-09-09 22:04:04'),(2097687484228149250,2097687484161040385,2088899232574640129,2,'2026-09-09 22:04:04'),(2097687484228149251,2097687484161040385,2088899228808155137,4,'2026-09-09 22:04:04'),(2097687484228149252,2097687484161040385,2088899211435347970,5,'2026-09-09 22:04:04'),(2097687484228149254,2097687484161040385,2088899229458272258,6,'2026-09-09 22:04:04'),(2097688006888759297,2088900286640979970,2088899232385896449,2,'2026-09-09 22:06:09'),(2097689303926939649,2097689303864025090,2088899232385896449,1,'2026-09-09 22:11:18'),(2097689303926939650,2097689303864025090,2088899228422279169,2,'2026-09-09 22:11:18'),(2097689303926939651,2097689303864025090,2088899229458272258,3,'2026-09-09 22:11:18'),(2097689303926939652,2097689303864025090,2088899232255873026,4,'2026-09-09 22:11:18'),(2097689303926939653,2097689303864025090,2088899232574640129,5,'2026-09-09 22:11:18'),(2097689303926939654,2097689303864025090,2088899228808155137,6,'2026-09-09 22:11:18'),(2097689303926939655,2097689303864025090,2088899211435347970,7,'2026-09-09 22:11:18'),(2097690117395369986,2097687484161040385,2088899228422279169,3,'2026-09-09 22:14:32');
/*!40000 ALTER TABLE `queue_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `queues`
--

DROP TABLE IF EXISTS `queues`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `queues` (
  `queue_id` bigint NOT NULL COMMENT '队列ID(雪花)',
  `queue_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `creator_id` bigint NOT NULL,
  `song_count` int NOT NULL DEFAULT '0',
  `is_current` tinyint(1) NOT NULL DEFAULT '0',
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`queue_id`),
  KEY `idx_creator_date` (`creator_id`,`updated_date` DESC),
  CONSTRAINT `fk_queues_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `queues`
--

LOCK TABLES `queues` WRITE;
/*!40000 ALTER TABLE `queues` DISABLE KEYS */;
INSERT INTO `queues` VALUES (2088900286640979970,'默认列表',2088884371316674561,9,0,'2026-08-16 16:06:53','2026-09-09 22:06:08'),(2097687484161040385,'播放历史',2088884371316674561,6,1,'2026-09-09 22:04:04','2026-09-09 22:11:26'),(2097689303864025090,'播放历史',2088884371316674561,7,0,'2026-09-09 22:11:18','2026-09-09 22:11:26');
/*!40000 ALTER TABLE `queues` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `songs`
--

DROP TABLE IF EXISTS `songs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `songs` (
  `song_id` bigint NOT NULL COMMENT '歌曲ID(雪花)',
  `song_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `artist` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '未知艺术家',
  `album` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '未知专辑',
  `file_size` bigint DEFAULT NULL,
  `uploader_id` bigint NOT NULL,
  `uploader_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `duration` int NOT NULL DEFAULT '0',
  `bitrate` int NOT NULL DEFAULT '0',
  `song_cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `song_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `date_added` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `file_format` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `lyrics` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `t_lyrics` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '翻译歌词(LRC格式)',
  `file_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件内容指纹',
  PRIMARY KEY (`song_id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_file_md5` (`file_md5`),
  FULLTEXT KEY `idx_fulltext_title` (`song_title`),
  CONSTRAINT `fk_songs_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `songs`
--

LOCK TABLES `songs` WRITE;
/*!40000 ALTER TABLE `songs` DISABLE KEYS */;
INSERT INTO `songs` VALUES (2088899211083026433,'24⧸7','Synth','空与风的收藏',3507992,2088884371316674561,'空与风',247,109,'/static/song_covers/auto-f40b87b4.jpg','/static/songs/24⧸7_Synth.mp3','2026-08-16 16:02:36','mp3',NULL,NULL,'05b4370b9a63f259bb7d9daa46fbe6d9'),(2088899211238215682,'8月31','吴宇深','空与风的收藏',2376788,2088884371316674561,'空与风',194,87,'/static/song_covers/auto-d3156d5f.jpg','/static/songs/8月31_吴宇深.mp3','2026-08-16 16:02:36','mp3',NULL,NULL,'6ddea3566d0bf96bb385b15528c50a63'),(2088899211305324546,'Adagio for Summer Wind','清水準一','Kud Wafter Original Sound Track',2305271,2088884371316674561,'空与风',141,125,'/static/song_covers/auto-6fdf032c.jpg','/static/songs/Adagio for Summer Wind_清水淳一.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'751ab06a548e4f02041e7e8ce1c208ae'),(2088899211435347970,'AIZO','King Gnu','AIZO',7175271,2088884371316674561,'空与风',216,262,'/static/song_covers/auto-77307d79.jpg','/static/songs/AIZO_咒术回战.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'29c6ff75592a228a2164b3346dae1cee'),(2088899211569565697,'Always','Peder B. Helland','Bright Future',3823119,2088884371316674561,'空与风',403,73,'/static/song_covers/auto-33f112bf.jpg','/static/songs/always_peder b.helland.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'3fcca090671042f490d5eca611fb1192'),(2088899211699589121,'An Old Song','MoreanP','空与风的收藏',4516403,2088884371316674561,'空与风',253,130,'/static/song_covers/auto-27e78064.jpg','/static/songs/An Old Song_MoreanP.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'f31b3fde8cc8a6b244987b19f2bede23'),(2088899211762503681,'Autumn','July','空与风的收藏',4093725,2088884371316674561,'空与风',222,134,'/static/song_covers/auto-809b2194.jpg','/static/songs/Autumn_July.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'9dbd100e01f549a7aca441507fa92f26'),(2088899211892527106,'Autumn','LJY','空与风的收藏',3482213,2088884371316674561,'空与风',199,127,'/static/song_covers/auto-990aaaf7.jpg','/static/songs/Autumn_LJY.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'a3f095648693c8c4e7aadd9bd91034e1'),(2088899212022550529,'Beyond The Memory','July','Beyond The Memory',4941873,2088884371316674561,'空与风',235,132,'/static/song_covers/auto-26c589dc.jpg','/static/songs/beyond the memory_July.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'2706cca8694f3d542523722b3877382f'),(2088899212085465090,'Blue Dragon (piano & guitar version)','澤野弘之','「医龍 Team Medical Dragon」オリジナルサウンドトラック',3290463,2088884371316674561,'空与风',214,120,'/static/song_covers/auto-743f8705.jpg','/static/songs/Blue Dragon_泽野弘之.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'28af2be4756bf7f03a6f3e3018a304fa'),(2088899212211294210,'born a stranger(Slow)','Lerge⧸Kan R. Gao','空与风的收藏',2755536,2088884371316674561,'空与风',186,102,'/static/song_covers/auto-36593568.jpg','/static/songs/born a stranger(Slow)_Lerge&Kan R. Gao.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'7e94e36d3d322a271b116d072618cee1'),(2088899212341317633,'Bury the Light','Casey Edwards & Victor Borba','Bury the Light',10052337,2088884371316674561,'空与风',580,136,'/static/song_covers/auto-55bff180.jpg','/static/songs/Bury the Light_dmc5.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'fe0bb131780d47b8cf96a795a4ca13bf'),(2088899212471341057,'Call of Silence','澤野弘之','\"Attack on Titan\" Season 2 Original Soundtrack',2986305,2088884371316674561,'空与风',178,130,'/static/song_covers/auto-4bf0b05d.jpg','/static/songs/Call of Silence(非纯音)_泽野弘之.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'7c2e0f38c935b28fba4c33a225d29734'),(2088899212538449922,'Call of silence','泽野弘之','空与风的收藏',2585077,2088884371316674561,'空与风',200,96,'/static/song_covers/auto-0f2ebe91.jpg','/static/songs/Call of silence_泽野弘之.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'a6e10b109da4545da5a821d5834da598'),(2088899212668473345,'China-汐','余天易，元小汐','空与风的收藏',9943486,2088884371316674561,'空与风',234,320,'/static/song_covers/auto-f10ab0bb.jpg','/static/songs/China-汐_余天易&元小汐.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'c350a4e6a2991e7a6aa0d24476a487f3'),(2088899212802691074,'City','羽肿','空与风的收藏',4662377,2088884371316674561,'空与风',253,134,'/static/song_covers/auto-7c7e994c.jpg','/static/songs/City_羽肿.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'d2bae84c697edd79aea7a3dcc7e92f4c'),(2088899212865605633,'cocoon','林ゆうき','Triangle Original Soundtrack',4761098,2088884371316674561,'空与风',284,130,'/static/song_covers/auto-a51b6950.jpg','/static/songs/cocoon_林ゆうき.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'feb376758354e85c4b173605fc3f3a1e'),(2088899212995629057,'Collapsing World','Lightscape','空与风的收藏',3400074,2088884371316674561,'空与风',191,135,'/static/song_covers/auto-d88319db.jpg','/static/songs/Collapsing World_Lightscape.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'24740f63afac55567a4333c31940402a'),(2088899213058543618,'Cornfield Chase','Hans Zimmer','Interstellar (bootleg remix)',1609589,2088884371316674561,'空与风',127,95,'/static/song_covers/auto-2d1b5d52.jpg','/static/songs/Cornfield Chase 原野追逐_汉斯·季默.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'acc0b1c4af9b5b61102597aa0a5f6063'),(2088899213125652481,'Counter Attack-Mankind (Sasha version)','Samuel Kim','Attack on Titan: Final Season Tribute',3979948,2088884371316674561,'空与风',292,107,'/static/song_covers/auto-405ce115.jpg','/static/songs/Counter Attack-Mankind_泽野弘之.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'3dc2219505ba4e1dc366a6cb6a30f3a6'),(2088899213255675906,'Counter Strike','Lyn','PERSONA5 SCRAMBLE The Phantom Strikers Original Soundtrack',4752316,2088884371316674561,'空与风',305,121,'/static/song_covers/auto-f2b13ab4.jpg','/static/songs/Counter Strike_p5s.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'644a3994b1b08b43a7db6832f0aca1bd'),(2088899213385699330,'crybaby','Seto','空与风的收藏',3147021,2088884371316674561,'空与风',141,116,'/static/song_covers/auto-d62a07d0.jpg','/static/songs/crybaby_Seto.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'85f7f1b711f2a15002cab977f5d12b93'),(2088899213515722753,'Daylight','Jome','Daylight',2645365,2088884371316674561,'空与风',162,129,'/static/song_covers/auto-68ad3d3b.jpg','/static/songs/Daylight_Seredris.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'1eb120de6e925d9b3d5274ff9590f0e7'),(2088899213645746177,'Ephemeral Memories','MoreanP','空与风的收藏',4408954,2088884371316674561,'空与风',236,134,'/static/song_covers/auto-24a38d0e.jpg','/static/songs/Ephemeral Memories_MoreanP.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'9ed5fdbecf2b2e0dcdb64f0fa75838f0'),(2088899213712855042,'Episode 33','She Her Her Hers','SPIRAL',2376371,2088884371316674561,'空与风',256,72,'/static/song_covers/auto-bace64d8.jpg','/static/songs/Episode 33(chunyinyue)_she her her hers.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'eed1e4f1caba3008ff3f50978e72ac58'),(2088899213775769601,'Euphoria','SmYang','空与风的收藏',3129365,2088884371316674561,'空与风',204,86,'/static/song_covers/auto-f477e676.jpg','/static/songs/Euphoria_SmYang.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'dd95dfec972883b84b6c347296020174'),(2088899213905793025,'Eutopia','Yooh','Eutopia',4895787,2088884371316674561,'空与风',293,120,'/static/song_covers/auto-a4f813bf.jpg','/static/songs/Eutopia_Yooh.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'b249650f2f1dc2c5ffba26459d60b6ae'),(2088899213964513281,'Fake Love','vlhs','空与风的收藏',3680517,2088884371316674561,'空与风',243,120,'/static/song_covers/auto-fb3be862.jpg','/static/songs/Fake Love_vlhs.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'af3611ef6cb7b5d07df4a7641b5773dd'),(2088899214031622146,'After the Rain','Novelists','After the Rain',3746527,2088884371316674561,'空与风',242,120,'/static/song_covers/auto-420e0172.jpg','/static/songs/Feeling The Rain_MoreanP.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'af522a484117272ae154cdf29f2b6da7'),(2088899214161645569,'Flower Dance','DJ Okawari','A Cup of Coffee',4274859,2088884371316674561,'空与风',264,126,'/static/song_covers/auto-75c714e1.jpg','/static/songs/Flower Dance_DJ OKAWARI.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'593a65236e9eaee382139bdf892ed806'),(2088899214228754434,'Forest Mixtape','christina kuong','空与风的收藏',2183901,2088884371316674561,'空与风',140,117,'/static/song_covers/auto-504b8011.jpg','/static/songs/Forest Mixtape_christina kuong.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'72354cad45fc303a2b6f483d2694d6f2'),(2088899214291668993,'fterglow','Saiakoup','空与风的收藏',2671939,2088884371316674561,'空与风',162,111,'/static/song_covers/auto-e9df265a.jpg','/static/songs/fterglow_Saiakoup.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'4afbe5dfd27b6dc6d8a5bdff49257593'),(2088899214354583553,'Garden of Lucy(露西的花园)','Maple暖枫','空与风的收藏',2686187,2088884371316674561,'空与风',202,102,'/static/song_covers/auto-89bcb0c1.jpg','/static/songs/Garden of Lucy(露西的花园)_Maple暖枫.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'bde3b2eb457ddf2a150a1d2a82671bcf'),(2088899214484606977,'Gypsophila','MoreanP','空与风的收藏',4857154,2088884371316674561,'空与风',247,144,'/static/song_covers/auto-cedb7cf1.jpg','/static/songs/Gypsophila_MoreanP.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'a9559585c33378ffa0bf1cd4507f4054'),(2088899214551715841,'Heart linked(心弦)','Jannik','空与风的收藏',4103482,2088884371316674561,'空与风',230,131,'/static/song_covers/auto-14df3466.jpg','/static/songs/Heart linked(心弦)_Jannik.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'b4b04e10b0d88d8a55910e7771cad3ac'),(2088899214614630401,'His Theme','Toby Fox','UNDERTALE',1999145,2088884371316674561,'空与风',122,129,'/static/song_covers/auto-657a0eaf.jpg','/static/songs/His Theme_传说之下.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'a6dd9068a043b488bbc49db62016ce38'),(2088899214744653825,'I believe','Lyn','『ペルソナ5 ザ・ロイヤル』 オリジナル・サウンドトラック',4504516,2088884371316674561,'空与风',271,129,'/static/song_covers/auto-6f3a1cea.jpg','/static/songs/I believe_p5r.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'d9415224353a1ad89a232f452898ec2d'),(2088899214811762690,'i still think of you','Emptiness','空与风的收藏',1191184,2088884371316674561,'空与风',86,98,'/static/song_covers/auto-853de346.jpg','/static/songs/i still think of you_Emptiness.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'b5b25bc95f7ec0891bb3835f10a50d6e'),(2088899214874677250,'it\'s 6pm but I miss u already','bluelee','空与风的收藏',3065439,2088884371316674561,'空与风',182,99,'/static/song_covers/auto-a9a52346.jpg','/static/songs/it\'s 6pm but I miss u already_bluelee.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'137781733b6097cd75a27e1088dd22c0'),(2088899215008894978,'it`s your birthday','叁X','空与风的收藏',7135208,2088884371316674561,'空与风',178,320,'/static/song_covers/auto-3249fbe4.jpg','/static/songs/it`s your birthday_叁X.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'09384cef108592001abca0867b85a488'),(2088899215076003841,'Kilig','CMJ','空与风的收藏',3526625,2088884371316674561,'空与风',204,118,'/static/song_covers/auto-657ffaf8.jpg','/static/songs/Kilig_CMJ.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'982c2ba4311095ef18093af415de65b7'),(2088899215138918401,'Kiss the Rain','Yiruma','Best Of',3977949,2088884371316674561,'空与风',302,104,'/static/song_covers/auto-7dc0cf9d.jpg','/static/songs/kiss the rain_Yiruma.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'e55b65335e509846ae89933b317828c4'),(2088899215268941825,'Komorebi','田雄理秀','空与风的收藏',3547691,2088884371316674561,'空与风',212,132,'/static/song_covers/auto-8e180b80.jpg','/static/songs/Komorebi_田雄理秀.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'70be6dceb816582ddff3a5ab27ee6273'),(2088899215352827906,'Life Will Change','p5r','空与风的收藏',5234162,2088884371316674561,'空与风',259,141,'/static/song_covers/auto-ebe37a5b.jpg','/static/songs/Life Will Change_p5r.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'aba5fb762251a688b5f8677d7ff604a7'),(2088899215461879810,'Lifeline','Zeraphym','Lifeline',5122426,2088884371316674561,'空与风',315,128,'/static/song_covers/auto-4683f6a6.jpg','/static/songs/Lifeline_六翼使徒.mp3','2026-08-16 16:02:37','mp3',NULL,NULL,'fbccb8e913d390c5a5fa5bed8b3f1e5e'),(2088899215524794369,'Light of Nibel','ori','空与风的收藏',3739833,2088884371316674561,'空与风',259,98,'/static/song_covers/auto-e204c35c.jpg','/static/songs/Light of Nibel_ori .mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'95b265b86da846b7dcbf536494c6c695'),(2088899215591903233,'RISE','The Glitch Mob, Mako feat. The Word Alive','League Of Legends Worlds Anthems - Vol. 1: 2014-2023',3236630,2088884371316674561,'空与风',194,129,'/static/song_covers/auto-55dfa1fb.jpg','/static/songs/lol - rise_fe18dfa4.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'2d328dc9698e1671becdd4ef64ca29a4'),(2088899215659012098,'【FREE】lucky','Selde4cash','空与风的收藏',1601109,2088884371316674561,'空与风',142,77,'/static/song_covers/auto-60e6bda0.jpg','/static/songs/lucky_Selde4cash.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'22e9111392ebf668dd17a68ea3b219e5'),(2088899215726120961,'MELANCHOLY','White Cherry','空与风的收藏',2985106,2088884371316674561,'空与风',259,88,'/static/song_covers/auto-1aec3143.jpg','/static/songs/MELANCHOLY_White Cherry.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'166444384eb6d3bcd7770f328c256e70'),(2088899215856144386,'Merry Christmas Mr. Lawrence','Ryuichi Sakamoto','Three',4864115,2088884371316674561,'空与风',335,114,'/static/song_covers/auto-b91ce147.jpg','/static/songs/Merry Christmas Mr. Lawrence_坂本龙一.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'cd20fad6130427293c75f1a19776355d'),(2088899215923253249,'midsummer','Maple暖枫','空与风的收藏',3950365,2088884371316674561,'空与风',219,105,'/static/song_covers/auto-27e74d0d.jpg','/static/songs/midsummer_Maple暖枫.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'9ed24fdea555923fb9f81339c742cc97'),(2088899215986167809,'Moon and You','Kryust','空与风的收藏',2644241,2088884371316674561,'空与风',203,101,'/static/song_covers/auto-71ca9389.jpg','/static/songs/Moon and You_Kryust.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'c86a074e6678174916a3c1f969c0ecfd'),(2088899216116191234,'My Soul (Instrumental) (Bonus Track)','July','Beyond The Memory',4118743,2088884371316674561,'空与风',230,120,'/static/song_covers/auto-f788827c.jpg','/static/songs/My Soul_July.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'4a1ffbb0657541684ff52841e5ad0099'),(2088899216183300098,'Night Cruising(夜间巡航)','Vesselax','空与风的收藏',2035983,2088884371316674561,'空与风',127,116,'/static/song_covers/auto-dd2ca90a.jpg','/static/songs/Night Cruising(夜间巡航)_Vesselax.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'56f0a7553c37715b122c7e243f062def'),(2088899216246214658,'Normal No More(小提琴violin)','Strictlyviolin荀博','空与风的收藏',2468648,2088884371316674561,'空与风',154,126,'/static/song_covers/auto-24e864d5.jpg','/static/songs/Normal No More(小提琴violin)_Strictlyviolin荀博 .mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'0ce19001cd8170e3469f415f69ee62e4'),(2088899216309129217,'Normal No More','TYSM','Normal No More',3224487,2088884371316674561,'空与风',205,124,'/static/song_covers/auto-9ccd21e3.jpg','/static/songs/Normal No More_TYSM.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'d31fd88e04df0a483e1c40e3449b0daa'),(2088899216376238081,'Old Memory','蓝云木','空与风的收藏',2923675,2088884371316674561,'空与风',191,118,'/static/song_covers/auto-0c27af6b.jpg','/static/songs/Old Memory_蓝云木.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'622a5bb7f9d8144c9f44402a2d1566c5'),(2088899216443346945,'Past Lives','Martin Arteta','Past Lives',1933379,2088884371316674561,'空与风',135,110,'/static/song_covers/auto-4a25c773.jpg','/static/songs/Past Lives_Sapientdream.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'1c32d8b50a4d88d01bad9fd2375b7d14'),(2088899216506261506,'Please Don\'t Go','Joel Adams','Please Don\'t Go',3339129,2088884371316674561,'空与风',213,121,'/static/song_covers/auto-5228c084.jpg','/static/songs/Please Don\'t Go_Joe Adams.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'11b78eeebc526d1805b2a1142bcb45b7'),(2088899216640479233,'Rain after Summer','羽肿','Rain after Summer',5852198,2088884371316674561,'空与风',345,133,'/static/song_covers/auto-889bc3a0.jpg','/static/songs/Rain after Summer_羽肿.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'35d8c15fdc18b16a1fac22d0f9263b2a'),(2088899216770502657,'Rainy Blue (蓝色阴雨)','饭碗的彼岸,夜莺与玫瑰','空与风的收藏',4974159,2088884371316674561,'空与风',266,141,'/static/song_covers/auto-19c03771.jpg','/static/songs/Rainy Blue (蓝色阴雨)_饭碗的彼岸,夜莺与玫瑰.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'ece2515d32d0bcef78f7ffaeb99a3456'),(2088899216833417217,'Rainy Dumplings','饭碗的彼岸','空与风的收藏',5772092,2088884371316674561,'空与风',311,140,'/static/song_covers/auto-29720f85.jpg','/static/songs/Rainy Dumplings_饭碗的彼岸.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'b6fa0294af3f04434078db4ecb9e6807'),(2088899216963440641,'rainy stars','Maple暖枫','空与风的收藏',2852836,2088884371316674561,'空与风',217,99,'/static/song_covers/auto-dcc477d7.jpg','/static/songs/rainy stars_Maple暖枫.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'6f9271e5c2602bc49ece1d007a203174'),(2088899217093464065,'Rainy Whisper','林玥风龄、SHRYKI-星韵雨','空与风的收藏',11869588,2088884371316674561,'空与风',297,320,NULL,'/static/songs/Rainy Whisper_林玥风龄、SHRYKI-星韵雨.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'53254f4360482faefdc3bd8617f285d4'),(2088899217219293186,'Right Here Waiting','bandari','空与风的收藏',3815645,2088884371316674561,'空与风',247,117,'/static/song_covers/auto-b77170df.jpg','/static/songs/Right Here Waiting_bandari.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'f3ad333ce55697b689e6f0c862283d28'),(2088899217290596354,'RISE','The Glitch Mob, Mako feat. The Word Alive','League Of Legends Worlds Anthems - Vol. 1: 2014-2023',3236630,2088884371316674561,'空与风',194,129,'/static/song_covers/auto-7443e2a4.jpg','/static/songs/rise_lol.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'2d328dc9698e1671becdd4ef64ca29a4'),(2088899217353510913,'Rivers in the Desert','Lyn','PERSONA5 ORIGINAL SOUNDTRACK',5585621,2088884371316674561,'空与风',315,139,'/static/song_covers/auto-16205dfa.jpg','/static/songs/Rivers In the Desert_p5r.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'aed1c01e2c603b92cd7e7f374544c256'),(2088899217420619778,'Sacred Play Secret Place(治愈)','Matryoshka','空与风的收藏',2263274,2088884371316674561,'空与风',209,68,'/static/song_covers/auto-b257eb49.jpg','/static/songs/Sacred Play Secret Place(治愈)_Matryoshka.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'aeeea98136967cb7103f0a471e694f32'),(2088899217546448898,'Sacred Play Secret Place','matryoshka','Laideronnette',4725854,2088884371316674561,'空与风',318,117,'/static/song_covers/auto-d8f90deb.jpg','/static/songs/Sacred Play Secret Place_Matryoshka.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'fb9771fae393d3e3a9f9ec80f07f9113'),(2088899217609363457,'Sample this','小鱼拖地','空与风的收藏',3023030,2088884371316674561,'空与风',204,111,'/static/song_covers/auto-ab26d241.jpg','/static/songs/Sample this_小鱼拖地.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'a1ccd73f619c65f551649a71012bc963'),(2088899217676472321,'Somewhere','Voctave','Somewhere',3905374,2088884371316674561,'空与风',230,132,'/static/song_covers/auto-c989eb01.jpg','/static/songs/somewhere_july.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'5350593da344410fb3c97afdf9df9d06'),(2088899217806495745,'Somnambulating','羽肿','空与风的收藏',5734269,2088884371316674561,'空与风',321,141,'/static/song_covers/auto-9bc2cbb7.jpg','/static/songs/Somnambulating_羽肿.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'7dbc298f6aa6eab80ddba3cc6125fc67'),(2088899217869410306,'바람에 쓰는 편지','July','Happiness',4158345,2088884371316674561,'空与风',244,130,'/static/song_covers/auto-79d08830.jpg','/static/songs/Story of us_July.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'f4a8792515ba9c402e5a0395f2cb1f52'),(2088899217936519169,'Stray','Feint','空与风的收藏',4014292,2088884371316674561,'空与风',247,124,'/static/song_covers/auto-5a77b827.jpg','/static/songs/Stray_Feint.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'53f37e8395a70999f903a98d5c102f92'),(2088899218003628034,'Summer (From “Kikujiro”)','久石譲','ENCORE',2010149,2088884371316674561,'空与风',154,101,'/static/song_covers/auto-2b533c00.jpg','/static/songs/Summer_久石让.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'b17c91adefe13b28d159768ea2408c1f'),(2088899218066542594,'The Rebel Path','P.T. Adamczyk','Cyberpunk 2077',3908462,2088884371316674561,'空与风',251,121,'/static/song_covers/auto-005ad4fb.jpg','/static/songs/The Rebel Path_cyberpunk2077.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'7713a586c60d40be26871716a96d0e18'),(2088899218133651457,'The Truth That You Leave','Pianoboy高至豪','Pianoboy',3246201,2088884371316674561,'空与风',222,113,'/static/song_covers/auto-af6a833a.jpg','/static/songs/The Truth That You Leave_高至豪.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'1f10ee42f1e4a14bea46af4b352470b3'),(2088899218259480577,'The Way I Still Love You','Alisa','空与风的收藏',2856685,2088884371316674561,'空与风',153,122,'/static/song_covers/auto-a145b5b9.jpg','/static/songs/The Way I Still Love You_Alisa.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'cac2dcea0da77e5d6a4af1e6e52bad8a'),(2088899218326589442,'The Way I Still Love You','Reynard Silva','空与风的收藏',3729155,2088884371316674561,'空与风',227,128,'/static/song_covers/auto-1d88836b.jpg','/static/songs/The Way I Still Love You_Reynard Silva.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'8c9f03c9563147747515ea5bc102e6f8'),(2088899218393698306,'Time is broken','xxx','空与风的收藏',3439301,2088884371316674561,'空与风',189,130,'/static/song_covers/auto-9794da9a.jpg','/static/songs/Time is broken_xxx.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'e23a92882672172452876cfeb8b9bab0'),(2088899218460807170,'Time Stop','L-79','Time Stop',3444886,2088884371316674561,'空与风',209,129,'/static/song_covers/auto-3da364e0.jpg','/static/songs/Time Stop_BLACKDD&CYTEAM&PICK&知晏.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'a440ac4ab41bd2e097de3528402cd99f'),(2088899218586636290,'Time To Love','악토버','Vain',3329633,2088884371316674561,'空与风',241,108,'/static/song_covers/auto-526c62d4.jpg','/static/songs/Time To Love_October.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'06f437f95898cd8e915790b0e26b1cd3'),(2088899218649550850,'Timelooper','Jannik','空与风的收藏',5611651,2088884371316674561,'空与风',308,142,'/static/song_covers/auto-7945a759.jpg','/static/songs/Timelooper_Jannik.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'6263bd6bfbdd0b5f0aa8a8a03e5f3a25'),(2088899218720854018,'Towards the Light','Jacoo','Release Your Mind',3756928,2088884371316674561,'空与风',237,123,'/static/song_covers/auto-05a91df8.jpg','/static/songs/Towards the Light_jacoo.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'e37dced6944a353f80c3ff0cc7bbc22a'),(2088899218783768578,'Town of Windmill','a_hisa','Single Collection',2039751,2088884371316674561,'空与风',142,111,'/static/song_covers/auto-49e52dd1.jpg','/static/songs/Town of Windmill_a_hisa.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'28473ce1196c44d592cecef9076aaebc'),(2088899218909597697,'Track in Time(追溯时光)','Dennis Kuo','空与风的收藏',4183634,2088884371316674561,'空与风',319,97,'/static/song_covers/auto-913ae772.jpg','/static/songs/Track in Time(追溯时光)_Dennis Kuo.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'03079ae33d4006e75275f56aba7dfe8f'),(2088899218980900865,'two as one','未知作家','空与风的收藏',4564639,2088884371316674561,'空与风',263,131,'/static/song_covers/auto-810b1728.jpg','/static/songs/two as one_未知作家.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'d12a0eba3d205badee339004672cc997'),(2088899219043815425,'ULTRA FLY','宮野真守','PASSAGE',3811378,2088884371316674561,'空与风',231,130,'/static/song_covers/auto-2f4efd18.jpg','/static/songs/Ultra Fly_zero.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'7d30d6f55e893c5ec85d52314a4deb63'),(2088899219178033154,'Una mattina','Ludovico Einaudi','Intouchables - Ziemlich Beste Freunde',5530847,2088884371316674561,'空与风',402,108,'/static/song_covers/auto-da1f16b2.jpg','/static/songs/Una Mattina_intouchables.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'c35c43f033cbb26839bc07141d492c20'),(2088899219240947714,'under the sea','Arealy仁辰&南有乔木','空与风的收藏',3230955,2088884371316674561,'空与风',208,120,'/static/song_covers/auto-bf2bd1c9.jpg','/static/songs/under the sea_Arealy仁辰&南有乔木.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'10cc7db3cf4652fc087d2425ac898f61'),(2088899219303862273,'Vagrant','Feint feat. Veela','Alchemy',4223538,2088884371316674561,'空与风',269,123,'/static/song_covers/auto-4cf33d99.jpg','/static/songs/vagrant_feint-veela.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'6474329c063ff0a85a9a74a4c04e4dd5'),(2088899219370971138,'We Are  Wonders of Existence','Madza','空与风的收藏',3646173,2088884371316674561,'空与风',215,134,'/static/song_covers/auto-f74c448d.jpg','/static/songs/We Are  Wonders of Existence_Madza.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'cef60b37c1508ed63d13ee5556d5ba3f'),(2088899219433885697,'We Don’t Talk Anymore','Charlie Puth & Selena Gomez','Love Like That - Feeling Blue',3924248,2088884371316674561,'空与风',220,140,'/static/song_covers/auto-b400da21.jpg','/static/songs/We Don\'t Talk Anymore_Charlie PuthSelena Gomez.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'ef56c82d29330c09bd18522602859bc1'),(2088899219500994562,'What Are Words','Chris Medina','What Are Words',3181503,2088884371316674561,'空与风',184,130,'/static/song_covers/auto-73501f03.jpg','/static/songs/what are words_Chris Medina.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'f5a8337ff8b3df2878ed0066c5395635'),(2088899219563909122,'Windy Hill','羽肿','Windy Hill',5398473,2088884371316674561,'空与风',309,138,'/static/song_covers/auto-86d079b0.jpg','/static/songs/Windy Hill_羽肿.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'424a86f4c39f9480cfebbd4b30b34bb3'),(2088899219631017986,'Улетали птицами гордыми','double x','空与风的收藏',3409245,2088884371316674561,'空与风',228,114,'/static/song_covers/auto-1909bdaa.jpg','/static/songs/Улетали птицами гордыми_double x.mp3','2026-08-16 16:02:38','mp3',NULL,NULL,'a00ad2021b6b6a1d74e71e45c37c6e66'),(2088899219698126849,'ᐇ','Seto','空与风的收藏',1251535,2088884371316674561,'空与风',84,112,'/static/song_covers/auto-d4751672.jpg','/static/songs/ᐇ_Seto.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'28dad4ea915cac488c29f0bfbcda3d5b'),(2088899219761041409,'ヤキモチ','高橋優','今、そこにある明滅と群生',5220223,2088884371316674561,'空与风',315,130,'/static/song_covers/auto-89bb5212.jpg','/static/songs/ヤキモチ_高桥优.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'2e7a69f25da25dcab17b6d256f5aee6d'),(2088899219886870529,'《 一等情事 》','红泪石戒指','',6617952,2088884371316674561,'空与风',217,242,'/static/song_covers/auto-1c75f89b.jpg','/static/songs/一等情事_许一鸣 .mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'7d9a8b59b9e499b138d9823c73b05731'),(2088899219953979393,'三葉のテーマ','RADWIMPS','君の名は。',3010897,2088884371316674561,'空与风',245,96,'/static/song_covers/auto-276a6fa4.jpg','/static/songs/三葉のテーマ（三叶的主题曲)_RADWIMPS.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'2b4a72fed902ecbe753b85a49826d78d'),(2088899220016893953,'下潜','川青','[standalone recordings]',3411553,2088884371316674561,'空与风',218,111,'/static/song_covers/auto-e20b863f.jpg','/static/songs/下潜 - 川青&Morerare.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'0a9be070aca04d9d2d5eb027b73e09f6'),(2088899220151111682,'不同的心脏 相同的頻率','hea2tside','空与风的收藏',7366290,2088884371316674561,'空与风',182,320,'/static/song_covers/auto-ffa824cb.jpg','/static/songs/不同的心脏 相同的頻率_hea2tside.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'e00dd34a0bbc34020c6fd5aea21d5c7f'),(2088899220218220545,'不要死在那个夏天','豆糕p','空与风的收藏',1375838,2088884371316674561,'空与风',90,109,'/static/song_covers/auto-85b7507e.jpg','/static/songs/不要死在那个夏天_豆糕p.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'057ff6c748de6964e8adc5f2608fd187'),(2088899220285329410,'与光同尘','逆时针向','空与风的收藏',3208290,2088884371316674561,'空与风',208,116,'/static/song_covers/auto-ee61a3af.jpg','/static/songs/与光同尘_逆时针向.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'eae5556f0e54cdfb7e0740302b0c8524'),(2088899220348243969,'临安初雨','水浒Q传游戏音乐','空与风的收藏',2180910,2088884371316674561,'空与风',121,137,'/static/song_covers/auto-7a239945.jpg','/static/songs/临安初雨_水浒Q传游戏音乐.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'8e7ed7349dd7da47578f54abbeaf770f'),(2088899220482461698,'为霜','羽肿','空与风的收藏',4455299,2088884371316674561,'空与风',297,116,'/static/song_covers/auto-23b51d16.jpg','/static/songs/为霜_羽肿.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'83dd892fe66560e6f7ebd9a9a799f504'),(2088899220683788289,'云村的告别','邱有句','空与风的收藏',7774003,2088884371316674561,'空与风',264,110,'/static/song_covers/auto-23a742f5.jpg','/static/songs/云村的告别_邱有句.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'988c7f0804988537483a8497b0bb1924'),(2088899220750897153,'Early Summer Rain','高梨康治','Naruto Shippuden Original Soundtrack II',3468877,2088884371316674561,'空与风',206,131,'/static/song_covers/auto-dc1cdf1e.jpg','/static/songs/五月雨_高梨康治.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'f29b960e3d45a815d907c334b78dbbd3'),(2088899220813811713,'人间蜉蝣','徐深','空与风的收藏',2346761,2088884371316674561,'空与风',143,113,'/static/song_covers/auto-171880c5.jpg','/static/songs/人间蜉蝣_徐深.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'05002a88d153b8577ac646721c62d6b2'),(2088899220876726273,'仲夏夜之梦','Nyaameow','空与风的收藏',1439830,2088884371316674561,'空与风',90,110,'/static/song_covers/auto-d1fc6d4f.jpg','/static/songs/仲夏夜之梦_Nyaameow.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'7f91c39d2f1e532491d61ece42d96078'),(2088899221010944002,'仲夏物语','茶青','空与风的收藏',3957032,2088884371316674561,'空与风',222,131,'/static/song_covers/auto-c640dc9d.jpg','/static/songs/仲夏物语_茶青.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'ab2912aa77bc4f3605f08531ea2be302'),(2088899221078052865,'仲夏的凉风','hea2t','空与风的收藏',2591492,2088884371316674561,'空与风',160,122,'/static/song_covers/auto-7aa59aa3.jpg','/static/songs/仲夏的凉风_hea2t.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'42e59eade1ef7e06cb54635e925432a3'),(2088899221145161730,'似水流年·落樱','皓','空与风的收藏',1587091,2088884371316674561,'空与风',106,110,'/static/song_covers/auto-a62fe887.jpg','/static/songs/似水流年·落樱_皓.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'f385c299b2e4382d6b684ed6ca4d4bc4'),(2088899221208076290,'你有听过那位传奇的故事吗','笔龙XDDD [BV1eD4y1B7qD]','空与风的收藏',2212854,2088884371316674561,'空与风',131,128,'/static/song_covers/auto-d249cfa2.jpg','/static/songs/你有听过那位传奇的故事吗_笔龙XDDD .mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'cb3521d0d23bb2e311467a34edad926a'),(2088899221275185154,'像鱼','王貳浪','像鱼',4525066,2088884371316674561,'空与风',286,120,'/static/song_covers/auto-1691d04a.jpg','/static/songs/像鱼_王贰浪“.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'84cda932bfc195e33176fba43cbb03ef'),(2088899221405208578,'蘭亭序','周杰倫','魔杰座',4090604,2088884371316674561,'空与风',256,125,'/static/song_covers/auto-f73ecad7.jpg','/static/songs/兰亭序_周杰伦.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'5b2e774657809574a8de32c3ebb2a8ee'),(2088899221472317442,'在飞行','金色大厅交响乐演奏星游记','空与风的收藏',1960658,2088884371316674561,'空与风',107,134,'/static/song_covers/auto-2e3e37c7.jpg','/static/songs/再飞行_金色大厅交响乐演奏星游记.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'aff7ddf1861862b5ada730d464502cee'),(2088899221535232002,'冬眠','司南','空与风的收藏',4168857,2088884371316674561,'空与风',270,118,'/static/song_covers/auto-d0790b99.jpg','/static/songs/冬眠_司南.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'03b1b72603ee58ad6c491ac5f0db7a72'),(2088899221598146561,'出山','花粥','[standalone recordings]',3727632,2088884371316674561,'空与风',202,137,'/static/song_covers/auto-ee255b76.jpg','/static/songs/出山_花粥-王胜娚.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'3ac17675ffacf9cbca3963b4958b9933'),(2088899221728169985,'初夏的风','Hea2t','空与风的收藏',3534790,2088884371316674561,'空与风',209,119,'/static/song_covers/auto-2e89f448.jpg','/static/songs/初夏的风_Hea2t.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'6ae7eeea7c37a263b4e4b9ee86060213'),(2088899221858193410,'别(Stirngs)','吴宇深','空与风的收藏',4241944,2088884371316674561,'空与风',238,106,'/static/song_covers/auto-9022f691.jpg','/static/songs/别(Stirngs)_吴宇深.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'6c9e7d09bea8389e8b11d56b3f26b6ce'),(2088899221988216833,'别念','funton','空与风的收藏',4014458,2088884371316674561,'空与风',235,128,'/static/song_covers/auto-ec72a2c2.jpg','/static/songs/别念_funton.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'4fce45b49788ae2bb2369fbb383aa825'),(2088899222063714306,'盗墓笔记·十年人间','李常超','盗墓笔记·十年人间',4918916,2088884371316674561,'空与风',277,136,'/static/song_covers/auto-26d70393.jpg','/static/songs/十年人间_李常超.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'bce31b427e31e4fef7ddaadc11322240'),(2088899222181154817,'千秋','古剑奇谭三','空与风的收藏',3632065,2088884371316674561,'空与风',193,125,'/static/song_covers/auto-94e26d64.jpg','/static/songs/千秋_古剑奇谭三.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'7fffa32ee1c822e4762cdf2fd29be791'),(2088899222248263682,'去年夏天','王大毛','去年夏天',4472561,2088884371316674561,'空与风',245,135,'/static/song_covers/auto-855d3667.jpg','/static/songs/去年夏天_王大毛.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'4bb7ac903b79200b474245b961f6b1f8'),(2088899222315372546,'向晚之诗Ⅱ','寒柴⧸壹勺籽糖','空与风的收藏',3123126,2088884371316674561,'空与风',197,102,'/static/song_covers/auto-5defad95.jpg','/static/songs/向晚之诗Ⅱ_寒柴⧸壹勺籽糖.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'9c025c6304e9773d8a1dfd1cfa8e6d36'),(2088899222441201665,'听雪楼默守','小里多','空与风的收藏',5250204,2088884371316674561,'空与风',190,114,'/static/song_covers/auto-500a4f96.jpg','/static/songs/听雪楼默守_小里多.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'09ae01bced6a4e592cfbcced46060216'),(2088899222504116226,'告白の夜 (LIVE!! Ayasa Theater episode 7)','Ayasa','LIVE!! Ayasa Theater episode 7',4840541,2088884371316674561,'空与风',290,130,'/static/song_covers/auto-bc8ae55c.jpg','/static/songs/告白の夜_Ayasa.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'b204ddf109cf8dcc6ba697d2ec5e8a06'),(2088899222634139650,'和煦的糖果风','[unknown]','葬花 Soundtrack',3588910,2088884371316674561,'空与风',183,107,'/static/song_covers/auto-d46eec98.jpg','/static/songs/和煦的糖果风_Candy_Wind.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'48095eca16492c112127cda0b1af4077'),(2088899222701248513,'哪吒','蔡翊昇','空与风的收藏',3299442,2088884371316674561,'空与风',193,121,'/static/song_covers/auto-ce8386b4.jpg','/static/songs/哪吒_蔡翊昇.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'7c73530d531bad76b35d8553801f0ed5'),(2088899222768357378,'四季予你','程响','[standalone recordings]',4108982,2088884371316674561,'空与风',248,125,'/static/song_covers/auto-7fd5dcd6.jpg','/static/songs/四季予你_程响.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'582b4f79905f78e5cc7025b13d14c098'),(2088899222831271938,'四月十一','BlackDD','空与风的收藏',2760671,2088884371316674561,'空与风',141,109,'/static/song_covers/auto-69bc4f18.jpg','/static/songs/四月十一_BlackDD.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'8bf248a39edd6d2f1ff4d0f302960de3'),(2088899222961295361,'城南花已开','三亩地','[standalone recordings]',5326155,2088884371316674561,'空与风',269,123,'/static/song_covers/auto-c44856a3.jpg','/static/songs/城南花已开_三亩地.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'6b84c3d55cf3dfa5683a1813c13d7fc6'),(2088899223024209922,'夏·烟火','LIKPIA','空与风的收藏',3403225,2088884371316674561,'空与风',229,112,'/static/song_covers/auto-da008060.jpg','/static/songs/夏·烟火_LIKPIA.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'510fbf067761affd7ce64c2b7c19db3a'),(2088899223091318785,'夏に花が散る (夏天花落)','羽肿','空与风的收藏',5414851,2088884371316674561,'空与风',284,133,'/static/song_covers/auto-6913e6ac.jpg','/static/songs/夏に花が散る (夏天花落)_羽肿.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'2f8b7b0328a5f1e78221d8f3676d85de'),(2088899223158427649,'夏の喚','邱有句','空与风的收藏',4983298,2088884371316674561,'空与风',297,127,'/static/song_covers/auto-49a954f6.jpg','/static/songs/夏の喚_邱有句.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'7f171e7b793ddd9d7c5c5da928e49356'),(2088899223221342209,'夏の夜 (夏夜)','凤凰院零零','空与风的收藏',3519450,2088884371316674561,'空与风',220,121,'/static/song_covers/auto-d427556a.jpg','/static/songs/夏の夜 (夏夜)_凤凰院零零.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'47d75dbffd5f09ed8648d6e061901cb9'),(2088899223355559938,'夏の雪','Maple暖枫','空与风的收藏',4427274,2088884371316674561,'空与风',200,102,'/static/song_covers/auto-8c745ee9.jpg','/static/songs/夏の雪_Maple暖枫.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'71b4dc249e52379bbdefef5fff8835da'),(2088899223481389058,'夏恋 - Insturmental -','Otokaze','夏恋',5611838,2088884371316674561,'空与风',266,134,'/static/song_covers/auto-7300f12c.jpg','/static/songs/夏恋_Otokaze.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'66449257f378128626d50dfa12693e95'),(2088899223548497922,'多情种','要不要买菜','空与风的收藏',3296509,2088884371316674561,'空与风',215,117,'/static/song_covers/auto-b14bc15c.jpg','/static/songs/多情种_要不要买菜.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'b378edf161774de49e0b9619c9bb87ec'),(2088899223615606785,'夜、萤火虫与你','Aniface','空与风的收藏',2949575,2088884371316674561,'空与风',189,102,'/static/song_covers/auto-53b9dd98.jpg','/static/songs/夜、萤火虫与你_Aniface.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'fdf9a785ee8e4153fd9affd48a2ffce2'),(2088899223682715650,'夜的钢琴曲5','石进','空与风的收藏',3357787,2088884371316674561,'空与风',214,122,'/static/song_covers/auto-8afc8d11.jpg','/static/songs/夜的钢琴曲5_石进.mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'aab1bb728537440b5903cebf1d2a2edc'),(2088899223745630209,'夢と葉桜','青木月光 feat. 初音ミク','花楽里漫葉集 feat.初音ミク',3667831,2088884371316674561,'空与风',253,112,'/static/song_covers/auto-75d5905d.jpg','/static/songs/夢と葉桜 (梦与叶樱)_青木月光&初音未来 (初音ミク).mp3','2026-08-16 16:02:39','mp3',NULL,NULL,'acadea0f62ca48cef6d44a22f9452f44'),(2088899223875653634,'天龙八部之宿敌','许嵩','空与风的收藏',5569702,2088884371316674561,'空与风',266,132,'/static/song_covers/auto-d4607058.jpg','/static/songs/天龙八部之宿敌_许嵩.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'c0a9c9cbfa15c15f5b2001ea22cf0eb6'),(2088899223942762497,'失真的梦','Maple暖枫','空与风的收藏',2786085,2088884371316674561,'空与风',196,110,'/static/song_covers/auto-cb796057.jpg','/static/songs/失真的梦_Maple暖枫.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'81eb183a096f00020fd9d28cbe8a95e1'),(2088899224005677057,'寂川','忘乡','空与风的收藏',5087097,2088884371316674561,'空与风',281,103,'/static/song_covers/auto-06ec7772.jpg','/static/songs/寂川_忘乡.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'54ac01a1e938d8e0129185a0ed77ebf5'),(2088899224152477698,'寄夏予你','MoreanP','空与风的收藏',5046511,2088884371316674561,'空与风',293,134,'/static/song_covers/auto-5c590751.jpg','/static/songs/寄夏予你_MoreanP.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'5b839f8815c7dc6aa28eb6722fcca06c'),(2088899224198615041,'小宇','蓝心羽','空与风的收藏',4180089,2088884371316674561,'空与风',266,124,'/static/song_covers/auto-ead86c22.jpg','/static/songs/小宇_蓝心羽.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'064684f8b4b7aa30829907fe9dd16e8c'),(2088899224265723906,'小水鲸','Maple暖枫','空与风的收藏',2917514,2088884371316674561,'空与风',194,119,'/static/song_covers/auto-e5ba4c1d.jpg','/static/songs/小水鲸_Maple暖枫.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'6c41b9be46676febb36b0933044e0e2e'),(2088899224332832769,'尘埃','MoFeansy','空与风的收藏',2792550,2088884371316674561,'空与风',187,105,'/static/song_covers/auto-46773064.jpg','/static/songs/尘埃_MoFeansy.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'a26cb24446713d10b1b3585b7aaee9f9'),(2088899224399941634,'尘埃与流萤','依旧在雨中等你,鹤见江野','空与风的收藏',4499507,2088884371316674561,'空与风',279,127,'/static/song_covers/auto-e754e94b.jpg','/static/songs/尘埃与流萤_依旧在雨中等你&鹤见江野.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'4ee7de6f5e117415cd2f72102d048ab9'),(2088899224467050497,'山外小楼夜听雨','任然','从小到大',4175639,2088884371316674561,'空与风',250,132,'/static/song_covers/auto-c682e6ff.jpg','/static/songs/山外小楼夜听雨_任然.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'c89d1ec047d77306275a88884a5033fc'),(2088899224525770754,'山海不可平','CMJ','空与风的收藏',1990617,2088884371316674561,'空与风',113,110,'/static/song_covers/auto-36b45157.jpg','/static/songs/山海不可平_CMJ.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'a3fa668487e97e87bf6c478cc2d7e86e'),(2088899224592879617,'山海皆可平','CMJ','空与风的收藏',2100993,2088884371316674561,'空与风',137,110,'/static/song_covers/auto-37c92daa.jpg','/static/songs/山海皆可平_CMJ.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'6651982e9568a5ea38f05b92d7c6ad35'),(2088899224655794178,'年轮（电视剧《花千骨》插曲）','张碧晨','花千骨 电视原声带',4537172,2088884371316674561,'空与风',275,130,'/static/song_covers/auto-92d43096.jpg','/static/songs/年轮_张碧晨.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'347bfc82d7191e540fe00fa2964a5894'),(2088899224722903041,'弱水三千','未知作家','空与风的收藏',3682732,2088884371316674561,'空与风',191,126,'/static/song_covers/auto-e6cf1fd8.jpg','/static/songs/弱水三千_未知作家.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'7c1c94d80d48aae0c86be5c249808ae3'),(2088899224790011905,'归寻','等什么君','空与风的收藏',3128855,2088884371316674561,'空与风',195,115,'/static/song_covers/auto-4ca07d94.jpg','/static/songs/归寻_等什么君.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'4c70778a54299e46c5499130f22d172c'),(2088899224920035329,'得似旧时','忘乡','空与风的收藏',3597632,2088884371316674561,'空与风',188,124,'/static/song_covers/auto-607a6bfa.jpg','/static/songs/得似旧时_忘乡.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'93cb58a3191a9479c5ff2ec91c1a8858'),(2088899225003921409,'忆夏思乡','MoreanP','空与风的收藏',4645591,2088884371316674561,'空与风',263,128,'/static/song_covers/auto-f92f8170.jpg','/static/songs/忆夏思乡_MoreanP.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'ab70899aad2598101fba5ca1e8f01306'),(2088899225045864450,'Grace','haruka nakamura','Grace',4919656,2088884371316674561,'空与风',302,129,'/static/song_covers/auto-d360bd87.jpg','/static/songs/惊鸿(Grace)_Jannik.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'2c34c08e6111e445e9eb3e83d559a686'),(2088899225112973314,'愛のテーマ ［M-62A pfのみ］','矢野立美','ウルトラマンティガ 25th ANNIVERSARY MUSIC COLLECTION',1719628,2088884371316674561,'空与风',120,105,'/static/song_covers/auto-1e7fc4de.jpg','/static/songs/愛のテーマ (爱的主题) 迪迦奥特曼插曲_矢野立美.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'ddaefb2ba9f9e86eecc36d3a91e87767'),(2088899225175887874,'慢慢','小乐哥','空与风的收藏',3885031,2088884371316674561,'空与风',238,122,'/static/song_covers/auto-e2386727.jpg','/static/songs/慢慢_小乐哥.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'de7b81b87e00acf4744eaafb432898e1'),(2088899225305911298,'戏文说','叫宝宝&祥嘞嘞','空与风的收藏',3104600,2088884371316674561,'空与风',173,125,'/static/song_covers/auto-31bf9331.jpg','/static/songs/戏文说_叫宝宝&祥嘞嘞.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'8b9c388fab2e7efea129890a701925dd'),(2088899225368825857,'我也想成为穿越旷野的风','Hea2t','空与风的收藏',2380710,2088884371316674561,'空与风',144,117,'/static/song_covers/auto-0878d866.jpg','/static/songs/我也想成为穿越旷野的风_Hea2t.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'1d940c115f59de2c1e71794168210a5a'),(2088899225431740418,'我和你(Ai重置版)','神奇啊呦','空与风的收藏',3028343,2088884371316674561,'空与风',185,121,'/static/song_covers/auto-c5489087.jpg','/static/songs/我和你(Ai重置版)_神奇啊呦.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'deb62641fb78a3180a8e731854094bef'),(2088899225494654977,'我好像在哪见过你','薛之谦','初学者',4412838,2088884371316674561,'空与风',281,123,'/static/song_covers/auto-c9891d13.jpg','/static/songs/我好像在哪见过你_薛之谦.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'bc90aef95ae37b1dca42adfcfdf7664c'),(2088899225561763842,'我将在何处游荡','AniFace','空与风的收藏',3086055,2088884371316674561,'空与风',184,126,'/static/song_covers/auto-b3adc014.jpg','/static/songs/我将在何处游荡_AniFace.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'5c25344782edeee516c01fb941a1fc46'),(2088899225624678401,'我爱你','Ayasa','空与风的收藏',3652153,2088884371316674561,'空与风',229,124,'/static/song_covers/auto-eabf6e66.jpg','/static/songs/我爱你_Ayasa.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'f574b3a06f4ca6aba401d9b0e4142ec7'),(2088899225754701826,'所念皆星河','CMJ','空与风的收藏',3933773,2088884371316674561,'空与风',225,132,'/static/song_covers/auto-ea4dba26.jpg','/static/songs/所念皆星河_CMJ.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'aaceb0ca14ae3469ff2bb3c76501c2cf'),(2088899225842782210,'所爱如月色','CMJ','空与风的收藏',3104646,2088884371316674561,'空与风',165,112,'/static/song_covers/auto-c4899d9c.jpg','/static/songs/所爱如月色_CMJ.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'2386fb1d7c3a47e4cc0c2730c2062ac1'),(2088899225888919554,'执迷不悟','小乐哥','空与风的收藏',2118286,2088884371316674561,'空与风',210,76,'/static/song_covers/auto-35353271.jpg','/static/songs/执迷不悟_小乐哥.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'d6586ed5ce2eff329d5f3d4a349850a1'),(2088899226018942977,'故梦','橙翼','空与风的收藏',4700362,2088884371316674561,'空与风',284,121,'/static/song_covers/auto-18b3c8d4.jpg','/static/songs/故梦_橙翼.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'378abd0d3027132f518e695954bbe73c'),(2088899226081857537,'斗战胜佛','老虎欧巴','空与风的收藏',4692246,2088884371316674561,'空与风',285,129,'/static/song_covers/auto-aba15164.jpg','/static/songs/斗战胜佛_老虎欧巴.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'f775dfb56a719ba096d145fc19bf04e2'),(2088899226144772097,'日暮里','JINBAO','空与风的收藏',2455812,2088884371316674561,'空与风',208,91,'/static/song_covers/auto-6165b01b.jpg','/static/songs/日暮里_JINBAO.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'52805ea4d8132e938d40e081f8ecb572'),(2088899226211880961,'时落叶影','Maple暖枫','空与风的收藏',3416416,2088884371316674561,'空与风',204,105,'/static/song_covers/auto-621caf3e.jpg','/static/songs/时落叶影_Maple暖枫.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'fe8b2a416ae33d3b070d8b5d75ed9ee2'),(2088899226337710081,'时落鲸海','MoFeansy','空与风的收藏',3845785,2088884371316674561,'空与风',183,113,'/static/song_covers/auto-e3016a82.jpg','/static/songs/时落鲸海_MoFeansy.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'09c1c38ad0fac774672bdfb83d389dbb'),(2088899226409013250,'星と僕らと','目黒将司','PERSONA DANCING P3D & P5D SOUND TRACKS -ADVANCED CD COLLECTOR\'S BOX-',6700369,2088884371316674561,'空与风',429,123,'/static/song_covers/auto-3975e47c.jpg','/static/songs/星と僕らと (星星与我们)_p5r.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'7ac32f5c3b8f05fd2ef6dc40f53fcbd6'),(2088899226471927810,'星之诗','AnRaain安林','空与风的收藏',3500774,2088884371316674561,'空与风',202,130,'/static/song_covers/auto-6e0a701a.jpg','/static/songs/星之诗_AnRaain安林.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'bda348924a7bb5c1a798f5989609aaf7'),(2088899226601951234,'星夜之下','MoreanP','空与风的收藏',3972106,2088884371316674561,'空与风',222,137,'/static/song_covers/auto-eab6e554.jpg','/static/songs/星夜之下_MoreanP.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'44af90617e8b5a5716333229f19b679e'),(2088899226681643010,'星空彼岸','逆时针向⧸MoreanP','空与风的收藏',4777918,2088884371316674561,'空与风',237,127,'/static/song_covers/auto-f0aba7e5.jpg','/static/songs/星空彼岸_逆时针向⧸MoreanP.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'37f3c8f4a055c1af46289748a095a2b4'),(2088899226736168962,'星茶会','灰澈','空与风的收藏',3378476,2088884371316674561,'空与风',197,125,'/static/song_covers/auto-a096a9d9.jpg','/static/songs/星茶会_灰澈.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'3260bb32849e318079d5166a6348af53'),(2088899226929106946,'星萤火','灰澈','空与风的收藏',8280490,2088884371316674561,'空与风',210,119,'/static/song_covers/auto-8b2c11d3.jpg','/static/songs/星萤火_灰澈.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'03968231b6477df5fe43f343570d6bcd'),(2088899227000410114,'春野','夏小调','空与风的收藏',3742093,2088884371316674561,'空与风',254,116,'/static/song_covers/auto-9ba93037.jpg','/static/songs/春野_夏小调.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'4e2aafc92aab20d78343218e30ba6e23'),(2088899227126239233,'春风从不入眠','Hea2t','空与风的收藏',4475063,2088884371316674561,'空与风',168,129,'/static/song_covers/auto-1b6ec445.jpg','/static/songs/春风从不入眠_Hea2t.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'3c7c11b8c7bfa76477bb58b21b70567c'),(2088899227256262658,'晚夜微雨问海棠','镜予歌、陈亦洺、喧笑','空与风的收藏',5517168,2088884371316674561,'空与风',287,133,'/static/song_covers/auto-a8830b26.jpg','/static/songs/晚夜微雨问海棠_镜予歌、陈亦洺、喧笑.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'3a5c65d0b17e3117174d96203fa5e553'),(2088899227319177217,'晚星','逆时针向','空与风的收藏',3786113,2088884371316674561,'空与风',194,132,'/static/song_covers/auto-1af42c8a.jpg','/static/songs/晚星_逆时针向.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'b58129d62e78f291ceeb6852eca48c0b'),(2088899227382091777,'晨与暮的约定','Melody_Fall','空与风的收藏',3311482,2088884371316674561,'空与风',207,123,'/static/song_covers/auto-66eae0c7.jpg','/static/songs/晨与暮的约定_Melody_Fall.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'22f3eb4797f09fe9c0df03f3ad70de25'),(2088899227449200642,'暮秋沉眠','MoreanP','空与风的收藏',3289468,2088884371316674561,'空与风',194,111,'/static/song_covers/auto-663b156e.jpg','/static/songs/暮秋沉眠_MoreanP.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'d73469cb9fc1c10b0c3a8e596f125545'),(2088899227516309505,'曾闻花名','路灰气球','空与风的收藏',3473537,2088884371316674561,'空与风',177,119,'/static/song_covers/auto-546896d4.jpg','/static/songs/曾闻花名_路灰气球.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'b3a5b4ccb40f2fc22cb964c5a53eb3b5'),(2088899227646332929,'最初','逆时针向','空与风的收藏',4825602,2088884371316674561,'空与风',313,121,'/static/song_covers/auto-5df28b6f.jpg','/static/songs/最初_逆时针向.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'a8e61568b5b89f5490e2264911e6fa14'),(2088899227713441793,'【8bit音乐改编】宝可梦-未白镇 (8-bit ver.)/Pokémon-Littleroot Town (8-bit ver.) FC红白机风格BGM','魂狼音乐','',4201567,2088884371316674561,'空与风',135,239,'/static/song_covers/auto-5131e119.jpg','/static/songs/未白镇_宝可梦.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'c11e2f3d734cfdee82b73ec4d6bbcddf'),(2088899227780550658,'未闻花名(钢琴曲纯音乐)','xxx','空与风的收藏',1856270,2088884371316674561,'空与风',115,94,'/static/song_covers/auto-15f092ec.jpg','/static/songs/未闻花名(钢琴曲纯音乐)_xxx.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'872bbd14241db8bf3f0b6a10b179a4d6'),(2088899227843465217,'松烟入墨','winky诗','空与风的收藏',4643408,2088884371316674561,'空与风',278,122,'/static/song_covers/auto-1c33b637.jpg','/static/songs/松烟入墨_winky诗.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'1801ac2377dc6401447f09c7c3e94b4f'),(2088899227918962690,'梦回长安','蓝云木','空与风的收藏',4365451,2088884371316674561,'空与风',234,132,'/static/song_covers/auto-928c4631.jpg','/static/songs/梦回长安_蓝云木.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'8f95cc18d4087f2dd0420fa3c712bb8d'),(2088899228040597505,'梦羽随风','Xwirok','空与风的收藏',4217231,2088884371316674561,'空与风',247,133,'/static/song_covers/auto-b63ff399.jpg','/static/songs/梦羽随风_Xwirok.mp3','2026-08-16 16:02:40','mp3',NULL,NULL,'3f751d508f16195fe92b369722ce8c7b'),(2088899228103512065,'森林','灰澈','空与风的收藏',3625551,2088884371316674561,'空与风',188,127,'/static/song_covers/auto-900e330f.jpg','/static/songs/森林_灰澈.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'b1983eddc46c888b6b13000c02e30ad2'),(2088899228166426625,'森林自习室','森水垚&沐灵仙','空与风的收藏',3617538,2088884371316674561,'空与风',174,110,'/static/song_covers/auto-6d20bd6a.jpg','/static/songs/森林自习室_森水垚&沐灵仙.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'7b251cf44b288c08a91c282690de4233'),(2088899228233535490,'欧布奥特曼口琴曲','未知作家','空与风的收藏',3728841,2088884371316674561,'空与风',197,139,'/static/song_covers/auto-236e0882.jpg','/static/songs/欧布奥特曼口琴曲_未知作家.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'3e4a24628379b7400bb5b256f8eb4dd7'),(2088899228359364610,'汐月','赵大鼾','空与风的收藏',3816254,2088884371316674561,'空与风',188,123,'/static/song_covers/auto-f2105db3.jpg','/static/songs/汐月_赵大鼾.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'3373ffd1dcc9d4cd4127e5891df150cb'),(2088899228422279169,'汐渐','Mofeansy','空与风的收藏',3443954,2088884371316674561,'空与风',241,108,'/static/song_covers/auto-1682a621.jpg','/static/songs/汐渐_Mofeansy.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'ce97b0756bc10fb13134bf957e3f813e'),(2088899228485193730,'江上清风游','变奏的梦想','空与风的收藏',3757227,2088884371316674561,'空与风',276,108,'/static/song_covers/auto-33abe2e8.jpg','/static/songs/江上清风游_变奏的梦想.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'c430f89fb966994391e943d43d035e89'),(2088899228552302593,'江辞白帝','Mofeansy','空与风的收藏',3050252,2088884371316674561,'空与风',183,118,'/static/song_covers/auto-d8e8b8bc.jpg','/static/songs/江辞白帝_Mofeansy.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'0afb86f84c7db1b8037b2156fc493fb2'),(2088899228615217154,'活着就是一种伟大','_Maple暖枫','空与风的收藏',3279801,2088884371316674561,'空与风',204,123,'/static/song_covers/auto-040e107b.jpg','/static/songs/活着就是一种伟大__Maple暖枫.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'a236f6caafe9dd1db42956137b6db5bd'),(2088899228682326018,'流星之夜','MoFeansy','空与风的收藏',4045460,2088884371316674561,'空与风',272,106,'/static/song_covers/auto-389ac883.jpg','/static/songs/流星之夜_MoFeansy.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'918feddf2c5c4f9337e67a228813cac4'),(2088899228745240577,'浮光 (The History)','Jannik','空与风的收藏',4082716,2088884371316674561,'空与风',258,120,'/static/song_covers/auto-14750041.jpg','/static/songs/浮光 (The History)_Jannik.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'1417046880f32a2bc4061ad30ad0854b'),(2088899228808155137,'浮生若梦','逆时针向','空与风的收藏',2927065,2088884371316674561,'空与风',200,114,'/static/song_covers/auto-3e64051a.jpg','/static/songs/浮生若梦_逆时针向 .mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'bc6cb76ed3952872ed8ce85f40b8eaa1'),(2088899228879458305,'海の形','昙轩','空与风的收藏',3570912,2088884371316674561,'空与风',251,109,'/static/song_covers/auto-872e247a.jpg','/static/songs/海の形_昙轩.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'d867bf5dffd7c15beb9f7f1752343886'),(2088899229009481730,'海风，海风请带我走','逆时针向','空与风的收藏',4520435,2088884371316674561,'空与风',257,131,'/static/song_covers/auto-8ddaa4d3.jpg','/static/songs/海风，海风请带我走_逆时针向.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'d27edf583fdb9fe0ca126578988c0545'),(2088899229072396289,'游京','海伦','空与风的收藏',3591395,2088884371316674561,'空与风',226,118,'/static/song_covers/auto-64f38563.jpg','/static/songs/游京_海伦.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'2d845df8b5d09af49dbb0c7082959572'),(2088899229139505154,'溯(治愈)','未知作家','空与风的收藏',3025829,2088884371316674561,'空与风',237,97,'/static/song_covers/auto-71903a85.jpg','/static/songs/溯(治愈)_未知作家.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'84f3c5c3f8353c2742549bebdd9d8e86'),(2088899229202419713,'溯(致郁)','未知作家','空与风的收藏',1963225,2088884371316674561,'空与风',121,124,'/static/song_covers/auto-da4688f2.jpg','/static/songs/溯(致郁)_未知作家.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'f6e087242b0ce3db4143083942d9097c'),(2088899229269528578,'烟火人间','添儿呗一','空与风的收藏',4312490,2088884371316674561,'空与风',265,122,'/static/song_covers/auto-bcd2bbcd.jpg','/static/songs/烟火人间_添儿呗一.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'2c14ca52e20412b29475cbef61c47bf9'),(2088899229328248834,'烟火焚(inst.)','逆时针向','空与风的收藏',4901488,2088884371316674561,'空与风',285,127,'/static/song_covers/auto-586fa7a8.jpg','/static/songs/烟火焚(inst.)_逆时针向.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'932189d1230b1f53b5918b4f886b84fc'),(2088899229458272258,'烟雨行舟','许诗茵','烟雨行舟',3989891,2088884371316674561,'空与风',268,117,'/static/song_covers/auto-74d218b5.jpg','/static/songs/烟雨行舟_司南.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'b5686746b25525708ea694f60f5da02a'),(2088899229521186818,'燕无歇','蒋雪儿Snow.J','燕无歇',3498686,2088884371316674561,'空与风',201,136,'/static/song_covers/auto-bf18791c.jpg','/static/songs/燕无歇_蒋雪儿.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'2b3c4e2bbfdbbe18e4081b85b2f58d40'),(2088899229592489985,'璀星湖','Maple暖枫','空与风的收藏',3565490,2088884371316674561,'空与风',238,115,'/static/song_covers/auto-cc10de3b.jpg','/static/songs/璀星湖_Maple暖枫.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'9bfd0ffa1c1b10ffe42b791f8febe9bb'),(2088899229655404545,'いのちの名前／「千と千尋の神隠し」より','広橋真紀子','リラクシング・ピアノ～宮崎駿コレクション',4466513,2088884371316674561,'空与风',347,101,'/static/song_covers/auto-f1ff9ced.jpg','/static/songs/生命之名_久石让.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'1ebc1433a33c0f2442a04b2a24c97ef5'),(2088899229718319106,'白羊','徐秉龙 & 沈以诚','白羊',2620541,2088884371316674561,'空与风',169,115,'/static/song_covers/auto-2a959ccc.jpg','/static/songs/白羊_徐秉龙&沈以诚.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'566c9c36be8569fff9b25ab322e12622'),(2088899229785427969,'白露','羽肿','空与风的收藏',4728099,2088884371316674561,'空与风',297,123,'/static/song_covers/auto-68803cc3.jpg','/static/songs/白露_羽肿.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'073712d783b570fdcfc037156e0a4b99'),(2088899229852536833,'盛夏','Alisa','空与风的收藏',3277128,2088884371316674561,'空与风',187,129,'/static/song_covers/auto-0b9aa493.jpg','/static/songs/盛夏_Alisa.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'a0bbf0e72d297ce4d83ad89d6b91e086'),(2088899229915451394,'盛夏与蝉鸣','hea2t','空与风的收藏',2524591,2088884371316674561,'空与风',171,111,'/static/song_covers/auto-2d7c4557.jpg','/static/songs/盛夏与蝉鸣_hea2t.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'c6981287be01b6baad6794d23a77984d'),(2088899229982560258,'瞬间的永恒','赵海洋','空与风的收藏',4467567,2088884371316674561,'空与风',309,111,'/static/song_covers/auto-4c80b191.jpg','/static/songs/瞬间的永恒_赵海洋.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'6eb0d3b34273d937e03ff280d9dbf37d'),(2088899230108389377,'知我','国风堂&哦漏','空与风的收藏',5644807,2088884371316674561,'空与风',281,133,'/static/song_covers/auto-7de40d06.jpg','/static/songs/知我_国风堂&哦漏.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'e1acaf51bd056cfa93ac0752d2429e76'),(2088899230238412801,'离开我的依赖','王艳薇','空与风的收藏',3913424,2088884371316674561,'空与风',234,127,'/static/song_covers/auto-64fcdd29.jpg','/static/songs/离开我的依赖_王艳薇.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'fa7d438c80d35400c5ec28724e70d527'),(2088899230301327362,'稻香','周杰倫','魔杰座',3651771,2088884371316674561,'空与风',224,127,'/static/song_covers/auto-7f35e3ee.jpg','/static/songs/稻香_周杰伦.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'9bfdc32cd0ced82e655e72bf003e47d9'),(2088899230364241922,'窗外的雨，屋内的你','Xwdit','空与风的收藏',2927661,2088884371316674561,'空与风',182,125,'/static/song_covers/auto-b16f8436.jpg','/static/songs/窗外的雨，屋内的你_Xwdit.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'ea5bbbba2f236e603c770d4c37acd105'),(2088899230431350785,'童話鎮','陳一發兒','童話鎮',4089695,2088884371316674561,'空与风',258,124,'/static/song_covers/auto-966f1965.jpg','/static/songs/童话镇_陈一发儿.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'a5e86d7abc3c9987eaad10a42fe6fa52'),(2088899230494265346,'等待','黄龄','空与风的收藏',4540196,2088884371316674561,'空与风',240,129,'/static/song_covers/auto-83ecf8a7.jpg','/static/songs/等待_黄龄.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'d4218d8379e4cd0a3ef887c3f4a8e166'),(2088899230624288770,'致那时候的你','當山みれい','空与风的收藏',5960452,2088884371316674561,'空与风',349,128,'/static/song_covers/auto-c193d481.jpg','/static/songs/致那时候的你_當山みれい.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'2aadbf368c1c74632911508ce3470201'),(2088899230687203329,'花','羽肿','空与风的收藏',3332762,2088884371316674561,'空与风',320,70,'/static/song_covers/auto-ad83164c.jpg','/static/songs/花_羽肿.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'06cbb5026bf93a026753ac6625d0252b'),(2088899230754312194,'花火が瞬く夜に','羽肿','花火が瞬く夜に',3665593,2088884371316674561,'空与风',272,106,'/static/song_covers/auto-7aa9965d.jpg','/static/songs/花火が瞬く夜に_羽肿.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'b0bf6ad729b522fd9b9e6fecf9020219'),(2088899230817226753,'荒川之月','忘乡','空与风的收藏',4027238,2088884371316674561,'空与风',216,109,'/static/song_covers/auto-ee469658.jpg','/static/songs/荒川之月_忘乡.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'53955e498847ed260bd7f42172b75c2c'),(2088899230880141313,'荒野を往く者','目黒将司','Metaphor: ReFantazio Special Soundtrack',3195464,2088884371316674561,'空与风',171,145,'/static/song_covers/auto-f42c23c8.jpg','/static/songs/荒野を往く者_本良敬典.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'295643131a07c4b656c9f38639485c7c'),(2088899230947250177,'萤火之森','CMJ','空与风的收藏',2645535,2088884371316674561,'空与风',153,125,'/static/song_covers/auto-5e5a5b47.jpg','/static/songs/萤火之森_CMJ.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'5a19a8d0a5cd5a0ea902f3f2a2b22041'),(2088899231014359041,'葬花','蓝云木&THT','空与风的收藏',3316694,2088884371316674561,'空与风',182,130,'/static/song_covers/auto-f067fbcd.jpg','/static/songs/葬花_蓝云木&THT.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'615592ccebfd83114cb7c52760a4359b'),(2088899231073079297,'西楼别序','尹昔眠','[standalone recordings]',4335984,2088884371316674561,'空与风',228,133,'/static/song_covers/auto-0d194ddb.jpg','/static/songs/西楼别序_尹昔眠-小田音乐社.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'42eff1c29a11b7035df405f01fecfa9f'),(2088899231207297026,'记住你','July','空与风的收藏',4645572,2088884371316674561,'空与风',237,129,'/static/song_covers/auto-6d53c279.jpg','/static/songs/记住你_July.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'3037132746d732d65e5fb3291daab684'),(2088899231270211585,'记忆停留的地方','July','空与风的收藏',3303284,2088884371316674561,'空与风',202,126,'/static/song_covers/auto-a6f5d110.jpg','/static/songs/记忆停留的地方_July.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'870fc82cd647ea0d5e31b160cc9a23c8'),(2088899231333126146,'账号已注销(小提琴版)','Strictlyviolin荀博&王朝','空与风的收藏',2480358,2088884371316674561,'空与风',130,125,'/static/song_covers/auto-4814cd40.jpg','/static/songs/账号已注销(小提琴版)_Strictlyviolin荀博&王朝 .mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'dad79a0bac96f5f96a2a1fb6ef83f546'),(2088899231400235010,'路雨','壹勺籽糖','空与风的收藏',2426789,2088884371316674561,'空与风',132,121,'/static/song_covers/auto-dc7bb72b.jpg','/static/songs/路雨_壹勺籽糖.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'13d396f4c945119e9c2ed5e6ed97881c'),(2088899231593172993,'远书','忘乡','空与风的收藏',4315197,2088884371316674561,'空与风',215,98,'/static/song_covers/auto-a565ecfe.jpg','/static/songs/远书_忘乡.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'fd0f222164ff97489fecd366b7ea514e'),(2088899231656087554,'银之树逃亡神级bgm','ori','空与风的收藏',2561107,2088884371316674561,'空与风',136,135,'/static/song_covers/auto-a6cf052a.jpg','/static/songs/银之树逃亡神级bgm_ori.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'7c5e47afc46e5fe5bb5db25a10ee1f52'),(2088899231727390722,'闪耀','南征北战','空与风的收藏',4032339,2088884371316674561,'空与风',263,120,'/static/song_covers/auto-121a21b5.jpg','/static/songs/闪耀_南征北战.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'11527e8f9ce7ea2a4557ca9bfb9c0be6'),(2088899231861608449,'隔岸','姚六一','空与风的收藏',4446701,2088884371316674561,'空与风',273,124,'/static/song_covers/auto-8d57cf93.jpg','/static/songs/隔岸_姚六一.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'a4c5af782ede3820bc57872f972f9067'),(2088899231928717313,'雨夜','四季音色','空与风的收藏',3018592,2088884371316674561,'空与风',179,125,'/static/song_covers/auto-43cc7ed1.jpg','/static/songs/雨夜_四季音色 .mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'7c1192ea6229c4471f45a796375bc477'),(2088899231928717314,'雨眠','Hea2t','空与风的收藏',2090344,2088884371316674561,'空与风',132,126,'/static/song_covers/auto-14a7dcaf.jpg','/static/songs/雨眠_Hea2t.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'dc42243b831036a5987215d7d5c55279'),(2088899232054546434,'雨葬花海','Maple暖枫','空与风的收藏',4011242,2088884371316674561,'空与风',219,92,'/static/song_covers/auto-0325de75.jpg','/static/songs/雨葬花海_Maple暖枫.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'7e3c1f880b0b11f38725675665a2d970'),(2088899232134238210,'青丝','等什么君','空与风的收藏',4719176,2088884371316674561,'空与风',271,127,'/static/song_covers/auto-407fa0e0.jpg','/static/songs/青丝_等什么君.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'cce637a197a2ccd5d2d887b9c0aee848'),(2088899232188764162,'青空','Candy_Wind','空与风的收藏',3295017,2088884371316674561,'空与风',201,122,'/static/song_covers/auto-ff80f8fa.jpg','/static/songs/青空_Candy_Wind.mp3','2026-08-16 16:02:41','mp3',NULL,NULL,'b1df45185c5c8b398b6979012f7fdea6'),(2088899232255873026,'青衣','九鸢&在下河帛','空与风的收藏',2256045,2088884371316674561,'空与风',148,119,'/static/song_covers/auto-773a0d8b.jpg','/static/songs/青衣_九鸢&在下河帛.mp3','2026-08-16 16:02:42','mp3',NULL,NULL,'0dcc2de698e8772d725fef0d1ec97ba9'),(2088899232318787586,'风与铃·夜与星','邹牧虞','空与风的收藏',2885707,2088884371316674561,'空与风',179,118,'/static/song_covers/auto-5f4cdd5f.jpg','/static/songs/风与铃·夜与星_邹牧虞.mp3','2026-08-16 16:02:42','mp3',NULL,NULL,'72edcfc7a37e29b6f0be4e64a84891ba'),(2088899232385896449,'风摇盛夏','逆时针向','空与风的收藏',2599745,2088884371316674561,'空与风',152,126,'/static/song_covers/auto-578b001c.jpg','/static/songs/风摇盛夏_逆时针向.mp3','2026-08-16 16:02:42','mp3',NULL,NULL,'ac61321db79c1b593dfecadb21887fd2'),(2088899232448811010,'风月明日来','鹤见江野','空与风的收藏',2676277,2088884371316674561,'空与风',167,123,'/static/song_covers/auto-83926cce.jpg','/static/songs/风月明日来_鹤见江野.mp3','2026-08-16 16:02:42','mp3',NULL,NULL,'7f954286c9ffe435484a014919f44194'),(2088899232511725570,'飞升','聲無哀樂乐队','空与风的收藏',5379941,2088884371316674561,'空与风',295,138,'/static/song_covers/auto-c16aa438.jpg','/static/songs/飞升_聲無哀樂乐队.mp3','2026-08-16 16:02:42','mp3',NULL,NULL,'de5f261c16b675ed3f011c41af60929c'),(2088899232574640129,'鸟之诗','折戸伸治','AIR ORIGINAL SOUNDTRACK',1896349,2088884371316674561,'空与风',202,71,'/static/song_covers/auto-5646d069.jpg','/static/songs/鸟之诗 (八音盒版)_折户伸治.mp3','2026-08-16 16:02:42','mp3','','','97ce826cc8b6f0626eb0c0ed401bfd67'),(2088899232704663554,'鸿迹','变奏的梦想','空与风的收藏',4756018,2088884371316674561,'空与风',273,129,'/static/song_covers/auto-b369bd77.jpg','/static/songs/鸿迹_变奏的梦想.mp3','2026-08-16 16:02:42','mp3',NULL,NULL,'02d702a674fa556ebf1089b8c4a33a60'),(2088899232771772417,'이젠 남 (现在是陌生人) (Now, we are strangers｜Inst.)','Noblesse (노블레스)','空与风的收藏',4406991,2088884371316674561,'空与风',225,146,'/static/song_covers/auto-1e7e54ca.jpg','/static/songs/이젠 남 (现在是陌生人) (Now, we are strangers｜Inst.)_Noblesse (노블레스).mp3','2026-08-16 16:02:42','mp3',NULL,NULL,'30b5cc83e37d343a17af2abbdd5d06c8'),(2092869288631459841,'beyond the memory','July','test',4941873,2088884371316674561,'空与风',235,132,'/static/song_covers/cover-3c05dd33.jpg','/static/songs/July - beyond the memory_6ce5b000.mp3','2026-08-27 14:58:17','mp3','',NULL,'b8556914e1f717664a519f8e4982864f'),(2097191485743222786,'AIZO','咒术回战','Unknown Album',7175271,2088884371316674561,'空与风',216,262,'/static/song_covers/auto-3d025082.jpg','/static/songs/咒术回战 - AIZO_57832e53.mp3','2026-09-08 13:13:09','mp3',NULL,NULL,'6e50a3891e441251f83f07ce8359b2a1');
/*!40000 ALTER TABLE `songs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `songs_playlists_relation`
--

DROP TABLE IF EXISTS `songs_playlists_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `songs_playlists_relation` (
  `playlist_id` bigint NOT NULL,
  `song_id` bigint NOT NULL,
  `song_playlist_position` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`playlist_id`,`song_id`),
  UNIQUE KEY `uq_playlist_song_position` (`playlist_id`,`song_playlist_position`),
  KEY `idx_song_id` (`song_id`),
  KEY `idx_playlist_position` (`playlist_id`,`song_playlist_position`),
  CONSTRAINT `fk_playlist_songs` FOREIGN KEY (`playlist_id`) REFERENCES `playlists` (`playlist_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_playlist_songs_song` FOREIGN KEY (`song_id`) REFERENCES `songs` (`song_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `songs_playlists_relation`
--

LOCK TABLES `songs_playlists_relation` WRITE;
/*!40000 ALTER TABLE `songs_playlists_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `songs_playlists_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `songs_tags_relation`
--

DROP TABLE IF EXISTS `songs_tags_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `songs_tags_relation` (
  `song_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`song_id`,`tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_songs_tags_song` FOREIGN KEY (`song_id`) REFERENCES `songs` (`song_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_songs_tags_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`tag_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `songs_tags_relation`
--

LOCK TABLES `songs_tags_relation` WRITE;
/*!40000 ALTER TABLE `songs_tags_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `songs_tags_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tags` (
  `tag_id` bigint NOT NULL COMMENT '标签ID(雪花)',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `tag_type` enum('genre','language','mood','scene','era') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流派/语种/心情/场景/年代',
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `idx_unique_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tags`
--

LOCK TABLES `tags` WRITE;
/*!40000 ALTER TABLE `tags` DISABLE KEYS */;
/*!40000 ALTER TABLE `tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL COMMENT '用户ID(雪花)',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `idx_unique_username` (`user_name`),
  UNIQUE KEY `idx_unique_email` (`user_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (2088884371316674561,'空与风','2034315213@qq.com','$2a$10$Ua0.mtPnoYCTPnSGPjXSu.pR2HQezNYIoymLh8e1E2BJqhqflXeyu',NULL),(2091459736899874818,'123','123@123.cafas15','$2a$10$Q9WD5t13xZJUm8aO/qnmTOUrnorXQnyo.74LH.ijbQlQocy9OUNKa',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users_likeplaylists_relation`
--

DROP TABLE IF EXISTS `users_likeplaylists_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_likeplaylists_relation` (
  `user_id` bigint NOT NULL,
  `playlist_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`playlist_id`),
  KEY `idx_playlist_id` (`playlist_id`),
  CONSTRAINT `fk_user_likes_playlist` FOREIGN KEY (`playlist_id`) REFERENCES `playlists` (`playlist_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_likes_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_likeplaylists_relation`
--

LOCK TABLES `users_likeplaylists_relation` WRITE;
/*!40000 ALTER TABLE `users_likeplaylists_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `users_likeplaylists_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users_playlists_relation`
--

DROP TABLE IF EXISTS `users_playlists_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_playlists_relation` (
  `user_id` bigint NOT NULL,
  `playlist_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`playlist_id`),
  KEY `fk_user_playlists_playlist` (`playlist_id`),
  CONSTRAINT `fk_user_playlists_playlist` FOREIGN KEY (`playlist_id`) REFERENCES `playlists` (`playlist_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_playlists_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_playlists_relation`
--

LOCK TABLES `users_playlists_relation` WRITE;
/*!40000 ALTER TABLE `users_playlists_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `users_playlists_relation` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-24  8:09:10
