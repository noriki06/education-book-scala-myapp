/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*
import ixias.core.security.PBKDF2

/**
 * 顧客パスワード
 */
import CustomerPassword.*
case class CustomerPassword(
  id:         Option[Id],          // 管理Id
  customerId: Customer.Id,         // 顧客Id
  hash:       String,              // PBKDF2ハッシュ文字列
  updatedAt:  LocalDateTime = Now, // データ更新日
  createdAt:  LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

object CustomerPassword:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerPassword]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerPassword]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Factory Methods ]---------------------------------------------
  /**
   * 平文のパスワードを PBKDF2 でハッシュ化して新しい行を作る
   */
  def hashed(customerId: Customer.Id, raw: String): WithNoId =
    CustomerPassword(
      id         = None,
      customerId = customerId,
      hash       = PBKDF2.hash(raw)
    ).toWithNoId

  // --[ Extensions ]--------------------------------------------------
  /**
   * 顧客パスワード: 変数値だけで完結する処理
   */
  extension (self: CustomerPassword)

    /**
     * 平文のパスワードが保存済みハッシュと一致するか
     */
    def verify(raw: String): Boolean =
      PBKDF2.compare(raw, self.hash)
