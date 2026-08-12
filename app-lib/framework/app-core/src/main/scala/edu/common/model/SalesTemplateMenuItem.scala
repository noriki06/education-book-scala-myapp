/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * 販売テンプレートのメニューの表示アイテム
 * メニューに並ぶ商品と、その表示順。
 *
 * [[SalesTemplateMenu]] と [[Product]] の対応を表す。
 */
import SalesTemplateMenuItem.*
case class SalesTemplateMenuItem(
  id:             Option[Id],           // 管理Id
  templateId:     SalesTemplate.Id,     // 販売テンプレートId
  templateMenuId: SalesTemplateMenu.Id, // メニューId
  productId:      Product.Id,           // 商品Id
  sortOrder:      Short,                // 表示順
  updatedAt:      LocalDateTime = Now,  // データ更新日
  createdAt:      LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

object SalesTemplateMenuItem:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, SalesTemplateMenuItem]
  type EmbeddedId = Entity.EmbeddedId[Id, SalesTemplateMenuItem]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
