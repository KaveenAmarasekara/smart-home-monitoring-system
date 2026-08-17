import { initializeApp } from "https://www.gstatic.com/firebasejs/12.17.1/firebase-app.js";
import {
  createUserWithEmailAndPassword,
  getAuth,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signOut,
} from "https://www.gstatic.com/firebasejs/12.17.1/firebase-auth.js";
import {
  addDoc,
  collection,
  deleteDoc,
  deleteField,
  doc,
  getDocs,
  getFirestore,
  onSnapshot,
  setDoc,
} from "https://www.gstatic.com/firebasejs/12.17.1/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "AIzaSyDgQpJuhCXJkGYR80oxZ1u_Z3szkginDkQ",
  authDomain: "smarthomemonitoring-ef36a.firebaseapp.com",
  projectId: "smarthomemonitoring-ef36a",
  storageBucket: "smarthomemonitoring-ef36a.firebasestorage.app",
  messagingSenderId: "86072852464",
  appId: "1:86072852464:web:showcase",
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);

// ── DOM refs ────────────────────────────────────────────────────────────────
const els = {
  authPanel: document.querySelector("#authPanel"),
  connectionStatus: document.querySelector("#connectionStatus"),
  dashboard: document.querySelector("#dashboard"),
  deviceList: document.querySelector("#deviceList"),
  emailInput: document.querySelector("#emailInput"),
  loginForm: document.querySelector("#loginForm"),
  message: document.querySelector("#message"),
  notificationList: document.querySelector("#notificationList"),
  onlineDevices: document.querySelector("#onlineDevices"),
  passwordInput: document.querySelector("#passwordInput"),
  sessionLabel: document.querySelector("#sessionLabel"),
  settingsList: document.querySelector("#settingsList"),
  shutdownCount: document.querySelector("#shutdownCount"),
  signOutButton: document.querySelector("#signOutButton"),
  totalDevices: document.querySelector("#totalDevices"),
  weeklyUsage: document.querySelector("#weeklyUsage"),
  // Device detail panel
  detailPanel: document.querySelector("#detailPanel"),
  detailOverlay: document.querySelector("#detailOverlay"),
  detailClose: document.querySelector("#detailClose"),
  detailContent: document.querySelector("#detailContent"),
  addDeviceBtn: document.querySelector("#addDeviceBtn"),
  addDeviceForm: document.querySelector("#addDeviceForm"),
};

// ── State ────────────────────────────────────────────────────────────────────
let devices = [];
let notifications = [];
let settings = { safetyAlerts: true, deviceAlerts: true, scheduleAlerts: false };
let unsubscribeHandlers = [];
let selectedDeviceId = null;

// ── Auth ─────────────────────────────────────────────────────────────────────
els.loginForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  setMessage("Connecting…");
  const email = els.emailInput.value.trim();
  const password = els.passwordInput.value;
  try {
    await signInWithEmailAndPassword(auth, email, password);
  } catch (err) {
    if (err.code === "auth/user-not-found" || err.code === "auth/invalid-credential") {
      try {
        await createUserWithEmailAndPassword(auth, email, password);
      } catch (createErr) {
        setMessage(createErr.message);
      }
      return;
    }
    setMessage(err.message);
  }
});

els.signOutButton.addEventListener("click", () => signOut(auth));

onAuthStateChanged(auth, (user) => {
  clearListeners();

  if (!user) {
    els.authPanel.classList.remove("hidden");
    els.dashboard.classList.add("hidden");
    els.signOutButton.classList.add("hidden");
    els.connectionStatus.classList.remove("online");
    els.sessionLabel.textContent = "Signed out";
    setMessage("");
    return;
  }

  els.authPanel.classList.add("hidden");
  els.dashboard.classList.remove("hidden");
  els.signOutButton.classList.remove("hidden");
  els.connectionStatus.classList.add("online");
  els.sessionLabel.textContent = user.email;
  setMessage("");

  listenForDevices();
  listenForNotifications();
  listenForSettings(user.uid);
});

els.clearReadNotifications?.addEventListener("click", clearReadNotifications);

// ── Listeners ─────────────────────────────────────────────────────────────────
function listenForDevices() {
  const unsub = onSnapshot(
    collection(db, "devices"),
    (snap) => {
      devices = snap.docs
        .map((d) => ({ id: d.id, ...d.data() }))
        .sort((a, b) =>
          `${a.floorId}-${a.gridY}-${a.gridX}`.localeCompare(`${b.floorId}-${b.gridY}-${b.gridX}`)
        );
      renderDevices();
      renderMetrics();
      applyDeviceAutomation();
      if (selectedDeviceId) renderDetailPanel(selectedDeviceId);
    },
    (err) => setMessage(err.message),
  );
  unsubscribeHandlers.push(unsub);
}

function listenForNotifications() {
  const unsub = onSnapshot(
    collection(db, "notifications"),
    (snap) => {
      notifications = snap.docs
        .map((d) => ({ id: d.id, ...d.data() }))
        .sort((a, b) => notificationTimestampMs(b) - notificationTimestampMs(a));
      renderNotifications();
    },
    (err) => setMessage(err.message),
  );
  unsubscribeHandlers.push(unsub);
}

function listenForSettings(uid) {
  const ref = doc(db, "users", uid, "settings", "notifications");
  const unsub = onSnapshot(
    ref,
    (snap) => {
      settings = { ...settings, ...snap.data() };
      renderSettings(uid);
      renderNotifications();
    },
    (err) => setMessage(err.message),
  );
  unsubscribeHandlers.push(unsub);
}

// ── Auto-shutdown ticker (every 20 s) ─────────────────────────────────────────
setInterval(() => {
  applyDeviceAutomation();
}, 20_000);

async function applyDeviceAutomation() {
  const now = Date.now();

  // Schedule automation for LIGHT devices.
  for (const device of devices) {
    if (device.type !== "LIGHT" || !device.scheduleEnabled) continue;
    if (device.status === "ERROR" || device.status === "DISCONNECTED") continue;

    const shouldBeOn = isNowWithinSchedule(device.scheduleStart, device.scheduleEnd);
    const targetStatus = shouldBeOn ? "ON" : "OFF";

    if (device.status !== targetStatus) {
      await updateDevice(device, { status: targetStatus }, { notifyStatus: false });
      await pushNotification({
        title: `${device.name} schedule`,
        description: `Light switched ${shouldBeOn ? "on" : "off"} automatically.`,
        important: false,
      }, `schedule_${device.id}_${targetStatus}_${Math.floor(now / 60_000)}`);
    }
  }

  for (const device of devices) {
    if (device.type !== "IRON" || device.status !== "ON") continue;
    const turnedOnAt = Number(device.turnedOnAt ?? 0);
    if (turnedOnAt <= 0) continue;
    const elapsedMin = (now - turnedOnAt) / 60_000;
    const maxMin = Number(device.maxOnDurationMinutes ?? 15);
    if (elapsedMin >= maxMin) {
      await updateDevice(device, { status: "OFF" }, { notifyStatus: false });
      await pushNotification({
        title: `${device.name} automatically switched off`,
        description: `Maximum ON duration of ${maxMin} minutes reached.`,
        important: true,
      }, `auto_${device.id}_${turnedOnAt}_${maxMin}`);
    }
  }
  // Re-render to refresh countdowns
  if (devices.some((d) => d.type === "IRON" && d.status === "ON" && (d.turnedOnAt ?? 0) > 0)) {
    renderDevices();
    if (selectedDeviceId) renderDetailPanel(selectedDeviceId);
  }
}

// ── Render: device list ────────────────────────────────────────────────────────
function renderDevices() {
  if (devices.length === 0) {
    els.deviceList.innerHTML = `<p class="empty">No devices found. Add a device to start monitoring Firebase data.</p>`;
    return;
  }

  const floors = [
    { id: "ground", label: "Ground Floor" },
    { id: "first", label: "First Floor" },
  ];

  els.deviceList.innerHTML = floors
    .map(({ id, label }) => {
      const floorDevices = devices.filter((d) => d.floorId === id);
      if (!floorDevices.length) return "";

      const rows = floorDevices.map((device) => {
        const isOn = device.status === "ON";
        const isUnavailable = device.status === "ERROR" || device.status === "DISCONNECTED";

        return `
          <div class="device-row" data-device-id="${escapeHtml(device.id)}">
            <div class="device-info">
              <div class="device-name">
                <span class="device-type-icon">${deviceIcon(device.type)}</span>
                ${escapeHtml(device.name)}
              </div>
              <div class="device-meta">
                <span>${escapeHtml(device.room)}</span>
                <span class="badge status-${device.status.toLowerCase()}">${escapeHtml(device.status)}</span>
                ${ironCountdownHtml(device)}
              </div>
              ${device.type === "LIGHT" && device.scheduleEnabled
                ? `<div class="schedule-line">📅 ${escapeHtml(device.scheduleStart)}–${escapeHtml(device.scheduleEnd)}</div>`
                : ""}
            </div>
            <div class="device-actions">
              ${device.type === "LIGHT"
                ? `<input type="range" min="0" max="100" value="${device.brightness ?? 100}"
                     data-brightness="${device.id}" aria-label="brightness" />`
                : ""}
              <button
                class="toggle ${isOn ? "on" : ""}"
                data-toggle="${device.id}"
                ${isUnavailable ? "disabled" : ""}
                aria-label="Toggle ${escapeHtml(device.name)}"
              ></button>
            </div>
          </div>`;
      }).join("");

      return `<div class="floor-group"><div class="floor-label">${label}</div>${rows}</div>`;
    })
    .join("");

  // Toggle handlers
  document.querySelectorAll("[data-toggle]").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      const device = devices.find((d) => d.id === btn.dataset.toggle);
      updateDevice(device, { status: device.status === "ON" ? "OFF" : "ON" });
    });
  });

  // Brightness handlers
  document.querySelectorAll("[data-brightness]").forEach((slider) => {
    slider.addEventListener("change", (e) => {
      e.stopPropagation();
      const device = devices.find((d) => d.id === slider.dataset.brightness);
      updateDevice(device, { brightness: Number(slider.value) });
    });
  });

  // Row click → detail panel
  document.querySelectorAll(".device-row[data-device-id]").forEach((row) => {
    row.addEventListener("click", () => {
      selectedDeviceId = row.dataset.deviceId;
      renderDetailPanel(selectedDeviceId);
      openDetailPanel();
    });
  });
}

// ── Render: metrics ────────────────────────────────────────────────────────────
function renderMetrics() {
  els.totalDevices.textContent = devices.length;
  els.onlineDevices.textContent = devices.filter((d) => d.status === "ON").length;
  els.weeklyUsage.textContent = formatMinutes(
    devices.reduce((s, d) => s + Number(d.usageMinutesThisWeek ?? 0), 0)
  );
  els.shutdownCount.textContent = devices.reduce(
    (s, d) => s + Number(d.safetyShutdownsThisMonth ?? 0), 0
  );
}

// ── Render: notifications ──────────────────────────────────────────────────────
function renderNotifications() {
  const visible = notifications.filter((n) => {
    const t = String(n.title ?? "").toLowerCase();
    if (t.includes("switched off")) return settings.safetyAlerts;
    if (t.includes("disconnected")) return settings.deviceAlerts;
    if (t.includes("schedule")) return settings.scheduleAlerts;
    return true;
  });

  els.notificationList.innerHTML = !visible.length
    ? `<p class="empty">No visible notifications.</p>`
    : visible.map((n) => `
        <div class="notification-row ${n.read ? "read" : ""} ${n.important && !n.read ? "important" : ""}">
          <strong>${escapeHtml(n.title)}</strong>
          <p>${escapeHtml(n.description)}</p>
          <span>${escapeHtml(notificationDisplayTime(n))}${n.read ? " - Read" : ""}</span>
          <div class="notification-actions">
            ${n.read ? "" : `<button class="ghost icon-btn" data-mark-read="${escapeHtml(n.id)}">Mark read</button>`}
            <button class="ghost icon-btn" data-clear-notification="${escapeHtml(n.id)}">Clear</button>
          </div>
        </div>`).join("");

  document.querySelectorAll("[data-mark-read]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      await setDoc(doc(db, "notifications", btn.dataset.markRead), { read: true }, { merge: true });
    });
  });

  document.querySelectorAll("[data-clear-notification]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      await deleteDoc(doc(db, "notifications", btn.dataset.clearNotification));
    });
  });
}

// ── Render: settings ───────────────────────────────────────────────────────────
function renderSettings(uid) {
  const rows = [
    ["safetyAlerts", "Safety Alerts", "Auto-shutdown notifications"],
    ["deviceAlerts", "Device Alerts", "Errors and disconnected"],
    ["scheduleAlerts", "Schedule Alerts", "Scheduled light events"],
  ];

  els.settingsList.innerHTML = rows.map(([key, title, desc]) => `
    <div class="setting-row">
      <div>
        <strong>${title}</strong>
        <div class="device-meta">${desc}</div>
      </div>
      <button class="toggle ${settings[key] ? "on" : ""}" data-setting="${key}" aria-label="${title}"></button>
    </div>`).join("");

  document.querySelectorAll("[data-setting]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const key = btn.dataset.setting;
      settings = { ...settings, [key]: !settings[key] };
      renderSettings(uid);
      renderNotifications();
      await setDoc(
        doc(db, "users", uid, "settings", "notifications"),
        settings,
        { merge: true }
      );
    });
  });
}

// ── Device detail panel ────────────────────────────────────────────────────────
function openDetailPanel() {
  els.detailPanel.classList.add("open");
  els.detailOverlay.classList.add("visible");
}

function closeDetailPanel() {
  els.detailPanel.classList.remove("open");
  els.detailOverlay.classList.remove("visible");
  selectedDeviceId = null;
}

els.detailClose?.addEventListener("click", closeDetailPanel);
els.detailOverlay?.addEventListener("click", closeDetailPanel);

function renderDetailPanel(deviceId) {
  const device = devices.find((d) => d.id === deviceId);
  if (!device || !els.detailContent) return;

  const isOn = device.status === "ON";
  const isUnavailable = device.status === "ERROR" || device.status === "DISCONNECTED";

  let typeControls = "";

  if (device.type === "LIGHT") {
    typeControls = `
      <div class="detail-section">
        <label class="detail-label">Brightness: <strong>${device.brightness ?? 100}%</strong></label>
        <input type="range" min="0" max="100" value="${device.brightness ?? 100}"
          id="dp-brightness" class="detail-slider" />
      </div>
      <div class="detail-row">
        <div>
          <strong>Auto Schedule</strong>
          <div class="device-meta">Turn on/off automatically</div>
        </div>
        <button class="toggle ${device.scheduleEnabled ? "on" : ""}" id="dp-schedule-toggle"></button>
      </div>
      ${device.scheduleEnabled ? `
        <div class="detail-row">
          <span>Start</span>
          <input type="time" value="${device.scheduleStart ?? "18:00"}" id="dp-schedule-start" class="time-input" />
        </div>
        <div class="detail-row">
          <span>End</span>
          <input type="time" value="${device.scheduleEnd ?? "23:00"}" id="dp-schedule-end" class="time-input" />
        </div>` : ""}`;
  }

  if (device.type === "IRON") {
    const countdown = ironCountdownHtml(device);
    typeControls = `
      ${countdown ? `<div class="detail-section">${countdown}</div>` : ""}
      <div class="detail-section">
        <label class="detail-label">Max ON duration: <strong id="dp-iron-label">${device.maxOnDurationMinutes ?? 15} min</strong></label>
        <input type="range" min="5" max="60" value="${device.maxOnDurationMinutes ?? 15}"
          id="dp-iron-slider" class="detail-slider" />
      </div>
      <p class="detail-hint">Auto-shutdown fires when this limit is exceeded.</p>`;
  }

  if (device.type === "CAMERA") {
    typeControls = `
      <div class="camera-preview">
        <div class="camera-icon">📷</div>
        <div>${escapeHtml(device.name)} · Live Stream</div>
        <div class="live-badge">● LIVE</div>
      </div>`;
  }

  if (device.type === "MULTI_SWITCH") {
    const switchButtons = Object.entries(device.switches ?? {}).map(([name, on]) => `
      <div class="detail-row">
        <span>${escapeHtml(name)}</span>
        <button class="toggle ${on ? "on" : ""}" data-sub-switch="${escapeHtml(name)}"></button>
      </div>`).join("");
    typeControls = `<div class="detail-section">${switchButtons}</div>`;
  }

  els.detailContent.innerHTML = `
    <div class="detail-header-info">
      <div class="detail-icon">${deviceIcon(device.type)}</div>
      <div>
        <div class="detail-device-name">${escapeHtml(device.name)}</div>
        <div class="device-meta">
          ${escapeHtml(device.room)} ·
          <span class="badge status-${device.status.toLowerCase()}">${escapeHtml(device.status)}</span>
          · ${formatFloor(device.floorId)}
        </div>
      </div>
    </div>

    <div class="detail-row">
      <strong>Power</strong>
      <button class="toggle ${isOn ? "on" : ""}" id="dp-main-toggle" ${isUnavailable ? "disabled" : ""}></button>
    </div>

    ${typeControls}

    <div class="detail-section">
      <button class="delete-btn" id="dp-delete">Delete device</button>
    </div>
  `;

  // Wire up controls
  document.querySelector("#dp-main-toggle")?.addEventListener("click", () => {
    updateDevice(device, { status: device.status === "ON" ? "OFF" : "ON" });
  });

  document.querySelector("#dp-brightness")?.addEventListener("change", (e) => {
    updateDevice(device, { brightness: Number(e.target.value) });
  });
  document.querySelector("#dp-brightness")?.addEventListener("input", (e) => {
    // live label update
  });

  document.querySelector("#dp-schedule-toggle")?.addEventListener("click", () => {
    updateDevice(device, { scheduleEnabled: !device.scheduleEnabled });
  });

  document.querySelector("#dp-schedule-start")?.addEventListener("change", (e) => {
    updateDevice(device, { scheduleStart: e.target.value });
  });

  document.querySelector("#dp-schedule-end")?.addEventListener("change", (e) => {
    updateDevice(device, { scheduleEnd: e.target.value });
  });

  const ironSlider = document.querySelector("#dp-iron-slider");
  const ironLabel = document.querySelector("#dp-iron-label");
  ironSlider?.addEventListener("input", (e) => {
    if (ironLabel) ironLabel.textContent = `${e.target.value} min`;
  });
  ironSlider?.addEventListener("change", (e) => {
    updateDevice(device, { maxOnDurationMinutes: Number(e.target.value) });
  });

  document.querySelectorAll("[data-sub-switch]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const switchName = btn.dataset.subSwitch;
      const updated = { ...device.switches, [switchName]: !device.switches[switchName] };
      const anyOn = Object.values(updated).some(Boolean);
      updateDevice(device, { switches: updated, status: anyOn ? "ON" : "OFF" });
    });
  });

  document.querySelector("#dp-delete")?.addEventListener("click", async () => {
    if (!confirm(`Delete "${device.name}"?`)) return;
    await deleteDoc(doc(db, "devices", device.id));
    closeDetailPanel();
  });
}

// ── Add device form ────────────────────────────────────────────────────────────
els.addDeviceBtn?.addEventListener("click", () => {
  els.addDeviceForm?.classList.toggle("hidden");
});

document.querySelector("#addDeviceSubmit")?.addEventListener("click", async () => {
  const name = document.querySelector("#newDeviceName")?.value.trim();
  const room = document.querySelector("#newDeviceRoom")?.value.trim();
  const type = document.querySelector("#newDeviceType")?.value;
  const floorId = document.querySelector("#newDeviceFloor")?.value;
  const gridX = Number(document.querySelector("#newDeviceX")?.value ?? 0);
  const gridY = Number(document.querySelector("#newDeviceY")?.value ?? 0);

  if (!name || !room) { setMessage("Name and room required."); return; }

  const newDevice = {
    name, room, type, floorId,
    status: "OFF",
    gridX: Math.min(3, Math.max(0, gridX)),
    gridY: Math.min(3, Math.max(0, gridY)),
    brightness: 100,
    scheduleEnabled: false,
    scheduleStart: "18:00",
    scheduleEnd: "23:00",
    maxOnDurationMinutes: 15,
    switches: type === "MULTI_SWITCH"
      ? { "Switch 1": false, "Switch 2": false, "Switch 3": false }
      : {},
    usageMinutesThisWeek: 0,
    safetyShutdownsThisMonth: 0,
  };

  await addDoc(collection(db, "devices"), newDevice);
  els.addDeviceForm?.classList.add("hidden");
  setMessage(`${name} added.`);
});

// ── Core helpers ───────────────────────────────────────────────────────────────
async function updateDevice(device, changes, options = {}) {
  if (!device) return;
  const { notifyStatus = true } = options;

  const finalChanges = { ...changes };
  if ("status" in changes) {
    if (changes.status === "ON" && device.status !== "ON") {
      finalChanges.turnedOnAt = Date.now();
    } else if (changes.status !== "ON" && device.status === "ON") {
      finalChanges.turnedOnAt = deleteField();
    }
  }

  const { id: _id, ...deviceData } = device;
  await setDoc(doc(db, "devices", device.id), { ...deviceData, ...finalChanges }, { merge: true });

  if (notifyStatus && "status" in changes && changes.status !== device.status) {
    await pushNotification({
      title: `${device.name} status changed`,
      description: `${device.name} switched ${String(changes.status).toLowerCase()}.`,
      important: changes.status === "ERROR" || changes.status === "DISCONNECTED",
    }, `status_${device.id}_${Date.now()}`);
  }
}

async function pushNotification(data, id = null) {
  const time = new Date().toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" });
  const payload = { ...data, time, timestamp: Date.now(), read: false };
  if (id) {
    await setDoc(doc(db, "notifications", id), payload, { merge: true });
  } else {
    await addDoc(collection(db, "notifications"), payload);
  }
}

async function clearReadNotifications() {
  const snapshot = await getDocs(collection(db, "notifications"));
  await Promise.all(
    snapshot.docs
      .filter((item) => item.data()?.read === true)
      .map((item) => deleteDoc(item.ref))
  );
}

function notificationTimestampMs(notification) {
  const raw = notification?.timestamp;
  if (typeof raw === "number") return raw;
  if (raw && typeof raw.toMillis === "function") return raw.toMillis();
  return 0;
}

function notificationDisplayTime(notification) {
  if (notification?.time) return String(notification.time);
  const timestamp = notificationTimestampMs(notification);
  if (!timestamp) return "";
  return new Date(timestamp).toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" });
}

function parseClockMinutes(value) {
  if (typeof value !== "string") return null;
  const match = /^(\d{1,2}):(\d{2})$/.exec(value.trim());
  if (!match) return null;
  const hour = Number(match[1]);
  const minute = Number(match[2]);
  if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null;
  return hour * 60 + minute;
}

function isNowWithinSchedule(start, end) {
  const startMin = parseClockMinutes(start);
  const endMin = parseClockMinutes(end);
  if (startMin == null || endMin == null) return false;

  const now = new Date();
  const nowMin = now.getHours() * 60 + now.getMinutes();

  if (startMin === endMin) return true;
  if (startMin < endMin) return nowMin >= startMin && nowMin < endMin;
  return nowMin >= startMin || nowMin < endMin;
}

function clearListeners() {
  unsubscribeHandlers.forEach((u) => u());
  unsubscribeHandlers = [];
}

function setMessage(msg) { els.message.textContent = msg; }
function formatFloor(id) { return id === "ground" ? "Ground Floor" : "First Floor"; }
function formatMinutes(m) {
  const h = Math.floor(m / 60), r = m % 60;
  return h === 0 ? `${r}m` : `${h}h ${r}m`;
}
function escapeHtml(v) {
  return String(v ?? "")
    .replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;").replaceAll("'", "&#039;");
}
function deviceIcon(type) {
  return { LIGHT: "💡", OUTLET: "🔌", IRON: "🔥", CAMERA: "📷", MULTI_SWITCH: "🎚" }[type] ?? "📦";
}
function ironCountdownHtml(device) {
  if (device.type !== "IRON" || device.status !== "ON") return "";
  const t = Number(device.turnedOnAt ?? 0);
  if (t <= 0) return "";
  const remaining = Number(device.maxOnDurationMinutes ?? 15) - Math.floor((Date.now() - t) / 60_000);
  if (remaining <= 0) return `<span class="countdown danger">⚠ Auto-shutdown pending</span>`;
  return `<span class="${remaining <= 2 ? "countdown danger" : "countdown"}">⏱ ${remaining} min left</span>`;
}


