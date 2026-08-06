/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer

import javax.inject.*
import ixias.core.inject.IxiasModule
import edu.customer.persistence.table.*

package object persistence:

  /** Guice module wiring the Customer persistence singletons. */
  class Module extends IxiasModule:
    def bindings(): Unit =
      singleton[CustomerTable]
      singleton[CustomerPasswordTable]
      singleton[CustomerSessionTable]
      singleton[CustomerRepository]
      singleton[CustomerPasswordRepository]
      singleton[CustomerSessionRepository]
      singleton[RepositoryFacade]

  /** Aggregated repositories for the Customer domain (injected by app-api). */
  @Singleton
  class RepositoryFacade @Inject()(
    val customer:         CustomerRepository,
    val customerPassword: CustomerPasswordRepository,
    val customerSession:  CustomerSessionRepository,
  )
