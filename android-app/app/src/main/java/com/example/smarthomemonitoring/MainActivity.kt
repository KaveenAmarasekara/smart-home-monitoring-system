package com.example.smarthomemonitoring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smarthomemonitoring.ui.navigation.AppNavigation
import com.example.smarthomemonitoring.ui.theme.SmartHomeMonitoringTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        setContent {

            SmartHomeMonitoringTheme {

                AppNavigation()
            }
        }
    }
}