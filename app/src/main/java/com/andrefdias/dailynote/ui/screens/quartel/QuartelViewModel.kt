package com.andrefdias.dailynote.ui.screens.quartel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.model.Quartel
import com.andrefdias.dailynote.domain.repository.QuartelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuartelViewModel @Inject constructor(
    private val repository: QuartelRepository
) : ViewModel() {

    val quarteis = repository.getAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _uiState = MutableStateFlow(QuartelUiState())
    val uiState: StateFlow<QuartelUiState> = _uiState.asStateFlow()

    fun updateForm(unidade: String, posto: String, municipio: String = "") {
        _uiState.value = _uiState.value.copy(
            unidade = unidade,
            posto = posto,
            municipio = municipio,
            error = null
        )
    }

    fun selectQuartel(quartel: Quartel?) {
        if (quartel != null) {
            _uiState.value = _uiState.value.copy(
                selectedId = quartel.id,
                unidade = quartel.unidade,
                posto = quartel.posto,
                municipio = quartel.municipio,
                isEditing = true,
                error = null
            )
        } else {
            _uiState.value = QuartelUiState()
        }
    }

    fun saveQuartel() {
        val currentState = _uiState.value
        val unidade = currentState.unidade.trim()
        val posto = currentState.posto.trim()
        val municipio = currentState.municipio.trim()

        if (unidade.isBlank() || posto.isBlank()) {
            _uiState.value = currentState.copy(error = "Unidade e Posto são obrigatórios")
            return
        }

        val exists = quarteis.value.any { it.unidade == unidade && it.posto == posto && it.id != currentState.selectedId }
        if (exists) {
            _uiState.value = currentState.copy(error = "Esta combinação de Unidade e Posto já existe")
            return
        }

        viewModelScope.launch {
            if (currentState.isEditing && currentState.selectedId != null) {
                repository.update(Quartel(id = currentState.selectedId, unidade = unidade, posto = posto, municipio = municipio))
            } else {
                repository.insert(Quartel(unidade = unidade, posto = posto, municipio = municipio))
            }
            selectQuartel(null)
        }
    }

    fun deleteQuartel(quartel: Quartel) {
        viewModelScope.launch {
            repository.delete(quartel)
        }
    }
}

data class QuartelUiState(
    val selectedId: String? = null,
    val unidade: String = "",
    val posto: String = "",
    val municipio: String = "",
    val isEditing: Boolean = false,
    val error: String? = null
)
