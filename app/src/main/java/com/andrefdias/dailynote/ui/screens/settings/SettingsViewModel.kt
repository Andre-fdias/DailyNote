package com.andrefdias.dailynote.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isGoogleConnected: Boolean = false,
    val googleAccountName: String? = null,
    val isProcessing: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    
    val idioma: String = "Português (BR)",
    val formatoData: String = "DD/MM/YYYY",
    val sistemaUnidades: String = "Métrico"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: com.andrefdias.dailynote.domain.repository.SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.languageFlow.collect { lang ->
                _uiState.update { it.copy(idioma = lang) }
            }
        }
        viewModelScope.launch {
            settingsRepository.dateFormatFlow.collect { format ->
                _uiState.update { it.copy(formatoData = format) }
            }
        }
        viewModelScope.launch {
            settingsRepository.unitSystemFlow.collect { system ->
                _uiState.update { it.copy(sistemaUnidades = system) }
            }
        }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setIdioma(idioma: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(idioma)
        }
    }

    fun setFormatoData(formato: String) {
        viewModelScope.launch {
            settingsRepository.setDateFormat(formato)
        }
    }

    fun setSistemaUnidades(sistema: String) {
        viewModelScope.launch {
            settingsRepository.setUnitSystem(sistema)
        }
    }

    fun setTema(tema: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(tema)
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometricEnabled(enabled)
        }
    }

    fun setPinCode(pin: String) {
        viewModelScope.launch {
            if (pin.isEmpty()) {
                settingsRepository.setPinEnabled(false)
            } else {
                settingsRepository.setPinEnabled(true)
            }
            settingsRepository.setPinCode(pin)
        }
    }
}
