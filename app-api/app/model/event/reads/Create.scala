/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package model.event.reads

import io.circe.Decoder
import io.circe.generic.semiauto.*

/**
 * Request body for proposing an event.
 *
 * The two timestamps arrive as strings, not as `LocalDateTime`: every time in
 * this application is JST, so an offset is not accepted, and parsing them in
 * the controller lets a malformed value come back as a sentence rather than a
 * decoder failure.
 *
 * `closeAt` may be omitted — the default deadline is the meeting time itself.
 * Who is proposing is never part of the body; it comes from the session.
 */
case class JsValueCreate(
  title:          String,
  startAt:        String,
  closeAt:        Option[String],
  minEntries:     Int,
  slackChannelId: String,
)

object JsValueCreate:
  given Decoder[JsValueCreate] = deriveDecoder
