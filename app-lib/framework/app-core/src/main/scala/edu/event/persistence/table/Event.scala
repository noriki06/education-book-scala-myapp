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
import edu.event.model.Event

/**
 * Table Definition: Event (`event`)
 */
@Singleton
class EventTable @Inject()(ctx: SlickDatabaseContext)
  extends SlickTable[Event.Id, Event, JdbcProfile](ctx):
  import api.*

  val ds = Map(
    HostSpec.PRIMARY -> DataSourceFactory("ixias.db.mysql://primary/app"),
    HostSpec.REPLICA -> DataSourceFactory("ixias.db.mysql://replica/app")
  )

  val query = TableQuery[Table]

  case class Table(tag: Tag) extends BasicTable(tag, "event"):
    import Event.*

    @pk  def id             = column[Id]                    ("id",               O.UInt64, O.AutoInc, O.PrimaryKey)
    @col def code           = column[Code]                  ("code",             O.Varchar(64, Charset.Ascii))
    @col def memberId       = column[Member.Id]             ("member_id",        O.UInt64)
    @col def title          = column[String]                ("title",            O.Varchar(255))
    @col def startAt        = column[LocalDateTime]         ("start_at",         O.Timestamp)
    @col def closeAt        = column[LocalDateTime]         ("close_at",         O.Timestamp)
    @col def minEntries     = column[Short]                 ("min_entries",      O.Int16)
    @col def slackChannelId = column[String]                ("slack_channel_id", O.Varchar(64, Charset.Ascii))
    @col def slackMessageId = column[Option[String]]        ("slack_message_id", O.Varchar(64, Charset.Ascii))
    @col def confirmedAt    = column[Option[LocalDateTime]] ("confirmed_at",     O.Timestamp)
    @col def state          = column[Status]                ("state",            O.Int16)
    @col def updatedAt      = column[LocalDateTime]         ("updated_at",       O.Timestamp(onUpdate = true))
    @col def createdAt      = column[LocalDateTime]         ("created_at",       O.Timestamp)

    def ukey01 = index("ukey01", code, unique = true)
    def key01  = index("key01", memberId)
    def key02  = index("key02", (state, startAt))

    /**
     * The bidirectional mappings.
     * 1) Tuple(table) => Model
     * 2) Model        => Tuple(table)
     */
    def * = deriveColumns.mapTo[Event](
      onWrite = _.copy(updatedAt = Now)
    )
