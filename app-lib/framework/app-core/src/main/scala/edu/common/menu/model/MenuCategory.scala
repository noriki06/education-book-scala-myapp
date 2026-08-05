/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.menu.model

import ixias.core.model.*

/**
 * MenuCategory: a section of the menu (burgers, sides, drinks).
 *
 * Master data owned by the head office. Items hang off a category and are
 * listed by `sortOrder` inside it. A category has no lifecycle of its own —
 * retiring one means retiring the items under it, so there is no state here.
 */
import MenuCategory.*
case class MenuCategory(
  id:        Option[Id],           // 管理 ID（永続化前は None）
  name:      String,               // 表示名（バーガー / サイド / ドリンク）
  sortOrder: Short,                // 表示順（小さいほど先に並ぶ）
  updatedAt: LocalDateTime = Now,  // データ更新日
  createdAt: LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

object MenuCategory:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, MenuCategory]
  type EmbeddedId = Entity.EmbeddedId[Id, MenuCategory]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
