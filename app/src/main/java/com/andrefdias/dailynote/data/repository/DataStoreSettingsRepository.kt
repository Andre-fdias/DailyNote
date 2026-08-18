package com.andrefdias.dailynote.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.andrefdias.dailynote.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    companion object {
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val KEY_PIN_CODE = stringPreferencesKey("pin_code")
        val KEY_ACTIVE_CALENDAR_FILTER = stringPreferencesKey("active_calendar_filter")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        

        
        val KEY_LANGUAGE = stringPreferencesKey("idioma")
        val KEY_DATE_FORMAT = stringPreferencesKey("formato_data")
        val KEY_UNIT_SYSTEM = stringPreferencesKey("sistema_unidades")
    }

    override val themeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "Automático"
    }

    override val pinEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_PIN_ENABLED] ?: false
    }

    override val pinCodeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_PIN_CODE] ?: ""
    }

    override val activeCalendarFilterFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_ACTIVE_CALENDAR_FILTER] ?: "Todos"
    }

    override val biometricEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_BIOMETRIC_ENABLED] ?: false
    }



    override val languageFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: "Português (BR)"
    }

    override val dateFormatFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_DATE_FORMAT] ?: "DD/MM/YYYY"
    }

    override val unitSystemFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_UNIT_SYSTEM] ?: "Métrico"
    }

    override suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    override suspend fun setPinEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_PIN_ENABLED] = enabled
        }
    }

    override suspend fun setPinCode(pin: String) {
        dataStore.edit { preferences ->
            preferences[KEY_PIN_CODE] = pin
        }
    }

    override suspend fun setActiveCalendarFilter(filter: String) {
        dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_CALENDAR_FILTER] = filter
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }



    override suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }

    override suspend fun setDateFormat(format: String) {
        dataStore.edit { preferences ->
            preferences[KEY_DATE_FORMAT] = format
        }
    }

    override suspend fun setUnitSystem(system: String) {
        dataStore.edit { preferences ->
            preferences[KEY_UNIT_SYSTEM] = system
        }
    }
}
