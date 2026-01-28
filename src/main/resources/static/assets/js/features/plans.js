/**
 * Income plans feature: loads and renders income plans in the sidebar,
 * handles creating/deleting plans, and toggles hourly fields in the plan form.
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
        </div>
      </div>
      <div class="side-meta">${money(p.amount)}</div>

      <div class="side-actions" style="grid-column: 1 / -1;">
        <button class="side-btn" onclick="delPlan(${p.id})">Delete</button>
      </div>
    </div>
  `).join('');
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
      hoursPerWeek: $('#phours').disabled ? null : parseFloat($('#phours').value || 0),
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
}
