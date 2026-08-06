/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * CouponIssue: one offering of a [[Coupon]] — when it is available, to whom,
 * and how many.
 *
 * Every way of handing out a discount goes through here, so there is one place
 * to ask "can this be picked up right now".
 *
 *  - `code` is None — the coupon appears in the app's list and anyone may take it
 *  - `code` is Some — it is only reachable by entering that code
 *
 * `issueLimit` caps how many may be taken; None is unlimited. The count of
 * takers is the number of [[edu.customer.model.CustomerCoupon]] rows against
 * this issue, never a counter kept here — a counter and its rows drift apart
 * the first time something fails halfway.
 *
 * A direct-consumption issue produces no CustomerCoupon rows at all, so a cap
 * on one is meaningless; that combination is rejected on creation.
 */
import CouponIssue.*
case class CouponIssue(
  id:         Option[Id],          // 配布Id
  couponId:   Coupon.Id,           // クーポンId
  useType:    UseType,             // 消費形態: 直接消費 / 付与消費
  offerCode:  Option[Code],        // クーポンコード。None は一覧配布
  offerLimit: Option[Int],         // 発行上限。None は無制限
  dateStart:  Option[LocalDate],   // 配布: 開始日
  dateEnd:    Option[LocalDate],   // 配布: 終了日
  state:      Status,              // 配布状態
  updatedAt:  LocalDateTime = Now, // データ更新日
  createdAt:  LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

object CouponIssue:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type Code       = Code.Repr
  type WithNoId   = Entity.WithNoId[Id, CouponIssue]
  type EmbeddedId = Entity.EmbeddedId[Id, CouponIssue]

  // --[ Objects ]-----------------------------------------------------
  object Id   extends Entity.Id[Long]
  object Code extends Entity.Id[String]

  // --[ Value Objects ]-----------------------------------------------
  /** 消費形態: 取得を挟むかどうか */
  enum UseType(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_DIRECT  extends UseType(code = 1, name = "直接消費")  // 取得せずカートで使う
    case IS_GRANTED extends UseType(code = 2, name = "付与消費")  // 取得してから使う

  /** 配布状態 */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CLOSED extends Status(code = -1) // 停止: 配布を打ち切った
    case IS_OPEN   extends Status(code =  1) // 配布中
