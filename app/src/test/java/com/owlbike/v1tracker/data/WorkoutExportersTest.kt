package com.owlbike.v1tracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutExportersTest {
    private val session = WorkoutSessionEntity(
        id = "session-1",
        startTimeMillis = 1_700_000_000_000L,
        endTimeMillis = 1_700_000_060_000L,
        state = "finished",
        deviceName = "YSV100637",
        deviceAddress = "00:11:22:33:44:55",
        totalDistanceMeters = 512.4,
        totalCalories = 42,
        averagePowerWatts = 101.0,
        averageCadenceRpm = 72.0,
        averageHeartRateBpm = 130.0,
        sampleCount = 2,
    )

    private val samples = listOf(
        WorkoutSampleEntity(
            sessionId = "session-1",
            timestampMillis = 1_700_000_000_000L,
            elapsedSeconds = 0,
            speedKmh = 22.1234,
            cadenceRpm = 70.2,
            powerWatts = 98,
            heartRateBpm = 128,
            resistanceLevel = 3.7,
            distanceMeters = 10.5,
            calories = 1,
        ),
        WorkoutSampleEntity(
            sessionId = "session-1",
            timestampMillis = 1_700_000_001_000L,
            elapsedSeconds = 1,
            speedKmh = null,
            cadenceRpm = 72.0,
            powerWatts = 104,
            heartRateBpm = null,
            resistanceLevel = null,
            distanceMeters = 16.0,
            calories = 2,
        ),
    )

    @Test
    fun exportsCsvWithStableHeaderAndBlankNulls() {
        val csv = WorkoutExporters.toCsv(samples)

        val rows = csv.trim().lines()
        assertEquals(
            "timestamp_iso,elapsed_seconds,speed_kmh,cadence_rpm,power_watts,heart_rate_bpm," +
                "resistance_level,distance_meters,calories",
            rows[0],
        )
        assertEquals("2023-11-14T22:13:20Z,0,22.123,70.2,98,128,3.7,10.5,1", rows[1])
        assertEquals("2023-11-14T22:13:21Z,1,,72.0,104,,,16.0,2", rows[2])
    }

    @Test
    fun exportsTcxWithCyclingSamplesAndPowerExtension() {
        val tcx = WorkoutExporters.toTcx(session, samples)

        assertTrue(tcx.contains("""<Activity Sport="Biking">"""))
        assertTrue(tcx.contains("<TotalTimeSeconds>60</TotalTimeSeconds>"))
        assertTrue(tcx.contains("<DistanceMeters>512.4</DistanceMeters>"))
        assertTrue(tcx.contains("<Time>2023-11-14T22:13:20Z</Time>"))
        assertTrue(tcx.contains("<Cadence>70</Cadence>"))
        assertTrue(tcx.contains("<Value>128</Value>"))
        assertTrue(tcx.contains("<ns3:Watts>98</ns3:Watts>"))
    }

    @Test
    fun namesExportFilesFromSessionStart() {
        assertEquals("owl-bike-20231114-221320.csv", WorkoutExporters.fileName(session, WorkoutExportFormat.Csv))
        assertEquals("owl-bike-20231114-221320.tcx", WorkoutExporters.fileName(session, WorkoutExportFormat.Tcx))
    }
}
