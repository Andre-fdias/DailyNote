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
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.andrefdias.dailynote.domain.model.EquipeConfig
import com.andrefdias.dailynote.domain.model.EscalaConfig
import com.andrefdias.dailynote.domain.calendar.ScaleEngine

data class AgendaState(
    val selectedDate: LocalDate = LocalDate.now(),
    val eventosDoDia: List<CalendarEvento> = emptyList(),
    val tarefasDoDia: List<CalendarTarefa> = emptyList(),
    val todosEventos: List<CalendarEvento> = emptyList(),
    val todasTarefas: List<CalendarTarefa> = emptyList(),
    val equipes: List<EquipeConfig> = emptyList(),
    val escalas: List<EscalaConfig> = emptyList(),
    val escalasPorDia: Map<LocalDate, Map<Int, List<EquipeConfig>>> = emptyMap()
)

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AgendaState())
    val state: StateFlow<AgendaState> = _state.asStateFlow()

    init {
        loadData()
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
            repository.getAllEventosFlow().collect { todos ->
                _state.value = _state.value.copy(todosEventos = todos)
            }
        }
        viewModelScope.launch {
            repository.getAllTarefasFlow().collect { todas ->
                _state.value = _state.value.copy(todasTarefas = todas)
            }
        }
        viewModelScope.launch {
            repository.getEquipesFlow().collect { equipes ->
                _state.value = _state.value.copy(equipes = equipes)
                // Precompute after loading
                precomputeScales(_state.value.selectedDate.withDayOfMonth(1))
            }
        }
        viewModelScope.launch {
            repository.getEscalasFlow().collect { escalas ->
                _state.value = _state.value.copy(escalas = escalas)
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
                lembreteMinutos = 15,
                escalaId = escalaId
            )
            repository.saveEvento(eventoToSave)
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
        }
    }

    fun deleteTarefa(tarefa: CalendarTarefa) {
        viewModelScope.launch {
            repository.deleteTarefa(tarefa.id)
        }
    }
    
    fun deleteEvento(evento: CalendarEvento) {
        viewModelScope.launch {
            repository.deleteEvento(evento.id)
        }
    }
}
