package com.example.smarthomemonitoring.data.mock

import com.example.smarthomemonitoring.data.model.Device
import com.example.smarthomemonitoring.data.model.DeviceStatus
import com.example.smarthomemonitoring.data.model.DeviceType

object MockData {

    val groundFloorDevices = listOf(
        Device(
            id = "device1",
            name = "Living Room Light",
            type = DeviceType.LIGHT,
            status = DeviceStatus.ON,
            room = "Living Room",
            gridX = 0,
            gridY = 0,
            brightness = 75,
            scheduleEnabled = true,
            scheduleStart = "18:00",
            scheduleEnd = "23:00"
        ),

        Device(
            id = "device2",
            name = "TV Outlet",
            type = DeviceType.OUTLET,
            status = DeviceStatus.OFF,
            room = "Living Room",
            gridX = 1,
            gridY = 0
        ),

        Device(
            id = "device3",
            name = "Clothing Iron",
            type = DeviceType.IRON,
            status = DeviceStatus.OFF,
            room = "Laundry Room",
            gridX = 2,
            gridY = 1,
            maxOnDurationMinutes = 15
        ),

        Device(
            id = "device4",
            name = "Front Camera",
            type = DeviceType.CAMERA,
            status = DeviceStatus.ON,
            room = "Entrance",
            gridX = 0,
            gridY = 2,
            cameraSnapshotUri = "mock://camera/front-camera/snapshot.jpg",
            cameraStreamUri = "mock://camera/front-camera/live.mjpeg",
            cameraLastSnapshotAt = "Today 08:45",
            cameraMotionDetected = true
        ),

        Device(
            id = "device5",
            name = "Kitchen Switch Unit",
            type = DeviceType.MULTI_SWITCH,
            status = DeviceStatus.OFF,
            room = "Kitchen",
            gridX = 3,
            gridY = 2,
            switches = mapOf(
                "Main Light" to false,
                "Counter Light" to false,
                "Dining Light" to false
            )
        ),

        Device(
            id = "device6",
            name = "Garage Outlet",
            type = DeviceType.OUTLET,
            status = DeviceStatus.DISCONNECTED,
            room = "Garage",
            gridX = 3,
            gridY = 3
        )
    )

    val firstFloorDevices = listOf(
        Device(
            id = "device7",
            name = "Bedroom Light",
            type = DeviceType.LIGHT,
            status = DeviceStatus.OFF,
            room = "Master Bedroom",
            gridX = 0,
            gridY = 0,
            brightness = 60
        ),

        Device(
            id = "device8",
            name = "Bedroom Outlet",
            type = DeviceType.OUTLET,
            status = DeviceStatus.ON,
            room = "Master Bedroom",
            gridX = 1,
            gridY = 0
        ),

        Device(
            id = "device9",
            name = "Hallway Camera",
            type = DeviceType.CAMERA,
            status = DeviceStatus.ON,
            room = "Hallway",
            gridX = 1,
            gridY = 1,
            cameraSnapshotUri = "mock://camera/hallway-camera/snapshot.jpg",
            cameraStreamUri = "mock://camera/hallway-camera/live.mjpeg",
            cameraLastSnapshotAt = "Today 08:42"
        ),

        Device(
            id = "device10",
            name = "Study Light",
            type = DeviceType.LIGHT,
            status = DeviceStatus.ERROR,
            room = "Study Room",
            gridX = 2,
            gridY = 1
        ),

        Device(
            id = "device11",
            name = "Bedroom Switch Unit",
            type = DeviceType.MULTI_SWITCH,
            status = DeviceStatus.ON,
            room = "Master Bedroom",
            gridX = 3,
            gridY = 2,
            switches = mapOf(
                "Ceiling Light" to true,
                "Bedside Light" to false,
                "Bathroom Light" to true,
                "Balcony Light" to false
            )
        )
    )
}
