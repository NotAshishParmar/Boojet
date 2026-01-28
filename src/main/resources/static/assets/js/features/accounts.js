import { ACCOUNT } from '../core/config.js';
import { $ } from '../core/dom.js';
import { j } from '../core/api.js';
import { esc, money, toNum } from '../core/format.js';
import { state } from '../core/state.js';
import { refreshTxPage } from './txController.js';
import { loadNet } from './net.js';
import { loadCategorySummary } from './categorySummary.js';

function ensureDefaultAccount() {
  const sel = $('#account');
  if (sel && sel.options.length > 0 && !sel.value) sel.value = sel.options[0].value;
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
