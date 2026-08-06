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
 * ShopExclusionMenu: a menu in the shop's template that this shop does not run.
 *
 * A negative list. Recording what a shop *cannot* do takes a handful of rows;
 * recording what it *can* would take one row per shop per menu, rewritten
 * every time head office adds a menu.
 *
 * A row may be registered before the menu goes public, so a shop can decline a
 * seasonal menu in advance. Nothing here checks that the menu belongs to the
 * shop's current template — reassigning a template leaves stale exclusions
 * that are harmless but worth cleaning up.
 */
import ShopExclusionMenu.*
case class ShopExclusionMenu(
  id:        Option[Id],           // 管理Id
  shopId:    Shop.Id,              // 店舗Id
  menuId:    SalesTemplateMenu.Id, // 除外するメニューId
  note:      Option[String],       // 理由の覚書。判定には使わない
  updatedAt: LocalDateTime = Now,  // データ更新日
  createdAt: LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

object ShopExclusionMenu:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, ShopExclusionMenu]
  type EmbeddedId = Entity.EmbeddedId[Id, ShopExclusionMenu]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
