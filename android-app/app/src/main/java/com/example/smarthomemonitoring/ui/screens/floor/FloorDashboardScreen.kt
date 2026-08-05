package com.example.smarthomemonitoring.ui.screens.floor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private enum class DeviceStatus {
    ON,
    OFF,
    ERROR,
    DISCONNECTED
}

private enum class DeviceType(val label: String) {
    OUTLET("Outlet"),
    LIGHT("Light"),
    IRON("Iron"),
    CAMERA("Camera"),
    MULTI_SWITCH("Switch Unit")
}

private data class SmartDevice(
    val id: String,
    val name: String,
    val room: String,
    val type: DeviceType,
    val status: DeviceStatus,
    val row: Int,
    val column: Int,
    val maxOnDuration: String? = null,
    val switches: List<Boolean> = emptyList()
)

@Composable
fun FloorDashboardScreen() {
    var devices by remember {
        mutableStateOf(
            listOf(
                SmartDevice(
                    id = "living-light",
                    name = "Living Room Light",
                    room = "Living Room",
                    type = DeviceType.LIGHT,
                    status = DeviceStatus.ON,
                    row = 0,
                    column = 0
                ),
                SmartDevice(
                    id = "kitchen-outlet",
                    name = "Kitchen Outlet",
                    room = "Kitchen",
                    type = DeviceType.OUTLET,
                    status = DeviceStatus.OFF,
                    row = 0,
                    column = 2
                ),
                SmartDevice(
                    id = "utility-iron",
                    name = "Iron Slot",
                    room = "Utility",
                    type = DeviceType.IRON,
                    status = DeviceStatus.ON,
                    row = 2,
                    column = 1,
                    maxOnDuration = "20 min"
                ),
                SmartDevice(
                    id = "front-camera",
                    name = "Front Door Camera",
                    room = "Entrance",
                    type = DeviceType.CAMERA,
                    status = DeviceStatus.ON,
                    row = 1,
                    column = 3
                ),
                SmartDevice(
                    id = "bed-switches",
                    name = "Bedroom Switch Unit",
                    room = "Bedroom",
                    type = DeviceType.MULTI_SWITCH,
                    status = DeviceStatus.ON,
                    row = 3,
                    column = 2,
                    switches = listOf(true, false, true, false)
                )
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        DashboardHeader(devices = devices)
        FloorGrid(devices = devices)

        Text(
            text = "Devices",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        devices.forEach { device ->
            DeviceCard(
                device = device,
                onToggle = {
                    devices = devices.map {
                        if (it.id == device.id) {
                            it.copy(
                                status = if (it.status == DeviceStatus.ON) {
                                    DeviceStatus.OFF
                                } else {
                                    DeviceStatus.ON
                                }
                            )
                        } else {
                            it
                        }
                    }
                },
                onSwitchToggle = { switchIndex ->
                    devices = devices.map {
                        if (it.id == device.id) {
                            it.copy(
                                switches = it.switches.mapIndexed { index, isOn ->
                                    if (index == switchIndex) !isOn else isOn
                                },
                                status = DeviceStatus.ON
                            )
                        } else {
                            it
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun DashboardHeader(devices: List<SmartDevice>) {
    val activeCount = devices.count { it.status == DeviceStatus.ON }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Ground Floor",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$activeCount active devices • ${devices.size} monitored points",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FloorGrid(devices: List<SmartDevice>) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) { column ->
                        val device = devices.firstOrNull {
                            it.row == row && it.column == column
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (device == null) {
                                    Text(
                                        text = "Room",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    DeviceMarker(device = device)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceMarker(device: SmartDevice) {
    val markerColor = when (device.status) {
        DeviceStatus.ON -> Color(0xFF2E7D32)
        DeviceStatus.OFF -> MaterialTheme.colorScheme.outline
        DeviceStatus.ERROR -> MaterialTheme.colorScheme.error
        DeviceStatus.DISCONNECTED -> Color(0xFF616161)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(markerColor, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = device.type.label.first().toString(),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = device.room,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeviceCard(
    device: SmartDevice,
    onToggle: () -> Unit,
    onSwitchToggle: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${device.type.label} • ${device.room}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(status = device.status)
            }

            if (device.type == DeviceType.CAMERA) {
                CameraPreview()
            }

            if (device.maxOnDuration != null) {
                SafetyRule(maxOnDuration = device.maxOnDuration)
            }

            if (device.switches.isNotEmpty()) {
                MultiSwitchControls(
                    switches = device.switches,
                    onSwitchToggle = onSwitchToggle
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Power",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = device.status == DeviceStatus.ON,
                        onCheckedChange = { onToggle() },
                        enabled = device.status != DeviceStatus.DISCONNECTED
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: DeviceStatus) {
    val color = when (status) {
        DeviceStatus.ON -> Color(0xFF2E7D32)
        DeviceStatus.OFF -> MaterialTheme.colorScheme.outline
        DeviceStatus.ERROR -> MaterialTheme.colorScheme.error
        DeviceStatus.DISCONNECTED -> Color(0xFF616161)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f),
        contentColor = color
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SafetyRule(maxOnDuration: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = "Safety cutoff after $maxOnDuration",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun CameraPreview() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF263238)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Mock camera stream",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MultiSwitchControls(
    switches: List<Boolean>,
    onSwitchToggle: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalDivider()
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            switches.forEachIndexed { index, isOn ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "S${index + 1}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(
                        checked = isOn,
                        onCheckedChange = { onSwitchToggle(index) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}
