package com.andrefdias.dailynote.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.model.*
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsCalendarUiState(
    val scales: List<EscalaConfig> = emptyList(),
    val calendarSettings: CalendarSettings = CalendarSettings()
)

@HiltViewModel
class SettingsCalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsCalendarUiState())
    val uiState: StateFlow<SettingsCalendarUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                calendarRepository.getEscalasFlow(),
                calendarRepository.getSettingsFlow()
            ) { scales, settings ->
                SettingsCalendarUiState(
                    scales = scales,
                    calendarSettings = settings ?: CalendarSettings()
                )
            }.collect { updated ->
                _uiState.value = updated
            }
        }
    }

    fun deleteEscala(id: String) {
        viewModelScope.launch {
            calendarRepository.deleteEscala(id)
        }
    }
}
