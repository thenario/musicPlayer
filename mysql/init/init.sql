-- ============================================================
-- 数据库初始化脚本（纯建库建表，不含数据）
--
-- 主键策略：雪花 BIGINT ID，由应用层（MyBatis-Plus IdType.ASSIGN_ID）生成，
--           数据库不设置 AUTO_INCREMENT。
-- 命名约定：表名小写下划线；主键一律 `<实体>_id`；时间戳 `*_date`；
--           索引 `idx_*`；外键 `fk_<父表>_<子表>`。
-- 提示：数据库名 musicPlayer 会被 scaffold.sh 替换为目标项目名。
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
