package com.owlbike.v1tracker.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementFormatterTest {
    @Test
    fun formatsMetricAndImperialDistance() {
        assertEquals("5.00 km", MeasurementFormatter.distance(5_000.0, UnitSystem.Metric))
        assertEquals("3.11 mi", MeasurementFormatter.distance(5_000.0, UnitSystem.Imperial))
    }

    @Test
    fun formatsMetricAndImperialSpeed() {
        assertEquals("22.0 km/h", MeasurementFormatter.speed(22.0, UnitSystem.Metric))
        assertEquals("13.7 mph", MeasurementFormatter.speed(22.0, UnitSystem.Imperial))
    }

    @Test
    fun convertsDistanceInputToStoredMeters() {
        assertEquals(1_000.0, MeasurementFormatter.distanceInputToMeters(1.0, UnitSystem.Metric), 0.001)
        assertEquals(1_609.344, MeasurementFormatter.distanceInputToMeters(1.0, UnitSystem.Imperial), 0.001)
    }
}
