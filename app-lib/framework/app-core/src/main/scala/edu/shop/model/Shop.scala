/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import ixias.core.model.value.PhoneNumber

import edu.common.model.SalesTemplate

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
  id:          Option[Id],                     // 管理Id
  name:        String,                         // 店舗名
  templateId:  SalesTemplate.Id,               // 適用する販売テンプレートId
  openTimeMon: Option[(LocalTime, Duration)],  // 月曜: 開店時刻, 営業時間(h)
  openTimeTue: Option[(LocalTime, Duration)],  // 火曜: 開店時刻, 営業時間(h)
  openTimeWed: Option[(LocalTime, Duration)],  // 水曜: 開店時刻, 営業時間(h)
  openTimeThu: Option[(LocalTime, Duration)],  // 木曜: 開店時刻, 営業時間(h)
  openTimeFri: Option[(LocalTime, Duration)],  // 金曜: 開店時刻, 営業時間(h)
  openTimeSat: Option[(LocalTime, Duration)],  // 土曜: 開店時刻, 営業時間(h)
  openTimeSun: Option[(LocalTime, Duration)],  // 日曜: 開店時刻, 営業時間(h)
  phone:       PhoneNumber,                    // 電話番号
  address:     String,                         // 住所
  state:       Status        = Status.IS_OPEN, // 店舗の状態
  updatedAt:   LocalDateTime = Now,            // データ更新日
  createdAt:   LocalDateTime = Now             // データ作成日
) extends EntityModel[Id]

object Shop:

  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Shop]
  type EmbeddedId = Entity.EmbeddedId[Id, Shop]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** 店舗の状態: 数年に一度しか変わらない。「本日休業」はここではない */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CLOSED    extends Status(code = -1) // 休業中: 長期休業・閉店
    case IS_PREPARING extends Status(code =  0) // 開店準備中: まだ注文を受けない
    case IS_OPEN      extends Status(code =  1) // 営業中
