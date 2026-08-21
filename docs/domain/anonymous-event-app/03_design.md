# 匿名イベントアプリ — 詳細要件定義

対象は [01_requirements.md](./01_requirements.md) で確定した全要件。コンテキストは会員 `member`／イベント `event`／店 `place` の 3 つ（[02_analysis.md](./02_analysis.md)）。追加するエンティティは次の 7 つ。

| エンティティ | テーブル | コンテキスト | 役割 |
|---|---|---|---|
| `Member` | `member` | member | 会員（ログインする本人） |
| `MemberPassword` | `member_password` | member | 認証情報 |
| `MemberSession` | `member_session` | member | ログインセッション |
| `Event` | `event` | event | イベント。匿名で立ち、成立・終了までを持つ |
| `EventEntry` | `event_entry` | event | 参加。誰がどのイベントに押したか |
| `Place` | `place` | place | 店。Google の店への参照＋手動登録 |
| `PlaceReview` | `place_review` | place | レビュー。星と一言・実名 |

## 用語 — 全体の用語集に追加する呼称

| 呼称（これで統一する） | 何を指すか | 言い換えない |
|---|---|---|
| イベント | 匿名で立ち、成立・終了までを含む 1 件 | 「募集」「企画」 |
| 参加 | 会員がイベントに押した 1 件 | 「エントリー」「申込」 |
| 会員 | ログインする本人 | 「社員」「アカウント」「ユーザー」 |
| 店 | レビューと提案の紐づけ先（社食・オフィス内の場所も含む） | 「お店」「店舗」「スポット」 |
| レビュー | 星（5段階）と一言 | 「口コミ」「評価」 |
| 成立 | 参加が成立人数に達し、名前が開示された状態 | 「開催決定」「マッチング」 |
| 取り消し | 立案者がイベントをやめること | 「取り下げ」「中止」 |
| 不成立 | 締切までに人数が届かず終わったこと | 「流れた」「キャンセル」 |

## ER 図

まず、データの繋がりだけを確認します。何が必須か・日時をどう持つかは、この図では決めません。

![匿名イベントアプリの ER 図。左に会員（Member）があり、会員 ID・公開用 UUID・メール・表示名・状態を持つ。その下に認証情報（MemberPassword）が 1 対 1、セッション（MemberSession）が 1 対多で繋がる。中央の参加（EventEntry）はイベント ID と会員 ID（成立まで誰にも見えない）を持ち、イベント × 会員の組で一意。会員とは 1 対多、右のイベント（Event）とは多対 1。イベントは公開用 ID・立案者の会員 ID（誰にも表示しない）・内容・集合日時・締切・成立人数・店 ID（任意）・投稿先チャンネル・投稿の記録・成立日時・状態を持つ。右下の店（Place）は Google の店への参照（手動登録なら無し）と店名の写しを持ち、イベントから点線（0..1・店を選んだときだけ）で結ばれる。中央下のレビュー（PlaceReview）は店 ID・会員 ID（実名で出る）・星・一言を持ち、会員と店それぞれに 1 対多で繋がる。注記: この図で確認するのは「どこに何があって、いくつ繋がるか」だけ。点線はコンテキストをまたぐ ID 参照（一方向）。updatedAt / createdAt は省略](./img/er.svg)

- コンテキストをまたぐ参照は **ID だけ・一方向**（event → member、event → place、place → member）。place と member の世界はイベントを知らない
- `EventEntry` は会員とイベントの交差。**(eventId, memberId) の組で一意**

## EntityModel

形だけを書き、メソッドや検証は書かない。型では表せない決めごとは文章で添える。

### `Member` — 会員

```scala
case class Member(
  id:        Option[Id],                    // 管理 ID（永続化前は None）
  uuid:      UUID,                          // 公開用 UUID（連番を外に晒さない）
  email:     String,                        // ログイン ID（一意）
  name:      String,                        // 表示名（開示・レビューに出る）
  state:     Status        = Status.IS_ACTIVE, // アカウント状態
  updatedAt: LocalDateTime = Now,           // データ更新日
  createdAt: LocalDateTime = Now            // データ作成日
) extends EntityModel[Id]

object Member:

  /** 会員の識別子と状態 */
  type   Id = Id.Repr
  object Id extends Entity.Id[Long]

  type   UUID = UUID.Repr
  object UUID extends Entity.Id[String]

  /** アカウント状態 */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_INACTIVE extends Status(code = -1) // 無効
    case IS_ACTIVE   extends Status(code =  1) // 有効
```

**ここで型が語っていること**
- `uuid` が公開用、`id` は DB の中だけ。連番を晒さない

**型では守れない決めごと**
- `email` は一意

**保存されるデータの例**

| email | name | state |
|---|---|---|
| ohira@example.co.jp | 大平 | IS_ACTIVE |

### `MemberPassword` — 認証情報 ／ `MemberSession` — セッション

```scala
case class MemberPassword(
  uid:       Member.Id,                     // 会員 ID（1 会員に 1 行）
  hash:      String,                        // パスワードハッシュ
  updatedAt: LocalDateTime = Now,           // データ更新日
  createdAt: LocalDateTime = Now            // データ作成日
) extends EntityModel[Member.Id]

case class MemberSession(
  uid:       Member.Id,                     // 会員 ID
  token:     String,                        // セッショントークン（一意）
  expiredAt: LocalDateTime,                 // 有効期限
  updatedAt: LocalDateTime = Now,           // データ更新日
  createdAt: LocalDateTime = Now            // データ作成日
) extends EntityModel[Member.Id]
```

**型では守れない決めごと**
- `MemberPassword` は会員と 1:1、`MemberSession` は 1:*。`token` は一意

### `Event` — イベント

```scala
case class Event(
  id:             Option[Id],                      // 管理 ID（永続化前は None）
  code:           Code,                            // 公開用 ID（URL に使う。連番を晒さない）
  memberId:       Member.Id,                       // 立案者（誰にも表示しない）
  title:          String,                          // 内容（「今日ランチ行きたい」）
  meetAt:         LocalDateTime,                   // 集合日時
  closeAt:        LocalDateTime,                   // 締切（既定＝集合日時。closeAt ≦ meetAt）
  capacity:       Short,                           // 成立人数（立案者込み。2 以上）
  placeId:        Option[Place.Id],                // 店（任意。選ばないことのほうが多い）
  slackChannelId: String,                          // 投稿先チャンネル（立案時に選ぶ）
  slackMessageId: Option[String],                  // bot 投稿の記録（投稿前は None）
  confirmedAt:    Option[LocalDateTime],           // 成立した日時（成立前は None）
  state:          Status        = Status.IS_OPEN,  // イベントの状態
  updatedAt:      LocalDateTime = Now,             // データ更新日
  createdAt:      LocalDateTime = Now              // データ作成日
) extends EntityModel[Id]

object Event:

  /** イベントの識別子 */
  type   Id = Id.Repr
  object Id extends Entity.Id[Long]

  /** 公開用 ID（URL 用） */
  type   Code = Code.Repr
  object Code extends Entity.Id[String]

  /** イベントの状態 */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CANCELED extends Status(code = -2) // 取り消し: 立案者がやめた
    case IS_FAILED   extends Status(code = -1) // 不成立: 締切までに人数が届かなかった
    case IS_OPEN     extends Status(code =  1) // 募集中
    case IS_CONFIRMED extends Status(code = 2) // 成立: 名前が開示されている
    case IS_FINISHED extends Status(code =  3) // 終了: 集合時刻を過ぎた
```

**ここで型が語っていること**
- `slackMessageId` が `Option` なのは、**まだ投稿していない状態が存在する**から（教材の `Payment.transactionId` と同型）
- `confirmedAt` が `Option` なのは、成立しないまま終わるイベントがあるから。**成立後に取り消されたかどうか**は `state = IS_CANCELED` かつ `confirmedAt` が `Some` で読む（→ [論点1](#論点1取り消しを-1-つの状態で表しcode成立前後の違いは-confirmedat-で読む)）
- 立案者の匿名はデータでは守らない。`memberId` は普通に持ち、**表示しない**だけ（アクセス境界の話）

**型では守れない決めごと**
- `closeAt ≦ meetAt`。過去の `meetAt` では作れない
- `capacity` は 2 以上
- `state` と `confirmedAt` は連動する: `IS_CONFIRMED`・`IS_FINISHED` なら必ず `Some`、`IS_OPEN`・`IS_FAILED` なら必ず `None`
- 状態遷移は `IS_OPEN → IS_CONFIRMED → IS_FINISHED` が本線。`IS_OPEN → IS_FAILED`（締切）、`IS_OPEN / IS_CONFIRMED → IS_CANCELED`（取り消し）。`IS_FINISHED` からはどこへも動かない

**保存されるデータの例**

| title | meetAt | closeAt | capacity | state | confirmedAt |
|---|---|---|---|---|---|
| 今日ラーメン行きたい | 8/21 12:00 | 8/21 12:00 | 4 | IS_CONFIRMED | 8/21 11:02 |
| 金曜スマブラやろう | 8/22 19:00 | 8/22 18:00 | 6 | IS_OPEN | — |
| 朝ラン誰か | 8/21 07:00 | 8/21 07:00 | 2 | IS_FAILED | — |

### `EventEntry` — 参加

```scala
case class EventEntry(
  id:        Option[Id],                    // 管理 ID（永続化前は None）
  eventId:   Event.Id,                      // どのイベントか
  memberId:  Member.Id,                     // 誰が押したか（成立まで誰にも表示しない）
  updatedAt: LocalDateTime = Now,           // データ更新日
  createdAt: LocalDateTime = Now            // データ作成日
) extends EntityModel[Id]

object EventEntry:

  /** 参加の識別子 */
  type   Id = Id.Repr
  object Id extends Entity.Id[Long]
```

**ここで型が語っていること**
- 状態を持たない。参加は「ある」か「ない」かだけ——取り消しは行を消す（→ [論点2](#論点2参加の取り消しは行を消す)）

**型では守れない決めごと**
- `(eventId, memberId)` は一意。同じイベントに 2 回参加できない
- イベントを立てた瞬間に、立案者の参加が 1 行できる

**保存されるデータの例**

| eventId | memberId | 意味 |
|---|---|---|
| 1 | 10 | 立案者の 1 行（公開と同時にできる） |
| 1 | 24 | 参加を押した人 |

### `Place` — 店

```scala
case class Place(
  id:            Option[Id],                // 管理 ID（永続化前は None）
  googlePlaceId: Option[String],            // Google の店への参照（手動登録なら None）
  name:          String,                    // 店名（Google からの写し。手動なら入力値）
  updatedAt:     LocalDateTime = Now,       // データ更新日
  createdAt:     LocalDateTime = Now        // データ作成日
) extends EntityModel[Id]

object Place:

  /** 店の識別子 */
  type   Id = Id.Repr
  object Id extends Entity.Id[Long]
```

**ここで型が語っていること**
- `googlePlaceId` が `Option` なのは、API に無い店・社食・オフィス内の場所を手動登録できるから

**型では守れない決めごと**
- `googlePlaceId` は（値があるとき）一意。同じ Google の店から 2 行作らない
- 保存するのは `googlePlaceId` と `name` だけ。住所・営業時間などは表示のたびに Google から取得する（→ [論点4](#論点4google-から写すのは-place_id-と店名だけ)）

**保存されるデータの例**

| googlePlaceId | name |
|---|---|
| ChIJ…abc | 麺屋いちば |
| — | 社食（本社 3F） |

### `PlaceReview` — レビュー

```scala
case class PlaceReview(
  id:        Option[Id],                    // 管理 ID（永続化前は None）
  placeId:   Place.Id,                      // どの店か
  memberId:  Member.Id,                     // 書いた人（実名で表示する）
  star:      Short,                         // 星（1〜5）
  comment:   String,                        // 一言（時間帯・行列もここに自由に書く）
  updatedAt: LocalDateTime = Now,           // データ更新日
  createdAt: LocalDateTime = Now            // データ作成日
) extends EntityModel[Id]

object PlaceReview:

  /** レビューの識別子 */
  type   Id = Id.Repr
  object Id extends Entity.Id[Long]
```

**ここで型が語っていること**
- イベントへの参照を持たない。レビューは店と会員だけの世界で完結する（確定済みの要件）

**型では守れない決めごと**
- `star` は 1〜5（→ [論点3](#論点3成立人数と星を値オブジェクトにしない)）
- 編集・削除できるのは本人だけ。1 人 1 店に複数書ける（一意制約なし）

**保存されるデータの例**

| placeId | memberId | star | comment |
|---|---|---|---|
| 5 | 24 | 4 | 11:45 なら並ばず入れた。味玉推し |

## 区分値の考え方

区分値は `Event.Status` の 1 つだけ。**正が「生きている・進んでいる」、負が「途中で終わった」**。

| 値 | code | 意味 | 一覧に出るか |
|---|---|---|---|
| `IS_OPEN` | 1 | 募集中 | 出る（進捗 2/4 つき） |
| `IS_CONFIRMED` | 2 | 成立。名前が開示されている | 出る |
| `IS_FINISHED` | 3 | 終了（集合時刻を過ぎた） | 当日中だけ出る。翌日から参加者の履歴のみ |
| `IS_FAILED` | -1 | 不成立（締切までに届かず） | 出ない。各自の履歴のみ |
| `IS_CANCELED` | -2 | 取り消し | 出ない。成立後の取り消しは参加者の履歴に見える |

業務には「成立前の取り消し」「成立後の取り消し」の 2 語があるが、**区分値は `IS_CANCELED` 1 つ**。どちらだったかは `confirmedAt` の有無で読む（呼称の数と区分値の数は一致しなくてよい）。

## エンティティをまたぐルール

- **成立判定**: `Event` の参加数（`EventEntry` の行数）が `capacity` に達したら成立。立案者の 1 行を含めて数える
- **進捗（2/4）・本人の履歴・店の平均点・提案 3 件は保存しない**。すべて都度計算する（提案＝社内レビューの高評価順、足りなければ Google の評価・近さで補完）
- 参加・取り消し・あと乗りができるのは `meetAt` まで。締切（`closeAt`）を過ぎた `IS_OPEN` はバッチが `IS_FAILED` に、`meetAt` を過ぎた `IS_CONFIRMED` は `IS_FINISHED` にする

## 置き場所（コンテキスト）

**判断:** `member`（Member / MemberPassword / MemberSession）、`event`（Event / EventEntry）、`place`（Place / PlaceReview）。`common` は作らない。

**理由:** 変更の理由・ライフサイクル・公開性が 3 つとも違う（詳細は [02_analysis.md](./02_analysis.md)）。common に入れてよい 3 条件を満たすデータが無い。

---

# 付録A：今回やらないこと

- 定員（上限人数）。あと乗り自由と整合させる
- 公開後のイベントの編集（取り消して立て直す）
- グループ分け・マルチテナント（1 社・全員に見える）
- 登録制限（メールドメイン制限・招待制）
- 通報・強制削除・管理者ロール・管理画面（設定は設定ファイル）
- 参加者からの手動の店提案（提案はアプリの自動のみ）
- レビューとイベントの紐づけ
- ユーザー個人の位置情報の利用
- 集計・レポート機能
- 参加の取り消し履歴（取り消したという記録は残さない → 論点2）

# 付録B：判断の記録

### 論点1：取り消しを 1 つの状態で表し、成立前後の違いは `confirmedAt` で読む

**判断:** `IS_CANCELED` は 1 つ。成立していたかは `confirmedAt`（成立日時）の有無で判定する。

見え方は「成立前の取り消し＝本人の履歴のみ／成立後の取り消し＝参加者にも見え、Slack にも流れる」と違うので、区別は必要になる。`IS_CANCELED_BEFORE` / `IS_CANCELED_AFTER` の 2 値に割る案もあったが、成立の事実はすでに `confirmedAt` として持っており、状態を 2 つにすると同じ事実を 2 か所に書くことになる。`confirmedAt` は成立時刻の記録・Slack への追記にも使うので、増えるものが無い。

**代償:** `state` だけを見ても成立後の取り消しかは分からず、必ず `confirmedAt` とセットで読む。この連動は「型では守れない決めごと」に明記した。

**判断が変わる条件:** 取り消し理由や取り消し時刻を独立に扱う要求が来たら、取り消しを別モデルに切り出す方が良くなる。

### 論点2：参加の取り消しは行を消す

**判断:** `EventEntry` に状態を持たせず、取り消しは物理削除。`(eventId, memberId)` の一意制約で再参加は再 INSERT になる。

「押したものが本人の履歴に残る」という要件は、**いま押している参加＋終了・不成立時点で残っていた参加**で満たせる。取り消した事実まで残すと「誰が断ったか」に近い記録が生まれ、「不参加という記録自体が存在しない」という確定済みの原則と衝突する。消えて困る人がいない（教材の「消していいのは、消えても誰も困らないデータだけ」を満たす、数少ないケース）。

**代償:** 「一度参加して取り消した」ことは本人にも分からなくなる。

**判断が変わる条件:** ドタキャン分析のような要求が来たとき。ただしそれは匿名の原則ごと見直す話になる。

### 論点3：成立人数と星を値オブジェクトにしない

**判断:** `capacity: Short`（2 以上）と `star: Short`（1〜5）は素の型で持ち、検証はモデルを作るときに行う。

どちらも使う場所が 1 か所で、型を作っても守れる範囲が広がらない（教材の `Money` / `Quantity` はコンテキスト横断で頻出だから型にした）。「増やしすぎない」に倒す。

**判断が変わる条件:** 星の集計・人数の比較ロジックが複数箇所に散り始めたら型に昇格する。

### 論点4：Google から写すのは place_id と店名だけ

**判断:** `Place` に保存するのは `googlePlaceId`（無期限に保存してよい）と `name`（表示の最低限）だけ。住所・営業時間・写真・評価は表示のたびに Google から取得する。

Google Places の規約では place_id 以外のデータのキャッシュに期限がある。全部写すと規約リスクと「古い情報を表示する」リスクを両方抱える。店名だけは、レビュー一覧を Google 呼び出しなしで表示するために写す（レビューの紐づけ先が名無しになるのは実用に耐えない）。

**代償:** 店名の写しも厳密には規約のキャッシュ期限の対象になり得る。社内利用の規模ではリスクは小さいと判断したが、公開サービス化するなら要再検討。

**判断が変わる条件:** Google が規約を厳格化したとき、または公開サービス化するとき。名前も都度取得に倒し、`Place` は place_id と手動店名だけになる。
