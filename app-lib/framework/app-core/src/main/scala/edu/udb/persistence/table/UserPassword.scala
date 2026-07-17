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
import edu.udb.model.{ User, UserPassword }

/** Table Definition: UserPassword (`udb_user_password`) */
@Singleton
class UserPasswordTable @Inject()(ctx: SlickDatabaseContext)
  extends SlickTable[UserPassword.Id, UserPassword, JdbcProfile](ctx):
  import api.*

  val ds = Map(
    HostSpec.PRIMARY -> DataSourceFactory("ixias.db.mysql://primary/app"),
    HostSpec.REPLICA -> DataSourceFactory("ixias.db.mysql://replica/app")
  )

  val query = TableQuery[Table]

  case class Table(tag: Tag) extends BasicTable(tag, "udb_user_password"):
    import UserPassword.*

    /* @1 */ def id        = column[Id]            ("id",         O.UInt64, O.PrimaryKey, O.AutoInc)
    /* @2 */ def uid       = column[User.Id]       ("uid",        O.UInt64)
    /* @3 */ def hash      = column[String]        ("hash",       O.Varchar(255, Charset.Ascii))
    /* @4 */ def updatedAt = column[LocalDateTime] ("updated_at", O.Timestamp(onUpdate = true))
    /* @5 */ def createdAt = column[LocalDateTime] ("created_at", O.Timestamp)

    def ukey01 = index("ukey01", uid, unique = true)

    def * = (
      id.?, uid, hash, updatedAt, createdAt
    ) <> (
      UserPassword.apply.tupled,
      Tuple.fromProductTyped[UserPassword].andThen(t => t.copy(_4 = Now))
    )
