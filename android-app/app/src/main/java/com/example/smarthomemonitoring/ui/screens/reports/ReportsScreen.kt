package com.example.smarthomemonitoring.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
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
                    Text("Usage Reports")
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

            Card(
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "This Week",
                        style =
                            MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "24 h 35 min",
                        style =
                            MaterialTheme.typography.headlineLarge
                    )

                    Text(
                        text =
                            "Total device usage"
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Most Used Devices",
                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            UsageBar(
                title = "Living Room Light",
                value = "8h",
                percentage = 0.8f
            )

            UsageBar(
                title = "Bedroom Outlet",
                value = "6h",
                percentage = 0.6f
            )

            UsageBar(
                title = "Front Camera",
                value = "5h",
                percentage = 0.5f
            )

            UsageBar(
                title = "Kitchen Switch",
                value = "3h",
                percentage = 0.3f
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Card(
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(
                        text =
                            "Safety Shutdowns",
                        style =
                            MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text =
                            "2 automatic shutdowns this month"
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text =
                    "Mock data only. Real reports will come from Firestore usage records."
            )
        }
    }
}

@Composable
private fun UsageBar(
    title: String,
    value: String,
    percentage: Float
) {
    Text(
        text = "$title • $value"
    )

    Spacer(
        modifier = Modifier.height(6.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(
                RoundedCornerShape(20.dp)
            )
            .background(
                MaterialTheme.colorScheme
                    .surfaceVariant
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth(
                    percentage
                )
                .height(12.dp)
                .background(
                    MaterialTheme
                        .colorScheme.primary
                )
        )
    }

    Spacer(
        modifier = Modifier.height(18.dp)
    )
}