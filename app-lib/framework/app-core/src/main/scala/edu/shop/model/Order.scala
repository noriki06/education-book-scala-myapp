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
 * 注文: 確定した注文。
 * 金額は持たず、請求額の内訳は [[Payment]] が保持する。
 * `code` は外部に見せる受付番号。
 */
import Order.*
case class Order(
  id:         Option[Id],             // 管理Id
  shopId:     Shop.Id,                // 店舗Id
  customerId: Customer.Id,            // 顧客Id
  code:       Code,                   // 受付番号（公開用の識別子）
  state:      Status,                 // 注文の状態
  pickupAt:   LocalDateTime,          // 受取: 予定時刻
  pickupedAt: Option[LocalDateTime],  // 受取: 完了時刻。IS_HANDED で埋まる
  updatedAt:  LocalDateTime = Now,    // データ更新日
  createdAt:  LocalDateTime = Now     // データ作成日
) extends EntityModel[Id]

object Order:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type Code       = Code.Repr
  type WithNoId   = Entity.WithNoId[Id, Order]
  type EmbeddedId = Entity.EmbeddedId[Id, Order]

  // --[ Opaque Values ]-----------------------------------------------
  object Id   extends Entity.Id[Long]
  object Code extends Entity.Id[String]:
    def generate: Code = Code(java.util.UUID.randomUUID.toString.take(8).toUpperCase)

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 注文の状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CANCELED extends Status(code = -1) // キャンセル
    case IS_ACCEPTED extends Status(code =  1) // 受付
    case IS_COOKING  extends Status(code =  2) // 調理中
    case IS_READY    extends Status(code =  3) // 受取準備完了
    case IS_HANDED   extends Status(code =  4) // 受渡し完了
