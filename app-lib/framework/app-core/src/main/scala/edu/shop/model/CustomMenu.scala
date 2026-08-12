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
 * 店舗独自メニュー: 店舗が自分で立てるタブ。
 *
 * [[SalesTemplateMenu]] とほぼ同じ形だが、テンプレートに属さないので
 * `templateId` を持たない。販売期間の 2 つの枠の扱いは本部メニューと同じ。
 */
import CustomMenu.*
case class CustomMenu(
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

object CustomMenu:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomMenu]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomMenu]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
