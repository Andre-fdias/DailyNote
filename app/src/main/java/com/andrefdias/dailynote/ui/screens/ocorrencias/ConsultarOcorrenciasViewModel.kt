package com.andrefdias.dailynote.ui.screens.ocorrencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import com.andrefdias.dailynote.domain.model.Viatura
import com.andrefdias.dailynote.domain.repository.OcorrenciaRepository
import com.andrefdias.dailynote.domain.repository.ViaturaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConsultarOcorrenciasState(
    val isLoading: Boolean = false,
    val ocorrencias: List<RoomNovaOcorrencia> = emptyList(),
    val viaturaMap: Map<String, String> = emptyMap(), // id -> prefixo
    val veiculosMap: Map<String, List<com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo>> = emptyMap(), // ocorrenciaId -> list of vehicles
    val errorMessage: String? = null
)

@HiltViewModel
class ConsultarOcorrenciasViewModel @Inject constructor(
    private val ocorrenciaRepository: OcorrenciaRepository,
    private val viaturaRepository: ViaturaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ConsultarOcorrenciasState())
    val state: StateFlow<ConsultarOcorrenciasState> = _state.asStateFlow()

    init {
        observarDados()
    }

    private fun observarDados() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                combine(
                    ocorrenciaRepository.getAllLocalOcorrenciasFlow(),
                    viaturaRepository.getAll(),
                    ocorrenciaRepository.getAllVeiculosFlow()
                ) { ocorrencias, viaturas, veiculos ->
                    // Build a map: id -> prefixo AND prefixo -> prefixo (for records that already stored prefixo)
                    val map = mutableMapOf<String, String>()
                    viaturas.forEach { v ->
                        v.id?.let { id -> map[id] = v.prefixo }
                        map[v.prefixo] = v.prefixo
                    }
                    val veiculosMap = veiculos.groupBy { it.ocorrenciaId }
                    Triple(ocorrencias.sortedByDescending { it.createdAt }, map, veiculosMap)
                }.collect { (ocorrencias, map, veiculosMap) ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        ocorrencias = ocorrencias,
                        viaturaMap = map,
                        veiculosMap = veiculosMap,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Erro ao carregar ocorrências"
                )
            }
        }
    }

    fun excluirOcorrencia(ocorrencia: RoomNovaOcorrencia) {
        // TODO: implement delete
    }
}
