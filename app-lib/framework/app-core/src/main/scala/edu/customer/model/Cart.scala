/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*

import edu.shop.model.Shop
import edu.common.model.{ Product, Coupon, CouponIssue }

/**
 * Cart: what a member is about to order from one shop.
 *
 * Kept on the server, not in the browser, so the phone and the desktop show
 * the same cart and closing the app does not empty it.
 *
 * A cart holds no totals. It is a draft, so prices must follow the current
 * menu — the sum is recomputed every time it is shown. The opposite is true
 * of an order, which freezes what was charged (see [[Order]]).
 */
import Cart.*
case class Cart(
  id:         Option[Id],                        // 管理Id
  customerId: Customer.Id,                       // 顧客Id
  shopId:     Shop.Id,                           // 店舗Id
  items:      Seq[BuyItem]     = Nil,               // 購入商品
  coupons:    Seq[UseCoupon]   = Nil,               // 利用クーポン
  state:      Status        = Status.IS_EDITING, // カートの状態
  updatedAt:  LocalDateTime = Now,               // データ更新日
  createdAt:  LocalDateTime = Now                // データ作成日
) extends EntityModel[Id]

object Cart:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Cart]
  type EmbeddedId = Entity.EmbeddedId[Id, Cart]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Models ]------------------------------------------------------
  /**
   * カートに入れた商品 1 行。
   *
   * 商品への参照と個数しか持たない。カートは「これから買うもの」の下書きな
   * ので、本部が夜に値上げしたら翌朝のカートは新しい価格で見えるべきであり、
   * 価格は表示のたびに [[Product]] から引く。
   *
   * 注文が確定すると [[edu.shop.model.OrderItem]] へ移る。あちらも参照しか
   * 持たない形にしてあり、確定した金額は [[edu.shop.model.Payment]] がまとめて
   * 保持する。
   */
  case class BuyItem(
    productId:  Product.Id,  // 商品Id
    productNum: Int,         // 注文数
  )

  /**
   * カートに適用したクーポン 1 行。
   *
   * ID を 3 つ持つのは、それぞれ別のことを指しているため。
   *
   *  - `couponId`         … 何が割引されるか（内容のマスタ）
   *  - `couponIssueId`    … どの配布から来たか（コード配布か一覧配布か）
   *  - `customerCouponId` … 会員が保有するどの 1 枚を消費するか
   *
   * 最後だけ Option。直接消費型（[[CouponIssue.UseType.IS_DIRECT]]）は取得を
   * 挟まないので、消費すべき保有分が存在しない。
   *
   * 割引額はここに持たない。金額が決まるのは会計時なので、実際に引いた額は
   * [[edu.shop.model.PaymentDiscount]] に記録される。
   */
  case class UseCoupon(
    couponId:         Coupon.Id,                  // クーポンId
    couponIssueId:    CouponIssue.Id,             // クーポン: 配布Id
    customerCouponId: Option[CustomerCoupon.Id]   // 顧客: 保有クーポンId
  )

  // --[ Value Objects ]-----------------------------------------------
  /**
   * Cart status. An ordered cart is kept,
   * not deleted — it is the draft an order came from.
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_DISCARDED extends Status(code = -1) // 破棄
    case IS_EDITING   extends Status(code =  1) // 編集中
    case IS_ORDERED   extends Status(code =  2) // 注文済み

