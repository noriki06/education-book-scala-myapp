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
 * CustomerPassword: a user's credential, kept separate from the [[Customer]] profile.
 * `hash` is a self-contained PBKDF2 hash string (salt + iterations + digest)
 * produced by `ixias.core.security.PBKDF2`. The raw password is never stored.
 */
import CustomerPassword.*
case class CustomerPassword(
  id:        Option[Id],           // 管理 ID
  customerId:       Customer.Id,              // ユーザーID
  hash:      String,               // PBKDF2ハッシュ文字列
  updatedAt: LocalDateTime = Now,  // データ更新日
  createdAt: LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]:

  /**
   * Verify a raw password against the stored PBKDF2 hash.
   */
  def verify(raw: String): Boolean = PBKDF2.compare(raw, hash)

object CustomerPassword:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerPassword]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerPassword]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  /**
   * Build a new credential record, hashing the raw password with PBKDF2.
   */
  def hashed(customerId: Customer.Id, raw: String): WithNoId =
    CustomerPassword(
      id   = None,
      customerId  = customerId,
      hash = PBKDF2.hash(raw)
    ).toWithNoId
