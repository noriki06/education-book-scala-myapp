# education-book-scala-app

研修（第4部・実践）向けの **最小フルスタック雛形** です。
実務プロジェクトの 3 モジュール構成（`app` / `app-api` / `app-lib`）を、
学習に必要な骨格と配線だけへ縮小してあります。

サンプルとして **メール + パスワードによる最小のユーザー登録・セッション認証**（`user` ドメイン）を、
ixias-v3 の流儀（`EntityModel` / `SlickTable` / `Repository` / 1コントローラー=1エンドポイント /
cats `EitherT` フロー）で一気通貫に実装しています。

```
education-book-scala-app/
├── app/       … SvelteKit フロントエンド（pnpm ワークスペース）    ← TypeScript / Svelte 5
├── app-api/   … Play + ixias の Web/API 層                         ← Scala / Play 3
├── app-lib/   … ixias フレームワーク層（ドメインモデル + 永続化）  ← Scala / ixias-core
└── etc/       … モジュール横断のインフラ/契約
    ├── docker/     … ローカル用 Docker 資材（MySQL）
    ├── database/   … DB マイグレーション（flyway）
    └── openapi/    … API 契約（OpenAPI 仕様）＋ 型生成
```

## モジュールと責務

| モジュール | 役割 | 主な技術 |
|---|---|---|
| `app` | 画面。API を叩く SPA | Node 24 / SvelteKit(Svelte 5) / Vite |
| `app-api` | HTTP エンドポイント。`app-lib` を依存に取り、コントローラーから Repository を呼ぶ | Scala 3 / Play 3 / ixias-web / cats |
| `app-lib` | ドメインモデルと永続化（`EntityModel`/`SlickTable`/`Repository`）。ビルドすると `net.ixias %% education-book-app-lib` として publish される | Scala 3 / ixias-core / Slick |
| `etc` | Docker / DB マイグレーション / OpenAPI を集約 | — |

依存の向き：`app` → (HTTP) → `app-api` → (sbt 依存) → `app-lib`

各ディレクトリの詳細は [app](app/README.md) / [etc](etc/README.md) /
[etc/openapi](etc/openapi/README.md) / [etc/database](etc/database/README.md) を参照してください。

---

# セットアップ

## 前提環境

| 用途 | 必要なもの |
|---|---|
| Scala（`app-lib` / `app-api`） | JDK 21 (Amazon Corretto) / sbt 1.12.11 |
| DB（`app-api`） | Docker（MySQL コンテナ） |
| フロントエンド（`app`） | Node 24 系 / pnpm 9 |

Scala 本体（3.9.0-RC1）は sbt が `build.sbt` の `scalaVersion` に従って取得するため、
個別インストールは不要です。
ixias-v3 は `build.sbt` の resolver（`https://s3-ap-northeast-1.amazonaws.com/maven.ixias.net`）
から sbt が自動で取得するので、事前準備は要りません。

## 1. 個人リポジトリへ付け替える

このリポジトリは **雛形** です。実装は自分の個人リポジトリで進めます
（雛形リポジトリへ直接コミット／push はしないでください）。

```bash
$ git clone git@github.com:hybrid-tech-dev/education-book-scala-app.git
$ cd education-book-scala-app
```

GitHub 等で自分用の空リポジトリを作ってから、雛形を指している `origin` を
いったん外し、自分のリポジトリを `origin` として登録し直します。

```bash
$ git remote remove origin
$ git remote add origin git@github.com:<自分のアカウント>/education-book-scala-app.git

$ git remote -v      # 自分のリポジトリになっていることを確認
origin  git@github.com:<自分のアカウント>/education-book-scala-app.git (fetch)
origin  git@github.com:<自分のアカウント>/education-book-scala-app.git (push)

$ git push -u origin main
```

> `remove` してから `add` することで、雛形リポジトリへの誤 push を確実に防げます。

## 2. JDK (Amazon Corretto 21)

[Amazon Corretto 21](https://aws.amazon.com/jp/corretto/) をインストールし、`JAVA_HOME` を通します。

```bash
$ /usr/libexec/java_home -V      # インストール済み JDK 一覧
$ /usr/libexec/java_home -v 21   # 21 系のパス
```

`~/.zshrc` に追記して切り替えます。

```bash
export JAVA_HOME=`/usr/libexec/java_home -v 21`
PATH=$JAVA_HOME/bin:$PATH
```

```bash
$ source ~/.zshrc
$ java -version    # 21 系になっていることを確認
```

## 3. sbt

```bash
$ brew install sbt
$ sbt --version    # sbt script version: 1.12.11
```

## 4. Docker（MySQL）

Docker Desktop 等を用意し、リポジトリ直下で MySQL を起動します。

```bash
$ docker compose up -d
$ docker compose ps        # mysql が healthy であることを確認
```

初回起動時に `etc/docker/mysql/data/init.sql` が実行され、
`myapp` データベースとユーザー（`myapp` / `pass`）が作成されます。ポートは **13307** です。

> 教材の雛形リポジトリ（`education-book-scala-app`）は `app` を **13306** で公開します。
> こちらを **13307 / `myapp`** にしてあるのは、両方を同時に起動できるようにするためと、
> 接続先を間違えたときに「そんなデータベースは無い」で止めるためです
> （ポートだけ変えると、間違えた先にも `app` があるので黙って繋がってしまう）。

### コマンドラインで DB に接続する

ホストの MySQL クライアントから、公開ポート **13307** へ接続します。

```bash
$ brew install mysql-client          # 未インストールの場合
$ mysql -h 127.0.0.1 -P 13307 -umyapp -p myapp
mysql> SHOW TABLES;
mysql> SELECT * FROM member\G
```

ワンライナーで叩くこともできます。

```bash
$ mysql -h 127.0.0.1 -P 13307 -umyapp -p myapp -e "SHOW TABLES;"
```

| 項目 | 値 |
|---|---|
| ホスト / ポート | `127.0.0.1` / `13307` |
| データベース | `myapp` |
| ユーザー / パスワード | `myapp` / `pass` |
| root パスワード | `pass` |

> `brew install mysql-client` は PATH に入らないことがあります。その場合は
> `export PATH="/opt/homebrew/opt/mysql-client/bin:$PATH"` を `~/.zshrc` に追記してください。

> `-p` の直後にパスワードを書くと「Using a password on the command line
> interface can be insecure」と警告が出ます。ローカル開発用なので無視して
> 構いませんが、気になる場合は `-p` だけ書いて対話入力にしてください。

## 5. Node / pnpm（フロントエンド）

Node 24 系（`app/.nvmrc` 参照）を用意し、Corepack で pnpm を有効化します。

```bash
$ node -v            # v24.x
$ corepack enable    # pnpm を使えるようにする
$ cd app
$ pnpm install
```

### `node -v` が v24 でないとき（nvm でのバージョン切り替え）

nvm を使って 24 系へ切り替えます。**「一覧を見る → 入れる → 既定にする」** の 3 ステップです。

#### 1. 入れられるバージョンを確認する（`nvm ls-remote`）

```bash
$ nvm ls-remote --lts
       v20.20.2   (Latest LTS: Iron)
       v22.23.1   (Latest LTS: Jod)
       v24.18.0   (Latest LTS: Krypton)
```

`--lts` を付けると LTS だけに絞れます。`Latest LTS: Krypton` が付いている 24 系の
最新版を使います（`--lts` なしだと膨大な一覧が出るので注意）。

#### 2. インストールする（`nvm install`）

```bash
$ nvm install 24
$ nvm ls             # 入っているバージョン一覧（-> が現在の使用中）
```

バージョンを省略して `app` ディレクトリで `nvm install` と打つと、
`app/.nvmrc` の `24` を読んでその系列を取得します。

#### 3. 既定にして使う（`nvm alias default` → `nvm use default`）

`nvm use` は **そのシェルの中だけ** 有効なので、ターミナルを開き直すと元に戻ります。
既定バージョンを 24 にしておけば、以降は毎回指定する必要がありません。

```bash
$ nvm alias default 24     # 既定を 24 系にする
$ nvm use default          # 現在のシェルにも即反映
Now using node v24.15.0 (npm v11.12.1)

$ node -v
v24.15.0
```

> プロジェクト単位で切り替えたい場合は、`app` ディレクトリで `nvm use` と打つと
> `app/.nvmrc` を読んで自動で 24 系になります（バージョン番号を覚える必要がありません）。
>
> ```bash
> $ cd app
> $ nvm use
> Found '/path/to/education-book-scala-app/app/.nvmrc' with version <24>
> Now using node v24.15.0 (npm v11.12.1)
> ```

> nvm 自体が入っていない場合は
> [nvm-sh/nvm](https://github.com/nvm-sh/nvm#installing-and-updating) の手順でインストールし、
> シェルを開き直してください（`~/.zshrc` に `nvm.sh` を読み込む設定が追記されます）。

> `nvm: command not found` と出る場合、nvm はコマンドではなくシェル関数のため、
> `~/.zshrc` に以下が入っているか確認してください。
>
> ```bash
> export NVM_DIR="$HOME/.nvm"
> [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
> ```

---

# 起動

依存の下（`app-lib`）から上（`app`）へ順に起動します。

```
app-lib ──(sbt publishLocal)──▶ app-api ──(HTTP :9000)──▶ app (:3000)
```

sbt はコマンドごとに `sbt xxx` と打つと毎回 JVM の起動から始まり時間がかかります。
**`sbt` だけ打ってシェルを立ち上げ、その中でコマンドを実行する**のが基本です
（シェルを開いたままにしておけば、2 回目以降のビルドが大幅に速くなります）。

## 1. app-lib を publishLocal

`app-api` は `app-lib` を artifact として依存するため、先にローカルの Ivy へ publish します。

```bash
$ cd app-lib
$ sbt
sbt:education-book-app-lib> publishLocal
```

> `app-lib` を修正したら、都度この `publishLocal` をやり直さないと `app-api` に反映されません。
> sbt シェルは開いたままにして、修正のたびに `publishLocal` を打ち直すのが効率的です。

sbt シェルでよく使うコマンド：

| コマンド | 内容 |
|---|---|
| `compile` | コンパイルのみ |
| `publishLocal` | ローカル Ivy へ publish（`app-api` へ反映） |
| `~compile` | ファイル変更を監視して自動コンパイル（`Enter` で抜ける） |
| `reload` | `build.sbt` を編集したあと再読み込み |
| `exit`（または `Ctrl-D`） | sbt シェルを抜ける |

## 2. DB マイグレーション

別ターミナルで `app-api` の sbt シェルを開きます。

```bash
$ cd app-api
$ sbt
sbt:education-book-app-api> migrateAll
```

`etc/database/migration/<db>/common/*.sql` を適用します（`member` などが作られます）。

## 3. app-api（Play）を起動

同じ sbt シェルで続けて実行します。

```bash
sbt:education-book-app-api> run
```

```bash
$ curl http://localhost:9000/ping
ok
```

起動中は `Enter` で停止して sbt シェルへ戻れます。`app-api` のコードは
Play が変更を検知して自動で再コンパイルするため、`run` の入れ直しは不要です。

`app-api` の sbt シェルでよく使うコマンド：

| コマンド | 内容 |
|---|---|
| `run` | Play を開発モードで起動（:9000） |
| `migrateAll` | 全 DB へマイグレーション適用 |
| `migrateApp/flywayInfo` | `app` DB の適用状況を確認 |
| `compile` | コンパイルのみ |

## 4. app（SvelteKit）を起動

別ターミナルで：

```bash
$ cd app
$ pnpm dev
```

ブラウザで http://localhost:3000 を開くと、`/ping` の疎通結果と、
ユーザー登録・ログイン・ログアウトのフォームが表示されます。
API 通信は Vite の proxy で :9000 へ転送されるため、同一オリジン扱いになり
session Cookie がそのまま効きます（`app/web/vite.config.ts`）。

---

# 開発の流れ

| やること | どこで | コマンド |
|---|---|---|
| `app-lib`（モデル/テーブル/リポジトリ）を変更した | `app-lib` の sbt シェル | `publishLocal` |
| マイグレーション SQL を追加した | `app-api` の sbt シェル | `migrateAll` |
| API の契約（OpenAPI）を変更した | シェル | `./etc/openapi/build.sh` |
| フロントの型チェック | `app` ディレクトリ | `pnpm -r check` |

API の型はすべて `etc/openapi` の仕様から生成されます（`app/packages` = `@app/api`）。
**フロント側でリクエスト/レスポンス型を手書きしません** — 仕様を変えたら再生成し、
実装がズレていれば型エラーで落ちます。詳細は [etc/openapi/README.md](etc/openapi/README.md)。

## ユーザー登録・セッション認証のサンプル

| 層 | ファイル |
|---|---|
| model | `app-lib/framework/app-core/src/main/scala/edu/udb/model/` |
| 永続化 | `app-lib/framework/app-core/src/main/scala/edu/udb/persistence/` |
| 認証 | `app-api/app/mvc/auth/AuthProfile.scala`（ixias `TokenManagerViaCookie`） |
| コントローラー | `app-api/app/controllers/auth/`（クラス名は `XxxController`） |
| ルート | `app-api/conf/routes` |
| API 契約 | `etc/openapi/paths/member-*.yaml` |
| フロント | `app/web/src/lib/member.ts` |

Cookie は HMAC 署名付きで、値は `{署名}-{nonce}-{トークン}`。
DB (`member_session.token`) には未署名のトークンだけを保存し、リクエストごとに
署名を検証してから DB と突き合わせます。ログアウトは DB 行を削除するため即時失効します。
