/**
 * Transaction feature: renders transaction rows, handles add/edit/delete flows,
 * manages the transaction form state, and persists the "last used date".
 *
 * NOTE:
 * - income is derived from category.type on the backend
 * - user selects category via picker (catId + display), not a <select>
 */

import { API, LAST_TX_DATE_KEY, LAST_TX_ACCOUNT_KEY } from '../core/config.js';
import { $ } from '../core/dom.js';
import { j } from '../core/api.js';
import { esc, money } from '../core/format.js';
import { state } from '../core/state.js';
import { refreshTxPage } from './txController.js';
import { loadNet } from './net.js';
import { loadAccounts } from './accounts.js';

// ---------------- RENDER ----------------

function categoryLabel(t) {
  // Try to be resilient to different shapes during migration:
  // - t.category could be string (legacy)
  // - t.category could be object { id, code, name, parentCode }
  // - t.categoryName could exist (if you add it later)
  if (typeof t.category === 'string') return t.category;
  if (t.category?.name && t.category?.parentCode) return `${t.category.parentCode} → ${t.category.name}`;
  if (t.category?.name) return t.category.name;
  if (t.category?.code) return t.category.code;
  if (t.categoryName) return t.categoryName;
  if (t.categoryCode) return t.categoryCode;
  return '—';
}

export function renderTx(list) {
  const tb = $('#tbl tbody');
  tb.innerHTML = '';

  list.forEach(t => {
    const tr = document.createElement('tr');

    const catLabel = t.categoryName ?? t.categoryCode ?? (t.categoryId ? `#${t.categoryId}` : '—');
    const acctLabel = t.accountName ?? (t.accountId ? `#${t.accountId}` : '—');

    const acctName = t.account?.name ?? (t.accountId ?? '');
    const amt = signedAmount(t);

    tr.innerHTML = `
      <td>${t.date}</td>
      <td>${esc(t.description)}</td>
      <td>${esc(catLabel)}</td>
      <td>${esc(acctLabel)}</td>
      <td class="right num ${amt < 0 ? 'bad' : 'ok'}">${money(amt)}</td>
      <td>
        <button onclick="editTx(${t.id})">Edit</button>
        <button onclick="delTx(${t.id})">Delete</button>
      </td>`;

    tb.appendChild(tr);
  });
}

// ---------------- EDIT / DELETE ----------------

export async function editTx(id) {
  const t = await j(`${API}/${id}`);

  $('#editId').value = id;
  $('#desc').value = t.description;
  window.__clearDescAutocomplete?.();

  // amount: your backend might return Money object or number
  $('#amount').value = (typeof t.amount === 'number' ? t.amount : (t.amount?.amount ?? t.amount?.value ?? 0));
  $('#date').value = t.date;

  // category
  $('#catId').value = t.categoryId ? String(t.categoryId) : '';
  $('#catDisplay').value = t.categoryName
    ? `${t.categoryName}`
    : (t.categoryCode ?? '');

  // account
  $('#account').value = t.accountId ? String(t.accountId) : '';

  // hint (derived)
  $('#incomeHint').textContent = t.income ? 'Income' : 'Expense';

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

// ---------------- Persist last used ----------------

export function getLastTxDateOrToday() {
  const saved = localStorage.getItem(LAST_TX_DATE_KEY);
  if (saved && /^\d{4}-\d{2}-\d{2}$/.test(saved)) return saved;
  return new Date().toISOString().slice(0, 10);
}

export function saveLastTxDate(d) {
  if (d && /^\d{4}-\d{2}-\d{2}$/.test(d)) localStorage.setItem(LAST_TX_DATE_KEY, d);
}

function saveLastTxAccountId(id) {
  if (id) localStorage.setItem(LAST_TX_ACCOUNT_KEY, String(id));
}

// ---------------- FORM ----------------

export function resetTxForm() {
  $('#f').reset();
  window.__clearDescAutocomplete?.();
  $('#editId').value = '';
  document.querySelector('#f button[type="submit"]').textContent = 'Add';
  $('#cancel').style.display = 'none';
  $('#date').value = getLastTxDateOrToday();

  // category picker fields
  $('#catId').value = '';
  $('#catDisplay').value = '';
  $('#incomeHint').textContent = 'Type auto';
}

export function initTxForm() {
  $('#account').addEventListener('change', () => {
    const v = parseInt($('#account').value, 10);
    if (!Number.isNaN(v)) saveLastTxAccountId(v);
  });

  $('#f').addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!$('#account').value) { alert('Please create/select an account first.'); return; }
    if (!$('#catId').value) { alert('Please pick a category.'); return; }

    const rawAmount = parseFloat($('#amount').value);
    if (!Number.isFinite(rawAmount)) { alert('Amount is invalid'); return; }

    const payload = {
      description: $('#desc').value.trim(),
      amount: Math.abs(rawAmount),       
      date: $('#date').value,
      categoryId: parseInt($('#catId').value, 10),
      accountId: parseInt($('#account').value, 10),
    };

    const id = $('#editId').value;
    if (id) await j(`${API}/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
    else await j(API, { method: 'POST', body: JSON.stringify(payload) });

    saveLastTxAccountId(payload.accountId);
    saveLastTxDate(payload.date);
    resetTxForm();

    $('#desc')?.focus();
    $('#desc')?.select();

    await refreshTxPage(state.page);
    await loadNet();
    await loadAccounts();
  });


  $('#cancel').addEventListener('click', resetTxForm);
}


// ---------------- HELPER ----------------

function amountNumber(a){
  return (typeof a === 'number') ? a : (a?.amount ?? 0);
}

function signedAmount(t){
  const raw = amountNumber(t.amount);

  // If backend already sends negative expenses, keep it
  if (raw < 0) return raw;

  // Otherwise, use "income" flag to assign sign
  return t.income ? raw : -raw;
}