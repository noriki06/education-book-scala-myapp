/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.persistence

import javax.inject.*
import scala.concurrent.Future
import ixias.db.slick.{ SlickBaseRepository, SlickDatabaseContext }
import ixias.core.persistence.HostSpec

import edu.customer.model.Customer
import edu.customer.persistence.table.CustomerPasswordTable

/**
 * Repository for CustomerPassword persistence (credentials).
 */
@Singleton
class CustomerPasswordRepository @Inject()(
  table: CustomerPasswordTable,
  ctx:   SlickDatabaseContext
) extends SlickBaseRepository(table, ctx):
  import api.*

  /**
   * Resolve a user's credential by user id (used at login).
   */
  def findByUserId(customerId: Customer.Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.customerId === customerId)
        .result.headOption
