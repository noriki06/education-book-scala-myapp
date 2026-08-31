--
-- Copyright IxiaS, Inc. All Rights Reserved.
--
-- The confirmed roster is sorted by reading, not by entry order (docs/domain/
-- anonymous-event-app/01_requirements.md): an order correlated with insertion
-- would put the proposer first and break anonymity. Kanji display names cannot
-- be sorted by reading, so the reading is collected at signup as its own
-- column. Existing dev rows get '' — there is no sensible backfill.
--

ALTER TABLE `member`
  ADD COLUMN `name_kana` VARCHAR(255) NOT NULL AFTER `name`;
