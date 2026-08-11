/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * StampCard: the head office definition of a stamp card.
 *
 * Collect `requiredNum` stamps and the card is exchanged for one coupon. It is
 * a machine that produces coupons, not a second kind of discount: everything
 * that takes money off a bill still goes through
 * [[edu.customer.model.CustomerCoupon]], so pricing has one path to follow.
 *
 * It points at [[Coupon]] and not at [[CouponOffer]] on purpose. An offer
 * answers "how is this handed out — by code, from the list, how many, until
 * when", and for an exchange every one of those is already answered here:
 * whoever filled a card gets one, until `dateEnd`. Pointing at an offer would
 * mean creating a row with every field empty for each card.
 *
 * How stamps are earned is [[Rule]] — the decision is code, only the numbers
 * are data. One card carries one rule; two rules on one card means two cards.
 */
import StampCard.*
case class StampCard(
  id:          Option[Id],          // 台帳Id
  name:        String,              // 台帳名 (例: 2026 春のスタンプカード)
  couponId:    Coupon.Id,           // 引き換えで発行するクーポン
  requiredNum: Int,                 // 引き換えに必要なスタンプ数
  rule:        Rule,                // 付与ルールの種類
  ruleUnit:    Int,                 // 付与の単位 (会計金額なら 1000 円ごと)
  grantNum:    Int,                 // 1 単位あたりに押す数
  dateStart:   Option[LocalDate],   // 配布: 開始日
  dateEnd:     Option[LocalDate],   // 配布: 終了日。当日を含む
  state:       Status,              // 提供状態
  updatedAt:   LocalDateTime = Now, // データ更新日
  createdAt:   LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]:

  /** その日に新しいカードを配れるか */
  def isOpenOn(date: LocalDate): Boolean =
    state == Status.IS_ACTIVE
      && dateStart.forall(!date.isBefore(_))
      && dateEnd.forall(!date.isAfter(_))

  /** 1 会計で何個押されるか */
  def stampsFor(billedTotal: Int): Int =
    rule.stampsFor(billedTotal, ruleUnit, grantNum)

object StampCard:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, StampCard]
  type EmbeddedId = Entity.EmbeddedId[Id, StampCard]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 付与ルール。
   *
   * 判定はコード、しきい値はデータ。「1000 円ごとに 1 個」の 1000 を行にして
   * も、割り算そのものは結局コードに書くことになり、二重管理になる。
   *
   * 引数が [[edu.shop.model.Payment]] ではなく Int なのは、`common` が `shop`
   * を参照しないため。呼ぶ側が会計金額を渡す。
   */
  enum Rule(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_PAYMENT_AMOUNT extends Rule(code = 1, name = "会計金額") // ruleUnit 円ごと
    case IS_PAYMENT_COUNT  extends Rule(code = 2, name = "会計回数") // 1 会計につき

    /** 会計 1 回で押される数 */
    def stampsFor(billedTotal: Int, ruleUnit: Int, grantNum: Int): Int = this match
      case IS_PAYMENT_AMOUNT => if ruleUnit <= 0 then 0 else (billedTotal / ruleUnit) * grantNum
      case IS_PAYMENT_COUNT  => grantNum

  /** 提供状態: 廃止しても配布済みのカードは残るため、行は消さない */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_ARCHIVE extends Status(code = -1) // 廃止: 新しい配布を止めた
    case IS_ACTIVE  extends Status(code =  1) // 有効
