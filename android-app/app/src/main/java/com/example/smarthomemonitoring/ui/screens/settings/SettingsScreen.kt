package com.example.smarthomemonitoring.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smarthomemonitoring.data.model.UserSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onSettingsChanged: (UserSettings) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    TextButton(
                        onClick = onBack
                    ) {
                        Text("Back")
                    }
                },
                title = {
                    Text("Settings")
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {

            Text(
                text = "Notifications",
                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            SettingSwitch(
                title = "Safety Alerts",
                description =
                    "Automatic shutdown notifications",
                checked = settings.safetyAlerts,
                onCheckedChange = {
                    onSettingsChanged(
                        settings.copy(
                            safetyAlerts = it
                        )
                    )
                }
            )

            SettingSwitch(
                title = "Device Alerts",
                description =
                    "Errors and disconnected devices",
                checked = settings.deviceAlerts,
                onCheckedChange = {
                    onSettingsChanged(
                        settings.copy(
                            deviceAlerts = it
                        )
                    )
                }
            )

            SettingSwitch(
                title = "Schedule Alerts",
                description =
                    "Scheduled light activity",
                checked = settings.scheduleAlerts,
                onCheckedChange = {
                    onSettingsChanged(
                        settings.copy(
                            scheduleAlerts = it
                        )
                    )
                }
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Text(
                text = "Application",
                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text =
                    "Smart Home Monitoring System"
            )

            Text(
                text =
                    "Version 1.0 Demo",
                style =
                    MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleMedium
            )

            Text(
                text = description,
                style =
                    MaterialTheme.typography.bodySmall
            )
        }

        Switch(
            checked = checked,
            onCheckedChange =
                onCheckedChange
        )
    }
}
