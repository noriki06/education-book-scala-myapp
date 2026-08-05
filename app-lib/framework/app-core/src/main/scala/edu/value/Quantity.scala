/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.value

import ixias.core.model.*

/**
 * Quantity: how many of one item are ordered.
 *
 * Bounded to 1..99. Zero is not a quantity — removing an item from the cart
 * deletes the line rather than setting it to zero, so the model never has to
 * decide what a zero line means.
 */
object Quantity extends OpaqueValue[Short]:

  val Min: Short = 1
  val Max: Short = 99

  val One: Quantity = Quantity(1)

  /** Build a quantity, rejecting values outside 1..99. */
  override def apply(v: Short): Quantity =
    require(v >= Min && v <= Max, s"Quantity must be between $Min and $Max: $v")
    super.apply(v)

  /** Build a quantity without throwing (for values coming from outside). */
  def from(v: Short): Either[String, Quantity] =
    Either.cond(
      v >= Min && v <= Max,
      super.apply(v),
      s"Quantity must be between $Min and $Max: $v"
    )

type Quantity = Quantity.Repr
