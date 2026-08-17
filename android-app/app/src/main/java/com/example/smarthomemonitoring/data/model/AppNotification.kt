package com.example.smarthomemonitoring.data.model

data class AppNotification(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val important: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)
