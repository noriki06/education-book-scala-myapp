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

実装メモ: 認証まわり（Member / MemberPassword / MemberSession）は、雛形の認証コード（Customer 系）をリネームして流用する（[naming.md](./naming.md)）。設計上の扱いは他と同じく、このアプリ自身のエンティティである。

## 用語（ユビキタス言語）

サービス全体で使う呼称をここで 1 つに決める。会議でも仕様書でもコードでも、この呼称だけを使い、言い換えない。

| 呼称（これで統一する） | 何を指すか | 言い換えない |
|---|---|---|
| イベント | 匿名で立ち、成立・終了までを含む 1 件 | 「募集」「企画」 |
| 立案者 | イベントを立てた本人（ずっと匿名） | 「主催者」「ホスト」「オーナー」 |
| 参加 | 会員がイベントに押した 1 件 | 「エントリー」「申込」 |
| 参加者 | 参加を押している会員（立案者を含む） | 「メンバー」「出席者」 |
| 会員 | ログインする本人 | 「社員」「アカウント」「ユーザー」 |
| 集合日時 | 実際に集まる日時。過ぎたら終了 | 「開催日時」「開始時刻」「集合日時」 |
| 締切 | 参加を受け付ける期限（既定＝集合日時） | 「応募期限」「エントリー期限」 |
| 成立人数 | 成立に必要な人数（立案者込み・2 以上） | 「定員」（上限の意味になる。定員は作らない） |
| 募集中 | 立ってから、成立か締切までの状態 | 「受付中」「オープン」 |
| 成立 | 参加が成立人数に達し、名前が開示された状態 | 「開催決定」「マッチング」 |
| 開示 | 成立した瞬間に、参加者の名前が全員に見えるようになること | 「公開」「発表」 |
| 終了 | 集合日時を過ぎた状態（当日中は一覧に残る） | 「開催済み」「クローズ」 |
| 不成立 | 締切までに人数が届かず終わったこと | 「流れた」「キャンセル」 |
| 取り消し | 立案者がイベントをやめること | 「取り下げ」「中止」 |
| 店 | レビューと提案の紐づけ先（社食・オフィス内の場所も含む） | 「お店」「店舗」「スポット」 |
| 提案 | 成立時にアプリが自動で出す店（3 件程度） | 「おすすめ」「レコメンド」 |
| レビュー | 星（5段階）と一言 | 「口コミ」「評価」 |
| 履歴 | 自分が立てた・押したイベントの一覧（本人だけが見る） | 「アクティビティ」「マイページ」 |

## ER 図

まず、データの繋がりだけを確認します。何が必須か・日時をどう持つかは、この図では決めません。

![匿名イベントアプリの ER 図。左に会員（Member）があり、会員 ID・公開用 UUID・メール・表示名・状態を持つ。その下に認証情報（MemberPassword）が 1 対 1、セッション（MemberSession）が 1 対多で繋がる。中央の参加（EventEntry）はイベント ID と会員 ID（成立まで誰にも見えない）を持ち、イベント × 会員の組で一意。会員とは 1 対多、右のイベント（Event）とは多対 1。イベントは公開用 ID・立案者の会員 ID（誰にも表示しない・点線の参照）・内容・集合日時・締切・成立人数・投稿先チャンネル・投稿の記録・成立日時・状態を持ち、店への参照は持たない。右下の店（Place）は Google の店への参照（手動登録なら無し）と店名（会員が付けた呼び名・誰でも直せる）を持つ。中央下のレビュー（PlaceReview）は店 ID・会員 ID（実名で出る）・星・一言を持ち、会員と店それぞれに 1 対多で繋がる。注記: この図で確認するのは「どこに何があって、いくつ繋がるか」だけ。イベントは店を知らず、place と member の世界もイベントを知らない。updatedAt / createdAt と各エンティティの管理 ID は省略](./img/er.svg)

- コンテキストをまたぐ参照は **ID だけ・一方向**（event → member、place → member）。**イベントは店を知らず**、place と member の世界もイベントを知らない
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
- `uuid` が公開用、`id` は DB の中だけ。開示・レビュー表示の API レスポンスで会員を指す識別子として `uuid` を使い、連番を外に晒さない

**型では守れない決めごと**
- `email` は一意
- 無効化（`IS_INACTIVE`）の操作・画面は作らない。問題発生時の DB 直接対応用の受け皿（→ [論点7](#論点7memberstatus-を残す)）

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
  startAt:        LocalDateTime,                   // 集合日時（イベントの始まり。過ぎたら終了）
  closeAt:        LocalDateTime,                   // 締切（既定＝集合日時。closeAt ≦ startAt）
  minEntries:     Short,                           // 成立人数（立案者込み・2 以上。達したら成立）
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
    case IS_FINISHED extends Status(code =  3) // 終了: 集合日時を過ぎた
```

**ここで型が語っていること**
- `slackMessageId` が `Option` なのは、**まだ投稿していない状態が存在する**から（教材の `Payment.transactionId` と同型）
- `confirmedAt` が `Option` なのは、成立しないまま終わるイベントがあるから。**成立後に取り消されたかどうか**は `state = IS_CANCELED` かつ `confirmedAt` が `Some` で読む（→ [論点1](#論点1取り消しを-1-つの状態で表しcode成立前後の違いは-confirmedat-で読む)）
- 立案者の匿名はデータでは守らない。`memberId` は普通に持ち、**表示しない**だけ（アクセス境界の話）

**型では守れない決めごと**
- `closeAt ≦ startAt`。過去の `startAt` では作れない
- `minEntries` は 2 以上
- `state` と `confirmedAt` は連動する: `IS_CONFIRMED`・`IS_FINISHED` なら必ず `Some`、`IS_OPEN`・`IS_FAILED` なら必ず `None`
- `slackMessageId` が `None` のまま成立・取り消しが起きたときの通知は、スレッド追記ではなく新規投稿として流す（bot の投稿は再試行し、失敗してもイベントは公開のまま）
- 状態遷移は `IS_OPEN → IS_CONFIRMED → IS_FINISHED` が本線。`IS_OPEN → IS_FAILED`（締切）、`IS_OPEN / IS_CONFIRMED → IS_CANCELED`（取り消し）。`IS_FINISHED` からはどこへも動かない

**保存されるデータの例**

| title | startAt | closeAt | minEntries | state | confirmedAt |
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
- イベントを立てた瞬間に、立案者の参加が 1 行できる。**立案者の行は参加の取り消しでは消せない**（やめたいときはイベントの取り消し）

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
  name:          String,                    // 店名（会員が付けた呼び名。登録時に Google から転記して確定）
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
- `name` は会員の入力データ（登録時に Google の名前を転記して確定。共有マスタなので誰でも直せる）。住所・営業時間などは保存せず、表示のたびに Google から取得する（→ [論点4](#論点4店名は写しではなく会員が付けた呼び名として持つ)）

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
- 削除は物理削除（行を消す）。訪問日は持たない——`createdAt` が実質の訪問日（→ [論点6](#論点6レビューに足さなかったもの)）

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
| `IS_FINISHED` | 3 | 終了（集合日時を過ぎた） | 当日中だけ出る。翌日から参加者の履歴のみ |
| `IS_FAILED` | -1 | 不成立（締切までに届かず） | 出ない。各自の履歴のみ |
| `IS_CANCELED` | -2 | 取り消し | 出ない。成立後の取り消しは参加者の履歴に見える |

業務には「成立前の取り消し」「成立後の取り消し」の 2 語があるが、**区分値は `IS_CANCELED` 1 つ**。どちらだったかは `confirmedAt` の有無で読む（呼称の数と区分値の数は一致しなくてよい）。

## エンティティをまたぐルール

- **成立判定**: `Event` の参加数（`EventEntry` の行数）が `minEntries` に達したら成立。立案者の 1 行を含めて数える。**判定は参加を受け付けた処理の中で即時に行う**（バッチが動かすのは締切→不成立と集合日時→終了だけ。判定が重なったら成立を優先）
- **開示する名簿は表示名の五十音順**。参加順・作成時刻・ID と相関する並びにしない（先頭＝立案者の特定を防ぐ）
- **進捗（2/4）・本人の履歴・店の平均点・提案 3 件は保存しない**。すべて都度計算する（提案＝社内レビューの高評価順、足りなければ Google の評価・近さで補完）
- 参加できるのは `IS_OPEN`（`closeAt` まで）と `IS_CONFIRMED`（`startAt` まで）のみ。取り消し（行の削除）も同じ 2 状態に限り、`IS_FAILED`・`IS_CANCELED`・`IS_FINISHED` の参加行は変更しない。締切を過ぎた `IS_OPEN` はバッチが `IS_FAILED` に、`startAt` を過ぎた `IS_CONFIRMED` は `IS_FINISHED` にする

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
- 立案時に店を選ぶ機能（場所を伝えたいときはタイトルに書く → 論点5）
- レビューとイベントの紐づけ
- 会員個人の位置情報の利用
- 集計・レポート機能
- 参加の取り消し履歴（取り消したという記録は残さない → 論点2）
- レビューへの写真の添付（要求は「星と一言」。画像ストレージ一式が増えるため作らない）

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

**判断:** `minEntries: Short`（2 以上）と `star: Short`（1〜5）は素の型で持ち、検証はモデルを作るときに行う。

どちらも使う場所が 1 か所で、型を作っても守れる範囲が広がらない（教材の `Money` / `Quantity` はコンテキスト横断で頻出だから型にした）。「増やしすぎない」に倒す。

**判断が変わる条件:** 星の集計・人数の比較ロジックが複数箇所に散り始めたら型に昇格する。

### 論点4：店名は「写し」ではなく「会員が付けた呼び名」として持つ

**判断:** `Place` に保存するのは `googlePlaceId`（規約上、無期限に保存してよい唯一のデータ）と `name` だけ。そして `name` は Google コンテンツの写しではなく、**会員の入力データ**として持つ——登録時に Google の検索結果を選ぶと店名がフォームに転記され、会員がそのまま（または編集して）確定する。共有マスタなので、あとから誰でも直せる。住所・営業時間・写真・Google の評価は保存せず、表示のたびに取得する。

place_id だけで運用できないかも検討したが、成立しない。①レビュー一覧を開くたびに店の数だけ Google を呼ぶことになる ②Google 障害時に店名すら出ず、デグレード方針（登録済みの店だけで出す）が成立しない ③**閉店すると place_id は無効になることがあり、「レビューは無期限に残す」という前提を Google のデータでは守れない**——5 年後のレビューが名無しになる。表示に要る最低限（名前）は自前で持つしかない。

「写し」でなく「会員の入力」とする理由: 規約上の性格が「Google コンテンツのキャッシュ（30 日期限）」から「place_id を参照するユーザー生成データ」に変わる。30 日ごとに名前をリフレッシュする案は、バッチと課金が増えるだけで閉店時の名無し問題を解決しないので採らない。

**代償:** Google 上の正式名と社内の呼び名がズレうる（ただし「裏のラーメン屋」のような社内の通り名を残せるのは、このアプリではむしろ利点）。この規約整理は一般的な実務慣行であって Google の保証ではない。

**判断が変わる条件:** 公開サービス化するとき（法務レビュー必須）。または Google がユーザー確定入力の扱いを明文化したとき。

### 論点5：イベントは場所を持たない

**判断:** `Event` から店への参照（placeId）も、場所の自由記述も持たない。場所を伝えたいときはタイトルに書く（「今日ランチ@サイゼ」）。

要求文には「店から場所を選んでもいい」とあったが、選ばないことのほうが多いと明記されており、店の「決定」も記録しないと確定済み。参照を持つと event → place の依存が 1 本増え、自由記述を持つと思いつきの雑な記入が溜まりやすい。イベントは「いつ・何人で」だけを持ち、店の世界（店リスト・レビュー・提案）とは完全に独立させる。

**代償:** 立案時に登録済みの店を選んでレビューへ飛ぶ、という導線は作れない。

**判断が変わる条件:** 「イベントから店のレビューを開きたい」という要望が実際に出たとき。そのときは placeId（参照）を足す——自由記述ではなく。

### 論点6：レビューに足さなかったもの

**判断:** `PlaceReview` は placeId・memberId・star・comment（＋updatedAt/createdAt）だけで確定。候補に挙がった 4 つを検討して、すべて落とした。

- **訪問日（visitedAt）**: 「食べたらそのまま書ける」が本線なので投稿日≒訪問日。後日投稿のズレは一言に書けば足りる。任意入力にしても大半が空欄になり、入力項目が増える分だけ軽さが削れる
- **編集済みフラグ**: `updatedAt ≠ createdAt` で計算できる
- **店名のスナップショット**: 店は消さない共有マスタで参照が切れない。呼び名が変われば過去レビューも新しい呼び名で表示されてよい（同じ店なので）
- **論理削除（deletedAt）**: 本人の意思で消すものは「消えて困る人がいない」（参加の取り消しと同じ整理）。論理削除にすると全クエリに除外条件が要り、書き忘れがバグになる。復元要求もない

**判断が変わる条件:** 「あとから思い出して書く」使い方が実際に多いと分かったら訪問日を足す。復元・監査の要求が出たら論理削除に切り替える。

### 論点7：Member.Status を残す

**判断:** 会員を無効にする操作・画面は今回作らないが、`Member.Status`（IS_ACTIVE / IS_INACTIVE）は持つ。

要件に退会・無効化は無い（レビューで「根拠のない区分値」と指摘された）。それでも残すのは、「不適切な事態が起きたら DB 直接対応」という確定済みの運用の**受け皿**が必要だから——state を 1 行変えるだけでログインを止められる。無効会員の既存の参加・レビューはそのまま残る（消えて困る人がいないものだけ消す原則）。

**代償:** 使われないかもしれない区分値を 1 つ持つ。

**判断が変わる条件:** 退会機能の要求が出たら、そのとき遷移・画面ごと設計する。
