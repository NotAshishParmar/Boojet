// /assets/js/features/categoryAdmin.js
import { $ } from "../core/dom.js";
import { j } from "../core/api.js";
import { esc } from "../core/format.js";
import { preloadCategories, preloadCategoriesActive, getAllCategories, getCategoryById } from "./categories.js";

const CAT_API = "/category";

function normalizeCode(s) {
  return String(s || "").trim().toUpperCase().replace(/\s+/g, "_");
}

function buildTree(list) {
  const byParent = new Map();
  for (const c of list) {
    const pid = c.parentId ?? null;
    if (!byParent.has(pid)) byParent.set(pid, []);
    byParent.get(pid).push(c);
  }
  for (const arr of byParent.values()) {
    arr.sort((a, b) => {
      const soA = (a.sortOrder ?? 999999);
      const soB = (b.sortOrder ?? 999999);
      if (soA !== soB) return soA - soB;
      return String(a.name).localeCompare(String(b.name));
    });
  }
  return byParent;
}

function catLabel(c) {
  if (!c) return "—";
  if (c.parentCode) return `${c.parentCode} → ${c.name}`;
  return `${c.code} → ${c.name}`;
}

function setFormHint(msg) {
  const el = $("#catFormHint");
  if (el) el.textContent = msg || "";
}

function fillParentDropdown(all) {
  const sel = $("#catParent");
  if (!sel) return;

  sel.innerHTML = `<option value="">(No parent)</option>`;

  // parents = roots only (parentId == null)
  const roots = all.filter(c => c.parentId == null);

  for (const r of roots) {
    const opt = document.createElement("option");
    opt.value = String(r.id);
    opt.textContent = `${r.name} (${r.code})`;
    sel.appendChild(opt);
  }
}

function renderAdminList(all) {
  const host = $("#catAdminList");
  if (!host) return;

  const byParent = buildTree(all);
  const roots = byParent.get(null) || [];

  host.innerHTML = "";

  for (const r of roots) {
    const kids = byParent.get(r.id) || [];
    const hasKids = kids.length > 0;

    const group = document.createElement("div");
    group.className = "cat-admin-group";

    const root = document.createElement("div");
    root.className = "cat-admin-item";
    root.innerHTML = `
      <div class="cat-admin-main">
        <div class="cat-admin-topline">
          <button
            class="cat-toggle ${hasKids ? "" : "is-hidden"}"
            type="button"
            data-act="toggle"
            data-id="${r.id}"
            aria-expanded="false"
            title="${hasKids ? "Show subcategories" : ""}"
          >
            ▸
          </button>
          <div class="cat-admin-title">${esc(r.name)}</div>
        </div>
        <div class="cat-admin-meta">${esc(r.code)} • ${esc(r.type)}</div>
      </div>
      <div class="cat-admin-actions">
        <button class="side-btn btn-ghost" type="button" data-act="edit" data-id="${r.id}">Edit</button>
        <button class="side-btn btn-ghost danger" type="button" data-act="del" data-id="${r.id}">Delete</button>
      </div>
    `;
    root.classList.toggle("is-inactive", !r.active);
    group.appendChild(root);

    if (hasKids) {
      const childrenWrap = document.createElement("div");
      childrenWrap.className = "cat-admin-children";

      for (const c of kids) {
        const metaParts = [
          esc(c.code),
          esc(c.type),
          c.essential === true
            ? "Essential"
            : c.essential === false
              ? "Non-essential"
              : null
        ].filter(Boolean);

        const child = document.createElement("div");
        child.className = "cat-admin-item child";
        child.innerHTML = `
          <div class="cat-admin-main">
            <div class="cat-admin-title">${esc(c.name)}</div>
            <div class="cat-admin-meta">${metaParts.join(" • ")}</div>
          </div>
          <div class="cat-admin-actions">
            <button class="side-btn btn-ghost" type="button" data-act="edit" data-id="${c.id}">Edit</button>
            <button class="side-btn btn-ghost danger" type="button" data-act="del" data-id="${c.id}">Delete</button>
          </div>
        `;
        child.classList.toggle("is-inactive", !c.active);
        childrenWrap.appendChild(child);
      }

      group.appendChild(childrenWrap);
    }

    host.appendChild(group);
  }

  host.onclick = async (e) => {
    const btn = e.target.closest("button[data-act]");
    if (!btn) return;

    const act = btn.dataset.act;
    const id = Number(btn.dataset.id);
    if (!Number.isFinite(id) && act !== "toggle") return;

    if (act === "toggle") {
      const group = btn.closest(".cat-admin-group");
      if (!group) return;

      const isOpen = group.classList.toggle("is-open");
      btn.textContent = isOpen ? "▾" : "▸";
      btn.setAttribute("aria-expanded", String(isOpen));
      return;
    }

    if (act === "edit") {
      startEdit(id);
    } else if (act === "del") {
      await deleteCategory(id);
    }
  };
}

function startEdit(id) {
  const c = getCategoryById(id);
  if (!c) return;

  $("#catEditId").value = String(c.id);
  $("#catCode").value = c.code || "";
  $("#catName").value = c.name || "";
  $("#catParent").value = (c.parentId != null) ? String(c.parentId) : "";
  $("#catEssential").value = (c.essential === true) ? "true" : (c.essential === false) ? "false" : "";
  $("#catSortOrder").value = (c.sortOrder != null) ? String(c.sortOrder) : "";
  $("#catActive").checked = !!c.active;

  // type is read-only (derived)
  $("#catType").value = c.type || "EXPENSE";

  $("#catSaveBtn").textContent = "Update";
  setFormHint(`Editing: ${catLabel(c)}`);

  // open the collapsible
  $("#catAdminCollapse").open = true;
}

function clearForm() {
  $("#catEditId").value = "";
  $("#catCode").value = "";
  $("#catName").value = "";
  $("#catParent").value = "";
  $("#catEssential").value = "";
  $("#catSortOrder").value = "";
  $("#catActive").checked = true;
  $("#catType").value = "EXPENSE";
  $("#catSaveBtn").textContent = "Save";
  setFormHint("");
}

async function deleteCategory(id) {
  const c = getCategoryById(id);
  if (!confirm(`Delete category "${c?.name ?? id}"?`)) return;

  await j(`${CAT_API}/${id}`, { method: "DELETE" });
  await refreshAllCategoryUIs();
}

function parseOptionalInt(v) {
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
}

function syncEssentialEnabled() {
  const hasParent = !!$("#catParent").value;
  $("#catEssential").disabled = !hasParent;
  if (!hasParent) $("#catEssential").value = "";
}

async function saveCategoryFromForm() {
  const editId = $("#catEditId").value ? Number($("#catEditId").value) : null;

  const code = normalizeCode($("#catCode").value);
  const name = String($("#catName").value || "").trim();

  const parentId = $("#catParent").value ? Number($("#catParent").value) : null;

  // essential only meaningful if it’s a child
  const essentialRaw = $("#catEssential").value;
  let essential =
    essentialRaw === "true" ? true :
    essentialRaw === "false" ? false :
    null;

  // if root, force essential null (matches your backend semantics)
  if (!parentId) essential = null;

  const sortOrder = $("#catSortOrder").value ? parseOptionalInt($("#catSortOrder").value) : null;

  if (!code || !name) {
    alert("Code and Name are required.");
    return;
  }

  // -------------------------
  // CREATE (POST): CategoryCreateRequest
  // -------------------------
  if (!editId) {
    // type is REQUIRED on create
    // rule: if parent selected => inherit parent's type, else use the form's selected type
    let type = $("#catType").value || "EXPENSE";
    if (parentId) {
      const parent = getCategoryById(parentId);
      if (!parent) { alert("Parent not found."); return; }
      type = parent.type;
    }

    const payload = {
      code,
      name,
      type,        // REQUIRED by CategoryCreateRequest
      essential,
      sortOrder,
      parentId     // Long (nullable)
    };

    await j(CAT_API, { method: "POST", body: JSON.stringify(payload) });
    await refreshAllCategoryUIs();
    clearForm();
    return;
  }

  // -------------------------
  // UPDATE
  // PUT full update
  // -------------------------

  const USE_PUT_FOR_UPDATE = true;

  if (USE_PUT_FOR_UPDATE) {
    // PUT: CategoryPutRequest (no type, no active)
    const payload = {
      code,
      name,
      essential,
      sortOrder,
      parentId // Long (nullable)
    };

    await j(`${CAT_API}/${editId}`, { method: "PUT", body: JSON.stringify(payload) });
    await refreshAllCategoryUIs();
    clearForm();
    return;
  }

  await refreshAllCategoryUIs();
  clearForm();
}

async function refreshAllCategoryUIs() {
  // 1) refresh categories module (modal + filter)
  await preloadCategories();

  // 2) refresh admin list + parent dropdown using the cached categories from categories.js
  const all = getAllCategories();
  fillParentDropdown(all);
  renderAdminList(all);

  // 3) load ACTIVE for picker + filter dropdown
  await preloadCategoriesActive();
}

export function initCategoryAdmin() {
  $("#catClearBtn")?.addEventListener("click", clearForm);

  $("#catForm")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    await saveCategoryFromForm();
  });

  syncEssentialEnabled();

  // if parent changes, update type hint in the form
  $("#catParent")?.addEventListener("change", () => {
    const pid = $("#catParent").value ? Number($("#catParent").value) : null;
    if (!pid) {
      setFormHint("Root category (type locked for now).");
      return;
    }
    const p = getCategoryById(pid);
    $("#catType").value = p?.type ?? "EXPENSE";
    setFormHint(`Subcategory under: ${p?.name ?? "?"} (inherits ${p?.type ?? "type"})`);
  });
}

export async function loadCategoryAdmin() {
  await refreshAllCategoryUIs();
}
