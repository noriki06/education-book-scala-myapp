--
-- Copyright IxiaS, Inc. All Rights Reserved.
--
-- The User model became Customer, so its three tables follow.
--
-- Naming from here on: <context>_<model>, snake_case, singular, with the
-- prefix merged when the model name already carries it (Customer -> customer,
-- not customer_customer). The `common` context takes no prefix at all.
--
-- `uid` also becomes `customer_id`. A column that names its target the same way
-- everywhere is worth more than three saved characters, and `uid` reads as a
-- unix uid or a uuid to anyone arriving fresh.
--

RENAME TABLE `udb_user`          TO `customer`;
RENAME TABLE `udb_user_password` TO `customer_password`;
RENAME TABLE `udb_user_session`  TO `customer_session`;

ALTER TABLE `customer_password` CHANGE `uid` `customer_id` BIGINT UNSIGNED NOT NULL;
ALTER TABLE `customer_session`  CHANGE `uid` `customer_id` BIGINT UNSIGNED NOT NULL;
