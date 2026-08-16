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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.smarthomemonitoring.data.model.UsageReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    report: UsageReport,
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
                        text = report.totalUsageLabel,
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

            if (report.mostUsedDevices.isEmpty()) {
                Text(
                    text = "No usage records yet."
                )
            } else {
                report.mostUsedDevices.forEach { item ->
                    UsageBar(
                        title = item.title,
                        value = item.value,
                        percentage = item.percentage
                    )
                }
            }

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
                            "${report.safetyShutdownsThisMonth} automatic shutdowns this month"
                    )
                }
            }
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
