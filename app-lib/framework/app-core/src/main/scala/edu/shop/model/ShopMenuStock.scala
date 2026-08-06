/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*

/**
 * ShopMenuStock: whether one shop can serve one item right now.
 *
 * Being sold out is not a property of the product — Shibuya may be out of
 * fries while Shinjuku still has them — so it lives on the pairing of shop and
 * item. A flag on MenuItem would sell out every shop at once.
 *
 * This is the fastest moving data in the system: a shop manager flips it
 * during the lunch rush. That is why it sits in `shop` and not in `common`,
 * which is reserved for data the daily operation never writes.
 */
import ShopMenuStock.*
case class ShopMenuStock(
  id:         Option[Id],                        // 管理 ID（永続化前は None）
  shopId:     Shop.Id,                           // どの店舗で
  menuItemId: MenuItem.Id,                       // どの商品が
  state:      Status        = Status.IS_AVAILABLE, // 提供できるか
  updatedAt:  LocalDateTime = Now,               // データ更新日
  createdAt:  LocalDateTime = Now                // データ作成日
) extends EntityModel[Id]:

  /** Whether this shop can serve the item — checked again when an order is placed. */
  def isAvailable: Boolean = state == Status.IS_AVAILABLE

object ShopMenuStock:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, ShopMenuStock]
  type EmbeddedId = Entity.EmbeddedId[Id, ShopMenuStock]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** Stock status for this shop and item. */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_SOLD_OUT  extends Status(code = -1) // 売り切れ
    case IS_AVAILABLE extends Status(code =  1) // 提供可
