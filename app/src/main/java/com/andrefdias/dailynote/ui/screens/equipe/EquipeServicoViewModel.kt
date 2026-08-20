package com.andrefdias.dailynote.ui.screens.equipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.data.local.dao.CalendarDao
import com.andrefdias.dailynote.domain.calendar.ScaleEngine
import com.andrefdias.dailynote.domain.model.*
import com.andrefdias.dailynote.domain.repository.EquipeServicoRepository
import com.andrefdias.dailynote.domain.repository.MilitarRepository
import com.andrefdias.dailynote.domain.repository.QuartelRepository
import com.andrefdias.dailynote.domain.repository.ViaturaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EquipeServicoViewModel @Inject constructor(
    private val equipeServicoRepository: EquipeServicoRepository,
    private val quartelRepository: QuartelRepository,
    private val viaturaRepository: ViaturaRepository,
    private val militarRepository: MilitarRepository,
    private val calendarDao: CalendarDao
) : ViewModel() {

    private val _data = MutableStateFlow(LocalDate.now().toString())
    val data: StateFlow<String> = _data.asStateFlow()

    private val _unidade = MutableStateFlow("")
    val unidade: StateFlow<String> = _unidade.asStateFlow()

    private val _posto = MutableStateFlow("")
    val posto: StateFlow<String> = _posto.asStateFlow()

    val unidades = quartelRepository.getUnidades().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val postos = _unidade.flatMapLatest { uni ->
        if (uni.isBlank()) flowOf(emptyList()) else quartelRepository.getPostosByUnidade(uni)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val viaturasDisponiveis = combine(_unidade, _posto) { uni, pst ->
        uni to pst
    }.flatMapLatest { (uni, pst) ->
        if (uni.isNotBlank() && pst.isNotBlank()) viaturaRepository.getByLocal(uni, pst)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val militares = militarRepository.getAll().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _escalasConfiguradas = MutableStateFlow<List<EscalaConfig>>(emptyList())
    val escalasConfiguradas = _escalasConfiguradas.asStateFlow()

    private val _equipesTrabalhandoHoje = MutableStateFlow<List<EquipeConfig>>(emptyList())
    val equipesTrabalhandoHoje = _equipesTrabalhandoHoje.asStateFlow()

    val opcoesTipoEscala = combine(_escalasConfiguradas, _equipesTrabalhandoHoje) { escalas, equipes ->
        val list = equipes.mapNotNull { eq ->
            val esc = escalas.find { it.id == eq.escalaId }
            if (esc != null) "${esc.nome} - ${eq.nome}" else null
        }.toMutableList()
        list.add("DEJEM")
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("DEJEM"))

    private val _equipeServico = MutableStateFlow<EquipeServico?>(null)
    val equipeServico: StateFlow<EquipeServico?> = _equipeServico.asStateFlow()

    init {
        viewModelScope.launch {
            val dbEscalas = calendarDao.getEscalas()
            val escalas = dbEscalas.map {
                EscalaConfig(it.id, it.nome, it.trabalhoHoras, it.descansoHoras, it.quantidadeTurnos, it.ativa, it.descricao)
            }
            _escalasConfiguradas.value = escalas

            val dbEquipes = calendarDao.getEquipes()
            val equipes = dbEquipes.map {
                EquipeConfig(it.id, it.nome, it.sigla, it.corFundo, it.corTexto, it.corBorda, it.escalaId, it.dataInicial, it.ordemTurno, it.ativa, it.horaInicio, it.horaTermino)
            }
            
            // Calculate logical shift date
            val now = LocalDateTime.now()
            var logicalDate = now.toLocalDate()
            val activeTeams = ScaleEngine.getActiveTeamsRightNow(now, escalas, equipes)
            val flatActive = activeTeams.values.flatten()
            if (flatActive.isNotEmpty()) {
                val equipe = flatActive.first()
                val horaTermino = ScaleEngine.parseTime(equipe.horaTermino)
                val horaInicio = ScaleEngine.parseTime(equipe.horaInicio)
                if (horaTermino != null && horaInicio != null && !horaTermino.isAfter(horaInicio)) {
                    // Turno cruza meia noite
                    val currentTime = now.toLocalTime()
                    if (currentTime.isBefore(horaTermino)) {
                        // Ainda estamos no turno que começou ontem
                        logicalDate = logicalDate.minusDays(1)
                    }
                }
            }
            _data.value = logicalDate.toString()
        }

        // Load equipe servico when data, unidade or posto changes
        viewModelScope.launch {
            combine(_data, _unidade, _posto) { d, u, p -> Triple(d, u, p) }
                .collectLatest { (d, u, p) ->
                    if (d.isNotBlank() && u.isNotBlank() && p.isNotBlank()) {
                        equipeServicoRepository.getEquipeServico(d, u, p).collect { eq ->
                            if (eq != null) {
                                // Load relational objects
                                val viats = viaturasDisponiveis.firstOrNull() ?: emptyList()
                                val mils = militares.firstOrNull() ?: emptyList()
                                val dbEquipes = calendarDao.getEquipes()
                                val equipesConfig = dbEquipes.map { EquipeConfig(it.id, it.nome, it.sigla, it.corFundo, it.corTexto, it.corBorda, it.escalaId, it.dataInicial, it.ordemTurno, it.ativa, it.horaInicio, it.horaTermino) }
                                
                                val viaturasCompletas = eq.viaturas.map { ev ->
                                    val militaresCompletos = ev.militaresEscalados.map { me ->
                                        me.copy(
                                            militar = mils.find { it.id == me.militarId },
                                            equipeConfig = equipesConfig.find { it.id == me.escalaId }
                                        )
                                    }
                                    ev.copy(
                                        viatura = viats.find { it.id == ev.viaturaId },
                                        militaresEscalados = militaresCompletos
                                    )
                                }
                                _equipeServico.value = eq.copy(viaturas = viaturasCompletas)
                            } else {
                                _equipeServico.value = EquipeServico(
                                    data = d,
                                    unidade = u,
                                    posto = p
                                )
                                // It will be updated by updateEquipesTrabalhandoHoje if there's a team today
                            }
                        }
                    } else {
                        _equipeServico.value = null
                    }
                    updateEquipesTrabalhandoHoje(d)
                }
        }
    }

    private fun updateEquipesTrabalhandoHoje(dataStr: String) {
        viewModelScope.launch {
            try {
                val date = LocalDate.parse(dataStr)
                val dbEscalas = calendarDao.getEscalas()
                val escalas = dbEscalas.map { EscalaConfig(it.id, it.nome, it.trabalhoHoras, it.descansoHoras, it.quantidadeTurnos, it.ativa, it.descricao) }
                val dbEquipes = calendarDao.getEquipes()
                val equipes = dbEquipes.map { EquipeConfig(it.id, it.nome, it.sigla, it.corFundo, it.corTexto, it.corBorda, it.escalaId, it.dataInicial, it.ordemTurno, it.ativa, it.horaInicio, it.horaTermino) }
                
                val working = ScaleEngine.getActiveTeamsForDate(date, escalas, equipes)
                val flatWorking = working.values.flatten()
                _equipesTrabalhandoHoje.value = flatWorking

                val currentEq = _equipeServico.value
                if (currentEq != null && flatWorking.isNotEmpty() && (currentEq.tipoEscala == "24h" || currentEq.tipoEscala.isBlank())) {
                    val firstWorking = flatWorking.first()
                    val esc = escalas.find { it.id == firstWorking.escalaId }
                    if (esc != null) {
                        _equipeServico.value = currentEq.copy(
                            tipoEscala = "${esc.nome} - ${firstWorking.nome}",
                            escalaId = firstWorking.id
                        )
                    }
                }

            } catch (e: Exception) {
                _equipesTrabalhandoHoje.value = emptyList()
            }
        }
    }

    fun setFiltros(data: String, unidade: String, posto: String) {
        _data.value = data
        _unidade.value = unidade
        _posto.value = posto
    }

    fun setTipoEscala(tipo: String) {
        // Encontrar a equipe correspondente
        val parts = tipo.split(" - ")
        var newEscalaId: String? = null
        if (parts.size == 2) {
            val nomeEquipe = parts[1]
            val equipeConfig = _equipesTrabalhandoHoje.value.find { it.nome == nomeEquipe }
            newEscalaId = equipeConfig?.id
        }

        _equipeServico.value = _equipeServico.value?.copy(tipoEscala = tipo, escalaId = newEscalaId)
        if (tipo != "DEJEM") {
            _equipeServico.value = _equipeServico.value?.copy(dejemHorarioInicio = null, dejemHorarioFim = null)
        }
    }

    fun setDejemHorario(inicio: String) {
        _equipeServico.value = _equipeServico.value?.copy(dejemHorarioInicio = inicio)
        try {
            val time = LocalTime.parse(inicio)
            val fim = time.plusHours(8).format(DateTimeFormatter.ofPattern("HH:mm"))
            _equipeServico.value = _equipeServico.value?.copy(dejemHorarioFim = fim)
        } catch (e: Exception) {
            _equipeServico.value = _equipeServico.value?.copy(dejemHorarioFim = null)
        }
    }

    fun addViatura(viaturaId: String) {
        val current = _equipeServico.value ?: return
        val novaLista = current.viaturas.toMutableList()
        val novaViatura = EquipeViatura(
            equipeServicoId = current.id,
            viaturaId = viaturaId
        )
        // Resolve domain object immediately if available
        val v = viaturasDisponiveis.value.find { it.id == viaturaId }
        novaLista.add(novaViatura.copy(viatura = v))
        
        _equipeServico.value = current.copy(viaturas = novaLista)
    }

    private val _validationEvent = MutableSharedFlow<String>()
    val validationEvent = _validationEvent.asSharedFlow()

    fun updateViatura(viaturaAtualizada: EquipeViatura) {
        val current = _equipeServico.value ?: return

        val idsNaViatura = viaturaAtualizada.militaresEscalados.map { it.militarId }

        if (idsNaViatura.size != idsNaViatura.distinct().size) {
            viewModelScope.launch { _validationEvent.emit("Militar não pode ocupar mais de uma função na mesma viatura.") }
            return
        }

        val outrasViaturas = current.viaturas.filter { it.id != viaturaAtualizada.id }
        val idsOutrasViaturas = outrasViaturas.flatMap { v ->
            v.militaresEscalados.map { it.militarId }
        }.toSet()

        val duplicates = idsNaViatura.intersect(idsOutrasViaturas)
        if (duplicates.isNotEmpty()) {
            viewModelScope.launch { _validationEvent.emit("Este militar já está escalado em outra viatura neste dia.") }
            return
        }

        val novaLista = current.viaturas.map { 
            if (it.id == viaturaAtualizada.id) viaturaAtualizada else it 
        }
        _equipeServico.value = current.copy(viaturas = novaLista)
    }

    fun removeViatura(viaturaId: String) {
        val current = _equipeServico.value ?: return
        val novaLista = current.viaturas.filter { it.id != viaturaId }
        _equipeServico.value = current.copy(viaturas = novaLista)
    }

    fun saveEquipe() {
        val current = _equipeServico.value ?: return
        viewModelScope.launch {
            equipeServicoRepository.saveEquipeServico(current)
        }
    }

    fun limparEscala() {
        val current = _equipeServico.value ?: return
        _equipeServico.value = current.copy(viaturas = emptyList())
        viewModelScope.launch {
            equipeServicoRepository.deleteEquipeServico(current.id)
        }
    }
}
