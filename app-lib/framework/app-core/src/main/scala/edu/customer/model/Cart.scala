/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*
import ixias.core.model.value.Token

import edu.shop.model.Shop
import edu.common.model.{ Product, Coupon, CouponOffer }

/**
 * カート: ある店舗に対して注文しようとしている内容。
 *
 * ログインしていなくても利用できる。
 * 誰のものかではなくカート自身の `token`で識別し、
 * `customerId` はログインしたときに後から紐づけることができる
 */
import Cart.*
case class Cart(
  id:         Option[Id],                        // 管理Id
  token:      Token,                             // カートのトークン（未署名）
  customerId: Option[Customer.Id],               // 顧客Id。ログイン後に紐づく
  shopId:     Shop.Id,                           // 店舗Id
  items:      Seq[BuyItem]  = Nil,               // 購入商品
  coupons:    Seq[UseCoupon] = Nil,              // 利用クーポン
  state:      Status        = Status.IS_EDITING, // カートの状態
  updatedAt:  LocalDateTime = Now,               // データ更新日
  createdAt:  LocalDateTime = Now                // データ作成日
) extends EntityModel[Id]

object Cart:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Cart]
  type EmbeddedId = Entity.EmbeddedId[Id, Cart]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * カートに入れた商品
   */
  case class BuyItem(
    productId:  Product.Id,  // 商品Id
    productNum: Int,         // 注文数
  )

  /**
   * カートに適用したクーポン
   *
   * `couponId` は割引の内容。
   *  - `couponOfferId`    … 直接消費型（[[CouponOffer.IssueType.IS_DIRECT]]）
   *  - `customerCouponId` … 付与型。会員が保有する 1 枚を消費する
   */
  case class UseCoupon(
    couponId:         Coupon.Id,                 // クーポンId
    couponOfferId:    Option[CouponOffer.Id],    // クーポン: 配布Id（直接消費のとき）
    customerCouponId: Option[CustomerCoupon.Id]  // 顧客: 保有クーポンId（付与型のとき）
  )

  /**
   * カートの状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_DISCARDED extends Status(code = -1) // 破棄
    case IS_EDITING   extends Status(code =  1) // 編集中
    case IS_ORDERED   extends Status(code =  2) // 注文済み

  // --[ Extensions ]--------------------------------------------------
  /**
   * カート: 変数値だけで完結する処理
   */
  extension (self: Cart)

    /**
     * ログイン前のカートか
     */
    def isAnonymous: Boolean =
      self.customerId.isEmpty

