/**
 * Filters + paging UI bindings: reads filter controls into shared state,
 * hooks up Apply/Reset/Prev/Next/page-size events, and triggers refreshes.
 */

import { $ } from '../core/dom.js';
import { state } from '../core/state.js';
import { refreshTxPage } from './txController.js';
import { loadNet } from './net.js';
import { loadCategorySummary } from './categorySummary.js';

export async function applyFilters() {
  state.acc = $('#faccount')?.value || '';
  state.cat = $('#fcat').value || '';
  state.yr  = $('#fyr').value || '';
  state.mo  = $('#fmo').value || '';
  await refreshTxPage(0);
}

export function initFilters() {
  $('#apply').addEventListener('click', async (e) => {
    e.preventDefault();
    await applyFilters();
    await loadNet();
    await loadCategorySummary();
  });

  $('#reset').addEventListener('click', async () => {
    state.acc = state.cat = state.yr = state.mo = '';
    if ($('#faccount')) $('#faccount').value = '';
    $('#fcat').value = ''; $('#fyr').value = ''; $('#fmo').value = '';

    await refreshTxPage(0);
    await loadNet();
    await loadCategorySummary();
  });

  $('#prev').addEventListener('click', async () => {
    if (state.page > 0) await refreshTxPage(state.page - 1);
  });

  $('#next').addEventListener('click', async () => {
    await refreshTxPage(state.page + 1);
  });

  $('#psize').addEventListener('change', async () => {
    state.size = parseInt($('#psize').value, 10) || 20;
    localStorage.setItem('size', state.size);
    await refreshTxPage(0);
  });
}
