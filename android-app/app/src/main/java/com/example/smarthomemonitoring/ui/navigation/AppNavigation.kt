package com.example.smarthomemonitoring.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smarthomemonitoring.data.firebase.FirebaseSmartHomeRepository
import com.example.smarthomemonitoring.data.firebase.FirebaseSmartHomeRepository.FIRST_FLOOR_ID
import com.example.smarthomemonitoring.data.firebase.FirebaseSmartHomeRepository.GROUND_FLOOR_ID
import com.example.smarthomemonitoring.data.model.AppNotification
import com.example.smarthomemonitoring.data.model.Device
import com.example.smarthomemonitoring.data.model.DeviceStatus
import com.example.smarthomemonitoring.data.model.DeviceType
import com.example.smarthomemonitoring.data.model.UsageReport
import com.example.smarthomemonitoring.data.model.UserSettings
import com.example.smarthomemonitoring.ui.screens.device.DeviceDetailsScreen
import com.example.smarthomemonitoring.ui.screens.floor.FloorDashboardScreen
import com.example.smarthomemonitoring.ui.screens.home.HomeScreen
import com.example.smarthomemonitoring.ui.screens.login.LoginScreen
import com.example.smarthomemonitoring.ui.screens.notifications.NotificationsScreen
import com.example.smarthomemonitoring.ui.screens.reports.ReportsScreen
import com.example.smarthomemonitoring.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    var groundDevices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var firstFloorDevices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var settings by remember { mutableStateOf(UserSettings()) }
    var usageReport by remember {
        mutableStateOf(UsageReport(totalUsageLabel = "0m", mostUsedDevices = emptyList(), safetyShutdownsThisMonth = 0))
    }
    var backendError by remember { mutableStateOf<String?>(null) }
    var isSignedIn by remember { mutableStateOf(FirebaseSmartHomeRepository.isSignedIn) }
    val currentGroundDevices by rememberUpdatedState(groundDevices)
    val currentFirstFloorDevices by rememberUpdatedState(firstFloorDevices)

    fun notificationTime(timestamp: Long): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    fun writeDevice(device: Device, floorId: String) {
        groundDevices = groundDevices.map { if (it.id == device.id) device else it }
        firstFloorDevices = firstFloorDevices.map { if (it.id == device.id) device else it }
        FirebaseSmartHomeRepository.updateDevice(device, floorId) { backendError = it.message }
    }

    fun addNotification(
        id: String,
        title: String,
        description: String,
        important: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ) {
        FirebaseSmartHomeRepository.addNotification(
            AppNotification(
                id = id,
                title = title,
                description = description,
                time = notificationTime(timestamp),
                important = important,
                timestamp = timestamp
            )
        ) { backendError = it.message }
    }

    fun applyDeviceAutomation(
        sourceGroundDevices: List<Device>,
        sourceFirstFloorDevices: List<Device>
    ) {
        val all = sourceGroundDevices + sourceFirstFloorDevices
        val now = System.currentTimeMillis()
        val eventMinute = now / 60_000L

        all.filter {
            it.type == DeviceType.LIGHT &&
                it.scheduleEnabled &&
                it.status != DeviceStatus.ERROR &&
                it.status != DeviceStatus.DISCONNECTED
        }.forEach { light ->
            val shouldBeOn = isNowWithinSchedule(light.scheduleStart, light.scheduleEnd)
            val targetStatus = if (shouldBeOn) DeviceStatus.ON else DeviceStatus.OFF

            if (light.status != targetStatus) {
                val updated = if (targetStatus == DeviceStatus.ON) {
                    light.copy(status = targetStatus, turnedOnAt = now)
                } else {
                    light.copy(status = targetStatus, turnedOnAt = 0L)
                }
                val floorId = if (sourceGroundDevices.any { it.id == light.id }) {
                    GROUND_FLOOR_ID
                } else {
                    FIRST_FLOOR_ID
                }

                writeDevice(updated, floorId)
                addNotification(
                    id = "schedule_${light.id}_${targetStatus.name}_$eventMinute",
                    title = "${light.name} schedule",
                    description = "Light switched ${if (shouldBeOn) "on" else "off"} automatically.",
                    important = false,
                    timestamp = now
                )
            }
        }

        all.filter {
            it.type == DeviceType.IRON &&
                it.status == DeviceStatus.ON &&
                it.turnedOnAt > 0L
        }.forEach { iron ->
            val elapsed = now - iron.turnedOnAt
            if (elapsed >= iron.maxOnDurationMinutes * 60_000L) {
                val offDevice = iron.copy(status = DeviceStatus.OFF, turnedOnAt = 0L)
                val floorId = if (sourceGroundDevices.any { it.id == iron.id }) {
                    GROUND_FLOOR_ID
                } else {
                    FIRST_FLOOR_ID
                }

                writeDevice(offDevice, floorId)
                addNotification(
                    id = "auto_${iron.id}_${iron.turnedOnAt}_${iron.maxOnDurationMinutes}",
                    title = "${iron.name} automatically switched off",
                    description = "Maximum ON duration of ${iron.maxOnDurationMinutes} minutes reached.",
                    important = true,
                    timestamp = now
                )
            }
        }
    }

    val updateDevice: (Device) -> Unit = { updatedDevice ->
        val floorId = if (groundDevices.any { it.id == updatedDevice.id }) GROUND_FLOOR_ID else FIRST_FLOOR_ID
        val currentDevice = (groundDevices + firstFloorDevices).firstOrNull { it.id == updatedDevice.id }

        val deviceToWrite = when {
            updatedDevice.status == DeviceStatus.ON && currentDevice?.status != DeviceStatus.ON ->
                updatedDevice.copy(turnedOnAt = System.currentTimeMillis())
            updatedDevice.status != DeviceStatus.ON && currentDevice?.status == DeviceStatus.ON ->
                updatedDevice.copy(turnedOnAt = 0L)
            else -> updatedDevice
        }

        writeDevice(deviceToWrite, floorId)

        if (currentDevice != null && currentDevice.status != deviceToWrite.status) {
            val now = System.currentTimeMillis()
            addNotification(
                id = "status_${deviceToWrite.id}_$now",
                title = "${deviceToWrite.name} status changed",
                description = "${deviceToWrite.name} switched ${deviceToWrite.status.name.lowercase()}.",
                important = deviceToWrite.status == DeviceStatus.ERROR ||
                    deviceToWrite.status == DeviceStatus.DISCONNECTED,
                timestamp = now
            )
        }
    }

    // Device automation worker: schedule events + iron auto-shutdown.
    LaunchedEffect(isSignedIn, groundDevices, firstFloorDevices) {
        if (isSignedIn) {
            applyDeviceAutomation(groundDevices, firstFloorDevices)
        }
    }

    LaunchedEffect(isSignedIn) {
        while (true) {
            delay(20_000)
            if (isSignedIn) {
                applyDeviceAutomation(currentGroundDevices, currentFirstFloorDevices)
            }
        }
    }

    DisposableEffect(isSignedIn) {
        if (!isSignedIn) {
            onDispose { }
        } else {
            FirebaseSmartHomeRepository.seedDefaultDataIfNeeded { backendError = it.message }

            val groundListener = FirebaseSmartHomeRepository.observeDevices(
                floorId = GROUND_FLOOR_ID,
                onDevicesChanged = { groundDevices = it },
                onError = { backendError = it.message }
            )
            val firstFloorListener = FirebaseSmartHomeRepository.observeDevices(
                floorId = FIRST_FLOOR_ID,
                onDevicesChanged = { firstFloorDevices = it },
                onError = { backendError = it.message }
            )
            val notificationsListener = FirebaseSmartHomeRepository.observeNotifications(
                onNotificationsChanged = { notifications = it },
                onError = { backendError = it.message }
            )
            val usageReportListener = FirebaseSmartHomeRepository.observeUsageReport(
                onReportChanged = { usageReport = it },
                onError = { backendError = it.message }
            )
            val settingsListener = FirebaseSmartHomeRepository.observeUserSettings(
                onSettingsChanged = { settings = it },
                onError = { backendError = it.message }
            )

            onDispose {
                groundListener.remove()
                firstFloorListener.remove()
                notificationsListener.remove()
                usageReportListener.remove()
                settingsListener?.remove()
            }
        }
    }

    val addDevice: (Device, String) -> Unit = { device, floorId ->
        FirebaseSmartHomeRepository.addDevice(
            device = device,
            floorId = floorId,
            onSuccess = { /* snapshot listener updates state */ },
            onError = { backendError = it.message }
        )
    }

    val deleteDevice: (String) -> Unit = { deviceId ->
        groundDevices = groundDevices.filter { it.id != deviceId }
        firstFloorDevices = firstFloorDevices.filter { it.id != deviceId }
        FirebaseSmartHomeRepository.deleteDevice(deviceId, {}, { backendError = it.message })
    }

    NavHost(
        navController = navController,
        startDestination = if (isSignedIn) AppRoute.Home.route else AppRoute.Login.route
    ) {

        composable(AppRoute.Login.route) {
            LoginScreen(
                onLogin = { email, password, onResult ->
                    FirebaseSmartHomeRepository.signInOrCreateAccount(email, password) { result ->
                        onResult(result)
                        if (result.isSuccess) {
                            isSignedIn = true
                            navController.navigate(AppRoute.Home.route) {
                                popUpTo(AppRoute.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(AppRoute.Home.route) {
            Column {
                backendError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                HomeScreen(
                    onOpenGroundFloor = { navController.navigate(AppRoute.GroundFloor.route) },
                    onOpenFirstFloor = { navController.navigate(AppRoute.FirstFloor.route) },
                    onOpenReports = { navController.navigate(AppRoute.Reports.route) },
                    onOpenNotifications = { navController.navigate(AppRoute.Notifications.route) },
                    onOpenSettings = { navController.navigate(AppRoute.Settings.route) },
                    onSignOut = {
                        FirebaseSmartHomeRepository.signOut()
                        isSignedIn = false
                        groundDevices = emptyList()
                        firstFloorDevices = emptyList()
                        notifications = emptyList()
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(AppRoute.Home.route) { inclusive = true }
                        }
                    },
                    groundFloorTotal = groundDevices.size,
                    groundFloorOnline = groundDevices.count { it.status == DeviceStatus.ON },
                    firstFloorTotal = firstFloorDevices.size,
                    firstFloorOnline = firstFloorDevices.count { it.status == DeviceStatus.ON },
                    unreadAlerts = notifications.count { it.important && !it.read },
                    recentNotifications = notifications
                        .filter { it.important && !it.read }
                        .take(3)
                )
            }
        }

        composable(AppRoute.GroundFloor.route) {
            FloorDashboardScreen(
                floorName = "Ground Floor",
                floorId = GROUND_FLOOR_ID,
                devices = groundDevices,
                onUpdateDevice = updateDevice,
                onAddDevice = { device -> addDevice(device, GROUND_FLOOR_ID) },
                onOpenDevice = { device ->
                    navController.navigate(AppRoute.DeviceDetails.createRoute(device.id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.FirstFloor.route) {
            FloorDashboardScreen(
                floorName = "First Floor",
                floorId = FIRST_FLOOR_ID,
                devices = firstFloorDevices,
                onUpdateDevice = updateDevice,
                onAddDevice = { device -> addDevice(device, FIRST_FLOOR_ID) },
                onOpenDevice = { device ->
                    navController.navigate(AppRoute.DeviceDetails.createRoute(device.id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.DeviceDetails.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId")
            val device = (groundDevices + firstFloorDevices).firstOrNull { it.id == deviceId }

            if (device != null) {
                DeviceDetailsScreen(
                    device = device,
                    onUpdateDevice = updateDevice,
                    onDeleteDevice = {
                        deleteDevice(device.id)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                Text(text = "Device not found")
            }
        }

        composable(AppRoute.Reports.route) {
            ReportsScreen(
                report = usageReport,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Notifications.route) {
            NotificationsScreen(
                notifications = notifications.filter { notification ->
                    val title = notification.title.lowercase()
                    when {
                        title.contains("switched off") -> settings.safetyAlerts
                        title.contains("disconnected") -> settings.deviceAlerts
                        title.contains("schedule") -> settings.scheduleAlerts
                        else -> true
                    }
                },
                onMarkRead = { notification ->
                    FirebaseSmartHomeRepository.markNotificationRead(notification.id) {
                        backendError = it.message
                    }
                },
                onClear = { notification ->
                    FirebaseSmartHomeRepository.clearNotification(notification.id) {
                        backendError = it.message
                    }
                },
                onClearRead = {
                    FirebaseSmartHomeRepository.clearReadNotifications {
                        backendError = it.message
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Settings.route) {
            SettingsScreen(
                settings = settings,
                onSettingsChanged = {
                    settings = it
                    FirebaseSmartHomeRepository.updateUserSettings(it) { error ->
                        backendError = error.message
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun parseClockMinutes(value: String): Int? {
    val parts = value.trim().split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}

private fun isNowWithinSchedule(start: String, end: String): Boolean {
    val startMin = parseClockMinutes(start) ?: return false
    val endMin = parseClockMinutes(end) ?: return false

    val now = java.util.Calendar.getInstance()
    val nowMin = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)

    if (startMin == endMin) return true
    if (startMin < endMin) return nowMin in startMin until endMin
    return nowMin >= startMin || nowMin < endMin
}
