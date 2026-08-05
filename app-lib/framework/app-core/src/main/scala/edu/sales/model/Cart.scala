/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.sales.model

import ixias.core.model.*

import edu.udb.model.User
import edu.shop.model.Shop

/**
 * Cart: what a member is about to order from one shop.
 *
 * Kept on the server, not in the browser, so the phone and the desktop show
 * the same cart and closing the app does not empty it.
 *
 * A cart holds no totals. It is a draft, so prices must follow the current
 * menu — the sum is recomputed every time it is shown. The opposite is true
 * of an order, which freezes what was charged (see [[Order]]).
 */
import Cart.*
case class Cart(
  id:        Option[Id],                       // 管理 ID（永続化前は None）
  uid:       User.Id,                          // 誰のカートか
  shopId:    Shop.Id,                          // どの店舗で受け取るか
  state:     Status        = Status.IS_EDITING, // カートの状態
  updatedAt: LocalDateTime = Now,              // データ更新日
  createdAt: LocalDateTime = Now               // データ作成日
) extends EntityModel[Id]:

  /** Whether lines may still be added or removed. */
  def isEditable: Boolean = state == Status.IS_EDITING

object Cart:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Cart]
  type EmbeddedId = Entity.EmbeddedId[Id, Cart]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** Cart status. An ordered cart is kept, not deleted — it is the draft an order came from. */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_DISCARDED extends Status(code = -1) // 破棄
    case IS_EDITING   extends Status(code =  1) // 編集中
    case IS_ORDERED   extends Status(code =  2) // 注文済み
