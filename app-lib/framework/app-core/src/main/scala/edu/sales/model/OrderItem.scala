/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.sales.model

import ixias.core.model.*

import edu.common.menu.model.MenuItem
import edu.value.{ Money, Quantity }

/**
 * OrderItem: one line of a confirmed order.
 *
 * `name` and `unitPrice` are copied from the menu at the moment the order was
 * placed — a snapshot. Reading them back from MenuItem instead would rewrite
 * the amounts of past orders on every price revision, and would leave a
 * discontinued product with nothing to display in the history.
 *
 * `menuItemId` is still kept, but only to answer "which products sold well".
 * Nothing shown to the customer is read through it.
 */
import OrderItem.*
case class OrderItem(
  id:         Option[Id],          // 管理 ID（永続化前は None）
  orderId:    Order.Id,            // どの注文の行か
  menuItemId: MenuItem.Id,         // どの商品だったか（集計用。表示には使わない）
  name:       String,              // 注文時点の商品名
  unitPrice:  Money,               // 注文時点の単価（税抜）
  quantity:   Quantity,            // 数量
  updatedAt:  LocalDateTime = Now, // データ更新日
  createdAt:  LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]:

  /** The line amount, options excluded — derived, so it is not stored. */
  def subtotal: Money = unitPrice * quantity.value.toInt

object OrderItem:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, OrderItem]
  type EmbeddedId = Entity.EmbeddedId[Id, OrderItem]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
