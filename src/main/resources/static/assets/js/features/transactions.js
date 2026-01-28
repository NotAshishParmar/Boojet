/**
 * Transaction feature: renders transaction rows, handles add/edit/delete flows,
 * manages the transaction form state, and persists the "last used date".
 */

import { API, LAST_TX_DATE_KEY } from '../core/config.js';
import { $ } from '../core/dom.js';
import { j } from '../core/api.js';
import { esc, money } from '../core/format.js';
import { state } from '../core/state.js';
import { refreshTxPage } from './txController.js';
import { loadNet } from './net.js';
import { loadAccounts } from './accounts.js';

export function renderTx(list) {
  const tb = $('#tbl tbody');
  tb.innerHTML = '';

  list.forEach(t => {
    const tr = document.createElement('tr');
    const acctName = t.account?.name ?? (t.accountId ?? '');

    tr.innerHTML = `
      <td>${t.date}</td>
      <td>${esc(t.description)}</td>
      <td>${t.category}</td>
      <td>${esc(acctName)}</td>
      <td>${t.income ? 'INCOME' : 'EXPENSE'}</td>
      <td class="right">${money(t.amount)}</td>
      <td>
        <button onclick="editTx(${t.id})">Edit</button>
        <button onclick="delTx(${t.id})">Delete</button>
      </td>`;

    tb.appendChild(tr);
  });
}

export async function editTx(id) {
  const t = await j(`${API}/${id}`);
  $('#editId').value = id;
  $('#desc').value = t.description;
  window.__clearDescAutocomplete?.();

  $('#amount').value = (typeof t.amount === 'number' ? t.amount : t.amount.amount);
  $('#date').value = t.date;
  $('#cat').value = t.category;
  $('#income').value = t.income ? 'true' : 'false';
  if (t.account?.id) $('#account').value = String(t.account.id);

  document.querySelector('#f button[type="submit"]').textContent = 'Update';
  $('#cancel').style.display = 'inline-block';
}

export async function delTx(id) {
  if (!confirm('Delete this transaction?')) return;

  await fetch(`${API}/${id}`, { method: 'DELETE' });

  await refreshTxPage(state.page).catch(async () => {
    if (state.page > 0) await refreshTxPage(state.page - 1);
  });

  await loadNet();
  await loadAccounts();
}

export function getLastTxDateOrToday() {
  const saved = localStorage.getItem(LAST_TX_DATE_KEY);
  if (saved && /^\d{4}-\d{2}-\d{2}$/.test(saved)) return saved;
  return new Date().toISOString().slice(0, 10);
}

export function saveLastTxDate(d) {
  if (d && /^\d{4}-\d{2}-\d{2}$/.test(d)) localStorage.setItem(LAST_TX_DATE_KEY, d);
}

export function resetTxForm() {
  $('#f').reset();
  window.__clearDescAutocomplete?.();
  $('#editId').value = '';
  document.querySelector('#f button[type="submit"]').textContent = 'Add';
  $('#cancel').style.display = 'none';
  $('#date').value = getLastTxDateOrToday();
}

export function initTxForm() {
  $('#f').addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!$('#account').value) { alert('Please create/select an account first.'); return; }

    const payload = {
      description: $('#desc').value.trim(),
      amount: parseFloat($('#amount').value),
      date: $('#date').value,
      category: $('#cat').value,
      income: $('#income').value === 'true',
      account: { id: parseInt($('#account').value, 10) }
    };

    const id = $('#editId').value;
    if (id) await j(`${API}/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
    else await j(API, { method: 'POST', body: JSON.stringify(payload) });

    saveLastTxDate(payload.date);
    resetTxForm();

    await refreshTxPage(state.page);
    await loadNet();
    await loadAccounts();
  });

  $('#cancel').addEventListener('click', resetTxForm);
}
