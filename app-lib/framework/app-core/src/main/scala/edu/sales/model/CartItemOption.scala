/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.sales.model

import ixias.core.model.*

import edu.common.menu.model.MenuOptionItem

/**
 * CartItemOption: one choice attached to a cart line (size L, extra patty).
 *
 * A reference only, for the same reason as [[CartItem]] — the surcharge is
 * read from the master every time the cart is priced.
 */
import CartItemOption.*
case class CartItemOption(
  id:               Option[Id],          // 管理 ID（永続化前は None）
  cartItemId:       CartItem.Id,         // どのカート明細に付くか
  menuOptionItemId: MenuOptionItem.Id,   // どの選択肢か（追加料金は都度引く）
  updatedAt:        LocalDateTime = Now, // データ更新日
  createdAt:        LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

object CartItemOption:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CartItemOption]
  type EmbeddedId = Entity.EmbeddedId[Id, CartItemOption]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
