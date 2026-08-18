package com.andrefdias.dailynote.ui.screens.viatura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.model.Viatura
import com.andrefdias.dailynote.domain.repository.QuartelRepository
import com.andrefdias.dailynote.domain.repository.ViaturaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViaturaViewModel @Inject constructor(
    private val viaturaRepository: ViaturaRepository,
    private val quartelRepository: QuartelRepository
) : ViewModel() {

    val viaturas = viaturaRepository.getAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val unidades = quartelRepository.getUnidades().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _postos = MutableStateFlow<List<String>>(emptyList())
    val postos: StateFlow<List<String>> = _postos.asStateFlow()

    private val _uiState = MutableStateFlow(ViaturaUiState())
    val uiState: StateFlow<ViaturaUiState> = _uiState.asStateFlow()

    fun updateForm(prefixo: String, tipoAtendimento: String, unidade: String, posto: String) {
        val calculatedTipo = prefixo.substringBefore("-").uppercase()
        val formattedPrefixo = prefixo.uppercase().replace(Regex("[^A-Z0-9-]"), "")
        
        val oldUnidade = _uiState.value.unidade

        _uiState.value = _uiState.value.copy(
            prefixo = formattedPrefixo,
            tipo = calculatedTipo,
            tipoAtendimento = tipoAtendimento,
            unidade = unidade,
            posto = posto,
            error = null
        )

        // Load postos when unidade changes
        if (unidade.isNotBlank() && unidade != oldUnidade) {
            viewModelScope.launch {
                quartelRepository.getPostosByUnidade(unidade).collect { list ->
                    _postos.value = list
                    if (!list.contains(posto)) {
                        _uiState.value = _uiState.value.copy(posto = "")
                    }
                }
            }
        }
    }

    fun selectViatura(viatura: Viatura?) {
        if (viatura != null) {
            _uiState.value = _uiState.value.copy(
                selectedId = viatura.id,
                prefixo = viatura.prefixo,
                tipo = viatura.tipo,
                tipoAtendimento = viatura.tipoAtendimento,
                unidade = viatura.unidade,
                posto = viatura.posto,
                isEditing = true,
                error = null
            )
            // Load postos for the selected unidade
            viewModelScope.launch {
                quartelRepository.getPostosByUnidade(viatura.unidade).collect { list ->
                    _postos.value = list
                }
            }
        } else {
            _uiState.value = ViaturaUiState()
            _postos.value = emptyList()
        }
    }

    fun saveViatura() {
        val currentState = _uiState.value
        if (currentState.prefixo.isBlank() || currentState.unidade.isBlank() || currentState.posto.isBlank() || currentState.tipoAtendimento.isBlank()) {
            _uiState.value = currentState.copy(error = "Preencha todos os campos obrigatórios")
            return
        }

        val prefixoRegex = Regex("^[A-Z]{2,4}-\\d{5}$")
        if (!currentState.prefixo.matches(prefixoRegex)) {
            _uiState.value = currentState.copy(error = "Prefixo inválido. Formato esperado: XX-00000, XXX-00000 ou XXXX-00000")
            return
        }

        val exists = viaturas.value.any { it.prefixo == currentState.prefixo && it.id != currentState.selectedId }
        if (exists) {
            _uiState.value = currentState.copy(error = "Uma viatura com este prefixo já existe")
            return
        }

        viewModelScope.launch {
            val viatura = Viatura(
                id = currentState.selectedId ?: java.util.UUID.randomUUID().toString(),
                prefixo = currentState.prefixo,
                tipo = currentState.tipo,
                tipoAtendimento = currentState.tipoAtendimento,
                unidade = currentState.unidade,
                posto = currentState.posto
            )
            
            if (currentState.isEditing) {
                viaturaRepository.update(viatura)
            } else {
                viaturaRepository.insert(viatura)
            }
            selectViatura(null)
        }
    }

    fun deleteViatura(viatura: Viatura) {
        viewModelScope.launch {
            viaturaRepository.delete(viatura)
        }
    }
}

data class ViaturaUiState(
    val selectedId: String? = null,
    val prefixo: String = "",
    val tipo: String = "",
    val tipoAtendimento: String = "",
    val unidade: String = "",
    val posto: String = "",
    val isEditing: Boolean = false,
    val error: String? = null
)
