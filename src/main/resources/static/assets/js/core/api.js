export async function j(url, opts = {}) {
  const r = await fetch(url, Object.assign(
    { headers: { 'Content-Type': 'application/json' } },
    opts
  ));
  if (!r.ok) throw new Error(await r.text());
  return r.status === 204 ? null : r.json();
}
