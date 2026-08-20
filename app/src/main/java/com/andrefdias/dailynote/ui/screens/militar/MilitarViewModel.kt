package com.andrefdias.dailynote.ui.screens.militar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.model.Militar
import com.andrefdias.dailynote.domain.repository.MilitarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MilitarViewModel @Inject constructor(
    private val militarRepository: MilitarRepository
) : ViewModel() {

    val militares = militarRepository.getAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _uiState = MutableStateFlow(MilitarUiState())
    val uiState: StateFlow<MilitarUiState> = _uiState.asStateFlow()

    fun updateForm(
        re: String,
        digito: String,
        nomeCompleto: String,
        nomeGuerra: String,
        graduacao: String,
        situacao: String,
        mergulhador: Boolean = false,
        ovb: String = "Não Habilitado"
    ) {
        val cleanRe = re.replace(Regex("[^0-9]"), "").take(6) // limit to 6 digits
        val cleanDigito = digito.take(1).uppercase() // 1 char limit, uppercase
        _uiState.value = _uiState.value.copy(
            re = cleanRe,
            digito = cleanDigito,
            nomeCompleto = nomeCompleto,
            nomeGuerra = nomeGuerra,
            graduacao = graduacao,
            situacao = situacao,
            mergulhador = mergulhador,
            ovb = ovb,
            error = null
        )
    }

    fun selectMilitar(militar: Militar?) {
        if (militar != null) {
            val parts = militar.re.split("-")
            val extractedRe = parts.getOrNull(0) ?: militar.re
            val extractedDigito = parts.getOrNull(1) ?: ""

            _uiState.value = MilitarUiState(
                selectedId = militar.id,
                re = extractedRe,
                digito = extractedDigito,
                nomeCompleto = militar.nomeCompleto,
                nomeGuerra = militar.nomeGuerra,
                graduacao = militar.graduacao,
                situacao = militar.situacao,
                mergulhador = militar.mergulhador,
                ovb = militar.ovb,
                isEditing = true
            )
        } else {
            _uiState.value = MilitarUiState()
        }
    }

    fun saveMilitar() {
        val currentState = _uiState.value
        
        if (currentState.re.isBlank() || currentState.nomeCompleto.isBlank() || 
            currentState.nomeGuerra.isBlank() || currentState.graduacao.isBlank() || 
            currentState.situacao.isBlank()) {
            _uiState.value = currentState.copy(error = "Preencha todos os campos obrigatórios")
            return
        }

        if (currentState.re.length > 6) {
            _uiState.value = currentState.copy(error = "O RE deve conter até 6 dígitos")
            return
        }

        val formattedRe = if (currentState.digito.isNotBlank()) "${currentState.re}-${currentState.digito}" else currentState.re

        val exists = militares.value.any { it.re == formattedRe && it.id != currentState.selectedId }
        if (exists) {
            _uiState.value = currentState.copy(error = "Um militar com este RE já está cadastrado")
            return
        }

        viewModelScope.launch {
            val militar = Militar(
                id = currentState.selectedId ?: java.util.UUID.randomUUID().toString(),
                re = formattedRe,
                nomeCompleto = currentState.nomeCompleto,
                nomeGuerra = currentState.nomeGuerra,
                graduacao = currentState.graduacao,
                situacao = currentState.situacao,
                mergulhador = currentState.mergulhador,
                ovb = currentState.ovb
            )
            
            if (currentState.isEditing) {
                militarRepository.update(militar)
            } else {
                militarRepository.insert(militar)
            }
            selectMilitar(null)
        }
    }

    fun deleteMilitar(militar: Militar) {
        viewModelScope.launch {
            militarRepository.delete(militar)
        }
    }
}

data class MilitarUiState(
    val selectedId: String? = null,
    val re: String = "",
    val digito: String = "",
    val nomeCompleto: String = "",
    val nomeGuerra: String = "",
    val graduacao: String = "",
    val situacao: String = "",
    val mergulhador: Boolean = false,
    val ovb: String = "Não Habilitado",
    val isEditing: Boolean = false,
    val error: String? = null
)
