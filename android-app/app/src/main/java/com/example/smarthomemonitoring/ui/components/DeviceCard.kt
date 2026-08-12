package com.example.smarthomemonitoring.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Iron
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthomemonitoring.data.model.Device
import com.example.smarthomemonitoring.data.model.DeviceStatus
import com.example.smarthomemonitoring.data.model.DeviceType

@Composable
fun DeviceCard(
    device: Device,
    onToggle: (Boolean) -> Unit
) {
    val icon = getDeviceIcon(device.type)

    val statusColor = when (device.status) {
        DeviceStatus.ON -> Color(0xFF2E7D32)
        DeviceStatus.OFF -> Color(0xFF757575)
        DeviceStatus.ERROR -> Color(0xFFD32F2F)
        DeviceStatus.DISCONNECTED -> Color(0xFFFF8F00)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = device.name,
                    modifier = Modifier.size(34.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = device.room,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = device.status.name,
                        color = statusColor,
                        fontSize = 13.sp
                    )
                }
            }

            Switch(
                checked = device.status == DeviceStatus.ON,
                onCheckedChange = onToggle,
                enabled = device.status != DeviceStatus.ERROR &&
                        device.status != DeviceStatus.DISCONNECTED
            )
        }
    }
}

private fun getDeviceIcon(type: DeviceType): ImageVector {
    return when (type) {
        DeviceType.LIGHT -> Icons.Default.Lightbulb
        DeviceType.OUTLET -> Icons.Default.ElectricalServices
        DeviceType.IRON -> Icons.Default.Iron
        DeviceType.CAMERA -> Icons.Default.CameraAlt
        DeviceType.MULTI_SWITCH -> Icons.Default.ToggleOn
    }
}