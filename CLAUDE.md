# education-book-scala-app — コーディング規約

実務プロジェクト XMIT の規約に準拠します。以下は本テンプレートで使う範囲の抜粋です。

## Scala Style

- Scala 3。Optional Braces（インデントベース構文）を優先する
- `.method:` + インデントで `{ }` を置き換える
- **例外: `for`/`yield` は `{ }` を使用する** — インデントだと可読性が下がるため
- `implicit`/`case` を伴うラムダも `{ }` を使う（Scala 3 の制約・可読性）

```scala
// Good
EitherT.fromEither[Future]:
  request.decode[JsValueCreateXxx]
.subflatMap: body =>
  ...
```

## Controller Pattern

```scala
class XxxController @Inject()(
  cc: AppControllerComponents,
) extends BaseAbstractController(cc):

  def invoke = Action.async: request =>
    ...
```

- **1 コントローラー = 1 エンドポイント**
- private メソッドへ細かく分割せず、`invoke` 内に処理を直接記述する
- `BaseAbstractController(cc)` を継承（`repos` へアクセス可能）
- ルーティングはサービス単位に `conf/<service>.routes` を分割し、`conf/routes` から
  `-> /<service>/api <service>.Routes` で include する

## cats EitherT フロー

```scala
EitherT.fromEither[Future]:   // 同期 Either（JSON parse 等）を持ち上げ
  request.decode[T]
.subflatMap: body =>           // 同期処理 → Either（バリデーション等）
  ...
.semiflatMap: data =>          // 常に成功する非同期処理（DB 操作等）
  ...
```

| メソッド | 用途 |
|---|---|
| `EitherT.fromEither[Future]` | 同期 `Either` を持ち上げ |
| `.subflatMap` | 同期処理で `Either` を返す（`A => Either[L,B]`） |
| `.semiflatMap` | 常に成功する非同期処理（`A => F[B]`） |
| `.flatMap` | `EitherT` を返す非同期処理 |
| `.leftMap` / `.map` | Left / Right の値変換 |

- 最終 `map` の結果が `Result` なら、ixias の `ResultSyntax` により
  `EitherT[Future, Result, Result] → Future[Result]` が暗黙変換される
  （`import scala.language.implicitConversions` が必要）

## Request Model（reads）

- 配置: `model/<service>/reads/`（例: `model.udb.reads`）
- Circe `Decoder` を companion で `deriveDecoder`
- コントローラーで `request.decode[T]`（ixias `RequestJsonSyntax`）してデコード

## Response

- `Ok(Json.toJson(...))` — 200 OK
- `Created(Json.obj(...))` — 201 Created
- `BadRequest("...")` — 400 / `NotFound("...")` — 404

## ドメイン / 永続化（ixias, app-lib）

- モデルは `EntityModel[Id]`。companion に `Id` / `WithNoId` / `EmbeddedId` の型別名
- 値オブジェクトは `EnumStatus[Short]`（`code` を持つ enum）
- テーブルは `SlickTable`、`*` で双方向マッピング（`updatedAt` は書き込み時に `Now` へ）
- リポジトリは `SlickBaseRepository` を継承（`add` / `get` / `update` … を継承）
- `RunDBAction(HostSpec.REPLICA)` で読み取りをレプリカへ
- エンティティ読み出しは `e.id.value`（ID）/ `e.v.<field>`（モデル値）
