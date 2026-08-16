package com.example.smarthomemonitoring.ui.screens.home

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenGroundFloor: () -> Unit,
    onOpenFirstFloor: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    groundFloorTotal: Int = 0,
    groundFloorOnline: Int = 0,
    firstFloorTotal: Int = 0,
    firstFloorOnline: Int = 0,
    unreadAlerts: Int = 0
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Smart Home")

                        Text(
                            text = "Monitoring Dashboard",
                            style =
                                MaterialTheme.typography.bodySmall
                        )
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

            Text(
                text = "Welcome Home 👋",
                style =
                    MaterialTheme.typography.headlineMedium
            )

            Text(
                text =
                    "Choose a floor to monitor and control your devices.",
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            DashboardCard(
                title = "🏠 Ground Floor",
                subtitle =
                    "Living room, kitchen, entrance and garage",
                statusLine =
                    if (groundFloorTotal > 0) "$groundFloorOnline / $groundFloorTotal devices active" else null,
                containerColor =
                    MaterialTheme.colorScheme.primaryContainer,
                onClick = onOpenGroundFloor
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            DashboardCard(
                title = "🛏 First Floor",
                subtitle =
                    "Bedrooms, hallway and study room",
                statusLine =
                    if (firstFloorTotal > 0) "$firstFloorOnline / $firstFloorTotal devices active" else null,
                containerColor =
                    MaterialTheme.colorScheme.secondaryContainer,
                onClick = onOpenFirstFloor
            )

            Spacer(
                modifier = Modifier.height(26.dp)
            )

            Text(
                text = "Quick Access",
                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                SmallActionCard(
                    modifier = Modifier.weight(1f),
                    title = "📊",
                    subtitle = "Reports",
                    onClick = onOpenReports
                )

                SmallActionCard(
                    modifier = Modifier.weight(1f),
                    title = "🔔",
                    subtitle = "Alerts",
                    onClick = onOpenNotifications
                )

                SmallActionCard(
                    modifier = Modifier.weight(1f),
                    title = "⚙",
                    subtitle = "Settings",
                    onClick = onOpenSettings
                )
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    subtitle: String,
    containerColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    statusLine: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleLarge
            )

            Text(
                text = subtitle,
                modifier =
                    Modifier.padding(top = 5.dp)
            )

            if (statusLine != null) {
                Text(
                    text = statusLine,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SmallActionCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement =
                Arrangement.Center
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.headlineMedium
            )

            Text(
                text = subtitle,
                style =
                    MaterialTheme.typography.labelLarge
            )
        }
    }
}