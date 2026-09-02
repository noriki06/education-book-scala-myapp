/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package controllers.place

import javax.inject.Inject
import scala.language.implicitConversions
import scala.concurrent.Future

import cats.data.EitherT
import cats.implicits.*
import ixias.core.util.Log.*
import play.api.libs.json.Json

import mvc.{ AppControllerComponents, BaseAbstractController }
import model.place.reads.JsValuePlaceReview
import edu.member.model.Member
import edu.place.model.{ Place, PlaceReview }

/**
 * Reviewing a place.  POST /place/api/review
 *
 * A review is written afresh every visit, so there is nothing to overwrite
 * here and no uniqueness to defend: the same member reviewing the same place
 * twice is two rows, and `createdAt` is what says when each visit happened.
 *
 * Reviews carry the writer's name — this is the one place in the application
 * where a member is deliberately not anonymous. A colleague's "I like this
 * one" is the whole reason the reviews exist.
 */
class PlaceReviewController @Inject()(
  cc: AppControllerComponents,
) extends BaseAbstractController(cc):

  /** What the request layer settles before the place is looked up. */
  private case class Draft(
    memberId:  Member.Id,
    placeUuid: Place.UUID,
    star:      Short,
    comment:   String,
  )

  def invoke = Action.async: request =>
    // Step-1: Resolve who is writing. Reviews are signed, and the signature
    // comes from the session rather than the body.
    EitherT(auth.resolveUser(request))
    // Step-2: Parse the JSON body.
    .flatMap: member =>
      EitherT
        .fromEither[Future](request.decode[JsValuePlaceReview])
        .map(member.id -> _)
    // Step-3: Validate the star and the comment.
    .subflatMap { case (memberId, body) => validated(memberId, body) }
    // Step-4: Find the place. A review of nothing is not a review, and an
    // unknown identifier is the same answer as a deleted one: 404.
    .flatMapF { draft =>
      repos.place.place.findByUuid(draft.placeUuid).map {
        case None => Left(NotFound("no such place"))
        case Some(place) => Right(PlaceReview(
          id       = None,
          placeId  = place.id,
          memberId = draft.memberId,
          star     = draft.star,
          comment  = draft.comment,
        ))
      }
    }
    // Step-5: Save it.
    .semiflatMap { review =>
      repos.place.placeReview.add(review.toWithNoId).map { reviewId =>
        info(s"[PLACE] reviewed placeId=${review.placeId} star=${review.star}")
        Created(Json.obj("id" -> reviewId.value))
      }
    }

  /**
   * The rules from 03_design.md's 入力値の範囲. The star range is CHECKed by
   * the database as well, because it has to hold whatever writes the row;
   * saying it here is what turns that into a sentence.
   */
  private def validated(memberId: Member.Id, body: JsValuePlaceReview): Either[Result, Draft] =
    for
      star    <- asStar(body.star)
      comment <- asComment(body.comment)
    yield Draft(
      memberId  = memberId,
      placeUuid = Place.UUID(body.placeUuid.trim),
      star      = star,
      comment   = comment,
    )

  /** 1..5. Nothing outside that is a rating. */
  private def asStar(raw: Int): Either[Result, Short] =
    if raw < 1 || raw > 5 then Left(BadRequest("star must be between 1 and 5"))
    else Right(raw.toShort)

  /** Up to 200 characters on a single line, and optional — a star alone will do. */
  private def asComment(raw: Option[String]): Either[Result, String] =
    val comment = raw.getOrElse("").trim
    if comment.length > 200 then Left(BadRequest("comment must be 200 characters or fewer"))
    else if comment.exists(c => c == '\n' || c == '\r') then Left(BadRequest("comment must be a single line"))
    else Right(comment)
