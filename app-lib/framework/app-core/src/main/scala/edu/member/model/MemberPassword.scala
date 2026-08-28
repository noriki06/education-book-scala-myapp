/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.member.model

import ixias.core.model.*
import ixias.core.security.PBKDF2

/**
 * 会員パスワード
 */
import MemberPassword.*
case class MemberPassword(
  id:        Option[Id],          // 管理Id
  memberId:  Member.Id,           // 会員Id
  hash:      String,              // PBKDF2ハッシュ文字列
  updatedAt: LocalDateTime = Now, // データ更新日
  createdAt: LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

/**
 * 会員パスワード: 付随する型と処理の定義
 */
object MemberPassword:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, MemberPassword]
  type EmbeddedId = Entity.EmbeddedId[Id, MemberPassword]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Factory Methods ]---------------------------------------------
  /**
   * 平文のパスワードを PBKDF2 でハッシュ化して新しい行を作る
   */
  def hashed(memberId: Member.Id, raw: String): WithNoId =
    MemberPassword(
      id       = None,
      memberId = memberId,
      hash     = PBKDF2.hash(raw)
    ).toWithNoId

  // --[ Extensions ]--------------------------------------------------
  /**
   * 会員パスワード: 変数値だけで完結する処理
   */
  extension (self: MemberPassword)

    /**
     * 平文のパスワードが保存済みハッシュと一致するか
     */
    def verify(raw: String): Boolean =
      PBKDF2.compare(raw, self.hash)
