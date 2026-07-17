/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package controllers.auth

import javax.inject.Inject
import scala.language.implicitConversions
import scala.concurrent.Future

import cats.data.EitherT
import cats.implicits.*
import ixias.core.util.Log.*
import play.api.libs.json.Json
import play.api.mvc.Results.*

import mvc.{ AppControllerComponents, BaseAbstractController }
import mvc.auth.AuthCookies
import model.udb.reads.JsValueSignup
import edu.udb.model.{ User, UserPassword, UserSession }

/**
 * User registration.  POST /user/api/signup  { email, password, name }
 *
 * Validates input, ensures the email is unused, stores the profile ([[User]])
 * and its credential ([[UserPassword]], PBKDF2-hashed) separately, issues a
 * login session, and sets the session cookie.
 */
class SignupController @Inject()(
  cc: AppControllerComponents,
) extends BaseAbstractController(cc):

  def invoke = Action.async: request =>
    // Step-1: Parse the JSON body.
    EitherT.fromEither[Future]:
      request.decode[JsValueSignup]
    // Step-2: Validate.
    .subflatMap: body =>
      val email = body.email.trim.toLowerCase
      val name  = body.name.trim
      if email.isEmpty then Left(BadRequest("email is required"))
      else if body.password.length < 8 then Left(BadRequest("password must be at least 8 characters"))
      else if name.isEmpty then Left(BadRequest("name is required"))
      else Right((email, body.password, name))
    // Step-3: Reject a duplicate email.
    .flatMapF { case (email, password, name) =>
      repos.udb.user.findByEmail(email).map {
        case Some(_) => Left(Conflict("email already registered"))
        case None    => Right((email, password, name))
      }
    }
    // Step-4: Create the user + credential + session, set the cookie.
    .semiflatMap { case (email, password, name) =>
      for
        uid <- repos.udb.user.add(User(
          id    = None,
          uuid  = User.UUID.generate,
          email = email,
          name  = name,
        ).toWithNoId)
        _     <- repos.udb.userPassword.add(UserPassword.hashed(uid, password))
        token <- newSession(uid)
      yield
        info(s"[AUTH] signup complete uid=${uid.value}")
        AuthCookies.putSession(token)(Created(Json.obj("id" -> uid.value)))
    }

  private def newSession(uid: User.Id): Future[String] =
    val token = java.util.UUID.randomUUID.toString
    repos.udb.userSession
      .add(UserSession(id = None, uid = uid, token = token).toWithNoId)
      .map(_ => token)
