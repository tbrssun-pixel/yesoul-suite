package com.owlbike.v1tracker.race

import com.owlbike.v1tracker.data.WorkoutSessionEntity
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RaceModelsTest {
    private val zone = ZoneId.of("UTC")

    @Test
    fun buildsMedianBaselineAndStreaksFromFinishedRides() {
        val today = LocalDate.of(2026, 6, 12)
        val sessions = listOf(
            session(day = today.minusDays(3), distance = 8_000.0, calories = 180, duration = 1_800),
            session(day = today.minusDays(2), distance = 10_000.0, calories = 220, duration = 2_000),
            session(day = today.minusDays(1), distance = 12_000.0, calories = 260, duration = 2_200),
            session(day = today.minusDays(6), distance = 20_000.0, calories = 500, duration = 3_600),
        )

        val baseline = RaceCalculator.buildPersonalBaseline(
            sessions = sessions,
            nowMillis = today.atStartOfDay(zone).toInstant().toEpochMilli(),
            zoneId = zone,
        )

        assertEquals(11_000.0, baseline.medianDistanceMeters ?: 0.0, 0.001)
        assertEquals(240, baseline.medianCalories)
        assertEquals(2_100L, baseline.medianDistanceDurationSeconds)
        assertEquals(3, baseline.currentStreakDays)
        assertEquals(3, baseline.bestStreakDays)
        assertEquals(20_000.0, baseline.bestRideDistanceMeters ?: 0.0, 0.001)
    }

    @Test
    fun ghostMovesByMedianPaceInsteadOfStandingAtFinish() {
        val baseline = PersonalBaseline(
            medianDistanceMeters = 15_000.0,
            medianDistanceDurationSeconds = 3_600,
        )
        val goal = WorkoutGoal.distance(15_000.0, GoalSource.Median)

        val state = RaceCalculator.ghostRaceState(
            goal = goal,
            baseline = baseline,
            elapsedSeconds = 1_800,
            currentDistanceMeters = 7_600.0,
            currentCalories = null,
        )

        assertTrue(state.isActive)
        assertEquals(7_500.0, state.ghostValue, 0.001)
        assertEquals(100.0, state.deltaValue, 0.001)
        assertEquals(RaceZone.Neutral, state.zone)
    }

    @Test
    fun ghostCapsAtSmallerManualTarget() {
        val baseline = PersonalBaseline(
            medianDistanceMeters = 15_000.0,
            medianDistanceDurationSeconds = 3_600,
        )
        val goal = WorkoutGoal.distance(10_000.0, GoalSource.Manual)

        val state = RaceCalculator.ghostRaceState(
            goal = goal,
            baseline = baseline,
            elapsedSeconds = 7_200,
            currentDistanceMeters = 9_000.0,
            currentCalories = null,
        )

        assertEquals(10_000.0, state.ghostValue, 0.001)
        assertEquals(RaceZone.Behind, state.zone)
    }

    @Test
    fun hysteresisKeepsAheadUntilGapCrossesExitThreshold() {
        assertEquals(RaceZone.Ahead, RaceCalculator.resolveZone(0.021, RaceZone.Neutral))
        assertEquals(RaceZone.Ahead, RaceCalculator.resolveZone(0.000, RaceZone.Ahead))
        assertEquals(RaceZone.Neutral, RaceCalculator.resolveZone(-0.011, RaceZone.Ahead))
    }

    @Test
    fun hysteresisKeepsBehindUntilGapCrossesExitThreshold() {
        assertEquals(RaceZone.Behind, RaceCalculator.resolveZone(-0.021, RaceZone.Neutral))
        assertEquals(RaceZone.Behind, RaceCalculator.resolveZone(0.000, RaceZone.Behind))
        assertEquals(RaceZone.Neutral, RaceCalculator.resolveZone(0.011, RaceZone.Behind))
    }

    @Test
    fun noHistoryDisablesDefaultGoalAndGhost() {
        val baseline = RaceCalculator.buildPersonalBaseline(emptyList(), zoneId = zone)
        val goal = RaceCalculator.defaultGoal(baseline)
        val ghost = RaceCalculator.ghostRaceState(
            goal = goal,
            baseline = baseline,
            elapsedSeconds = 60,
            currentDistanceMeters = 100.0,
            currentCalories = null,
        )

        assertFalse(goal.isActive)
        assertFalse(ghost.isActive)
    }

    @Test
    fun goalProgressUsesCurrentMetricWhenGhostHasNoBaseline() {
        val baseline = PersonalBaseline()
        val goal = WorkoutGoal.distance(1_650.0, GoalSource.Manual)
        val ghost = RaceCalculator.ghostRaceState(
            goal = goal,
            baseline = baseline,
            elapsedSeconds = 60,
            currentDistanceMeters = 2_270.0,
            currentCalories = null,
        )

        val progress = RaceCalculator.goalProgressState(
            goal = goal,
            currentDistanceMeters = 2_270.0,
            currentCalories = null,
        )

        assertFalse(ghost.isActive)
        assertEquals(2_270.0, progress.userValue, 0.001)
        assertEquals(1_650.0, progress.targetValue, 0.001)
        assertEquals(1.0f, progress.progress, 0.001f)
        assertTrue(progress.completed)
    }

    private fun session(
        day: LocalDate,
        distance: Double,
        calories: Int,
        duration: Long,
    ): WorkoutSessionEntity {
        val start = day.atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
        return WorkoutSessionEntity(
            id = day.toString(),
            startTimeMillis = start,
            endTimeMillis = start + duration * 1_000,
            state = "finished",
            deviceName = "YSV100637",
            deviceAddress = null,
            totalDistanceMeters = distance,
            totalCalories = calories,
            averagePowerWatts = null,
            averageCadenceRpm = null,
            averageHeartRateBpm = null,
            sampleCount = 10,
        )
    }
}
