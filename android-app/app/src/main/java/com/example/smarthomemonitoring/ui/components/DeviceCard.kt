package com.example.smarthomemonitoring.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Iron
import androidx.compose.material.icons.filled.LightbulbCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.smarthomemonitoring.data.model.Device
import com.example.smarthomemonitoring.data.model.DeviceStatus
import com.example.smarthomemonitoring.data.model.DeviceType

@Composable
fun DeviceCard(
    device: Device,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val (statusColor, statusLabel) = when (device.status) {
        DeviceStatus.ON -> Color(0xFF2E7D32) to "ON"
        DeviceStatus.OFF -> Color(0xFF757575) to "OFF"
        DeviceStatus.ERROR -> Color(0xFFD32F2F) to "ERROR"
        DeviceStatus.DISCONNECTED -> Color(0xFFE65100) to "OFFLINE"
    }

    val icon: ImageVector = when (device.type) {
        DeviceType.LIGHT -> Icons.Filled.LightbulbCircle
        DeviceType.OUTLET -> Icons.Filled.ElectricBolt
        DeviceType.IRON -> Icons.Filled.Iron
        DeviceType.CAMERA -> Icons.Filled.CameraAlt
        DeviceType.MULTI_SWITCH -> Icons.Filled.Tune
    }

    val iconBg = when (device.status) {
        DeviceStatus.ON -> MaterialTheme.colorScheme.primaryContainer
        DeviceStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
        DeviceStatus.DISCONNECTED -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape),
                    color = iconBg
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = device.type.name,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(26.dp),
                        tint = if (device.status == DeviceStatus.ON)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = device.room,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        StatusChip(label = statusLabel, color = statusColor)

                        if (device.type == DeviceType.LIGHT && device.scheduleEnabled) {
                            StatusChip(label = "Scheduled", color = Color(0xFF0277BD))
                        }
                    }
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

@Composable
private fun StatusChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
