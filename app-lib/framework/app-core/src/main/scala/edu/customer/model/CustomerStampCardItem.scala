/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*

import edu.common.model.StampCard
import edu.shop.model.Payment

/**
 * スタンプ明細: カードに押されたスタンプ 1 個。
 * `cardId` は本部の台帳、`customerCardId` は会員が保有するカード。
 */
import CustomerStampCardItem.*
case class CustomerStampCardItem(
  id:             Option[Id],           // 管理Id
  cardId:         StampCard.Id,         // 台帳Id
  customerId:     Customer.Id,          // 顧客Id
  customerCardId: CustomerStampCard.Id, // 顧客: 保有カードId
  paymentId:      Payment.Id,           // どの会計で押されたか
  updatedAt:      LocalDateTime = Now,  // データ更新日
  createdAt:      LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

object CustomerStampCardItem:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerStampCardItem]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerStampCardItem]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]
