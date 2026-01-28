import { $ } from './core/dom.js';
import { state, initPageSizeFromStorage } from './core/state.js';

import { initDescriptionAutocomplete } from './features/autocomplete.js';
import { loadAccounts, initAccountForm, viewAccount, delAccount } from './features/accounts.js';
import { initTxForm, resetTxForm, getLastTxDateOrToday, editTx, delTx } from './features/transactions.js';
import { initPlans, loadPlans, toggleHours, delPlan } from './features/plans.js';
import { initFilters } from './features/filters.js';
import { refreshTxPage } from './features/txController.js';
import { loadNet } from './features/net.js';
import { loadCategorySummary } from './features/categorySummary.js';

function bootDefaults() {
  $('#date').value = getLastTxDateOrToday();
  $('#pfrom').value = new Date().toISOString().slice(0, 10);

  const d = new Date();
  $('#fyr').value = d.getFullYear();
  $('#fmo').value = d.getMonth() + 1;

  toggleHours();

  // page size persisted
  initPageSizeFromStorage();
  if ($('#psize')) $('#psize').value = String(state.size);
}

function attachGlobals() {
  // keep your inline onclick working
  window.editTx = editTx;
  window.delTx = delTx;
  window.viewAccount = viewAccount;
  window.delAccount = delAccount;
  window.delPlan = delPlan;
}

(async function boot() {
  attachGlobals();

  bootDefaults();
  initTxForm();
  initAccountForm();
  initPlans();
  initFilters();
  initDescriptionAutocomplete();

  await loadAccounts();
  resetTxForm();
  await refreshTxPage(0);
  await loadPlans();
  await loadNet();
  await loadCategorySummary();

  document.getElementById('refreshNet')?.addEventListener('click', async () => {
    await loadNet();
    await loadCategorySummary();
  });

  document.getElementById('refreshCat')?.addEventListener('click', (e) => {
    e.preventDefault();
    loadCategorySummary();
  });
})();
