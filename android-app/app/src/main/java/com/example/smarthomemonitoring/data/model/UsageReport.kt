package com.example.smarthomemonitoring.data.model

data class UsageReport(
    val totalUsageLabel: String,
    val mostUsedDevices: List<UsageReportItem>,
    val safetyShutdownsThisMonth: Int
)

data class UsageReportItem(
    val title: String,
    val value: String,
    val percentage: Float
)
