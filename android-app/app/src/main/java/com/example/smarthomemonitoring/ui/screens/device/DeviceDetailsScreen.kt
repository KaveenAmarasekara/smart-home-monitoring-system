package com.example.smarthomemonitoring.ui.screens.device

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smarthomemonitoring.data.model.Device
import com.example.smarthomemonitoring.data.model.DeviceStatus
import com.example.smarthomemonitoring.data.model.DeviceType
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsScreen(
    device: Device,
    onUpdateDevice: (Device) -> Unit,
    onDeleteDevice: () -> Unit,
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
                    Column {
                        Text(device.name)
                        Text(
                            text = "${device.room} - ${device.type.label()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDeleteDevice) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete device")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsCard {
                DetailRow(label = "Status", value = device.status.label())
                DetailRow(label = "Room", value = device.room)
                DetailRow(label = "Grid position", value = "X ${device.gridX}, Y ${device.gridY}")

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Power", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = if (device.status == DeviceStatus.ON) "Device is on" else "Device is off",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = device.status == DeviceStatus.ON,
                        enabled = device.status != DeviceStatus.ERROR &&
                            device.status != DeviceStatus.DISCONNECTED,
                        onCheckedChange = { isOn ->
                            onUpdateDevice(
                                device.copy(status = if (isOn) DeviceStatus.ON else DeviceStatus.OFF)
                            )
                        }
                    )
                }
            }

            when (device.type) {
                DeviceType.LIGHT -> LightSettings(device = device, onUpdateDevice = onUpdateDevice)
                DeviceType.IRON -> IronSettings(device = device, onUpdateDevice = onUpdateDevice)
                DeviceType.MULTI_SWITCH -> SwitchSettings(device = device, onUpdateDevice = onUpdateDevice)
                DeviceType.OUTLET,
                DeviceType.CAMERA -> Unit
            }

            OutlinedButton(
                onClick = onDeleteDevice,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Text(
                    text = "Delete device",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LightSettings(
    device: Device,
    onUpdateDevice: (Device) -> Unit
) {
    SettingsCard {
        Text("Light settings", style = MaterialTheme.typography.titleMedium)

        Text(
            text = "Brightness ${device.brightness}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
        Slider(
            value = device.brightness.toFloat(),
            onValueChange = { value ->
                onUpdateDevice(device.copy(brightness = value.roundToInt().coerceIn(0, 100)))
            },
            valueRange = 0f..100f
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Schedule", style = MaterialTheme.typography.titleSmall)
            Switch(
                checked = device.scheduleEnabled,
                onCheckedChange = { onUpdateDevice(device.copy(scheduleEnabled = it)) }
            )
        }

        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TimeInputButton(
                label = "Start",
                value = device.scheduleStart,
                onTimeSelected = { onUpdateDevice(device.copy(scheduleStart = it)) },
                modifier = Modifier.weight(1f)
            )
            TimeInputButton(
                label = "End",
                value = device.scheduleEnd,
                onTimeSelected = { onUpdateDevice(device.copy(scheduleEnd = it)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimeInputButton(
    label: String,
    value: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val (hour, minute) = parseClock(value) ?: (18 to 0)

    OutlinedButton(
        onClick = {
            TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    onTimeSelected("%02d:%02d".format(selectedHour, selectedMinute))
                },
                hour,
                minute,
                true
            ).show()
        },
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
            Text(text = value)
        }
    }
}

@Composable
private fun IronSettings(
    device: Device,
    onUpdateDevice: (Device) -> Unit
) {
    SettingsCard {
        Text("Safety settings", style = MaterialTheme.typography.titleMedium)

        if (device.status == DeviceStatus.ON) {
            val remainingMinutes = remainingSafetyMinutes(device)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Safety measure active",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = if (remainingMinutes != null) {
                            "Iron is turned on and will automatically turn off in $remainingMinutes minute${if (remainingMinutes == 1) "" else "s"}."
                        } else {
                            "Iron is turned on and set to automatically turn off after ${device.maxOnDurationMinutes} minutes."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Text(
                text = "Iron will automatically turn off after ${device.maxOnDurationMinutes} minutes when switched on.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        OutlinedTextField(
            value = device.maxOnDurationMinutes.toString(),
            onValueChange = { value ->
                val minutes = value.toIntOrNull()
                if (minutes != null) {
                    onUpdateDevice(device.copy(maxOnDurationMinutes = minutes.coerceIn(5, 120)))
                }
            },
            label = { Text("Max ON duration (min)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
    }
}

private fun remainingSafetyMinutes(device: Device): Int? {
    if (device.turnedOnAt <= 0L) return null
    val elapsedMs = System.currentTimeMillis() - device.turnedOnAt
    val remainingMs = device.maxOnDurationMinutes * 60_000L - elapsedMs
    return (remainingMs.coerceAtLeast(0L) / 60_000L + 1L).toInt()
}

@Composable
private fun SwitchSettings(
    device: Device,
    onUpdateDevice: (Device) -> Unit
) {
    SettingsCard {
        Text("Switches", style = MaterialTheme.typography.titleMedium)

        device.switches.forEach { (name, enabled) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = enabled,
                    onCheckedChange = { isEnabled ->
                        onUpdateDevice(
                            device.copy(switches = device.switches + (name to isEnabled))
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun DeviceType.label(): String {
    return when (this) {
        DeviceType.OUTLET -> "Outlet"
        DeviceType.LIGHT -> "Light"
        DeviceType.IRON -> "Iron"
        DeviceType.CAMERA -> "Camera"
        DeviceType.MULTI_SWITCH -> "Multi-Switch"
    }
}

private fun DeviceStatus.label(): String {
    return when (this) {
        DeviceStatus.ON -> "On"
        DeviceStatus.OFF -> "Off"
        DeviceStatus.ERROR -> "Error"
        DeviceStatus.DISCONNECTED -> "Disconnected"
    }
}

private fun parseClock(value: String): Pair<Int, Int>? {
    val parts = value.trim().split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour to minute
}
