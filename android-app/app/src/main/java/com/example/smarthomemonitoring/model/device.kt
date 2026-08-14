package com.example.smarthomemonitoring.data.model

data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    val status: DeviceStatus,
    val room: String,
    val gridX: Int,
    val gridY: Int,

    // Light settings
    val brightness: Int = 100,
    val scheduleEnabled: Boolean = false,
    val scheduleStart: String = "18:00",
    val scheduleEnd: String = "23:00",

    // Safety device settings
    val maxOnDurationMinutes: Int = 15,

    // Multi-switch
    val switches: Map<String, Boolean> = emptyMap()
)