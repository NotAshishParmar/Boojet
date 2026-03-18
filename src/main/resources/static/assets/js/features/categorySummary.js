/**
 * Monthly category summary table:
 * fetches totals by category for the selected year/month and renders
 * grouped summary by root/major category, with expandable child breakdown.
 */

import { $ } from '../core/dom.js';
import { esc, money } from '../core/format.js';
import { getCategoryById } from './categories.js';

function amountNumber(v) {
  if (typeof v === 'number') return v;
  if (typeof v === 'string') return parseFloat(v);
  return v?.amount ?? v?.value ?? 0;
}

function resolveCategory(summaryRow) {
  const raw = summaryRow?.category;
  const id = raw?.id;
  if (id == null) return raw ?? null;
  return getCategoryById(Number(id)) ?? raw;
}

function buildGroupedSummary(rows) {
  const groups = new Map();

  for (const r of rows) {
    const cat = resolveCategory(r);
    const amt = amountNumber(r.total);
    const safeAmt = Number.isFinite(amt) ? amt : 0;

    const isChild = !!cat?.parentId;
    const rootId = isChild ? cat.parentId : cat?.id;
    const rootName = isChild ? (cat.parentCode || 'Other') : (cat?.name || cat?.code || 'Other');

    const key = String(rootId ?? rootName);

    if (!groups.has(key)) {
      groups.set(key, {
        key,
        label: rootName,
        total: 0,
        children: []
      });
    }

    const g = groups.get(key);
    g.total += safeAmt;

    if (isChild) {
      g.children.push({
        id: cat.id,
        label: cat.name || cat.code || `#${cat.id}`,
        total: safeAmt
      });
    }
  }

  const result = [...groups.values()];

  for (const g of result) {
    g.children.sort((a, b) => Math.abs(b.total) - Math.abs(a.total));
  }

  result.sort((a, b) => Math.abs(b.total) - Math.abs(a.total));

  return result;
}

function renderEmpty(tb, msg) {
  tb.innerHTML = `<tr><td class="muted" colspan="2">${esc(msg)}</td></tr>`;
  $('#sumcat_total').textContent = money(0);
}

function renderGroupedTable(tb, groups) {
  tb.innerHTML = '';

  let grand = 0;

  for (const g of groups) {
    grand += g.total;
    const hasChildren = g.children.length > 0;

    const rootTr = document.createElement('tr');
    rootTr.className = 'sumcat-root';
    rootTr.dataset.group = g.key;

    rootTr.innerHTML = `
      <td>
        <div class="sumcat-label">
          <button
            type="button"
            class="sumcat-toggle ${hasChildren ? '' : 'is-hidden'}"
            data-act="toggle"
            data-group="${esc(g.key)}"
            aria-expanded="false"
            title="${hasChildren ? 'Show breakdown' : ''}"
          >▸</button>
          <span class="sumcat-root-name">${esc(g.label)}</span>
        </div>
      </td>
      <td class="right sum-amt ${g.total < 0 ? 'bad' : 'ok'}">${money(g.total)}</td>
    `;
    tb.appendChild(rootTr);

    for (const child of g.children) {
      const childTr = document.createElement('tr');
      childTr.className = 'sumcat-child';
      childTr.dataset.group = g.key;
      childTr.hidden = true;

      childTr.innerHTML = `
        <td><div class="sumcat-child-label">${esc(child.label)}</div></td>
        <td class="right sum-amt ${child.total < 0 ? 'bad' : 'ok'}">${money(child.total)}</td>
      `;
      tb.appendChild(childTr);
    }
  }

  $('#sumcat_total').textContent = money(grand);

  tb.onclick = (e) => {
    const btn = e.target.closest('button[data-act="toggle"]');
    if (!btn) return;

    const group = btn.dataset.group;
    if (!group) return;

    const expanded = btn.getAttribute('aria-expanded') === 'true';
    const next = !expanded;

    btn.setAttribute('aria-expanded', String(next));
    btn.textContent = next ? '▾' : '▸';

    tb.querySelectorAll(`tr.sumcat-child[data-group="${group}"]`).forEach(row => {
      row.hidden = !next;
    });
  };
}

export async function loadCategorySummary() {
  const yr = $('#fyr')?.value;
  const mo = $('#fmo')?.value;
  const tb = document.querySelector('#sumcat tbody');

  if (!(yr && mo)) {
    renderEmpty(tb, 'Select Year/Mon to see summary');
    return;
  }

  const url = `/transactions/summary/${yr}/${mo}`;

  try {
    const res = await fetch(url, { headers: { Accept: 'application/json' } });
    const text = await res.text();

    if (!res.ok) {
      renderEmpty(tb, `Failed (${res.status}): ${text}`);
      return;
    }

    let rows;
    try {
      rows = JSON.parse(text);
    } catch {
      renderEmpty(tb, 'Response was not JSON (see console)');
      return;
    }

    if (!Array.isArray(rows) || rows.length === 0) {
      renderEmpty(tb, 'No data for this month');
      return;
    }

    const groups = buildGroupedSummary(rows);
    renderGroupedTable(tb, groups);

  } catch (e) {
    console.error('Summary exception:', e);
    renderEmpty(tb, `Exception: ${String(e.message || e)}`);
  }
}