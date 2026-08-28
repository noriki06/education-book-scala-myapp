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

import mvc.{ AppControllerComponents, BaseAbstractController }
import model.member.reads.JsValueSignup
import edu.member.model.{ Member, MemberPassword }

/**
 * Member registration.  POST /user/api/signup  { email, password, name }
 *
 * Validates input, ensures the email is unused, stores the profile ([[Member]])
 * and its credential ([[MemberPassword]], PBKDF2-hashed) separately, issues a
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
      repos.member.member.findByEmail(email).map {
        case Some(_) => Left(Conflict("email already registered"))
        case None    => Right((email, password, name))
      }
    }
    // Step-4: Create the member + credential + session, set the cookie.
    .semiflatMap { case (email, password, name) =>
      for
        memberId <- repos.member.member.add(Member(
          id    = None,
          uuid  = Member.UUID.generate,
          email = email,
          name  = name,
        ).toWithNoId)
        _      <- repos.member.memberPassword.add(MemberPassword.hashed(memberId, password))
        result <- auth.open(memberId)(Created(Json.obj("id" -> memberId.value)))
      yield
        info(s"[AUTH] signup complete memberId=${memberId.value}")
        result
    }
