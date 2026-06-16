package com.owlbike.v1tracker.race

import com.owlbike.v1tracker.data.WorkoutSessionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class GoalType(val storageValue: String) {
    Distance("distance"),
    Calories("calories"),
    None("none");

    companion object {
        fun fromStorage(value: String?): GoalType {
            return entries.firstOrNull { it.storageValue == value } ?: None
        }
    }
}

enum class GoalSource(val storageValue: String) {
    Median("median"),
    Manual("manual");

    companion object {
        fun fromStorage(value: String?): GoalSource {
            return entries.firstOrNull { it.storageValue == value } ?: Median
        }
    }
}

enum class RaceZone {
    Neutral,
    Ahead,
    Behind,
}

data class WorkoutGoal(
    val type: GoalType = GoalType.None,
    val source: GoalSource = GoalSource.Median,
    val targetDistanceMeters: Double? = null,
    val targetCalories: Int? = null,
) {
    val isActive: Boolean
        get() = type != GoalType.None

    val targetValue: Double?
        get() = when (type) {
            GoalType.Distance -> targetDistanceMeters
            GoalType.Calories -> targetCalories?.toDouble()
            GoalType.None -> null
        }

    companion object {
        fun none(source: GoalSource = GoalSource.Median): WorkoutGoal {
            return WorkoutGoal(type = GoalType.None, source = source)
        }

        fun distance(targetMeters: Double, source: GoalSource): WorkoutGoal {
            return WorkoutGoal(
                type = GoalType.Distance,
                source = source,
                targetDistanceMeters = targetMeters.coerceAtLeast(0.0),
            )
        }

        fun calories(targetCalories: Int, source: GoalSource): WorkoutGoal {
            return WorkoutGoal(
                type = GoalType.Calories,
                source = source,
                targetCalories = targetCalories.coerceAtLeast(0),
            )
        }
    }
}

data class PersonalBaseline(
    val qualifyingRideCount: Int = 0,
    val medianDistanceMeters: Double? = null,
    val medianCalories: Int? = null,
    val medianDistanceDurationSeconds: Long? = null,
    val medianCaloriesDurationSeconds: Long? = null,
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val bestRideDistanceMeters: Double? = null,
    val lastRideStartTimeMillis: Long? = null,
    val ridesThisWeek: Int = 0,
) {
    val hasDistanceBaseline: Boolean
        get() = medianDistanceMeters != null && medianDistanceDurationSeconds != null

    val hasCaloriesBaseline: Boolean
        get() = medianCalories != null && medianCaloriesDurationSeconds != null
}

data class GhostRaceState(
    val isActive: Boolean = false,
    val zone: RaceZone = RaceZone.Neutral,
    val goalType: GoalType = GoalType.None,
    val userValue: Double = 0.0,
    val ghostValue: Double = 0.0,
    val targetValue: Double = 0.0,
    val userProgress: Float = 0f,
    val ghostProgress: Float = 0f,
    val deltaValue: Double = 0.0,
    val remainingValue: Double = 0.0,
    val completed: Boolean = false,
) {
    companion object {
        fun inactive(previousZone: RaceZone = RaceZone.Neutral): GhostRaceState {
            return GhostRaceState(zone = previousZone)
        }
    }
}

internal data class GoalProgressState(
    val isActive: Boolean,
    val goalType: GoalType,
    val userValue: Double,
    val targetValue: Double,
    val progress: Float,
    val remainingValue: Double,
    val completed: Boolean,
)

object RaceCalculator {
    private const val ENTER_AHEAD = 0.02
    private const val EXIT_AHEAD = -0.01
    private const val ENTER_BEHIND = -0.02
    private const val EXIT_BEHIND = 0.01
    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

    fun buildPersonalBaseline(
        sessions: List<WorkoutSessionEntity>,
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): PersonalBaseline {
        val finished = sessions.filter { it.state == "finished" && it.endTimeMillis != null }
        val distanceRides = finished.mapNotNull { session ->
            val duration = durationSeconds(session) ?: return@mapNotNull null
            val distance = session.totalDistanceMeters?.takeIf { it > 0.0 } ?: return@mapNotNull null
            MetricRide(distance, duration)
        }
        val calorieRides = finished.mapNotNull { session ->
            val duration = durationSeconds(session) ?: return@mapNotNull null
            val calories = session.totalCalories?.takeIf { it > 0 } ?: return@mapNotNull null
            MetricRide(calories.toDouble(), duration)
        }
        val rideDays = finished
            .map { Instant.ofEpochMilli(it.startTimeMillis).atZone(zoneId).toLocalDate() }
            .distinct()
            .toSet()
        val nowDate = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()

        return PersonalBaseline(
            qualifyingRideCount = finished.size,
            medianDistanceMeters = medianDouble(distanceRides.map { it.value }),
            medianCalories = medianDouble(calorieRides.map { it.value })?.roundToInt(),
            medianDistanceDurationSeconds = medianLong(distanceRides.map { it.durationSeconds }),
            medianCaloriesDurationSeconds = medianLong(calorieRides.map { it.durationSeconds }),
            currentStreakDays = currentStreak(rideDays, nowDate),
            bestStreakDays = bestStreak(rideDays),
            bestRideDistanceMeters = finished.mapNotNull { it.totalDistanceMeters }.maxOrNull(),
            lastRideStartTimeMillis = finished.maxByOrNull { it.startTimeMillis }?.startTimeMillis,
            ridesThisWeek = finished.count { it.startTimeMillis >= nowMillis - 7L * MILLIS_PER_DAY },
        )
    }

    fun defaultGoal(
        baseline: PersonalBaseline,
        preferredType: GoalType = GoalType.Distance,
    ): WorkoutGoal {
        if (preferredType == GoalType.Calories && baseline.hasCaloriesBaseline) {
            return WorkoutGoal.calories(baseline.medianCalories ?: 0, GoalSource.Median)
        }
        if (baseline.hasDistanceBaseline) {
            return WorkoutGoal.distance(baseline.medianDistanceMeters ?: 0.0, GoalSource.Median)
        }
        if (baseline.hasCaloriesBaseline) {
            return WorkoutGoal.calories(baseline.medianCalories ?: 0, GoalSource.Median)
        }
        return WorkoutGoal.none()
    }

    fun baselineDurationSeconds(goal: WorkoutGoal, baseline: PersonalBaseline): Long? {
        return when (goal.type) {
            GoalType.Distance -> baseline.medianDistanceDurationSeconds
            GoalType.Calories -> baseline.medianCaloriesDurationSeconds
            GoalType.None -> null
        }
    }

    fun ghostRaceState(
        goal: WorkoutGoal,
        baseline: PersonalBaseline,
        elapsedSeconds: Long,
        currentDistanceMeters: Double?,
        currentCalories: Int?,
        previousZone: RaceZone = RaceZone.Neutral,
    ): GhostRaceState {
        val target = goal.targetValue?.takeIf { it > 0.0 } ?: return GhostRaceState.inactive(previousZone)
        val baselineMetric = when (goal.type) {
            GoalType.Distance -> baseline.medianDistanceMeters
            GoalType.Calories -> baseline.medianCalories?.toDouble()
            GoalType.None -> null
        }?.takeIf { it > 0.0 } ?: return GhostRaceState.inactive(previousZone)
        val baselineDuration = baselineDurationSeconds(goal, baseline)
            ?.takeIf { it > 0L }
            ?: return GhostRaceState.inactive(previousZone)
        val userValue = when (goal.type) {
            GoalType.Distance -> currentDistanceMeters ?: 0.0
            GoalType.Calories -> currentCalories?.toDouble() ?: 0.0
            GoalType.None -> 0.0
        }.coerceAtLeast(0.0)
        val ghostCap = min(baselineMetric, target)
        val ghostValue = (baselineMetric / baselineDuration.toDouble() * elapsedSeconds.coerceAtLeast(0L))
            .coerceIn(0.0, ghostCap)
        val delta = userValue - ghostValue
        val gapRatio = if (target > 0.0) delta / target else 0.0
        val zone = resolveZone(gapRatio, previousZone)

        return GhostRaceState(
            isActive = true,
            zone = zone,
            goalType = goal.type,
            userValue = userValue,
            ghostValue = ghostValue,
            targetValue = target,
            userProgress = progress(userValue, target),
            ghostProgress = progress(ghostValue, target),
            deltaValue = delta,
            remainingValue = max(0.0, target - userValue),
            completed = userValue >= target,
        )
    }

    internal fun goalProgressState(
        goal: WorkoutGoal,
        currentDistanceMeters: Double?,
        currentCalories: Int?,
    ): GoalProgressState {
        val target = goal.targetValue?.takeIf { it > 0.0 }
        val userValue = when (goal.type) {
            GoalType.Distance -> currentDistanceMeters ?: 0.0
            GoalType.Calories -> currentCalories?.toDouble() ?: 0.0
            GoalType.None -> currentDistanceMeters ?: 0.0
        }.coerceAtLeast(0.0)

        if (!goal.isActive || target == null) {
            return GoalProgressState(
                isActive = false,
                goalType = goal.type,
                userValue = userValue,
                targetValue = 0.0,
                progress = 0f,
                remainingValue = 0.0,
                completed = false,
            )
        }

        return GoalProgressState(
            isActive = true,
            goalType = goal.type,
            userValue = userValue,
            targetValue = target,
            progress = progress(userValue, target),
            remainingValue = max(0.0, target - userValue),
            completed = userValue >= target,
        )
    }

    fun resolveZone(gapRatio: Double, previousZone: RaceZone): RaceZone {
        return when (previousZone) {
            RaceZone.Ahead -> if (gapRatio <= EXIT_AHEAD) RaceZone.Neutral else RaceZone.Ahead
            RaceZone.Behind -> if (gapRatio >= EXIT_BEHIND) RaceZone.Neutral else RaceZone.Behind
            RaceZone.Neutral -> when {
                gapRatio >= ENTER_AHEAD -> RaceZone.Ahead
                gapRatio <= ENTER_BEHIND -> RaceZone.Behind
                else -> RaceZone.Neutral
            }
        }
    }

    private fun progress(value: Double, target: Double): Float {
        return (value / target).coerceIn(0.0, 1.0).toFloat()
    }

    private fun durationSeconds(session: WorkoutSessionEntity): Long? {
        val end = session.endTimeMillis ?: return null
        return ((end - session.startTimeMillis) / 1000).coerceAtLeast(0L).takeIf { it > 0L }
    }

    private fun medianDouble(values: List<Double>): Double? {
        if (values.isEmpty()) return null
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        } else {
            sorted[middle]
        }
    }

    private fun medianLong(values: List<Long>): Long? {
        return medianDouble(values.map { it.toDouble() })?.roundToInt()?.toLong()
    }

    private fun currentStreak(rideDays: Set<LocalDate>, today: LocalDate): Int {
        val anchor = when {
            today in rideDays -> today
            today.minusDays(1) in rideDays -> today.minusDays(1)
            else -> return 0
        }
        var count = 0
        var cursor = anchor
        while (cursor in rideDays) {
            count += 1
            cursor = cursor.minusDays(1)
        }
        return count
    }

    private fun bestStreak(rideDays: Set<LocalDate>): Int {
        if (rideDays.isEmpty()) return 0
        val sorted = rideDays.sorted()
        var best = 1
        var current = 1
        for (index in 1 until sorted.size) {
            current = if (sorted[index - 1].plusDays(1) == sorted[index]) current + 1 else 1
            best = max(best, current)
        }
        return best
    }

    private data class MetricRide(
        val value: Double,
        val durationSeconds: Long,
    )
}
