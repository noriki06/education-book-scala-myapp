/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*
import edu.common.model.StampCard

/**
 * スタンプカード
 *
 * 押されたスタンプは [[CustomerStampCardItem]] の行で、たまった数はその件数。
 * `issuedCoupon*` は引き換えの記録。
 */
import CustomerStampCard.*
case class CustomerStampCard(
  id:             Option[Id],                // 保有Id
  cardId:         StampCard.Id,              // 台帳Id
  customerId:     Customer.Id,               // 顧客Id
  issuedCouponId: Option[CustomerCoupon.Id], // 発行: クーポンId
  issuedCouponAt: Option[LocalDateTime],     // 発行: 日時
  state:          Status,                    // 保有状態
  updatedAt:      LocalDateTime = Now,       // データ更新日
  createdAt:      LocalDateTime = Now        // データ作成日
) extends EntityModel[Id]:

  /** その日にまだスタンプを押せるカードか */
  def isCollectingOn(date: LocalDate, ledger: StampCard): Boolean =
    state != Status.IS_EXCHANGED && ledger.dateEnd.forall(!date.isAfter(_))

object CustomerStampCard:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerStampCard]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerStampCard]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** 保有状態 */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_EXPIRED    extends Status(code = -1) // 失効の印: 掃除で付ける。判定の正ではない
    case IS_COLLECTING extends Status(code =  1) // 集め中
    case IS_EXCHANGED  extends Status(code =  2) // 引換済: クーポンを発行した。ここだけが終端
