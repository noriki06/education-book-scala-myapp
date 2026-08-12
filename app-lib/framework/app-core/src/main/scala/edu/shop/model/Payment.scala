/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.customer.model.Customer

/**
 * 決済: 注文に対して実際に請求した記録。
 *
 * 金額は 2 つのグループに分かれる。
 *   `bill*` は計算した内訳
 *   `pay*`  は実際に動いた額。
 *
 * {{{
 *   billTaxTotal   = round((billSubTotal - billDiscountTotal) * billTaxRate)
 *   payBilledTotal = billSubTotal - billDiscountTotal + billTaxTotal
 *   0 <= payRefundedTotal <= payBilledTotal
 * }}}
 */
import Payment.*
case class Payment(
  id:                Option[Id],              // 決済Id
  shopId:            Shop.Id,                 // 店舗Id
  orderId:           Order.Id,                // オーダーId
  customerId:        Customer.Id,             // 顧客Id
  method:            Method,                  // 決済手段
  transactionId:     Option[TransactionId],   // 決済トランザクションId
  billSubTotal:      Int,                     // 金額: 税抜合計 (円)
  billDiscountTotal: Int,                     // 金額: 割引合計 (円)
  billTaxRate:       BigDecimal,              // 金額: 消費税率 (0.1000 = 10%)
  billTaxTotal:      Int,                     // 金額: 消費税額 (円)
  payBilledTotal:    Int,                     // 実績: 請求した額 (円)
  payRefundedTotal:  Int,                     // 実績: 返金した額 (円)
  state:             Status,                  // 決済の状態
  note:              Option[String],          // 備考
  completedAt:       Option[LocalDateTime],   // 決済日時
  refundedAt:        Option[LocalDateTime],   // 返金日時
  updatedAt:         LocalDateTime = Now,     // データ更新日
  createdAt:         LocalDateTime = Now      // データ作成日
) extends EntityModel[Id]

/**
 * 決済: 付随する型と処理の定義
 */
object Payment:

  // --[ Type Aliases ]------------------------------------------------
  type Id            = Id.Repr
  type TransactionId = TransactionId.Repr
  type WithNoId      = Entity.WithNoId[Id, Payment]
  type EmbeddedId    = Entity.EmbeddedId[Id, Payment]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  /**
   * 決済サービス側の取引Id
   */
  object TransactionId extends Entity.Id[String]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 決済手段
   */
  enum Method(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_CASH   extends Method(code = 1, name = "現金")
    case IS_CREDIT extends Method(code = 2, name = "クレジットカード")
    case IS_QR     extends Method(code = 3, name = "QRコード決済")

  /**
   * 決済の状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_REFUNDED  extends Status(code = -3) // 返金済: 完了後に返した
    case IS_CANCELED  extends Status(code = -2) // 取消:   決済前に注文がキャンセルされた
    case IS_FAILED    extends Status(code = -1) // 失敗:   決済サービスが拒否。再試行は新しい行
    case IS_DRAFT     extends Status(code =  0) // 下書き: 決済サービスをまだ呼んでいない
    case IS_PENDING   extends Status(code =  1) // 処理中: 決済サービスの応答待ち
    case IS_COMPLETED extends Status(code =  2) // 完了:   入金確定
