/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*

/**
 * MenuOptionGroup: a set of choices offered together (size, toppings).
 *
 * The group carries the rule — pick exactly one or pick any number, and
 * whether picking is mandatory. The choices themselves are MenuOptionItem.
 */
import MenuOptionGroup.*
case class MenuOptionGroup(
  id:         Option[Id],           // 管理 ID（永続化前は None）
  name:       String,               // 表示名（サイズ / トッピング）
  selection:  Selection,            // 単一選択か複数選択か
  isRequired: Boolean,              // 選択が必須か
  updatedAt:  LocalDateTime = Now,  // データ更新日
  createdAt:  LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

object MenuOptionGroup:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, MenuOptionGroup]
  type EmbeddedId = Entity.EmbeddedId[Id, MenuOptionGroup]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** How many choices the customer may pick. A classification, not a state. */
  enum Selection(val code: Short) extends EnumStatus[Short]:
    case IS_SINGLE   extends Selection(code = 0) // 1 つだけ選ぶ（サイズ）
    case IS_MULTIPLE extends Selection(code = 1) // いくつでも選べる（トッピング）
