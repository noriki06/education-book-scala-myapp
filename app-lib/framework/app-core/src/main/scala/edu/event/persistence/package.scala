/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.event

import javax.inject.*
import ixias.core.inject.IxiasModule
import edu.event.persistence.table.*

package object persistence:

  /** Guice module wiring the Event persistence singletons. */
  class Module extends IxiasModule:
    def bindings(): Unit =
      singleton[EventTable]
      singleton[EventEntryTable]
      singleton[EventRepository]
      singleton[EventEntryRepository]
      singleton[RepositoryFacade]

  /** Aggregated repositories for the Event domain (injected by app-api). */
  @Singleton
  class RepositoryFacade @Inject()(
    val event:      EventRepository,
    val eventEntry: EventEntryRepository,
  )
