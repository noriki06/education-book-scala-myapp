/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.event.persistence

import javax.inject.*
import scala.concurrent.Future
import ixias.db.slick.{ SlickBaseRepository, SlickDatabaseContext }
import ixias.core.persistence.HostSpec

import edu.member.model.Member
import edu.event.model.Event
import edu.event.persistence.table.EventEntryTable

/**
 * Repository for EventEntry persistence.
 */
@Singleton
class EventEntryRepository @Inject()(
  table: EventEntryTable,
  ctx:   SlickDatabaseContext
) extends SlickBaseRepository(table, ctx):
  import api.*

  /**
   * How many have pressed. Read on the primary: the confirmation check reads
   * this immediately after a write and must not see a stale replica.
   */
  def countByEvent(eventId: Event.Id): Future[Int] =
    RunDBAction: slick =>
      slick
        .filter(_.eventId === eventId)
        .length.result

  /**
   * The entries of one event. Who they are is resolved by the caller — this
   * layer only knows the ids.
   */
  def findByEvent(eventId: Event.Id): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.eventId === eventId)
        .result

  /**
   * The one entry a member holds on an event, if any. Absence is the answer to
   * "has this member joined" — an entry that is gone was taken back.
   */
  def find(eventId: Event.Id, memberId: Member.Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction: slick =>
      slick
        .filter(e => e.eventId === eventId && e.memberId === memberId)
        .result.headOption

  /**
   * Every event a member pressed, for their history.
   */
  def findByMember(memberId: Member.Id): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.memberId === memberId)
        .result

  /**
   * Take back one entry. Returns the rows removed, so the caller can tell a
   * real cancellation from a double submit.
   */
  def delete(eventId: Event.Id, memberId: Member.Id): Future[Int] =
    RunDBAction: slick =>
      slick
        .filter(e => e.eventId === eventId && e.memberId === memberId)
        .delete
