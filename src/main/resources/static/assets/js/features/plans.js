/**
 * Income plans feature: loads and renders income plans in the sidebar,
 * handles creating/deleting plans, setting effective-to dates,
 * and toggles hourly fields in the plan form.
 */

import { PLAN } from '../core/config.js';
import { $ } from '../core/dom.js';
import { j } from '../core/api.js';
import { esc, money } from '../core/format.js';
import { loadNet } from './net.js';

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
          <span class="chip">From: ${esc(fmtPlanDate(p.effectiveFrom))}</span>
          <span class="chip">${p.effectiveTo ? `To: ${esc(fmtPlanDate(p.effectiveTo))}` : 'Ongoing'}</span>
        </div>
      </div>

      <div class="side-meta">${money(p.amount)}</div>

      <div class="side-actions" style="grid-column: 1 / -1;">
        <button class="side-btn" onclick="openPlanToModal(${p.id}, '${escAttr(p.effectiveTo ?? '')}')">
          Set End Date
        </button>
        <button class="side-btn" onclick="delPlan(${p.id})">Delete</button>
      </div>
    </div>
  `).join('');
}

function fmtPlanDate(isoDate) {
  if (!isoDate) return '';
  const [yyyy, mm, dd] = String(isoDate).split('-');
  if (!yyyy || !mm || !dd) return isoDate;
  return `${dd}-${mm}-${yyyy}`;
}

function escAttr(value) {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;');
}

export function toggleHours() {
  const hourly = $('#ppaytype').value === 'HOURLY';
  $('#phoursWrap').style.opacity = hourly ? 1 : 0.5;
  $('#phours').disabled = !hourly;
}

export async function loadPlans() {
  const list = await j(PLAN);
  renderPlansSidebar(list);
}

export async function delPlan(id) {
  if (!confirm('Delete this plan?')) return;
  await fetch(`${PLAN}/${id}`, { method: 'DELETE' });
  await loadPlans();
  await loadNet();
}

export function openPlanToModal(id, effectiveTo = '') {
  $('#planToId').value = id;
  $('#planToDate').value = effectiveTo || '';
  $('#planToOverlay').hidden = false;
}

export function closePlanToModal() {
  $('#planToOverlay').hidden = true;
  $('#planToForm').reset();
  $('#planToId').value = '';
}

async function savePlanEffectiveTo(e) {
  e.preventDefault();

  const id = $('#planToId').value;
  const effectiveTo = $('#planToDate').value || null;

  await j(`${PLAN}/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ effectiveTo })
  });

  closePlanToModal();
  await loadPlans();
  await loadNet();
}

async function clearPlanEffectiveTo() {
  const id = $('#planToId').value;
  if (!id) return;

  await j(`${PLAN}/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ effectiveTo: null })
  });

  closePlanToModal();
  await loadPlans();
  await loadNet();
}

export function clearPlanForm() {
  $('#pform').reset();
  $('#pfrom').value = new Date().toISOString().slice(0, 10);
  toggleHours();
}

export function initPlans() {
  $('#ppaytype').addEventListener('change', toggleHours);

  $('#pform').addEventListener('submit', async (e) => {
    e.preventDefault();

    const payload = {
      sourceName: $('#psource').value.trim(),
      payType: $('#ppaytype').value,
      amount: parseFloat($('#pamount').value),
      hoursPerWeek: $('#phours').disabled ? null : ($('#phours').value ? parseFloat($('#phours').value) : null),
      effectiveFrom: $('#pfrom').value,
      effectiveTo: $('#pto').value || null,
    };

    await j(PLAN, { method: 'POST', body: JSON.stringify(payload) });
    document.getElementById('planCollapse')?.removeAttribute('open');
    clearPlanForm();
    await loadPlans();
    await loadNet();
  });

  $('#pclear').addEventListener('click', clearPlanForm);

  $('#planToForm')?.addEventListener('submit', savePlanEffectiveTo);
  $('#planToClose')?.addEventListener('click', closePlanToModal);
  $('#planToCancel')?.addEventListener('click', closePlanToModal);
  $('#planToClear')?.addEventListener('click', clearPlanEffectiveTo);

  window.delPlan = delPlan;
  window.openPlanToModal = openPlanToModal;
}