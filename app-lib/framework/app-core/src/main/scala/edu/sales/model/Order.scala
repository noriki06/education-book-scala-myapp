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
import edu.value.Money

/**
 * Order: a purchase that has been confirmed.
 *
 * The three amounts are stored, which breaks the usual rule that anything
 * derivable should not be kept. They are kept because they are not a
 * calculation — they are what the customer was charged at that moment. A tax
 * change or a price revision must never move them.
 *
 * `code` is what the counter calls out and what appears in URLs. The
 * auto-increment `id` never leaves the database: a sequential number in public
 * leaks how many orders exist and invites reading someone else's order by
 * subtracting one.
 */
import Order.*
case class Order(
  id:        Option[Id],                        // 管理 ID（永続化前は None）
  code:      Code,                              // 受付番号（公開用の識別子）
  uid:       User.Id,                           // 注文した会員
  shopId:    Shop.Id,                           // 受け取る店舗
  pickupAt:  LocalDateTime,                     // 受け取り予定時刻
  subtotal:  Money,                             // 税抜合計（確定時の金額）
  tax:       Money,                             // 消費税額（確定時の金額）
  total:     Money,                             // 税込合計（確定時の金額）
  state:     Status        = Status.IS_ACCEPTED, // 注文の状態
  updatedAt: LocalDateTime = Now,               // データ更新日
  createdAt: LocalDateTime = Now                // データ作成日
) extends EntityModel[Id]:

  /** Cancellable only until the kitchen starts — food already made cannot be un-made. */
  def isCancelable: Boolean = state == Status.IS_ACCEPTED

object Order:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type Code       = Code.Repr
  type WithNoId   = Entity.WithNoId[Id, Order]
  type EmbeddedId = Entity.EmbeddedId[Id, Order]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  /** Public receipt number. Random, so it reveals nothing about order volume. */
  object Code extends Entity.Id[String]:
    def generate: Code = Code(java.util.UUID.randomUUID.toString.take(8).toUpperCase)

  // --[ Value Objects ]-----------------------------------------------
  /** Order status. The happy path runs 1 → 2 → 3 → 4; cancellation leaves it. */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CANCELED extends Status(code = -1) // キャンセル
    case IS_ACCEPTED extends Status(code =  1) // 受付
    case IS_COOKING  extends Status(code =  2) // 調理中
    case IS_READY    extends Status(code =  3) // 受取準備完了
    case IS_HANDED   extends Status(code =  4) // 受渡し完了
