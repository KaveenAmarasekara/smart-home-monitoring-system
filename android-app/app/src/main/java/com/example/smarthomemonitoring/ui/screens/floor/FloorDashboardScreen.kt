package com.example.smarthomemonitoring.ui.screens.floor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smarthomemonitoring.data.model.Device
import com.example.smarthomemonitoring.data.model.DeviceStatus
import com.example.smarthomemonitoring.data.model.DeviceType
import com.example.smarthomemonitoring.ui.components.DeviceCard
import com.example.smarthomemonitoring.ui.components.FloorPlanGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorDashboardScreen(
    floorName: String
) {
    var devices by remember(floorName) {
        mutableStateOf(
            if (floorName == "Ground Floor") {
                listOf(
                    Device(
                        id = "device1",
                        name = "Living Room Light",
                        type = DeviceType.LIGHT,
                        status = DeviceStatus.ON,
                        room = "Living Room",
                        gridX = 0,
                        gridY = 0
                    ),
                    Device(
                        id = "device2",
                        name = "TV Outlet",
                        type = DeviceType.OUTLET,
                        status = DeviceStatus.OFF,
                        room = "Living Room",
                        gridX = 1,
                        gridY = 0
                    ),
                    Device(
                        id = "device3",
                        name = "Clothing Iron",
                        type = DeviceType.IRON,
                        status = DeviceStatus.OFF,
                        room = "Bedroom",
                        gridX = 2,
                        gridY = 1
                    ),
                    Device(
                        id = "device4",
                        name = "Front Camera",
                        type = DeviceType.CAMERA,
                        status = DeviceStatus.ON,
                        room = "Entrance",
                        gridX = 0,
                        gridY = 2
                    ),
                    Device(
                        id = "device5",
                        name = "Kitchen Switch Unit",
                        type = DeviceType.MULTI_SWITCH,
                        status = DeviceStatus.DISCONNECTED,
                        room = "Kitchen",
                        gridX = 3,
                        gridY = 2
                    )
                )
            } else {
                listOf(
                    Device(
                        id = "device6",
                        name = "Bedroom Light",
                        type = DeviceType.LIGHT,
                        status = DeviceStatus.OFF,
                        room = "Master Bedroom",
                        gridX = 0,
                        gridY = 0
                    ),
                    Device(
                        id = "device7",
                        name = "Bedroom Outlet",
                        type = DeviceType.OUTLET,
                        status = DeviceStatus.ON,
                        room = "Master Bedroom",
                        gridX = 1,
                        gridY = 0
                    ),
                    Device(
                        id = "device8",
                        name = "Hallway Camera",
                        type = DeviceType.CAMERA,
                        status = DeviceStatus.ON,
                        room = "Hallway",
                        gridX = 1,
                        gridY = 1
                    ),
                    Device(
                        id = "device9",
                        name = "Study Light",
                        type = DeviceType.LIGHT,
                        status = DeviceStatus.ERROR,
                        room = "Study Room",
                        gridX = 2,
                        gridY = 1
                    )
                )
            }
        )
    }

    var selectedDevice by remember {
        mutableStateOf<Device?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = floorName)

                        Text(
                            text = "${devices.size} devices",
                            style = MaterialTheme.typography.bodySmall
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
                    style = MaterialTheme.typography.headlineSmall,
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
                    onDeviceClick = { device ->
                        selectedDevice = device
                    }
                )
            }

            selectedDevice?.let { device ->

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = device.room,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Type: ${device.type}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "Status: ${device.status}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Devices",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(
                        top = 24.dp,
                        bottom = 8.dp
                    )
                )
            }

            items(
                items = devices,
                key = { it.id }
            ) { device ->

                DeviceCard(
                    device = device,
                    onToggle = { isOn ->

                        devices = devices.map { currentDevice ->

                            if (currentDevice.id == device.id) {

                                currentDevice.copy(
                                    status = if (isOn) {
                                        DeviceStatus.ON
                                    } else {
                                        DeviceStatus.OFF
                                    }
                                )

                            } else {
                                currentDevice
                            }
                        }

                        if (selectedDevice?.id == device.id) {
                            selectedDevice = devices.firstOrNull {
                                it.id == device.id
                            }
                        }
                    }
                )
            }
        }
    }
}