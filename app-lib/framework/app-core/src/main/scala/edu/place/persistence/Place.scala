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

import edu.place.model.Place
import edu.place.persistence.table.PlaceTable

/**
 * Repository for Place persistence.
 */
@Singleton
class PlaceRepository @Inject()(
  table: PlaceTable,
  ctx:   SlickDatabaseContext
) extends SlickBaseRepository(table, ctx):
  import api.*

  /**
   * Resolve a place by the identifier in its URL. This is how the detail page
   * finds it — the numeric id never leaves the server.
   */
  def findByUuid(uuid: Place.UUID): Future[Option[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.uuid === uuid)
        .result.headOption

  /**
   * Resolve a place by the Google reference it was registered from. Used to
   * reuse the existing row instead of creating a second one for the same shop.
   */
  def findByGooglePlaceId(googlePlaceId: String): Future[Option[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.googlePlaceId === googlePlaceId)
        .result.headOption

  /**
   * Places whose name starts with what the member typed. Shown before a manual
   * registration as "did you mean one of these?" — the only guard against the
   * same canteen being entered twice under slightly different names.
   */
  def findByNamePrefix(prefix: String): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.name.startsWith(prefix))
        .sortBy(_.name.asc)
        .result
