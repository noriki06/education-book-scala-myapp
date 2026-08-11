/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*

import edu.common.model.{ Coupon, CouponOffer, StampCard }

/**
 * CustomerCoupon: one coupon a member is holding.
 *
 * The `Customer` prefix is redundant with the package and kept anyway: without
 * it this would be `Coupon`, colliding with the master it points at, and every
 * file touching both would have to qualify one of them.
 *
 * `couponId` says what the coupon is. The two after it say where it came from,
 * and **only one of them is ever set**:
 *
 *  - `couponOfferId` … taken from a distribution, by code or from the list.
 *                      Only an offer of type IS_GRANTED lands here; a
 *                      direct-consumption one is applied straight in the cart
 *                      and leaves no row
 *  - `stampCardId`   … exchanged for a filled [[CustomerStampCard]]
 *
 * An exchange has no offer behind it. An offer answers "by code or from the
 * list, how many, until when", and for an exchange [[StampCard]] has already
 * answered all of it — pointing at one would mean creating a row with every
 * field empty.
 *
 * `expiredAt` is fixed at the moment of acquisition from the coupon's
 * `validDays`, not read back from the master. Reading it back would mean that
 * shortening the validity period retroactively expires coupons people are
 * already holding — the same reason an order line copies its price.
 *
 * The cap on an offer is simply the number of rows pointing at it, so a coupon
 * exchanged for a stamp card never counts against a distribution.
 */
import CustomerCoupon.*
case class CustomerCoupon(
  id:            Option[Id],              // 保有Id
  customerId:    Customer.Id,             // 顧客Id
  couponId:      Coupon.Id,               // クーポンId
  couponOfferId: Option[CouponOffer.Id],  // 配布Id（配布から取得したとき）
  stampCardId:   Option[StampCard.Id],    // 台帳Id（スタンプ引換のとき）
  expiredAt:     Option[LocalDateTime],   // 有効期限。取得時に確定。None は無期限
  state:         Status,                  // 保有状態
  usedAt:        Option[LocalDateTime],   // 使用日時。IS_USED で埋まる
  updatedAt:     LocalDateTime = Now,     // データ更新日
  createdAt:     LocalDateTime = Now      // データ作成日
) extends EntityModel[Id]:

  /** その時点で使えるか。失効は状態ではなく日付で判定する */
  def isUsableAt(at: LocalDateTime): Boolean =
    state == Status.IS_UNUSED && expiredAt.forall(at.isBefore)

object CustomerCoupon:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerCoupon]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerCoupon]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 保有状態。
   *
   * 失効を状態に持たないのは、期限切れが時刻の経過だけで起きるため。状態に
   * するとその瞬間に書き換えるバッチが要り、落ちた日に嘘をつく。
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_REVOKED extends Status(code = -1) // 取消: 不正取得などで運営が回収した
    case IS_UNUSED  extends Status(code =  1) // 未使用
    case IS_USED    extends Status(code =  2) // 使用済
