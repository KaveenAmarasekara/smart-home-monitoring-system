package com.example.smarthomemonitoring.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                DeviceType.LIGHT -> "💡"
                DeviceType.OUTLET -> "🔌"
                DeviceType.IRON -> "♨"
                DeviceType.CAMERA -> "📷"
                DeviceType.MULTI_SWITCH -> "🎚"
            }

            Column(
                modifier = Modifier
                    .offset(
                        x =
                            cellWidth * device.gridX +
                                    cellWidth / 2 -
                                    24.dp,

                        y =
                            cellHeight * device.gridY +
                                    cellHeight / 2 -
                                    28.dp
                    )
                    .clickable {
                        onDeviceClick(device)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(markerColor)
                        .border(
                            width = 2.dp,
                            color = Color.White.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = markerText,
                        fontSize = 18.sp
                    )
                }

                Text(
                    text = device.name,
                    color = Color.White,
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .background(
                            color = markerColor.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}