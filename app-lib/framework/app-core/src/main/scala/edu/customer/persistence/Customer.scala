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

import edu.customer.persistence.table.CustomerTable

/**
 * Repository for Customer persistence.
 */
@Singleton
class CustomerRepository @Inject()(
  table: CustomerTable,
  ctx:   SlickDatabaseContext
) extends SlickBaseRepository(table, ctx):
  import api.*

  /**
   * Resolve a user by login email (used by signup/login).
   */
  def findByEmail(email: String): Future[Option[EntityEmbeddedId]] =
    RunDBAction(HostSpec.REPLICA): slick =>
      slick
        .filter(_.email === email)
        .result.headOption
