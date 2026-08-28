/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package mvc

import javax.inject.{ Inject, Singleton }

/**
 * Aggregated repositories exposed to controllers (via [[AppControllerComponents]]).
 *
 * Add one field per domain as you build them in app-lib.
 */
@Singleton
class AppRepositoryFacade @Inject() (
  val member: edu.member.persistence.RepositoryFacade,
)
