/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package mvc

import com.google.inject.{ AbstractModule, Provides }
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import slick.jdbc.JdbcProfile

import ixias.db.slick.SlickDatabaseContext
import ixias.db.slick.driver.JdbcMySQLProfileExt

/**
 * Guice module providing the infrastructure bindings ixias needs
 * (the Slick JDBC profile and the database context).
 *
 * Enabled via `play.modules.enabled += mvc.AppModule` in application.conf.
 */
class AppModule extends AbstractModule:

  override def configure(): Unit = ()

  @Provides @Singleton
  def provideJdbcProfile: JdbcProfile =
    new JdbcMySQLProfileExt {}

  @Provides @Singleton
  def provideSlickDatabaseContext(
    jdbcProfile:      JdbcProfile,
    executionContext: ExecutionContext
  ): SlickDatabaseContext =
    new SlickDatabaseContext:
      val driver: JdbcProfile      = jdbcProfile
      val ec:     ExecutionContext = executionContext
