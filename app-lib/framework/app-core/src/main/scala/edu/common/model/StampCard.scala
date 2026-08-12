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
 * Collect `issueStampNum` stamps and the card is exchanged for one coupon. It
 * is a machine that produces coupons, not a second kind of discount: everything
 * that takes money off a bill still goes through
 * [[edu.customer.model.CustomerCoupon]], so pricing has one path to follow.
 *
 * It points at [[Coupon]] and not at [[CouponOffer]] on purpose. An offer
 * answers "how is this handed out — by code, from the list, how many, until
 * when", and for an exchange every one of those is already answered here:
 * whoever filled a card gets one, until `dateEnd`. Pointing at an offer would
 * mean creating a row with every field empty for each card.
 *
 * How stamps are earned is [[Rule]], which names the rule and nothing else —
 * the arithmetic lives in a rule implementation of its own. One card carries
 * one rule; two rules on one card means two cards.
 */
import StampCard.*
case class StampCard(
  id:            Option[Id],          // 台帳Id
  name:          String,              // 台帳名 (例: 2026 春のスタンプカード)
  rule:          Rule,                // 付与ルールの種類
  issueCouponId: Coupon.Id,           // 引換: 発行するクーポンId
  issueStampNum: Int,                 // 引換: 必要スタンプ数
  dateStart:     Option[LocalDate],   // 配布: 開始日
  dateEnd:       Option[LocalDate],   // 配布: 終了日。当日を含む
  state:         Status,              // 提供状態
  updatedAt:     LocalDateTime = Now, // データ更新日
  createdAt:     LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

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
   * ここが持つのは種類の識別だけで、「1 会計で何個押すか」は別ファイルのルール
   * 実装が受け持つ。しきい値を行に置かないので、「1000 円ごと」を変えるには
   * デプロイが要る代わりに、条件を Scala の式でそのまま書ける。
   *
   * 逆に言うと、台帳ごとにしきい値を変える運用が来たら、この enum ではなく列で
   * 持つ形に戻すことになる。
   *
   * ルール実装が [[edu.shop.model.Payment]] を見るなら、`common` は `shop` を
   * 参照できないので、実装は `shop` 側に置くか、会計金額のような素の値だけを
   * 受け取る形にする。
   */
  enum Rule(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_PAYMENT_COUNT  extends Rule(code = 1, name = "会計回数")
    case IS_PAYMENT_AMOUNT extends Rule(code = 2, name = "会計金額")

  /**
   * 提供状態: 廃止しても配布済みのカードは残るため、行は消さない
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_ARCHIVE extends Status(code = -1) // 廃止
    case IS_ACTIVE  extends Status(code =  1) // 有効
