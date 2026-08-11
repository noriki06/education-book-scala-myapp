/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * CouponOffer: one offering of a [[Coupon]] — when it is available, to whom,
 * and how many.
 *
 * Named for the side that hands the discount out. The one that somebody ends
 * up holding is [[edu.customer.model.CustomerCoupon]]; calling this an "issue"
 * pointed at the wrong end of that, since what gets issued is the holding.
 *
 * "Offer" also covers the case where nothing is ever held: a direct one is
 * presented and consumed on the spot, online, and leaves no row on the member
 * at all. A name built around holding or issuing could not describe that half.
 *
 * Every way of handing out a discount goes through here, so there is one place
 * to ask "can this be picked up right now".
 *
 *  - `promoCode` is None — the coupon appears in the app's list and anyone may take it
 *  - `promoCode` is Some — it is only reachable by entering that code
 *
 * A code is what an advert carries, so one coupon usually has several offers:
 * a different code per medium, each with its own cap and dates, which is how
 * "which advert actually worked" gets answered.
 *
 * `promoLimit` caps how many may be taken; None is unlimited. The count of
 * takers is the number of [[edu.customer.model.CustomerCoupon]] rows against
 * this offer, never a counter kept here — a counter and its rows drift apart
 * the first time something fails halfway.
 *
 * A direct-consumption offer produces no CustomerCoupon rows at all, so a cap
 * on one is meaningless; that combination is rejected on creation.
 */
import CouponOffer.*
case class CouponOffer(
  id:         Option[Id],          // 配布Id
  couponId:   Coupon.Id,           // クーポンId
  issueType:  IssueType,           // 発行形態: 直接消費 / 付与消費
  promoCode:  Option[Code],        // プロモコード。None は一覧配布
  promoLimit: Option[Int],         // 配布上限。None は無制限
  dateStart:  Option[LocalDate],   // 配布: 開始日
  dateEnd:    Option[LocalDate],   // 配布: 終了日
  state:      Status,              // 配布状態
  updatedAt:  LocalDateTime = Now, // データ更新日
  createdAt:  LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

object CouponOffer:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type Code       = Code.Repr
  type WithNoId   = Entity.WithNoId[Id, CouponOffer]
  type EmbeddedId = Entity.EmbeddedId[Id, CouponOffer]

  // --[ Objects ]-----------------------------------------------------
  object Id   extends Entity.Id[Long]
  object Code extends Entity.Id[String]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 発行形態: 保有を発行するかどうか。
   *
   * 会計での使われ方は両者で同じで、違うのはその手前。直接消費は
   * [[edu.customer.model.CustomerCoupon]] の行を作らないので、「使い方の種別」
   * ではなく「発行の有無」がこの区分の軸になる。
   */
  enum IssueType(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_DIRECT  extends IssueType(code = 1, name = "直接消費")  // 発行せずカートで使う
    case IS_GRANTED extends IssueType(code = 2, name = "付与消費")  // 発行して保有してから使う

  /** 配布状態 */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CLOSED extends Status(code = -1) // 停止: 配布を打ち切った
    case IS_OPEN   extends Status(code =  1) // 配布中
