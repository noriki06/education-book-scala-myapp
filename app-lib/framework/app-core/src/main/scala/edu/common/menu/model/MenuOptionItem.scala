/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.menu.model

import ixias.core.model.*

import edu.value.Money

/**
 * MenuOptionItem: one choice inside a group (size M, no pickles, extra patty).
 *
 * `extraPrice` is added on top of the item price. "No pickles" is simply a
 * choice priced at zero — the model does not need a separate notion of a
 * removal.
 */
import MenuOptionItem.*
case class MenuOptionItem(
  id:         Option[Id],                        // 管理 ID（永続化前は None）
  groupId:    MenuOptionGroup.Id,                // 属するオプショングループ
  name:       String,                            // 表示名（M サイズ / ピクルス抜き）
  extraPrice: Money,                             // 追加料金（税抜）。無料なら Money.Zero
  sortOrder:  Short,                             // グループ内の表示順
  state:      Status        = Status.IS_AVAILABLE, // 提供状態
  updatedAt:  LocalDateTime = Now,               // データ更新日
  createdAt:  LocalDateTime = Now                // データ作成日
) extends EntityModel[Id]

object MenuOptionItem:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, MenuOptionItem]
  type EmbeddedId = Entity.EmbeddedId[Id, MenuOptionItem]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** Whether the head office still offers this choice at all. */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_UNAVAILABLE extends Status(code = -1) // 提供不可
    case IS_AVAILABLE   extends Status(code =  1) // 提供可
