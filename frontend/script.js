const API_BASE_URL = "http://localhost:8080";

const state = {
  products: [],
  alerts: [],
  productsLoaded: false,
  alertsLoaded: false
};

const elements = {
  backendBanner: document.querySelector("#backendBanner"),
  connectionDot: document.querySelector("#connectionDot"),
  connectionLabel: document.querySelector("#connectionLabel"),
  productBody: document.querySelector("#productsTableBody"),
  alertsBody: document.querySelector("#alertsTableBody"),
  productSearch: document.querySelector("#productSearch"),
  productFilter: document.querySelector("#productFilter"),
  productModal: document.querySelector("#productModal"),
  movementModal: document.querySelector("#movementModal"),
  productForm: document.querySelector("#productForm"),
  movementForm: document.querySelector("#movementForm")
};

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (character) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  })[character]);
}

function formatDate(value) {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "—";
  return new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(date);
}

function getErrorMessage(error, fallback) {
  return error instanceof Error && error.message ? error.message : fallback;
}

async function requestJson(path, options = {}) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        Accept: "application/json",
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...options.headers
      }
    });
  } catch {
    throw new Error("Unable to connect to StockAlert backend. Make sure Spring Boot is running on port 8080.");
  }

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      message = body.message || body.error || body.detail || message;
    } catch {
      // Some backend error responses do not include a JSON body.
    }
    throw new Error(message);
  }

  if (response.status === 204) return null;
  return response.json();
}

function setBackendStatus(connected) {
  elements.connectionDot.classList.toggle("is-connected", connected);
  elements.connectionDot.classList.toggle("is-disconnected", !connected);
  elements.connectionLabel.textContent = connected ? "Backend connected" : "Backend unavailable";
  elements.backendBanner.hidden = connected;
}

function updateDashboardStats() {
  const lowStockCount = state.products.filter((product) => product.currentStock <= product.reorderThreshold).length;
  const stockUnits = state.products.reduce((total, product) => total + (Number(product.currentStock) || 0), 0);

  document.querySelector("#totalProducts").textContent = state.productsLoaded ? state.products.length.toLocaleString() : "--";
  document.querySelector("#lowStockProducts").textContent = state.productsLoaded ? lowStockCount.toLocaleString() : "--";
  document.querySelector("#openAlerts").textContent = state.alertsLoaded ? state.alerts.length.toLocaleString() : "--";
  document.querySelector("#totalStockUnits").textContent = state.productsLoaded ? stockUnits.toLocaleString() : "--";
  document.querySelector("#productCount").textContent = state.productsLoaded ? state.products.length.toLocaleString() : "0";
  document.querySelector("#productNavCount").textContent = state.productsLoaded ? state.products.length.toLocaleString() : "0";
  document.querySelector("#alertCount").textContent = state.alertsLoaded ? state.alerts.length.toLocaleString() : "0";
  document.querySelector("#alertNavCount").textContent = state.alertsLoaded ? state.alerts.length.toLocaleString() : "0";
}

function renderProducts() {
  if (!state.productsLoaded) return;
  const query = elements.productSearch.value.trim().toLowerCase();
  const filter = elements.productFilter.value;
  const products = state.products.filter((product) => {
    const isLow = product.currentStock <= product.reorderThreshold;
    const matchesQuery = `${product.name} ${product.sku}`.toLowerCase().includes(query);
    const matchesFilter = filter === "all" || (filter === "low" ? isLow : !isLow);
    return matchesQuery && matchesFilter;
  });

  if (products.length === 0) {
    const message = state.products.length === 0 ? "No products in the catalog yet." : "No products match this search.";
    elements.productBody.innerHTML = `<tr><td class="empty-state" colspan="7">${message}</td></tr>`;
    document.querySelector("#productTableSummary").textContent = state.products.length === 0 ? "Your product catalog is ready." : "No matching products.";
    return;
  }

  elements.productBody.innerHTML = products.map((product) => {
    const isLow = product.currentStock <= product.reorderThreshold;
    const initials = String(product.name || "?").trim().slice(0, 2);
    return `<tr>
      <td><div class="product-cell"><span class="product-avatar">${escapeHtml(initials)}</span><div><div class="product-name" title="${escapeHtml(product.name)}">${escapeHtml(product.name)}</div><div class="product-id">ID ${escapeHtml(product.id)}</div></div></div></td>
      <td><span class="sku-code">${escapeHtml(product.sku)}</span></td>
      <td class="numeric-cell ${isLow ? "stock-cell--low" : ""}">${Number(product.currentStock).toLocaleString()}</td>
      <td class="numeric-cell">${Number(product.reorderThreshold).toLocaleString()}</td>
      <td class="numeric-cell">${Number(product.reorderQuantity).toLocaleString()}</td>
      <td><span class="status-pill ${isLow ? "status-pill--low" : "status-pill--good"}">${isLow ? "LOW STOCK" : "IN STOCK"}</span></td>
      <td><button class="action-button" type="button" data-movement-product="${escapeHtml(product.id)}" aria-label="Record movement for ${escapeHtml(product.name)}"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>Movement</button></td>
    </tr>`;
  }).join("");

  document.querySelector("#productTableSummary").textContent = `Showing ${products.length} of ${state.products.length} ${state.products.length === 1 ? "product" : "products"}`;
}

function renderAlerts() {
  if (!state.alertsLoaded) return;
  if (state.alerts.length === 0) {
    elements.alertsBody.innerHTML = '<tr><td class="empty-state" colspan="6">No open reorder alerts. Stock levels are looking good.</td></tr>';
    document.querySelector("#alertTableSummary").textContent = "No open items in the reorder queue.";
    return;
  }

  elements.alertsBody.innerHTML = state.alerts.map((alert) => `<tr>
    <td><div class="product-cell"><span class="product-avatar">${escapeHtml(String(alert.productName || "?").trim().slice(0, 2))}</span><div><div class="product-name">${escapeHtml(alert.productName)}</div><div class="product-id">Alert #${escapeHtml(alert.id)}</div></div></div></td>
    <td class="numeric-cell stock-cell--low">${Number(alert.currentStock).toLocaleString()}</td>
    <td class="numeric-cell">${Number(alert.reorderQuantity).toLocaleString()}</td>
    <td><span class="status-pill status-pill--open">${escapeHtml(alert.status || "OPEN")}</span></td>
    <td>${escapeHtml(formatDate(alert.createdAt))}</td>
    <td><button class="action-button fulfill-button" type="button" data-fulfill-alert="${escapeHtml(alert.id)}" aria-label="Fulfill reorder alert for ${escapeHtml(alert.productName)}">Fulfill</button></td>
  </tr>`).join("");

  document.querySelector("#alertTableSummary").textContent = `${state.alerts.length} open ${state.alerts.length === 1 ? "alert" : "alerts"} in the reorder queue.`;
}

function renderMovementProductOptions() {
  const select = document.querySelector("#movementProductSelect");
  const previousValue = select.value;
  select.innerHTML = '<option value="">Select product</option>' + state.products.map((product) => `<option value="${escapeHtml(product.id)}">${escapeHtml(product.name)} · ${escapeHtml(product.sku)}</option>`).join("");
  if (state.products.some((product) => String(product.id) === previousValue)) select.value = previousValue;
  document.querySelector("#quickMovementButton").disabled = !select.value;
}

async function loadProducts() {
  const products = await requestJson("/api/products");
  if (!Array.isArray(products)) throw new Error("The products response was not a list.");
  state.products = products;
  state.productsLoaded = true;
  renderProducts();
  renderMovementProductOptions();
  updateDashboardStats();
}

async function loadAlerts() {
  const alerts = await requestJson("/api/reorder-alerts/open");
  if (!Array.isArray(alerts)) throw new Error("The reorder alerts response was not a list.");
  state.alerts = alerts;
  state.alertsLoaded = true;
  renderAlerts();
  updateDashboardStats();
}

async function refreshDashboard({ showErrorToast = false } = {}) {
  const refreshButton = document.querySelector("#refreshButton");
  refreshButton.classList.add("is-refreshing");
  const results = await Promise.allSettled([loadProducts(), loadAlerts()]);
  refreshButton.classList.remove("is-refreshing");

  const failed = results.filter((result) => result.status === "rejected");
  const connected = failed.length === 0;
  setBackendStatus(connected);
  if (failed.length > 0) {
    const message = failed.some((result) => result.reason?.message?.startsWith("Unable to connect"))
      ? "Unable to connect to StockAlert backend. Make sure Spring Boot is running on port 8080."
      : failed.map((result) => result.reason?.message).filter(Boolean).join(" ");
    if (showErrorToast) showToast(message, "error");
    if (!state.productsLoaded) elements.productBody.innerHTML = `<tr><td class="empty-state" colspan="7">${escapeHtml(message)}</td></tr>`;
    if (!state.alertsLoaded) elements.alertsBody.innerHTML = `<tr><td class="empty-state" colspan="6">${escapeHtml(message)}</td></tr>`;
    document.querySelector("#productTableSummary").textContent = state.productsLoaded ? document.querySelector("#productTableSummary").textContent : "Catalog unavailable.";
    document.querySelector("#alertTableSummary").textContent = state.alertsLoaded ? document.querySelector("#alertTableSummary").textContent : "Alert queue unavailable.";
  }
}

function showToast(message, type = "success") {
  const toast = document.createElement("div");
  toast.className = `toast ${type === "error" ? "toast--error" : ""}`;
  toast.innerHTML = `<span class="toast-icon" aria-hidden="true">${type === "error" ? "!" : "✓"}</span><span class="toast-message">${escapeHtml(message)}</span><button class="toast-dismiss" type="button" aria-label="Dismiss notification">×</button>`;
  const remove = () => toast.remove();
  toast.querySelector(".toast-dismiss").addEventListener("click", remove);
  document.querySelector("#toastRegion").append(toast);
  window.setTimeout(remove, 5200);
}

function setModalOpen(modal, open) {
  modal.hidden = !open;
  document.body.classList.toggle("modal-open", open);
  if (open) window.setTimeout(() => modal.querySelector("input:not([type=hidden]), select")?.focus(), 30);
}

function openAddProductModal() {
  elements.productForm.reset();
  document.querySelector("#productFormError").hidden = true;
  setModalOpen(elements.productModal, true);
}

function openMovementModal(productId) {
  const product = state.products.find((item) => String(item.id) === String(productId));
  if (!product) {
    showToast("Select a product before recording a movement.", "error");
    return;
  }
  elements.movementForm.reset();
  elements.movementForm.elements.productId.value = product.id;
  document.querySelector("#movementProductName").textContent = product.name;
  document.querySelector("#movementFormError").hidden = true;
  setModalOpen(elements.movementModal, true);
}

function setFormSubmitting(form, submitting) {
  const submitButton = form.querySelector('button[type="submit"]');
  submitButton.disabled = submitting;
  submitButton.classList.toggle("is-submitting", submitting);
}

async function submitProduct(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const errorElement = document.querySelector("#productFormError");
  const data = new FormData(form);
  const payload = {
    name: String(data.get("name")).trim(),
    sku: String(data.get("sku")).trim(),
    reorderThreshold: Number(data.get("reorderThreshold")),
    reorderQuantity: Number(data.get("reorderQuantity"))
  };
  if (!payload.name || !payload.sku || !Number.isInteger(payload.reorderThreshold) || payload.reorderThreshold < 0 || !Number.isInteger(payload.reorderQuantity) || payload.reorderQuantity < 0) {
    errorElement.textContent = "Enter a product name, SKU, and valid non-negative whole-number reorder values.";
    errorElement.hidden = false;
    return;
  }

  errorElement.hidden = true;
  setFormSubmitting(form, true);
  try {
    await requestJson("/api/products", { method: "POST", body: JSON.stringify(payload) });
    setModalOpen(elements.productModal, false);
    await refreshDashboard();
    showToast("Product created successfully.");
  } catch (error) {
    const message = getErrorMessage(error, "Unable to create product.");
    errorElement.textContent = message;
    errorElement.hidden = false;
    showToast(message, "error");
  } finally {
    setFormSubmitting(form, false);
  }
}

async function submitMovement(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const errorElement = document.querySelector("#movementFormError");
  const data = new FormData(form);
  const productId = String(data.get("productId"));
  const payload = {
    movementType: String(data.get("movementType")),
    reason: String(data.get("reason")),
    quantity: Number(data.get("quantity"))
  };
  if (!productId || !Number.isInteger(payload.quantity) || payload.quantity <= 0) {
    errorElement.textContent = "Choose a product and enter a whole-number quantity greater than zero.";
    errorElement.hidden = false;
    return;
  }

  errorElement.hidden = true;
  setFormSubmitting(form, true);
  try {
    await requestJson(`/api/products/${encodeURIComponent(productId)}/movements`, { method: "POST", body: JSON.stringify(payload) });
    setModalOpen(elements.movementModal, false);
    await refreshDashboard();
    showToast("Stock movement recorded successfully.");
  } catch (error) {
    const message = getErrorMessage(error, "Unable to record stock movement.");
    errorElement.textContent = message;
    errorElement.hidden = false;
    showToast(message, "error");
  } finally {
    setFormSubmitting(form, false);
  }
}

async function fulfillAlert(alertId, button) {
  if (!alertId || button.disabled) return;
  button.disabled = true;
  const originalText = button.textContent;
  button.textContent = "Working...";
  try {
    await requestJson(`/api/reorder-alerts/${encodeURIComponent(alertId)}/fulfill`, { method: "PUT" });
    await refreshDashboard();
    showToast("Reorder alert fulfilled successfully.");
  } catch (error) {
    button.disabled = false;
    button.textContent = originalText;
    showToast(getErrorMessage(error, "Unable to fulfill reorder alert."), "error");
  }
}

function setActiveNavigation(sectionId) {
  document.querySelectorAll(".nav-link").forEach((link) => link.classList.toggle("is-active", link.dataset.section === sectionId));
}

function initializeDashboard() {
  document.querySelector("#todayLabel").textContent = new Intl.DateTimeFormat(undefined, { weekday: "short", month: "short", day: "numeric", year: "numeric" }).format(new Date());
  document.querySelector("#refreshButton").addEventListener("click", () => refreshDashboard({ showErrorToast: true }));
  document.querySelector("#bannerRetryButton").addEventListener("click", () => refreshDashboard({ showErrorToast: true }));
  document.querySelector("#addProductButton").addEventListener("click", openAddProductModal);
  elements.productSearch.addEventListener("input", renderProducts);
  elements.productFilter.addEventListener("change", renderProducts);
  elements.productForm.addEventListener("submit", submitProduct);
  elements.movementForm.addEventListener("submit", submitMovement);
  document.querySelector("#quickMovementButton").addEventListener("click", () => openMovementModal(document.querySelector("#movementProductSelect").value));
  document.querySelector("#movementProductSelect").addEventListener("change", (event) => {
    document.querySelector("#quickMovementButton").disabled = !event.currentTarget.value;
  });

  document.addEventListener("click", (event) => {
    const movementButton = event.target.closest("[data-movement-product]");
    if (movementButton) openMovementModal(movementButton.dataset.movementProduct);
    const fulfillButton = event.target.closest("[data-fulfill-alert]");
    if (fulfillButton) fulfillAlert(fulfillButton.dataset.fulfillAlert, fulfillButton);
    if (event.target.matches("[data-close-modal]")) setModalOpen(event.target.closest(".modal-backdrop"), false);
    if (event.target.classList.contains("modal-backdrop")) setModalOpen(event.target, false);
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") document.querySelectorAll(".modal-backdrop:not([hidden])").forEach((modal) => setModalOpen(modal, false));
  });

  document.querySelectorAll(".nav-link").forEach((link) => link.addEventListener("click", () => setActiveNavigation(link.dataset.section)));
  if ("IntersectionObserver" in window) {
    const sections = ["dashboard", "products", "movements", "alerts"].map((id) => document.getElementById(id));
    const observer = new IntersectionObserver((entries) => {
      const visible = entries.filter((entry) => entry.isIntersecting).sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];
      if (visible) setActiveNavigation(visible.target.id);
    }, { rootMargin: "-20% 0px -65% 0px", threshold: [0, .2, .5] });
    sections.forEach((section) => observer.observe(section));
  }

  refreshDashboard();
}

document.addEventListener("DOMContentLoaded", initializeDashboard);