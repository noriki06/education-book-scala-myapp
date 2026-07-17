/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package model.udb.reads

import io.circe.Decoder
import io.circe.generic.semiauto.*

/** Request body for user signup. */
case class JsValueSignup(
  email:    String,
  password: String,
  name:     String,
)

object JsValueSignup:
  given Decoder[JsValueSignup] = deriveDecoder
