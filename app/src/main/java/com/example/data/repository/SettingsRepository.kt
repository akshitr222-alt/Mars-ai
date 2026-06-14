package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sole_ai_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val KEY_ACTIVE_USER_EMAIL = stringPreferencesKey("active_user_email")
        private val KEY_AMOLED_MODE = booleanPreferencesKey("amoled_mode")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_FONT_SIZE_MULTIPLIER = floatPreferencesKey("font_size_multiplier")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_TTS_ENABLED = booleanPreferencesKey("tts_enabled")
    }

    val activeUserEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ACTIVE_USER_EMAIL]
    }

    val isAmoledMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AMOLED_MODE] ?: true // default true for luxurious AMOLED dark mode
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE] ?: true // default true
    }

    val fontSizeMultiplier: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_FONT_SIZE_MULTIPLIER] ?: 1.0f
    }

    val currentLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: "en"
    }

    val isTtsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_TTS_ENABLED] ?: false
    }

    suspend fun setActiveUserEmail(email: String?) {
        context.dataStore.edit { preferences ->
            if (email == null) {
                preferences.remove(KEY_ACTIVE_USER_EMAIL)
            } else {
                preferences[KEY_ACTIVE_USER_EMAIL] = email
            }
        }
    }

    suspend fun setAmoledMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AMOLED_MODE] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun setFontSizeMultiplier(multiplier: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FONT_SIZE_MULTIPLIER] = multiplier
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TTS_ENABLED] = enabled
        }
    }
}
