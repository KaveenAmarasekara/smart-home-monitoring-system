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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Iron
import androidx.compose.material.icons.filled.LightbulbCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
            .height(320.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        val cellWidth = maxWidth / columns
        val cellHeight = maxHeight / rows

        // Grid lines
        for (column in 1 until columns) {
            Box(
                modifier = Modifier
                    .offset(x = cellWidth * column)
                    .size(width = 1.dp, height = maxHeight)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
        }
        for (row in 1 until rows) {
            Box(
                modifier = Modifier
                    .offset(y = cellHeight * row)
                    .size(width = maxWidth, height = 1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
        }

        // Cell indices
        for (col in 0 until columns) {
            for (row in 0 until rows) {
                Box(
                    modifier = Modifier
                        .offset(x = cellWidth * col, y = cellHeight * row)
                        .size(width = cellWidth, height = cellHeight),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = "${col},${row}",
                        fontSize = 7.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(3.dp)
                    )
                }
            }
        }

        // Device markers
        devices.forEach { device ->
            val markerColor = when (device.status) {
                DeviceStatus.ON -> Color(0xFF1c6b4c)
                DeviceStatus.OFF -> Color(0xFF757575)
                DeviceStatus.ERROR -> Color(0xFFD32F2F)
                DeviceStatus.DISCONNECTED -> Color(0xFFE65100)
            }

            val markerIcon: ImageVector = when (device.type) {
                DeviceType.LIGHT -> Icons.Filled.LightbulbCircle
                DeviceType.OUTLET -> Icons.Filled.ElectricBolt
                DeviceType.IRON -> Icons.Filled.Iron
                DeviceType.CAMERA -> Icons.Filled.CameraAlt
                DeviceType.MULTI_SWITCH -> Icons.Filled.Tune
            }

            val markerX = cellWidth * device.gridX + cellWidth / 2 - 22.dp
            val markerY = cellHeight * device.gridY + cellHeight / 2 - 26.dp

            Column(
                modifier = Modifier
                    .offset(x = markerX, y = markerY)
                    .clickable { onDeviceClick(device) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(elevation = 3.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(markerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = markerIcon,
                        contentDescription = device.name,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .background(
                            color = markerColor.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = device.name,
                        color = Color.White,
                        fontSize = 7.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
