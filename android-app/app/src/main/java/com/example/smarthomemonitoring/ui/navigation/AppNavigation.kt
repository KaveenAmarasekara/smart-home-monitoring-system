package com.example.smarthomemonitoring.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smarthomemonitoring.data.mock.MockData
import com.example.smarthomemonitoring.data.model.Device
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
        mutableStateOf(
            MockData.groundFloorDevices
        )
    }

    var firstFloorDevices by remember {
        mutableStateOf(
            MockData.firstFloorDevices
        )
    }

    val updateDevice:
                (Device) -> Unit = { updatedDevice ->

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
    }

    NavHost(
        navController = navController,
        startDestination =
            AppRoute.Login.route
    ) {

        composable(
            AppRoute.Login.route
        ) {

            LoginScreen(
                onLoginSuccess = {

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
            )
        }

        composable(
            AppRoute.Home.route
        ) {

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
                }
            )
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

                onOpenDevice = {
                        device ->

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

                onOpenDevice = {
                        device ->

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
                onBack = {
                    navController
                        .popBackStack()
                }
            )
        }
    }
}