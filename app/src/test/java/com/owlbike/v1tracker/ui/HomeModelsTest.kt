package com.owlbike.v1tracker.ui

import com.owlbike.v1tracker.data.WorkoutSessionEntity
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeModelsTest {
    @Test
    fun connectedTrainerOpensRide() {
        assertEquals(
            HomePrimaryAction.OpenRide,
            resolveHomePrimaryAction(
                isConnected = true,
                isConnecting = false,
                hasRememberedDevice = false,
            ),
        )
    }

    @Test
    fun rememberedTrainerReconnectsLast() {
        assertEquals(
            HomePrimaryAction.ReconnectLast,
            resolveHomePrimaryAction(
                isConnected = false,
                isConnecting = false,
                hasRememberedDevice = true,
            ),
        )
    }

    @Test
    fun noTrainerStartsFirstSetupConnect() {
        assertEquals(
            HomePrimaryAction.OpenFirstSetupConnect,
            resolveHomePrimaryAction(
                isConnected = false,
                isConnecting = false,
                hasRememberedDevice = false,
            ),
        )
    }

    @Test
    fun connectingTrainerWaitsForConnection() {
        assertEquals(
            HomePrimaryAction.WaitForConnection,
            resolveHomePrimaryAction(
                isConnected = false,
                isConnecting = true,
                hasRememberedDevice = true,
            ),
        )
    }

    @Test
    fun runningWorkoutContinuesFromHomeCta() {
        assertEquals(
            HomeWorkoutAction.ContinueRide,
            resolveHomeWorkoutAction(
                isWorkoutRunning = true,
                isConnected = true,
                isConnecting = false,
            ),
        )
    }

    @Test
    fun connectedIdleWorkoutStartsFromHomeCta() {
        assertEquals(
            HomeWorkoutAction.StartRide,
            resolveHomeWorkoutAction(
                isWorkoutRunning = false,
                isConnected = true,
                isConnecting = false,
            ),
        )
    }

    @Test
    fun connectingWorkoutCtaWaitsForConnection() {
        assertEquals(
            HomeWorkoutAction.WaitForConnection,
            resolveHomeWorkoutAction(
                isWorkoutRunning = false,
                isConnected = false,
                isConnecting = true,
            ),
        )
    }

    @Test
    fun disconnectedWorkoutCtaRequiresConnection() {
        assertEquals(
            HomeWorkoutAction.StartNeedsConnection,
            resolveHomeWorkoutAction(
                isWorkoutRunning = false,
                isConnected = false,
                isConnecting = false,
            ),
        )
    }

    @Test
    fun homeDashboardAggregatesAreEmptyWithoutFinishedSessions() {
        val aggregates = buildHomeDashboardAggregates(
            sessions = listOf(
                session(
                    state = "running",
                    start = millis(2026, 6, 20, 10, 0),
                    end = null,
                    distance = 5_000.0,
                    calories = 120,
                ),
            ),
            zoneId = testZone,
        )

        assertEquals(0.0, aggregates.totalDistanceMeters, 0.0)
        assertEquals(0L, aggregates.totalDurationSeconds)
        assertEquals(0, aggregates.workoutDays)
        assertEquals(0, aggregates.totalCalories)
    }

    @Test
    fun homeDashboardAggregatesSumFinishedDistanceCaloriesAndDuration() {
        val aggregates = buildHomeDashboardAggregates(
            sessions = listOf(
                session(
                    start = millis(2026, 6, 20, 10, 0),
                    end = millis(2026, 6, 20, 10, 30),
                    distance = 4_000.0,
                    calories = 100,
                ),
                session(
                    start = millis(2026, 6, 21, 11, 0),
                    end = millis(2026, 6, 21, 11, 45),
                    distance = 6_500.0,
                    calories = 160,
                ),
            ),
            zoneId = testZone,
        )

        assertEquals(10_500.0, aggregates.totalDistanceMeters, 0.0)
        assertEquals(4_500L, aggregates.totalDurationSeconds)
        assertEquals(2, aggregates.workoutDays)
        assertEquals(260, aggregates.totalCalories)
    }

    @Test
    fun homeDashboardAggregatesCountDistinctWorkoutDays() {
        val aggregates = buildHomeDashboardAggregates(
            sessions = listOf(
                session(
                    start = millis(2026, 6, 20, 8, 0),
                    end = millis(2026, 6, 20, 8, 10),
                ),
                session(
                    start = millis(2026, 6, 20, 19, 0),
                    end = millis(2026, 6, 20, 19, 20),
                ),
                session(
                    start = millis(2026, 6, 21, 9, 0),
                    end = millis(2026, 6, 21, 9, 20),
                ),
            ),
            zoneId = testZone,
        )

        assertEquals(2, aggregates.workoutDays)
    }

    private fun session(
        state: String = "finished",
        start: Long,
        end: Long?,
        distance: Double? = null,
        calories: Int? = null,
    ): WorkoutSessionEntity {
        return WorkoutSessionEntity(
            id = "session-$start",
            startTimeMillis = start,
            endTimeMillis = end,
            state = state,
            deviceName = null,
            deviceAddress = null,
            totalDistanceMeters = distance,
            totalCalories = calories,
            averagePowerWatts = null,
            averageCadenceRpm = null,
            averageHeartRateBpm = null,
            sampleCount = 0,
        )
    }

    private fun millis(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ): Long {
        return LocalDateTime.of(year, month, day, hour, minute)
            .atZone(testZone)
            .toInstant()
            .toEpochMilli()
    }

    private companion object {
        val testZone: ZoneId = ZoneId.of("UTC")
    }
}
