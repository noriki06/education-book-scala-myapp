/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package mvc.auth

import play.api.mvc.{ Cookie, DiscardingCookie, RequestHeader, Result }

/**
 * Minimal session-cookie helper.
 *
 * The `session` cookie holds an opaque token that is looked up in
 * `udb_user_session` to resolve the current user.
 *
 * NOTE: plain (unsigned) cookies keep the template minimal. The session token
 * is a random UUID validated by DB lookup. For production, prefer signed
 * cookies and `secure = true` (HTTPS).
 */
object AuthCookies:

  private val SESSION = "session"

  // Set `secure = true` once served over HTTPS.
  private val secure = false

  def putSession(token: String)(result: Result): Result =
    result.withCookies(Cookie(
      SESSION, token,
      maxAge   = Some(60 * 60 * 24 * 30), // 30 days
      httpOnly = true, secure = secure, sameSite = Some(Cookie.SameSite.Lax),
    ))

  def readSession(request: RequestHeader): Option[String] =
    request.cookies.get(SESSION).map(_.value)

  def clearSession(result: Result): Result =
    result.discardingCookies(DiscardingCookie(SESSION))
