/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.persistence.table

import javax.inject.*
import slick.jdbc.JdbcProfile
import io.circe.syntax.*
import io.circe.parser.decode
import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import ixias.core.model.*
import ixias.core.model.json.given
import ixias.db.slick.{ SlickTable, SlickDatabaseContext }
import ixias.core.persistence.HostSpec
import ixias.core.model.value.Token
import edu.customer.model.{ Cart, Customer }
import edu.shop.model.Shop

/**
 * Table Definition: Cart (`customer_cart`)
 *
 * `items` and `coupons` are stored as JSON rather than as their own tables.
 * That keeps a cart to one row — it is always read and written whole — at the
 * price of never being able to ask "how many carts contain this product"
 * without JSON functions, and of losing a concurrent edit: two tabs adding a
 * line each will leave only one of them.
 */
@Singleton
class CartTable @Inject()(ctx: SlickDatabaseContext)
  extends SlickTable[Cart.Id, Cart, JdbcProfile](ctx):
  import api.{ given, * }

  val ds = Map(
    HostSpec.PRIMARY -> DataSourceFactory("ixias.db.mysql://primary/app"),
    HostSpec.REPLICA -> DataSourceFactory("ixias.db.mysql://replica/app")
  )

  val query = TableQuery[Table]

  // --[ JSON codecs ]-------------------------------------------------
  given Encoder[Cart.BuyItem]   = deriveEncoder
  given Decoder[Cart.BuyItem]   = deriveDecoder
  given Encoder[Cart.UseCoupon] = deriveEncoder
  given Decoder[Cart.UseCoupon] = deriveDecoder

  /** Seq[A] <-> JSON text. Slick has no column type for a collection. */
  private def jsonColumn[A: { Encoder, Decoder }]: BaseColumnType[Seq[A]] =
    MappedColumnType.base[Seq[A], String](
      _.asJson.noSpaces,
      s => decode[Seq[A]](s).getOrElse(Nil)
    )

  given buyItemsColumn:   BaseColumnType[Seq[Cart.BuyItem]]   = jsonColumn[Cart.BuyItem]
  given useCouponsColumn: BaseColumnType[Seq[Cart.UseCoupon]] = jsonColumn[Cart.UseCoupon]

  case class Table(tag: Tag) extends BasicTable(tag, "customer_cart"):
    import Cart.*

    @pk  def id         = column[Id]                  ("id",          O.UInt64, O.AutoInc, O.PrimaryKey)
    @col def token      = column[Token]               ("token",       O.Varchar(255, Charset.Ascii))
    @col def customerId = column[Option[Customer.Id]] ("customer_id", O.UInt64)
    @col def shopId     = column[Shop.Id]             ("shop_id",     O.UInt64)
    @col def items      = column[Seq[BuyItem]]        ("items",       O.Json)
    @col def coupons    = column[Seq[UseCoupon]]      ("coupons",     O.Json)
    @col def state      = column[Status]              ("state",       O.Int16)
    @col def updatedAt  = column[LocalDateTime]       ("updated_at",  O.Timestamp(onUpdate = true))
    @col def createdAt  = column[LocalDateTime]       ("created_at",  O.Timestamp)

    def ukey01 = index("ukey01", token, unique = true)
    def key01  = index("key01",  customerId)

    def * = deriveColumns.mapTo[Cart](
      onWrite = _.copy(updatedAt = Now)
    )
