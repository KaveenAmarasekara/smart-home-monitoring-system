import { initializeApp } from "https://www.gstatic.com/firebasejs/12.17.1/firebase-app.js";
import {
  createUserWithEmailAndPassword,
  getAuth,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signOut,
} from "https://www.gstatic.com/firebasejs/12.17.1/firebase-auth.js";
import {
  collection,
  doc,
  getDocs,
  getFirestore,
  limit,
  onSnapshot,
  query,
  setDoc,
  where,
  writeBatch,
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
  seedButton: document.querySelector("#seedButton"),
  sessionLabel: document.querySelector("#sessionLabel"),
  settingsList: document.querySelector("#settingsList"),
  shutdownCount: document.querySelector("#shutdownCount"),
  signOutButton: document.querySelector("#signOutButton"),
  totalDevices: document.querySelector("#totalDevices"),
  weeklyUsage: document.querySelector("#weeklyUsage"),
};

let devices = [];
let notifications = [];
let settings = {
  safetyAlerts: true,
  deviceAlerts: true,
  scheduleAlerts: false,
};
let unsubscribeHandlers = [];

els.loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  setMessage("Connecting...");

  const email = els.emailInput.value.trim();
  const password = els.passwordInput.value;

  try {
    await signInWithEmailAndPassword(auth, email, password);
  } catch (error) {
    if (
      error.code === "auth/user-not-found" ||
      error.code === "auth/invalid-credential"
    ) {
      await createUserWithEmailAndPassword(auth, email, password);
      return;
    }

    setMessage(error.message);
  }
});

els.signOutButton.addEventListener("click", () => {
  signOut(auth);
});

els.seedButton.addEventListener("click", async () => {
  await seedDemoData();
  setMessage("Demo data seeded.");
});

onAuthStateChanged(auth, async (user) => {
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

  await seedDemoData();
  listenForDevices();
  listenForNotifications();
  listenForSettings(user.uid);
});

function listenForDevices() {
  const unsubscribe = onSnapshot(
    collection(db, "devices"),
    (snapshot) => {
      devices = snapshot.docs
        .map((documentSnapshot) => ({
          id: documentSnapshot.id,
          ...documentSnapshot.data(),
        }))
        .sort((a, b) => {
          return `${a.floorId}-${a.gridY}-${a.gridX}`.localeCompare(
            `${b.floorId}-${b.gridY}-${b.gridX}`,
          );
        });

      renderDevices();
      renderMetrics();
    },
    (error) => setMessage(error.message),
  );

  unsubscribeHandlers.push(unsubscribe);
}

function listenForNotifications() {
  const unsubscribe = onSnapshot(
    collection(db, "notifications"),
    (snapshot) => {
      notifications = snapshot.docs.map((documentSnapshot) => ({
        id: documentSnapshot.id,
        ...documentSnapshot.data(),
      }));
      renderNotifications();
    },
    (error) => setMessage(error.message),
  );

  unsubscribeHandlers.push(unsubscribe);
}

function listenForSettings(uid) {
  const settingsRef = doc(db, "users", uid, "settings", "notifications");

  const unsubscribe = onSnapshot(
    settingsRef,
    (snapshot) => {
      settings = {
        ...settings,
        ...snapshot.data(),
      };
      renderSettings(uid);
      renderNotifications();
    },
    (error) => setMessage(error.message),
  );

  unsubscribeHandlers.push(unsubscribe);
}

function renderDevices() {
  if (devices.length === 0) {
    els.deviceList.innerHTML = `<p>No devices found. Click "Seed demo data".</p>`;
    return;
  }

  els.deviceList.innerHTML = devices
    .map((device) => {
      const isOn = device.status === "ON";
      const isUnavailable =
        device.status === "ERROR" || device.status === "DISCONNECTED";

      return `
        <div class="device-row">
          <div>
            <div class="device-name">${escapeHtml(device.name)}</div>
            <div class="device-meta">
              <span>${formatFloor(device.floorId)}</span>
              <span>${escapeHtml(device.room)}</span>
              <span class="badge">${escapeHtml(device.type)}</span>
              <span class="badge">${escapeHtml(device.status)}</span>
            </div>
          </div>
          <div class="device-actions">
            ${
              device.type === "LIGHT"
                ? `<input
                    type="range"
                    min="0"
                    max="100"
                    value="${device.brightness ?? 100}"
                    data-brightness="${device.id}"
                    aria-label="${escapeHtml(device.name)} brightness"
                  />`
                : ""
            }
            <button
              class="toggle ${isOn ? "on" : ""}"
              data-toggle="${device.id}"
              ${isUnavailable ? "disabled" : ""}
              aria-label="Toggle ${escapeHtml(device.name)}"
            ></button>
          </div>
        </div>
      `;
    })
    .join("");

  document.querySelectorAll("[data-toggle]").forEach((button) => {
    button.addEventListener("click", () => {
      const device = devices.find((item) => item.id === button.dataset.toggle);
      updateDevice(device, {
        status: device.status === "ON" ? "OFF" : "ON",
      });
    });
  });

  document.querySelectorAll("[data-brightness]").forEach((slider) => {
    slider.addEventListener("change", () => {
      const device = devices.find((item) => item.id === slider.dataset.brightness);
      updateDevice(device, {
        brightness: Number(slider.value),
      });
    });
  });
}

function renderMetrics() {
  const onlineCount = devices.filter((device) => device.status === "ON").length;
  const totalMinutes = devices.reduce(
    (sum, device) => sum + Number(device.usageMinutesThisWeek ?? 0),
    0,
  );
  const shutdowns = devices.reduce(
    (sum, device) => sum + Number(device.safetyShutdownsThisMonth ?? 0),
    0,
  );

  els.totalDevices.textContent = devices.length;
  els.onlineDevices.textContent = onlineCount;
  els.weeklyUsage.textContent = formatMinutes(totalMinutes);
  els.shutdownCount.textContent = shutdowns;
}

function renderNotifications() {
  const visibleNotifications = notifications.filter((notification) => {
    const title = String(notification.title ?? "").toLowerCase();

    if (title.includes("switched off")) {
      return settings.safetyAlerts;
    }

    if (title.includes("disconnected")) {
      return settings.deviceAlerts;
    }

    if (title.includes("schedule")) {
      return settings.scheduleAlerts;
    }

    return true;
  });

  els.notificationList.innerHTML =
    visibleNotifications.length === 0
      ? `<p>No visible notifications.</p>`
      : visibleNotifications
          .map(
            (notification) => `
              <div class="notification-row ${notification.important ? "important" : ""}">
                <strong>${escapeHtml(notification.title)}</strong>
                <p>${escapeHtml(notification.description)}</p>
                <span>${escapeHtml(notification.time)}</span>
              </div>
            `,
          )
          .join("");
}

function renderSettings(uid) {
  const rows = [
    ["safetyAlerts", "Safety Alerts", "Automatic shutdown notifications"],
    ["deviceAlerts", "Device Alerts", "Errors and disconnected devices"],
    ["scheduleAlerts", "Schedule Alerts", "Scheduled light activity"],
  ];

  els.settingsList.innerHTML = rows
    .map(
      ([key, title, description]) => `
        <div class="setting-row">
          <div>
            <strong>${title}</strong>
            <div class="device-meta">${description}</div>
          </div>
          <button
            class="toggle ${settings[key] ? "on" : ""}"
            data-setting="${key}"
            aria-label="Toggle ${title}"
          ></button>
        </div>
      `,
    )
    .join("");

  document.querySelectorAll("[data-setting]").forEach((button) => {
    button.addEventListener("click", async () => {
      const key = button.dataset.setting;
      const nextSettings = {
        ...settings,
        [key]: !settings[key],
      };

      settings = nextSettings;
      renderSettings(uid);
      renderNotifications();

      await setDoc(
        doc(db, "users", uid, "settings", "notifications"),
        nextSettings,
        { merge: true },
      );
    });
  });
}

async function updateDevice(device, changes) {
  if (!device) {
    return;
  }

  await setDoc(
    doc(db, "devices", device.id),
    {
      ...device,
      ...changes,
    },
    { merge: true },
  );
}

async function seedDemoData() {
  const existingDevices = await getDocs(query(collection(db, "devices"), limit(1)));

  if (existingDevices.empty) {
    const batch = writeBatch(db);

    demoDevices.forEach((device) => {
      batch.set(doc(db, "devices", device.id), withoutId(device));
    });

    await batch.commit();
  }

  const existingNotifications = await getDocs(
    query(collection(db, "notifications"), limit(1)),
  );

  if (existingNotifications.empty) {
    const batch = writeBatch(db);

    demoNotifications.forEach((notification) => {
      batch.set(doc(db, "notifications", notification.id), withoutId(notification));
    });

    await batch.commit();
  }

  const user = auth.currentUser;

  if (user) {
    await setDoc(
      doc(db, "users", user.uid, "settings", "notifications"),
      settings,
      { merge: true },
    );
  }
}

function clearListeners() {
  unsubscribeHandlers.forEach((unsubscribe) => unsubscribe());
  unsubscribeHandlers = [];
}

function setMessage(message) {
  els.message.textContent = message;
}

function formatFloor(floorId) {
  return floorId === "ground" ? "Ground Floor" : "First Floor";
}

function formatMinutes(minutes) {
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;

  return hours === 0 ? `${remainingMinutes}m` : `${hours}h ${remainingMinutes}m`;
}

function withoutId(item) {
  const { id, ...data } = item;
  return data;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

const demoDevices = [
  {
    id: "device1",
    floorId: "ground",
    name: "Living Room Light",
    type: "LIGHT",
    status: "ON",
    room: "Living Room",
    gridX: 0,
    gridY: 0,
    brightness: 75,
    scheduleEnabled: true,
    scheduleStart: "18:00",
    scheduleEnd: "23:00",
    maxOnDurationMinutes: 15,
    switches: {},
    usageMinutesThisWeek: 480,
    safetyShutdownsThisMonth: 0,
  },
  {
    id: "device2",
    floorId: "ground",
    name: "TV Outlet",
    type: "OUTLET",
    status: "OFF",
    room: "Living Room",
    gridX: 1,
    gridY: 0,
    brightness: 100,
    scheduleEnabled: false,
    scheduleStart: "18:00",
    scheduleEnd: "23:00",
    maxOnDurationMinutes: 15,
    switches: {},
    usageMinutesThisWeek: 75,
    safetyShutdownsThisMonth: 0,
  },
  {
    id: "device3",
    floorId: "ground",
    name: "Clothing Iron",
    type: "IRON",
    status: "OFF",
    room: "Laundry Room",
    gridX: 2,
    gridY: 1,
    brightness: 100,
    scheduleEnabled: false,
    scheduleStart: "18:00",
    scheduleEnd: "23:00",
    maxOnDurationMinutes: 15,
    switches: {},
    usageMinutesThisWeek: 75,
    safetyShutdownsThisMonth: 2,
  },
  {
    id: "device4",
    floorId: "ground",
    name: "Front Camera",
    type: "CAMERA",
    status: "ON",
    room: "Entrance",
    gridX: 0,
    gridY: 2,
    brightness: 100,
    scheduleEnabled: false,
    scheduleStart: "18:00",
    scheduleEnd: "23:00",
    maxOnDurationMinutes: 15,
    switches: {},
    usageMinutesThisWeek: 300,
    safetyShutdownsThisMonth: 0,
  },
  {
    id: "device5",
    floorId: "ground",
    name: "Kitchen Switch Unit",
    type: "MULTI_SWITCH",
    status: "OFF",
    room: "Kitchen",
    gridX: 3,
    gridY: 2,
    brightness: 100,
    scheduleEnabled: false,
    scheduleStart: "18:00",
    scheduleEnd: "23:00",
    maxOnDurationMinutes: 15,
    switches: {
      "Main Light": false,
      "Counter Light": false,
      "Dining Light": false,
    },
    usageMinutesThisWeek: 180,
    safetyShutdownsThisMonth: 0,
  },
  {
    id: "device6",
    floorId: "ground",
    name: "Garage Outlet",
    type: "OUTLET",
    status: "DISCONNECTED",
    room: "Garage",
    gridX: 3,
    gridY: 3,
    brightness: 100,
    scheduleEnabled: false,
    scheduleStart: "18:00",
    scheduleEnd: "23:00",
    maxOnDurationMinutes: 15,
    switches: {},
    usageMinutesThisWeek: 75,
    safetyShutdownsThisMonth: 0,
  },
  {
    id: "device7",
    floorId: "first",
    name: "Bedroom Light",
    type: "LIGHT",
    status: "OFF",
    room: "Master Bedroom",
    gridX: 0,
    gridY: 0,
    brightness: 60,
    scheduleEnabled: false,
    scheduleStart: "18:00",
    scheduleEnd: "23:00",
    maxOnDurationMinutes: 15,
    switches: {},
    usageMinutesThisWeek: 145,
    safetyShutdownsThisMonth: 0,
  },
  {
    id: "device8",
    floorId: "first",
    name: "Bedroom Outlet",
    type: "OUTLET",
    status: "ON",
    room: "Master Bedroom",
    gridX: 1,
    gridY: 0,
    brightness: 100,
    scheduleEnabled: false,
    scheduleStart: "18:00",
    scheduleEnd: "23:00",
    maxOnDurationMinutes: 15,
    switches: {},
    usageMinutesThisWeek: 360,
    safetyShutdownsThisMonth: 0,
  },
  {
    id: "device9",
    floorId: "first",
    name: "Hallway Camera",
    type: "CAMERA",
    status: "ON",
    room: "Hallway",
    gridX: 1,
    gridY: 1,
    brightness: 100,
    scheduleEnabled: false,
    scheduleStart: "18:00",
    scheduleEnd: "23:00",
    maxOnDurationMinutes: 15,
    switches: {},
    usageMinutesThisWeek: 75,
    safetyShutdownsThisMonth: 0,
  },
  {
    id: "device10",
    floorId: "first",
    name: "Study Light",
    type: "LIGHT",
    status: "ERROR",
    room: "Study Room",
    gridX: 2,
    gridY: 1,
    brightness: 100,
    scheduleEnabled: false,
    scheduleStart: "18:00",
    scheduleEnd: "23:00",
    maxOnDurationMinutes: 15,
    switches: {},
    usageMinutesThisWeek: 75,
    safetyShutdownsThisMonth: 0,
  },
];

const demoNotifications = [
  {
    id: "notification1",
    title: "Iron automatically switched off",
    description: "Maximum active duration of 15 minutes was reached.",
    time: "10:35 AM",
    important: true,
  },
  {
    id: "notification2",
    title: "Garage Outlet disconnected",
    description: "The device is currently unreachable.",
    time: "9:20 AM",
    important: true,
  },
  {
    id: "notification3",
    title: "Living Room schedule",
    description: "Light switched on automatically.",
    time: "Yesterday",
    important: false,
  },
];
