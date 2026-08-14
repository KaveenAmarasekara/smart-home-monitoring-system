package com.example.smarthomemonitoring.ui.screens.floor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smarthomemonitoring.data.model.Device
import com.example.smarthomemonitoring.data.model.DeviceStatus
import com.example.smarthomemonitoring.ui.components.DeviceCard
import com.example.smarthomemonitoring.ui.components.FloorPlanGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorDashboardScreen(
    floorName: String,
    devices: List<Device>,
    onUpdateDevice: (Device) -> Unit,
    onOpenDevice: (Device) -> Unit,
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
                    Column {
                        Text(floorName)

                        Text(
                            text =
                                "${devices.size} devices",
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp)
        ) {

            item {
                Text(
                    text = "Floor Plan",
                    style =
                        MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(
                        top = 16.dp,
                        bottom = 12.dp
                    )
                )
            }

            item {
                FloorPlanGrid(
                    devices = devices,
                    columns = 4,
                    rows = 4,
                    onDeviceClick = {
                        onOpenDevice(it)
                    }
                )
            }

            item {
                Text(
                    text = "Devices",
                    style =
                        MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(
                        top = 24.dp,
                        bottom = 8.dp
                    )
                )
            }

            items(
                items = devices,
                key = {
                    it.id
                }
            ) { device ->

                DeviceCard(
                    device = device,

                    onToggle = { isOn ->

                        onUpdateDevice(
                            device.copy(
                                status =
                                    if (isOn) {
                                        DeviceStatus.ON
                                    } else {
                                        DeviceStatus.OFF
                                    }
                            )
                        )
                    },

                    onClick = {
                        onOpenDevice(device)
                    }
                )
            }
        }
    }
}