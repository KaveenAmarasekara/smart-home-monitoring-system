package com.example.smarthomemonitoring.ui.screens.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
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
                    Text("Notifications")
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

            NotificationCard(
                title =
                    "⚠ Iron automatically switched off",
                description =
                    "Maximum active duration of 15 minutes was reached.",
                time = "10:35 AM",
                important = true
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            NotificationCard(
                title =
                    "🔌 Garage Outlet disconnected",
                description =
                    "The device is currently unreachable.",
                time = "9:20 AM",
                important = true
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            NotificationCard(
                title =
                    "💡 Living Room schedule",
                description =
                    "Light switched on automatically.",
                time = "Yesterday",
                important = false
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text =
                    "These are mock alerts. Firebase Cloud Messaging will provide real notifications later.",
                style =
                    MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun NotificationCard(
    title: String,
    description: String,
    time: String,
    important: Boolean
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (important) {
                        MaterialTheme
                            .colorScheme
                            .errorContainer
                    } else {
                        MaterialTheme
                            .colorScheme
                            .secondaryContainer
                    }
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = description
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = time,
                style =
                    MaterialTheme.typography.labelSmall
            )
        }
    }
}