# app-lib

ixias フレームワーク層（ドメインモデル + 永続化）です。ビルドすると
`net.ixias %% education-book-app-lib` として publish され、`app-api` が依存します。

```
app-lib/
├── build.sbt              # app-core を集約し、education-book-app-lib として publish
└── framework/
    └── app-core/          # ドメイン 1 まとまり分のライブラリ（XMIT の xmit-core 相当）
        └── src/main/scala/
            └── <root>/<domain>/
                ├── model/                 # EntityModel / 値オブジェクト(EnumStatus)
                └── persistence/
                    ├── table/             # SlickTable
                    ├── <Domain>.scala     # Repository (SlickBaseRepository)
                    └── package.scala      # IxiasModule + RepositoryFacade(集約)
```

サンプルとして `edu.udb` ドメイン（メール+パスワード認証の `User` / `UserPassword` / `UserSession`）が
入っています。これを手本に新しいドメインを追加する手順（例: `xxx`）:

1. `framework/app-core/src/main/scala/edu/xxx/model/Xxx.scala` に `EntityModel`
2. `.../persistence/table/Xxx.scala` に `SlickTable`
3. `.../persistence/Xxx.scala` に `Repository`（`SlickBaseRepository` を継承）
4. `.../persistence/package.scala` に `Module` と集約 `RepositoryFacade`
5. `app-api` の `mvc/AppRepositoryFacade.scala` に `val xxx: edu.xxx.persistence.RepositoryFacade` を追加
6. `sbt publishLocal`（app-lib）→ `app-api` を再ビルド

> XMIT ではドメインごとに `framework/xmit-core` / `xmit-dux` / `xmit-knowledge` の
> ように複数ライブラリへ分割します。増やすときは `build.sbt` にプロジェクトを足して
> `aggregate` / `dependsOn` してください。
