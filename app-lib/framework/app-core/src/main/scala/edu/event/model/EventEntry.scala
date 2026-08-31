/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.event.model

import ixias.core.model.*

import edu.member.model.Member

/**
 * 参加: 会員がイベントに押した1件。状態を持たず「ある」か「ない」かだけ
 * ——取り消しは行を消す（不参加という記録を持たない）。
 */
import EventEntry.*
case class EventEntry(
  id:        Option[Id],          // 管理 ID（永続化前は None）
  eventId:   Event.Id,            // どのイベントか
  memberId:  Member.Id,           // 誰が押したか（成立まで誰にも表示しない）
  updatedAt: LocalDateTime = Now, // データ更新日
  createdAt: LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

/**
 * 参加: 付随する型と処理の定義
 */
object EventEntry:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, EventEntry]
  type EmbeddedId = Entity.EmbeddedId[Id, EventEntry]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
