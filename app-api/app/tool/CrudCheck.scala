/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package tool

import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global

import ixias.core.model.LocalDateTime
import ixias.db.slick.SlickDatabaseContext
import ixias.db.slick.driver.JdbcMySQLProfileExt

import edu.member.model.Member
import edu.event.model.{ Event, EventEntry }
import edu.place.model.{ Place, PlaceReview }
import edu.member.persistence.MemberRepository
import edu.member.persistence.table.MemberTable
import edu.event.persistence.{ EventRepository, EventEntryRepository }
import edu.event.persistence.table.{ EventTable, EventEntryTable }
import edu.place.persistence.{ PlaceRepository, PlaceReviewRepository }
import edu.place.persistence.table.{ PlaceTable, PlaceReviewTable }

/**
 * Writes one row of every entity, reads it back and compares. Types that only
 * exist on one side of the boundary are what this is really checking: an enum
 * stored as a number, an `Option` stored as NULL, a JST timestamp crossing the
 * driver twice.
 *
 * Everything it writes, it deletes again. Run it against a development
 * database only.
 *
 * Run from the app-api sbt shell:
 * {{{
 *   runMain tool.CrudCheck
 * }}}
 */
object CrudCheck:

  private val ctx = new SlickDatabaseContext:
    val driver = new JdbcMySQLProfileExt {}
    val ec     = global

  // Guice is what normally assembles these; a plain program has to do it by
  // hand. The constructors take the table and the context, nothing else.
  private val members     = new MemberRepository(new MemberTable(ctx), ctx)
  private val events      = new EventRepository(new EventTable(ctx), ctx)
  private val eventEntries = new EventEntryRepository(new EventEntryTable(ctx), ctx)
  private val places      = new PlaceRepository(new PlaceTable(ctx), ctx)
  private val placeReviews = new PlaceReviewRepository(new PlaceReviewTable(ctx), ctx)

  /** Waits for a `Future`, so each step reads as a plain statement. */
  private def await[A](f: scala.concurrent.Future[A]): A =
    Await.result(f, 30.seconds)

  private def check(label: String, wrote: Any, read: Any): Unit =
    val mark = if wrote == read then "ok  " else "FAIL"
    println(s"[$mark] $label: wrote=$wrote read=$read")
    if wrote != read then sys.error(s"$label did not survive the round trip")

  def main(args: Array[String]): Unit =
    // --[ Member ]------------------------------------------------------
    val memberId = await(members.add(Member(
      id       = None,
      uuid     = Member.UUID.generate,
      email    = "crud-check@example.com",
      name     = "疎通 太郎",
      nameKana = "ソツウタロウ",
    ).toWithNoId))
    val member = await(members.find(memberId)).get
    check("member.name",     "疎通 太郎",           member.v.name)
    check("member.nameKana", "ソツウタロウ",         member.v.nameKana)
    check("member.state",    Member.Status.IS_ACTIVE, member.v.state)

    // --[ Event ]-------------------------------------------------------
    // 12:00 JST に集合、締切は同時刻。Option の2列は None のまま保存する。
    val startAt = LocalDateTime.of(2026, 9, 1, 12, 0, 0)
    val eventId = await(events.add(Event(
      id             = None,
      code           = Event.Code.generate,
      memberId       = memberId,
      title          = "疎通確認ランチ",
      startAt        = startAt,
      closeAt        = startAt,
      minEntries     = 4,
      slackChannelId = "C0CRUDCHECK",
      slackMessageId = None,
      confirmedAt    = None,
    ).toWithNoId))
    val event = await(events.find(eventId)).get
    check("event.title",          "疎通確認ランチ",       event.v.title)
    check("event.state",          Event.Status.IS_OPEN,  event.v.state)
    check("event.minEntries",     4.toShort,             event.v.minEntries)
    check("event.startAt",        startAt,               event.v.startAt)
    check("event.slackMessageId", None,                  event.v.slackMessageId)
    check("event.confirmedAt",    None,                  event.v.confirmedAt)

    // 公開用 code から引けること（Slack の URL を踏んだ人が通る経路）
    val byCode = await(events.findByCode(event.v.code))
    check("event.findByCode", Some(eventId), byCode.map(_.id))

    // --[ EventEntry ]--------------------------------------------------
    val entryId = await(eventEntries.add(EventEntry(
      id       = None,
      eventId  = eventId,
      memberId = memberId,
    ).toWithNoId))
    check("eventEntry.count", 1, await(eventEntries.countByEvent(eventId)))
    check("eventEntry.find",  true, await(eventEntries.find(eventId, memberId)).isDefined)

    // --[ Place ]-------------------------------------------------------
    val placeUuid = Place.UUID.generate
    val placeId = await(places.add(Place(
      id            = None,
      uuid          = placeUuid,
      googlePlaceId = Some("ChIJcrudcheck"),
      name          = "疎通食堂",
    ).toWithNoId))
    val place = await(places.find(placeId)).get
    check("place.name",          "疎通食堂",              place.v.name)
    check("place.uuid",          placeUuid,             place.v.uuid)
    check("place.googlePlaceId", Some("ChIJcrudcheck"),  place.v.googlePlaceId)
    check("place.findByPrefix",  true, await(places.findByNamePrefix("疎通")).nonEmpty)
    check("place.findByUuid",    Some(placeId), await(places.findByUuid(placeUuid)).map(_.id))

    // --[ PlaceReview ]-------------------------------------------------
    val reviewId = await(placeReviews.add(PlaceReview(
      id       = None,
      placeId  = placeId,
      memberId = memberId,
      star     = 5,
      comment  = "型が往復して帰ってきた",
    ).toWithNoId))
    val review = await(placeReviews.find(reviewId)).get
    check("placeReview.star",    5.toShort, review.v.star)
    check("placeReview.comment", "型が往復して帰ってきた", review.v.comment)
    check("placeReview.byPlace", 1, await(placeReviews.findByPlace(placeId)).length)
    check("placeReview.rank",    Some(5.0), await(placeReviews.rankByStar(3)).headOption.flatMap(_._2))

    // --[ Update ]------------------------------------------------------
    // 更新も一周させる。書き換えたのは title だけで、他は元のまま残るはず。
    await(events.update(event.map(_.copy(title = "書き換え後"))))
    check("event.title(updated)", "書き換え後", await(events.find(eventId)).get.v.title)

    // --[ Cleanup ]-----------------------------------------------------
    await(placeReviews.delete(reviewId))
    await(places.delete(placeId))
    await(eventEntries.delete(entryId))
    await(events.delete(eventId))
    await(members.delete(memberId))
    check("cleanup.member", None, await(members.find(memberId)))
    check("cleanup.event",  None, await(events.find(eventId)))
    check("cleanup.place",  None, await(places.find(placeId)))

    println("[CrudCheck] all round trips passed")
