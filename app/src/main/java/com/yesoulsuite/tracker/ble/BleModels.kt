package com.yesoulsuite.tracker.ble

data class BleDeviceItem(
    val name: String?,
    val address: String,
    val rssi: Int,
    val serviceUuids: List<String>,
    val lastSeenMillis: Long,
)

data class BleCapabilities(
    val hasFitnessMachineService: Boolean = false,
    val hasIndoorBikeData: Boolean = false,
    val hasFitnessMachineControlPoint: Boolean = false,
    val hasCyclingSpeedCadence: Boolean = false,
    val hasHeartRate: Boolean = false,
    val canSetResistance: Boolean = false,
)

data class BleConnectionState(
    val isScanning: Boolean = false,
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val deviceName: String? = null,
    val deviceAddress: String? = null,
    val status: String = "Idle",
    val capabilities: BleCapabilities = BleCapabilities(),
)

data class BikeMetrics(
    val timestampMillis: Long = System.currentTimeMillis(),
    val speedKmh: Double? = null,
    val cadenceRpm: Double? = null,
    val powerWatts: Int? = null,
    val heartRateBpm: Int? = null,
    val resistanceLevel: Double? = null,
    val distanceMeters: Double? = null,
    val calories: Int? = null,
    val source: String? = null,
) {
    fun merge(newer: BikeMetrics): BikeMetrics {
        return BikeMetrics(
            timestampMillis = newer.timestampMillis,
            speedKmh = newer.speedKmh ?: speedKmh,
            cadenceRpm = newer.cadenceRpm ?: cadenceRpm,
            powerWatts = newer.powerWatts ?: powerWatts,
            heartRateBpm = newer.heartRateBpm ?: heartRateBpm,
            resistanceLevel = newer.resistanceLevel ?: resistanceLevel,
            distanceMeters = newer.distanceMeters ?: distanceMeters,
            calories = newer.calories ?: calories,
            source = newer.source ?: source,
        )
    }
}

data class GattDiagnosticSnapshot(
    val createdAtMillis: Long,
    val deviceName: String?,
    val deviceAddress: String?,
    val text: String,
)
