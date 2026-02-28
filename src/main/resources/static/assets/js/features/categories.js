/**
 * Category Picker Modal logic: 
 */


import { $ } from "../core/dom.js";
import { state } from "../core/state.js";
import { j } from "../core/api.js";

let categories = [];          // flat list
let byId = new Map();         // id -> category
let childrenByParent = new Map(); // parentId (null for roots) -> [categories]
let selectedRoot = null;
let lastFocus = null;

let rootEls = [];
let childEls = [];
let rootIndex = 0;
let childIndex = 0;

let restoreFocusEl = null;   // where focus goes when modal closes


// ----------------------------------Focus Logic-----------------------------------------
function getModalEl() {
  // your overlay has #catOverlay, modal has .modal (per your CSS)
  const overlay = $("#catOverlay");
  return overlay?.querySelector(".modal") || overlay;
}

function getFocusable(container) {
  return [...container.querySelectorAll(`
    button, [href], input, select, textarea,
    [tabindex]:not([tabindex="-1"])
  `)].filter(el => !el.disabled && !el.hidden && el.offsetParent !== null);
}

function focusFirstInModal() {
  const modal = getModalEl();
  if (!modal) return;

  // If you add a search box later, focus it here.
  const focusables = getFocusable(modal);
  (focusables[0] || modal).focus();
}

function focusEl(el) {
  if (!el) return;
  el.tabIndex = 0;
  el.focus({ preventScroll: true });
  el.scrollIntoView({ block: "nearest" });
}

function setRoving(list, idx) {
  list.forEach((el, i) => (el.tabIndex = i === idx ? 0 : -1));
}

function clamp(n, min, max) {
  return Math.max(min, Math.min(max, n));
}

function moveFocus(list, currentIdx, delta) {
  if (!list.length) return currentIdx;
  const next = clamp(currentIdx + delta, 0, list.length - 1);
  setRoving(list, next);
  focusEl(list[next]);
  return next;
}

function trapTab(e) {
  const overlay = $("#catOverlay");
  if (!overlay || overlay.hidden) return;

  if (e.key === "Escape") {
    e.preventDefault();
    closeOverlay(true); // restore focus
    return;
  }

  if (e.key !== "Tab") return;

  const modal = getModalEl();
  const focusables = getFocusable(modal);
  if (!focusables.length) return;

  const first = focusables[0];
  const last = focusables[focusables.length - 1];

  // shift+tab on first -> last
  if (e.shiftKey && document.activeElement === first) {
    e.preventDefault();
    last.focus();
  }
  // tab on last -> first
  else if (!e.shiftKey && document.activeElement === last) {
    e.preventDefault();
    first.focus();
  }
}

function wireArrowNav(containerSel) {
  const host = $(containerSel);
  if (!host) return;

  host.addEventListener("keydown", (e) => {
    if (e.key !== "ArrowDown" && e.key !== "ArrowUp") return;

    const items = Array.from(host.querySelectorAll(".calItem"));
    const i = items.indexOf(document.activeElement);
    if (i === -1) return;

    e.preventDefault();
    const next = e.key === "ArrowDown" ? items[i + 1] : items[i - 1];
    next?.focus();
  });
}
// -----------------------------------------------------------------------

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
  if (hint) {
    hint.textContent =
      cat.type === "INCOME" ? "Income" :
      cat.type === "EXPENSE" ? "Expense" :
      "Transfer";
  }

  // IMPORTANT: notify listeners (transactions.js) that catId changed
  $("#catId").dispatchEvent(new Event("change", { bubbles: true }));
}

function openOverlay() {
  const overlay = $("#catOverlay");
  if (!overlay) return;

  // remember what had focus BEFORE opening
  lastFocus = document.activeElement;

  // Decide where focus should go AFTER closing:
  // ✅ after picking category, you want to go to Account
  restoreFocusEl = $("#account") || lastFocus;

  overlay.hidden = false;
  document.body.classList.add("modal-open");

  // enable tab trap / esc close while open
  document.addEventListener("keydown", trapTab, true);

  queueMicrotask(() => {
    if (!rootEls.length) return;
    rootIndex = clamp(rootIndex, 0, rootEls.length - 1);
    setRoving(rootEls, rootIndex);
    focusEl(rootEls[rootIndex]);
  });
}

function closeOverlay(focusTarget) {
  const overlay = $("#catOverlay");
  if (overlay) overlay.hidden = true;

  document.body.classList.remove("modal-open");
  document.removeEventListener("keydown", trapTab, true);

  queueMicrotask(() => {
    // focusTarget can be an element, or true = restoreFocusEl, or nothing = lastFocus
    if (focusTarget && focusTarget !== true) focusTarget.focus();
    else if (focusTarget === true) restoreFocusEl?.focus();
    else lastFocus?.focus();
  });
}
function renderRoots() {
  const roots = childrenByParent.get(null) || [];
  const host = $("#catRoots");
  host.innerHTML = "";
  rootEls = [];
  rootIndex = 0;

  roots.forEach((r, i) => {
    const div = document.createElement("div");
    div.className = "calItem";
    div.setAttribute("role", "button");
    div.tabIndex = i === 0 ? 0 : -1;
    div.dataset.rootId = String(r.id);

    div.innerHTML = `
      <div>
        <div class="calDate">${r.name}</div>
        <div class="calPreview muted">${r.code}</div>
      </div>
      <div class="muted small">Open</div>
    `;

    // 👇 KEY: focusing a root shows children
    div.addEventListener("focus", () => {
      rootIndex = i;
      selectedRoot = r;
      $("#catPath").textContent = r.name;
      renderChildren(r.id);

      // keep roving tabindex consistent
      setRoving(rootEls, rootIndex);
    });

    // Click also works
    div.addEventListener("click", () => div.focus());

    // Keyboard on roots
    div.addEventListener("keydown", (e) => {
      if (e.key === "ArrowDown") { e.preventDefault(); rootIndex = moveFocus(rootEls, rootIndex, +1); }
      else if (e.key === "ArrowUp") { e.preventDefault(); rootIndex = moveFocus(rootEls, rootIndex, -1); }
      else if (e.key === "ArrowRight") {
        // Jump into children pane (first child)
        e.preventDefault();
        if (childEls.length) {
          childIndex = 0;
          setRoving(childEls, childIndex);
          focusEl(childEls[childIndex]);
        }
      }
      else if (e.key === "Enter" || e.key === " ") {
        // Treat as "open" (same as focus)
        e.preventDefault();
        div.focus();
      }
      else if (e.key === "Escape") {
        e.preventDefault();
        closeOverlay();
      }
    });

    host.appendChild(div);
    rootEls.push(div);
  });

  // If you already had a selectedRoot, restore it
  if (selectedRoot) {
    const idx = roots.findIndex(x => x.id === selectedRoot.id);
    if (idx >= 0) rootIndex = idx;
  }
  setRoving(rootEls, rootIndex);
}

function renderChildren(parentId) {
  const kids = childrenByParent.get(parentId) || [];
  const host = $("#catChildren");
  host.innerHTML = "";
  childEls = [];
  childIndex = 0;

  $("#catEmpty").style.display = kids.length ? "none" : "";

  kids.forEach((c, i) => {
    const div = document.createElement("div");
    div.className = "calItem";
    div.setAttribute("role", "button");
    div.tabIndex = i === 0 ? 0 : -1;
    div.dataset.childId = String(c.id);

    div.innerHTML = `
      <div>
        <div class="calDate">${c.name}</div>
        <div class="calPreview muted">${c.code}</div>
      </div>
      <div class="muted small">${c.type}</div>
    `;

    div.addEventListener("focus", () => {
      childIndex = i;
      setRoving(childEls, childIndex);
    });

    div.addEventListener("click", () => {
      setCatField(c);
      closeOverlay(true); // go to Account after picking category
    });

    div.addEventListener("keydown", (e) => {
      if (e.key === "ArrowDown") { e.preventDefault(); childIndex = moveFocus(childEls, childIndex, +1); }
      else if (e.key === "ArrowUp") { e.preventDefault(); childIndex = moveFocus(childEls, childIndex, -1); }
      else if (e.key === "ArrowLeft") {
        // GO BACK TO PARENT ROOT
        e.preventDefault();
        if (rootEls.length) {
          setRoving(rootEls, rootIndex);
          focusEl(rootEls[rootIndex]);
        }
      }
      else if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        setCatField(c);
        closeOverlay(true);
      }
      else if (e.key === "Escape") {
        e.preventDefault();
        closeOverlay();
      }
    });

    host.appendChild(div);
    childEls.push(div);
  });

  setRoving(childEls, childIndex);
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

export async function preloadCategoriesActive() {
  const out = await j("/category/active"); // ONLY active
  indexCategories(out);
  populateFilterDropdown();
  renderRoots();
}

export async function preloadCategories() {
  const out = await j("/category"); // returns array of CategoryResponse
  indexCategories(out);

  // populateFilterDropdown();
  renderRoots();
}

export function initCategoryPicker() {
  $("#pickCat")?.addEventListener("click", () => {
    if (!selectedRoot) {
      const roots = childrenByParent.get(null) || [];
      selectedRoot = roots[0] || null;
      rootIndex = 0;
      if (selectedRoot) {
        $("#catPath").textContent = selectedRoot.name;
        renderChildren(selectedRoot.id);
      }
    }
    openOverlay();
  });

  $("#catClose")?.addEventListener("click", closeOverlay);

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
