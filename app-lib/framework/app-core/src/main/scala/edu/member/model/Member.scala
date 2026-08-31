/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.member.model

import ixias.core.model.*

/**
 * 会員: 会員登録されたアカウント。プロフィールのみを持つ。
 */
import Member.*
case class Member(
  id:        Option[Id],                        // 管理Id
  uuid:      UUID,                              // UUID
  email:     String,                            // ログインId (メールアドレス)
  name:      String,                            // 表示名
  nameKana:  String,                            // 表示名の読み (名簿の並び順に使う)
  state:     Status        = Status.IS_ACTIVE,  // アカウント状態
  updatedAt: LocalDateTime = Now,               // データ更新日
  createdAt: LocalDateTime = Now                // データ作成日
) extends EntityModel[Id]

/**
 * 会員: 付随する型と処理の定義
 */
object Member:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type UUID       = UUID.Repr
  type WithNoId   = Entity.WithNoId[Id, Member]
  type EmbeddedId = Entity.EmbeddedId[Id, Member]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  /**
   * 公開用の識別子
   */
  object UUID extends Entity.Id[String]:
    def generate: UUID = UUID(java.util.UUID.randomUUID.toString)

  // --[ Value Objects ]-----------------------------------------------
  /**
   * アカウント状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_INACTIVE extends Status(code = -1) // 停止
    case IS_ACTIVE   extends Status(code =  1) // 有効
