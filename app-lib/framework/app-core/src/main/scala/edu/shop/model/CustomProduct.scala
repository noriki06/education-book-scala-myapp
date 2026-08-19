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
 * 店舗独自商品: その店舗だけで販売する商品。
 */
import CustomProduct.*
case class CustomProduct(
  id:          Option[Id],           // 商品Id
  shopId:      Shop.Id,              // 店舗Id
  name:        String,               // 商品名
  category:    Product.Category,     // 商品: カテゴリ
  price:       Int,                  // 価格
  state:       Product.Status,       // 販売状態
  description: String,               // 説明文
  updatedAt:   LocalDateTime = Now,  // データ更新日
  createdAt:   LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

/**
 * 店舗独自商品: 付随する型と処理の定義
 */
object CustomProduct:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomProduct]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomProduct]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
