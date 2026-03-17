CREATE TABLE IF NOT EXISTS `users` (
  `user_id` INT NOT NULL AUTO_INCREMENT,
  `user_name` VARCHAR(50) NOT NULL,
  `user_email` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `idx_unique_username` (`user_name`), 
  UNIQUE KEY `idx_unique_email` (`user_email`)    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `songs` (
  `song_id` INT NOT NULL AUTO_INCREMENT,
  `song_title` VARCHAR(255) NOT NULL,
  `artist` VARCHAR(255) DEFAULT '未知艺术家',
  `album` VARCHAR(255) DEFAULT '未知专辑',
  `file_size` BIGINT NOT NULL DEFAULT 0,
  `uploader_id` INT NOT NULL,
  `uploader_name` VARCHAR(50) NOT NULL,
  `duration` INT NOT NULL DEFAULT 0,
  `bitrate` INT NOT NULL DEFAULT 0,
  `song_cover_url` VARCHAR(500) NOT NULL,
  `song_url` VARCHAR(500) NOT NULL,
  `date_added` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `file_format` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`song_id`),
  KEY `idx_uploader_id` (`uploader_id`),         
  FULLTEXT KEY `idx_fulltext_title` (`song_title`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS `playlists` (
  `playlist_id` INT NOT NULL AUTO_INCREMENT,
  `playlist_name` VARCHAR(255) NOT NULL,
  `creator_id` INT NOT NULL,
  `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `playlist_cover_url` VARCHAR(500) NOT NULL,
  `song_count` INT NOT NULL DEFAULT 0,
  `like_count` INT NOT NULL DEFAULT 0,
  `play_count` INT NOT NULL DEFAULT 0,
  `is_public` BOOLEAN NOT NULL DEFAULT TRUE,
  `description` TEXT,
  PRIMARY KEY (`playlist_id`),
  KEY `idx_creator_id` (`creator_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `songs_playlists_relation` (
  `playlist_id` INT NOT NULL,
  `song_id` INT NOT NULL,
  `song_playlist_position` INT NOT NULL DEFAULT 0, 
  PRIMARY KEY (`playlist_id`, `song_id`), 
  KEY `idx_song_id` (`song_id`),          
  KEY `idx_playlist_position` (`playlist_id`, `song_playlist_position`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `users_playlists_relation` (
  `user_id` INT NOT NULL,
  `playlist_id` INT NOT NULL,
  PRIMARY KEY (`user_id`, `playlist_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `users_likeplaylists_relation` (
  `user_id` INT NOT NULL,
  `playlist_id` INT NOT NULL,
  PRIMARY KEY (`user_id`, `playlist_id`), 
  KEY `idx_playlist_id` (`playlist_id`)   
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `queues` (
  `queue_id` INT NOT NULL AUTO_INCREMENT,
  `queue_name` VARCHAR(255) NOT NULL,
  `creator_id` INT NOT NULL,
  `song_count` INT NOT NULL DEFAULT 0,
  `is_current` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`queue_id`),
  KEY `idx_creator_date` (`creator_id`, `updated_date` DESC) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `queue_items` (
  `queue_item_id` INT NOT NULL AUTO_INCREMENT,
  `queue_id` INT NOT NULL,
  `song_id` INT NOT NULL,
  `queue_item_position` INT NOT NULL DEFAULT 0, 
  `added_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`queue_item_id`),
  KEY `idx_queue_song` (`queue_id`, `song_id`),          
  KEY `idx_queue_position` (`queue_id`, `queue_item_position`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `play_state` (
  `user_id` INT NOT NULL,
  `current_queue_id` INT DEFAULT NULL,
  `current_song_id` INT DEFAULT NULL,
  `current_position` INT DEFAULT 0,
  `current_progress` INT DEFAULT 0,
  `playmode` VARCHAR(20) DEFAULT 'sequence', 
  `updated_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  KEY `idx_current_queue` (`current_queue_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;