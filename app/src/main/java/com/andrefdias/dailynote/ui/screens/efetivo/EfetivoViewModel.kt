package com.andrefdias.dailynote.ui.screens.efetivo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.model.EfetivoMilitar
import com.andrefdias.dailynote.domain.model.FolgaMensal
import com.andrefdias.dailynote.domain.repository.EfetivoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EfetivoUiState(
    val isLoading: Boolean = false,
    val efetivoList: List<EfetivoMilitar> = emptyList(),
    val folgasList: List<FolgaMensal> = emptyList(),
    val afastamentosList: List<com.andrefdias.dailynote.domain.model.AfastamentoMilitar> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class EfetivoViewModel @Inject constructor(
    private val repository: EfetivoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EfetivoUiState())
    val uiState: StateFlow<EfetivoUiState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                com.andrefdias.dailynote.util.LogHelper.d("EfetivoViewModel", "Iniciando busca de efetivo, folgas e afastamentos.")
                
                kotlinx.coroutines.flow.combine(
                    repository.getEfetivo(),
                    repository.getFolgas(),
                    repository.getAfastamentos()
                ) { efetivo, folgas, afastamentos ->
                    Triple(efetivo, folgas, afastamentos)
                }
                .catch { e ->
                    com.andrefdias.dailynote.util.LogHelper.e("EfetivoViewModel", "Erro ao combinar fluxos", e)
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Erro desconhecido") }
                }
                .collect { (efetivo, folgas, afastamentos) ->
                    com.andrefdias.dailynote.util.LogHelper.i("EfetivoViewModel", "Busca concluída: ${efetivo.size} militares, ${folgas.size} folgas, ${afastamentos.size} afastamentos.")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            efetivoList = efetivo,
                            folgasList = folgas,
                            afastamentosList = afastamentos,
                            errorMessage = null
                        ) 
                    }
                }
            } catch (e: Exception) {
                com.andrefdias.dailynote.util.LogHelper.e("EfetivoViewModel", "Exceção inesperada na busca de dados", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
    }
}
