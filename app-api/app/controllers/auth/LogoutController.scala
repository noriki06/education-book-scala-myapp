/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package controllers.auth

import javax.inject.Inject
import scala.concurrent.Future

import play.api.mvc.Results.*

import mvc.{ AppControllerComponents, BaseAbstractController }
import mvc.auth.AuthCookies

/**
 * Logout.  POST /user/api/logout
 *
 * Deletes the session row (if any) and clears the session cookie.
 */
class LogoutController @Inject()(
  cc: AppControllerComponents,
) extends BaseAbstractController(cc):

  def invoke = Action.async: request =>
    val done = AuthCookies.readSession(request) match
      case Some(token) => repos.udb.userSession.deleteByToken(token).map(_ => ())
      case None        => Future.unit
    done.map(_ => AuthCookies.clearSession(NoContent))
