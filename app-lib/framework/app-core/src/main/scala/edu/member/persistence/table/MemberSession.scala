/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.member.persistence.table

import javax.inject.*
import slick.jdbc.JdbcProfile
import ixias.core.model.*
import ixias.core.model.value.Token
import ixias.db.slick.{ SlickTable, SlickDatabaseContext }
import ixias.core.persistence.HostSpec
import edu.member.model.{ Member, MemberSession }

/**
 * Table Definition: MemberSession (`member_session`)
 */
@Singleton
class MemberSessionTable @Inject()(ctx: SlickDatabaseContext)
  extends SlickTable[MemberSession.Id, MemberSession, JdbcProfile](ctx):
  import api.{ given, * }

  val ds = Map(
    HostSpec.PRIMARY -> DataSourceFactory("ixias.db.mysql://primary/app"),
    HostSpec.REPLICA -> DataSourceFactory("ixias.db.mysql://replica/app")
  )

  val query = TableQuery[Table]

  case class Table(tag: Tag) extends BasicTable(tag, "member_session"):
    import MemberSession.*

    @pk  def id        = column[Id]            ("id",         O.UInt64, O.AutoInc, O.PrimaryKey)
    @col def memberId  = column[Member.Id]     ("member_id",  O.UInt64)
    @col def token     = column[Token]         ("token",      O.Varchar(255, Charset.Ascii))
    @col def state     = column[Status]        ("state",      O.Int16)
    @col def expiresAt = column[LocalDateTime] ("expires_at", O.Timestamp)
    @col def updatedAt = column[LocalDateTime] ("updated_at", O.Timestamp(onUpdate = true))
    @col def createdAt = column[LocalDateTime] ("created_at", O.Timestamp)

    def ukey01 = index("ukey01", token, unique = true)
    def key01  = index("key01", memberId)

    /**
     * The bidirectional mappings.
     * 1) Tuple(table) => Model
     * 2) Model        => Tuple(table)
     */
    def * = deriveColumns.mapTo[MemberSession](
      onWrite = _.copy(updatedAt = Now)
    )
