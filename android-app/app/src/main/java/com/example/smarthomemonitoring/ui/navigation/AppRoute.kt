package com.example.smarthomemonitoring.ui.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Home : AppRoute("home")
    data object GroundFloor : AppRoute("ground_floor")
    data object FirstFloor : AppRoute("first_floor")
}