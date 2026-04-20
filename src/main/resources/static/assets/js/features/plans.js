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
    <div class="side-item plan-card">
      <div class="plan-head">
        <div class="side-title">${esc(p.sourceName ?? '')}</div>
        <div class="plan-amount">${money(p.amount)}</div>
      </div>

      <div class="plan-meta">
        <span class="chip">${esc(p.payType)}</span>
        ${p.hoursPerWeek != null ? `<span class="chip">${esc(String(p.hoursPerWeek))} hrs/wk</span>` : ''}
        <span class="chip">${esc(deductionPercentLabel(p.estimatedDeductionRate))}</span>
      </div>

      <div class="plan-dates-text muted">
        From ${esc(fmtPlanDate(p.effectiveFrom))}
        ${p.effectiveTo ? ` · To ${esc(fmtPlanDate(p.effectiveTo))}` : ' · Ongoing'}
      </div>

      <div class="side-actions plan-actions">
        <button
          class="side-btn"
          onclick="openPlanDeductionModal(${p.id}, '${escAttr(String(p.estimatedDeductionRate ?? '0.2200'))}')">
          Deductions
        </button>

        <button
          class="side-btn"
          onclick="openPlanToModal(${p.id}, '${escAttr(p.effectiveTo ?? '')}')">
          End Date
        </button>

        <button class="side-btn plan-delete" onclick="delPlan(${p.id})">
          Delete
        </button>
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

function deductionPercentLabel(rate) {
  const n = Number(rate ?? 0.22);
  if (!Number.isFinite(n)) return '22% deductions';
  return `${(n * 100).toFixed(2).replace(/\.00$/, '')}% deductions`;
}

function deductionPercentInputValue(rate) {
  const n = Number(rate ?? 0.22);
  if (!Number.isFinite(n)) return '22';
  return (n * 100).toFixed(2).replace(/\.00$/, '');
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

export function openPlanDeductionModal(id, rate = '0.2200') {
  $('#planDeductionId').value = id;
  $('#planDeductionRate').value = deductionPercentInputValue(rate);
  $('#planDeductionOverlay').hidden = false;
}

export function closePlanDeductionModal() {
  $('#planDeductionOverlay').hidden = true;
  $('#planDeductionForm').reset();
  $('#planDeductionId').value = '';
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

async function savePlanDeductionRate(e) {
  e.preventDefault();

  const id = $('#planDeductionId').value;
  const pct = parseFloat($('#planDeductionRate').value);

  if (!Number.isFinite(pct) || pct < 0 || pct > 100) {
    alert('Deduction rate must be between 0 and 100.');
    return;
  }

  const estimatedDeductionRate = +(pct / 100).toFixed(4);

  await j(`${PLAN}/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ estimatedDeductionRate })
  });

  closePlanDeductionModal();
  await loadPlans();
  await loadNet();
}

async function resetPlanDeductionRateDefault() {
  const id = $('#planDeductionId').value;
  if (!id) return;

  await j(`${PLAN}/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ estimatedDeductionRate: 0.2200 })
  });

  closePlanDeductionModal();
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
      estimatedDeductionRate: 0.2200
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

  $('#planDeductionForm')?.addEventListener('submit', savePlanDeductionRate);
  $('#planDeductionClose')?.addEventListener('click', closePlanDeductionModal);
  $('#planDeductionCancel')?.addEventListener('click', closePlanDeductionModal);
  $('#planDeductionDefault')?.addEventListener('click', resetPlanDeductionRateDefault);

  window.delPlan = delPlan;
  window.openPlanToModal = openPlanToModal;
  window.openPlanDeductionModal = openPlanDeductionModal;
}