/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.common.model.{ Coupon, CouponOffer, Product }
import edu.customer.model.CustomerCoupon

/**
 * 決済割引:
 * 会計で実際に適用した割引 1 行。
 * `coupon*` は割引の出どころ、`discount*` は実際に引いた内容。
 *
 * {{{
 *   discountUnitValue * discountProductNum = discountSubTotal
 *   billDiscountTotal = SUM(discountSubTotal)
 * }}}
 */
import PaymentDiscount.*
case class PaymentDiscount(
  id:                 Option[Id],                 // 管理Id
  paymentId:          Payment.Id,                 // 決済Id
  couponId:           Coupon.Id,                  // クーポンId
  couponOfferId:      Option[CouponOffer.Id],     // クーポン: 配布Id。スタンプ引換なら None
  customerCouponId:   Option[CustomerCoupon.Id],  // 顧客: 所持クーポンId
  discountType:       Coupon.DiscountType,        // 割引: 種別
  discountProductId:  Product.Id,                 // 割引: 対象商品Id
  discountProductNum: Int,                        // 割引: 対象商品個数
  discountUnitValue:  Int,                        // 割引: 単品あたり (円)
  discountSubTotal:   Int,                        // 割引: 小計 (円)
  updatedAt:          LocalDateTime = Now,        // データ更新日
  createdAt:          LocalDateTime = Now         // データ作成日
) extends EntityModel[Id]

object PaymentDiscount:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, PaymentDiscount]
  type EmbeddedId = Entity.EmbeddedId[Id, PaymentDiscount]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
