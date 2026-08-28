/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.member.persistence

import javax.inject.*
import scala.concurrent.Future
import ixias.db.slick.{ SlickBaseRepository, SlickDatabaseContext }
import ixias.core.persistence.HostSpec

import edu.member.model.Member
import edu.member.persistence.table.MemberPasswordTable

/**
 * Repository for MemberPassword persistence (credentials).
 */
@Singleton
class MemberPasswordRepository @Inject()(
  table: MemberPasswordTable,
  ctx:   SlickDatabaseContext
) extends SlickBaseRepository(table, ctx):
  import api.*

  /**
   * Resolve a member's credential by member id (used at login).
   */
  def findByMemberId(memberId: Member.Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.memberId === memberId)
        .result.headOption
