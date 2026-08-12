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
 * 会員が保有しているクーポン 1 枚。
 *
 * `couponId` は割引の内容
 *  - `couponOfferId` … 配布から取得したとき（[[CouponOffer]] の付与型）
 *  - `stampCardId`   … [[CustomerStampCard]] の引き換えで発行されたとき
 */
import CustomerCoupon.*
case class CustomerCoupon(
  id:            Option[Id],             // 保有Id
  customerId:    Customer.Id,            // 顧客Id
  couponId:      Coupon.Id,              // クーポンId
  couponOfferId: Option[CouponOffer.Id], // 配布Id（配布から取得したとき）
  stampCardId:   Option[StampCard.Id],   // 台帳Id（スタンプ引換のとき）
  expiredAt:     LocalDateTime,          // 有効期限。取得時に確定
  state:         Status,                 // 保有状態
  usedAt:        Option[LocalDateTime],  // 使用日時。IS_USED で埋まる
  updatedAt:     LocalDateTime = Now,    // データ更新日
  createdAt:     LocalDateTime = Now     // データ作成日
) extends EntityModel[Id]

/**
 * 保有クーポン: 付随する型と処理の定義
 */
object CustomerCoupon:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerCoupon]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerCoupon]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 保有状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_REVOKED extends Status(code = -1) // 取消: 不正取得などで運営が回収した
    case IS_UNUSED  extends Status(code =  1) // 未使用
    case IS_USED    extends Status(code =  2) // 使用済

  // --[ Extensions ]--------------------------------------------------
  /**
   * 保有クーポン: 変数値だけで完結する処理
   */
  extension (self: CustomerCoupon)

    /**
     * その時点で使えるか。失効は状態ではなく日付で判定する
     */
    def isUsableAt(at: LocalDateTime): Boolean =
      self.state == Status.IS_UNUSED && at.isBefore(self.expiredAt)
