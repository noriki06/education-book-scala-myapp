/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*

/**
 * Payment: the record of settling one order through an external provider.
 *
 * We do not process money — we record what the provider told us. That is why
 * `transactionId` is here: it is the only handle that ties our order to their
 * ledger when the two disagree, and the first thing anyone asks for when a
 * customer says they were charged twice.
 *
 * It stays optional because the row exists before the provider is called.
 */
import Payment.*
case class Payment(
  id:            Option[Id],                       // 管理 ID（永続化前は None）
  orderId:       Order.Id,                         // どの注文の支払いか
  amount:        Int,                              // 請求金額（円・税込）
  transactionId: Option[String] = None,            // 決済サービスの取引 ID
  state:         Status         = Status.IS_UNPAID, // 決済の状態
  paidAt:        Option[LocalDateTime] = None,     // 決済完了日時
  updatedAt:     LocalDateTime  = Now,             // データ更新日
  createdAt:     LocalDateTime  = Now              // データ作成日
) extends EntityModel[Id]

object Payment:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Payment]
  type EmbeddedId = Entity.EmbeddedId[Id, Payment]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** Settlement status, as reported by the external provider. */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_FAILED   extends Status(code = -1) // 失敗
    case IS_UNPAID   extends Status(code =  1) // 未決済
    case IS_PAID     extends Status(code =  2) // 完了
    case IS_REFUNDED extends Status(code =  3) // 返金
