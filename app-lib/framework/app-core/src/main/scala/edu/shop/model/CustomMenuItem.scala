/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.common.model.Product

/**
 * 店舗独自メニューの表示アイテム
 * 店舗独自タブに並ぶ商品。
 *
 * 本部商品と店舗独自商品のどちらも載るため参照を 2 つ持ち、
 * どちらか一方だけがSome になる
 *  - `productId`       … 本部商品（[[Product]]）
 *  - `productCustomId` … 店舗独自商品（[[CustomProduct]]）
 */
import CustomMenuItem.*
case class CustomMenuItem(
  id:              Option[Id],               // 管理Id
  shopId:          Shop.Id,                  // 店舗Id
  menuId:          CustomMenu.Id,            // メニューId
  productId:       Option[Product.Id],       // 商品: 本部商品Id
  productCustomId: Option[CustomProduct.Id], // 商品: 店舗独自商品Id
  sortOrder:       Short,                    // 表示順
  updatedAt:       LocalDateTime = Now,      // データ更新日
  createdAt:       LocalDateTime = Now       // データ作成日
) extends EntityModel[Id]

object CustomMenuItem:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomMenuItem]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomMenuItem]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
