package com.example.smarthomemonitoring.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smarthomemonitoring.ui.screens.floor.FloorDashboardScreen
import com.example.smarthomemonitoring.ui.screens.home.HomeScreen
import com.example.smarthomemonitoring.ui.screens.login.LoginScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Login.route
    ) {

        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(AppRoute.Home.route) {
            HomeScreen(
                onOpenGroundFloor = {
                    navController.navigate(AppRoute.GroundFloor.route)
                },
                onOpenFirstFloor = {
                    navController.navigate(AppRoute.FirstFloor.route)
                }
            )
        }

        composable(AppRoute.GroundFloor.route) {
            FloorDashboardScreen(
                floorName = "Ground Floor"
            )
        }

        composable(AppRoute.FirstFloor.route) {
            FloorDashboardScreen(
                floorName = "First Floor"
            )
        }
    }
}