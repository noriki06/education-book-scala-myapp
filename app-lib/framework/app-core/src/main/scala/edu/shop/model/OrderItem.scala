/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*

import edu.common.model.Product
import edu.customer.model.Customer

/**
 * OrderItem: one line of a confirmed order — what was asked for, not what it
 * cost.
 *
 * The line keeps a reference and a count, nothing else. Money is not recorded
 * per line because it does not need to be: [[Payment]] freezes the subtotal,
 * the tax and the billed total at settlement, so a later price revision cannot
 * move what the customer was charged.
 *
 * What that costs is the per-line breakdown. A receipt reprinted next year can
 * still show the correct total, but the amount beside each line has to be read
 * from today's [[Product]], and after a revision those amounts will no longer
 * add up to the total. Recording `productName` and `unitPrice` here is what
 * makes a line reproducible; it is deliberately not done while the total is
 * the only figure the business asks to reproduce.
 */
import OrderItem.*
case class OrderItem(
  id:         Option[Id],          // 管理Id
  shopId:     Shop.Id,             // 店舗Id
  orderId:    Order.Id,            // オーダーId
  customerId: Customer.Id,         // 顧客Id
  productId:  Product.Id,          // 商品Id
  productNum: Int,                 // 注文数
  updatedAt:  LocalDateTime = Now, // データ更新日
  createdAt:  LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

object OrderItem:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, OrderItem]
  type EmbeddedId = Entity.EmbeddedId[Id, OrderItem]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
