/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.udb.persistence.table

import javax.inject.*
import slick.jdbc.JdbcProfile
import ixias.core.model.*
import ixias.db.slick.{ SlickTable, SlickDatabaseContext }
import ixias.core.persistence.HostSpec
import edu.udb.model.{ User, UserSession }

/** Table Definition: UserSession (`udb_user_session`) */
@Singleton
class UserSessionTable @Inject()(ctx: SlickDatabaseContext)
  extends SlickTable[UserSession.Id, UserSession, JdbcProfile](ctx):
  import api.*

  val ds = Map(
    HostSpec.PRIMARY -> DataSourceFactory("ixias.db.mysql://primary/app"),
    HostSpec.REPLICA -> DataSourceFactory("ixias.db.mysql://replica/app")
  )

  val query = TableQuery[Table]

  case class Table(tag: Tag) extends BasicTable(tag, "udb_user_session"):
    import UserSession.*

    /* @1 */ def id        = column[Id]            ("id",         O.UInt64, O.PrimaryKey, O.AutoInc)
    /* @2 */ def uid       = column[User.Id]       ("uid",        O.UInt64)
    /* @3 */ def token     = column[String]        ("token",      O.Varchar(255, Charset.Ascii))
    /* @4 */ def state     = column[Status]        ("state",      O.Int16)
    /* @5 */ def expiresAt = column[LocalDateTime] ("expires_at", O.Timestamp)
    /* @6 */ def updatedAt = column[LocalDateTime] ("updated_at", O.Timestamp(onUpdate = true))
    /* @7 */ def createdAt = column[LocalDateTime] ("created_at", O.Timestamp)

    def ukey01 = index("ukey01", token, unique = true)
    def key01  = index("key01", uid)

    def * = (
      id.?, uid, token, state, expiresAt, updatedAt, createdAt
    ) <> (
      UserSession.apply.tupled,
      Tuple.fromProductTyped[UserSession].andThen(t => t.copy(_6 = Now))
    )
