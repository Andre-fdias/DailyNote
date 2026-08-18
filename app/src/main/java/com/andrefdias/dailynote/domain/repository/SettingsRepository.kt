package com.andrefdias.dailynote.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeFlow: Flow<String>
    val pinEnabledFlow: Flow<Boolean>
    val pinCodeFlow: Flow<String>
    val activeCalendarFilterFlow: Flow<String>
    val biometricEnabledFlow: Flow<Boolean>

    val languageFlow: Flow<String>
    val dateFormatFlow: Flow<String>
    val unitSystemFlow: Flow<String>

    suspend fun setTheme(theme: String)
    suspend fun setPinEnabled(enabled: Boolean)
    suspend fun setPinCode(pin: String)
    suspend fun setActiveCalendarFilter(filter: String)
    suspend fun setBiometricEnabled(enabled: Boolean)

    suspend fun setLanguage(language: String)
    suspend fun setDateFormat(format: String)
    suspend fun setUnitSystem(system: String)
}
