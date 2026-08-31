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
import edu.member.model.Member
import edu.place.model.{ Place, PlaceReview }

/**
 * Table Definition: PlaceReview (`place_review`)
 */
@Singleton
class PlaceReviewTable @Inject()(ctx: SlickDatabaseContext)
  extends SlickTable[PlaceReview.Id, PlaceReview, JdbcProfile](ctx):
  import api.*

  val ds = Map(
    HostSpec.PRIMARY -> DataSourceFactory("ixias.db.mysql://primary/app"),
    HostSpec.REPLICA -> DataSourceFactory("ixias.db.mysql://replica/app")
  )

  val query = TableQuery[Table]

  case class Table(tag: Tag) extends BasicTable(tag, "place_review"):
    import PlaceReview.*

    @pk  def id        = column[Id]            ("id",         O.UInt64, O.AutoInc, O.PrimaryKey)
    @col def placeId   = column[Place.Id]      ("place_id",   O.UInt64)
    @col def memberId  = column[Member.Id]     ("member_id",  O.UInt64)
    @col def star      = column[Short]         ("star",       O.Int16)
    @col def comment   = column[String]        ("comment",    O.Varchar(255))
    @col def updatedAt = column[LocalDateTime] ("updated_at", O.Timestamp(onUpdate = true))
    @col def createdAt = column[LocalDateTime] ("created_at", O.Timestamp)

    def key01 = index("key01", (placeId, createdAt))
    def key02 = index("key02", memberId)

    /**
     * The bidirectional mappings.
     * 1) Tuple(table) => Model
     * 2) Model        => Tuple(table)
     */
    def * = deriveColumns.mapTo[PlaceReview](
      onWrite = _.copy(updatedAt = Now)
    )
