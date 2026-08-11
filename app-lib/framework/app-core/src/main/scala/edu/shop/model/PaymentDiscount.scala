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
 * PaymentDiscount: one discount actually applied at checkout, and what it took
 * off.
 *
 * The breakdown behind [[Payment]] `billDiscountTotal`:
 *
 * {{{
 *   discountUnitValue * discountProductNum = discountSubTotal
 *   billDiscountTotal = SUM(discountSubTotal)
 * }}}
 *
 * Three levels, and the names say which is which: per unit, per row, per
 * payment.
 *
 * Fields come in two halves. `discount*` is what was taken off — copied at
 * checkout, never read back from the master. Head office renames a coupon or
 * changes 200 yen to 300 and last month's receipts must still read what the
 * customer was shown, the same reason an order line keeps its own figures.
 * `coupon*` is where the discount came from, kept for analysis: which campaign
 * actually gets used.
 *
 * `discountSubTotal` is what was deducted, which is not the coupon's value
 * multiplied blindly: a 500 yen coupon on a 300 yen product takes off 300.
 *
 * The name is broader than what it currently holds. Every discount today comes
 * from a coupon, so `couponId` is required — a stamp card grants a coupon
 * rather than discounting directly. An employee discount or an automatic
 * "3 for 10% off" would be the first source that is not a coupon, and that is
 * the point to make `couponId` optional and introduce a source kind.
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

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, PaymentDiscount]
  type EmbeddedId = Entity.EmbeddedId[Id, PaymentDiscount]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
