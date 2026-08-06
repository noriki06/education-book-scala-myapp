/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*

import edu.customer.model.User

/**
 * Order: a purchase that has been confirmed.
 *
 * It lives in `shop` because the shop is what moves it forward — accepted,
 * cooking, ready, handed over. The member creates it, but every state after
 * that is a staff action on the store tablet.
 *
 * `taxRate` is stored rather than read from configuration: it is the rate the
 * customer was actually charged at. When the rate changes, past orders must
 * keep theirs.
 *
 * `code` is what the counter calls out and what appears in URLs. The
 * auto-increment `id` never leaves the database: a sequential number in public
 * leaks how many orders exist and invites reading someone else's order by
 * subtracting one.
 */
import Order.*
case class Order(
  id:        Option[Id],            // 管理 ID（永続化前は None）
  uid:       User.Id,               // 顧客Id
  shopId:    Shop.Id,               // 店舗Id
  code:      Code,                  // 受付番号（公開用の識別子）
  taxRate:   BigDecimal,            // 消費税率 (0.1000 = 10%)
  state:     Status,                // 注文の状態
  pickupAt:  LocalDateTime,         // 受け取り予定時刻
  updatedAt: LocalDateTime = Now,   // データ更新日
  createdAt: LocalDateTime = Now    // データ作成日
) extends EntityModel[Id]

object Order:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type Code       = Code.Repr
  type WithNoId   = Entity.WithNoId[Id, Order]
  type EmbeddedId = Entity.EmbeddedId[Id, Order]

  // --[ Objects ]-----------------------------------------------------
  object Id   extends Entity.Id[Long]
  object Code extends Entity.Id[String]:
    def generate: Code = Code(java.util.UUID.randomUUID.toString.take(8).toUpperCase)

  // --[ Value Objects ]-----------------------------------------------
  /** Order status. The happy path runs 1 → 2 → 3 → 4; cancellation leaves it. */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CANCELED extends Status(code = -1) // キャンセル
    case IS_ACCEPTED extends Status(code =  1) // 受付
    case IS_COOKING  extends Status(code =  2) // 調理中
    case IS_READY    extends Status(code =  3) // 受取準備完了
    case IS_HANDED   extends Status(code =  4) // 受渡し完了
