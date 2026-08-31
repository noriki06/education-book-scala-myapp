--
-- Copyright IxiaS, Inc. All Rights Reserved.
--
-- The event and the entries pressed against it. They are created together
-- because an entry without its event is meaningless.
--
-- Constraints the design placed on this layer (docs/domain/anonymous-event-app/
-- memo.md, "制約をどこで守るか"): `code` is the only key an anonymous visitor
-- has, so it must be unique; a member may hold at most one entry per event, so
-- the pair is unique; `min_entries` and the close/start ordering are CHECKed
-- here because they must hold no matter which code path writes the row.
--
-- No foreign keys, per the project's convention — the typed ids on the Scala
-- side carry that guarantee, and the indexes below carry the lookups.
--

CREATE TABLE `event` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`             VARCHAR(64)     NOT NULL,
  `member_id`        BIGINT UNSIGNED NOT NULL,
  `title`            VARCHAR(255)    NOT NULL,
  `start_at`         DATETIME        NOT NULL,
  `close_at`         DATETIME        NOT NULL,
  `min_entries`      SMALLINT        NOT NULL,
  `slack_channel_id` VARCHAR(64)     NOT NULL,
  `slack_message_id` VARCHAR(64)     NULL,
  `confirmed_at`     DATETIME        NULL,
  `state`            SMALLINT        NOT NULL DEFAULT 1,
  `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ukey01` (`code`),
  KEY `key01` (`member_id`),
  KEY `key02` (`state`, `start_at`),
  CONSTRAINT `chk_event_min_entries` CHECK (`min_entries` BETWEEN 2 AND 50),
  CONSTRAINT `chk_event_close_at`    CHECK (`close_at` <= `start_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `event_entry` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `event_id`   BIGINT UNSIGNED NOT NULL,
  `member_id`  BIGINT UNSIGNED NOT NULL,
  `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ukey01` (`event_id`, `member_id`),
  KEY `key01` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
