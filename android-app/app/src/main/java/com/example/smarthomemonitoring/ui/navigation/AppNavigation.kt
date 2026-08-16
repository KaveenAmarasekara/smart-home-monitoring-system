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
import com.example.smarthomemonitoring.data.model.UsageReport
import com.example.smarthomemonitoring.data.model.UserSettings
import com.example.smarthomemonitoring.ui.screens.device.DeviceDetailsScreen
import com.example.smarthomemonitoring.ui.screens.floor.FloorDashboardScreen
import com.example.smarthomemonitoring.ui.screens.home.HomeScreen
import com.example.smarthomemonitoring.ui.screens.login.LoginScreen
import com.example.smarthomemonitoring.ui.screens.notifications.NotificationsScreen
import com.example.smarthomemonitoring.ui.screens.reports.ReportsScreen
import com.example.smarthomemonitoring.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation() {

    val navController =
        rememberNavController()

    var groundDevices by remember {
        mutableStateOf<List<Device>>(emptyList())
    }

    var firstFloorDevices by remember {
        mutableStateOf<List<Device>>(emptyList())
    }

    var notifications by remember {
        mutableStateOf<List<AppNotification>>(emptyList())
    }

    var settings by remember {
        mutableStateOf(UserSettings())
    }

    var usageReport by remember {
        mutableStateOf(
            UsageReport(
                totalUsageLabel = "0m",
                mostUsedDevices = emptyList(),
                safetyShutdownsThisMonth = 0
            )
        )
    }

    var backendError by remember {
        mutableStateOf<String?>(null)
    }

    var isSignedIn by remember {
        mutableStateOf(FirebaseSmartHomeRepository.isSignedIn)
    }

    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            FirebaseSmartHomeRepository.seedDefaultDataIfNeeded {
                backendError = it.message
            }
        }
    }

    DisposableEffect(isSignedIn) {
        if (!isSignedIn) {
            onDispose {
            }
        } else {
            val groundListener =
                FirebaseSmartHomeRepository.observeDevices(
                    floorId = GROUND_FLOOR_ID,
                    onDevicesChanged = {
                        groundDevices = it
                    },
                    onError = {
                        backendError = it.message
                    }
                )

            val firstFloorListener =
                FirebaseSmartHomeRepository.observeDevices(
                    floorId = FIRST_FLOOR_ID,
                    onDevicesChanged = {
                        firstFloorDevices = it
                    },
                    onError = {
                        backendError = it.message
                    }
                )

            val notificationsListener =
                FirebaseSmartHomeRepository.observeNotifications(
                    onNotificationsChanged = {
                        notifications = it
                    },
                    onError = {
                        backendError = it.message
                    }
                )

            val usageReportListener =
                FirebaseSmartHomeRepository.observeUsageReport(
                    onReportChanged = {
                        usageReport = it
                    },
                    onError = {
                        backendError = it.message
                    }
                )

            val settingsListener =
                FirebaseSmartHomeRepository.observeUserSettings(
                    onSettingsChanged = {
                        settings = it
                    },
                    onError = {
                        backendError = it.message
                    }
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

    val updateDevice:
        (Device) -> Unit = { updatedDevice ->

            val floorId =
                if (groundDevices.any { it.id == updatedDevice.id }) {
                    GROUND_FLOOR_ID
                } else {
                    FIRST_FLOOR_ID
                }

            groundDevices =
                groundDevices.map {
                    if (it.id == updatedDevice.id) {
                        updatedDevice
                    } else {
                        it
                    }
                }

            firstFloorDevices =
                firstFloorDevices.map {
                    if (it.id == updatedDevice.id) {
                        updatedDevice
                    } else {
                        it
                    }
                }

            FirebaseSmartHomeRepository.updateDevice(
                device = updatedDevice,
                floorId = floorId,
                onError = {
                    backendError = it.message
                }
            )
        }

    NavHost(
        navController = navController,
        startDestination =
            if (isSignedIn) {
                AppRoute.Home.route
            } else {
                AppRoute.Login.route
            }
    ) {

        composable(
            AppRoute.Login.route
        ) {

            LoginScreen(
                onLogin = { email, password, onResult ->
                    FirebaseSmartHomeRepository.signInOrCreateAccount(
                        email = email,
                        password = password
                    ) { result ->
                        onResult(result)

                        if (result.isSuccess) {
                            isSignedIn = true

                            FirebaseSmartHomeRepository.seedDefaultDataIfNeeded {
                                backendError = it.message
                            }

                            navController.navigate(
                                AppRoute.Home.route
                            ) {
                                popUpTo(
                                    AppRoute.Login.route
                                ) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
            )
        }

        composable(
            AppRoute.Home.route
        ) {

            Column {
                backendError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                HomeScreen(

                    onOpenGroundFloor = {
                        navController.navigate(
                            AppRoute.GroundFloor.route
                        )
                    },

                    onOpenFirstFloor = {
                        navController.navigate(
                            AppRoute.FirstFloor.route
                        )
                    },

                    onOpenReports = {
                        navController.navigate(
                            AppRoute.Reports.route
                        )
                    },

                    onOpenNotifications = {
                        navController.navigate(
                            AppRoute.Notifications.route
                        )
                    },

                    onOpenSettings = {
                        navController.navigate(
                            AppRoute.Settings.route
                        )
                    },

                    groundFloorTotal = groundDevices.size,
                    groundFloorOnline = groundDevices.count {
                        it.status == com.example.smarthomemonitoring.data.model.DeviceStatus.ON
                    },
                    firstFloorTotal = firstFloorDevices.size,
                    firstFloorOnline = firstFloorDevices.count {
                        it.status == com.example.smarthomemonitoring.data.model.DeviceStatus.ON
                    },
                    unreadAlerts = notifications.count { it.important }
                )
            }
        }

        composable(
            AppRoute.GroundFloor.route
        ) {

            FloorDashboardScreen(
                floorName =
                    "Ground Floor",

                devices =
                    groundDevices,

                onUpdateDevice =
                    updateDevice,

                onOpenDevice = { device ->

                    navController.navigate(
                        AppRoute.DeviceDetails
                            .createRoute(
                                device.id
                            )
                    )
                },

                onBack = {
                    navController
                        .popBackStack()
                }
            )
        }

        composable(
            AppRoute.FirstFloor.route
        ) {

            FloorDashboardScreen(
                floorName =
                    "First Floor",

                devices =
                    firstFloorDevices,

                onUpdateDevice =
                    updateDevice,

                onOpenDevice = { device ->

                    navController.navigate(
                        AppRoute.DeviceDetails
                            .createRoute(
                                device.id
                            )
                    )
                },

                onBack = {
                    navController
                        .popBackStack()
                }
            )
        }

        composable(
            route =
                AppRoute.DeviceDetails.route,

            arguments =
                listOf(
                    navArgument(
                        "deviceId"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { backStackEntry ->

            val deviceId =
                backStackEntry
                    .arguments
                    ?.getString(
                        "deviceId"
                    )

            val device =
                (
                    groundDevices +
                        firstFloorDevices
                    ).firstOrNull {
                    it.id == deviceId
                }

            if (device != null) {

                DeviceDetailsScreen(
                    device = device,

                    onUpdateDevice =
                        updateDevice,

                    onBack = {
                        navController
                            .popBackStack()
                    }
                )

            } else {

                Text(
                    text =
                        "Device not found"
                )
            }
        }

        composable(
            AppRoute.Reports.route
        ) {

            ReportsScreen(
                report = usageReport,
                onBack = {
                    navController
                        .popBackStack()
                }
            )
        }

        composable(
            AppRoute.Notifications.route
        ) {

            NotificationsScreen(
                notifications =
                    notifications.filter { notification ->
                        val title =
                            notification.title.lowercase()

                        when {
                            title.contains("switched off") ->
                                settings.safetyAlerts

                            title.contains("disconnected") ->
                                settings.deviceAlerts

                            title.contains("schedule") ->
                                settings.scheduleAlerts

                            else ->
                                true
                        }
                    },
                onBack = {
                    navController
                        .popBackStack()
                }
            )
        }

        composable(
            AppRoute.Settings.route
        ) {

            SettingsScreen(
                settings = settings,
                onSettingsChanged = {
                    settings = it
                    FirebaseSmartHomeRepository.updateUserSettings(
                        settings = it,
                        onError = { error ->
                            backendError = error.message
                        }
                    )
                },
                onBack = {
                    navController
                        .popBackStack()
                }
            )
        }
    }
}
