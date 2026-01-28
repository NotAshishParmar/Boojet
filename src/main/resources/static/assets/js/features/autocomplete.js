/**
 * Transaction description autocomplete:
 * fetches suggestion lists + optional details, renders a suggestion menu,
 * and fills category/account/income/amount when a suggestion is selected.
 */

import { API } from '../core/config.js';
import { j } from '../core/api.js';
import { esc } from '../core/format.js';

export function initDescriptionAutocomplete() {
  const input = document.getElementById('desc');
  const menu = document.getElementById('desc-ac');
  if (!input || !menu) return;

  const catEl = document.getElementById('cat');
  const incomeEl = document.getElementById('income');
  const accountEl = document.getElementById('account');
  const amountEl = document.getElementById('amount');

  let debounceTimer = null;
  let activeIndex = -1;
  let items = [];
  let lastReqId = 0;

  function openMenu() {
    if (!items.length) return;
    menu.hidden = false;
    input.setAttribute('aria-expanded', 'true');
  }

  function closeMenu() {
    menu.hidden = true;
    input.setAttribute('aria-expanded', 'false');
    activeIndex = -1;
  }

  function clearMenu() {
    menu.innerHTML = '';
    items = [];
    closeMenu();
  }

  function setActive(idx) {
    activeIndex = idx;
    const nodes = menu.querySelectorAll('.ac-item');
    nodes.forEach((n, i) => n.classList.toggle('active', i === activeIndex));
  }

  function renderMenu(list) {
    items = Array.isArray(list) ? list : [];

    if (!items.length) {
      menu.innerHTML = `<div class="ac-empty">No suggestions</div>`;
      menu.hidden = false;
      input.setAttribute('aria-expanded', 'true');
      activeIndex = -1;
      return;
    }

    menu.innerHTML = items.map((t, i) => `
      <div class="ac-item" role="option" data-idx="${i}">${esc(t)}</div>
    `).join('');

    activeIndex = -1;
    openMenu();
  }

  async function fetchSuggestions(q, limit = 15) {
    const reqId = ++lastReqId;
    const url = `${API}/suggestions?description=${encodeURIComponent(q)}&howMany=${limit}`;
    try {
      const list = await j(url);
      if (reqId !== lastReqId) return null;
      return list;
    } catch {
      return null;
    }
  }

  async function fetchSuggestionDetails(description) {
    const url = `${API}/suggestions/details?description=${encodeURIComponent(description)}`;
    return j(url);
  }

  function moneyToNumber(m) {
    if (m == null) return null;
    if (typeof m === 'number') return m;
    if (typeof m === 'string' && m.trim() !== '' && !Number.isNaN(Number(m))) return Number(m);
    if (typeof m === 'object' && m.amount != null) return Number(m.amount);
    return null;
  }

  async function applyDetailsIfFound(selectedDescription) {
    try {
      const d = await fetchSuggestionDetails(selectedDescription);
      if (!d) return;

      if (catEl && d.category) catEl.value = d.category;
      if (incomeEl && typeof d.income === 'boolean') incomeEl.value = d.income ? 'true' : 'false';

      if (accountEl && d.accountId != null) {
        const idStr = String(d.accountId);
        const exists = [...accountEl.options].some(o => o.value === idStr);
        if (exists) accountEl.value = idStr;
      }

      if (amountEl && (amountEl.value == null || amountEl.value.trim() === '')) {
        const amt = moneyToNumber(d.amount);
        if (amt != null && Number.isFinite(amt)) amountEl.value = String(amt);
      }
    } catch {}
  }

  async function selectValue(v) {
    input.value = v;
    clearMenu();
    await applyDetailsIfFound(v);
  }

  input.addEventListener('input', () => {
    clearTimeout(debounceTimer);

    const q = input.value.trim();
    if (q.length < 2) {
      clearMenu();
      return;
    }

    debounceTimer = setTimeout(async () => {
      const list = await fetchSuggestions(q, 15);
      if (!list) return;
      renderMenu(list);
    }, 150);
  });

  input.addEventListener('keydown', (e) => {
    const isOpen = !menu.hidden;
    const max = items.length - 1;

    if (!isOpen) {
      if (e.key === 'ArrowDown' && items.length && input.value.trim().length >= 2) {
        openMenu();
        setActive(0);
        e.preventDefault();
      }
      return;
    }

    if (e.key === 'ArrowDown') {
      setActive(activeIndex < max ? activeIndex + 1 : 0);
      e.preventDefault();
    } else if (e.key === 'ArrowUp') {
      setActive(activeIndex > 0 ? activeIndex - 1 : max);
      e.preventDefault();
    } else if (e.key === 'Enter') {
      if (activeIndex >= 0 && activeIndex <= max) {
        selectValue(items[activeIndex]);
        e.preventDefault();
      }
    } else if (e.key === 'Escape') {
      clearMenu();
      e.preventDefault();
    }
  });

  menu.addEventListener('mousedown', (e) => {
    const el = e.target.closest('.ac-item');
    if (!el) return;
    const idx = Number(el.dataset.idx);
    if (!Number.isNaN(idx) && items[idx] != null) selectValue(items[idx]);
  });

  document.addEventListener('mousedown', (e) => {
    const wrap = e.target.closest('.ac-wrap');
    if (wrap) return;
    clearMenu();
  });

  // expose hook for edit/reset like before
  window.__clearDescAutocomplete = clearMenu;
}
