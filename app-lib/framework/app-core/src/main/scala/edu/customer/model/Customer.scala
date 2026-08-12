/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*

/**
 * 顧客: 会員登録されたアカウント。プロフィールのみを持つ。
 */
import Customer.*
case class Customer(
  id:        Option[Id],                        // 管理Id
  uuid:      UUID,                              // UUID
  email:     String,                            // ログインId (メールアドレス)
  name:      String,                            // 表示名
  state:     Status        = Status.IS_ACTIVE,  // アカウント状態
  updatedAt: LocalDateTime = Now,               // データ更新日
  createdAt: LocalDateTime = Now                // データ作成日
) extends EntityModel[Id]

object Customer:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type UUID       = UUID.Repr
  type WithNoId   = Entity.WithNoId[Id, Customer]
  type EmbeddedId = Entity.EmbeddedId[Id, Customer]

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
