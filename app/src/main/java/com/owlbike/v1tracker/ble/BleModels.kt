package com.owlbike.v1tracker.ble

import java.util.Locale

enum class BleDeviceType(val scanSortRank: Int) {
    Trainer(0),
    CyclingDevice(1),
    Other(2),
}

object BleDeviceClassifier {
    fun classify(name: String?, serviceUuids: List<String>): BleDeviceType {
        return when {
            hasFitnessMachineService(serviceUuids) || looksLikeTrainerName(name) -> BleDeviceType.Trainer
            hasCyclingService(serviceUuids) -> BleDeviceType.CyclingDevice
            else -> BleDeviceType.Other
        }
    }

    private fun hasFitnessMachineService(serviceUuids: List<String>): Boolean {
        return serviceUuids.hasUuid(BleUuids.FITNESS_MACHINE_SERVICE.toString(), "1826")
    }

    private fun hasCyclingService(serviceUuids: List<String>): Boolean {
        return serviceUuids.hasUuid(BleUuids.CYCLING_SPEED_AND_CADENCE_SERVICE.toString(), "1816") ||
            serviceUuids.hasUuid(BleUuids.CYCLING_POWER_SERVICE.toString(), "1818")
    }

    private fun looksLikeTrainerName(name: String?): Boolean {
        val normalized = name?.lowercase(Locale.US).orEmpty()
        return "yesoul" in normalized ||
            "ysv" in normalized ||
            "bike" in normalized ||
            "cycle" in normalized ||
            "indoor" in normalized
    }

    private fun List<String>.hasUuid(fullUuid: String, shortUuid: String): Boolean {
        return any { uuid ->
            uuid.equals(fullUuid, ignoreCase = true) ||
                uuid.equals(shortUuid, ignoreCase = true)
        }
    }
}

object RememberedEquipmentClassifier {
    fun classify(
        name: String?,
        serviceUuidsText: String?,
        nearbyDevice: BleDeviceItem? = null,
    ): BleDeviceType {
        if (nearbyDevice != null && nearbyDevice.type != BleDeviceType.Other) {
            return nearbyDevice.type
        }
        return BleDeviceClassifier.classify(name, parseServiceUuids(serviceUuidsText))
    }

    fun isLikelyTrainer(
        name: String?,
        serviceUuidsText: String?,
        nearbyDevice: BleDeviceItem? = null,
    ): Boolean = classify(name, serviceUuidsText, nearbyDevice) == BleDeviceType.Trainer

    fun canRememberConnection(connection: BleConnectionState): Boolean {
        return connection.servicesDiscovered &&
            connection.capabilities.hasIndoorBikeData &&
            !connection.deviceAddress.isNullOrBlank()
    }

    fun parseServiceUuids(serviceUuidsText: String?): List<String> {
        return serviceUuidsText
            ?.lines()
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
    }
}

data class BleDeviceItem(
    val name: String?,
    val address: String,
    val rssi: Int,
    val serviceUuids: List<String>,
    val type: BleDeviceType = BleDeviceType.Other,
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
    val servicesDiscovered: Boolean = false,
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
