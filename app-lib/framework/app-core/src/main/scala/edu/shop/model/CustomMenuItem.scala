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
 * ShopCustomMenuItem: one product on a shop's own menu tab.
 *
 * A shop's tab may hold either kind of product, so there are two references
 * and **only one of them is ever set**:
 *
 *  - `productId`       … a head office product ([[Product]])
 *  - `productCustomId` … one the shop invented ([[ShopCustomProduct]])
 *
 * They cannot be one column. The two Ids are distinct opaque types, and a
 * single `Long` would give up both the foreign key and the compiler's ability
 * to catch a swap — the same reason [[ShopExcludedProduct]] and
 * [[ShopExcludedMenu]] stay apart rather than sharing a `targetId`.
 *
 * The exclusion is not checked here, the way `Cart.UseCoupon` leaves its own
 * alone: a `require` would fire while reading rows back, and one bad row would
 * take a whole menu down with it. Registration checks it instead.
 *
 * A head office product listed here is not a way around
 * [[ShopExcludedProduct]]. Exclusion is a standing limitation of the shop — no
 * fryer means no fries, on any tab — so it wins wherever it appears.
 */
import ShopCustomMenuItem.*
case class ShopCustomMenuItem(
  id:              Option[Id],                   // 管理Id
  shopId:          Shop.Id,                      // 店舗Id
  menuId:          ShopCustomMenu.Id,            // メニューId
  productId:       Option[Product.Id],           // 商品: 本部商品Id
  productCustomId: Option[ShopCustomProduct.Id], // 商品: 店舗独自商品Id
  sortOrder:       Short,                        // 表示順
  updatedAt:       LocalDateTime = Now,          // データ更新日
  createdAt:       LocalDateTime = Now           // データ作成日
) extends EntityModel[Id]

object ShopCustomMenuItem:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, ShopCustomMenuItem]
  type EmbeddedId = Entity.EmbeddedId[Id, ShopCustomMenuItem]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
