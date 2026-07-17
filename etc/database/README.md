# etc/database

DB スキーママイグレーション（flyway）の置き場です。

```
etc/database/migration/<db>/common/V<日付>_<連番>__<説明>.sql
```

- 本テンプレートの DB 名は `app`（`etc/database/migration/app/common/`）。
- 命名は XMIT に準拠：`V20260717_01__create_xxx.sql`。
- 適用は `app-api` 側の flyway-sbt タスク：

```bash
$ cd app-api && sbt flywayMigrate
```

接続情報は `app-api/conf/application.conf` の `db.app` を読み取ります（`build.sbt` 参照）。
スケルトンには SQL はまだありません。最初のドメインを作るときに追加してください。
