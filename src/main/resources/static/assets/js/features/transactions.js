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

// UI-only state for the transaction form (not persisted)
let typeExplicitlyChosen = false;


function setIncomePill(isIncome) {
  const root = document.querySelector('.pill');
  if (!root) return;

  const hidden = $('#income');
  hidden.value = isIncome ? 'true' : 'false';

  root.querySelectorAll('.pill-btn').forEach(btn => {
    const v = btn.dataset.income === 'true';
    btn.classList.toggle('is-active', v === isIncome);
    btn.setAttribute('aria-pressed', (v === isIncome) ? 'true' : 'false');
  });
}

function initIncomePill() {
  const root = document.querySelector('.pill');
  if (!root) return;

  // click behavior
  root.addEventListener('click', (e) => {
    const btn = e.target.closest('.pill-btn');
    if (!btn) return;
    typeExplicitlyChosen = true;
    setIncomePill(btn.dataset.income === 'true');
  });


  // initialize from hidden input if present
  setIncomePill($('#income').value === 'true');
}

function syncPillFromAmountSign() {
  const raw = ($('#amount').value || '').trim();
  if (!raw) return;

  // user typed -... => Expense, +... => Income
  if (raw.startsWith('-')) setIncomePill(false);
  else if (raw.startsWith('+')) setIncomePill(true);
}


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
  
  typeExplicitlyChosen = true;
  setIncomePill(!!t.income);

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
  typeExplicitlyChosen = false;
  setIncomePill(true); // default to Income

}

export function initTxForm() {
  initIncomePill();

  $('#amount').addEventListener('keydown', (e) => {
    if (e.key === '-') { typeExplicitlyChosen = true; setIncomePill(false); }
    if (e.key === '+') { typeExplicitlyChosen = true; setIncomePill(true); }
  });

  // Handles paste / typing digits (since + may not stay in value for type="number")
  $('#amount').addEventListener('input', () => {
    const raw = String($('#amount').value || '').trim();
    if (!raw) return;

    if (raw.startsWith('-')) {
      typeExplicitlyChosen = true;
      setIncomePill(false);
      return;
    }

    // If user hasn't explicitly chosen type, plain number => Income
    if (!typeExplicitlyChosen) setIncomePill(true);
  });



  $('#f').addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!$('#account').value) { alert('Please create/select an account first.'); return; }

    const rawAmount = parseFloat($('#amount').value);
    const isIncome = $('#income').value === 'true';

    const payload = {
      description: $('#desc').value.trim(),
      amount: Math.abs(rawAmount),         // always positive to backend
      date: $('#date').value,
      category: $('#cat').value,
      income: isIncome,                    // type from pill
      account: { id: parseInt($('#account').value, 10) }
    };

    const id = $('#editId').value;
    if (id) await j(`${API}/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
    else await j(API, { method: 'POST', body: JSON.stringify(payload) });

    saveLastTxDate(payload.date);
    resetTxForm();

    //auto focus description block after submit
    $('#desc')?.focus();
    $('#desc')?.select();

    await refreshTxPage(state.page);
    await loadNet();
    await loadAccounts();
  });

  $('#cancel').addEventListener('click', resetTxForm);
}
