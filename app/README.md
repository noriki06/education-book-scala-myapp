# app (frontend)

SvelteKit (Svelte 5) の pnpm ワークスペースです。XMIT の `app/` を最小化したもので、
本テンプレートでは 1 アプリ（`web`）のみを含みます。

```
app/
├── package.json          # ワークスペースのルート (pnpm -r ...)
├── pnpm-workspace.yaml    # packages: ["web"]
└── web/                   # SvelteKit アプリ (@app/web)
```

> XMIT はアプリを `app/app/*` 配下に置きますが、本テンプレートは 1 アプリのみで深さも抑えるため
> `app/web` に直接置いています。アプリを増やすときは `app/<name>` を足し、`pnpm-workspace.yaml` に追記します。

## セットアップ / 起動

```bash
# Node 22 系（.nvmrc）
$ corepack enable          # pnpm を有効化
$ pnpm install
$ pnpm dev                 # http://localhost:3000
```

`vite.config.ts` の `server.proxy` で `/ping` などを Play バックエンド
（`app-api`, http://localhost:9000）へ転送します。先に `app-api` を起動してください。
API を足すたびに、そのプレフィックス（例: `/user`）を proxy に追記します。

> XMIT では SMUI(Material) / OpenAPI 型生成 / i18n / 共有 `packages/` を使いますが、
> 本テンプレートでは「最小」を優先して省いています。
