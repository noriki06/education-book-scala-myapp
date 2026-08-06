/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * SalesTemplateMenu: one menu inside a template (grand, morning, night,
 * seasonal) — the tabs across the top of the ordering screen.
 *
 * A limited-time menu is not a separate concept: it is a menu with a bounded
 * window. Modelling a campaign apart would duplicate the listing, the ordering
 * and the screen for something that differs only by having an end.
 *
 * When it is sold is two independent windows, written differently on purpose:
 *
 *  - `salesStartDate` .. `salesEndDate` — on which days the menu is offered
 *    ("the spring menu runs 3/1 to 5/31"). A pair of dates, both ends
 *    inclusive, because that is how the business says it.
 *  - `salesStartTime` ＋ `salesDuration` — within those days, the hours it can
 *    be ordered ("breakfast from 5:00 for 5h30m"). A length, not an end time,
 *    so a window crossing midnight (22:00 for 4h) stays one value and cannot
 *    be written the wrong way round. Same reason as
 *    [[edu.shop.model.ShopBusinessHour]].
 *
 * Either side of either window may be None — no limit on that side. All four
 * None is a permanent, all-day menu.
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
