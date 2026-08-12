/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * 販売テンプレート: 店舗に割り当てるメニュー構成のひな型。
 *
 * 本部がいくつか用意し、各店舗はこのうち 1 つを参照する。
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

/**
 * 販売テンプレート: 付随する型と処理の定義
 */
object SalesTemplate:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, SalesTemplate]
  type EmbeddedId = Entity.EmbeddedId[Id, SalesTemplate]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 提供状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_ARCHIVE extends Status(code = -1) // 廃止: 新しい割り当てを止めた
    case IS_ACTIVE  extends Status(code =  1) // 有効
