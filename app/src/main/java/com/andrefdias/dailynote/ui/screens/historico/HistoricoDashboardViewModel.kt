package com.andrefdias.dailynote.ui.screens.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.usecase.OcorrenciaAssociacaoUseCase
import com.andrefdias.dailynote.domain.model.Militar
import com.andrefdias.dailynote.domain.model.OcorrenciaComMilitares
import com.andrefdias.dailynote.domain.model.Viatura
import com.andrefdias.dailynote.domain.repository.EquipeServicoRepository
import com.andrefdias.dailynote.domain.repository.MilitarRepository
import com.andrefdias.dailynote.domain.repository.OcorrenciaRepository
import com.andrefdias.dailynote.domain.repository.ViaturaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HistoricoDashboardState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val ocorrenciasTotais: List<OcorrenciaComMilitares> = emptyList(),
    val ocorrenciasFiltradas: List<OcorrenciaComMilitares> = emptyList(),
    val todosMilitares: List<Militar> = emptyList(),
    val todasViaturas: List<Viatura> = emptyList(),
    
    // API State
    val totalRegistros: Int = 0,
    val paginaAtual: Int = 1,
    val totalPaginas: Int = 1,

    // Listas para Filtros Dinâmicos
    val cidadesDisponiveis: List<String> = emptyList(),
    val naturezasDisponiveis: List<String> = emptyList(),

    // Filtros
    val filtroDataInicio: LocalDate? = null,
    val filtroDataFim: LocalDate? = null,
    val filtroMilitarId: String? = null,
    val filtroViaturaId: String? = null,
    val filtroNatureza: String? = null,
    val filtroCidade: String? = null,
    val filtroTextoLivre: String = ""
)

@HiltViewModel
class HistoricoDashboardViewModel @Inject constructor(
    private val ocorrenciaRepository: OcorrenciaRepository,
    private val equipeRepository: EquipeServicoRepository,
    private val militarRepository: MilitarRepository,
    private val viaturaRepository: ViaturaRepository,
    private val associacaoUseCase: OcorrenciaAssociacaoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistoricoDashboardState())
    val state: StateFlow<HistoricoDashboardState> = _state.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    init {
        carregarDadosBase()
        val hoje = LocalDate.now()
        atualizarFiltroData(hoje.minusDays(30), hoje)
    }

    private fun carregarDadosBase() {
        viewModelScope.launch {
            militarRepository.getAll().collect { militares ->
                _state.value = _state.value.copy(todosMilitares = militares)
            }
        }
        viewModelScope.launch {
            viaturaRepository.getAll().collect { viaturas ->
                _state.value = _state.value.copy(todasViaturas = viaturas)
            }
        }
    }

    fun atualizarFiltroData(inicio: LocalDate?, fim: LocalDate?) {
        _state.value = _state.value.copy(filtroDataInicio = inicio, filtroDataFim = fim, paginaAtual = 1)
        buscarOcorrencias()
    }

    fun atualizarFiltroMilitar(militarId: String?) {
        _state.value = _state.value.copy(filtroMilitarId = militarId)
        aplicarFiltrosLocais()
    }

    fun atualizarFiltroViatura(viaturaId: String?) {
        _state.value = _state.value.copy(filtroViaturaId = viaturaId)
        aplicarFiltrosLocais()
    }

    fun setFiltroNatureza(natureza: String?) {
        _state.value = _state.value.copy(filtroNatureza = natureza)
        aplicarFiltrosLocais()
    }

    fun setFiltroCidade(cidade: String?) {
        _state.value = _state.value.copy(filtroCidade = cidade)
        aplicarFiltrosLocais()
    }
    
    fun setFiltroTextoLivre(texto: String) {
        _state.value = _state.value.copy(filtroTextoLivre = texto)
        aplicarFiltrosLocais()
    }

    fun limparFiltros() {
        _state.value = _state.value.copy(
            filtroDataInicio = null,
            filtroDataFim = null,
            filtroMilitarId = null,
            filtroViaturaId = null,
            filtroNatureza = null,
            filtroCidade = null,
            filtroTextoLivre = "",
            paginaAtual = 1
        )
        aplicarFiltrosLocais()
    }

    fun buscarOcorrencias() {
        val inicio = _state.value.filtroDataInicio?.format(dateFormatter)
        val fim = _state.value.filtroDataFim?.format(dateFormatter)

        // Podemos buscar na API enviando RE ou VTR também
        val militarSelected = _state.value.todosMilitares.find { it.id == _state.value.filtroMilitarId }
        val reBusca = militarSelected?.re

        val viaturaSelected = _state.value.todasViaturas.find { it.id == _state.value.filtroViaturaId }
        val vtrBusca = viaturaSelected?.prefixo

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = ocorrenciaRepository.getOcorrencias(
                dataInicio = inicio,
                dataFim = fim,
                re = reBusca,
                vtr = vtrBusca,
                limite = 500, // Pegar até 500 de uma vez
                pagina = _state.value.paginaAtual
            )

            result.onSuccess { response ->
                if (response.sucesso) {
                    val ocorrenciasApi = response.ocorrencias
                    val equipes = equipeRepository.getAllEquipesServico().first()
                    
                    val associadas = associacaoUseCase.associar(
                        ocorrenciasApi, 
                        equipes, 
                        _state.value.todasViaturas, 
                        _state.value.todosMilitares
                    ).sortedWith(
                        compareByDescending<OcorrenciaComMilitares> { 
                            try { LocalDate.parse(it.ocorrencia.data, dateFormatter) } catch (e: Exception) { LocalDate.MIN } 
                        }.thenByDescending { 
                            it.ocorrencia.qtrSaida
                        }
                    )
                    
                    val cidades = associadas.mapNotNull { it.ocorrencia.cidade }.filter { it.isNotBlank() }.distinct().sorted()
                    val naturezas = associadas.mapNotNull { it.ocorrencia.natureza }.filter { it.isNotBlank() }.distinct().sorted()

                    _state.value = _state.value.copy(
                        ocorrenciasTotais = associadas,
                        totalRegistros = response.total,
                        totalPaginas = response.totalPaginas,
                        cidadesDisponiveis = cidades,
                        naturezasDisponiveis = naturezas,
                        isLoading = false
                    )
                    aplicarFiltrosLocais()
                } else {
                    android.util.Log.e("HistoricoDashboard", "API retornou falso em sucesso.")
                    _state.value = _state.value.copy(isLoading = false, error = "A API retornou falha.")
                }
            }.onFailure { e ->
                android.util.Log.e("HistoricoDashboard", "Erro na API", e)
                _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    private fun aplicarFiltrosLocais() {
        val estadoAtual = _state.value
        var filtradas = estadoAtual.ocorrenciasTotais

        // Militar filter (aplicado localmente também caso a API não tenha filtrado perfeitamente todas as posições da equipe)
        estadoAtual.filtroMilitarId?.let { mId ->
            filtradas = filtradas.filter { occ -> occ.militares.any { it.id == mId } }
        }

        // Viatura filter
        estadoAtual.filtroViaturaId?.let { vId ->
            val vtr = estadoAtual.todasViaturas.find { it.id == vId }
            vtr?.prefixo?.let { prefixo ->
                val prefixoNorm = prefixo.replace(Regex("[^a-zA-Z0-9]"), "").uppercase()
                filtradas = filtradas.filter { occ -> 
                    occ.ocorrencia.vtr.replace(Regex("[^a-zA-Z0-9]"), "").uppercase() == prefixoNorm
                }
            }
        }

        // Natureza filter
        estadoAtual.filtroNatureza?.let { nat ->
            if (nat.isNotBlank()) {
                filtradas = filtradas.filter { it.ocorrencia.natureza.contains(nat, ignoreCase = true) }
            }
        }

        // Cidade filter
        estadoAtual.filtroCidade?.let { cid ->
            if (cid.isNotBlank()) {
                filtradas = filtradas.filter { it.ocorrencia.cidade.contains(cid, ignoreCase = true) }
            }
        }

        // Texto Livre filter
        val txt = estadoAtual.filtroTextoLivre.lowercase().trim()
        if (txt.isNotEmpty()) {
            filtradas = filtradas.filter { occMil ->
                val o = occMil.ocorrencia
                val content = "${o.talao} ${o.endereco} ${o.natureza} ${o.cidade}".lowercase()
                content.contains(txt)
            }
        }

        _state.value = estadoAtual.copy(ocorrenciasFiltradas = filtradas)
    }
}
