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
 * Payment: what an order was actually charged.
 *
 * The amounts live here rather than on [[Order]] because they are a different
 * kind of fact. An order records what was asked for; a payment records what was
 * charged, at the tax rate and the prices of that moment. A price revision or a
 * tax change must never move a figure on a confirmed payment.
 *
 * Two groups of figures. `bill*` is the breakdown as it was calculated; `pay*`
 * is what actually moved. Every one of them is stored, including the parts
 * that look derivable — they are the numbers printed on the receipt, and
 * rounding rules change. `billTaxTotal` matters most: it is the rounded
 * result, not the rate applied a second time.
 *
 * The figures hold this relationship, which the type cannot enforce:
 *
 * {{{
 *   billTaxTotal   = round((billSubTotal - billDiscountTotal) * billTaxRate)
 *   payBilledTotal = billSubTotal - billDiscountTotal + billTaxTotal
 *   0 <= payRefundedTotal <= payBilledTotal
 * }}}
 *
 * That places the discount before tax — the customer is taxed on what they
 * actually pay for. Discounting after tax would give a different total. Note
 * that the sum of the breakdown lands in `payBilledTotal`: the `bill*` group
 * holds the parts, and the first figure that counts as actual is a `pay*` one.
 *
 * Sales and refunds are separate accounts, so the two are never netted into
 * one column. `payBilledTotal` is fixed once the settlement completes and a
 * later refund does not touch it; the refund lands in `payRefundedTotal` with
 * its own date. A January sale refunded in March must leave January alone.
 *
 * {{{
 *   売上 = SUM(pay_billed_total)   WHERE state IN (IS_COMPLETED, IS_REFUNDED)
 *   返金 = SUM(pay_refunded_total) -- payRefundedAt の月で集計する
 * }}}
 *
 * Only one refund fits. A second partial refund has nowhere to record its own
 * date, and that is the point where refunds become their own entity.
 *
 * An order has many payment rows, not one. A declined card is not corrected in
 * place: the failed row stays as the record of what was attempted, and the
 * customer picking a different method creates a new row. Of all the rows
 * against one order, at most one ever reaches IS_COMPLETED.
 *
 * That means no unique constraint on `orderId` — "only one live payment" is a
 * rule the application keeps, not the database. Overwriting the failed row
 * instead would have bought the constraint at the price of the history, and
 * the history is what answers "why was this customer charged twice".
 *
 * `billTaxRate` is the rate this settlement was closed at, kept for the books.
 * It is a record of a moment, not a lookup — a later rate change leaves it
 * alone.
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

object Payment:

  // --[ Typedefs ]----------------------------------------------------
  type Id            = Id.Repr
  type TransactionId = TransactionId.Repr
  type WithNoId      = Entity.WithNoId[Id, Payment]
  type EmbeddedId    = Entity.EmbeddedId[Id, Payment]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  /**
   * 決済サービス側の取引Id。
   *
   * こちらの記録と向こうの台帳を突き合わせる唯一の手がかりで、「二重に引き落と
   * された」という問い合わせで最初に聞かれる値。素の String と混ざらないよう
   * 専用の型にする。
   *
   * 決済サービスを呼ぶ前に行を作るため Option。通信が切れて結果が分からなく
   * なっても、「呼んだ形跡」が残る。現金決済では最後まで None のまま。
   */
  object TransactionId extends Entity.Id[String]

  // --[ Value Objects ]-----------------------------------------------
  /** 決済手段: 区分値なので 0 から振る */
  enum Method(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_CASH   extends Method(code = 0, name = "現金")
    case IS_CREDIT extends Method(code = 1, name = "クレジットカード")
    case IS_QR     extends Method(code = 2, name = "QRコード決済")

  /**
   * 決済の状態。
   *
   * 正の側が正常な進行（下書き → 処理中 → 完了）、負の側がそこから外れた
   * 終わり方。**失敗した行は書き換えず残す** ——支払い方法を変えて再試行する
   * ときは新しい行を作る。1 注文に複数行が並び、IS_COMPLETED に到達するのは
   * 最大 1 行。
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_REFUNDED  extends Status(code = -3) // 返金済: 完了後に返した
    case IS_CANCELED  extends Status(code = -2) // 取消:   決済前に注文がキャンセルされた
    case IS_FAILED    extends Status(code = -1) // 失敗:   決済サービスが拒否。再試行は新しい行
    case IS_DRAFT     extends Status(code =  1) // 下書き: 決済サービスをまだ呼んでいない
    case IS_PENDING   extends Status(code =  2) // 処理中: 決済サービスの応答待ち
    case IS_COMPLETED extends Status(code =  3) // 完了:   入金確定
