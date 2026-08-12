/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*

import edu.common.model.StampCard

/**
 * CustomerStampCard: one card a member is filling.
 *
 * The package repeats the prefix, and the prefix stays: without it the short
 * name is [[StampCard]], which is head office's ledger — the rule, the target,
 * the period. This is one member's card issued under it.
 *
 * A card is exchanged and then finished — the next stamp starts a new card,
 * exactly as with a paper one. That is what keeps "the ten already spent" and
 * "the one just earned" apart without a flag on each stamp.
 *
 * The stamps themselves are [[CustomerStampCardItem]] rows; how many are on
 * this card is their count. A number kept here would drift from them the first
 * time something failed halfway.
 *
 * The `issuedCoupon*` pair is filled together at the exchange, and is what
 * makes a card traceable to the coupon it produced. The other direction only
 * records the ledger, so without this pair a member who filled the same card
 * twice would leave two coupons nobody could tell apart.
 *
 * Elsewhere a held coupon is `customerCouponId` ([[edu.shop.model.PaymentDiscount]],
 * [[Cart.UseCoupon]]). Here the prefix groups by the event instead, the way
 * `bill*` does in [[edu.shop.model.Payment]], because the two are meaningless
 * apart: neither alone says what happened.
 *
 * `Coupon` sits in both names on purpose. Plain `issuedAt` would read as the
 * day the card itself was handed to the member, which is `createdAt`. What was
 * issued here is the coupon; what happened to the card is `IS_EXCHANGED`.
 */
import CustomerStampCard.*
case class CustomerStampCard(
  id:             Option[Id],                // 保有Id
  cardId:         StampCard.Id,              // 台帳Id
  customerId:     Customer.Id,               // 顧客Id
  issuedCouponId: Option[CustomerCoupon.Id], // 発行: クーポンId
  issuedCouponAt: Option[LocalDateTime],     // 発行: 日時
  state:          Status,                    // 保有状態
  updatedAt:      LocalDateTime = Now,       // データ更新日
  createdAt:      LocalDateTime = Now        // データ作成日
) extends EntityModel[Id]:

  /**
   * その日にまだスタンプを押せるカードか。
   *
   * 判定の正は [[StampCard.dateEnd]] であって `state` ではない。`IS_EXPIRED`
   * は掃除の処理が後から付ける印で、期限を過ぎた瞬間に立つとは限らないため、
   * それを信じると **失効済みのカードにスタンプを押せてしまう**。逆に配布期間
   * が延長されたときは、印が付いたままでも再び押せるのが正しい。
   *
   * 終端として扱うのは `IS_EXCHANGED` だけ。引き換えたカードは、日付が何であれ
   * もう使わない。
   */
  def isCollectingOn(date: LocalDate, ledger: StampCard): Boolean =
    state != Status.IS_EXCHANGED && ledger.dateEnd.forall(!date.isAfter(_))

object CustomerStampCard:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerStampCard]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerStampCard]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 保有状態。
   *
   * `IS_EXPIRED` は掃除のための印で、**判定には使わない**。期限切れは時刻の
   * 経過だけで起きるので、印を立てる処理は必ず遅れる。使えるかどうかは
   * [[CustomerStampCard.isCollectingOn]] が台帳の日付で判定する。
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_EXPIRED    extends Status(code = -1) // 失効の印: 掃除で付ける。判定の正ではない
    case IS_COLLECTING extends Status(code =  1) // 集め中
    case IS_EXCHANGED  extends Status(code =  2) // 引換済: クーポンを発行した。ここだけが終端
