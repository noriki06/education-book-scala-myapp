/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

import { api, request, requestVoid } from '@app/api';

export type Member = { id: number; uuid: string; name: string; email: string };

export type AuthResult = { ok: true } | { ok: false; error: string };

/**
 * The logged-in member, or null when not authenticated.
 *
 * A 401 is the normal "logged out" answer here rather than a failure, so it
 * collapses to null instead of surfacing a message.
 */
export async function fetchMe(): Promise<Member | null> {
  try {
    const { data, response } = await api.GET('/member/api/me', {
      credentials: 'include',
    });
    if (!response.ok || !data) return null;
    return data;
  } catch {
    return null;
  }
}

/**
 * Register and sign in in one step — the backend issues the session cookie as
 * part of the signup response, so no follow-up login is needed.
 */
export async function signup(
  email: string,
  password: string,
  name: string,
  nameKana: string,
): Promise<AuthResult> {
  return requestVoid(
    api.POST('/member/api/signup', {
      body:        { email, password, name, nameKana },
      credentials: 'include',
    }),
    '登録に失敗しました',
  );
}

/**
 * Email/password login. The backend answers an unknown address and a wrong
 * password identically, so nothing here distinguishes them either.
 */
export async function login(email: string, password: string): Promise<AuthResult> {
  return requestVoid(
    api.POST('/member/api/login', {
      body:        { email, password },
      credentials: 'include',
    }),
    'ログインに失敗しました',
  );
}

/**
 * Revoke the session server-side and drop the cookie. Idempotent — the backend
 * answers 204 even when the caller has no valid cookie.
 */
export async function logout(): Promise<AuthResult> {
  return requestVoid(
    api.POST('/member/api/logout', { credentials: 'include' }),
    'ログアウトに失敗しました',
  );
}
