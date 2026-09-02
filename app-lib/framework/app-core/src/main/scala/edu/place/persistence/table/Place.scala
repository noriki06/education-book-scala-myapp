/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.place.persistence.table

import javax.inject.*
import slick.jdbc.JdbcProfile
import ixias.core.model.*
import ixias.db.slick.{ SlickTable, SlickDatabaseContext }
import ixias.core.persistence.HostSpec
import edu.place.model.Place

/**
 * Table Definition: Place (`place`)
 */
@Singleton
class PlaceTable @Inject()(ctx: SlickDatabaseContext)
  extends SlickTable[Place.Id, Place, JdbcProfile](ctx):
  import api.*

  val ds = Map(
    HostSpec.PRIMARY -> DataSourceFactory("ixias.db.mysql://primary/app"),
    HostSpec.REPLICA -> DataSourceFactory("ixias.db.mysql://replica/app")
  )

  val query = TableQuery[Table]

  case class Table(tag: Tag) extends BasicTable(tag, "place"):
    import Place.*

    @pk  def id            = column[Id]             ("id",              O.UInt64, O.AutoInc, O.PrimaryKey)
    @col def uuid          = column[UUID]           ("uuid",            O.Varchar(64, Charset.Ascii))
    @col def googlePlaceId = column[Option[String]] ("google_place_id", O.Varchar(255, Charset.Ascii))
    @col def name          = column[String]         ("name",            O.Varchar(255))
    @col def updatedAt     = column[LocalDateTime]  ("updated_at",      O.Timestamp(onUpdate = true))
    @col def createdAt     = column[LocalDateTime]  ("created_at",      O.Timestamp)

    def ukey01 = index("ukey01", uuid,          unique = true)
    def ukey02 = index("ukey02", googlePlaceId, unique = true)
    def key01  = index("key01",  name)

    /**
     * The bidirectional mappings.
     * 1) Tuple(table) => Model
     * 2) Model        => Tuple(table)
     */
    def * = deriveColumns.mapTo[Place](
      onWrite = _.copy(updatedAt = Now)
    )
