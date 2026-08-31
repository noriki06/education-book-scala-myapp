/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.event.persistence

import javax.inject.*
import scala.concurrent.Future
import ixias.core.model.LocalDateTime
import ixias.db.slick.{ SlickBaseRepository, SlickDatabaseContext }
import ixias.core.persistence.HostSpec

import edu.member.model.Member
import edu.event.model.Event
import edu.event.persistence.table.EventTable

/**
 * Repository for Event persistence.
 */
@Singleton
class EventRepository @Inject()(
  table: EventTable,
  ctx:   SlickDatabaseContext
) extends SlickBaseRepository(table, ctx):
  import api.*

  /**
   * Resolve an event by the code in its URL. This is the lookup an anonymous
   * visitor arriving from Slack performs.
   */
  def findByCode(code: Event.Code): Future[Option[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.code === code)
        .result.headOption

  /**
   * The events shown on the list page: everything still worth looking at,
   * soonest first. Finished events stay until the day is over — the caller
   * decides that cut-off, since only it knows the current time.
   */
  def findVisible(from: LocalDateTime): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.state inSet Seq(Event.Status.IS_OPEN, Event.Status.IS_CONFIRMED, Event.Status.IS_FINISHED))
        .filter(_.startAt >= from)
        .sortBy(_.startAt.asc)
        .result

  /**
   * Every event a member proposed, newest first — their own history. Includes
   * the states nobody else can see (failed, canceled).
   */
  def findByProposer(memberId: Member.Id): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.memberId === memberId)
        .sortBy(_.startAt.desc)
        .result
