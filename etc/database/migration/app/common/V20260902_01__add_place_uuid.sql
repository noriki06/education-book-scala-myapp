--
-- Copyright IxiaS, Inc. All Rights Reserved.
--
-- A place now carries a public identifier of its own (docs/domain/
-- anonymous-event-app/03_design.md, 論点11): the place detail page is
-- addressed by it, so the numeric id stays inside, as it already does for a
-- member and an event.
--
-- The unique keys are renumbered so that `ukey01` is the public identifier
-- here too, matching `member` and `event`. Safe to do outright because the
-- table holds no rows yet — with data, the added NOT NULL column would take
-- '' for every row and the unique index would refuse them.
--

ALTER TABLE `place` ADD COLUMN `uuid` VARCHAR(64) NOT NULL AFTER `id`;

ALTER TABLE `place` DROP INDEX `ukey01`;
ALTER TABLE `place` ADD UNIQUE KEY `ukey01` (`uuid`);
ALTER TABLE `place` ADD UNIQUE KEY `ukey02` (`google_place_id`);
