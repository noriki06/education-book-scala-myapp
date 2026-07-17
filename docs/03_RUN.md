# 起動・動作確認

スケルトンは `/ping` の疎通のみを含みます（ドメイン実装なし）。
3 モジュールを **依存の下（app-lib）から上（app）へ** 起動します。

```
app-lib  ──(sbt publishLocal)──▶  app-api  ──(HTTP :9000)──▶  app
```

## 1. app-lib を publishLocal

`app-api` は `app-lib` を artifact（`net.ixias %% education-book-app-lib`）として
依存します。まずローカルの Ivy リポジトリへ publish します。

```bash
$ cd app-lib
$ sbt publishLocal
$ cd ..
```

> ここで ixias の解決が走ります。失敗する場合は AWS 認証情報（[02_SETUP.md](02_SETUP.md) の 5）を確認してください。
> `app-lib` を修正したら、都度この `publishLocal` をやり直すと `app-api` に反映されます。

## 2. app-api（Play）を起動

```bash
$ cd app-api
$ sbt run
```

```bash
$ curl http://localhost:9000/ping
ok
```

> スケルトンの `/ping` は DB を使わないため、MySQL なしで起動できます。

## 3. app（SvelteKit）を起動

別ターミナルで：

```bash
$ cd app
$ pnpm install     # 初回のみ
$ pnpm dev
```

ブラウザで http://localhost:3000 を開くと、`/ping` の疎通結果が表示されます。
（`/ping` への通信は Vite の proxy 設定で :9000 へ転送されます）

## 4.（任意）DB を使うドメインを足すとき

1. `docker compose up -d` で MySQL を起動（初回に `app` DB/ユーザーを自動作成）
2. ドメインを `app-lib` に実装（[app-lib/README.md](../app-lib/README.md)）→ `sbt publishLocal`
3. マイグレーション SQL を `etc/database/migration/app/common/` に追加
4. 適用：`cd app-api && sbt flywayMigrate`
5. `app-api` にコントローラー / ルートを追加して再起動

---

## つまずきやすい点

| 症状 | 対処 |
|---|---|
| `sbt` で ixias が見つからない | AWS 認証情報を設定（[02_SETUP.md](02_SETUP.md) の 5） |
| `app-api` の起動で `education-book-app-lib` が見つからない | 先に `app-lib` で `sbt publishLocal` を実行したか確認 |
| `flywayMigrate` で DB 接続エラー | `docker compose ps` で MySQL 起動を確認、ポート 13306 が空いているか |
| フロントから API が 404 | `app-api`（:9000）を先に起動、`app/web/vite.config.ts` の proxy を確認 |
