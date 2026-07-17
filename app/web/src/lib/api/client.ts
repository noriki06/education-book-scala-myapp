/**
 * Minimal typed API client for the Play backend (app-api).
 *
 * In development, requests are same-origin and forwarded to :9000 by the Vite
 * proxy (see vite.config.ts). Authentication is a plain session cookie set by
 * the backend, so no token handling is needed here.
 */

export async function ping(): Promise<string> {
  const res = await fetch('/ping');
  return res.text();
}

export interface Me {
  id: number;
  uuid: string;
  name: string;
  email: string;
}

/** Returns the logged-in user (from the session cookie), or null if not authenticated. */
export async function getMe(): Promise<Me | null> {
  const res = await fetch('/user/api/me');
  return res.ok ? res.json() : null;
}

export async function signup(input: { email: string; password: string; name: string }): Promise<void> {
  const res = await fetch('/user/api/signup', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input)
  });
  if (!res.ok) throw new Error(await res.text());
}

export async function login(input: { email: string; password: string }): Promise<void> {
  const res = await fetch('/user/api/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input)
  });
  if (!res.ok) throw new Error(await res.text());
}

export async function logout(): Promise<void> {
  await fetch('/user/api/logout', { method: 'POST' });
}
