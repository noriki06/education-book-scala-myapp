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
  def isCancelable: Boolean = state.canMoveTo(Status.IS_CANCELED)

  /**
   * Move to `next`, or explain why the flow does not allow it.
   *
   * Returning an Either rather than throwing keeps the rule usable from the
   * controller's EitherT flow: an illegal transition is a 409, not a 500.
   */
  def moveTo(next: Status): Either[String, Order] =
    Either.cond(
      state.canMoveTo(next),
      copy(state = next),
      s"An order cannot move from $state to $next"
    )

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

    /**
     * The transition table, in one place.
     *
     * Cancelling is only allowed before the kitchen starts — once cooking has
     * begun the food exists and someone has to pay for it.
     */
    def canMoveTo(next: Status): Boolean = (this, next) match
      case (IS_ACCEPTED, IS_COOKING)  => true
      case (IS_ACCEPTED, IS_CANCELED) => true
      case (IS_COOKING,  IS_READY)    => true
      case (IS_READY,    IS_HANDED)   => true
      case _                          => false
