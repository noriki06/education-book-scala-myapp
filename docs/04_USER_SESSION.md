# ユーザー登録・セッション認証（メール + パスワード / 最小実装）

`user` ドメインは、SSO を使わない **メール + パスワードによる最小のユーザー登録・
ログイン**の実装例です。認証状態は **session Cookie**（httpOnly）＋ `udb_user_session`
の突き合わせで保持します（JWT/OIDC/SSO は使いません）。

## エンドポイント

| メソッド / パス | 内容 |
|---|---|
| `POST /user/api/signup`  | 登録（`{email, password, name}`）→ PBKDF2 でハッシュ保存（`UserPassword`）→ セッション発行 → Cookie |
| `POST /user/api/login`   | ログイン（`{email, password}`）→ ハッシュ照合 → セッション発行 → Cookie |
| `POST /user/api/logout`  | セッション削除 + Cookie 破棄 |
| `GET  /user/api/me`      | session Cookie からログイン中ユーザーを返す |

## フロー

```
signup ─▶ 入力検証(email/パスワード長/name) ─▶ email 重複チェック
        ─▶ User + UserPassword 作成(PBKDF2) ─▶ UserSession 発行 ─▶ session Cookie
login  ─▶ email で User 取得 ─▶ UserPassword を PBKDF2 照合 ─▶ UserSession 発行 ─▶ session Cookie
me     ─▶ session Cookie ─▶ UserSession(find by token) ─▶ User
logout ─▶ session Cookie ─▶ UserSession 削除 ─▶ Cookie 破棄
```

## 関連ファイル

| 層 | ファイル |
|---|---|
| model | `app-lib/.../edu/udb/model/{User,UserPassword,UserSession}.scala` |
| 永続化 | `app-lib/.../edu/udb/persistence/**`（table / repository / package） |
| パスワード | `UserPassword`（`hashed` / `verify`）が `ixias.core.security.PBKDF2` を利用 |
| 認証 | `app-api/app/mvc/auth/AuthProfile.scala`（ixias `AuthProfile` / `TokenManagerViaCookie`） |
| Cookie 設定 | `app-api/conf/application.conf` の `cookie.session.*` |
| リクエスト | `app-api/app/model/udb/reads/{Signup,Login}.scala`（circe） |
| コントローラ | `app-api/app/controllers/auth/{Signup,Login,Logout,GetMyProfile}.scala`（クラス名は `XxxController`） |
| ルート | `app-api/conf/routes`（`/user/api/*` を直接定義） |
| スキーマ | `etc/database/migration/app/common/V20260717_01__create_user.sql` |

## 試し方

```bash
$ docker compose up -d                       # MySQL
$ (cd app-api && sbt migrateAll)          # udb_user / udb_user_session を作成
$ (cd app-lib && sbt publishLocal)
$ (cd app-api && sbt run)                    # :9000
$ (cd app && pnpm install && pnpm dev)       # :3000
```

ブラウザ http://localhost:3000 で登録 → 自動ログイン →「ログイン中: …」表示 → ログアウト、が試せます。

API 単体でも確認できます（Cookie を保存/送信する `-c` / `-b`）:

```bash
$ curl -c cj.txt -X POST http://localhost:9000/user/api/signup \
    -H 'Content-Type: application/json' \
    -d '{"email":"a@example.com","password":"password123","name":"太郎"}'
{"id":1}
$ curl -b cj.txt http://localhost:9000/user/api/me
{"id":1,"uuid":"...","name":"太郎","email":"a@example.com"}
$ curl -c cj.txt -X POST http://localhost:9000/user/api/login \
    -H 'Content-Type: application/json' \
    -d '{"email":"a@example.com","password":"password123"}'
```

## 注意（最小実装ゆえの割り切り）

- Cookie は ixias の `TokenManagerViaCookie` による **HMAC 署名付き**。値は
  `{署名}-{nonce}-{トークン}` で、DB (`udb_user_session.token`) には未署名の
  `Token` のみを保存します。改竄された Cookie は DB を引く前に 401 になります。
  本番では `cookie.session.secure = true`（HTTPS）にしてください。
- 署名鍵は `mvc.AppModule` の `MacSigner.fromSecretKey("secret-hash", "HmacSHA256")`
  にベタ書き。本番では設定/シークレットストアから読むこと。
- パスワードの複雑さ要件は「8 文字以上」のみ、セッションの回転は未実装。
  `UserSession.expiresAt` / `state` も **参照されていません**（`findByToken` は
  token 一致のみ）。期限切れ判定を入れるなら `UserSessionRepository` を拡張します。
- レート制限・メール確認・パスワード再設定などは含みません（最小のため）。
