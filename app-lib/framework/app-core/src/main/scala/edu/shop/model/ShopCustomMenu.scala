/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*

import edu.common.model.SalesTemplateMenu

/**
 * ShopCustomMenu: a menu tab this shop runs on its own.
 *
 * Same shape as [[SalesTemplateMenu]] with one structural difference: no
 * `templateId`. A head office menu reaches a shop because the shop runs that
 * template; this one belongs to the shop directly, so head office reassigning
 * a template leaves it untouched.
 *
 * `sortOrder` competes in the same list as the head office tabs, which is what
 * lets a shop slot its own tab between two of theirs rather than always after.
 *
 * `state` reuses [[SalesTemplateMenu.Status]] so both kinds of tab publish and
 * archive on the same three codes, and the app needs one mapping instead of
 * two that drift.
 *
 * The date and time windows work exactly as on the head office menu: either
 * side of either may be None, and all four None is a permanent, all-day tab.
 */
import ShopCustomMenu.*
case class ShopCustomMenu(
  id:        Option[Id],                // メニューId
  shopId:    Shop.Id,                   // 店舗Id
  name:      String,                    // メニュー名 (例: 当店限定)
  dateStart: Option[LocalDate],         // 販売日: 開始
  dateEnd:   Option[LocalDate],         // 販売日: 終了
  timeStart: Option[LocalTime],         // 販売時間: 開始
  timeOpen:  Option[Duration],          // 販売時間: 長さ。深夜またぎもこれで表す
  state:     SalesTemplateMenu.Status,  // 公開状態
  sortOrder: Short,                     // タブの表示順
  updatedAt: LocalDateTime = Now,       // データ更新日
  createdAt: LocalDateTime = Now        // データ作成日
) extends EntityModel[Id]

object ShopCustomMenu:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, ShopCustomMenu]
  type EmbeddedId = Entity.EmbeddedId[Id, ShopCustomMenu]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
