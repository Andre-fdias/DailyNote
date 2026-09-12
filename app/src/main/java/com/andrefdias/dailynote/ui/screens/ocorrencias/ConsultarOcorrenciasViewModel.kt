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
    val vitimasMap: Map<String, List<com.andrefdias.dailynote.data.local.entities.RoomNovaVitima>> = emptyMap(), // ocorrenciaId -> list of vitimas
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
                kotlinx.coroutines.flow.combine(
                    ocorrenciaRepository.getAllLocalOcorrenciasFlow(),
                    viaturaRepository.getAll(),
                    ocorrenciaRepository.getAllVeiculosFlow(),
                    ocorrenciaRepository.getAllVitimasFlow()
                ) { ocorrencias, viaturas, veiculos, vitimas ->
                    // Build a map: id -> prefixo AND prefixo -> prefixo (for records that already stored prefixo)
                    val map = mutableMapOf<String, String>()
                    viaturas.forEach { v ->
                        v.id?.let { id -> map[id] = v.prefixo }
                        map[v.prefixo] = v.prefixo
                    }
                    val veiculosMap = veiculos.groupBy { it.ocorrenciaId }
                    val vitimasMap = vitimas.groupBy { it.ocorrenciaId }
                    // returning a list or custom object because Combine with 4 flows needs a custom transform if we want a tuple, wait, combine function in Flow takes up to 5 flows.
                    // Actually, combine 4 flows produces a Tuple4 or we can just return a custom class.
                    // I will return a 4-element data structure or use an array. Wait, in kotlin `combine` with 4 arguments has a lambda with 4 parameters.
                    // The lambda returns whatever we want. Let's return a list or data class. Let's just return a list and cast, or better, return a data class.
                    ConsultarOcorrenciasState(
                        isLoading = false,
                        ocorrencias = ocorrencias.sortedByDescending { it.createdAt },
                        viaturaMap = map,
                        veiculosMap = veiculosMap,
                        vitimasMap = vitimasMap,
                        errorMessage = null
                    )
                }.collect { newState ->
                    _state.value = newState
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
