/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*

import edu.common.model.Product

/**
 * ShopCustomProduct: a product that exists only at this shop.
 *
 * The other half of [[ShopExcludedProduct]]. One takes a head office product
 * away from a shop; this one adds a product head office never defined — the
 * local speciality, the tie-up with the shop next door.
 *
 * It is a separate entity, not an override of [[Product]]. Nothing here points
 * at a head office row, because there is no head office row: the shop invented
 * this. A shop changing the price of a head office product would be a third,
 * different thing, and would belong in a table that carries `productId`.
 *
 * `category` and `state` reuse [[Product]]'s enums rather than declaring their
 * own. Head office owns what a category is, so copying the list here would
 * leave shops behind the day a new one is added — and the app can render both
 * kinds of product through one mapping.
 *
 * Sets are out of scope. A set is a composition head office assembles, and a
 * shop-local set would have to hold both kinds of product in `subItem`, which
 * no single Id type can express. Sets stay with [[Product]].
 */
import ShopCustomProduct.*
case class ShopCustomProduct(
  id:          Option[Id],           // 商品Id
  shopId:      Shop.Id,              // 店舗Id
  name:        String,               // 商品名
  category:    Product.Category,     // 商品: カテゴリ
  price:       Int,                  // 価格
  state:       Product.Status,       // 販売状態
  description: String,               // 説明文
  updatedAt:   LocalDateTime = Now,  // データ更新日
  createdAt:   LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

object ShopCustomProduct:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, ShopCustomProduct]
  type EmbeddedId = Entity.EmbeddedId[Id, ShopCustomProduct]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
