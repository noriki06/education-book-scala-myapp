/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package model.place.reads

import io.circe.Decoder
import io.circe.generic.semiauto.*

/**
 * Request body for reviewing a place.
 *
 * The place is named by the identifier in its URL, not by its row number —
 * that one never leaves the server.
 *
 * `comment` may be omitted: a star on its own is a review. `star` arrives as
 * an `Int` so that a value outside 1..5 can be answered with a sentence
 * instead of failing to decode. Who is writing comes from the session.
 */
case class JsValuePlaceReview(
  placeUuid: String,
  star:      Int,
  comment:   Option[String],
)

object JsValuePlaceReview:
  given Decoder[JsValuePlaceReview] = deriveDecoder
