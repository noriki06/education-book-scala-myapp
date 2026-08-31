/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.place.persistence

import javax.inject.*
import scala.concurrent.Future
import ixias.db.slick.{ SlickBaseRepository, SlickDatabaseContext }
import ixias.core.persistence.HostSpec

import edu.member.model.Member
import edu.place.model.Place
import edu.place.persistence.table.PlaceReviewTable

/**
 * Repository for PlaceReview persistence.
 */
@Singleton
class PlaceReviewRepository @Inject()(
  table: PlaceReviewTable,
  ctx:   SlickDatabaseContext
) extends SlickBaseRepository(table, ctx):
  import api.*

  /**
   * The reviews of one place, newest first. A member appears as often as they
   * have been — there is one row per visit, not one per person.
   */
  def findByPlace(placeId: Place.Id): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.placeId === placeId)
        .sortBy(_.createdAt.desc)
        .result

  /**
   * Everything a member has written, newest first — their own history, and the
   * only list from which they can edit or delete.
   */
  def findByMember(memberId: Member.Id): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.memberId === memberId)
        .sortBy(_.createdAt.desc)
        .result

  /**
   * Places ranked by how well colleagues rated them, best first, as
   * (place id, average star, review count).
   *
   * This is what the suggestions on a confirmed event are drawn from: a
   * colleague's "I like this one" outranks a stranger's score, which is the
   * whole reason reviews exist here. Google fills in only what is missing.
   */
  def rankByStar(limit: Int): Future[Seq[(Place.Id, Option[Double], Int)]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .groupBy(_.placeId)
        .map { case (placeId, rows) =>
          (placeId, rows.map(_.star.asColumnOf[Double]).avg, rows.length)
        }
        .sortBy { case (_, avg, count) => (avg.desc, count.desc) }
        .take(limit)
        .result
