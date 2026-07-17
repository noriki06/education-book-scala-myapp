# education-book-scala-app

研修（第4部・実践）向けの **最小フルスタック雛形** です。
実務プロジェクト **XMIT** の 3 モジュール構成（`app` / `app-api` / `app-lib`）を、
学習に必要な骨格と配線だけへ縮小してあります。

サンプルとして **メール + パスワードによる最小のユーザー登録・セッション認証**（`user` ドメイン）を、
XMIT/ixias-v3 の流儀（`EntityModel` / `SlickTable` / `Repository` / 1コントローラー=1エンドポイント /
cats `EitherT` フロー）で一気通貫に実装しています。詳細は [docs/04_USER_SESSION.md](docs/04_USER_SESSION.md) を参照。

```
education-book-scala-app/
├── app-lib/   … ixias フレームワーク層（ドメインモデル + 永続化）  ← Scala / ixias-core
├── app-api/   … Play + ixias の Web/API 層（/ping と mvc 配線）    ← Scala / Play 3
├── app/       … SvelteKit フロントエンド（pnpm ワークスペース）    ← TypeScript / Svelte 5
└── etc/       … モジュール横断のインフラ/契約
    ├── docker/     … ローカル用 Docker 資材（MySQL）
    ├── database/   … DB マイグレーション（flyway）
    └── openapi/    … API 契約（OpenAPI 仕様）
```

## モジュールと責務

| モジュール | 役割 | 主な技術 | 対応する XMIT |
|---|---|---|---|
| `app-lib` | ドメインモデルと永続化（`EntityModel`/`SlickTable`/`Repository`）。ビルドすると `net.ixias %% education-book-app-lib` として publish される | Scala 3 / ixias-core / Slick | `app-lib/framework/xmit-*` |
| `app-api` | HTTP エンドポイント。`app-lib` を依存に取り、コントローラーから Repository を呼ぶ | Scala 3 / Play 3 / ixias-web / cats | `app-api` |
| `app` | 画面。API を叩く SPA | Node 22 / SvelteKit(Svelte 5) / Vite | `app` |
| `etc` | Docker / DB マイグレーション / OpenAPI を集約 | — | `app-api/docker`, `conf/db`, `app/**/openapi` |

依存の向き：`app` → (HTTP) → `app-api` → (sbt 依存) → `app-lib`

各モジュールの詳細は、それぞれの README を参照してください：
[app-lib](app-lib/README.md) / [app](app/README.md) / [etc](etc/README.md)。

## 前提環境

- JDK 21 (Amazon Corretto) / sbt 1.12.11 / Scala 3.6.4
- Docker（DB を使うドメインを実装する段階で必要。`/ping` だけなら不要）
- Node 22 系 + pnpm（フロントエンド用）
- **ixias-v3 を解決するための AWS 認証情報**（ixias は非公開の S3 Maven 配布）

セットアップ手順は [docs/02_SETUP.md](docs/02_SETUP.md) を参照してください。

## 使い方（クイックスタート）

このリポジトリは **雛形** です。まず自分の個人リポジトリへ付け替えてから進めます
（[docs/01_GIT.md](docs/01_GIT.md)）。起動手順の詳細は [docs/03_RUN.md](docs/03_RUN.md) にあります。

```bash
# app-lib をローカル publish（app-api が依存する artifact を用意）
$ cd app-lib && sbt publishLocal && cd ..

# app-api を起動（/ping だけなら MySQL 不要）
$ cd app-api && sbt run          # http://localhost:9000/ping → "ok"

# フロントエンドを起動（別ターミナル）
$ cd app && corepack enable && pnpm install && pnpm dev   # http://localhost:3000
```

ユーザー登録・ログイン（`user` ドメイン）を試すには MySQL とマイグレーションが必要です：

```bash
$ docker compose up -d                       # MySQL
$ (cd app-api && sbt flywayMigrate)          # udb_user / udb_user_session を作成
```

詳細は [docs/04_USER_SESSION.md](docs/04_USER_SESSION.md)。

## コーディング規約

XMIT に準拠します。要点は [CLAUDE.md](CLAUDE.md) を参照してください。
