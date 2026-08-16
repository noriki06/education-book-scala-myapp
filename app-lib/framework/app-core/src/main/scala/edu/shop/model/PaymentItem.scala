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
 * 決済明細:
 * 会計で実際に請求した商品 1 行。
 * `product*` は請求した時点の商品内容を写したもの。
 *
 * 注文明細（[[OrderItem]]）は受渡しが終われば消える作業用のデータなので、
 * 何をいくらで売ったかはこちらに残す。
 * 本部が商品マスタ（[[Product]]）の名前や価格を後から変えても、
 * 過去の会計はこの行だけで再現できる。
 *
 * {{{
 *   productUnitPrice * productNum = productSubTotal
 *   billSubTotal = SUM(productSubTotal)
 * }}}
 */
import PaymentItem.*
case class PaymentItem(
  id:               Option[Id],          // 管理Id
  shopId:           Shop.Id,             // 店舗Id
  paymentId:        Payment.Id,          // 決済Id
  productId:        Product.Id,          // 商品: 商品Id
  productName:      String,              // 商品: 商品名
  productCategory:  Product.Category,    // 商品: カテゴリ
  productNum:       Int,                 // 商品: 個数
  productUnitPrice: Int,                 // 商品: 単品あたり (円)
  productSubTotal:  Int,                 // 商品: 小計 (円)
  updatedAt:        LocalDateTime = Now, // データ更新日
  createdAt:        LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

/**
 * 決済明細: 付随する型と処理の定義
 */
object PaymentItem:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, PaymentItem]
  type EmbeddedId = Entity.EmbeddedId[Id, PaymentItem]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
