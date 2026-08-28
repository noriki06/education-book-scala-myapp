/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.member

import javax.inject.*
import ixias.core.inject.IxiasModule
import edu.member.persistence.table.*

package object persistence:

  /** Guice module wiring the Member persistence singletons. */
  class Module extends IxiasModule:
    def bindings(): Unit =
      singleton[MemberTable]
      singleton[MemberPasswordTable]
      singleton[MemberSessionTable]
      singleton[MemberRepository]
      singleton[MemberPasswordRepository]
      singleton[MemberSessionRepository]
      singleton[RepositoryFacade]

  /** Aggregated repositories for the Member domain (injected by app-api). */
  @Singleton
  class RepositoryFacade @Inject()(
    val member:         MemberRepository,
    val memberPassword: MemberPasswordRepository,
    val memberSession:  MemberSessionRepository,
  )
