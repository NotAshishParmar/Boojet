import { $ } from "../core/dom.js";
import { j } from "../core/api.js";
import { money, colorize } from "../core/format.js";

function moneyNumber(m) {
  return (typeof m === "number") ? m : (m?.amount ?? m?.value ?? 0);
}

export async function loadCreditMonthly() {
  const year = parseInt($("#fyr").value, 10);
  const month = parseInt($("#fmo").value, 10);

  if (!Number.isFinite(year) || !Number.isFinite(month) || month < 1 || month > 12) {
    setCredit({ accumulated: 0, paidOff: 0, netChange: 0 });
    return;
  }

  try {
    const res = await j(`/insights/credit/monthly?year=${year}&month=${month}`);
    setCredit({
      accumulated: moneyNumber(res.accumulated),
      paidOff: moneyNumber(res.paidOff),
      netChange: moneyNumber(res.netChange),
    });
  } catch {
    setCredit({ accumulated: 0, paidOff: 0, netChange: 0 });
  }
}

function setCredit(r) {
  $("#c_acc").textContent = money(r.accumulated);
  $("#c_paid").textContent = money(r.paidOff);
  $("#c_net").textContent = money(r.netChange);

  // For credit debt: positive netChange means debt ↑ (bad), negative means debt ↓ (good)
  colorize($("#c_net"), -r.netChange);

}