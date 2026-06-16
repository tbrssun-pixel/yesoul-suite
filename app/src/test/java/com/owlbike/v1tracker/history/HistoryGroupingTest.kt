package com.owlbike.v1tracker.history

import com.owlbike.v1tracker.data.WorkoutSessionEntity
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryGroupingTest {
    private val zone = ZoneId.of("UTC")

    @Test
    fun groupsSessionsByMondayWeekStart() {
        val monday = millis(2026, 6, 15)
        val wednesday = millis(2026, 6, 17)
        val nextMonday = millis(2026, 6, 22)

        val groups = buildHistoryWeekGroups(
            sessions = listOf(
                session("wed", wednesday),
                session("next", nextMonday),
                session("mon", monday),
            ),
            expandedWeekStarts = setOf(weekStartMillis(monday, zone)),
            zoneId = zone,
        )

        assertEquals(2, groups.size)
        assertEquals(weekStartMillis(nextMonday, zone), groups[0].weekStartMillis)
        assertFalse(groups[0].isExpanded)
        assertEquals(weekStartMillis(monday, zone), groups[1].weekStartMillis)
        assertTrue(groups[1].isExpanded)
        assertEquals(listOf("wed", "mon"), groups[1].sessions.map { it.id })
    }

    @Test
    fun defaultExpandedWeekIsMostRecentSessionWeek() {
        val older = millis(2026, 6, 15)
        val recent = millis(2026, 6, 25)

        assertEquals(
            setOf(weekStartMillis(recent, zone)),
            defaultExpandedHistoryWeekStarts(listOf(session("older", older), session("recent", recent)), zone),
        )
    }

    private fun millis(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day).atStartOfDay(zone).toInstant().toEpochMilli()
    }

    private fun session(id: String, startMillis: Long): WorkoutSessionEntity {
        return WorkoutSessionEntity(
            id = id,
            startTimeMillis = startMillis,
            endTimeMillis = startMillis + 1_800_000L,
            state = "finished",
            deviceName = "Bike",
            deviceAddress = "00:00",
            totalDistanceMeters = 1_000.0,
            totalCalories = 50,
            averagePowerWatts = null,
            averageCadenceRpm = null,
            averageHeartRateBpm = null,
            sampleCount = 1,
        )
    }
}
