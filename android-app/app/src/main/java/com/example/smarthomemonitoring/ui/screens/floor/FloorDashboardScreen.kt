package com.example.smarthomemonitoring.ui.screens.floor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smarthomemonitoring.data.model.Device
import com.example.smarthomemonitoring.data.model.DeviceStatus
import com.example.smarthomemonitoring.data.model.DeviceType
import com.example.smarthomemonitoring.ui.components.DeviceCard
import com.example.smarthomemonitoring.ui.components.FloorPlanGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorDashboardScreen(
    floorName: String,
    floorId: String,
    devices: List<Device>,
    onUpdateDevice: (Device) -> Unit,
    onAddDevice: (Device) -> Unit,
    onOpenDevice: (Device) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

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
                        Text(floorName)
                        Text(
                            text = "${devices.size} devices · ${devices.count { it.status == DeviceStatus.ON }} online",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add device")
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            item {
                Text(
                    text = "Floor Plan",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                )
            }

            item {
                FloorPlanGrid(
                    devices = devices,
                    columns = 4,
                    rows = 4,
                    onDeviceClick = { onOpenDevice(it) }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Devices",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${devices.count { it.status == DeviceStatus.ON }}/${devices.size} active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            items(items = devices, key = { it.id }) { device ->
                DeviceCard(
                    device = device,
                    onToggle = { isOn ->
                        onUpdateDevice(device.copy(status = if (isOn) DeviceStatus.ON else DeviceStatus.OFF))
                    },
                    onClick = { onOpenDevice(device) }
                )
            }

            item { Spacer(modifier = Modifier.height(88.dp)) } // FAB clearance
        }

        if (showAddDialog) {
            AddDeviceDialog(
                onAdd = { device ->
                    onAddDevice(device)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDeviceDialog(
    onAdd: (Device) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DeviceType.LIGHT) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var gridX by remember { mutableStateOf("0") }
    var gridY by remember { mutableStateOf("0") }
    var maxOnMinutes by remember { mutableStateOf("15") }

    val typeLabels = mapOf(
        DeviceType.LIGHT to "Light",
        DeviceType.OUTLET to "Outlet",
        DeviceType.IRON to "Iron",
        DeviceType.CAMERA to "Camera",
        DeviceType.MULTI_SWITCH to "Multi-Switch"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Add Device") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Device name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = typeLabels[selectedType] ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        DeviceType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(typeLabels[type] ?: type.name) },
                                onClick = {
                                    selectedType = type
                                    typeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedType == DeviceType.IRON) {
                    OutlinedTextField(
                        value = maxOnMinutes,
                        onValueChange = { maxOnMinutes = it },
                        label = { Text("Max ON duration (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = gridX,
                        onValueChange = { gridX = it },
                        label = { Text("Grid X (0-3)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = gridY,
                        onValueChange = { gridY = it },
                        label = { Text("Grid Y (0-3)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || room.isBlank()) return@TextButton
                    onAdd(
                        Device(
                            id = "",
                            name = name.trim(),
                            type = selectedType,
                            status = DeviceStatus.OFF,
                            room = room.trim(),
                            gridX = gridX.toIntOrNull()?.coerceIn(0, 3) ?: 0,
                            gridY = gridY.toIntOrNull()?.coerceIn(0, 3) ?: 0,
                            maxOnDurationMinutes = maxOnMinutes.toIntOrNull()?.coerceIn(5, 120) ?: 15,
                            switches = if (selectedType == DeviceType.MULTI_SWITCH)
                                mapOf("Switch 1" to false, "Switch 2" to false, "Switch 3" to false)
                            else emptyMap()
                        )
                    )
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
