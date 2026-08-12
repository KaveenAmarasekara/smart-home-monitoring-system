package com.example.smarthomemonitoring.ui.navigation

sealed class AppRoute(
    val route: String
) {

    data object Login :
        AppRoute("login")

    data object Home :
        AppRoute("home")

    data object GroundFloor :
        AppRoute("ground_floor")

    data object FirstFloor :
        AppRoute("first_floor")

    data object Reports :
        AppRoute("reports")

    data object Notifications :
        AppRoute("notifications")

    data object Settings :
        AppRoute("settings")

    data object DeviceDetails :
        AppRoute("device_details/{deviceId}") {

        fun createRoute(
            deviceId: String
        ): String {
            return "device_details/$deviceId"
        }
    }
}