package com.example.smarthomemonitoring.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val statusColor = when (device.status) {
        DeviceStatus.ON -> Color(0xFF2E7D32)
        DeviceStatus.OFF -> Color(0xFF757575)
        DeviceStatus.ERROR -> Color(0xFFD32F2F)
        DeviceStatus.DISCONNECTED -> Color(0xFFFF8F00)
    }

    val icon = when (device.type) {
        DeviceType.LIGHT -> "💡"
        DeviceType.OUTLET -> "🔌"
        DeviceType.IRON -> "♨"
        DeviceType.CAMERA -> "📷"
        DeviceType.MULTI_SWITCH -> "🎚"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(
                    modifier = Modifier.width(14.dp)
                )

                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = device.room,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = device.status.name,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Switch(
                checked = device.status == DeviceStatus.ON,
                onCheckedChange = onToggle,
                enabled =
                    device.status != DeviceStatus.ERROR &&
                            device.status != DeviceStatus.DISCONNECTED
            )
        }
    }
}