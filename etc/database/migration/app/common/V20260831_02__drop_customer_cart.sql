--
-- Copyright IxiaS, Inc. All Rights Reserved.
--
-- `customer_cart` came from the curriculum's burger-shop exercise and has no
-- place in an anonymous event app: it carries a shop, order items and coupons,
-- none of which exist here. The Scala side was deleted when the training
-- domain was stripped back to the auth base; this drops the table itself so
-- the schema holds only the seven entities the design settled on.
--
-- The migration that created it stays untouched, as always — history is added
-- to, never rewritten.
--

DROP TABLE IF EXISTS `customer_cart`;
