/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package controllers.auth

import javax.inject.Inject
import scala.concurrent.Future

import play.api.libs.json.Json
import play.api.mvc.Results.*

import mvc.{ AppControllerComponents, BaseAbstractController }
import mvc.auth.AuthCookies

/**
 * Returns the currently logged-in user resolved from the session cookie.
 * GET /user/api/me  →  200 with the user, or 401 if not logged in.
 */
class MeController @Inject()(
  cc: AppControllerComponents,
) extends BaseAbstractController(cc):

  def invoke = Action.async: request =>
    AuthCookies.readSession(request) match
      case None =>
        Future.successful(Unauthorized(Json.obj("error" -> "no session")))
      case Some(token) =>
        repos.udb.userSession.findByToken(token).flatMap {
          case Some(session) =>
            repos.udb.user.find(session.v.uid).map {
              case Some(user) =>
                Ok(Json.obj(
                  "id"    -> user.id.value,
                  "uuid"  -> user.v.uuid.value,
                  "name"  -> user.v.name,
                  "email" -> user.v.email,
                ))
              case None =>
                Unauthorized(Json.obj("error" -> "user not found"))
            }
          case None =>
            Future.successful(Unauthorized(Json.obj("error" -> "invalid session")))
        }
