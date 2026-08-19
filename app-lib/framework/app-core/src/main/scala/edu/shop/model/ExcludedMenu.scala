/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.common.model.{ SalesTemplate, SalesTemplateMenu }

/**
 * 除外メニュー: この店舗が運用しないメニューの登録。
 * `templateId` は登録した時点の販売テンプレート。
 */
import ExcludedMenu.*
case class ExcludedMenu(
  id:             Option[Id],           // 管理Id
  shopId:         Shop.Id,              // 店舗Id
  templateId:     SalesTemplate.Id,     // 販売テンプレートId
  templateMenuId: SalesTemplateMenu.Id, // メニューId
  note:           Option[String],       // 理由の覚書。判定には使わない
  updatedAt:      LocalDateTime = Now,  // データ更新日
  createdAt:      LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

/**
 * 除外メニュー: 付随する型と処理の定義
 */
object ExcludedMenu:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, ExcludedMenu]
  type EmbeddedId = Entity.EmbeddedId[Id, ExcludedMenu]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
