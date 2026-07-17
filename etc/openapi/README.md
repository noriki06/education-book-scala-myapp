# etc/openapi

API 契約（OpenAPI 仕様）の置き場です。`openapi.yaml` がエントリポイントです。

- サーバー（`app-api`）に実装したエンドポイントを、ここへ記述して契約として管理します。
- XMIT ではこの仕様から、フロントエンドの型定義・API クライアントを生成します
  （`openapi-typescript` / `openapi-fetch`）。本テンプレートでは生成は行わず、
  `app/web/src/lib/api/client.ts` に最小のクライアントを手書きしています。

> 生成を導入する場合の例（XMIT 準拠）:
> `redocly bundle openapi.yaml -o .openapi.yaml && openapi-typescript .openapi.yaml -o <out>/schema.d.ts`
