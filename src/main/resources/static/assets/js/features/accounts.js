/**
 * Accounts feature: loads accounts from backend, populates account dropdowns,
 * renders the sidebar (including per-account balances), and handles account CRUD.
 */

import { ACCOUNT, LAST_TX_ACCOUNT_KEY } from '../core/config.js';
import { $ } from '../core/dom.js';
import { j } from '../core/api.js';
import { esc, money, toNum } from '../core/format.js';
import { state } from '../core/state.js';
import { refreshTxPage } from './txController.js';
import { loadNet } from './net.js';
import { loadCategorySummary } from './categorySummary.js';

function ensureDefaultAccount() {
  const sel = $('#account');
  if (!sel || sel.options.length === 0) return;

  // 1) Try last-used account
  const last = localStorage.getItem(LAST_TX_ACCOUNT_KEY);
  if (last) {
    const exists = Array.from(sel.options).some(o => o.value === String(last));
    if (exists) {
      sel.value = String(last);
      return;
    }
  }

  // 2) Fallback: first load / no saved / saved invalid
  if (!sel.value) sel.value = sel.options[0].value;
}


export async function renderAccountsSidebar(list) {
  const wrap = document.getElementById('acctList');
  if (!wrap) return;

  wrap.innerHTML = list.map(a => `
    <div class="side-item">
      <div>
        <div class="side-title">${esc(a.name)}</div>
        <div class="side-sub"><span class="chip">${esc(a.type)}</span></div>
      </div>

      <div class="side-meta" id="accbal-${a.id}">${money(0)}</div>

      <div class="side-actions">
        <button class="side-btn" onclick="openBalanceDialog(${a.id}, '${esc(a.name)}')">Set Balance</button>
        <button class="side-btn" onclick="viewAccount(${a.id})">View Tx</button>
        <button class="side-btn danger" onclick="delAccount(${a.id})">Delete</button>
      </div>
    </div>
  `).join('');

  let net = 0;

  await Promise.all(list.map(async (a) => {
    try {
      const raw = await j(`${ACCOUNT}/balance/${a.id}`);
      const bal = toNum(raw);
      net += bal;

      const el = document.getElementById(`accbal-${a.id}`);
      if (el) {
        el.textContent = money(bal);
        el.classList.toggle('bad', bal < 0);
        el.classList.toggle('ok', bal >= 0);
      }
    } catch {}
  }));

  const netEl = document.getElementById('netWorth');
  if (netEl) netEl.textContent = money(net);
}

export async function loadAccounts() {
  const list = await j(ACCOUNT);
  const txSel = $('#account');
  const fSel = $('#faccount');

  txSel.innerHTML = '';
  fSel.innerHTML = '<option value="">(All)</option>';

  list.forEach(a => {
    txSel.insertAdjacentHTML('beforeend', `<option value="${a.id}">${esc(a.name)} (${a.type})</option>`);
    fSel.insertAdjacentHTML('beforeend', `<option value="${a.id}">${esc(a.name)}</option>`);
  });

  ensureDefaultAccount();
  await renderAccountsSidebar(list);
}

export async function viewAccount(id) {
  $('#faccount').value = String(id);
  // mimic applyFilters logic (no import cycle)
  state.acc = String(id);
  await refreshTxPage(0);
}

export async function delAccount(id) {
  if (!confirm('Delete this account? (Any transactions linked to it may block deletion)')) return;

  try {
    await fetch(`${ACCOUNT}/${id}`, { method: 'DELETE' });

    if ($('#faccount')?.value === String(id)) {
      $('#faccount').value = '';
      state.acc = '';
    }

    await loadAccounts();
    await refreshTxPage(0);
    await loadNet();
    await loadCategorySummary();
  } catch (e) {
    alert(`Could not delete account.\n\n${String(e?.message || e)}`);
  }
}

export function initAccountForm() {
  $('#acctForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const payload = {
      user: { id: 1 },
      name: $('#aname').value.trim(),
      type: $('#atype').value,
      openingBalance: parseFloat($('#aopen').value || 0),
    };

    await j(ACCOUNT, { method: 'POST', body: JSON.stringify(payload) });
    document.getElementById('acctCollapse')?.removeAttribute('open');
    $('#acctForm').reset();
    await loadAccounts();
  });

  $('#aclear').addEventListener('click', () => $('#acctForm').reset());
}

function fmtDate(d) {
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function openBalOverlay() {
  document.getElementById('balOverlay')?.removeAttribute('hidden');
}
function closeBalOverlay() {
  document.getElementById('balOverlay')?.setAttribute('hidden', '');
}

export function initBalanceSnapshotUI() {
  const ov = document.getElementById('balOverlay');
  const form = document.getElementById('balForm');
  if (!ov || !form) return;

  // ensure it's hidden on load
  closeBalOverlay();

  document.getElementById('balCancel')?.addEventListener('click', closeBalOverlay);
  document.getElementById('balClose')?.addEventListener('click', closeBalOverlay);

  // click outside the modal to close
  ov.addEventListener('click', (e) => {
    if (e.target === ov) closeBalOverlay();
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const id = Number(document.getElementById('balAccountId').value);
    const asOfDate = document.getElementById('balDate').value;
    const balance = Number(document.getElementById('balAmount').value);

    if (!id || !asOfDate || Number.isNaN(balance)) {
      alert('Please enter a valid date and balance.');
      return;
    }

    try {
      await j(`${ACCOUNT}/${id}/balance-snapshot`, {
        method: 'PUT',
        body: JSON.stringify({ asOfDate, balance })
      });

      closeBalOverlay();
      await loadAccounts();
      await loadNet();
      await loadCategorySummary();
    } catch (err) {
      alert(`Could not save balance snapshot.\n\n${String(err?.message || err)}`);
    }
  });
}

// Make it accessible for inline onclick
window.openBalanceDialog = (accountId, accountName) => {
  document.getElementById('balAccountId').value = String(accountId);

  // default to tomorrow
  const t = new Date();
  t.setDate(t.getDate() + 1);
  document.getElementById('balDate').value = fmtDate(t);

  document.getElementById('balAmount').value = '';
  openBalOverlay();
};