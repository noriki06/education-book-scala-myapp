/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * 販売テンプレートのメニュー: 注文画面のタブにあたる区分（グランド / 朝 / 夜 / 期間限定）。
 *
 * 販売できる期間を 2 つの枠で持つ。
 *
 *  - `dateStart` 〜 `dateEnd`  … 販売する日の範囲。両端を含む
 *  - `timeStart` ＋ `timeOpen` … その日のうち注文できる時間帯。終了時刻ではなく
 *    長さで持つので、深夜をまたぐ時間帯も 1 組で表せる
 *
 * どちらの枠も、片側が None ならその側に制限がない。4 つとも None なら常時販売。
 */
import SalesTemplateMenu.*
case class SalesTemplateMenu(
  id:         Option[Id],           // メニューId
  templateId: SalesTemplate.Id,     // 販売テンプレートId
  name:       String,               // メニュー名 (例: グランド / 朝 / 夜 / 春の期間限定)
  dateStart:  Option[LocalDate],    // 販売日: 開始
  dateEnd:    Option[LocalDate],    // 販売日: 終了
  timeStart:  Option[LocalTime],    // 販売時間: 開始
  timeOpen:   Option[Duration],     // 販売時間: 長さ。深夜またぎもこれで表す
  state:      Status,               // 公開状態
  sortOrder:  Short,                // タブの表示順
  updatedAt:  LocalDateTime = Now,  // データ更新日
  createdAt:  LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

object SalesTemplateMenu:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, SalesTemplateMenu]
  type EmbeddedId = Entity.EmbeddedId[Id, SalesTemplateMenu]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** 公開状態 */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_ARCHIVE extends Status(code = -1) // 取り下げ: 公開したものを下げた
    case IS_PLAN    extends Status(code = 0)  // 準備中: まだ公開していない
    case IS_PUBLIC  extends Status(code = 1)  // 公開
