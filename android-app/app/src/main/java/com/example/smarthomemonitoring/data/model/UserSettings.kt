package com.example.smarthomemonitoring.data.model

data class UserSettings(
    val safetyAlerts: Boolean = true,
    val deviceAlerts: Boolean = true,
    val scheduleAlerts: Boolean = false
)
