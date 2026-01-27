/* ------------ helpers ------------ */
const API = '/transactions';
const PLAN = '/plan';
const ACCOUNT = '/account';
const state = { page: 0, size: 20, acc: '', cat: '', yr: '', mo: '' };
const $ = s => document.querySelector(s);

function money(n) {
  const v = (typeof n === 'number') ? n : (n?.amount ?? 0);
  return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'CAD' }).format(v);
}
function esc(s) { return (s || '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c])); }
async function j(url, opts = {}) {
  const r = await fetch(url, Object.assign({ headers: { 'Content-Type': 'application/json' } }, opts));
  if (!r.ok) throw new Error(await r.text());
  return r.status === 204 ? null : r.json();
}


/* ------------ pagination ------------ */
function buildQuery(pageOverride) {
  const p = new URLSearchParams();
  p.set('page', pageOverride ?? state.page);
  p.set('size', state.size);
  if (state.acc) p.set('accountId', state.acc);
  if (state.cat) p.set('category', state.cat);
  if (state.yr && state.mo) { p.set('year', state.yr); p.set('month', state.mo); }
  return p.toString();
}
async function fetchPage(page = 0) {
  state.page = page;
  const data = await j(`${API}?${buildQuery(page)}`);
  renderTx(data.content);
  updatePager(data);
  try { $('#balance').textContent = money(await j(`${API}/balance`)); } catch { }
  return data;
}
function updatePager(p) {
  $('#pages').textContent = `Page ${p.number + 1} of ${Math.max(1, p.totalPages)} • ${p.totalElements} items`;
  $('#prev').disabled = p.first; $('#next').disabled = p.last;
}


/* ------------ accounts ------------ */
function ensureDefaultAccount() {
  const sel = $('#account');
  if (sel && sel.options.length > 0 && !sel.value) sel.value = sel.options[0].value;
}
async function loadAccounts() {
  const list = await j(ACCOUNT);
  const txSel = $('#account'); const fSel = $('#faccount');
  txSel.innerHTML = ''; fSel.innerHTML = '<option value="">(All)</option>';
  list.forEach(a => {
    txSel.insertAdjacentHTML('beforeend', `<option value="${a.id}">${esc(a.name)} (${a.type})</option>`);
    fSel.insertAdjacentHTML('beforeend', `<option value="${a.id}">${esc(a.name)}</option>`);
  });
  ensureDefaultAccount();
  await renderAccountsSidebar(list);
}

window.viewAccount = async function (id) {
  $('#faccount').value = String(id);
  await applyFilters();
};
$('#acctForm').addEventListener('submit', async e => {
  e.preventDefault();
  const payload = {
    user: { id: 1 },
    name: $('#aname').value.trim(),
    type: $('#atype').value,
    openingBalance: parseFloat($('#aopen').value || 0)
  };
  await j(ACCOUNT, { method: 'POST', body: JSON.stringify(payload) });
  $('#acctForm').reset();
  await loadAccounts();
});
$('#aclear').addEventListener('click', () => $('#acctForm').reset());


/* ------------ transactions ------------ */
const LAST_TX_DATE_KEY = 'boojet:lastTxDate';

function renderTx(list) {
  const tb = $('#tbl tbody'); tb.innerHTML = '';
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
window.editTx = async function (id) {
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
};
window.delTx = async function (id) {
  if (!confirm('Delete this transaction?')) return;
  await fetch(`${API}/${id}`, { method: 'DELETE' });
  await fetchPage(state.page).catch(async () => {
    if (state.page > 0) await fetchPage(state.page - 1);
  });
  await loadNet(); await loadAccounts();
};
$('#f').addEventListener('submit', async e => {
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
  await fetchPage(state.page); await loadNet(); await loadAccounts();
});
$('#cancel').addEventListener('click', resetTxForm);
function resetTxForm() {
  $('#f').reset();
  window.__clearDescAutocomplete?.();
  $('#editId').value = '';
  document.querySelector('#f button[type="submit"]').textContent = 'Add';
  $('#cancel').style.display = 'none';
  $('#date').value = getLastTxDateOrToday(); // today on first load, otherwise last used
}

function getLastTxDateOrToday() {
  const saved = localStorage.getItem(LAST_TX_DATE_KEY);
  if (saved && /^\d{4}-\d{2}-\d{2}$/.test(saved)) return saved;
  return new Date().toISOString().slice(0, 10);
}

function saveLastTxDate(d) {
  if (d && /^\d{4}-\d{2}-\d{2}$/.test(d)) {
    localStorage.setItem(LAST_TX_DATE_KEY, d);
  }
}

/* ------------ autocomplete: transaction description ------------ */
function initDescriptionAutocomplete() {
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
      <div class="ac-item" role="option" data-idx="${i}">
        ${esc(t)}
      </div>
    `).join('');

    activeIndex = -1;
    openMenu();
  }

  async function fetchSuggestions(q, limit = 15) {
    const reqId = ++lastReqId;

    // NOTE: This must match your backend @RequestParam name.
    // Your current code uses "description" and it's working, so keep it consistent.
    const url = `${API}/suggestions?description=${encodeURIComponent(q)}&howMany=${limit}`;

    try {
      const list = await j(url);
      if (reqId !== lastReqId) return null; // stale response
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
    // your Money object likely serializes like { amount: 12.34, ... }
    if (typeof m === 'object' && m.amount != null) return Number(m.amount);
    return null;
  }

  async function applyDetailsIfFound(selectedDescription) {
    // This call should be made AFTER user selects an item
    try {
      const d = await fetchSuggestionDetails(selectedDescription);
      if (!d) return;

      // Category
      if (catEl && d.category) {
        catEl.value = d.category;
      }

      // Income/Expense
      // backend returns boolean income; HTML expects "true"/"false"
      if (incomeEl && typeof d.income === 'boolean') {
        incomeEl.value = d.income ? 'true' : 'false';
      }

      // Account
      if (accountEl && d.accountId != null) {
        const idStr = String(d.accountId);
        const exists = [...accountEl.options].some(o => o.value === idStr);
        if (exists) accountEl.value = idStr;
      }

      // Amount (only fill if user hasn't typed anything)
      if (amountEl && (amountEl.value == null || amountEl.value.trim() === '')) {
        const amt = moneyToNumber(d.amount);
        if (amt != null && Number.isFinite(amt)) amountEl.value = String(amt);
      }

    } catch {
      // silently ignore; description was still applied
    }
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

  // Keyboard support
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
      const next = activeIndex < max ? activeIndex + 1 : 0;
      setActive(next);
      e.preventDefault();
    } else if (e.key === 'ArrowUp') {
      const prev = activeIndex > 0 ? activeIndex - 1 : max;
      setActive(prev);
      e.preventDefault();
    } else if (e.key === 'Enter') {
      if (activeIndex >= 0 && activeIndex <= max) {
        // async selection (safe to fire-and-forget)
        selectValue(items[activeIndex]);
        e.preventDefault(); // prevents form submit when selecting
      }
    } else if (e.key === 'Escape') {
      clearMenu();
      e.preventDefault();
    }
  });

  // Click selection (mousedown beats blur)
  menu.addEventListener('mousedown', (e) => {
    const el = e.target.closest('.ac-item');
    if (!el) return;
    const idx = Number(el.dataset.idx);
    if (!Number.isNaN(idx) && items[idx] != null) {
      selectValue(items[idx]);
    }
  });

  // Close when clicking outside
  document.addEventListener('mousedown', (e) => {
    const wrap = e.target.closest('.ac-wrap');
    if (wrap) return;
    clearMenu();
  });

  // expose a hook used by edit/reset
  window.__clearDescAutocomplete = clearMenu;
}

async function renderAccountsSidebar(list) {
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
      </div>
    </div>
  `).join('');

  // fill balances after DOM exists
  await Promise.all(list.map(async (a) => {
    try {
      const bal = await j(`${ACCOUNT}/balance/${a.id}`);
      const el = document.getElementById(`accbal-${a.id}`);
      if (el) el.textContent = money(bal);
    } catch {}
  }));
}


  function renderPlansSidebar(list) {
    const wrap = document.getElementById('planList');
    if (!wrap) return;

    wrap.innerHTML = list.map(p => `
    <div class="side-item">
      <div>
        <div class="side-title">${esc(p.sourceName ?? '')}</div>
        <div class="side-sub">
          <span class="chip">${esc(p.payType)}</span>
          ${p.hoursPerWeek != null ? `<span class="chip">${esc(String(p.hoursPerWeek))} hrs/wk</span>` : ''}
        </div>
      </div>
      <div class="side-meta">${money(p.amount)}</div>

      <div class="side-actions" style="grid-column: 1 / -1;">
        <button class="side-btn" onclick="delPlan(${p.id})">Delete</button>
      </div>
    </div>
  `).join('');
  }


/* ------------ filters & pager ------------ */
async function applyFilters() {
  state.acc = $('#faccount')?.value || '';
  state.cat = $('#fcat').value || '';
  state.yr = $('#fyr').value || '';
  state.mo = $('#fmo').value || '';
  await fetchPage(0);
}
$('#apply').addEventListener('click', async e => {
  e.preventDefault();
  await applyFilters();
  await loadNet();
  await loadCategorySummary();
});

$('#reset').addEventListener('click', async () => {
  state.acc = state.cat = state.yr = state.mo = '';
  if ($('#faccount')) $('#faccount').value = '';
  $('#fcat').value = ''; $('#fyr').value = ''; $('#fmo').value = '';

  await fetchPage(0);
  await loadNet();
  await loadCategorySummary();
});
$('#prev').addEventListener('click', async () => { if (state.page > 0) await fetchPage(state.page - 1); });
$('#next').addEventListener('click', async () => { await fetchPage(state.page + 1); });
/* persist size */
state.size = parseInt(localStorage.getItem('size') || '20', 10);
if ($('#psize')) $('#psize').value = String(state.size);
$('#psize').addEventListener('change', async () => {
  state.size = parseInt($('#psize').value, 10) || 20;
  localStorage.setItem('size', state.size);
  await fetchPage(0);
});

/* ------------ income plans ------------ */
function toggleHours() {
  const hourly = $('#ppaytype').value === 'HOURLY';
  $('#phoursWrap').style.opacity = hourly ? 1 : 0.5;
  $('#phours').disabled = !hourly;
}
$('#ppaytype').addEventListener('change', toggleHours);
$('#pform').addEventListener('submit', async e => {
  e.preventDefault();
  const payload = {
    sourceName: $('#psource').value.trim(),
    payType: $('#ppaytype').value,
    amount: parseFloat($('#pamount').value),
    hoursPerWeek: $('#phours').disabled ? null : parseFloat($('#phours').value || 0),
    effectiveFrom: $('#pfrom').value,
    effectiveTo: $('#pto').value || null
  };
  await j(PLAN, { method: 'POST', body: JSON.stringify(payload) });
  clearPlanForm(); await loadPlans(); await loadNet();
});
$('#pclear').addEventListener('click', clearPlanForm);
function clearPlanForm() {
  $('#pform').reset();
  $('#pfrom').value = new Date().toISOString().slice(0, 10);
  toggleHours();
}

async function loadPlans() {
  const list = await j(PLAN);
  renderPlansSidebar(list);
}

window.delPlan = async function (id) {
  if (!confirm('Delete this plan?')) return;
  await fetch(`${PLAN}/${id}`, { method: 'DELETE' });
  await loadPlans(); await loadNet();
};

/* ------------ monthly net ------------ */
async function loadNet() {
  const yr = $('#fyr').value, mo = $('#fmo').value;
  if (!(yr && mo)) { setNet({ expectedIncome: 0, actualIncome: 0, expenses: 0, netExpected: 0, netActual: 0 }); return; }
  try { const r = await j(`${PLAN}/net/${yr}/${mo}`); setNet(r); }
  catch { setNet({ expectedIncome: 0, actualIncome: 0, expenses: 0, netExpected: 0, netActual: 0 }); }
}
function setNet(r) {
  $('#n_exp').textContent = money(r.expectedIncome);
  $('#n_act').textContent = money(r.actualIncome);
  $('#n_expenses').textContent = money(r.expenses);
  $('#n_netexp').textContent = money(r.netExpected);
  $('#n_netact').textContent = money(r.netActual);
  colorize($('#n_netexp'), r.netExpected);
  colorize($('#n_netact'), r.netActual);
}
function colorize(el, v) {
  const num = (typeof v === 'number') ? v : (v?.amount ?? 0);
  el.classList.remove('ok', 'bad'); el.classList.add(num >= 0 ? 'ok' : 'bad');
}

$('#refreshNet').addEventListener('click', async () => {
  await loadNet();
  await loadCategorySummary();
});

/* ------------ monthly category summary ------------ */
async function loadCategorySummary() {
  const yr = $('#fyr')?.value;
  const mo = $('#fmo')?.value;
  const tb = document.querySelector('#sumcat tbody');

  if (!(yr && mo)) {
    tb.innerHTML = `<tr><td class="muted" colspan="2">Select Year/Mon to see summary</td></tr>`;
    $('#sumcat_total').textContent = money(0);
    return;
  }

  const url = `/transactions/summary/${yr}/${mo}`;

  try {
    const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
    const text = await res.text();              // <- read raw text first (works even if it's HTML/error)

    if (!res.ok) {
      tb.innerHTML = `<tr><td class="muted" colspan="2">Failed (${res.status}): ${esc(text)}</td></tr>`;
      $('#sumcat_total').textContent = money(0);
      return;
    }

    let rows;
    try {
      rows = JSON.parse(text);                  // <- now parse as JSON
    } catch (e) {
      tb.innerHTML = `<tr><td class="muted" colspan="2">Response was not JSON (see console)</td></tr>`;
      $('#sumcat_total').textContent = money(0);
      return;
    }

    if (!Array.isArray(rows) || rows.length === 0) {
      tb.innerHTML = `<tr><td class="muted" colspan="2">No data for this month</td></tr>`;
      $('#sumcat_total').textContent = money(0);
      return;
    }

    tb.innerHTML = '';
    let grand = 0;

    rows.forEach(r => {
      const amt =
        (typeof r.total === 'number') ? r.total :
          (typeof r.total === 'string') ? parseFloat(r.total) :
            (r.total?.amount ?? r.total?.value ?? 0);

      grand += (Number.isFinite(amt) ? amt : 0);

      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${r.category}</td>
        <td class="right sum-amt ${amt < 0 ? 'bad' : 'ok'}">${money(amt)}</td>
      `;
      tb.appendChild(tr);
    });

    $('#sumcat_total').textContent = money(grand);

  } catch (e) {
    console.error('Summary exception:', e);
    tb.innerHTML = `<tr><td class="muted" colspan="2">Exception: ${esc(String(e.message || e))}</td></tr>`;
    $('#sumcat_total').textContent = money(0);
  }
}

document.getElementById('refreshCat').addEventListener('click', e => { e.preventDefault(); loadCategorySummary(); });


/* ------------ boot ------------ */
function bootDefaults() {
  $('#date').value = getLastTxDateOrToday();  // today on first load, otherwise last used
  $('#pfrom').value = new Date().toISOString().slice(0, 10);

  const d = new Date();
  $('#fyr').value = d.getFullYear();
  $('#fmo').value = d.getMonth() + 1;

  toggleHours();
}
(async function boot() {
  bootDefaults();
  initDescriptionAutocomplete();
  await loadAccounts();
  await fetchPage(0);
  await loadPlans();
  await loadNet();
  await loadCategorySummary();
})();
