/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*

/**
 * MenuItem: one product on sale (a burger, a drink, a set).
 *
 * A set meal is a MenuItem too — what it contains is expressed with option
 * groups, not with a second entity. That keeps pricing, availability and
 * display on one path instead of two.
 *
 * `price` is the current price. An order never reads it back: an order line
 * copies the price it was charged at (see `edu.sales.model.OrderItem`), so a
 * price revision here can never rewrite past orders.
 */
import MenuItem.*
case class MenuItem(
  id:          Option[Id],                      // 管理 ID（永続化前は None）
  categoryId:  MenuCategory.Id,                 // 属するカテゴリ
  name:        String,                          // 商品名
  description: String,                          // 説明文
  price:       Int,                             // 現在の価格（円・税抜）
  sortOrder:   Short,                           // カテゴリ内の表示順
  state:       Status        = Status.IS_ON_SALE, // 販売状態
  updatedAt:   LocalDateTime = Now,             // データ更新日
  createdAt:   LocalDateTime = Now              // データ作成日
) extends EntityModel[Id]:

  /** Whether this item may be put into a cart at all (stock is per shop). */
  def isOnSale: Boolean = state == Status.IS_ON_SALE

object MenuItem:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, MenuItem]
  type EmbeddedId = Entity.EmbeddedId[Id, MenuItem]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** Sales status. Discontinued items stay readable for past orders. */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_DISCONTINUED extends Status(code = -1) // 販売終了
    case IS_ON_SALE      extends Status(code =  1) // 販売中
