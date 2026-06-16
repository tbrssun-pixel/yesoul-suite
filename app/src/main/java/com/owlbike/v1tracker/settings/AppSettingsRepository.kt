package com.owlbike.v1tracker.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

class AppSettingsRepository(context: Context) {
    private val dataStore = context.applicationContext.appSettingsDataStore

    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            themeMode = ThemeMode.fromStorage(preferences[THEME_MODE_KEY]),
            unitSystem = UnitSystem.fromStorage(preferences[UNIT_SYSTEM_KEY]),
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.storageValue
        }
    }

    suspend fun setUnitSystem(unitSystem: UnitSystem) {
        dataStore.edit { preferences ->
            preferences[UNIT_SYSTEM_KEY] = unitSystem.storageValue
        }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val UNIT_SYSTEM_KEY = stringPreferencesKey("unit_system")
    }
}
