/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.event.persistence.table

import javax.inject.*
import slick.jdbc.JdbcProfile
import ixias.core.model.*
import ixias.db.slick.{ SlickTable, SlickDatabaseContext }
import ixias.core.persistence.HostSpec
import edu.member.model.Member
import edu.event.model.{ Event, EventEntry }

/**
 * Table Definition: EventEntry (`event_entry`)
 */
@Singleton
class EventEntryTable @Inject()(ctx: SlickDatabaseContext)
  extends SlickTable[EventEntry.Id, EventEntry, JdbcProfile](ctx):
  import api.*

  val ds = Map(
    HostSpec.PRIMARY -> DataSourceFactory("ixias.db.mysql://primary/app"),
    HostSpec.REPLICA -> DataSourceFactory("ixias.db.mysql://replica/app")
  )

  val query = TableQuery[Table]

  case class Table(tag: Tag) extends BasicTable(tag, "event_entry"):
    import EventEntry.*

    @pk  def id        = column[Id]            ("id",         O.UInt64, O.AutoInc, O.PrimaryKey)
    @col def eventId   = column[Event.Id]      ("event_id",   O.UInt64)
    @col def memberId  = column[Member.Id]     ("member_id",  O.UInt64)
    @col def updatedAt = column[LocalDateTime] ("updated_at", O.Timestamp(onUpdate = true))
    @col def createdAt = column[LocalDateTime] ("created_at", O.Timestamp)

    def ukey01 = index("ukey01", (eventId, memberId), unique = true)
    def key01  = index("key01", memberId)

    /**
     * The bidirectional mappings.
     * 1) Tuple(table) => Model
     * 2) Model        => Tuple(table)
     */
    def * = deriveColumns.mapTo[EventEntry](
      onWrite = _.copy(updatedAt = Now)
    )
