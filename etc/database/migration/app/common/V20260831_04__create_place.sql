--
-- Copyright IxiaS, Inc. All Rights Reserved.
--
-- Places and the reviews written about them. Created together because a review
-- without its place is meaningless.
--
-- Written from the output of `runMain tool.ShowCreateTable`, then given what
-- the generator does not know about: the CHECK on `star`, the storage engine,
-- DATETIME instead of TIMESTAMP (matching every other table here, and clear of
-- the 2038 limit), and the reasoning below.
--
-- `google_place_id` is unique only where it has a value — MySQL does not treat
-- two NULLs as duplicates, which is what a manually registered place (a canteen,
-- a corner of the office) needs.
--
-- `place_review` deliberately has no unique key: a member reviews the same place
-- every time they go (docs/domain/anonymous-event-app/03_design.md, 論点6).
--

CREATE TABLE `place` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `google_place_id` VARCHAR(255)    NULL,
  `name`            VARCHAR(255)    NOT NULL,
  `updated_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ukey01` (`google_place_id`),
  KEY `key01` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `place_review` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `place_id`   BIGINT UNSIGNED NOT NULL,
  `member_id`  BIGINT UNSIGNED NOT NULL,
  `star`       SMALLINT        NOT NULL,
  `comment`    VARCHAR(255)    NOT NULL,
  `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `key01` (`place_id`, `created_at`),
  KEY `key02` (`member_id`),
  CONSTRAINT `chk_place_review_star` CHECK (`star` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
