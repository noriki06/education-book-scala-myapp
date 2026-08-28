--
-- Copyright IxiaS, Inc. All Rights Reserved.
--
-- The Customer model became Member (docs/domain/anonymous-event-app/naming.md §3):
-- the people registering here are colleagues joining events, not customers
-- being sold to. The three tables and the columns pointing at them follow,
-- the same move as user -> customer before it.
--

RENAME TABLE `customer`          TO `member`;
RENAME TABLE `customer_password` TO `member_password`;
RENAME TABLE `customer_session`  TO `member_session`;

ALTER TABLE `member_password` CHANGE `customer_id` `member_id` BIGINT UNSIGNED NOT NULL;
ALTER TABLE `member_session`  CHANGE `customer_id` `member_id` BIGINT UNSIGNED NOT NULL;
