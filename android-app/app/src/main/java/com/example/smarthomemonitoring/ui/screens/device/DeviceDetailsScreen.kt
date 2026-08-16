package com.example.smarthomemonitoring.ui.screens.device

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete device?") },
            text = { Text("\"${device.name}\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDeleteDevice() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(device.name) },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete device",
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(20.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = device.name,
                        style =
                            MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = device.room,
                        style =
                            MaterialTheme.typography.bodyLarge
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text =
                            "Status: ${device.status}"
                    )

                    Text(
                        text =
                            "Type: ${device.type}"
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Power",
                style =
                    MaterialTheme.typography.titleLarge
            )

            Switch(
                checked =
                    device.status == DeviceStatus.ON,

                onCheckedChange = { enabled ->

                    onUpdateDevice(
                        device.copy(
                            status =
                                if (enabled) {
                                    DeviceStatus.ON
                                } else {
                                    DeviceStatus.OFF
                                }
                        )
                    )
                },

                enabled =
                    device.status != DeviceStatus.ERROR &&
                            device.status != DeviceStatus.DISCONNECTED
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            when (device.type) {

                DeviceType.LIGHT -> {
                    LightControls(
                        device = device,
                        onUpdateDevice =
                            onUpdateDevice
                    )
                }

                DeviceType.IRON -> {
                    IronControls(
                        device = device,
                        onUpdateDevice =
                            onUpdateDevice
                    )
                }

                DeviceType.CAMERA -> {
                    CameraControls(
                        device = device
                    )
                }

                DeviceType.MULTI_SWITCH -> {
                    MultiSwitchControls(
                        device = device,
                        onUpdateDevice =
                            onUpdateDevice
                    )
                }

                DeviceType.OUTLET -> {
                    OutletControls()
                }
            }
        }
    }
}

@Composable
private fun LightControls(
    device: Device,
    onUpdateDevice: (Device) -> Unit
) {
    var brightnessSlider by remember(device.id) { mutableStateOf(device.brightness.toFloat()) }

    Text(
        text = "Brightness",
        style = MaterialTheme.typography.titleLarge
    )

    Text(
        text = "${brightnessSlider.roundToInt()}%"
    )

    Slider(
        value = brightnessSlider,
        onValueChange = { brightnessSlider = it },
        onValueChangeFinished = {
            onUpdateDevice(device.copy(brightness = brightnessSlider.roundToInt()))
        },
        valueRange = 0f..100f
    )

    Spacer(
        modifier = Modifier.height(18.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Automatic Schedule",
                style =
                    MaterialTheme.typography.titleMedium
            )

            Text(
                text =
                    "Turn the light on/off automatically",
                style =
                    MaterialTheme.typography.bodySmall
            )
        }

        Switch(
            checked =
                device.scheduleEnabled,

            onCheckedChange = {
                onUpdateDevice(
                    device.copy(
                        scheduleEnabled = it
                    )
                )
            }
        )
    }

    if (device.scheduleEnabled) {

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = device.scheduleStart,
            onValueChange = {
                onUpdateDevice(
                    device.copy(
                        scheduleStart = it
                    )
                )
            },
            label = {
                Text("Start Time")
            },
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = device.scheduleEnd,
            onValueChange = {
                onUpdateDevice(
                    device.copy(
                        scheduleEnd = it
                    )
                )
            },
            label = {
                Text("End Time")
            },
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun IronControls(
    device: Device,
    onUpdateDevice: (Device) -> Unit
) {
    var durationSlider by remember(device.id) { mutableStateOf(device.maxOnDurationMinutes.toFloat()) }

    val elapsedMinutes = if (device.status == DeviceStatus.ON && device.turnedOnAt > 0L) {
        ((System.currentTimeMillis() - device.turnedOnAt) / 60_000L).toInt()
    } else null

    if (elapsedMinutes != null) {
        val remaining = device.maxOnDurationMinutes - elapsedMinutes
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (remaining <= 2)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (remaining > 0) "Auto-shutdown in $remaining min" else "Shutting down…",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "ON for $elapsedMinutes min · max ${device.maxOnDurationMinutes} min",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = "⚠ Safety Protection",
                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    "Maximum ON duration: ${durationSlider.roundToInt()} minutes"
            )

            Slider(
                value = durationSlider,
                onValueChange = { durationSlider = it },
                onValueChangeFinished = {
                    onUpdateDevice(
                        device.copy(maxOnDurationMinutes = durationSlider.roundToInt())
                    )
                },
                valueRange = 5f..60f
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text =
                    "Auto-shutdown: the backend safety worker monitors this device and forces it OFF if the active duration exceeds the limit above. A notification is pushed when this triggers.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CameraControls(
    device: Device
) {
    Text(
        text = "Camera Monitor",
        style =
            MaterialTheme.typography.titleLarge
    )

    Spacer(
        modifier = Modifier.height(12.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                ),
                shape =
                    RoundedCornerShape(22.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "📷",
                style =
                    MaterialTheme.typography.displayLarge
            )

            Text(
                text =
                    "${device.name} Mock Stream"
            )

            Text(
                text =
                    "LIVE",
                color =
                    MaterialTheme.colorScheme.error
            )
        }
    }

    Spacer(
        modifier = Modifier.height(10.dp)
    )

    Text(
        text =
            "A mock snapshot or URI stream can replace this panel later."
    )
}

@Composable
private fun MultiSwitchControls(
    device: Device,
    onUpdateDevice: (Device) -> Unit
) {
    Text(
        text = "Individual Switches",
        style =
            MaterialTheme.typography.titleLarge
    )

    Spacer(
        modifier = Modifier.height(10.dp)
    )

    device.switches.forEach {
            switch ->

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = switch.key
            )

            Switch(
                checked = switch.value,

                onCheckedChange = { enabled ->

                    val updatedSwitches =
                        device.switches
                            .toMutableMap()

                    updatedSwitches[
                        switch.key
                    ] = enabled

                    val anySwitchOn =
                        updatedSwitches
                            .values
                            .any {
                                it
                            }

                    onUpdateDevice(
                        device.copy(
                            switches =
                                updatedSwitches,
                            status =
                                if (anySwitchOn) {
                                    DeviceStatus.ON
                                } else {
                                    DeviceStatus.OFF
                                }
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun OutletControls() {
    Text(
        text = "Electrical Outlet",
        style =
            MaterialTheme.typography.titleLarge
    )

    Spacer(
        modifier = Modifier.height(8.dp)
    )

    Text(
        text =
            "Use the power switch above to enable or disable this outlet."
    )
}