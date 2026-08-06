/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*

/**
 * MenuItemOptionGroup: which option groups an item offers (many-to-many).
 *
 * The same "size" group is reused by every burger, so the pairing lives in its
 * own entity rather than on either side. `sortOrder` belongs here, not on the
 * group: the order in which choices are shown is a property of the pairing —
 * one item may ask for size first, another for toppings first.
 */
import MenuItemOptionGroup.*
case class MenuItemOptionGroup(
  id:        Option[Id],           // 管理 ID（永続化前は None）
  itemId:    MenuItem.Id,          // どの商品に
  groupId:   MenuOptionGroup.Id,   // どのオプショングループが付くか
  sortOrder: Short,                // その商品での表示順
  updatedAt: LocalDateTime = Now,  // データ更新日
  createdAt: LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

object MenuItemOptionGroup:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, MenuItemOptionGroup]
  type EmbeddedId = Entity.EmbeddedId[Id, MenuItemOptionGroup]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
