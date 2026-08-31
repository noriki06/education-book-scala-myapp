/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.place.model

import ixias.core.model.*

import edu.member.model.Member

/**
 * レビュー: 店に対する星と一言。実名で表示する。イベントへの参照は持たず、
 * 店と会員だけの世界で完結する。
 */
import PlaceReview.*
case class PlaceReview(
  id:        Option[Id],          // 管理 ID（永続化前は None）
  placeId:   Place.Id,            // どの店か
  memberId:  Member.Id,           // 書いた人（実名で表示する）
  star:      Short,               // 星（1〜5）
  comment:   String,              // 一言（時間帯・行列もここに自由に書く）
  updatedAt: LocalDateTime = Now, // データ更新日
  createdAt: LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

/**
 * レビュー: 付随する型と処理の定義
 */
object PlaceReview:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, PlaceReview]
  type EmbeddedId = Entity.EmbeddedId[Id, PlaceReview]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
