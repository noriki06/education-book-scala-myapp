/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * クーポン: 割引の内容を定義する本部マスタ。
 *
 * 配布方法は持たない。誰にどう配るかは [[CouponOffer]] が受け持つ。
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

/**
 * クーポン: 付随する型と処理の定義
 */
object Coupon:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Coupon]
  type EmbeddedId = Entity.EmbeddedId[Id, Coupon]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 割引種別: discountValue の読み方が変わる
   */
  enum DiscountType(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_FREE   extends DiscountType(code = 1, name = "商品無料")
    case IS_AMOUNT extends DiscountType(code = 2, name = "定額割引")

  /**
   * 提供状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_ARCHIVE   extends Status(code = -1) // 廃止:   新しい配布を止めた
    case IS_PREPARING extends Status(code =  0) // 準備中: 配布はまだ始まっていない
    case IS_ACTIVE    extends Status(code =  1) // 有効
