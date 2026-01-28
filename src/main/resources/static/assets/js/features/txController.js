import { API } from '../core/config.js';
import { state } from '../core/state.js';
import { $ } from '../core/dom.js';
import { j } from '../core/api.js';
import { money } from '../core/format.js';
import { renderTx } from './transactions.js';

function buildQuery(pageOverride) {
  const p = new URLSearchParams();
  p.set('page', pageOverride ?? state.page);
  p.set('size', state.size);
  if (state.acc) p.set('accountId', state.acc);
  if (state.cat) p.set('category', state.cat);
  if (state.yr && state.mo) { p.set('year', state.yr); p.set('month', state.mo); }
  return p.toString();
}

function updatePager(p) {
  $('#pages').textContent = `Page ${p.number + 1} of ${Math.max(1, p.totalPages)} • ${p.totalElements} items`;
  $('#prev').disabled = p.first;
  $('#next').disabled = p.last;
}

export async function refreshTxPage(page = 0) {
  state.page = page;

  const data = await j(`${API}?${buildQuery(page)}`);
  renderTx(data.content);
  updatePager(data);

  try { $('#balance').textContent = money(await j(`${API}/balance`)); } catch {}

  return data;
}
