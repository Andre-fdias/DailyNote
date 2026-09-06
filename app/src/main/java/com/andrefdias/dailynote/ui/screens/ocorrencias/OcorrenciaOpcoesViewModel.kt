package com.andrefdias.dailynote.ui.screens.ocorrencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import com.andrefdias.dailynote.domain.repository.OcorrenciaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.andrefdias.dailynote.domain.repository.MilitarRepository
import com.andrefdias.dailynote.domain.repository.ViaturaRepository
import com.andrefdias.dailynote.domain.model.Militar
import com.andrefdias.dailynote.domain.model.Viatura

@HiltViewModel
class OcorrenciaOpcoesViewModel @Inject constructor(
    private val ocorrenciaRepository: OcorrenciaRepository,
    private val militarRepository: MilitarRepository,
    private val viaturaRepository: ViaturaRepository
) : ViewModel() {

    private val _ocorrencia = MutableStateFlow<RoomNovaOcorrencia?>(null)
    val ocorrencia: StateFlow<RoomNovaOcorrencia?> = _ocorrencia.asStateFlow()

    private val _viaturasDisponiveis = MutableStateFlow<List<Viatura>>(emptyList())
    val viaturasDisponiveis: StateFlow<List<Viatura>> = _viaturasDisponiveis.asStateFlow()

    private val _militaresDisponiveis = MutableStateFlow<List<Militar>>(emptyList())
    val militaresDisponiveis: StateFlow<List<Militar>> = _militaresDisponiveis.asStateFlow()

    init {
        viewModelScope.launch {
            viaturaRepository.getAll().collect { _viaturasDisponiveis.value = it }
        }
        viewModelScope.launch {
            militarRepository.getAll().collect { _militaresDisponiveis.value = it }
        }
    }

    fun loadOcorrencia(id: String) {
        viewModelScope.launch {
            ocorrenciaRepository.getAllLocalOcorrenciasFlow().collect { ocorrencias ->
                val oco = ocorrencias.find { it.id == id }
                _ocorrencia.value = oco
            }
        }
    }

    fun concluirOcorrencia() {
        val current = _ocorrencia.value ?: return
        viewModelScope.launch {
            ocorrenciaRepository.updateOcorrenciaLocal(current.copy(isConcluida = true))
        }
    }

    fun updateOcorrenciaBase(talao: String, natureza: String, rua: String, numero: String, bairro: String, cidade: String) {
        val current = _ocorrencia.value ?: return
        val updated = current.copy(
            talao = talao,
            natureza = natureza,
            rua = rua,
            numero = numero,
            bairro = bairro,
            cidade = cidade
        )
        _ocorrencia.value = updated
        viewModelScope.launch {
            ocorrenciaRepository.updateOcorrenciaLocal(updated)
            // Local state will update because of the collect
        }
    }

    fun updateViaturas(viaturasStr: String) {
        val current = _ocorrencia.value ?: return
        
        // Cascata: se uma viatura for apagada, apagar militares associados
        var newGuarnicaoJson = current.guarnicaoJson
        try {
            val typeStr = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            val typeMap = object : com.google.gson.reflect.TypeToken<List<Map<String, String>>>() {}.type
            
            val militaresAtuais = com.google.gson.Gson().fromJson<List<String>>(current.guarnicaoJson, typeStr) ?: emptyList()
            
            val newViaturas = if (viaturasStr.trim().startsWith("[")) {
                com.google.gson.Gson().fromJson<List<Map<String, String>>>(viaturasStr, typeMap)
                    ?.mapNotNull { it["prefixo"] } ?: emptyList()
            } else {
                listOf(viaturasStr) // fallback
            }

            val militaresFiltrados = militaresAtuais.filter { militar ->
                val viaturaDoMilitar = if (militar.contains(" | ")) militar.substringBefore(" | ").trim() else "Sem Viatura"
                newViaturas.contains(viaturaDoMilitar) || viaturaDoMilitar == "Sem Viatura"
            }
            newGuarnicaoJson = com.google.gson.Gson().toJson(militaresFiltrados)

        } catch (e: Exception) {
            android.util.Log.e("OcorrenciaOpcoesVM", "Erro ao filtrar militares: ${e.message}")
        }

        val updated = current.copy(viatura = viaturasStr, guarnicaoJson = newGuarnicaoJson)
        _ocorrencia.value = updated
        viewModelScope.launch {
            ocorrenciaRepository.updateOcorrenciaLocal(updated)
        }
    }

    fun updateMilitares(guarnicaoJson: String) {
        val current = _ocorrencia.value ?: return
        val updated = current.copy(guarnicaoJson = guarnicaoJson)
        _ocorrencia.value = updated
        viewModelScope.launch {
            ocorrenciaRepository.updateOcorrenciaLocal(updated)
        }
    }

    fun updatePessoas(pessoasJson: String) {
        val current = _ocorrencia.value ?: return
        val updated = current.copy(pessoasJson = pessoasJson)
        _ocorrencia.value = updated
        viewModelScope.launch {
            ocorrenciaRepository.updateOcorrenciaLocal(updated)
        }
    }

    fun updateApoios(apoiosJson: String) {
        val current = _ocorrencia.value ?: return
        val updated = current.copy(apoios = apoiosJson)
        _ocorrencia.value = updated
        viewModelScope.launch {
            ocorrenciaRepository.updateOcorrenciaLocal(updated)
        }
    }

    fun updateHistorico(texto: String) {
        val current = _ocorrencia.value ?: return
        val updated = current.copy(historico = texto)
        _ocorrencia.value = updated
        viewModelScope.launch {
            ocorrenciaRepository.updateOcorrenciaLocal(updated)
        }
    }

    fun updateFotos(fotosJson: String) {
        val current = _ocorrencia.value ?: return
        val updated = current.copy(fotosUrisJson = fotosJson)
        _ocorrencia.value = updated
        viewModelScope.launch {
            ocorrenciaRepository.updateOcorrenciaLocal(updated)
        }
    }

    // ==================================
    // Veículos Envolvidos
    // ==================================
    private val _veiculos = kotlinx.coroutines.flow.MutableStateFlow<List<com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo>>(emptyList())
    val veiculos: kotlinx.coroutines.flow.StateFlow<List<com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo>> = _veiculos

    fun loadVeiculos(ocorrenciaId: String) {
        viewModelScope.launch {
            ocorrenciaRepository.getVeiculosByOcorrenciaIdFlow(ocorrenciaId).collect {
                _veiculos.value = it
            }
        }
    }

    fun addVeiculo(veiculo: com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo) {
        viewModelScope.launch {
            ocorrenciaRepository.insertVeiculoEnvolvido(veiculo)
        }
    }

    fun updateVeiculo(veiculo: com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo) {
        viewModelScope.launch {
            ocorrenciaRepository.updateVeiculoEnvolvido(veiculo)
        }
    }

    fun deleteVeiculo(veiculo: com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo) {
        viewModelScope.launch {
            ocorrenciaRepository.deleteVeiculoEnvolvido(veiculo)
        }
    }

    // ==================================
    // Vítimas
    // ==================================
    private val _vitimas = kotlinx.coroutines.flow.MutableStateFlow<List<com.andrefdias.dailynote.data.local.entities.RoomNovaVitima>>(emptyList())
    val vitimas: kotlinx.coroutines.flow.StateFlow<List<com.andrefdias.dailynote.data.local.entities.RoomNovaVitima>> = _vitimas

    fun loadVitimas(ocorrenciaId: String) {
        viewModelScope.launch {
            ocorrenciaRepository.getVitimasByOcorrenciaIdFlow(ocorrenciaId).collect {
                _vitimas.value = it
            }
        }
    }

    fun addVitima(vitima: com.andrefdias.dailynote.data.local.entities.RoomNovaVitima) {
        viewModelScope.launch {
            ocorrenciaRepository.insertVitima(vitima)
        }
    }

    fun updateVitima(vitima: com.andrefdias.dailynote.data.local.entities.RoomNovaVitima) {
        viewModelScope.launch {
            ocorrenciaRepository.updateVitima(vitima)
        }
    }

    fun deleteVitima(vitima: com.andrefdias.dailynote.data.local.entities.RoomNovaVitima) {
        viewModelScope.launch {
            ocorrenciaRepository.deleteVitima(vitima)
        }
    }
}

