/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package tool

import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global

import slick.jdbc.JdbcProfile
import ixias.db.slick.{ SlickBaseTableManager, SlickDatabaseContext }
import ixias.db.slick.driver.JdbcMySQLProfileExt

import edu.place.persistence.table.{ PlaceTable, PlaceReviewTable }

/**
 * Prints the `CREATE TABLE` a table definition implies, so a flyway migration
 * can be written from it rather than by hand. Nothing is executed against the
 * database — the statement still needs its indexes, checks, engine clause and
 * comments added by a person before it becomes a migration.
 *
 * It lives in app-api, not app-lib, because reaching the database needs the
 * connection settings and the MySQL driver, and in this three-module layout
 * both belong to app-api. Setup tooling is kept out of `persistence`, which is
 * for the application's own reads and writes.
 *
 * Run from the app-api sbt shell:
 * {{{
 *   runMain tool.ShowCreateTable
 * }}}
 */
object ShowCreateTable:

  private object Manager extends SlickBaseTableManager[JdbcProfile]:
    val driver = new JdbcMySQLProfileExt {}

  private val ctx = new SlickDatabaseContext:
    val driver = new JdbcMySQLProfileExt {}
    val ec     = global

  def main(args: Array[String]): Unit =
    Await.result(Manager.showCreateTable(new PlaceTable(ctx)),       30.seconds)
    Await.result(Manager.showCreateTable(new PlaceReviewTable(ctx)), 30.seconds)
