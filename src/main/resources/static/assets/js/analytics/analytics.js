const currencyFormatter = new Intl.NumberFormat("en-CA", {
  style: "currency",
  currency: "CAD"
});

const essentialFromDateEl = document.getElementById("essentialFromDate");
const essentialToDateEl = document.getElementById("essentialToDate");
const loadEssentialBtn = document.getElementById("loadEssentialBtn");
const essentialStatusEl = document.getElementById("essentialStatus");
const essentialCardsEl = document.getElementById("essentialCards");

const summaryYearEl = document.getElementById("summaryYear");
const summaryMonthEl = document.getElementById("summaryMonth");
const summaryViewEl = document.getElementById("summaryView");
const loadSummaryBtn = document.getElementById("loadSummaryBtn");
const summaryStatusEl = document.getElementById("summaryStatus");
const summaryListEl = document.getElementById("summaryList");

const debtFromMonthEl = document.getElementById("debtFromMonth");
const debtToMonthEl = document.getElementById("debtToMonth");
const loadDebtBtn = document.getElementById("loadDebtBtn");
const debtStatusEl = document.getElementById("debtStatus");
const debtListEl = document.getElementById("debtList");

let essentialChart = null;
let summaryChart = null;
let debtChart = null;

document.addEventListener("DOMContentLoaded", init);

function init() {
  setDefaultValues();

  loadEssentialBtn.addEventListener("click", loadEssentialBreakdown);
  loadSummaryBtn.addEventListener("click", loadMonthlySummary);
  loadDebtBtn.addEventListener("click", loadDebtTrend);
  summaryViewEl.addEventListener("change", loadMonthlySummary);

  loadEssentialBreakdown();
  loadMonthlySummary();
  loadDebtTrend();
}

function setDefaultValues() {
  const today = new Date();

  const currentYear = today.getFullYear();
  const currentMonth = today.getMonth() + 1;

  summaryYearEl.value = currentYear;
  summaryMonthEl.value = currentMonth;

  const firstDayOfMonth = new Date(currentYear, today.getMonth(), 1);
  essentialFromDateEl.value = toDateInputValue(firstDayOfMonth);
  essentialToDateEl.value = toDateInputValue(today);

  const fromMonth = new Date(currentYear, today.getMonth() - 5, 1);
  debtFromMonthEl.value = toMonthInputValue(fromMonth);
  debtToMonthEl.value = toMonthInputValue(today);
}

async function loadEssentialBreakdown() {
  const fromDate = essentialFromDateEl.value;
  const toDate = essentialToDateEl.value;

  if (!fromDate || !toDate) {
    renderError(essentialStatusEl, "Please select both dates.");
    essentialCardsEl.innerHTML = "";
    destroyChart(essentialChart);
    essentialChart = null;
    return;
  }

  setLoading(essentialStatusEl, "Loading essential breakdown...");
  essentialCardsEl.innerHTML = "";

  try {
    const url = `/analytics/essential-breakdown?fromDate=${encodeURIComponent(fromDate)}&toDate=${encodeURIComponent(toDate)}`;
    const response = await fetch(url);

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    const data = await response.json();
    renderEssentialBreakdown(data);
    renderEssentialChart(data);
    essentialStatusEl.textContent = "";
    essentialStatusEl.className = "muted";
  } catch (error) {
    renderError(essentialStatusEl, `Could not load essential breakdown. ${error.message}`);
    essentialCardsEl.innerHTML = "";
    destroyChart(essentialChart);
    essentialChart = null;
  }
}

function renderEssentialBreakdown(data) {
  if (!data) {
    essentialCardsEl.innerHTML = "";
    essentialStatusEl.textContent = "No data available.";
    destroyChart(essentialChart);
    essentialChart = null;
    return;
  }

  essentialCardsEl.innerHTML = `
    <div class="card">
      <div class="card-title">Total Expenses</div>
      <div class="card-value">${formatMoney(data.totalExpenseAmount)}</div>
    </div>

    <div class="card">
      <div class="card-title">Essential</div>
      <div class="card-value">${formatMoney(data.essential.amount)}</div>
      <div class="muted">${data.essential.transactionCount} tx • ${formatPercent(data.essential.percentageOfTotal)}</div>
    </div>

    <div class="card">
      <div class="card-title">Non-Essential</div>
      <div class="card-value">${formatMoney(data.nonEssential.amount)}</div>
      <div class="muted">${data.nonEssential.transactionCount} tx • ${formatPercent(data.nonEssential.percentageOfTotal)}</div>
    </div>

    <div class="card">
      <div class="card-title">Total Transactions</div>
      <div class="card-value">${data.totalExpenseTransactionCount}</div>
    </div>
  `;
}

function renderEssentialChart(data) {
  destroyChart(essentialChart);

  const ctx = document.getElementById("essentialChart");
  if (!ctx || !data) return;

  essentialChart = new Chart(ctx, {
    type: "doughnut",
    data: {
      labels: ["Essential", "Non-Essential"],
      datasets: [
        {
          data: [
            Number(data.essential.amount || 0),
            Number(data.nonEssential.amount || 0)
          ]
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: "bottom"
        },
        tooltip: {
          callbacks: {
            label(context) {
              const label = context.label ?? "";
              const value = context.parsed ?? 0;
              return `${label}: ${formatMoney(value)}`;
            }
          }
        }
      }
    }
  });
}

async function loadMonthlySummary() {
  const year = summaryYearEl.value;
  const month = summaryMonthEl.value;
  const view = summaryViewEl.value;

  if (!year || !month) {
    renderError(summaryStatusEl, "Please select year and month.");
    summaryListEl.innerHTML = "";
    destroyChart(summaryChart);
    summaryChart = null;
    return;
  }

  setLoading(summaryStatusEl, "Loading monthly summary...");
  summaryListEl.innerHTML = "";

  try {
    const endpoint =
      view === "parent"
        ? `/analytics/monthly-summary-parent/${encodeURIComponent(year)}/${encodeURIComponent(month)}`
        : `/analytics/monthly-summary-all/${encodeURIComponent(year)}/${encodeURIComponent(month)}`;

    const response = await fetch(endpoint);

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    const data = await response.json();
    renderMonthlySummary(data);
    renderSummaryChart(data);

    summaryStatusEl.textContent =
      view === "parent"
        ? "Showing parent categories."
        : "Showing sub-categories.";
    summaryStatusEl.className = "muted";
  } catch (error) {
    renderError(summaryStatusEl, `Could not load monthly summary. ${error.message}`);
    summaryListEl.innerHTML = "";
    destroyChart(summaryChart);
    summaryChart = null;
  }
}

function renderMonthlySummary(items) {
  if (!items || items.length === 0) {
    summaryStatusEl.textContent = "No category summary available for this month.";
    summaryListEl.innerHTML = "";
    destroyChart(summaryChart);
    summaryChart = null;
    return;
  }

  summaryListEl.innerHTML = items
    .map(item => {
      const categoryName =
        item.categoryName ?? 
        item.categoryCode ??
        "Unknown Category";

      const total = item.total ?? 0;

      return `
        <div class="list-row">
          <span>${escapeHtml(categoryName)}</span>
          <strong>${formatMoney(total)}</strong>
        </div>
      `;
    })
    .join("");
}

function renderSummaryChart(items) {
  destroyChart(summaryChart);

  const ctx = document.getElementById("summaryChart");
  if (!ctx || !items || items.length === 0) return;

  const normalized = items
    .map(item => {
      const categoryName =
        item.categoryName ?? 
        item.categoryCode ??
        "Unknown Category";

      const rawTotal = Number(item.total || 0);

      return {
        label: categoryName,
        rawValue: rawTotal,
        chartValue: Math.abs(rawTotal)
      };
    })
    .filter(item => item.rawValue < 0) // expenditures only
    .sort((a, b) => b.chartValue - a.chartValue);

  if (normalized.length === 0) {
    summaryStatusEl.textContent = "No expenditure categories available for charting.";
    summaryListEl.innerHTML = "";
    destroyChart(summaryChart);
    summaryChart = null;
    return;
  }

  const labels = normalized.map(item => item.label);
  const values = normalized.map(item => item.chartValue);

  summaryChart = new Chart(ctx, {
    type: "bar",
    data: {
      labels,
      datasets: [
        {
          label: "Category Spending",
          data: values
        }
      ]
    },
    options: {
      indexAxis: "y",
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          beginAtZero: true,
          ticks: {
            callback(value) {
              return formatMoney(value);
            }
          }
        }
      },
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          callbacks: {
            label(context) {
              return formatMoney(context.parsed.x);
            }
          }
        }
      }
    }
  });
}

async function loadDebtTrend() {
  const fromMonth = debtFromMonthEl.value;
  const toMonth = debtToMonthEl.value;

  if (!fromMonth || !toMonth) {
    renderError(debtStatusEl, "Please select both months.");
    debtListEl.innerHTML = "";
    destroyChart(debtChart);
    debtChart = null;
    return;
  }

  setLoading(debtStatusEl, "Loading debt trend...");
  debtListEl.innerHTML = "";

  try {
    const url = `/analytics/debt/monthly?fromMonth=${encodeURIComponent(fromMonth)}&toMonth=${encodeURIComponent(toMonth)}`;
    const response = await fetch(url);

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    const data = await response.json();
    renderDebtTrend(data);
    renderDebtChart(data);
    debtStatusEl.textContent = "";
    debtStatusEl.className = "muted";
  } catch (error) {
    renderError(debtStatusEl, `Could not load debt trend. ${error.message}`);
    debtListEl.innerHTML = "";
    destroyChart(debtChart);
    debtChart = null;
  }
}

function renderDebtTrend(items) {
  if (!items || items.length === 0) {
    debtStatusEl.textContent = "No debt trend data available.";
    debtListEl.innerHTML = "";
    destroyChart(debtChart);
    debtChart = null;
    return;
  }

  debtListEl.innerHTML = items
    .map(item => `
      <div class="list-row">
        <span>${formatYearMonth(item.month)}</span>
        <strong>${formatMoney(item.totalDebtAtMonthEnd)}</strong>
      </div>
    `)
    .join("");
}

function renderDebtChart(items) {
  destroyChart(debtChart);

  const ctx = document.getElementById("debtChart");
  if (!ctx || !items || items.length === 0) return;

  const labels = items.map(item => formatYearMonth(item.month));
  const values = items.map(item => Number(item.totalDebtAtMonthEnd || 0));

  debtChart = new Chart(ctx, {
    type: "line",
    data: {
      labels,
      datasets: [
        {
          label: "Debt at Month End",
          data: values,
          tension: 0.25
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          ticks: {
            callback(value) {
              return formatMoney(value);
            }
          }
        }
      },
      plugins: {
        tooltip: {
          callbacks: {
            label(context) {
              return formatMoney(context.parsed.y);
            }
          }
        }
      }
    }
  });
}

function destroyChart(chart) {
  if (chart) {
    chart.destroy();
  }
}

function formatMoney(value) {
  return currencyFormatter.format(Number(value || 0));
}

function formatPercent(value) {
  return `${Number(value || 0).toFixed(2)}%`;
}

function formatYearMonth(value) {
  if (!value) return "";

  const [year, month] = value.split("-");
  const date = new Date(Number(year), Number(month) - 1, 1);

  return date.toLocaleDateString("en-CA", {
    year: "numeric",
    month: "long"
  });
}

function toDateInputValue(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function toMonthInputValue(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  return `${year}-${month}`;
}

function setLoading(element, message) {
  element.className = "loading";
  element.textContent = message;
}

function renderError(element, message) {
  element.className = "error";
  element.textContent = message;
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}