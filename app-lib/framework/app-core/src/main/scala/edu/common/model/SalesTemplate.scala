/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * SalesTemplate: a reusable set of menus a shop can be assigned (default,
 * large store, small store).
 *
 * Head office builds a handful of these; every shop points at exactly one.
 * Without it, opening a store would mean re-entering the whole menu structure,
 * and a chain-wide change would mean editing every shop.
 *
 * A shop that cannot handle part of its template does not get its own copy —
 * it registers exclusions ([[edu.shop.model.ExcludedMenu]]). Templates
 * stay few; the differences stay small and visible.
 */
import SalesTemplate.*
case class SalesTemplate(
  id:        Option[Id],          // 管理Id
  name:      String,              // テンプレート名 (例: デフォルト / 大型店舗 / 小型店舗)
  state:     Status,              // 提供状態
  note:      String,              // 説明文
  updatedAt: LocalDateTime = Now, // データ更新日
  createdAt: LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

object SalesTemplate:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, SalesTemplate]
  type EmbeddedId = Entity.EmbeddedId[Id, SalesTemplate]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** 提供状態: 廃止しても店舗が参照したままなので、行は消さない */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_ARCHIVE extends Status(code = -1) // 廃止: 新しい割り当てを止めた
    case IS_ACTIVE  extends Status(code =  1) // 有効
