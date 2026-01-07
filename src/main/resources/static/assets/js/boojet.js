/* ------------ helpers ------------ */
const API = '/transactions';
const PLAN = '/plan';
const ACCOUNT = '/account';
const state = { page: 0, size: 20, acc: '', cat: '', yr: '', mo: '' };
const $ = s => document.querySelector(s);

function money(n){
  const v = (typeof n === 'number') ? n : (n?.amount ?? 0);
  return new Intl.NumberFormat(undefined,{style:'currency',currency:'CAD'}).format(v);
}
function esc(s){ return (s||'').replace(/[&<>"']/g,c=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[c])); }
async function j(url, opts = {}) {
  const r = await fetch(url, Object.assign({ headers: { 'Content-Type': 'application/json' } }, opts));
  if (!r.ok) throw new Error(await r.text());
  return r.status === 204 ? null : r.json();
}

/* ------------ pagination ------------ */
function buildQuery(pageOverride){
  const p = new URLSearchParams();
  p.set('page', pageOverride ?? state.page);
  p.set('size', state.size);
  if (state.acc) p.set('accountId', state.acc);
  if (state.cat) p.set('category', state.cat);
  if (state.yr && state.mo){ p.set('year', state.yr); p.set('month', state.mo); }
  return p.toString();
}
async function fetchPage(page = 0){
  state.page = page;
  const data = await j(`${API}?${buildQuery(page)}`);
  renderTx(data.content);
  updatePager(data);
  try { $('#balance').textContent = money(await j(`${API}/balance`)); } catch {}
  return data;
}
function updatePager(p){
  $('#pages').textContent = `Page ${p.number + 1} of ${Math.max(1, p.totalPages)} • ${p.totalElements} items`;
  $('#prev').disabled = p.first; $('#next').disabled = p.last;
}

/* ------------ accounts ------------ */
function ensureDefaultAccount(){
  const sel = $('#account');
  if (sel && sel.options.length > 0 && !sel.value) sel.value = sel.options[0].value;
}
async function loadAccounts(){
  const list = await j(ACCOUNT);
  const txSel = $('#account'); const fSel = $('#faccount');
  txSel.innerHTML = ''; fSel.innerHTML = '<option value="">(All)</option>';
  list.forEach(a => {
    txSel.insertAdjacentHTML('beforeend', `<option value="${a.id}">${esc(a.name)} (${a.type})</option>`);
    fSel.insertAdjacentHTML('beforeend', `<option value="${a.id}">${esc(a.name)}</option>`);
  });
  ensureDefaultAccount();
  renderAccounts(list);
}
async function renderAccounts(list){
  const tb = $('#atbl tbody'); tb.innerHTML = '';
  for (const a of list) {
    let bal = 0; try { bal = await j(`${ACCOUNT}/balance/${a.id}`); } catch {}
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${esc(a.name)}</td>
      <td>${a.type}</td>
      <td class="right">${money(bal)}</td>
      <td><button onclick="viewAccount(${a.id})">View Tx</button></td>`;
    tb.appendChild(tr);
  }
}
window.viewAccount = async function(id){
  $('#faccount').value = String(id);
  await applyFilters();
};
$('#acctForm').addEventListener('submit', async e=>{
  e.preventDefault();
  const payload = {
    // If your service assigns default user, omit next line; else include it:
    user: { id: 1 },
    name: $('#aname').value.trim(),
    type: $('#atype').value,
    openingBalance: parseFloat($('#aopen').value || 0)
  };
  await j(ACCOUNT, { method:'POST', body: JSON.stringify(payload) });
  $('#acctForm').reset();
  await loadAccounts();
});
$('#aclear').addEventListener('click', ()=> $('#acctForm').reset());

/* ------------ transactions ------------ */
function renderTx(list){
  const tb = $('#tbl tbody'); tb.innerHTML = '';
  list.forEach(t=>{
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
window.editTx = async function(id){
  const t = await j(`${API}/${id}`);
  $('#editId').value = id;
  $('#desc').value = t.description;
  $('#amount').value = (typeof t.amount === 'number' ? t.amount : t.amount.amount);
  $('#date').value = t.date;
  $('#cat').value = t.category;
  $('#income').value = t.income ? 'true' : 'false';
  if (t.account?.id) $('#account').value = String(t.account.id);
  document.querySelector('#f button[type="submit"]').textContent = 'Update';
  $('#cancel').style.display = 'inline-block';
};
window.delTx = async function(id){
  if (!confirm('Delete this transaction?')) return;
  await fetch(`${API}/${id}`, { method:'DELETE' });
  await fetchPage(state.page).catch(async ()=>{
    if (state.page > 0) await fetchPage(state.page - 1);
  });
  await loadNet(); await loadAccounts();
};
$('#f').addEventListener('submit', async e=>{
  e.preventDefault();
  if (!$('#account').value){ alert('Please create/select an account first.'); return; }
  const payload = {
    description: $('#desc').value.trim(),
    amount: parseFloat($('#amount').value),
    date: $('#date').value,
    category: $('#cat').value,
    income: $('#income').value === 'true',
    account: { id: parseInt($('#account').value, 10) }
  };
  const id = $('#editId').value;
  if (id) await j(`${API}/${id}`, { method:'PUT', body: JSON.stringify(payload) });
  else    await j(API, { method:'POST', body: JSON.stringify(payload) });
  resetTxForm();
  await fetchPage(state.page); await loadNet(); await loadAccounts();
});
$('#cancel').addEventListener('click', resetTxForm);
function resetTxForm(){
  $('#f').reset(); $('#editId').value = '';
  document.querySelector('#f button[type="submit"]').textContent = 'Add';
  $('#cancel').style.display = 'none';
  $('#date').value = new Date().toISOString().slice(0,10);
}

/* filters + pager */
async function applyFilters(){
  state.acc = $('#faccount')?.value || '';
  state.cat = $('#fcat').value || '';
  state.yr  = $('#fyr').value || '';
  state.mo  = $('#fmo').value || '';
  await fetchPage(0);
}
$('#apply').addEventListener('click', async e=>{ e.preventDefault(); await applyFilters(); await loadNet(); });
$('#reset').addEventListener('click', async ()=>{
  state.acc = state.cat = state.yr = state.mo = '';
  if ($('#faccount')) $('#faccount').value = '';
  $('#fcat').value=''; $('#fyr').value=''; $('#fmo').value='';
  await fetchPage(0); await loadNet();
});
$('#prev').addEventListener('click', async ()=>{ if (state.page > 0) await fetchPage(state.page - 1); });
$('#next').addEventListener('click', async ()=>{ await fetchPage(state.page + 1); });
/* persist size */
state.size = parseInt(localStorage.getItem('size') || '20', 10);
if ($('#psize')) $('#psize').value = String(state.size);
$('#psize').addEventListener('change', async ()=>{
  state.size = parseInt($('#psize').value, 10) || 20;
  localStorage.setItem('size', state.size);
  await fetchPage(0);
});

/* ------------ income plans ------------ */
function toggleHours(){
  const hourly = $('#ppaytype').value === 'HOURLY';
  $('#phoursWrap').style.opacity = hourly ? 1 : 0.5;
  $('#phours').disabled = !hourly;
}
$('#ppaytype').addEventListener('change', toggleHours);
$('#pform').addEventListener('submit', async e=>{
  e.preventDefault();
  const payload = {
    sourceName: $('#psource').value.trim(),
    payType: $('#ppaytype').value,
    amount: parseFloat($('#pamount').value),
    hoursPerWeek: $('#phours').disabled ? null : parseFloat($('#phours').value || 0),
    effectiveFrom: $('#pfrom').value,
    effectiveTo: $('#pto').value || null
  };
  await j(PLAN, { method:'POST', body: JSON.stringify(payload) });
  clearPlanForm(); await loadPlans(); await loadNet();
});
$('#pclear').addEventListener('click', clearPlanForm);
function clearPlanForm(){
  $('#pform').reset();
  $('#pfrom').value = new Date().toISOString().slice(0,10);
  toggleHours();
}
async function loadPlans(){
  const list = await j(PLAN);
  const tb = $('#ptbl tbody'); tb.innerHTML = '';
  list.forEach(p=>{
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${esc(p.sourceName ?? '')}</td>
      <td>${p.payType}</td>
      <td class="right">${money(p.amount)}</td>
      <td>${p.hoursPerWeek ?? ''}</td>
      <td>${p.effectiveFrom ?? ''}</td>
      <td>${p.effectiveTo ?? ''}</td>
      <td><button onclick="delPlan(${p.id})">Delete</button></td>`;
    tb.appendChild(tr);
  });
}
window.delPlan = async function(id){
  if (!confirm('Delete this plan?')) return;
  await fetch(`${PLAN}/${id}`, { method:'DELETE' });
  await loadPlans(); await loadNet();
};

/* ------------ monthly net ------------ */
async function loadNet(){
  const yr = $('#fyr').value, mo = $('#fmo').value;
  if (!(yr && mo)) { setNet({ expectedIncome:0, actualIncome:0, expenses:0, netExpected:0, netActual:0 }); return; }
  try { const r = await j(`${PLAN}/net/${yr}/${mo}`); setNet(r); }
  catch { setNet({ expectedIncome:0, actualIncome:0, expenses:0, netExpected:0, netActual:0 }); }
}
function setNet(r){
  $('#n_exp').textContent = money(r.expectedIncome);
  $('#n_act').textContent = money(r.actualIncome);
  $('#n_expenses').textContent = money(r.expenses);
  $('#n_netexp').textContent = money(r.netExpected);
  $('#n_netact').textContent = money(r.netActual);
  colorize($('#n_netexp'), r.netExpected);
  colorize($('#n_netact'), r.netActual);
}
function colorize(el, v){
  const num = (typeof v === 'number') ? v : (v?.amount ?? 0);
  el.classList.remove('ok','bad'); el.classList.add(num >= 0 ? 'ok' : 'bad');
}
$('#refreshNet').addEventListener('click', loadNet);

/* ------------ boot ------------ */
function bootDefaults(){
  $('#date').value = new Date().toISOString().slice(0,10);
  $('#pfrom').value = new Date().toISOString().slice(0,10);
  toggleHours();
}
(async function boot(){
  bootDefaults();
  await loadAccounts();
  await fetchPage(0);
  await loadPlans();
  await loadNet();
})();
