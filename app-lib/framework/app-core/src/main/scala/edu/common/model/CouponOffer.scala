/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * クーポン配布: [[Coupon]] を「いつ・誰に・何枚まで」配るかの設定。
 * 1 つのクーポンに対して複数の配布を持てる。
 *
 *  - `promoCode`  … None はアプリの一覧配布、Some はコード入力による配布
 *  - `promoLimit` … 配布上限。None は無制限
 */
import CouponOffer.*
case class CouponOffer(
  id:         Option[Id],          // 配布Id
  couponId:   Coupon.Id,           // クーポンId
  issueType:  IssueType,           // 発行形態: 直接消費 / 付与消費
  promoCode:  Option[Code],        // プロモコード。None は一覧配布
  promoLimit: Option[Int],         // 配布上限。None は無制限
  dateStart:  Option[LocalDate],   // 配布: 開始日
  dateEnd:    Option[LocalDate],   // 配布: 終了日
  state:      Status,              // 配布状態
  updatedAt:  LocalDateTime = Now, // データ更新日
  createdAt:  LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

object CouponOffer:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type Code       = Code.Repr
  type WithNoId   = Entity.WithNoId[Id, CouponOffer]
  type EmbeddedId = Entity.EmbeddedId[Id, CouponOffer]

  // --[ Objects ]-----------------------------------------------------
  object Id   extends Entity.Id[Long]
  object Code extends Entity.Id[String]

  // --[ Value Objects ]-----------------------------------------------
  /** 発行形態 */
  enum IssueType(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_DIRECT  extends IssueType(code = 1, name = "直接消費")  // 発行せずカートで使う
    case IS_GRANTED extends IssueType(code = 2, name = "付与消費")  // 発行して保有してから使う

  /** 配布状態 */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CLOSED extends Status(code = -1) // 停止: 配布を打ち切った
    case IS_OPEN   extends Status(code =  1) // 配布中
