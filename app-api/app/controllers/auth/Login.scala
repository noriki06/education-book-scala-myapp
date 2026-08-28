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
import model.member.reads.JsValueLogin

/**
 * Member login.  POST /user/api/login  { email, password }
 *
 * Looks up the member by email, verifies the password against the stored
 * PBKDF2 hash ([[edu.member.model.MemberPassword]]), issues a login session,
 * and sets the session cookie.
 */
class LoginController @Inject()(
  cc: AppControllerComponents,
) extends BaseAbstractController(cc):

  def invoke = Action.async: request =>
    // Step-1: Parse the JSON body.
    EitherT.fromEither[Future]:
      request.decode[JsValueLogin]
    // Step-2: Look up the member and verify the password.
    .flatMapF { body =>
      repos.member.member.findByEmail(body.email.trim.toLowerCase).flatMap {
        case None =>
          Future.successful(Left(Unauthorized("invalid email or password")))
        case Some(member) =>
          repos.member.memberPassword.findByMemberId(member.id).map {
            case Some(pw) if pw.v.verify(body.password) => Right(member)
            case _ => Left(Unauthorized("invalid email or password"))
          }
      }
    }
    // Step-3: Issue a session and set the cookie.
    .semiflatMap { member =>
      auth.open(member.id)(Ok(Json.obj("id" -> member.id.value))).map { result =>
        info(s"[AUTH] login memberId=${member.id.value}")
        result
      }
    }
