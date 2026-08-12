/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.persistence.table

import javax.inject.*
import slick.jdbc.JdbcProfile
import ixias.core.model.*
import ixias.core.model.value.Token
import ixias.db.slick.{ SlickTable, SlickDatabaseContext }
import ixias.core.persistence.HostSpec
import edu.customer.model.{ Customer, CustomerSession }

/**
 * Table Definition: CustomerSession (`customer_session`)
 */
@Singleton
class CustomerSessionTable @Inject()(ctx: SlickDatabaseContext)
  extends SlickTable[CustomerSession.Id, CustomerSession, JdbcProfile](ctx):
  import api.{ given, * }

  val ds = Map(
    HostSpec.PRIMARY -> DataSourceFactory("ixias.db.mysql://primary/app"),
    HostSpec.REPLICA -> DataSourceFactory("ixias.db.mysql://replica/app")
  )

  val query = TableQuery[Table]

  case class Table(tag: Tag) extends BasicTable(tag, "customer_session"):
    import CustomerSession.*

    @pk  def id         = column[Id]            ("id",          O.UInt64, O.AutoInc, O.PrimaryKey)
    @col def customerId = column[Customer.Id]   ("customer_id", O.UInt64)
    @col def token      = column[Token]         ("token",       O.Varchar(255, Charset.Ascii))
    @col def state      = column[Status]        ("state",       O.Int16)
    @col def expiresAt  = column[LocalDateTime] ("expires_at",  O.Timestamp)
    @col def updatedAt  = column[LocalDateTime] ("updated_at",  O.Timestamp(onUpdate = true))
    @col def createdAt  = column[LocalDateTime] ("created_at",  O.Timestamp)

    def ukey01 = index("ukey01", token, unique = true)
    def key01  = index("key01", customerId)

    /**
     * The bidirectional mappings.
     * 1) Tuple(table) => Model
     * 2) Model        => Tuple(table)
     */
    def * = deriveColumns.mapTo[CustomerSession](
      onWrite = _.copy(updatedAt = Now)
    )
