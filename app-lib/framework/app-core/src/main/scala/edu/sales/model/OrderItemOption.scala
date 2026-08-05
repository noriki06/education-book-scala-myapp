/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.sales.model

import ixias.core.model.*

import edu.common.menu.model.MenuOptionItem
import edu.value.Money

/**
 * OrderItemOption: one choice attached to an order line.
 *
 * Snapshots the label and the surcharge, exactly as [[OrderItem]] does. A
 * receipt that reads "extra patty +100" must still read +100 next year, even
 * if the surcharge is 150 by then.
 */
import OrderItemOption.*
case class OrderItemOption(
  id:               Option[Id],          // 管理 ID（永続化前は None）
  orderItemId:      OrderItem.Id,        // どの注文明細に付くか
  menuOptionItemId: MenuOptionItem.Id,   // どの選択肢だったか（集計用）
  name:             String,              // 注文時点の表示名
  extraPrice:       Money,               // 注文時点の追加料金（税抜）
  updatedAt:        LocalDateTime = Now, // データ更新日
  createdAt:        LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

object OrderItemOption:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, OrderItemOption]
  type EmbeddedId = Entity.EmbeddedId[Id, OrderItemOption]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
