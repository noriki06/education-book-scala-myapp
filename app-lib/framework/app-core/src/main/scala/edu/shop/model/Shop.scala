/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import ixias.core.model.value.PhoneNumber

/**
 * Shop: a physical store an order is picked up from.
 *
 * `state` is the shop itself being in business — it changes once every few
 * years (a new store, a permanent closure), not day to day. "Closed today" is
 * not stored anywhere: it is derived from the opening hours and the current
 * time, because a stored flag becomes a lie the moment the date rolls over.
 *
 * `phone` is ixias' [[PhoneNumber]], which parses and normalises to E.164.
 * A domestic string needs the region — `PhoneNumber("0312345678", "JP")` —
 * while an already international one does not: `PhoneNumber("+81312345678")`.
 * The one-argument form has no default region and throws on a domestic number.
 */
import Shop.*
case class Shop(
  id:           Option[Id],                    // 管理 ID（永続化前は None）
  name:         String,                        // 店名（渋谷店）
  address:      String,                        // 住所
  phone:        PhoneNumber,                   // 電話番号（値オブジェクト）
  openingHours: OpeningHours,                  // 営業時間
  state:        Status        = Status.IS_OPEN, // 店舗の状態
  updatedAt:    LocalDateTime = Now,           // データ更新日
  createdAt:    LocalDateTime = Now            // データ作成日
) extends EntityModel[Id]:

  /** Whether an order may be placed at `time` — used before accepting one. */
  def isOpenAt(time: LocalTime): Boolean =
    state == Status.IS_OPEN && openingHours.includes(time)

object Shop:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Shop]
  type EmbeddedId = Entity.EmbeddedId[Id, Shop]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * Opening hours: the time the shop opens and how long it stays open.
   *
   * There is no closing time. Keeping a start and an end needs an "open is
   * before close" check, and `02:00` cannot say whether it means tonight or
   * tomorrow morning. A length makes the pair impossible to get wrong and lets
   * `10:00 + 16h` cross midnight unambiguously.
   */
  case class OpeningHours(startAt: LocalTime, duration: Duration):
    require(
      !duration.isNegative && !duration.isZero && duration.getSeconds <= 86400L,
      s"Opening hours must last between 0 and 24 hours: $duration"
    )

    /** The closing time, derived — it wraps past midnight when it has to. */
    def endAt: LocalTime = startAt.plus(duration)

    /** Whether `time` falls inside the opening hours (midnight-safe). */
    def includes(time: LocalTime): Boolean =
      val elapsed = time.toSecondOfDay.toLong - startAt.toSecondOfDay.toLong
      val since   = if elapsed >= 0L then elapsed else elapsed + 86400L
      since < duration.getSeconds

  /** Whether the shop is in business at all (not "closed today"). */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CLOSED extends Status(code = -1) // 休業中（長期休業・閉店）
    case IS_OPEN   extends Status(code =  1) // 営業中
