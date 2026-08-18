package com.andrefdias.dailynote.ui.screens.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.calendar.ScaleEngine
import com.andrefdias.dailynote.domain.calendar.NotificationCenter
import com.andrefdias.dailynote.domain.model.*
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import com.andrefdias.dailynote.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: LocalDate = LocalDate.now().withDayOfMonth(1),
    val viewType: CalendarViewType = CalendarViewType.MONTH,
    val scales: List<EscalaConfig> = emptyList(),
    val teams: List<EquipeConfig> = emptyList(),
    val events: List<CalendarEvento> = emptyList(),
    val tasks: List<CalendarTarefa> = emptyList(),
    val settings: CalendarSettings = CalendarSettings(),
    val settingsLoaded: Boolean = false,
    val activeLayers: Set<String> = setOf("ESCALA", "EVENTO", "TAREFA"),
    val activeTeamsOnSelectedDate: Map<Int, List<EquipeConfig>> = emptyMap(),
    val activeTeamsRightNow: Map<Int, List<EquipeConfig>> = emptyMap(),
    val consecutiveWorkDays: Int = 0,
    val selectedEscalaFilter: String? = null
)

enum class CalendarViewType {
    MONTH, WEEK, THREE_DAYS, DAY, AGENDA
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadData()
        startRightNowTimer()
        viewModelScope.launch {
            settingsRepository.activeCalendarFilterFlow.collect { filter ->
                val activeFilter = if (filter == "Todos") null else filter
                _uiState.update { it.copy(selectedEscalaFilter = activeFilter) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                calendarRepository.getEscalasFlow(),
                calendarRepository.getEquipesFlow(),
                calendarRepository.getAllEventosFlow(),
                calendarRepository.getAllTarefasFlow(),
                calendarRepository.getSettingsFlow(),
                _uiState.map { it.selectedEscalaFilter }.distinctUntilChanged()
            ) { args ->
                val scales = args[0] as List<EscalaConfig>
                val allTeams = args[1] as List<EquipeConfig>
                val allEvents = args[2] as List<CalendarEvento>
                val allTasks = args[3] as List<CalendarTarefa>
                val settings = args[4] as? CalendarSettings
                val filter = args[5] as String?

                val teams = when (filter) {
                    "NONE" -> emptyList()
                    null -> allTeams
                    else -> allTeams.filter { it.escalaId == filter }
                }

                val events = when (filter) {
                    "NONE" -> allEvents.filter { it.escalaId == null }
                    null -> allEvents
                    else -> allEvents.filter { it.escalaId == null || it.escalaId == filter }
                }

                val tasks = when (filter) {
                    "NONE" -> allTasks.filter { it.escalaId == null }
                    null -> allTasks
                    else -> allTasks.filter { it.escalaId == null || it.escalaId == filter }
                }

                val currentState = _uiState.value
                val date = currentState.selectedDate
                
                val activeTeams = ScaleEngine.getActiveTeamsForDate(date, scales, teams)
                
                val firstTeam = activeTeams.values.flatten().firstOrNull()
                val consecutive = if (firstTeam != null) {
                    ScaleEngine.getConsecutiveWorkDays(date, scales, teams, firstTeam.id)
                } else 0

                currentState.copy(
                    scales = scales,
                    teams = teams,
                    events = events,
                    tasks = tasks,
                    settings = settings ?: CalendarSettings(),
                    settingsLoaded = true,
                    activeTeamsOnSelectedDate = activeTeams,
                    activeTeamsRightNow = ScaleEngine.getActiveTeamsRightNow(
                        java.time.LocalDateTime.now(), scales, teams
                    ),
                    consecutiveWorkDays = consecutive,
                    selectedEscalaFilter = filter
                )
            }.collect { updatedState ->
                _uiState.value = updatedState
            }
        }
    }

    fun selectDate(date: LocalDate) {
        viewModelScope.launch {
            val scales = _uiState.value.scales
            val teams = _uiState.value.teams
            val activeTeams = ScaleEngine.getActiveTeamsForDate(date, scales, teams)
            
            val firstTeam = activeTeams.values.flatten().firstOrNull()
            val consecutive = if (firstTeam != null) {
                ScaleEngine.getConsecutiveWorkDays(date, scales, teams, firstTeam.id)
            } else 0

            _uiState.update {
                it.copy(
                    selectedDate = date,
                    activeTeamsOnSelectedDate = activeTeams,
                    consecutiveWorkDays = consecutive
                )
            }
        }
    }

    private fun startRightNowTimer() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000L)
                val state = _uiState.value
                _uiState.update {
                    it.copy(
                        activeTeamsRightNow = ScaleEngine.getActiveTeamsRightNow(
                            java.time.LocalDateTime.now(), state.scales, state.teams
                        )
                    )
                }
            }
        }
    }

    fun setEscalaFilter(filter: String?) {
        viewModelScope.launch {
            settingsRepository.setActiveCalendarFilter(filter ?: "Todos")
        }
    }

    fun selectMonth(month: LocalDate) {
        _uiState.update { it.copy(currentMonth = month) }
    }

    fun setViewType(viewType: CalendarViewType) {
        _uiState.update { it.copy(viewType = viewType) }
    }

    fun toggleLayer(layer: String) {
        _uiState.update { state ->
            val newLayers = state.activeLayers.toMutableSet()
            if (newLayers.contains(layer)) {
                newLayers.remove(layer)
            } else {
                newLayers.add(layer)
            }
            state.copy(activeLayers = newLayers)
        }
    }

    fun addEvento(
        titulo: String,
        descricao: String,
        data: LocalDate,
        hora: LocalTime?,
        local: String?,
        categoria: CategoriaEvento,
        cor: String,
        recorrencia: RecorrenciaTipo,
        lembreteMinutos: Int?,
        escalaId: String? = null
    ) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val evento = CalendarEvento(
                id = id,
                titulo = titulo,
                descricao = descricao,
                data = data.toString(),
                hora = hora?.toString()?.take(5),
                local = local,
                categoria = categoria,
                cor = cor,
                recorrencia = recorrencia,
                lembreteMinutos = lembreteMinutos,
                escalaId = escalaId
            )
            calendarRepository.saveEvento(evento)

            if (lembreteMinutos != null) {
                val triggerTime = java.time.LocalDateTime.of(data, hora ?: LocalTime.of(8, 0))
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli() - (lembreteMinutos * 60 * 1000)
                
                NotificationCenter.scheduleReminder(
                    context = context,
                    id = id,
                    titulo = titulo,
                    descricao = descricao,
                    timeInMillis = triggerTime,
                    categoria = CategoriaNotificacao.EVENTOS
                )
            }

            NotificationCenter.dispatchNotification(
                context = context,
                repository = calendarRepository,
                categoria = CategoriaNotificacao.EVENTOS,
                titulo = "Novo Evento Cadastrado",
                descricao = "$titulo agendado para $data"
            )
        }
    }

    fun deleteEvento(id: String) {
        viewModelScope.launch {
            calendarRepository.deleteEvento(id)
        }
    }

    fun addTarefa(
        titulo: String,
        descricao: String,
        data: LocalDate,
        hora: LocalTime?,
        prioridade: PrioridadeTarefa,
        categoria: String,
        responsavel: String?,
        checklist: List<ChecklistItem>,
        escalaId: String? = null
    ) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val tarefa = CalendarTarefa(
                id = id,
                titulo = titulo,
                descricao = descricao,
                data = data.toString(),
                hora = hora?.toString()?.take(5),
                prioridade = prioridade,
                status = StatusTarefa.PENDENTE,
                categoria = categoria,
                responsavel = responsavel,
                checklist = checklist,
                escalaId = escalaId
            )
            calendarRepository.saveTarefa(tarefa)

            val triggerTime = java.time.LocalDateTime.of(data, hora ?: LocalTime.of(8, 0))
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            
            NotificationCenter.scheduleReminder(
                context = context,
                id = id,
                titulo = "Tarefa Pendente: $titulo",
                descricao = descricao,
                timeInMillis = triggerTime,
                categoria = CategoriaNotificacao.TAREFAS
            )

            NotificationCenter.dispatchNotification(
                context = context,
                repository = calendarRepository,
                categoria = CategoriaNotificacao.TAREFAS,
                titulo = "Nova Tarefa Criada",
                descricao = "Pendente: $titulo"
            )
        }
    }

    fun toggleTarefaStatus(tarefa: CalendarTarefa) {
        viewModelScope.launch {
            val newStatus = if (tarefa.status == StatusTarefa.CONCLUIDA) StatusTarefa.PENDENTE else StatusTarefa.CONCLUIDA
            val updated = tarefa.copy(status = newStatus)
            calendarRepository.saveTarefa(updated)

            if (newStatus == StatusTarefa.CONCLUIDA) {
                NotificationCenter.dispatchNotification(
                    context = context,
                    repository = calendarRepository,
                    categoria = CategoriaNotificacao.TAREFAS,
                    titulo = "Tarefa Concluída",
                    descricao = "Sucesso: ${tarefa.titulo}"
                )
            }
        }
    }

    fun deleteTarefa(id: String) {
        viewModelScope.launch {
            calendarRepository.deleteTarefa(id)
        }
    }

    fun setPopupExibido() {
        viewModelScope.launch {
            val settings = _uiState.value.settings
            calendarRepository.saveSettings(settings.copy(popupExibidoHoje = LocalDate.now().toString()))
        }
    }
}
