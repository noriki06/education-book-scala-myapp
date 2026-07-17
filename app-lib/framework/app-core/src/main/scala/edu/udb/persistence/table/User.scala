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
import edu.udb.model.User

/** Table Definition: User (`udb_user`) */
@Singleton
class UserTable @Inject()(ctx: SlickDatabaseContext)
  extends SlickTable[User.Id, User, JdbcProfile](ctx):
  import api.*

  val ds = Map(
    HostSpec.PRIMARY -> DataSourceFactory("ixias.db.mysql://primary/app"),
    HostSpec.REPLICA -> DataSourceFactory("ixias.db.mysql://replica/app")
  )

  val query = TableQuery[Table]

  case class Table(tag: Tag) extends BasicTable(tag, "udb_user"):
    import User.*

    /* @1 */ def id        = column[Id]            ("id",         O.UInt64, O.PrimaryKey, O.AutoInc)
    /* @2 */ def uuid      = column[UUID]          ("uuid",       O.Varchar(64, Charset.Ascii))
    /* @3 */ def email     = column[String]        ("email",      O.Varchar(255, Charset.Ascii))
    /* @4 */ def name      = column[String]        ("name",       O.Varchar(255))
    /* @5 */ def state     = column[Status]        ("state",      O.Int16)
    /* @6 */ def updatedAt = column[LocalDateTime] ("updated_at", O.Timestamp(onUpdate = true))
    /* @7 */ def createdAt = column[LocalDateTime] ("created_at", O.Timestamp)

    def ukey01 = index("ukey01", uuid,  unique = true)
    def ukey02 = index("ukey02", email, unique = true)

    def * = (
      id.?, uuid, email, name, state, updatedAt, createdAt
    ) <> (
      User.apply.tupled,
      Tuple.fromProductTyped[User].andThen(t => t.copy(_6 = Now))
    )
