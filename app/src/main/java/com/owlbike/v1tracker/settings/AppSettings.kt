package com.owlbike.v1tracker.settings

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.Dark,
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val languageMode: LanguageMode = LanguageMode.System,
)

enum class ThemeMode(val storageValue: String) {
    Dark("dark"),
    Light("light");

    companion object {
        fun fromStorage(value: String?): ThemeMode {
            return entries.firstOrNull { it.storageValue == value } ?: Dark
        }
    }
}

enum class UnitSystem(val storageValue: String) {
    Metric("metric"),
    Imperial("imperial");

    companion object {
        fun fromStorage(value: String?): UnitSystem {
            return entries.firstOrNull { it.storageValue == value } ?: Metric
        }
    }
}

enum class LanguageMode(val localeTag: String?) {
    System(null),
    Russian("ru"),
    English("en");

    companion object {
        fun fromLocaleTags(localeTags: String?): LanguageMode {
            val primaryTag = localeTags
                ?.split(',')
                ?.firstOrNull()
                ?.trim()
                ?.lowercase()
                .orEmpty()
            return when {
                primaryTag.startsWith("ru") -> Russian
                primaryTag.startsWith("en") -> English
                else -> System
            }
        }
    }
}
