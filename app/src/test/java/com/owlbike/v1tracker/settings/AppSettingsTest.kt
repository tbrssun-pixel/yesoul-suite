package com.owlbike.v1tracker.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsTest {
    @Test
    fun parsesStoredSettingsWithSafeDefaults() {
        assertEquals(ThemeMode.Dark, ThemeMode.fromStorage(null))
        assertEquals(ThemeMode.Light, ThemeMode.fromStorage("light"))
        assertEquals(UnitSystem.Metric, UnitSystem.fromStorage("unknown"))
        assertEquals(UnitSystem.Imperial, UnitSystem.fromStorage("imperial"))
    }

    @Test
    fun mapsLocaleTagsToLanguageMode() {
        assertEquals(LanguageMode.System, LanguageMode.fromLocaleTags(""))
        assertEquals(LanguageMode.Russian, LanguageMode.fromLocaleTags("ru"))
        assertEquals(LanguageMode.English, LanguageMode.fromLocaleTags("en-US"))
    }
}
