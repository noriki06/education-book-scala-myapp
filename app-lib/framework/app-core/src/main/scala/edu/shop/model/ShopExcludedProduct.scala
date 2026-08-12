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
 * ShopExcludedProduct: a product this shop cannot serve at all.
 *
 * Independent of menus on purpose. A shop without a fryer cannot serve fries
 * in the grand menu, the morning menu or any seasonal one — stating it once
 * against the product covers every menu, now and in the future.
 *
 * This is a standing limitation of the shop (no equipment, no space), not
 * today's stock. Running out of fries at lunch is a different question with a
 * different lifetime, and does not belong here.
 *
 * "Excluded", not "Exclusion", so the name reads as a product rather than as
 * three nouns in a row. It also states the fact instead of the mechanism: a
 * negative list is how this is stored, not what it means. [[ShopExcludedMenu]]
 * is its pair.
 */
import ShopExcludedProduct.*
case class ShopExcludedProduct(
  id:        Option[Id],          // 管理Id
  shopId:    Shop.Id,             // 店舗Id
  productId: Product.Id,          // 除外する商品Id
  note:      Option[String],      // 理由の覚書
  updatedAt: LocalDateTime = Now, // データ更新日
  createdAt: LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

object ShopExcludedProduct:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, ShopExcludedProduct]
  type EmbeddedId = Entity.EmbeddedId[Id, ShopExcludedProduct]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
