import { $ } from '../core/dom.js';
import { esc, money } from '../core/format.js';

export async function loadCategorySummary() {
  const yr = $('#fyr')?.value;
  const mo = $('#fmo')?.value;
  const tb = document.querySelector('#sumcat tbody');

  if (!(yr && mo)) {
    tb.innerHTML = `<tr><td class="muted" colspan="2">Select Year/Mon to see summary</td></tr>`;
    $('#sumcat_total').textContent = money(0);
    return;
  }

  const url = `/transactions/summary/${yr}/${mo}`;

  try {
    const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
    const text = await res.text();

    if (!res.ok) {
      tb.innerHTML = `<tr><td class="muted" colspan="2">Failed (${res.status}): ${esc(text)}</td></tr>`;
      $('#sumcat_total').textContent = money(0);
      return;
    }

    let rows;
    try { rows = JSON.parse(text); }
    catch {
      tb.innerHTML = `<tr><td class="muted" colspan="2">Response was not JSON (see console)</td></tr>`;
      $('#sumcat_total').textContent = money(0);
      return;
    }

    if (!Array.isArray(rows) || rows.length === 0) {
      tb.innerHTML = `<tr><td class="muted" colspan="2">No data for this month</td></tr>`;
      $('#sumcat_total').textContent = money(0);
      return;
    }

    tb.innerHTML = '';
    let grand = 0;

    rows.forEach(r => {
      const amt =
        (typeof r.total === 'number') ? r.total :
        (typeof r.total === 'string') ? parseFloat(r.total) :
        (r.total?.amount ?? r.total?.value ?? 0);

      grand += (Number.isFinite(amt) ? amt : 0);

      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${r.category}</td>
        <td class="right sum-amt ${amt < 0 ? 'bad' : 'ok'}">${money(amt)}</td>
      `;
      tb.appendChild(tr);
    });

    $('#sumcat_total').textContent = money(grand);

  } catch (e) {
    console.error('Summary exception:', e);
    tb.innerHTML = `<tr><td class="muted" colspan="2">Exception: ${esc(String(e.message || e))}</td></tr>`;
    $('#sumcat_total').textContent = money(0);
  }
}
