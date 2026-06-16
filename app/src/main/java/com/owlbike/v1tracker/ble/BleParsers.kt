package com.owlbike.v1tracker.ble

object FtmsIndoorBikeParser {
    private const val FLAG_MORE_DATA = 0
    private const val FLAG_AVERAGE_SPEED = 1
    private const val FLAG_INSTANT_CADENCE = 2
    private const val FLAG_AVERAGE_CADENCE = 3
    private const val FLAG_TOTAL_DISTANCE = 4
    private const val FLAG_RESISTANCE = 5
    private const val FLAG_INSTANT_POWER = 6
    private const val FLAG_AVERAGE_POWER = 7
    private const val FLAG_EXPENDED_ENERGY = 8
    private const val FLAG_HEART_RATE = 9
    private const val FLAG_MET = 10
    private const val FLAG_ELAPSED_TIME = 11
    private const val FLAG_REMAINING_TIME = 12

    fun parse(value: ByteArray, timestampMillis: Long = System.currentTimeMillis()): BikeMetrics? {
        return parseWithResistanceWidth(value, timestampMillis, resistanceBytes = 2)
            ?: parseWithResistanceWidth(value, timestampMillis, resistanceBytes = 1)
    }

    private fun parseWithResistanceWidth(
        value: ByteArray,
        timestampMillis: Long,
        resistanceBytes: Int,
    ): BikeMetrics? {
        if (value.size < 2) return null
        val reader = LittleEndianReader(value)
        val flags = reader.uint16() ?: return null

        var speedKmh: Double? = null
        var cadenceRpm: Double? = null
        var powerWatts: Int? = null
        var heartRateBpm: Int? = null
        var resistanceLevel: Double? = null
        var distanceMeters: Double? = null
        var calories: Int? = null

        if (!flags.hasBit(FLAG_MORE_DATA)) {
            speedKmh = reader.uint16()?.let { it / 100.0 }
        }
        if (flags.hasBit(FLAG_AVERAGE_SPEED)) {
            reader.skip(2)
        }
        if (flags.hasBit(FLAG_INSTANT_CADENCE)) {
            cadenceRpm = reader.uint16()?.let { it / 2.0 }
        }
        if (flags.hasBit(FLAG_AVERAGE_CADENCE)) {
            reader.skip(2)
        }
        if (flags.hasBit(FLAG_TOTAL_DISTANCE)) {
            distanceMeters = reader.uint24()?.toDouble()
        }
        if (flags.hasBit(FLAG_RESISTANCE)) {
            resistanceLevel = when (resistanceBytes) {
                1 -> reader.uint8()?.let { it / 10.0 }
                else -> reader.sint16()?.let { it / 10.0 }
            }
        }
        if (flags.hasBit(FLAG_INSTANT_POWER)) {
            powerWatts = reader.sint16()
        }
        if (flags.hasBit(FLAG_AVERAGE_POWER)) {
            reader.skip(2)
        }
        if (flags.hasBit(FLAG_EXPENDED_ENERGY)) {
            calories = reader.uint16()
            reader.skip(3)
        }
        if (flags.hasBit(FLAG_HEART_RATE)) {
            heartRateBpm = reader.uint8()
        }
        if (flags.hasBit(FLAG_MET)) {
            reader.skip(1)
        }
        if (flags.hasBit(FLAG_ELAPSED_TIME)) {
            reader.skip(2)
        }
        if (flags.hasBit(FLAG_REMAINING_TIME)) {
            reader.skip(2)
        }
        if (reader.failed || reader.position != value.size) return null

        return BikeMetrics(
            timestampMillis = timestampMillis,
            speedKmh = speedKmh,
            cadenceRpm = cadenceRpm,
            powerWatts = powerWatts,
            heartRateBpm = heartRateBpm,
            resistanceLevel = resistanceLevel,
            distanceMeters = distanceMeters,
            calories = calories,
            source = if (flags.hasBit(FLAG_RESISTANCE) && resistanceBytes == 2) "YESOUL legacy FTMS" else "FTMS",
        )
    }
}

class CscMeasurementParser(
    private val wheelCircumferenceMeters: Double = 2.096,
) {
    private var lastWheelRevolutions: Long? = null
    private var lastWheelEventTime: Int? = null
    private var lastCrankRevolutions: Int? = null
    private var lastCrankEventTime: Int? = null

    fun parse(value: ByteArray, timestampMillis: Long = System.currentTimeMillis()): BikeMetrics? {
        if (value.isEmpty()) return null
        val reader = LittleEndianReader(value)
        val flags = reader.uint8() ?: return null
        val hasWheel = flags.hasBit(0)
        val hasCrank = flags.hasBit(1)

        var speedKmh: Double? = null
        var distanceMeters: Double? = null
        var cadenceRpm: Double? = null

        if (hasWheel) {
            val cumulativeRevolutions = reader.uint32() ?: return null
            val eventTime = reader.uint16() ?: return null
            distanceMeters = cumulativeRevolutions * wheelCircumferenceMeters
            val previousRevolutions = lastWheelRevolutions
            val previousEventTime = lastWheelEventTime
            if (previousRevolutions != null && previousEventTime != null) {
                val revolutionsDelta = cumulativeRevolutions - previousRevolutions
                val eventDelta = eventTime.delta16(previousEventTime)
                if (revolutionsDelta >= 0 && eventDelta > 0) {
                    val seconds = eventDelta / 1024.0
                    speedKmh = (revolutionsDelta * wheelCircumferenceMeters / seconds) * 3.6
                }
            }
            lastWheelRevolutions = cumulativeRevolutions
            lastWheelEventTime = eventTime
        }

        if (hasCrank) {
            val cumulativeRevolutions = reader.uint16() ?: return null
            val eventTime = reader.uint16() ?: return null
            val previousRevolutions = lastCrankRevolutions
            val previousEventTime = lastCrankEventTime
            if (previousRevolutions != null && previousEventTime != null) {
                val revolutionsDelta = cumulativeRevolutions.delta16(previousRevolutions)
                val eventDelta = eventTime.delta16(previousEventTime)
                if (eventDelta > 0) {
                    val seconds = eventDelta / 1024.0
                    cadenceRpm = revolutionsDelta / seconds * 60.0
                }
            }
            lastCrankRevolutions = cumulativeRevolutions
            lastCrankEventTime = eventTime
        }

        return BikeMetrics(
            timestampMillis = timestampMillis,
            speedKmh = speedKmh,
            cadenceRpm = cadenceRpm,
            distanceMeters = distanceMeters,
            source = "CSC",
        )
    }
}

object HeartRateMeasurementParser {
    fun parse(value: ByteArray, timestampMillis: Long = System.currentTimeMillis()): BikeMetrics? {
        if (value.size < 2) return null
        val reader = LittleEndianReader(value)
        val flags = reader.uint8() ?: return null
        val heartRate = if (flags.hasBit(0)) {
            reader.uint16()
        } else {
            reader.uint8()
        } ?: return null
        return BikeMetrics(
            timestampMillis = timestampMillis,
            heartRateBpm = heartRate,
            source = "HRS",
        )
    }
}

object FtmsStatusParser {
    private const val TARGET_RESISTANCE_LEVEL_CHANGED = 0x07

    fun parse(value: ByteArray, timestampMillis: Long = System.currentTimeMillis()): BikeMetrics? {
        if (value.size < 2) return null
        val opCode = value[0].toInt() and 0xFF
        if (opCode != TARGET_RESISTANCE_LEVEL_CHANGED) return null
        val rawResistance = if (value.size >= 3) {
            (value[1].toInt() and 0xFF) or ((value[2].toInt() and 0xFF) shl 8)
        } else {
            value[1].toInt() and 0xFF
        }
        val signed = if (rawResistance and 0x8000 != 0) rawResistance - 0x10000 else rawResistance
        return BikeMetrics(
            timestampMillis = timestampMillis,
            resistanceLevel = signed / 10.0,
            source = "FTMS Status",
        )
    }
}

fun supportsFtmsResistanceTargetSetting(value: ByteArray): Boolean {
    if (value.size < 8) return false
    val targetSettingFeatures =
        (value[4].toInt() and 0xFF) or
            ((value[5].toInt() and 0xFF) shl 8) or
            ((value[6].toInt() and 0xFF) shl 16) or
            ((value[7].toInt() and 0xFF) shl 24)
    return targetSettingFeatures.hasBit(2)
}

internal class LittleEndianReader(private val value: ByteArray) {
    private var index = 0
    var failed: Boolean = false
        private set
    val position: Int
        get() = index

    fun uint8(): Int? {
        if (index >= value.size) {
            failed = true
            return null
        }
        return value[index++].toInt() and 0xFF
    }

    fun uint16(): Int? {
        val b0 = uint8() ?: return null
        val b1 = uint8() ?: return null
        return b0 or (b1 shl 8)
    }

    fun sint16(): Int? {
        val unsigned = uint16() ?: return null
        return if (unsigned and 0x8000 != 0) unsigned - 0x10000 else unsigned
    }

    fun uint24(): Int? {
        val b0 = uint8() ?: return null
        val b1 = uint8() ?: return null
        val b2 = uint8() ?: return null
        return b0 or (b1 shl 8) or (b2 shl 16)
    }

    fun uint32(): Long? {
        val b0 = uint8() ?: return null
        val b1 = uint8() ?: return null
        val b2 = uint8() ?: return null
        val b3 = uint8() ?: return null
        return (b0.toLong() or (b1.toLong() shl 8) or (b2.toLong() shl 16) or (b3.toLong() shl 24)) and 0xFFFF_FFFFL
    }

    fun skip(count: Int) {
        if (index + count > value.size) {
            failed = true
            index = value.size
        } else {
            index += count
        }
    }
}

private fun Int.hasBit(bit: Int): Boolean = this and (1 shl bit) != 0

private fun Int.delta16(previous: Int): Int {
    return if (this >= previous) this - previous else this + 0x10000 - previous
}
