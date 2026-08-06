/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * Product: one thing on sale (a burger, a drink, a set).
 *
 * A set is a Product too, holding the products it contains in `subItem`. That
 * keeps pricing, availability and display on one path instead of two.
 *
 * `price` is the current price. An order never reads it back: an order line
 * copies the price it was charged at ([[edu.customer.model.OrderItem]]), so a
 * price revision here can never rewrite past orders.
 *
 * Named Product, not MenuItem: `*Item` is the suffix for a line — a cart line,
 * an order line, a line on a menu ([[SalesTemplateMenuItem]]). Using it for the
 * thing being sold as well would make `Item` mean two things in one file.
 */
import Product.*
case class Product(
  id:          Option[Id],           // 商品Id
  name:        String,               // 商品名
  category:    Category,             // 商品: カテゴリ
  subItem:     Seq[Product.Id],      // セットメニュー: 含まれる商品Id
  price:       Int,                  // 価格
  state:       Status,               // 販売状態
  description: String,               // 説明文
  updatedAt:   LocalDateTime = Now,  // データ更新日
  createdAt:   LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

object Product:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Product]
  type EmbeddedId = Entity.EmbeddedId[Id, Product]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** 商品カテゴリ: 100 番台が単品、200 番台がセット */
  enum Category(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_BURGER  extends Category(code = 100, name = "バーガー")
    case IS_SIDE    extends Category(code = 101, name = "サイド")
    case IS_DRINK   extends Category(code = 102, name = "ドリンク")
    case IS_DESSERT extends Category(code = 103, name = "デザート")
    case IS_SET     extends Category(code = 200, name = "セットメニュー")

  /** Sales status. Discontinued products stay readable for past orders. */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_DISCONTINUED extends Status(code = -1) // 販売終了
    case IS_PLAN         extends Status(code =  0) // 販売予定
    case IS_ON_SALE      extends Status(code =  1) // 販売中
