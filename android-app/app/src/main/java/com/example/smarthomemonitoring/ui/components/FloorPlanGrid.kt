package com.example.smarthomemonitoring.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smarthomemonitoring.data.model.Device
import com.example.smarthomemonitoring.data.model.DeviceStatus
import com.example.smarthomemonitoring.data.model.DeviceType

@Composable
fun FloorPlanGrid(
    devices: List<Device>,
    columns: Int = 4,
    rows: Int = 4,
    onDeviceClick: (Device) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        val cellWidth = maxWidth / columns
        val cellHeight = maxHeight / rows

        for (column in 1 until columns) {
            Box(
                modifier = Modifier
                    .offset(
                        x = cellWidth * column
                    )
                    .size(
                        width = 1.dp,
                        height = maxHeight
                    )
                    .background(
                        MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }

        for (row in 1 until rows) {
            Box(
                modifier = Modifier
                    .offset(
                        y = cellHeight * row
                    )
                    .size(
                        width = maxWidth,
                        height = 1.dp
                    )
                    .background(
                        MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }

        devices.forEach { device ->

            val markerColor = when (device.status) {
                DeviceStatus.ON -> Color(0xFF2E7D32)
                DeviceStatus.OFF -> Color(0xFF616161)
                DeviceStatus.ERROR -> Color(0xFFD32F2F)
                DeviceStatus.DISCONNECTED -> Color(0xFFFF8F00)
            }

            val markerText = when (device.type) {
                DeviceType.LIGHT -> "L"
                DeviceType.OUTLET -> "O"
                DeviceType.IRON -> "I"
                DeviceType.CAMERA -> "C"
                DeviceType.MULTI_SWITCH -> "S"
            }

            Box(
                modifier = Modifier
                    .offset(
                        x =
                            cellWidth * device.gridX +
                                    cellWidth / 2 -
                                    20.dp,

                        y =
                            cellHeight * device.gridY +
                                    cellHeight / 2 -
                                    20.dp
                    )
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(markerColor)
                    .clickable {
                        onDeviceClick(device)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = markerText,
                    color = Color.White
                )
            }
        }
    }
}