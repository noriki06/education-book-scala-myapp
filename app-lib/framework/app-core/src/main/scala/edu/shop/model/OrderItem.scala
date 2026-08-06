/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*

import edu.common.model.Product
import edu.customer.model.User

/**
 * OrderItem: one line of a confirmed order.
 *
 * This line currently holds only a reference to the product. It carries no
 * copy of the name or the price it was charged at, so displaying a past order
 * means reading today's [[edu.common.model.Product]] — and a price revision
 * silently rewrites what past orders appear to have cost.
 */
import OrderItem.*
case class OrderItem(
  id:         Option[Id],          // 管理Id
  shopId:     Shop.Id,             // 店舗Id
  orderId:    Order.Id,            // オーダーId
  uid:        User.Id,             // 顧客Id
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
