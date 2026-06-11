package com.yesoulsuite.tracker.ble

import java.util.UUID

object BleUuids {
    val CLIENT_CHARACTERISTIC_CONFIG: UUID = uuid16("2902")

    val FITNESS_MACHINE_SERVICE: UUID = uuid16("1826")
    val FITNESS_MACHINE_FEATURE: UUID = uuid16("2ACC")
    val INDOOR_BIKE_DATA: UUID = uuid16("2AD2")
    val TRAINING_STATUS: UUID = uuid16("2AD3")
    val SUPPORTED_SPEED_RANGE: UUID = uuid16("2AD4")
    val SUPPORTED_RESISTANCE_LEVEL_RANGE: UUID = uuid16("2AD6")
    val SUPPORTED_HEART_RATE_RANGE: UUID = uuid16("2AD7")
    val SUPPORTED_POWER_RANGE: UUID = uuid16("2AD8")
    val FITNESS_MACHINE_CONTROL_POINT: UUID = uuid16("2AD9")
    val FITNESS_MACHINE_STATUS: UUID = uuid16("2ADA")

    val YESOUL_VENDOR_WRITE: UUID = UUID.fromString("d18d2c10-c44c-11e8-a355-529269fb1459")

    val VENDOR_FFF0_SERVICE: UUID = uuid16("FFF0")
    val VENDOR_FFF1: UUID = uuid16("FFF1")
    val VENDOR_FFF2: UUID = uuid16("FFF2")
    val VENDOR_FFF3: UUID = uuid16("FFF3")
    val VENDOR_FFF4: UUID = uuid16("FFF4")
    val VENDOR_FFF5: UUID = uuid16("FFF5")

    val VENDOR_FAB0_SERVICE: UUID = uuid16("FAB0")
    val VENDOR_FAB1: UUID = uuid16("FAB1")
    val VENDOR_FAB2: UUID = uuid16("FAB2")
    val VENDOR_FAB3: UUID = uuid16("FAB3")

    val CYCLING_SPEED_AND_CADENCE_SERVICE: UUID = uuid16("1816")
    val CSC_MEASUREMENT: UUID = uuid16("2A5B")

    val HEART_RATE_SERVICE: UUID = uuid16("180D")
    val HEART_RATE_MEASUREMENT: UUID = uuid16("2A37")

    val DEVICE_INFORMATION_SERVICE: UUID = uuid16("180A")

    fun uuid16(shortUuid: String): UUID {
        return UUID.fromString("0000$shortUuid-0000-1000-8000-00805f9b34fb")
    }
}
