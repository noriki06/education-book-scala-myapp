# 環境構築

本テンプレートは 3 モジュール（`app-lib` / `app-api` / `app`）から成り、
それぞれに必要なツールが少しずつ異なります。

| 用途 | 必要なもの |
|---|---|
| Scala（app-lib / app-api） | JDK 21 (Amazon Corretto) / sbt 1.12.11 / Scala 3.6.4 |
| DB（app-api） | Docker（MySQL コンテナ） |
| フロントエンド（app） | Node 22 系 / pnpm |
| ixias 解決 | AWS 認証情報（ixias は非公開の S3 Maven 配布） |

Scala 本体は sbt が `build.sbt` の `scalaVersion` に従って取得するため、個別インストールは不要です。

---

## 1. JDK (Amazon Corretto 21)

[Amazon Corretto 21](https://aws.amazon.com/jp/corretto/) をインストールし、`JAVA_HOME` を通します。

```bash
$ /usr/libexec/java_home -V          # インストール済み JDK 一覧
$ /usr/libexec/java_home -v 21        # 21 系のパス
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

## 2. sbt

```bash
$ brew install sbt
$ sbt --version    # sbt script version: 1.12.11
```

## 3. Docker（MySQL）

Docker Desktop 等を用意し、リポジトリ直下で MySQL を起動します。

```bash
$ docker compose up -d
$ docker compose ps        # mysql が healthy/up であることを確認
```

初回起動時に `etc/docker/mysql/data/init.sql` が実行され、
`app` データベースとユーザー（`app` / `pass`）が作成されます。

## 4. Node / pnpm（フロントエンド）

Node 22 系（`.nvmrc` 参照）を用意し、Corepack で pnpm を有効化します。

```bash
$ node -v            # v22.x
$ corepack enable    # pnpm を使えるようにする
$ cd app && pnpm install
```

## 5. AWS 認証情報（ixias の解決）

ixias-v3 は非公開の S3 Maven リポジトリ
（`s3://maven.ixias.net`）で配布されています。`sbt compile` 時に
`fm-sbt-s3-resolver` プラグインがこのリポジトリへアクセスするため、
**AWS 認証情報が設定されていないと ixias の解決に失敗します**。

`~/.aws/credentials` などに、ixias リポジトリを読める認証情報を設定してください
（設定方法・付与範囲は研修担当に確認してください）。

```ini
# ~/.aws/credentials（例）
[default]
aws_access_key_id     = <YOUR_KEY>
aws_secret_access_key = <YOUR_SECRET>
```

---

セットアップが完了したら、起動手順は [03_RUN.md](03_RUN.md) を参照してください。
