/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.udb.model

import ixias.core.model.*
import ixias.core.model.value.Token

/**
 * UserSession: a server-side login session.
 *
 * After login succeeds a random [[Token]] is issued, stored here, and handed to
 * the client in an httpOnly cookie — signed, so the cookie value is the
 * `SignedToken` form (`{signature}-{nonce}-{token}`) while this table keeps the
 * raw token. Subsequent requests verify the signature, recover the raw token,
 * and resolve the user by looking it up here.
 */
import UserSession.*
case class UserSession(
  id:        Option[Id],                       // Management ID
  uid:       User.Id,                          // User ID
  token:     Token,                            // Opaque session token (unsigned form)
  state:     Status        = Status.IS_ACTIVE, // Status
  expiresAt: LocalDateTime = Now.plusDays(30), // Expiry
  updatedAt: LocalDateTime = Now,              // Data update date
  createdAt: LocalDateTime = Now               // Data creation date
) extends EntityModel[Id]

object UserSession:
  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, UserSession]
  type EmbeddedId = Entity.EmbeddedId[Id, UserSession]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /** Session status */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CLOSED extends Status(code = -1) // Closed / invalidated
    case IS_ACTIVE extends Status(code =  1) // Active
