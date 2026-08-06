/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*

import edu.common.model.CouponIssue

/**
 * CustomerCoupon: one coupon a member is holding.
 *
 * Only produced by an issue of type IS_GRANTED. A direct-consumption issue is
 * applied straight in the cart and leaves no row here.
 *
 * `expiredAt` is fixed at the moment of acquisition from the coupon's
 * `validDays`, not read back from the master. Reading it back would mean that
 * shortening the validity period retroactively expires coupons people are
 * already holding — the same reason an order line copies its price.
 *
 * It points at the issue rather than the coupon, so "which campaign did this
 * come from" survives, and the cap on that issue is simply the number of rows
 * pointing at it.
 */
import CustomerCoupon.*
case class CustomerCoupon(
  id:            Option[Id],             // 保有Id
  customerId:    Customer.Id,            // 顧客Id
  couponIssueId: CouponIssue.Id,         // どの配布から取得したか
  expiredAt:     Option[LocalDateTime],  // 有効期限。取得時に確定。None は無期限
  state:         Status,                 // 保有状態
  usedAt:        Option[LocalDateTime],  // 使用日時。IS_USED で埋まる
  updatedAt:     LocalDateTime = Now,    // データ更新日
  createdAt:     LocalDateTime = Now     // データ作成日
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
