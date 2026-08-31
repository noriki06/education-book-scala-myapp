/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.place

import javax.inject.*
import ixias.core.inject.IxiasModule
import edu.place.persistence.table.*

package object persistence:

  /** Guice module wiring the Place persistence singletons. */
  class Module extends IxiasModule:
    def bindings(): Unit =
      singleton[PlaceTable]
      singleton[PlaceReviewTable]
      singleton[PlaceRepository]
      singleton[PlaceReviewRepository]
      singleton[RepositoryFacade]

  /** Aggregated repositories for the Place domain (injected by app-api). */
  @Singleton
  class RepositoryFacade @Inject()(
    val place:       PlaceRepository,
    val placeReview: PlaceReviewRepository,
  )
