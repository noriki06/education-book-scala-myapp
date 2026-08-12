/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*
import ixias.core.model.value.Token

/**
 * 顧客セッション: サーバ側で保持するログインセッション。
 */
import CustomerSession.*
case class CustomerSession(
  id:         Option[Id],                        // 管理 ID
  customerId: Customer.Id,                       // ユーザー ID
  token:      Token,                             // セッショントークン（未署名）
  state:      Status        = Status.IS_ACTIVE,  // セッション状態
  expiresAt:  LocalDateTime = Now.plusDays(30),  // 有効期限
  updatedAt:  LocalDateTime = Now,               // データ更新日
  createdAt:  LocalDateTime = Now                // データ作成日
) extends EntityModel[Id]

/**
 * 顧客セッション: 付随する型と処理の定義
 */
object CustomerSession:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerSession]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerSession]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * セッション状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CLOSED extends Status(code = -1) // 無効化: ログアウト済み
    case IS_ACTIVE extends Status(code =  1) // 有効
