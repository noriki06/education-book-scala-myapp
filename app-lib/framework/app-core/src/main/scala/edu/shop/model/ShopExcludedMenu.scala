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
 * ShopExcludedMenu: a menu in the shop's template that this shop does not run.
 *
 * A negative list. Recording what a shop *cannot* do takes a handful of rows;
 * recording what it *can* would take one row per shop per menu, rewritten
 * every time head office adds a menu.
 *
 * A row may be registered before the menu goes public, so a shop can decline a
 * seasonal menu in advance.
 *
 * `templateId` rides along with the menu, as it does on
 * [[edu.common.model.SalesTemplateMenuItem]]. It is the ancestor the row was
 * written against, which is what makes a stale exclusion visible: move a shop
 * to another template and its old rows still name the one they were meant for,
 * so they can be found and cleaned up instead of silently applying to a menu
 * nobody chose. Nothing here enforces the pair — the menu it names is the one
 * that matters at read time.
 */
import ShopExcludedMenu.*
case class ShopExcludedMenu(
  id:             Option[Id],           // 管理Id
  shopId:         Shop.Id,              // 店舗Id
  templateId:     SalesTemplate.Id,     // 販売テンプレートId
  templateMenuId: SalesTemplateMenu.Id, // メニューId
  note:           Option[String],       // 理由の覚書。判定には使わない
  updatedAt:      LocalDateTime = Now,  // データ更新日
  createdAt:      LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

object ShopExcludedMenu:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, ShopExcludedMenu]
  type EmbeddedId = Entity.EmbeddedId[Id, ShopExcludedMenu]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
