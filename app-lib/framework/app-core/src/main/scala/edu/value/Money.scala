/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.value

import ixias.core.model.*

/**
 * Money: an amount in Japanese yen.
 *
 * The yen has no minor unit in daily use, so an `Int` holding whole yen is
 * exact — no rounding error, unlike a floating point amount. Negative amounts
 * are rejected: this domain has no refunds expressed as negative money (a
 * refund is a [[edu.sales.model.Payment]] state, not a negative price).
 */
object Money extends OpaqueValue[Int]:

  val Zero: Money = Money(0)

  /** Build an amount, rejecting negatives. */
  override def apply(v: Int): Money =
    require(v >= 0, s"Money must not be negative: $v")
    super.apply(v)

  /** Build an amount without throwing (for values coming from outside). */
  def from(v: Int): Either[String, Money] =
    Either.cond(v >= 0, super.apply(v), s"Money must not be negative: $v")

  extension (self: Money)
    /** Add two amounts. */
    def +(that: Money): Money = Money(self.value + that.value)

    /** Repeat an amount — a unit price times a quantity. */
    def *(times: Int): Money = Money(self.value * times)

type Money = Money.Repr
