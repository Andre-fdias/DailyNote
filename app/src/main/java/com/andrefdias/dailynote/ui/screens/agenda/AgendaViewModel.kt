package com.andrefdias.dailynote.ui.screens.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.model.CalendarEvento
import com.andrefdias.dailynote.domain.model.CalendarTarefa
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.andrefdias.dailynote.domain.repository.SettingsRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.andrefdias.dailynote.domain.model.EquipeConfig
import com.andrefdias.dailynote.domain.model.EscalaConfig
import com.andrefdias.dailynote.domain.calendar.ScaleEngine
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.andrefdias.dailynote.domain.calendar.NotificationScheduler

enum class EventSourceFilter(val label: String) {
    ALL("Ambas as Agendas"),
    DAILY_NOTES("Somente Fire Notes"),
    GOOGLE("Somente Google"),
    NONE("Ocultar Tudo")
}

data class AgendaState(
    val selectedDate: LocalDate = LocalDate.now(),
    val eventosDoDia: List<CalendarEvento> = emptyList(),
    val tarefasDoDia: List<CalendarTarefa> = emptyList(),
    val todosEventos: List<CalendarEvento> = emptyList(),
    val todasTarefas: List<CalendarTarefa> = emptyList(),
    val equipes: List<EquipeConfig> = emptyList(),
    val escalas: List<EscalaConfig> = emptyList(),
    val escalasPorDia: Map<LocalDate, Map<Int, List<EquipeConfig>>> = emptyMap(),
    val selectedEscalaFilter: String? = null,
    val eventSourceFilter: EventSourceFilter = EventSourceFilter.ALL
)

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val repository: CalendarRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AgendaState())
    val state: StateFlow<AgendaState> = _state.asStateFlow()

    init {
        loadData()
        loadAllData()
        viewModelScope.launch {
            settingsRepository.activeCalendarFilterFlow.collect { filter ->
                val activeFilter = if (filter == "Todos") null else filter
                _state.value = _state.value.copy(selectedEscalaFilter = activeFilter)
                loadData()
            }
        }
    }

    fun setEscalaFilter(filter: String?) {
        viewModelScope.launch {
            settingsRepository.setActiveCalendarFilter(filter ?: "Todos")
        }
    }

    fun setEventSourceFilter(filter: EventSourceFilter) {
        _state.value = _state.value.copy(eventSourceFilter = filter)
        loadAllData()
    }

    fun selectDate(date: LocalDate) {
        _state.value = _state.value.copy(selectedDate = date)
        loadData()
    }
    
    fun precomputeScales(startMonthDate: LocalDate) {
        if (_state.value.equipes.isEmpty() || _state.value.escalas.isEmpty()) return
        val map = ScaleEngine.getPrecomputedMonthScales(startMonthDate, _state.value.escalas, _state.value.equipes)
        // Merge with existing maps
        val newMap = _state.value.escalasPorDia.toMutableMap()
        newMap.putAll(map)
        _state.value = _state.value.copy(escalasPorDia = newMap)
    }

    private fun loadData() {
        viewModelScope.launch {
            val dateStr = _state.value.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            repository.getEventosForDayFlow(dateStr).collect { eventos ->
                _state.value = _state.value.copy(eventosDoDia = eventos)
            }
        }
        viewModelScope.launch {
            val dateStr = _state.value.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            repository.getTarefasForDayFlow(dateStr).collect { tarefas ->
                _state.value = _state.value.copy(tarefasDoDia = tarefas)
            }
        }
    }

    private fun loadAllData() {
        viewModelScope.launch {
            combine(
                repository.getAllEventosFlow(),
                repository.getAllTarefasFlow(),
                repository.getEquipesFlow(),
                repository.getEscalasFlow(),
                _state.map { it.selectedEscalaFilter }.distinctUntilChanged()
            ) { args ->
                val allEventos = args[0] as List<CalendarEvento>
                val allTarefas = args[1] as List<CalendarTarefa>
                val allEquipes = args[2] as List<EquipeConfig>
                val allEscalas = args[3] as List<EscalaConfig>
                val filter = args[4] as String?

                val baseEventos = if (filter == null) allEventos else allEventos.filter { it.escalaId == null || it.escalaId == filter }
                val baseTarefas = if (filter == null) allTarefas else allTarefas.filter { it.escalaId == null || it.escalaId == filter }
                val filteredEquipes = if (filter == null) allEquipes else allEquipes.filter { it.escalaId == filter }

                val sourceFilter = _state.value.eventSourceFilter
                
                val finalEventos = when (sourceFilter) {
                    EventSourceFilter.ALL -> baseEventos
                    EventSourceFilter.DAILY_NOTES -> baseEventos.filter { it.googleEventId == null }
                    EventSourceFilter.GOOGLE -> baseEventos.filter { it.googleEventId != null }
                    EventSourceFilter.NONE -> emptyList()
                }
                
                val finalTarefas = when (sourceFilter) {
                    EventSourceFilter.ALL, EventSourceFilter.DAILY_NOTES -> baseTarefas
                    EventSourceFilter.GOOGLE, EventSourceFilter.NONE -> emptyList()
                }

                _state.value.copy(
                    todosEventos = finalEventos,
                    todasTarefas = finalTarefas,
                    equipes = filteredEquipes,
                    escalas = allEscalas
                )
            }.collect { newState ->
                _state.value = newState
                precomputeScales(_state.value.selectedDate.withDayOfMonth(1))
            }
        }
    }

    fun toggleTarefaStatus(tarefa: CalendarTarefa) {
        val novoStatus = if (tarefa.status == com.andrefdias.dailynote.domain.model.StatusTarefa.CONCLUIDA) {
            com.andrefdias.dailynote.domain.model.StatusTarefa.PENDENTE
        } else {
            com.andrefdias.dailynote.domain.model.StatusTarefa.CONCLUIDA
        }
        
        viewModelScope.launch {
            repository.saveTarefa(tarefa.copy(status = novoStatus))
        }
    }

    fun saveEvento(id: String? = null, titulo: String, descricao: String = "", data: String, hora: String?, local: String? = null, escalaId: String? = null, cor: String) {
        viewModelScope.launch {
            val eventoToSave = CalendarEvento(
                id = id ?: java.util.UUID.randomUUID().toString(),
                titulo = titulo,
                descricao = descricao,
                data = data,
                hora = hora,
                local = local ?: "",
                categoria = com.andrefdias.dailynote.domain.model.CategoriaEvento.PERSONALIZADO,
                cor = cor,
                recorrencia = com.andrefdias.dailynote.domain.model.RecorrenciaTipo.NUNCA,
                lembreteMinutos = 60,
                escalaId = escalaId
            )
            repository.saveEvento(eventoToSave)
            // Agenda 3 lembretes: 1 dia antes, 1 hora antes, e no horário exato
            NotificationScheduler.scheduleForEvento(context, eventoToSave)
        }
    }

    fun saveTarefa(id: String? = null, titulo: String, descricao: String = "", data: String, hora: String?, escalaId: String? = null, cor: String? = null, checklist: List<com.andrefdias.dailynote.domain.model.ChecklistItem> = emptyList()) {
        viewModelScope.launch {
            val tarefaToSave = CalendarTarefa(
                id = id ?: java.util.UUID.randomUUID().toString(),
                titulo = titulo,
                descricao = descricao,
                data = data,
                hora = hora,
                prioridade = com.andrefdias.dailynote.domain.model.PrioridadeTarefa.MEDIA,
                status = com.andrefdias.dailynote.domain.model.StatusTarefa.PENDENTE,
                categoria = "Geral",
                responsavel = null,
                escalaId = escalaId,
                cor = cor,
                checklist = checklist
            )
            repository.saveTarefa(tarefaToSave)
            // Agenda 3 lembretes: 1 dia antes, 1 hora antes, e no horário exato
            NotificationScheduler.scheduleForTarefa(context, tarefaToSave)
        }
    }

    fun deleteTarefa(tarefa: CalendarTarefa) {
        viewModelScope.launch {
            NotificationScheduler.cancelRemindersFor(context, tarefa.id)
            repository.deleteTarefa(tarefa.id)
        }
    }
    
    fun deleteEvento(evento: CalendarEvento) {
        viewModelScope.launch {
            NotificationScheduler.cancelRemindersFor(context, evento.id)
            repository.deleteEvento(evento.id)
        }
    }
}
