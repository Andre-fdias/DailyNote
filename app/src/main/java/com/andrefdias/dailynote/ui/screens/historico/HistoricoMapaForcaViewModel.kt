package com.andrefdias.dailynote.ui.screens.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.model.EquipeServico
import com.andrefdias.dailynote.domain.repository.EquipeServicoRepository
import com.andrefdias.dailynote.domain.repository.MilitarRepository
import com.andrefdias.dailynote.domain.repository.ViaturaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoricoMapaForcaState(
    val isLoading: Boolean = true,
    val todasEquipes: List<EquipeServico> = emptyList(),
    val equipesFiltradas: List<EquipeServico> = emptyList(),
    val searchQuery: String = ""
)

@HiltViewModel
class HistoricoMapaForcaViewModel @Inject constructor(
    private val repository: EquipeServicoRepository,
    private val viaturaRepository: ViaturaRepository,
    private val militarRepository: MilitarRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoricoMapaForcaState())
    val state: StateFlow<HistoricoMapaForcaState> = _state.asStateFlow()

    init {
        carregarEquipes()
    }

    fun carregarEquipes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val equipesFlow = repository.getAllEquipesServico()
            val viaturasFlow = viaturaRepository.getAll()
            val militaresFlow = militarRepository.getAll()
            
            combine(equipesFlow, viaturasFlow, militaresFlow) { equipes, viaturas, militares ->
                equipes.map { equipe ->
                    equipe.copy(
                        viaturas = equipe.viaturas.map { ev ->
                            ev.copy(
                                viatura = viaturas.find { it.id == ev.viaturaId },
                                militaresEscalados = ev.militaresEscalados.map { me ->
                                    me.copy(militar = militares.find { it.id == me.militarId })
                                }
                            )
                        }
                    )
                }.sortedByDescending { it.data }
            }.collect { equipes ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    todasEquipes = equipes,
                    equipesFiltradas = equipes
                )
                filtrarEquipes()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        filtrarEquipes()
    }

    private fun filtrarEquipes() {
        val query = _state.value.searchQuery.lowercase()
        val filtradas = _state.value.todasEquipes.filter { equipe ->
            if (query.isEmpty()) return@filter true
            
            val matchData = equipe.data.lowercase().contains(query)
            val matchUnidade = equipe.unidade.lowercase().contains(query)
            val matchPosto = (equipe.posto ?: "").lowercase().contains(query)
            val matchViatura = equipe.viaturas.any { v -> 
                (v.viatura?.prefixo ?: "").lowercase().contains(query) || 
                v.militaresEscalados.any { m -> 
                    (m.militar?.nomeGuerra ?: "").lowercase().contains(query) || (m.militar?.re ?: "").lowercase().contains(query)
                }
            }
            
            matchData || matchUnidade || matchPosto || matchViatura
        }
        _state.value = _state.value.copy(equipesFiltradas = filtradas)
    }

    fun exportToPdf(context: android.content.Context) {
        com.andrefdias.dailynote.util.ExportUtils.exportEquipesToPdfAndShare(context, _state.value.equipesFiltradas)
    }

    fun exportToExcel(context: android.content.Context) {
        com.andrefdias.dailynote.util.ExportUtils.exportEquipesToExcelAndShare(context, _state.value.equipesFiltradas)
    }

    fun shareAsJson(context: android.content.Context) {
        com.andrefdias.dailynote.util.ExportUtils.exportEquipesToJsonAndShare(context, _state.value.equipesFiltradas)
    }
}
