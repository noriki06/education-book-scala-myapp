/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.persistence

import javax.inject.*
import scala.concurrent.Future
import ixias.core.model.value.Token
import ixias.db.slick.{ SlickBaseRepository, SlickDatabaseContext }

import edu.customer.persistence.table.UserSessionTable

/**
 * Repository for UserSession persistence (server-side login sessions).
 */
@Singleton
class UserSessionRepository @Inject()(
  table: UserSessionTable,
  ctx:   SlickDatabaseContext
) extends SlickBaseRepository(table, ctx):
  import api.*

  /**
   * Resolve a session by its cookie token (the raw, unsigned form).
   */
  def findByToken(token: Token): Future[Option[EntityEmbeddedId]] =
    RunDBAction: slick =>
      slick
        .filter(_.token === token)
        .result
        .headOption

  /**
   * Delete a session by its cookie token (logout). Returns the rows removed.
   */
  def deleteByToken(token: Token): Future[Int] =
    RunDBAction: slick =>
      slick
        .filter(_.token === token)
        .delete
