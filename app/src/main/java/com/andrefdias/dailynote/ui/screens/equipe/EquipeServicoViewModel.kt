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
        if (!list.contains("24h")) list.add("24h")
        if (!list.contains("12h")) list.add("12h")
        list.add("DEJEM")
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("24h", "12h", "DEJEM"))

    private val _equipeServico = MutableStateFlow<EquipeServico?>(null)
    val equipeServico: StateFlow<EquipeServico?> = _equipeServico.asStateFlow()

    init {
        viewModelScope.launch {
            val dbEscalas = calendarDao.getEscalas()
            _escalasConfiguradas.value = dbEscalas.map {
                EscalaConfig(it.id, it.nome, it.trabalhoHoras, it.descansoHoras, it.quantidadeTurnos, it.ativa, it.descricao)
            }
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
                                val viaturasCompletas = eq.viaturas.map { ev ->
                                    ev.copy(
                                        viatura = viats.find { it.id == ev.viaturaId },
                                        motorista = mils.find { it.id == ev.motoristaId },
                                        comandante = mils.find { it.id == ev.comandanteId },
                                        auxiliares = ev.auxiliaresIds.mapNotNull { aid -> mils.find { it.id == aid } }
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
                        _equipeServico.value = currentEq.copy(tipoEscala = "${esc.nome} - ${firstWorking.nome}")
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
        _equipeServico.value = _equipeServico.value?.copy(tipoEscala = tipo)
        if (tipo != "DEJEM") {
            _equipeServico.value = _equipeServico.value?.copy(dejemHorarioInicio = null, dejemHorarioFim = null)
        }
    }

    fun setDejemHorario(inicio: String) {
        try {
            val time = LocalTime.parse(inicio)
            val fim = time.plusHours(8).format(DateTimeFormatter.ofPattern("HH:mm"))
            _equipeServico.value = _equipeServico.value?.copy(
                dejemHorarioInicio = inicio,
                dejemHorarioFim = fim
            )
        } catch (e: Exception) {
            // ignore invalid time
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

        val idsNaViatura = mutableListOf<String>()
        viaturaAtualizada.comandanteId?.takeIf { it.isNotBlank() }?.let { idsNaViatura.add(it) }
        viaturaAtualizada.motoristaId?.takeIf { it.isNotBlank() }?.let { idsNaViatura.add(it) }
        idsNaViatura.addAll(viaturaAtualizada.auxiliaresIds.filter { it.isNotBlank() })

        if (idsNaViatura.size != idsNaViatura.distinct().size) {
            viewModelScope.launch { _validationEvent.emit("Militar não pode ocupar mais de uma função na mesma viatura.") }
            return
        }

        val outrasViaturas = current.viaturas.filter { it.id != viaturaAtualizada.id }
        val idsOutrasViaturas = outrasViaturas.flatMap { v ->
            listOfNotNull(v.comandanteId, v.motoristaId) + v.auxiliaresIds
        }.filter { it.isNotBlank() }.toSet()

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
}
