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
 * Cart: what someone is about to order from one shop.
 *
 * Kept on the server, not in the browser, so the phone and the desktop show
 * the same cart and closing the app does not empty it.
 *
 * The cart is identified by its own `token`, not by who owns it. Requiring a
 * customer would mean asking people to log in before they can put anything in
 * the basket, which is the surest way to lose them — they browse first and
 * sign in at checkout. `customerId` is therefore filled in later, when they do.
 *
 * The token lives in a cookie exactly as [[CustomerSession]]'s does, so an
 * anonymous cart survives a page reload and a return visit.
 *
 * A cart holds no totals. It is a draft, so prices must follow the current
 * menu — the sum is recomputed every time it is shown. The opposite is true
 * of an order, which freezes what was charged (see [[edu.shop.model.Order]]).
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
) extends EntityModel[Id]:

  /** ログイン前のカートか。true の間は付与型クーポンを載せられない */
  def isAnonymous: Boolean = customerId.isEmpty

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
   * `couponId` は何が割引されるか（内容のマスタ）。残る 2 つは出どころで、
   * **どちらか一方だけが Some になる。**
   *
   *  - `couponOfferId`    … 直接消費型（[[CouponOffer.IssueType.IS_DIRECT]]）。
   *                         取得を挟まないので配布口を直に指す
   *  - `customerCouponId` … 付与型。会員が保有する 1 枚を消費する
   *
   * 両方を持たせると、直接消費と「付与型なのに保有分が指定されていない設定
   * ミス」が同じ形になり、区別できなくなる。一方だけにすれば、**どちらが入って
   * いるかがそのまま消費形態を語る。**
   *
   * 使うときに必要な ID とも一致する。直接消費は配布口の期間を見て、付与型は
   * 保有分の有効期限を見る。付与型から配布口をたどりたいときは
   * [[CustomerCoupon.couponOfferId]] から引ける。
   *
   * 排他はここでは検査しない。`require` を置くと JSON からの復元時にも走り、
   * 壊れた行が 1 件あるだけで一覧の取得ごと落ちるため。カートに載せる処理と
   * 会計処理で守る。
   *
   * 割引額はここに持たない。金額が決まるのは会計時なので、実際に引いた額は
   * [[edu.shop.model.PaymentDiscount]] に記録される。
   */
  case class UseCoupon(
    couponId:         Coupon.Id,                     // クーポンId
    couponOfferId:    Option[CouponOffer.Id],        // クーポン: 配布Id（直接消費のとき）
    customerCouponId: Option[CustomerCoupon.Id]      // 顧客: 保有クーポンId（付与型のとき）
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

