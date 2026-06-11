package com.yesoulsuite.tracker.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BleParsersTest {
    @Test
    fun parsesFtmsIndoorBikeDataWithStandardCadenceFlag() {
        val value = byteArrayOf(
            0x44, 0x02,
            0xC4.toByte(), 0x09,
            0xA0.toByte(), 0x00,
            0xB4.toByte(), 0x00,
            0x96.toByte(),
        )

        val metrics = FtmsIndoorBikeParser.parse(value, timestampMillis = 1L)

        assertEquals(25.0, metrics?.speedKmh ?: 0.0, 0.001)
        assertEquals(80.0, metrics?.cadenceRpm ?: 0.0, 0.001)
        assertEquals(180, metrics?.powerWatts)
        assertEquals(150, metrics?.heartRateBpm)
    }

    @Test
    fun parsesShortYesoulSpeedPacketFromDiagnostics() {
        val value = byteArrayOf(
            0x00, 0x08,
            0x04, 0x0C,
            0x43, 0x00,
        )

        val metrics = FtmsIndoorBikeParser.parse(value, timestampMillis = 1L)

        assertEquals(30.76, metrics?.speedKmh ?: 0.0, 0.001)
    }

    @Test
    fun parsesLongYesoulMetricsPacketFromDiagnostics() {
        val value = byteArrayOf(
            0xF5.toByte(), 0x01,
            0x74, 0x00,
            0x50, 0x01, 0x00,
            0x25, 0x00,
            0x64, 0x00,
            0x22, 0x00,
            0x03, 0x00,
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
        )

        val metrics = FtmsIndoorBikeParser.parse(value, timestampMillis = 1L)

        assertEquals(58.0, metrics?.cadenceRpm ?: 0.0, 0.001)
        assertEquals(336.0, metrics?.distanceMeters ?: 0.0, 0.001)
        assertEquals(3.7, metrics?.resistanceLevel ?: 0.0, 0.001)
        assertEquals(100, metrics?.powerWatts)
        assertEquals(3, metrics?.calories)
    }

    @Test
    fun parsesCscCadenceFromCrankDeltas() {
        val parser = CscMeasurementParser()

        parser.parse(byteArrayOf(0x02, 100, 0, 0, 4), timestampMillis = 1L)
        val metrics = parser.parse(byteArrayOf(0x02, 102, 0, 0, 8), timestampMillis = 2L)

        assertEquals(120.0, metrics?.cadenceRpm ?: 0.0, 0.001)
    }

    @Test
    fun parsesHeartRateUint8AndUint16() {
        val eightBit = HeartRateMeasurementParser.parse(byteArrayOf(0x00, 72), timestampMillis = 1L)
        val sixteenBit = HeartRateMeasurementParser.parse(byteArrayOf(0x01, 0x2C, 0x01), timestampMillis = 1L)

        assertEquals(72, eightBit?.heartRateBpm)
        assertEquals(300, sixteenBit?.heartRateBpm)
    }

    @Test
    fun parsesFtmsResistanceTargetFeatureBit() {
        val supported = byteArrayOf(0, 0, 0, 0, 0x04, 0, 0, 0)
        val unsupported = byteArrayOf(0, 0, 0, 0, 0x02, 0, 0, 0)

        assertTrue(supportsFtmsResistanceTargetSetting(supported))
        assertFalse(supportsFtmsResistanceTargetSetting(unsupported))
    }

    @Test
    fun parsesYesoulFtmsStatusResistanceLevel() {
        val metrics = FtmsStatusParser.parse(byteArrayOf(0x07, 0x25), timestampMillis = 1L)

        assertEquals(3.7, metrics?.resistanceLevel ?: 0.0, 0.001)
    }
}
