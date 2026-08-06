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
import ixias.db.slick.{ SlickTable, SlickDatabaseContext }
import ixias.core.persistence.HostSpec
import edu.customer.model.{ Customer, CustomerPassword }

/**
 * Table Definition: CustomerPassword (`udb_user_password`)
 */
@Singleton
class CustomerPasswordTable @Inject()(ctx: SlickDatabaseContext)
  extends SlickTable[CustomerPassword.Id, CustomerPassword, JdbcProfile](ctx):
  import api.*

  val ds = Map(
    HostSpec.PRIMARY -> DataSourceFactory("ixias.db.mysql://primary/app"),
    HostSpec.REPLICA -> DataSourceFactory("ixias.db.mysql://replica/app")
  )

  val query = TableQuery[Table]

  case class Table(tag: Tag) extends BasicTable(tag, "udb_user_password"):
    import CustomerPassword.*

    @pk  def id        = column[Id]            ("id",         O.UInt64, O.AutoInc, O.PrimaryKey)
    @col def customerId       = column[Customer.Id]       ("customerId",        O.UInt64)
    @col def hash      = column[String]        ("hash",       O.Varchar(255, Charset.Ascii))
    @col def updatedAt = column[LocalDateTime] ("updated_at", O.Timestamp(onUpdate = true))
    @col def createdAt = column[LocalDateTime] ("created_at", O.Timestamp)

    def ukey01 = index("ukey01", customerId, unique = true)

    /**
     * The bidirectional mappings.
     * 1) Tuple(table) => Model
     * 2) Model        => Tuple(table)
     */
    def * = deriveColumns.mapTo[CustomerPassword](
      onWrite = _.copy(updatedAt = Now)
    )
