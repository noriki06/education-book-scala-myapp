/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.sales.model

import ixias.core.model.*

import edu.common.menu.model.MenuItem
import edu.value.Quantity

/**
 * CartItem: one line in a cart.
 *
 * It keeps a reference and nothing else — no name, no price. A cart is a
 * draft of a purchase that has not happened yet, so if the head office raises
 * a price tonight, the cart must show tomorrow's price in the morning.
 *
 * Compare with [[OrderItem]], which copies name and price instead. Same word,
 * opposite design, and the reason is which moment the line describes.
 */
import CartItem.*
case class CartItem(
  id:         Option[Id],          // 管理 ID（永続化前は None）
  cartId:     Cart.Id,             // どのカートの行か
  menuItemId: MenuItem.Id,         // どの商品か（名前と価格は都度引く）
  quantity:   Quantity,            // 数量
  updatedAt:  LocalDateTime = Now, // データ更新日
  createdAt:  LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

object CartItem:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CartItem]
  type EmbeddedId = Entity.EmbeddedId[Id, CartItem]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
