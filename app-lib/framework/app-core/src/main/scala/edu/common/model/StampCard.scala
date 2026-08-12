/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * スタンプ台帳: スタンプカードを定義する本部マスタ。
 * `issueStampNum` 個たまると [[Coupon]] 1 枚に引き換えられる。
 */
import StampCard.*
case class StampCard(
  id:            Option[Id],          // 台帳Id
  name:          String,              // 台帳名 (例: 2026 春のスタンプカード)
  rule:          Rule,                // 付与ルールの種類
  issueCouponId: Coupon.Id,           // 引換: 発行するクーポンId
  issueStampNum: Int,                 // 引換: 必要スタンプ数
  dateStart:     Option[LocalDate],   // 配布: 開始日
  dateEnd:       Option[LocalDate],   // 配布: 終了日。当日を含む
  state:         Status,              // 提供状態
  updatedAt:     LocalDateTime = Now, // データ更新日
  createdAt:     LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

object StampCard:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, StampCard]
  type EmbeddedId = Entity.EmbeddedId[Id, StampCard]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** 付与ルール */
  enum Rule(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_PAYMENT_COUNT  extends Rule(code = 1, name = "会計回数")
    case IS_PAYMENT_AMOUNT extends Rule(code = 2, name = "会計金額")

  /** 提供状態 */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_ARCHIVE extends Status(code = -1) // 廃止
    case IS_ACTIVE  extends Status(code =  1) // 有効
