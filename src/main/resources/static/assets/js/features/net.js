/**
 * Monthly net calculation widget:
 * fetches expected/actual income, expenses, and net for the selected year/month,
 * updates the UI totals, and applies positive/negative styling.
 */

import { ANALYTICS } from '../core/config.js';
import { $ } from '../core/dom.js';
import { j } from '../core/api.js';
import { money, colorize } from '../core/format.js';

export async function loadNet() {
  const yr = $('#fyr').value, mo = $('#fmo').value;
  if (!(yr && mo)) { setNet({ expectedGrossIncome: 0, expectedNetIncome: 0, actualIncome: 0, expenses: 0, netExpected: 0, netActual: 0 }); return; }

  try {
    const r = await j(`${ANALYTICS}/net/${yr}/${mo}`);
    setNet(r);
  } catch {
    setNet({ expectedGrossIncome: 0, expectedNetIncome: 0, actualIncome: 0, expenses: 0, netExpected: 0, netActual: 0 });
  }
}

function setNet(r) {
  $('#n_expg').textContent = money(r.expectedGrossIncome);
  $('#n_expn').textContent = money(r.expectedNetIncome);
  $('#n_act').textContent = money(r.actualIncome);
  $('#n_expenses').textContent = money(r.expenses);
  $('#n_netexp').textContent = money(r.netExpected);
  $('#n_netact').textContent = money(r.netActual);
  colorize($('#n_netexp'), r.netExpected);
  colorize($('#n_netact'), r.netActual);
}
