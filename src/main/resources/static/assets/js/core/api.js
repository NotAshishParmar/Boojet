/**
 * Fetch helpers for calling the backend API.
 * Provides a JSON wrapper that throws on non-2xx and handles 204 responses.
 */

export async function j(url, opts = {}) {
  const r = await fetch(url, Object.assign(
    { headers: { 'Content-Type': 'application/json' } },
    opts
  ));
  if (!r.ok) throw new Error(await r.text());
  return r.status === 204 ? null : r.json();
}
