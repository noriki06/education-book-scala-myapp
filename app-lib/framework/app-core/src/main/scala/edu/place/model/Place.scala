/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.place.model

import ixias.core.model.*

/**
 * 店: レビューの対象。Google の店への参照と、会員が付けた呼び名を持つ。
 * 住所・営業時間は保存せず、表示のたびに Google から取得する。
 */
import Place.*
case class Place(
  id:            Option[Id],          // 管理 ID（永続化前は None）
  googlePlaceId: Option[String],      // Google の店への参照（手動登録なら None）
  name:          String,              // 店名（会員が付けた呼び名。登録時に Google から転記して確定）
  updatedAt:     LocalDateTime = Now, // データ更新日
  createdAt:     LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

/**
 * 店: 付随する型と処理の定義
 */
object Place:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Place]
  type EmbeddedId = Entity.EmbeddedId[Id, Place]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
