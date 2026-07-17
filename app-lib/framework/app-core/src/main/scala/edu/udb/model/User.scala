/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.udb.model

import ixias.core.model.*

/**
 * User: a registered account (profile only).
 *
 * Credentials are kept separately in [[UserPassword]], and login sessions in
 * [[UserSession]].
 */
import User.*
case class User(
  id:        Option[Id],                 // User ID (None before persisted)
  uuid:      UUID,                        // Public UUID
  email:     String,                     // Login ID (unique)
  name:      String,                     // Display name
  state:     Status        = Status.IS_ACTIVE,
  updatedAt: LocalDateTime = Now,        // Data update date
  createdAt: LocalDateTime = Now         // Data creation date
) extends EntityModel[Id]

object User:
  // --[ Typedefs ]----------------------------------------------------
  type Id         = Id.Repr
  type UUID       = UUID.Repr
  type WithNoId   = Entity.WithNoId[Id, User]
  type EmbeddedId = Entity.EmbeddedId[Id, User]

  // --[ Objects ]-----------------------------------------------------
  object Id extends Entity.Id[Long]

  /** Public UUID identifier. */
  object UUID extends Entity.Id[String]:
    def generate: UUID = UUID(java.util.UUID.randomUUID.toString)

  // --[ Value Objects ]-----------------------------------------------
  /** Account status */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_INACTIVE extends Status(code = -1) // Inactive
    case IS_ACTIVE   extends Status(code =  1) // Active
