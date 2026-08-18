package com.andrefdias.dailynote.ui.screens.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.model.*
import com.andrefdias.dailynote.domain.calendar.ScaleEngine
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import com.andrefdias.dailynote.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

// ──────────────────────────────────────────────
// HOME VIEW MODEL (without Occurrences)
// ──────────────────────────────────────────────
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow<LocalDate>(LocalDate.now().withDayOfMonth(1))
    val currentMonth: StateFlow<LocalDate> = _currentMonth.asStateFlow()

    private val _allEventos = MutableStateFlow<List<CalendarEvento>>(emptyList())
    val allEventos: StateFlow<List<CalendarEvento>> = _allEventos.asStateFlow()

    private val _allTarefas = MutableStateFlow<List<CalendarTarefa>>(emptyList())
    val allTarefas: StateFlow<List<CalendarTarefa>> = _allTarefas.asStateFlow()

    private val _notifications = MutableStateFlow<List<CalendarNotificacao>>(emptyList())
    val notifications: StateFlow<List<CalendarNotificacao>> = _notifications.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

    private val _selectedEscalaFilter = MutableStateFlow<String?>(null)
    val selectedEscalaFilter: StateFlow<String?> = _selectedEscalaFilter.asStateFlow()

    private val _availableEscalas = MutableStateFlow<List<EscalaConfig>>(emptyList())
    val availableEscalas: StateFlow<List<EscalaConfig>> = _availableEscalas.asStateFlow()

    private val _previewDays = MutableStateFlow<Map<LocalDate, Map<Int, List<EquipeConfig>>>>(emptyMap())
    val previewDays: StateFlow<Map<LocalDate, Map<Int, List<EquipeConfig>>>> = _previewDays.asStateFlow()

    private val _hasDismissedAlertsThisSession = MutableStateFlow(false)
    val hasDismissedAlertsThisSession: StateFlow<Boolean> = _hasDismissedAlertsThisSession.asStateFlow()

    init {
        observeData()
        observeEscalaFilter()
        observePreviewDays()
    }

    private fun observeData() {
        viewModelScope.launch {
            calendarRepository.getAllEventosFlow().collect {
                _allEventos.value = it
            }
        }
        viewModelScope.launch {
            calendarRepository.getAllTarefasFlow().collect {
                _allTarefas.value = it
            }
        }
        viewModelScope.launch {
            calendarRepository.getNotificacoesFlow().collect {
                _notifications.value = it
                _unreadNotificationCount.value = it.count { n -> !n.lida }
            }
        }
    }

    private fun observeEscalaFilter() {
        viewModelScope.launch {
            settingsRepository.activeCalendarFilterFlow.collect { filter ->
                _selectedEscalaFilter.value = if (filter == "Todos") null else filter
            }
        }
    }

    private fun observePreviewDays() {
        viewModelScope.launch {
            combine(
                _selectedEscalaFilter,
                _currentMonth,
                calendarRepository.getEscalasFlow(),
                calendarRepository.getEquipesFlow()
            ) { filter, month, scales, teams ->
                _availableEscalas.value = scales
                if (filter == "NONE") {
                    emptyMap()
                } else {
                    val filteredScales = if (filter == null) scales else scales.filter { it.id == filter }
                    val filteredTeams = if (filter == null) teams else teams.filter { it.escalaId == filter }
                    ScaleEngine.getPrecomputedMonthScales(month, filteredScales, filteredTeams)
                }
            }.collect { _previewDays.value = it }
        }
    }

    // ── Navigation ──────────────────────────────
    fun selectDate(date: LocalDate) { _selectedDate.value = date }
    fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }
    fun previousMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }
    fun dismissAlertsPermanently() { _hasDismissedAlertsThisSession.value = true }

    fun setEscalaFilter(filter: String?) {
        viewModelScope.launch { settingsRepository.setActiveCalendarFilter(filter ?: "Todos") }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(800)
            _isRefreshing.value = false
        }
    }

    // ── Eventos ──────────────────────────────────
    fun addEvento(
        titulo: String, descricao: String = "", data: LocalDate,
        hora: String? = null, local: String? = null, cor: String = "#3B82F6",
        escalaId: String? = null, lembreteMinutos: Int? = null
    ) {
        if (titulo.isBlank()) return
        viewModelScope.launch {
            calendarRepository.saveEvento(
                CalendarEvento(
                    id = UUID.randomUUID().toString(),
                    titulo = titulo.trim(),
                    descricao = descricao,
                    data = data.toString(),
                    hora = hora,
                    local = local,
                    categoria = CategoriaEvento.PERSONALIZADO,
                    cor = cor,
                    recorrencia = RecorrenciaTipo.NUNCA,
                    lembreteMinutos = lembreteMinutos ?: 0,
                    escalaId = escalaId
                )
            )
        }
    }

    fun updateEvento(evento: CalendarEvento) {
        viewModelScope.launch { calendarRepository.saveEvento(evento) }
    }

    fun deleteEvento(id: String) {
        viewModelScope.launch { calendarRepository.deleteEvento(id) }
    }

    // ── Tarefas ──────────────────────────────────
    fun addTarefa(
        titulo: String, descricao: String = "", data: LocalDate,
        hora: String? = null, prioridade: PrioridadeTarefa = PrioridadeTarefa.MEDIA,
        categoria: String = "Operacional", cor: String = "#10B981", escalaId: String? = null,
        subtarefas: List<SubtarefaInput> = emptyList()
    ) {
        if (titulo.isBlank()) return
        viewModelScope.launch {
            calendarRepository.saveTarefa(
                CalendarTarefa(
                    id = UUID.randomUUID().toString(),
                    titulo = titulo.trim(),
                    descricao = descricao,
                    data = data.toString(),
                    hora = hora,
                    prioridade = prioridade,
                    status = StatusTarefa.PENDENTE,
                    categoria = categoria,
                    responsavel = null,
                    escalaId = escalaId,
                    checklist = subtarefas.map { 
                        ChecklistItem(id = it.id, titulo = it.titulo, concluido = it.concluida)
                    }
                )
            )
        }
    }

    fun toggleTarefa(tarefa: CalendarTarefa) {
        viewModelScope.launch {
            val newStatus = if (tarefa.status == StatusTarefa.CONCLUIDA) StatusTarefa.PENDENTE else StatusTarefa.CONCLUIDA
            calendarRepository.saveTarefa(tarefa.copy(status = newStatus))
        }
    }

    fun updateTarefa(tarefa: CalendarTarefa) {
        viewModelScope.launch { calendarRepository.saveTarefa(tarefa) }
    }

    fun deleteTarefa(id: String) {
        viewModelScope.launch { calendarRepository.deleteTarefa(id) }
    }

    // ── Notifications ────────────────────────────
    fun markAllNotificacoesAsRead() {
        viewModelScope.launch { calendarRepository.markAllAsRead() }
    }

    fun deleteNotificacao(id: String) {
        viewModelScope.launch { calendarRepository.deleteNotificacao(id) }
    }

    fun clearAllNotificacoes() {
        viewModelScope.launch { calendarRepository.clearAllNotificacoes() }
    }
}
