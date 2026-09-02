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
 * イベント: 匿名の誘い。成立するまで立案者も参加者も表示されない。
 */
import Event.*
case class Event(
  id:             Option[Id],                      // 管理 ID（永続化前は None）
  code:           Code,                            // 公開用 ID（URL に使う。一意・推測できない）
  memberId:       Member.Id,                       // 立案者（誰にも表示しない）
  title:          String,                          // 内容（「今日ランチ行きたい」）
  startAt:        LocalDateTime,                   // 集合日時（イベントの始まり。過ぎたら終了）
  closeAt:        LocalDateTime,                   // 締切（既定＝集合日時。現在 < closeAt ≦ startAt）
  minEntries:     Short,                           // 成立人数（立案者込み・2〜50。達したら成立）
  slackChannelId: String,                          // 投稿先チャンネル（立案時に選ぶ）
  slackMessageId: Option[String],                  // bot 投稿の記録（投稿前は None）
  confirmedAt:    Option[LocalDateTime],           // 成立した日時（成立前は None）
  state:          Status        = Status.IS_OPEN,  // イベントの状態
  updatedAt:      LocalDateTime = Now,             // データ更新日
  createdAt:      LocalDateTime = Now              // データ作成日
) extends EntityModel[Id]

/**
 * イベント: 付随する型と処理の定義
 */
object Event:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type Code       = Code.Repr
  type WithNoId   = Entity.WithNoId[Id, Event]
  type EmbeddedId = Entity.EmbeddedId[Id, Event]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  /**
   * 公開用の識別子。ログイン不要で詳細が見える唯一の入場鍵なので、
   * 一意かつ推測できない値にする（連番を URL に晒さない）。
   * 128bit を URL-safe な 22 文字に符号化する（03_design.md の入力値の範囲）
   */
  object Code extends Entity.Id[String]:
    private val random = new java.security.SecureRandom

    def generate: Code =
      val bytes = new Array[Byte](16)
      random.nextBytes(bytes)
      Code(java.util.Base64.getUrlEncoder.withoutPadding.encodeToString(bytes))

  // --[ Value Objects ]-----------------------------------------------
  /**
   * イベントの状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CANCELED  extends Status(code = -2) // 取り消し: 立案者がやめた
    case IS_FAILED    extends Status(code = -1) // 不成立: 締切までに人数が届かなかった
    case IS_OPEN      extends Status(code =  1) // 募集中
    case IS_CONFIRMED extends Status(code =  2) // 成立: 名前が開示されている
    case IS_FINISHED  extends Status(code =  3) // 終了: 集合日時を過ぎた

  // --[ Extensions ]--------------------------------------------------
  /**
   * イベント: 変数値だけで完結する処理
   */
  extension (self: Event)

    /**
     * 成立させる。`state` と `confirmedAt` は連動する決めごと
     * （IS_CONFIRMED なら必ず Some）なので、2 つを別々に触らせず
     * ここを唯一の入口にする
     */
    def confirmed(at: LocalDateTime): Event =
      self.copy(state = Status.IS_CONFIRMED, confirmedAt = Some(at))
