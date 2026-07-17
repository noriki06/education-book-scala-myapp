<script lang="ts">
  import { onMount } from 'svelte';
  import { ping, getMe, signup, login, logout, type Me } from '$lib/api/client';

  let health = $state('...');
  let me = $state<Me | null>(null);
  let error = $state('');

  let email = $state('');
  let password = $state('');
  let name = $state('');

  async function refresh() {
    me = await getMe();
  }

  async function onSignup(event: SubmitEvent) {
    event.preventDefault();
    error = '';
    try {
      await signup({ email, password, name });
      await refresh();
    } catch (e) {
      error = String(e);
    }
  }

  async function onLogin(event: SubmitEvent) {
    event.preventDefault();
    error = '';
    try {
      await login({ email, password });
      await refresh();
    } catch (e) {
      error = String(e);
    }
  }

  async function onLogout() {
    await logout();
    await refresh();
  }

  onMount(async () => {
    health = await ping().catch(() => 'NG');
    await refresh();
  });
</script>

<h1>education-book-scala-app</h1>
<p>API health (<code>/ping</code>): <strong>{health}</strong></p>

{#if error}<p style="color: crimson;">{error}</p>{/if}

{#if me}
  <p>ログイン中: <strong>{me.name}</strong>（{me.email}）</p>
  <button onclick={onLogout}>ログアウト</button>
{:else}
  <h2>新規登録</h2>
  <form onsubmit={onSignup}>
    <input placeholder="name" bind:value={name} />
    <input placeholder="email" type="email" bind:value={email} />
    <input placeholder="password (8文字以上)" type="password" bind:value={password} />
    <button type="submit">登録</button>
  </form>

  <h2>ログイン</h2>
  <form onsubmit={onLogin}>
    <input placeholder="email" type="email" bind:value={email} />
    <input placeholder="password" type="password" bind:value={password} />
    <button type="submit">ログイン</button>
  </form>
{/if}
