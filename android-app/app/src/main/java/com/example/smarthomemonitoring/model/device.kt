package com.example.smarthomemonitoring.data.model

data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    val status: DeviceStatus,
    val room: String,
    val gridX: Int,
    val gridY: Int
)