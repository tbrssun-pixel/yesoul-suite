package com.owlbike.v1tracker.race

import com.owlbike.v1tracker.settings.MeasurementFormatter
import com.owlbike.v1tracker.settings.UnitSystem
import java.util.Locale
import kotlin.math.roundToInt

internal enum class GoalInputError {
    Distance,
    Calories,
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
            GoalType.None -> ""
        }
    }

    private fun compactDecimal(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
            .trimEnd('0')
            .trimEnd('.')
    }
}
