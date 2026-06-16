package com.owlbike.v1tracker.history

import com.owlbike.v1tracker.data.WorkoutSessionEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class HistoryWeekGroup(
    val weekStartMillis: Long,
    val weekEndMillis: Long,
    val sessions: List<WorkoutSessionEntity>,
    val isExpanded: Boolean,
)

fun buildHistoryWeekGroups(
    sessions: List<WorkoutSessionEntity>,
    expandedWeekStarts: Set<Long>,
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<HistoryWeekGroup> {
    return sessions
        .groupBy { session -> weekStartMillis(session.startTimeMillis, zoneId) }
        .toSortedMap(compareByDescending { it })
        .map { (weekStart, weekSessions) ->
            HistoryWeekGroup(
                weekStartMillis = weekStart,
                weekEndMillis = weekStart + WEEK_MILLIS - 1L,
                sessions = weekSessions.sortedByDescending { it.startTimeMillis },
                isExpanded = weekStart in expandedWeekStarts,
            )
        }
}

fun defaultExpandedHistoryWeekStarts(
    sessions: List<WorkoutSessionEntity>,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Set<Long> {
    return sessions.maxByOrNull { it.startTimeMillis }
        ?.let { setOf(weekStartMillis(it.startTimeMillis, zoneId)) }
        ?: emptySet()
}

fun knownHistoryWeekStarts(
    sessions: List<WorkoutSessionEntity>,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Set<Long> {
    return sessions.map { weekStartMillis(it.startTimeMillis, zoneId) }.toSet()
}

fun weekStartMillis(timeMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Long {
    val date = Instant.ofEpochMilli(timeMillis).atZone(zoneId).toLocalDate()
    val weekStart = date.withPreviousOrSame(DayOfWeek.MONDAY)
    return weekStart.atStartOfDay(zoneId).toInstant().toEpochMilli()
}

private fun LocalDate.withPreviousOrSame(dayOfWeek: DayOfWeek): LocalDate {
    val diff = (dayOfWeek.value - this.dayOfWeek.value + 7) % 7
    return minusDays(if (diff == 0) 0L else (7 - diff).toLong())
}

private const val WEEK_MILLIS = 7L * 24L * 60L * 60L * 1000L
