/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * SalesTemplateMenuItem: which products a menu lists, and in what order.
 *
 * The crossing of [[SalesTemplateMenu]] and [[Product]]. One product appears
 * in several menus (a burger in both the grand menu and a seasonal one) and a
 * menu holds many products, so the pairing is its own entity.
 *
 * `sortOrder` belongs here rather than on the product: the same burger may be
 * shown first in the seasonal menu and fifth in the grand menu. The order is a
 * property of the pairing.
 *
 * A shop that cannot serve one of these products does not edit the template —
 * it registers [[edu.shop.model.ExcludedProduct]].
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

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, SalesTemplateMenuItem]
  type EmbeddedId = Entity.EmbeddedId[Id, SalesTemplateMenuItem]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
