--
-- Copyright IxiaS, Inc. All Rights Reserved.
--
-- The cart a customer is filling. `customer_id` is nullable on purpose: a cart
-- exists before anyone logs in and is claimed later, which is why `token` and
-- not the customer is what identifies it.
--
-- `items` and `coupons` are JSON. They are only ever read and written with the
-- cart itself — nothing queries a cart line on its own — so a child table would
-- buy joins and a delete cascade for nothing. An order line is the opposite and
-- gets its own table.
--

CREATE TABLE `customer_cart` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `token`       VARCHAR(255)    NOT NULL,
  `customer_id` BIGINT UNSIGNED NULL,
  `shop_id`     BIGINT UNSIGNED NOT NULL,
  `items`       JSON            NOT NULL,
  `coupons`     JSON            NOT NULL,
  `state`       SMALLINT        NOT NULL DEFAULT 1,
  `updated_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ukey01` (`token`),
  KEY `key01` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
