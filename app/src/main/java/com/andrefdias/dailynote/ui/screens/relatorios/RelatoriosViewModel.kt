package com.andrefdias.dailynote.ui.screens.relatorios

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import com.andrefdias.dailynote.domain.repository.OcorrenciaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class RelatoriosState(
    val dataInicial: String = "",
    val dataFinal: String = "",
    val talaoBusca: String = "",
    val incluirImagens: Boolean = true,
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class RelatoriosViewModel @Inject constructor(
    private val ocorrenciaRepository: OcorrenciaRepository,
    private val equipeServicoRepository: com.andrefdias.dailynote.domain.repository.EquipeServicoRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RelatoriosState())
    val state: StateFlow<RelatoriosState> = _state.asStateFlow()

    fun onDataInicialChanged(data: String) { _state.value = _state.value.copy(dataInicial = data) }
    fun onDataFinalChanged(data: String) { _state.value = _state.value.copy(dataFinal = data) }
    fun onTalaoBuscaChanged(talao: String) { _state.value = _state.value.copy(talaoBusca = talao) }
    fun onIncluirImagensChanged(incluir: Boolean) { _state.value = _state.value.copy(incluirImagens = incluir) }
    fun clearMessage() { _state.value = _state.value.copy(message = null) }

    fun gerarRelatorioPorTalao(context: Context) {
        val talao = state.value.talaoBusca.trim()
        if (talao.isEmpty()) {
            _state.value = _state.value.copy(message = "Digite o número do talão para buscar.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            // Busca simplificada varrendo o fluxo
            ocorrenciaRepository.getAllLocalOcorrenciasFlow().collect { list ->
                val ocorrencia = list.find { it.talao == talao }
                if (ocorrencia != null) {
                    com.andrefdias.dailynote.util.ExportUtils.exportOcorrenciaToPdf(context, ocorrencia, state.value.incluirImagens)
                    _state.value = _state.value.copy(isLoading = false, message = "Relatório gerado com sucesso!")
                } else {
                    _state.value = _state.value.copy(isLoading = false, message = "Ocorrência não encontrada com o talão: $talao")
                }
            }
        }
    }

    fun gerarRelatorioGeral(context: Context) {
        val dataIni = state.value.dataInicial
        val dataFim = state.value.dataFinal
        
        if (dataIni.isEmpty() || dataFim.isEmpty()) {
            _state.value = _state.value.copy(message = "Selecione o período (Data Inicial e Final).")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            ocorrenciaRepository.getAllLocalOcorrenciasFlow().collect { list ->
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val start = try { LocalDate.parse(dataIni, formatter) } catch (e: Exception) { null }
                val end = try { LocalDate.parse(dataFim, formatter) } catch (e: Exception) { null }

                if (start == null || end == null) {
                    _state.value = _state.value.copy(isLoading = false, message = "Formato de data inválido.")
                    return@collect
                }

                val filtered = list.filter { occ ->
                    try {
                        val d = try { LocalDate.parse(occ.data, formatter) } catch(e: Exception) { LocalDate.parse(occ.data) }
                        (d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end))
                    } catch (e: Exception) {
                        false
                    }
                }

                if (filtered.isEmpty()) {
                    _state.value = _state.value.copy(isLoading = false, message = "Nenhuma ocorrência encontrada neste período.")
                } else {
                    com.andrefdias.dailynote.util.ExportUtils.exportGeralOcorrenciasToPdf(context, filtered)
                    _state.value = _state.value.copy(isLoading = false, message = "Relatório geral gerado! (${filtered.size} registros)")
                }
            }
        }
    }

    fun gerarRelatorioHistorico(context: Context) {
        val dataIni = state.value.dataInicial
        val dataFim = state.value.dataFinal
        
        if (dataIni.isEmpty() || dataFim.isEmpty()) {
            _state.value = _state.value.copy(message = "Selecione o período (Data Inicial e Final).")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            ocorrenciaRepository.getAllLegacyOcorrenciasFlow().collect { list ->
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val start = try { LocalDate.parse(dataIni, formatter) } catch (e: Exception) { null }
                val end = try { LocalDate.parse(dataFim, formatter) } catch (e: Exception) { null }

                if (start == null || end == null) {
                    _state.value = _state.value.copy(isLoading = false, message = "Formato de data inválido.")
                    return@collect
                }

                val filtered = list.filter { occ ->
                    try {
                        val dtStr = occ.dataHora.take(10) // "2026-08-30" from "2026-08-30T10:00:00"
                        val d = try { LocalDate.parse(dtStr) } catch(e: Exception) { LocalDate.parse(occ.dataHora, formatter) }
                        (d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end))
                    } catch (e: Exception) {
                        false
                    }
                }

                if (filtered.isEmpty()) {
                    _state.value = _state.value.copy(isLoading = false, message = "Nenhum histórico encontrado neste período.")
                } else {
                    com.andrefdias.dailynote.util.ExportUtils.exportHistoricoGeralToPdf(context, filtered)
                    _state.value = _state.value.copy(isLoading = false, message = "Relatório histórico gerado! (${filtered.size} registros)")
                }
            }
        }
    }

    fun gerarRelatorioMapaForca(context: Context) {
        val dataIni = state.value.dataInicial
        val dataFim = state.value.dataFinal
        
        if (dataIni.isEmpty() || dataFim.isEmpty()) {
            _state.value = _state.value.copy(message = "Selecione o período (Data Inicial e Final).")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            equipeServicoRepository.getAllEquipesServico().collect { list ->
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val start = try { LocalDate.parse(dataIni, formatter) } catch (e: Exception) { null }
                val end = try { LocalDate.parse(dataFim, formatter) } catch (e: Exception) { null }

                if (start == null || end == null) {
                    _state.value = _state.value.copy(isLoading = false, message = "Formato de data inválido.")
                    return@collect
                }

                val filtered = list.filter { mapa ->
                    try {
                        val d = try { LocalDate.parse(mapa.data, formatter) } catch(e: Exception) { LocalDate.parse(mapa.data) }
                        (d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end))
                    } catch (e: Exception) {
                        false
                    }
                }

                if (filtered.isEmpty()) {
                    _state.value = _state.value.copy(isLoading = false, message = "Nenhum Mapa Força encontrado neste período.")
                } else {
                    com.andrefdias.dailynote.util.ExportUtils.exportMapaForcaGeralToPdf(context, filtered)
                    _state.value = _state.value.copy(isLoading = false, message = "Relatório Mapa Força gerado! (${filtered.size} registros)")
                }
            }
        }
    }
}
