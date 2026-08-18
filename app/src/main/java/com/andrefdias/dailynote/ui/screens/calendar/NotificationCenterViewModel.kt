package com.andrefdias.dailynote.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.model.CalendarNotificacao
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationCenterUiState(
    val notifications: List<CalendarNotificacao> = emptyList(),
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationCenterViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationCenterUiState())
    val uiState: StateFlow<NotificationCenterUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                calendarRepository.getNotificacoesFlow(),
                calendarRepository.getUnreadNotificacoesCountFlow()
            ) { list, unread ->
                NotificationCenterUiState(
                    notifications = list,
                    unreadCount = unread
                )
            }.collect { updated ->
                _uiState.value = updated
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            calendarRepository.markAllAsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            calendarRepository.clearAllNotificacoes()
        }
    }

    fun toggleNotificationLida(notif: CalendarNotificacao) {
        viewModelScope.launch {
            calendarRepository.saveNotificacao(notif.copy(lida = !notif.lida))
        }
    }

    fun deleteNotification(notif: CalendarNotificacao) {
        viewModelScope.launch {
            calendarRepository.deleteNotificacao(notif.id)
        }
    }
}
