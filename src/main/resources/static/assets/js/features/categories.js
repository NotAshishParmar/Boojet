// /assets/js/features/categories.js
import { $ } from "../core/dom.js";
import { state } from "../core/state.js";
import { j } from "../core/api.js";

let categories = [];          // flat list
let byId = new Map();         // id -> category
let childrenByParent = new Map(); // parentId (null for roots) -> [categories]
let selectedRoot = null;

function indexCategories(list) {
  categories = list || [];
  byId = new Map(categories.map(c => [c.id, c]));

  childrenByParent = new Map();
  for (const c of categories) {
    const pid = c.parentId ?? null;
    if (!childrenByParent.has(pid)) childrenByParent.set(pid, []);
    childrenByParent.get(pid).push(c);
  }

  // keep sorting consistent with backend ordering, but safe to sort again:
  for (const [pid, arr] of childrenByParent.entries()) {
    arr.sort((a, b) => {
      const soA = (a.sortOrder ?? 999999);
      const soB = (b.sortOrder ?? 999999);
      if (soA !== soB) return soA - soB;
      return String(a.name).localeCompare(String(b.name));
    });
  }
}

function setCatField(cat) {
  $("#catId").value = String(cat.id);
  $("#catDisplay").value = cat.parentCode
    ? `${cat.parentCode} → ${cat.name}`
    : `${cat.code} → ${cat.name}`;

  // Optional hint in UI: derived type
  const hint = $("#incomeHint");
  if (hint) hint.textContent = cat.type === "INCOME" ? "Income" : "Expense";
}

function openOverlay() {
  $("#catOverlay").hidden = false;
}

function closeOverlay() {
  const el = $("#catOverlay");
  if (el) el.hidden = true;
}

function renderRoots() {
  const roots = childrenByParent.get(null) || [];
  const host = $("#catRoots");
  host.innerHTML = "";

  for (const r of roots) {
    const div = document.createElement("div");
    div.className = "calItem"; // reuse your list styling
    div.innerHTML = `
      <div>
        <div class="calDate">${r.name}</div>
        <div class="calPreview muted">${r.code}</div>
      </div>
      <div class="muted small">Open</div>
    `;
    div.onclick = () => {
      selectedRoot = r;
      $("#catPath").textContent = r.name;
      renderChildren(r.id);
    };
    host.appendChild(div);
  }
}

function renderChildren(parentId) {
  const kids = childrenByParent.get(parentId) || [];
  const host = $("#catChildren");
  host.innerHTML = "";

  $("#catEmpty").style.display = kids.length ? "none" : "";

  for (const c of kids) {
    const div = document.createElement("div");
    div.className = "calItem";
    div.innerHTML = `
      <div>
        <div class="calDate">${c.name}</div>
        <div class="calPreview muted">${c.code}</div>
      </div>
      <div class="muted small">${c.type}</div>
    `;
    div.onclick = () => {
      setCatField(c);
      closeOverlay();
    };
    host.appendChild(div);
  }
}

function populateFilterDropdown() {
  const sel = $("#fcat");
  if (!sel) return;

  // keep (All)
  sel.innerHTML = `<option value="">(All)</option>`;

  // for filters, it usually makes sense to list leaf categories (what transactions actually use)
  const leaves = categories.filter(c => !c.hasChildren);

  for (const c of leaves) {
    const opt = document.createElement("option");
    opt.value = String(c.id);
    opt.textContent = `${c.name}`; // or `${c.code}` / `${c.parentCode} → ${c.name}`
    sel.appendChild(opt);
  }
}

export async function preloadCategories() {
  const out = await j("/category"); // returns array of CategoryResponse
  indexCategories(out);

  populateFilterDropdown();
  renderRoots();
}

export function initCategoryPicker() {
  // Button that opens modal
  $("#pickCat")?.addEventListener("click", () => {
    // If you want: default children panel to first root
    if (!selectedRoot) {
      const roots = childrenByParent.get(null) || [];
      selectedRoot = roots[0] || null;
      if (selectedRoot) {
        $("#catPath").textContent = selectedRoot.name;
        renderChildren(selectedRoot.id);
      }
    }
    openOverlay();
  });

  // Close button
  $("#catClose")?.addEventListener("click", closeOverlay);

  // Clicking outside modal can close (optional)
  $("#catOverlay")?.addEventListener("click", (e) => {
    if (e.target === $("#catOverlay")) closeOverlay();
  });
}

// Optional helpers used by other modules:
export function getCategoryById(id) {
  return byId.get(Number(id));
}
export function getAllCategories() {
  return categories.slice();
}
