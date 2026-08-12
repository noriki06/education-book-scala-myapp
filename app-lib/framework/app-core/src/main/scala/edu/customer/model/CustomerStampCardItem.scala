/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*

import edu.common.model.StampCard
import edu.shop.model.Payment

/**
 * CustomerStampCardItem: one stamp on a card.
 *
 * The unit is not named again — the card already says what these are, the same
 * way an order line is `OrderItem` and not `OrderProduct`.
 *
 * Both ancestors are kept — the ledger and the member's card — the same way an
 * order line carries `shopId` beside `orderId`. Counting a campaign's stamps
 * then needs no join, and the pair reads as one rule: `customer*` is the one
 * somebody is holding, as with `customerCouponId`.
 *
 * This file is why the two cards keep different names. Drop the prefix and
 * both fields below want to be called `cardId`, with nothing but a comment
 * telling the ledger from the card in someone's hand.
 *
 * Each row records the payment that earned it, so "when and on which bill was
 * this stamp given" can be answered later. That is also the guard against
 * pressing twice for one payment: the pair of card and payment is unique.
 */
import CustomerStampCardItem.*
case class CustomerStampCardItem(
  id:             Option[Id],           // 管理Id
  cardId:         StampCard.Id,         // 台帳Id
  customerId:     Customer.Id,          // 顧客Id
  customerCardId: CustomerStampCard.Id, // 顧客: 保有カードId
  paymentId:      Payment.Id,           // どの会計で押されたか
  updatedAt:      LocalDateTime = Now,  // データ更新日
  createdAt:      LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

object CustomerStampCardItem:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerStampCardItem]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerStampCardItem]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
