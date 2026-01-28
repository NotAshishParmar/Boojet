export function money(n) {
  const v = (typeof n === 'number') ? n : (n?.amount ?? 0);
  return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'CAD' }).format(v);
}

export function toNum(x) {
  if (typeof x === 'number') return x;
  if (typeof x === 'string') return Number(x) || 0;
  return Number(x?.amount ?? 0) || 0;
}

export function esc(s) {
  return (s || '').replace(/[&<>"']/g, c => (
    { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]
  ));
}

export function colorize(el, v) {
  const num = (typeof v === 'number') ? v : (v?.amount ?? 0);
  el.classList.remove('ok', 'bad');
  el.classList.add(num >= 0 ? 'ok' : 'bad');
}
