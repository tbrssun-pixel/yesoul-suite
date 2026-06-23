package com.owlbike.v1tracker.race

import com.owlbike.v1tracker.settings.UnitSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalInputParserTest {
    @Test
    fun parsesCustomDistanceWithDotAndComma() {
        val dot = GoalInputParser.parse(GoalType.Distance, "1.65").goal
        val comma = GoalInputParser.parse(GoalType.Distance, "2,5").goal

        assertEquals(GoalType.Distance, dot?.type)
        assertEquals(GoalSource.Manual, dot?.source)
        assertEquals(1_650.0, dot?.targetDistanceMeters ?: 0.0, 0.001)
        assertEquals(2_500.0, comma?.targetDistanceMeters ?: 0.0, 0.001)
    }

    @Test
    fun parsesCustomCaloriesWithDecimalInput() {
        val result = GoalInputParser.parse(GoalType.Calories, "125.6").goal

        assertEquals(GoalType.Calories, result?.type)
        assertEquals(GoalSource.Manual, result?.source)
        assertEquals(126, result?.targetCalories)
    }

    @Test
    fun parsesDurationMinutesAndMinuteSecondsInput() {
        val minutes = GoalInputParser.parse(GoalType.Duration, "25").goal
        val minuteSeconds = GoalInputParser.parse(GoalType.Duration, "12:30").goal

        assertEquals(GoalType.Duration, minutes?.type)
        assertEquals(GoalSource.Manual, minutes?.source)
        assertEquals(1_500L, minutes?.targetDurationSeconds)
        assertEquals(750L, minuteSeconds?.targetDurationSeconds)
        assertEquals("12:30", GoalInputParser.inputText(minuteSeconds!!, GoalType.Duration))
    }

    @Test
    fun rejectsInvalidCustomInputsWithoutGoal() {
        val empty = GoalInputParser.parse(GoalType.Distance, "")
        val zero = GoalInputParser.parse(GoalType.Distance, "0")
        val negative = GoalInputParser.parse(GoalType.Calories, "-10")
        val invalidDuration = GoalInputParser.parse(GoalType.Duration, "3:99")

        assertNull(empty.goal)
        assertEquals(GoalInputError.Distance, empty.error)
        assertNull(zero.goal)
        assertEquals(GoalInputError.Distance, zero.error)
        assertNull(negative.goal)
        assertEquals(GoalInputError.Calories, negative.error)
        assertNull(invalidDuration.goal)
        assertEquals(GoalInputError.Duration, invalidDuration.error)
    }

    @Test
    fun newCustomValueReplacesExistingManualGoal() {
        val existing = WorkoutGoal.distance(1_650.0, GoalSource.Manual)
        val parsed = GoalInputParser.parse(existing.type, "3.2")

        assertTrue(parsed.isValid)
        assertEquals(3_200.0, parsed.goal?.targetDistanceMeters ?: 0.0, 0.001)
    }

    @Test
    fun parsesImperialDistanceInputIntoStoredMeters() {
        val parsed = GoalInputParser.parse(GoalType.Distance, "1", UnitSystem.Imperial)

        assertTrue(parsed.isValid)
        assertEquals(1_609.344, parsed.goal?.targetDistanceMeters ?: 0.0, 0.001)
        assertEquals("1", GoalInputParser.inputText(parsed.goal!!, GoalType.Distance, UnitSystem.Imperial))
    }
}
