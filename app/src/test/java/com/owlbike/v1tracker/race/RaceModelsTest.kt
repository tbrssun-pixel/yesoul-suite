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

    @Test
    fun durationGoalProgressUsesElapsedSecondsWithoutMedianGhost() {
        val baseline = PersonalBaseline(
            medianDistanceMeters = 10_000.0,
            medianDistanceDurationSeconds = 2_400L,
        )
        val goal = WorkoutGoal.duration(1_800L, GoalSource.Manual)

        val ghost = RaceCalculator.ghostRaceState(
            goal = goal,
            baseline = baseline,
            elapsedSeconds = 900L,
            currentDistanceMeters = 5_000.0,
            currentCalories = 120,
        )
        val progress = RaceCalculator.goalProgressState(
            goal = goal,
            currentDistanceMeters = 5_000.0,
            currentCalories = 120,
            elapsedSeconds = 900L,
        )

        assertFalse(ghost.isActive)
        assertEquals(900.0, progress.userValue, 0.001)
        assertEquals(1_800.0, progress.targetValue, 0.001)
        assertEquals(0.5f, progress.progress, 0.001f)
        assertFalse(progress.completed)
    }

    @Test
    fun durationGoalProgressCompletesAtTarget() {
        val goal = WorkoutGoal.duration(1_800L, GoalSource.Manual)

        val progress = RaceCalculator.goalProgressState(
            goal = goal,
            currentDistanceMeters = null,
            currentCalories = null,
            elapsedSeconds = 1_800L,
        )

        assertEquals(1.0f, progress.progress, 0.001f)
        assertEquals(0.0, progress.remainingValue, 0.001)
        assertTrue(progress.completed)
    }

    @Test
    fun defaultGoalFromLastWorkoutReusesPersistedDistanceTarget() {
        val today = LocalDate.of(2026, 6, 12)
        val latest = session(day = today.minusDays(1), distance = 10_000.0, calories = 220, duration = 1_500)
            .copy(
                goalType = GoalType.Distance.storageValue,
                goalSource = GoalSource.Manual.storageValue,
                goalTargetDistanceMeters = 12_500.0,
            )

        val goal = RaceCalculator.defaultGoalFromLastWorkout(
            sessions = listOf(latest),
            baseline = PersonalBaseline(),
        )

        assertEquals(GoalType.Distance, goal.type)
        assertEquals(GoalSource.Previous, goal.source)
        assertEquals(12_500.0, goal.targetDistanceMeters ?: 0.0, 0.001)
    }

    @Test
    fun defaultGoalFromLastWorkoutReusesPersistedCaloriesTarget() {
        val today = LocalDate.of(2026, 6, 12)
        val latest = session(day = today.minusDays(1), distance = 10_000.0, calories = 220, duration = 1_500)
            .copy(
                goalType = GoalType.Calories.storageValue,
                goalSource = GoalSource.Manual.storageValue,
                goalTargetCalories = 320,
            )

        val goal = RaceCalculator.defaultGoalFromLastWorkout(
            sessions = listOf(latest),
            baseline = PersonalBaseline(),
        )

        assertEquals(GoalType.Calories, goal.type)
        assertEquals(GoalSource.Previous, goal.source)
        assertEquals(320, goal.targetCalories)
    }

    @Test
    fun defaultGoalFromLastWorkoutReusesPersistedDurationTarget() {
        val today = LocalDate.of(2026, 6, 12)
        val older = session(day = today.minusDays(2), distance = 8_000.0, calories = 180, duration = 1_200)
        val latest = session(day = today.minusDays(1), distance = 10_000.0, calories = 220, duration = 1_500)
            .copy(
                goalType = GoalType.Duration.storageValue,
                goalSource = GoalSource.Manual.storageValue,
                goalTargetDurationSeconds = 1_800L,
            )

        val goal = RaceCalculator.defaultGoalFromLastWorkout(
            sessions = listOf(older, latest),
            baseline = RaceCalculator.buildPersonalBaseline(listOf(older, latest), zoneId = zone),
        )

        assertEquals(GoalType.Duration, goal.type)
        assertEquals(GoalSource.Previous, goal.source)
        assertEquals(1_800L, goal.targetDurationSeconds)
    }

    @Test
    fun defaultGoalFromLastWorkoutUsesLastMetricWhenNoStoredGoalExists() {
        val today = LocalDate.of(2026, 6, 12)
        val latest = session(day = today.minusDays(1), distance = 10_000.0, calories = 220, duration = 1_500)

        val goal = RaceCalculator.defaultGoalFromLastWorkout(
            sessions = listOf(latest),
            baseline = PersonalBaseline(),
        )

        assertEquals(GoalType.Distance, goal.type)
        assertEquals(GoalSource.Previous, goal.source)
        assertEquals(10_000.0, goal.targetDistanceMeters ?: 0.0, 0.001)
    }

    @Test
    fun defaultGoalFromLastWorkoutFallsBackToActualCaloriesThenDuration() {
        val today = LocalDate.of(2026, 6, 12)
        val caloriesOnly = session(
            day = today.minusDays(2),
            distance = null,
            calories = 220,
            duration = 1_500,
        )
        val durationOnly = session(
            day = today.minusDays(1),
            distance = null,
            calories = null,
            duration = 1_800,
        )

        val caloriesGoal = RaceCalculator.defaultGoalFromLastWorkout(
            sessions = listOf(caloriesOnly),
            baseline = PersonalBaseline(),
        )
        val durationGoal = RaceCalculator.defaultGoalFromLastWorkout(
            sessions = listOf(durationOnly),
            baseline = PersonalBaseline(),
        )

        assertEquals(GoalType.Calories, caloriesGoal.type)
        assertEquals(220, caloriesGoal.targetCalories)
        assertEquals(GoalType.Duration, durationGoal.type)
        assertEquals(1_800L, durationGoal.targetDurationSeconds)
    }

    @Test
    fun defaultGoalFromLastWorkoutIgnoresRunningSessions() {
        val today = LocalDate.of(2026, 6, 12)
        val finished = session(day = today.minusDays(2), distance = 6_000.0, calories = 120, duration = 900)
        val running = session(day = today.minusDays(1), distance = 20_000.0, calories = 500, duration = 2_000)
            .copy(state = "running", endTimeMillis = null)

        val goal = RaceCalculator.defaultGoalFromLastWorkout(
            sessions = listOf(running, finished),
            baseline = PersonalBaseline(),
        )

        assertEquals(GoalType.Distance, goal.type)
        assertEquals(6_000.0, goal.targetDistanceMeters ?: 0.0, 0.001)
    }

    @Test
    fun defaultGoalFromLastWorkoutFallsBackToMedianThenNone() {
        val distanceBaseline = PersonalBaseline(
            medianDistanceMeters = 7_000.0,
            medianDistanceDurationSeconds = 1_200L,
        )

        val medianGoal = RaceCalculator.defaultGoalFromLastWorkout(emptyList(), distanceBaseline)
        val noneGoal = RaceCalculator.defaultGoalFromLastWorkout(emptyList(), PersonalBaseline())

        assertEquals(GoalType.Distance, medianGoal.type)
        assertEquals(7_000.0, medianGoal.targetDistanceMeters ?: 0.0, 0.001)
        assertFalse(noneGoal.isActive)
    }

    @Test
    fun adjustGoalChangesDistanceByStepAndClampsToMinimum() {
        val goal = WorkoutGoal.distance(1_000.0, GoalSource.Median)

        val increased = RaceCalculator.adjustGoal(goal, 1, distanceStepMeters = 100.0)
        val clamped = RaceCalculator.adjustGoal(goal, -20, distanceStepMeters = 100.0)

        assertEquals(GoalSource.Manual, increased.source)
        assertEquals(1_100.0, increased.targetDistanceMeters ?: 0.0, 0.001)
        assertEquals(100.0, clamped.targetDistanceMeters ?: 0.0, 0.001)
    }

    @Test
    fun adjustGoalChangesCaloriesByStepAndClampsToMinimum() {
        val goal = WorkoutGoal.calories(25, GoalSource.Median)

        val decreased = RaceCalculator.adjustGoal(goal, -1, distanceStepMeters = 100.0, caloriesStep = 5)
        val clamped = RaceCalculator.adjustGoal(goal, -20, distanceStepMeters = 100.0, caloriesStep = 5)

        assertEquals(GoalSource.Manual, decreased.source)
        assertEquals(20, decreased.targetCalories)
        assertEquals(5, clamped.targetCalories)
    }

    @Test
    fun adjustGoalChangesDurationByStepAndClampsToMinimum() {
        val goal = WorkoutGoal.duration(180L, GoalSource.Median)

        val increased = RaceCalculator.adjustGoal(goal, 1, distanceStepMeters = 100.0, durationStepSeconds = 60L)
        val clamped = RaceCalculator.adjustGoal(goal, -20, distanceStepMeters = 100.0, durationStepSeconds = 60L)

        assertEquals(GoalSource.Manual, increased.source)
        assertEquals(240L, increased.targetDurationSeconds)
        assertEquals(60L, clamped.targetDurationSeconds)
    }

    @Test
    fun adjustGoalLeavesNoGoalUnchanged() {
        val goal = WorkoutGoal.none()

        val adjusted = RaceCalculator.adjustGoal(goal, 1, distanceStepMeters = 100.0)

        assertFalse(adjusted.isActive)
        assertEquals(goal, adjusted)
    }

    private fun session(
        day: LocalDate,
        distance: Double?,
        calories: Int?,
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
