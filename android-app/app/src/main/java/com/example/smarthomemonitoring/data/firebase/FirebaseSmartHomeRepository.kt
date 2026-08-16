package com.example.smarthomemonitoring.data.firebase

import com.example.smarthomemonitoring.data.mock.MockData
import com.example.smarthomemonitoring.data.model.AppNotification
import com.example.smarthomemonitoring.data.model.Device
import com.example.smarthomemonitoring.data.model.DeviceStatus
import com.example.smarthomemonitoring.data.model.DeviceType
import com.example.smarthomemonitoring.data.model.UsageReport
import com.example.smarthomemonitoring.data.model.UsageReportItem
import com.example.smarthomemonitoring.data.model.UserSettings
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.FieldValue

object FirebaseSmartHomeRepository {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val devicesCollection = firestore.collection("devices")
    private val notificationsCollection = firestore.collection("notifications")

    val isSignedIn: Boolean
        get() = auth.currentUser != null

    fun signInOrCreateAccount(
        email: String,
        password: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).addOnSuccessListener {
            onResult(Result.success(Unit))
        }.addOnFailureListener { signInError ->
            val code =
                (signInError as? FirebaseAuthException)
                    ?.errorCode

            if (
                code == "ERROR_USER_NOT_FOUND" ||
                code == "ERROR_INVALID_CREDENTIAL"
            ) {
                createAccount(
                    email = email,
                    password = password,
                    onResult = onResult
                )
            } else {
                onResult(Result.failure(signInError))
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun observeDevices(
        floorId: String,
        onDevicesChanged: (List<Device>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return devicesCollection
            .whereEqualTo("floorId", floorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val devices =
                    snapshot
                        ?.documents
                        ?.mapNotNull {
                            it.data?.toDevice(it.id)
                        }
                        ?.sortedWith(
                            compareBy<Device> {
                                it.gridY
                            }.thenBy {
                                it.gridX
                            }
                        )
                        .orEmpty()

                onDevicesChanged(devices)
            }
    }

    fun updateDevice(
        device: Device,
        floorId: String,
        onError: (Exception) -> Unit
    ) {
        devicesCollection
            .document(device.id)
            .set(
                device.toFirestoreMap(floorId),
                SetOptions.merge()
            )
            .addOnFailureListener {
                onError(it)
            }
    }

    fun addDevice(
        device: Device,
        floorId: String,
        onSuccess: (Device) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val newRef = devicesCollection.document()
        val deviceWithId = device.copy(id = newRef.id)
        newRef.set(deviceWithId.toFirestoreMap(floorId))
            .addOnSuccessListener { onSuccess(deviceWithId) }
            .addOnFailureListener { onError(it) }
    }

    fun deleteDevice(
        deviceId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        devicesCollection.document(deviceId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun addNotification(
        notification: AppNotification,
        onError: (Exception) -> Unit
    ) {
        notificationsCollection
            .document(notification.id)
            .set(notification.toFirestoreMap())
            .addOnFailureListener { onError(it) }
    }

    fun observeNotifications(
        onNotificationsChanged: (List<AppNotification>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return notificationsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val notifications =
                    snapshot
                        ?.documents
                        ?.mapNotNull {
                            it.data?.toAppNotification(it.id)
                        }
                        .orEmpty()

                onNotificationsChanged(notifications)
            }
    }

    fun observeUsageReport(
        onReportChanged: (UsageReport) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return devicesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val items =
                    snapshot
                        ?.documents
                        ?.mapNotNull { document ->
                            val data =
                                document.data
                                    ?: return@mapNotNull null

                            val name =
                                data["name"] as? String
                                    ?: return@mapNotNull null

                            val minutes =
                                (data["usageMinutesThisWeek"] as? Number)
                                    ?.toInt()
                                    ?: 0

                            name to minutes
                        }
                        .orEmpty()

                val totalMinutes =
                    items.sumOf {
                        it.second
                    }

                val maxMinutes =
                    items.maxOfOrNull {
                        it.second
                    } ?: 1

                val mostUsedDevices =
                    items
                        .sortedByDescending {
                            it.second
                        }
                        .take(4)
                        .map { (name, minutes) ->
                            UsageReportItem(
                                title = name,
                                value = minutes.toUsageLabel(),
                                percentage =
                                    if (maxMinutes == 0) {
                                        0f
                                    } else {
                                        minutes.toFloat() / maxMinutes
                                    }
                            )
                        }

                val safetyShutdowns =
                    snapshot
                        ?.documents
                        ?.sumOf {
                            (it.data?.get("safetyShutdownsThisMonth") as? Number)
                                ?.toInt()
                                ?: 0
                        }
                        ?: 0

                onReportChanged(
                    UsageReport(
                        totalUsageLabel = totalMinutes.toUsageLabel(),
                        mostUsedDevices = mostUsedDevices,
                        safetyShutdownsThisMonth = safetyShutdowns
                    )
                )
            }
    }

    fun observeUserSettings(
        onSettingsChanged: (UserSettings) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        val uid =
            auth.currentUser?.uid
                ?: return null

        return firestore
            .collection("users")
            .document(uid)
            .collection("settings")
            .document("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                onSettingsChanged(
                    snapshot
                        ?.data
                        ?.toUserSettings()
                        ?: UserSettings()
                )
            }
    }

    fun updateUserSettings(
        settings: UserSettings,
        onError: (Exception) -> Unit
    ) {
        val uid =
            auth.currentUser?.uid
                ?: return

        firestore
            .collection("users")
            .document(uid)
            .collection("settings")
            .document("notifications")
            .set(
                settings.toFirestoreMap(),
                SetOptions.merge()
            )
            .addOnFailureListener {
                onError(it)
            }
    }

    fun seedDefaultDataIfNeeded(
        onError: (Exception) -> Unit
    ) {
        seedDefaultDevicesIfNeeded(onError)
        seedDefaultNotificationsIfNeeded(onError)
        seedDefaultUserSettingsIfNeeded(onError)
    }

    fun seedDefaultDevicesIfNeeded(
        onError: (Exception) -> Unit
    ) {
        devicesCollection
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    return@addOnSuccessListener
                }

                firestore.runBatch { batch ->
                    MockData.groundFloorDevices.forEach { device ->
                        batch.set(
                            devicesCollection.document(device.id),
                            device.toFirestoreMap(
                                floorId = GROUND_FLOOR_ID,
                                usageMinutesThisWeek =
                                    defaultUsageMinutes(device.id),
                                safetyShutdownsThisMonth =
                                    defaultSafetyShutdowns(device.id)
                            )
                        )
                    }

                    MockData.firstFloorDevices.forEach { device ->
                        batch.set(
                            devicesCollection.document(device.id),
                            device.toFirestoreMap(
                                floorId = FIRST_FLOOR_ID,
                                usageMinutesThisWeek =
                                    defaultUsageMinutes(device.id),
                                safetyShutdownsThisMonth =
                                    defaultSafetyShutdowns(device.id)
                            )
                        )
                    }
                }.addOnFailureListener {
                    onError(it)
                }
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    private fun seedDefaultNotificationsIfNeeded(
        onError: (Exception) -> Unit
    ) {
        notificationsCollection
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    return@addOnSuccessListener
                }

                firestore.runBatch { batch ->
                    defaultNotifications().forEach { notification ->
                        batch.set(
                            notificationsCollection.document(notification.id),
                            notification.toFirestoreMap()
                        )
                    }
                }.addOnFailureListener {
                    onError(it)
                }
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    private fun seedDefaultUserSettingsIfNeeded(
        onError: (Exception) -> Unit
    ) {
        val uid =
            auth.currentUser?.uid
                ?: return

        val settingsDocument =
            firestore
                .collection("users")
                .document(uid)
                .collection("settings")
                .document("notifications")

        settingsDocument
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    return@addOnSuccessListener
                }

                settingsDocument
                    .set(UserSettings().toFirestoreMap())
                    .addOnFailureListener {
                        onError(it)
                    }
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    private fun createAccount(
        email: String,
        password: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(
            email.trim(),
            password
        ).addOnSuccessListener {
            onResult(Result.success(Unit))
        }.addOnFailureListener {
            onResult(Result.failure(it))
        }
    }

    private fun Device.toFirestoreMap(
        floorId: String,
        usageMinutesThisWeek: Int? = null,
        safetyShutdownsThisMonth: Int? = null
    ): Map<String, Any> {
        val data =
            mutableMapOf<String, Any>(
            "floorId" to floorId,
            "name" to name,
            "type" to type.name,
            "status" to status.name,
            "room" to room,
            "gridX" to gridX,
            "gridY" to gridY,
            "brightness" to brightness,
            "scheduleEnabled" to scheduleEnabled,
            "scheduleStart" to scheduleStart,
            "scheduleEnd" to scheduleEnd,
            "maxOnDurationMinutes" to maxOnDurationMinutes,
            "switches" to switches
        )

        if (turnedOnAt > 0L) {
            data["turnedOnAt"] = turnedOnAt
        } else {
            data["turnedOnAt"] = FieldValue.delete()
        }

        usageMinutesThisWeek?.let {
            data["usageMinutesThisWeek"] = it
        }

        safetyShutdownsThisMonth?.let {
            data["safetyShutdownsThisMonth"] = it
        }

        return data
    }

    private fun Map<String, Any>.toDevice(
        id: String
    ): Device? {
        val name =
            get("name") as? String
                ?: return null

        val room =
            get("room") as? String
                ?: return null

        val type =
            enumValueOrNull<DeviceType>(
                get("type") as? String
            ) ?: DeviceType.OUTLET

        val status =
            enumValueOrNull<DeviceStatus>(
                get("status") as? String
            ) ?: DeviceStatus.OFF

        return Device(
            id = id,
            name = name,
            type = type,
            status = status,
            room = room,
            gridX = (get("gridX") as? Number)?.toInt() ?: 0,
            gridY = (get("gridY") as? Number)?.toInt() ?: 0,
            brightness = (get("brightness") as? Number)?.toInt() ?: 100,
            scheduleEnabled = get("scheduleEnabled") as? Boolean ?: false,
            scheduleStart = get("scheduleStart") as? String ?: "18:00",
            scheduleEnd = get("scheduleEnd") as? String ?: "23:00",
            maxOnDurationMinutes =
                (get("maxOnDurationMinutes") as? Number)?.toInt() ?: 15,
            switches =
                (get("switches") as? Map<*, *>)
                    ?.mapNotNull { (key, value) ->
                        val switchName =
                            key as? String
                                ?: return@mapNotNull null

                        val isOn =
                            value as? Boolean
                                ?: return@mapNotNull null

                        switchName to isOn
                    }
                    ?.toMap()
                    .orEmpty(),
            turnedOnAt =
                (get("turnedOnAt") as? Number)?.toLong() ?: 0L
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrNull(
        value: String?
    ): T? {
        return value?.let {
            runCatching {
                enumValueOf<T>(it)
            }.getOrNull()
        }
    }

    private fun Map<String, Any>.toAppNotification(
        id: String
    ): AppNotification? {
        return AppNotification(
            id = id,
            title = get("title") as? String ?: return null,
            description = get("description") as? String ?: return null,
            time = get("time") as? String ?: "",
            important = get("important") as? Boolean ?: false
        )
    }

    private fun AppNotification.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "title" to title,
            "description" to description,
            "time" to time,
            "important" to important
        )
    }

    private fun Map<String, Any>.toUserSettings(): UserSettings {
        return UserSettings(
            safetyAlerts = get("safetyAlerts") as? Boolean ?: true,
            deviceAlerts = get("deviceAlerts") as? Boolean ?: true,
            scheduleAlerts = get("scheduleAlerts") as? Boolean ?: false
        )
    }

    private fun UserSettings.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "safetyAlerts" to safetyAlerts,
            "deviceAlerts" to deviceAlerts,
            "scheduleAlerts" to scheduleAlerts
        )
    }

    private fun Int.toUsageLabel(): String {
        val hours =
            this / 60

        val minutes =
            this % 60

        return if (hours == 0) {
            "${minutes}m"
        } else {
            "${hours}h ${minutes}m"
        }
    }

    private fun defaultUsageMinutes(
        deviceId: String
    ): Int {
        return when (deviceId) {
            "device1" -> 480
            "device8" -> 360
            "device4" -> 300
            "device5" -> 180
            "device7" -> 145
            else -> 75
        }
    }

    private fun defaultSafetyShutdowns(
        deviceId: String
    ): Int {
        return if (deviceId == "device3") {
            2
        } else {
            0
        }
    }

    private fun defaultNotifications(): List<AppNotification> {
        return listOf(
            AppNotification(
                id = "notification1",
                title = "Iron automatically switched off",
                description = "Maximum active duration of 15 minutes was reached.",
                time = "10:35 AM",
                important = true
            ),
            AppNotification(
                id = "notification2",
                title = "Garage Outlet disconnected",
                description = "The device is currently unreachable.",
                time = "9:20 AM",
                important = true
            ),
            AppNotification(
                id = "notification3",
                title = "Living Room schedule",
                description = "Light switched on automatically.",
                time = "Yesterday",
                important = false
            )
        )
    }

    const val GROUND_FLOOR_ID = "ground"
    const val FIRST_FLOOR_ID = "first"
}
