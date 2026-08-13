-- ============================================================
-- musicPlayer 数据库初始化脚本（纯建库建表，不含数据）
--
-- 主键策略：雪花 BIGINT ID，由应用层（MyBatis-Plus IdType.ASSIGN_ID）生成，
--           数据库不设置 AUTO_INCREMENT。
-- 命名约定：表名小写下划线；主键一律 `<实体>_id`；时间戳 `*_date`；
--           索引 `idx_*`；外键 `fk_<父表>_<子表>`。
-- ============================================================

CREATE DATABASE IF NOT EXISTS musicPlayer DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE musicPlayer;

-- ------------------------------------------------------------------
-- 1. 用户 users
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id`        bigint       NOT NULL COMMENT '用户ID(雪花)',
  `user_name`      varchar(50)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_email`     varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password`       varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_cover_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `idx_unique_username` (`user_name`),
  UNIQUE KEY `idx_unique_email` (`user_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------
-- 2. 歌曲 songs
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `songs`;
CREATE TABLE `songs` (
  `song_id`        bigint       NOT NULL COMMENT '歌曲ID(雪花)',
  `song_title`     varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `artist`         varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '未知艺术家',
  `album`          varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '未知专辑',
  `file_size`      varchar(50)  COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `uploader_id`    bigint       NOT NULL,
  `uploader_name`  varchar(50)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `duration`       int          NOT NULL DEFAULT '0',
  `bitrate`        int          NOT NULL DEFAULT '0',
  `song_cover_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `song_url`       varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `date_added`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `file_format`    varchar(20)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `lyrics`         mediumtext   COLLATE utf8mb4_unicode_ci,
  `t_lyrics`       text         COLLATE utf8mb4_unicode_ci COMMENT '翻译歌词(LRC格式)',
  `file_md5`       varchar(32)  COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件内容指纹',
  PRIMARY KEY (`song_id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_file_md5` (`file_md5`),
  FULLTEXT KEY `idx_fulltext_title` (`song_title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------
-- 3. 歌单 playlists
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `playlists`;
CREATE TABLE `playlists` (
  `playlist_id`        bigint       NOT NULL COMMENT '歌单ID(雪花)',
  `playlist_name`      varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `creator_id`         bigint       NOT NULL,
  `created_date`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_date`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `playlist_cover_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `song_count`         int          NOT NULL DEFAULT '0',
  `like_count`         int          NOT NULL DEFAULT '0',
  `play_count`         int          NOT NULL DEFAULT '0',
  `is_public`          tinyint(1)   NOT NULL DEFAULT '1',
  `description`        text         COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`playlist_id`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------
-- 4. 标签字典 tags
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `tag_id`       bigint       NOT NULL COMMENT '标签ID(雪花)',
  `tag_name`     varchar(50)  COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `tag_type`     enum('genre','language','mood','scene','era') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流派/语种/心情/场景/年代',
  `created_date` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `idx_unique_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------
-- 5. 播放状态 play_state
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `play_state`;
CREATE TABLE `play_state` (
  `user_id`          bigint      NOT NULL COMMENT '用户ID(雪花)',
  `current_queue_id` bigint      DEFAULT NULL,
  `current_song_id`  bigint      DEFAULT NULL,
  `current_position` int         NOT NULL DEFAULT '0',
  `current_progress` int         NOT NULL DEFAULT '0',
  `playmode`         varchar(20) DEFAULT 'sequence',
  `updated_date`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  KEY `idx_current_queue` (`current_queue_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------------
-- 6. 播放队列 queues
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `queues`;
CREATE TABLE `queues` (
  `queue_id`     bigint       NOT NULL COMMENT '队列ID(雪花)',
  `queue_name`   varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `creator_id`   bigint       NOT NULL,
  `song_count`   int          NOT NULL DEFAULT '0',
  `is_current`   tinyint(1)   NOT NULL DEFAULT '0',
  `created_date` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_date` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`queue_id`),
  KEY `idx_creator_date` (`creator_id`,`updated_date` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------
-- 7. 队列项 queue_items
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `queue_items`;
CREATE TABLE `queue_items` (
  `queue_item_id`      bigint   NOT NULL COMMENT '队列项ID(雪花)',
  `queue_id`           bigint   NOT NULL,
  `song_id`            bigint   NOT NULL,
  `queue_item_position` int     NOT NULL DEFAULT '0',
  `added_date`         datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`queue_item_id`),
  KEY `idx_queue_song` (`queue_id`,`song_id`),
  KEY `idx_queue_position` (`queue_id`,`queue_item_position`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------------
-- 8. 歌单-歌曲关联 songs_playlists_relation
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `songs_playlists_relation`;
CREATE TABLE `songs_playlists_relation` (
  `playlist_id`           bigint NOT NULL,
  `song_id`               bigint NOT NULL,
  `song_playlist_position` int    NOT NULL DEFAULT '0',
  PRIMARY KEY (`playlist_id`,`song_id`),
  KEY `idx_song_id` (`song_id`),
  KEY `idx_playlist_position` (`playlist_id`,`song_playlist_position`),
  CONSTRAINT `fk_playlist_songs` FOREIGN KEY (`playlist_id`) REFERENCES `playlists` (`playlist_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------------
-- 9. 用户-收藏歌单关联 users_likeplaylists_relation
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `users_likeplaylists_relation`;
CREATE TABLE `users_likeplaylists_relation` (
  `user_id`     bigint NOT NULL,
  `playlist_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`playlist_id`),
  KEY `idx_playlist_id` (`playlist_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------------
-- 10. 用户-歌单关联 users_playlists_relation
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `users_playlists_relation`;
CREATE TABLE `users_playlists_relation` (
  `user_id`     bigint NOT NULL,
  `playlist_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`playlist_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------------
-- 11. 歌曲-标签关联 songs_tags_relation（多对多）
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `songs_tags_relation`;
CREATE TABLE `songs_tags_relation` (
  `song_id` bigint NOT NULL,
  `tag_id`  bigint NOT NULL,
  PRIMARY KEY (`song_id`,`tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_songs_tags_song` FOREIGN KEY (`song_id`) REFERENCES `songs` (`song_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_songs_tags_tag`  FOREIGN KEY (`tag_id`)  REFERENCES `tags`  (`tag_id`)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------
-- 12. 评论 comments（支持回复）
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
  `comment_id`   bigint NOT NULL COMMENT '评论ID(雪花)',
  `user_id`      bigint NOT NULL COMMENT '评论用户ID',
  `target_type`  enum('song','playlist') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论目标类型',
  `target_id`    bigint NOT NULL COMMENT '评论目标ID(歌曲/歌单, 多态无外键)',
  `parent_id`    bigint DEFAULT NULL COMMENT '父评论ID(支持回复)',
  `content`      text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  KEY `idx_target` (`target_type`,`target_id`,`created_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_comments_user`   FOREIGN KEY (`user_id`)    REFERENCES `users` (`user_id`)    ON DELETE CASCADE,
  CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`comment_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------
-- 13. 播放历史 play_history
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `play_history`;
CREATE TABLE `play_history` (
  `history_id`  bigint   NOT NULL COMMENT '播放历史ID(雪花)',
  `user_id`     bigint   NOT NULL,
  `song_id`     bigint   NOT NULL,
  `played_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`history_id`),
  KEY `idx_user_played` (`user_id`,`played_date`),
  KEY `idx_song_id` (`song_id`),
  CONSTRAINT `fk_play_history_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_play_history_song` FOREIGN KEY (`song_id`) REFERENCES `songs` (`song_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------
-- 14. 专辑 albums
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `albums`;
CREATE TABLE `albums` (
  `album_id`     bigint       NOT NULL COMMENT '专辑ID(雪花)',
  `album_name`   varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '专辑名称',
  `artist_name`  varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '艺术家名(未建 artists 表)',
  `cover_url`    varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专辑封面',
  `release_date` date         DEFAULT NULL COMMENT '发行日期',
  `description`  text         COLLATE utf8mb4_unicode_ci,
  `created_date` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`album_id`),
  KEY `idx_album_name` (`album_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
