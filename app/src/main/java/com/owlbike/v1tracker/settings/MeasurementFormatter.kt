package com.owlbike.v1tracker.settings

import java.util.Locale

object MeasurementFormatter {
    private const val METERS_PER_KILOMETER = 1_000.0
    private const val METERS_PER_MILE = 1_609.344
    private const val KMH_TO_MPH = 0.621371192237334

    fun distanceUnit(unitSystem: UnitSystem): String {
        return when (unitSystem) {
            UnitSystem.Metric -> "km"
            UnitSystem.Imperial -> "mi"
        }
    }

    fun speedUnit(unitSystem: UnitSystem): String {
        return when (unitSystem) {
            UnitSystem.Metric -> "km/h"
            UnitSystem.Imperial -> "mph"
        }
    }

    fun distanceValue(meters: Double, unitSystem: UnitSystem): Double {
        return when (unitSystem) {
            UnitSystem.Metric -> meters / METERS_PER_KILOMETER
            UnitSystem.Imperial -> meters / METERS_PER_MILE
        }
    }

    fun speedValue(kmh: Double, unitSystem: UnitSystem): Double {
        return when (unitSystem) {
            UnitSystem.Metric -> kmh
            UnitSystem.Imperial -> kmh * KMH_TO_MPH
        }
    }

    fun distance(meters: Double?, unitSystem: UnitSystem, decimals: Int = 2): String {
        return meters?.let { "${distanceValue(it, unitSystem).fixed(decimals)} ${distanceUnit(unitSystem)}" } ?: "-"
    }

    fun speed(kmh: Double?, unitSystem: UnitSystem, decimals: Int = 1): String {
        return kmh?.let { "${speedValue(it, unitSystem).fixed(decimals)} ${speedUnit(unitSystem)}" } ?: "-"
    }

    fun distanceInputToMeters(value: Double, unitSystem: UnitSystem): Double {
        return when (unitSystem) {
            UnitSystem.Metric -> value * METERS_PER_KILOMETER
            UnitSystem.Imperial -> value * METERS_PER_MILE
        }
    }

    fun distanceInputValue(meters: Double?, unitSystem: UnitSystem, decimals: Int = 2): String {
        return meters?.let { distanceValue(it, unitSystem).fixed(decimals) }.orEmpty()
    }

    private fun Double.fixed(decimals: Int): String {
        return "%.${decimals}f".format(Locale.US, this)
    }
}
