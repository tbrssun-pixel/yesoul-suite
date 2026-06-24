package com.owlbike.v1tracker.ui

import com.owlbike.v1tracker.data.WorkoutSessionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class HomePrimaryAction {
    OpenRide,
    ReconnectLast,
    OpenFirstSetupConnect,
    WaitForConnection,
}

internal enum class HomeWorkoutAction {
    ContinueRide,
    StartRide,
    WaitForConnection,
    StartNeedsConnection,
}

internal data class HomeDashboardAggregates(
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Long,
    val workoutDays: Int,
    val totalCalories: Int,
)

fun resolveHomePrimaryAction(
    isConnected: Boolean,
    isConnecting: Boolean,
    hasRememberedDevice: Boolean,
): HomePrimaryAction {
    return when {
        isConnected -> HomePrimaryAction.OpenRide
        isConnecting -> HomePrimaryAction.WaitForConnection
        hasRememberedDevice -> HomePrimaryAction.ReconnectLast
        else -> HomePrimaryAction.OpenFirstSetupConnect
    }
}

internal fun resolveHomeWorkoutAction(
    isWorkoutRunning: Boolean,
    isConnected: Boolean,
    isConnecting: Boolean,
): HomeWorkoutAction {
    return when {
        isWorkoutRunning -> HomeWorkoutAction.ContinueRide
        isConnecting -> HomeWorkoutAction.WaitForConnection
        isConnected -> HomeWorkoutAction.StartRide
        else -> HomeWorkoutAction.StartNeedsConnection
    }
}

internal fun buildHomeDashboardAggregates(
    sessions: List<WorkoutSessionEntity>,
    zoneId: ZoneId = ZoneId.systemDefault(),
): HomeDashboardAggregates {
    val finished = sessions.filter { it.state == "finished" && it.endTimeMillis != null }
    val workoutDays = finished
        .map { session -> localDate(session.startTimeMillis, zoneId) }
        .toSet()
        .size
    return HomeDashboardAggregates(
        totalDistanceMeters = finished.mapNotNull { it.totalDistanceMeters }.sum(),
        totalDurationSeconds = finished.sumOf { session ->
            val end = session.endTimeMillis ?: session.startTimeMillis
            ((end - session.startTimeMillis) / 1000L).coerceAtLeast(0L)
        },
        workoutDays = workoutDays,
        totalCalories = finished.mapNotNull { it.totalCalories }.sum(),
    )
}

private fun localDate(timestampMillis: Long, zoneId: ZoneId): LocalDate {
    return Instant.ofEpochMilli(timestampMillis).atZone(zoneId).toLocalDate()
}
