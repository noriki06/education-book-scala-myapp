/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * Coupon: what a discount is — and nothing about how it is handed out.
 *
 * The same discount can be offered several times over: on the list in spring,
 * behind a code in summer, again next year with a different cap. Each of those
 * is a [[CouponOffer]]. Keeping distribution out of here means the wording of
 * a discount is edited in one place, however many times it has been offered.
 *
 * `productId` narrows the discount to one product; None applies it to the whole
 * cart. A discount on "any burger" is not expressible — that needs a category
 * or a list, and neither is in scope.
 */
import Coupon.*
case class Coupon(
  id:                 Option[Id],           // クーポンId
  name:               String,               // クーポン名
  discountType:       DiscountType,         // 割引種別
  discountProductId:  Option[Product.Id],   // 対象商品, None: 全商品
  discountValue:      Option[Int],          // 割引値
  validDays:          Short,                // 取得後の有効日数
  state:              Status,               // 提供状態
  updatedAt:          LocalDateTime = Now,  // データ更新日
  createdAt:          LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

object Coupon:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Coupon]
  type EmbeddedId = Entity.EmbeddedId[Id, Coupon]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** 割引種別: 区分値なので 0 から振る。discountValue の読み方が変わる */
  enum DiscountType(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_FREE   extends DiscountType(code = 1, name = "商品無料")
    case IS_AMOUNT extends DiscountType(code = 2, name = "定額割引")

  /** 提供状態: 廃止しても発行済みのクーポンは残るため、行は消さない */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_ARCHIVE   extends Status(code = -1) // 廃止:   新しい配布を止めた
    case IS_PREPARING extends Status(code =  0) // 準備中: 配布はまだ始まっていない
    case IS_ACTIVE    extends Status(code =  1) // 有効
