/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package controllers.event

import java.time.Clock
import javax.inject.Inject
import scala.language.implicitConversions
import scala.concurrent.Future

import cats.data.EitherT
import cats.implicits.*
import ixias.core.model.LocalDateTime
import ixias.core.util.Log.*
import play.api.libs.json.Json

import mvc.{ AppControllerComponents, BaseAbstractController }
import model.event.reads.JsValueEventCreate
import edu.member.model.Member
// `Event` alone would resolve to the event-stream helper BaseController
// inherits, which wins over an import, so the model is brought in renamed.
import edu.event.model.{ Event as EventModel, EventEntry }

/**
 * Proposing an event.  POST /event/api/create
 *
 * Stores the event and, in the same breath, the proposer's own entry: "four
 * people and we go" counts them, so an event without that row is one short
 * from the moment it is published.
 *
 * The response carries only the code. It is the whole address of the event —
 * what the bot posts to Slack, and the one thing a visitor needs — while the
 * numeric id stays inside.
 */
class EventCreateController @Inject()(
  cc:    AppControllerComponents,
  clock: Clock,
) extends BaseAbstractController(cc):

  def invoke = Action.async: request =>
    // Step-1: Resolve who is proposing. It comes from the session cookie and
    // never from the body — an id taken from the body would let anyone
    // propose in someone else's name.
    EitherT(auth.resolveUser(request))
    // Step-2: Parse the JSON body.
    .flatMap: member =>
      EitherT
        .fromEither[Future](request.decode[JsValueEventCreate])
        .map(member.id -> _)
    // Step-3: Validate, turning the raw strings into the values Event holds.
    .subflatMap { case (memberId, body) => validated(memberId, body) }
    // Step-4: Publish the event, then the proposer's entry. The event has to
    // exist first: the entry points at its id.
    .semiflatMap { event =>
      for
        eventId <- repos.event.event.add(event.toWithNoId)
        _       <- repos.event.eventEntry.add(EventEntry(
                     id       = None,
                     eventId  = eventId,
                     memberId = event.memberId,
                   ).toWithNoId)
      yield
        info(s"[EVENT] published eventId=${eventId.value} minEntries=${event.minEntries}")
        Created(Json.obj("code" -> event.code.value))
    }

  /**
   * The rules this layer owns, from 03_design.md's 入力値の範囲. The database
   * checks the two that must hold whatever writes the row (`min_entries` and
   * `close_at <= start_at`); repeating them here is what turns a constraint
   * violation into a sentence the caller can act on.
   */
  private def validated(memberId: Member.Id, body: JsValueEventCreate): Either[Result, EventModel] =
    val now = LocalDateTime.now(clock)
    for
      title      <- asTitle(body.title)
      startAt    <- asTimestamp("startAt", body.startAt)
      _          <- require(startAt.isAfter(now), "startAt must be in the future")
      _          <- require(startAt.isBefore(now.plusYears(1)), "startAt must be within a year from now")
      // An omitted deadline means the meeting time itself.
      closeAt    <- body.closeAt.fold(Right(startAt))(asTimestamp("closeAt", _))
      _          <- require(closeAt.isAfter(now), "closeAt must be in the future")
      _          <- require(!closeAt.isAfter(startAt), "closeAt must not be later than startAt")
      minEntries <- asMinEntries(body.minEntries)
      channelId  <- asChannelId(body.slackChannelId)
    yield EventModel(
      id             = None,
      code           = EventModel.Code.generate,
      memberId       = memberId,
      title          = title,
      startAt        = startAt,
      closeAt        = closeAt,
      minEntries     = minEntries,
      slackChannelId = channelId,
      slackMessageId = None,
      confirmedAt    = None,
    )

  private def require(ok: Boolean, message: String): Either[Result, Unit] =
    Either.cond(ok, (), BadRequest(message))

  /** JST, no offset — the format `2026-09-02T12:00` is the only one accepted. */
  private def asTimestamp(field: String, raw: String): Either[Result, LocalDateTime] =
    Either.catchNonFatal(LocalDateTime.parse(raw.trim)).leftMap: _ =>
      BadRequest(s"$field must read like 2026-09-02T12:00 (JST, without an offset)")

  /** 1..60 characters on a single line. The place, if any, is written in here. */
  private def asTitle(raw: String): Either[Result, String] =
    val title = raw.trim
    if title.isEmpty then Left(BadRequest("title is required"))
    else if title.length > 60 then Left(BadRequest("title must be 60 characters or fewer"))
    else if title.exists(c => c == '\n' || c == '\r') then Left(BadRequest("title must be a single line"))
    else Right(title)

  /** 2..50, the proposer included. */
  private def asMinEntries(raw: Int): Either[Result, Short] =
    if raw < 2 || raw > 50 then Left(BadRequest("minEntries must be between 2 and 50"))
    else Right(raw.toShort)

  private def asChannelId(raw: String): Either[Result, String] =
    val id = raw.trim
    if id.isEmpty then Left(BadRequest("slackChannelId is required"))
    else if id.length > 64 then Left(BadRequest("slackChannelId must be 64 characters or fewer"))
    else Right(id)
