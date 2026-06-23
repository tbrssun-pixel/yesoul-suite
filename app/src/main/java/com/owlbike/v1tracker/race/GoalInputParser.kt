package com.owlbike.v1tracker.race

import com.owlbike.v1tracker.settings.MeasurementFormatter
import com.owlbike.v1tracker.settings.UnitSystem
import java.util.Locale
import kotlin.math.roundToInt

internal enum class GoalInputError {
    Distance,
    Calories,
    Duration,
}

internal data class GoalInputResult(
    val goal: WorkoutGoal? = null,
    val error: GoalInputError? = null,
) {
    val isValid: Boolean
        get() = goal != null && error == null
}

internal object GoalInputParser {
    fun parse(
        type: GoalType,
        input: String,
        unitSystem: UnitSystem = UnitSystem.Metric,
    ): GoalInputResult {
        val normalized = input.trim().replace(',', '.')
        return when (type) {
            GoalType.Distance -> {
                val distance = normalized.toDoubleOrNull()
                if (distance == null || distance <= 0.0) {
                    GoalInputResult(error = GoalInputError.Distance)
                } else {
                    GoalInputResult(
                        goal = WorkoutGoal.distance(
                            MeasurementFormatter.distanceInputToMeters(distance, unitSystem),
                            GoalSource.Manual,
                        ),
                    )
                }
            }
            GoalType.Calories -> {
                val calories = normalized.toDoubleOrNull()?.roundToInt()
                if (calories == null || calories <= 0) {
                    GoalInputResult(error = GoalInputError.Calories)
                } else {
                    GoalInputResult(goal = WorkoutGoal.calories(calories, GoalSource.Manual))
                }
            }
            GoalType.Duration -> {
                val durationSeconds = parseDurationSeconds(normalized)
                if (durationSeconds == null || durationSeconds <= 0L) {
                    GoalInputResult(error = GoalInputError.Duration)
                } else {
                    GoalInputResult(goal = WorkoutGoal.duration(durationSeconds, GoalSource.Manual))
                }
            }
            GoalType.None -> GoalInputResult(error = GoalInputError.Distance)
        }
    }

    fun inputText(
        goal: WorkoutGoal,
        type: GoalType,
        unitSystem: UnitSystem = UnitSystem.Metric,
    ): String {
        return when (type) {
            GoalType.Distance -> goal.targetDistanceMeters
                ?.let { compactDecimal(MeasurementFormatter.distanceValue(it, unitSystem)) }
                .orEmpty()
            GoalType.Calories -> goal.targetCalories?.toString().orEmpty()
            GoalType.Duration -> goal.targetDurationSeconds?.let(::durationInputText).orEmpty()
            GoalType.None -> ""
        }
    }

    private fun parseDurationSeconds(value: String): Long? {
        if (value.contains(':')) {
            val parts = value.split(':')
            if (parts.size != 2) return null
            val minutes = parts[0].toLongOrNull() ?: return null
            val seconds = parts[1].toLongOrNull() ?: return null
            if (minutes < 0L || seconds !in 0L..59L) return null
            return minutes * 60L + seconds
        }
        val minutes = value.toDoubleOrNull() ?: return null
        return (minutes * 60.0).roundToInt().toLong()
    }

    private fun durationInputText(totalSeconds: Long): String {
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return if (seconds == 0L) {
            minutes.toString()
        } else {
            "%d:%02d".format(Locale.US, minutes, seconds)
        }
    }

    private fun compactDecimal(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
            .trimEnd('0')
            .trimEnd('.')
    }
}
