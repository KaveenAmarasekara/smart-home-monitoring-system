package com.example.smarthomemonitoring.ui.screens.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smarthomemonitoring.data.model.AppNotification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    onMarkRead: (AppNotification) -> Unit,
    onClear: (AppNotification) -> Unit,
    onClearRead: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text("Notifications")
                },
                actions = {
                    TextButton(
                        onClick = onClearRead,
                        enabled = notifications.any { it.read }
                    ) {
                        Text("Clear read")
                    }
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

            if (notifications.isEmpty()) {
                Text(
                    text = "No notifications yet.",
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            } else {
                notifications.forEach { notification ->
                    NotificationCard(
                        notification = notification,
                        onMarkRead = { onMarkRead(notification) },
                        onClear = { onClear(notification) }
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    onMarkRead: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (notification.important && !notification.read) {
                        MaterialTheme
                            .colorScheme
                            .errorContainer
                    } else if (!notification.read) {
                        MaterialTheme
                            .colorScheme
                            .secondaryContainer
                    } else {
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                    }
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                text = notification.title,
                style =
                    MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = notification.description
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "${notification.time}${if (notification.read) " - Read" else ""}",
                style =
                    MaterialTheme.typography.labelSmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (!notification.read) {
                    OutlinedButton(onClick = onMarkRead) {
                        Text("Mark read")
                    }
                }
                TextButton(onClick = onClear) {
                    Text("Clear")
                }
            }
        }
    }
}
