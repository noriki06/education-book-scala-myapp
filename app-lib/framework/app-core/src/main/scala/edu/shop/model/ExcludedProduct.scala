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
 * 除外商品: この店舗が提供できない商品の登録。
 * メニューとは独立して除外商品を登録することで、
 * メニューに載っている商品でも除外できる。
 */
import ExcludedProduct.*
case class ExcludedProduct(
  id:        Option[Id],          // 管理Id
  shopId:    Shop.Id,             // 店舗Id
  productId: Product.Id,          // 除外する商品Id
  note:      Option[String],      // 理由の覚書
  updatedAt: LocalDateTime = Now, // データ更新日
  createdAt: LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

/**
 * 除外商品: 付随する型と処理の定義
 */
object ExcludedProduct:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, ExcludedProduct]
  type EmbeddedId = Entity.EmbeddedId[Id, ExcludedProduct]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
