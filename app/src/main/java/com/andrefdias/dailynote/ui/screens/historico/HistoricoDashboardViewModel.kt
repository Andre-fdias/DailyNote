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

import com.andrefdias.dailynote.domain.model.MapOccurrence
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import com.andrefdias.dailynote.domain.usecase.GetUnifiedMapOccurrencesUseCase

data class HistoricoDashboardState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val ocorrenciasTotais: List<OcorrenciaComMilitares> = emptyList(),
    val ocorrenciasFiltradas: List<OcorrenciaComMilitares> = emptyList(),
    val mapOccurrencesTotais: List<MapOccurrence> = emptyList(),
    val mapOccurrencesFiltradas: List<MapOccurrence> = emptyList(),
    val todosMilitares: List<Militar> = emptyList(),
    val todasViaturas: List<Viatura> = emptyList(),
    
    // API State
    val totalRegistros: Int = 0,
    val paginaAtual: Int = 1,
    val totalPaginas: Int = 1,

    // Listas para Filtros Dinâmicos
    val cidadesDisponiveis: List<String> = emptyList(),
    val naturezasDisponiveis: List<String> = emptyList(),
    val resultadosDisponiveis: List<String> = emptyList(),
    val prontidoesDisponiveis: List<String> = emptyList(),

    // Filtros
    val filtroDataInicio: LocalDate? = null,
    val filtroDataFim: LocalDate? = null,
    val filtroMilitarId: String? = null,
    val filtroViaturaId: String? = null,
    val filtroNatureza: String? = null,
    val filtroCidade: String? = null,
    val filtroHoraInicio: String? = null,
    val filtroHoraFim: String? = null,
    val filtroProntidao: String? = null,
    val filtroResultado: String? = null,
    val filtroTextoLivre: String = ""
)

@HiltViewModel
class HistoricoDashboardViewModel @Inject constructor(
    private val ocorrenciaRepository: OcorrenciaRepository,
    private val equipeRepository: EquipeServicoRepository,
    private val militarRepository: MilitarRepository,
    private val viaturaRepository: ViaturaRepository,
    private val associacaoUseCase: OcorrenciaAssociacaoUseCase,
    private val calendarRepository: CalendarRepository,
    private val unifiedMapUseCase: GetUnifiedMapOccurrencesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistoricoDashboardState())
    val state: StateFlow<HistoricoDashboardState> = _state.asStateFlow()

    // Para envio à API, usamos o padrão garantido
    private val dateFormatterOut = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Multi-parser para leitura de dados da API e organização local
    private val dateParsers = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yy"),
        DateTimeFormatter.ofPattern("d/M/yy"),
        DateTimeFormatter.ofPattern("dd-MM-yy"),
        DateTimeFormatter.ofPattern("d-M-yy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    )

    fun parseDate(dateStr: String): LocalDate {
        val safeDateStr = dateStr.trim().split(" ").firstOrNull() ?: ""
        for (parser in dateParsers) {
            try { return LocalDate.parse(safeDateStr, parser) } catch (e: Exception) {}
        }
        return LocalDate.MIN
    }

    init {
        carregarDadosBase()
        val hoje = LocalDate.now()
        atualizarFiltroData(hoje.minusMonths(1), hoje)
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
    
    fun setFiltroHorario(inicio: String?, fim: String?) {
        _state.value = _state.value.copy(filtroHoraInicio = inicio, filtroHoraFim = fim)
        aplicarFiltrosLocais()
    }
    
    fun setFiltroTextoLivre(texto: String) {
        _state.value = _state.value.copy(filtroTextoLivre = texto)
        aplicarFiltrosLocais()
    }
    
    fun setFiltroProntidao(prontidao: String?) {
        _state.value = _state.value.copy(filtroProntidao = prontidao)
        aplicarFiltrosLocais()
    }
    
    fun setFiltroResultado(resultado: String?) {
        _state.value = _state.value.copy(filtroResultado = resultado)
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
            filtroHoraInicio = null,
            filtroHoraFim = null,
            filtroProntidao = null,
            filtroResultado = null,
            filtroTextoLivre = "",
            paginaAtual = 1
        )
        aplicarFiltrosLocais()
    }

    fun exportToPdf(context: android.content.Context) {
        com.andrefdias.dailynote.util.ExportUtils.exportToPdfAndShare(context, _state.value.ocorrenciasFiltradas)
    }

    fun exportToExcel(context: android.content.Context) {
        com.andrefdias.dailynote.util.ExportUtils.exportToExcelAndShare(context, _state.value.ocorrenciasFiltradas)
    }

    fun shareAsJson(context: android.content.Context) {
        com.andrefdias.dailynote.util.ExportUtils.exportToJsonAndShare(context, _state.value.ocorrenciasFiltradas)
    }

    fun importFromJson(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader()?.use { it.readText() }
                if (jsonString != null) {
                    val type = object : com.google.gson.reflect.TypeToken<List<OcorrenciaComMilitares>>() {}.type
                    val importedData: List<OcorrenciaComMilitares> = com.google.gson.Gson().fromJson(jsonString, type)
                    
                    val tarefas = calendarRepository.getAllTarefas()
                    val eventos = calendarRepository.getAllEventos()

                    val unifiedMapList = unifiedMapUseCase.execute(
                        ocorrenciasEtl = importedData,
                        tarefasApp = tarefas,
                        eventosApp = eventos
                    )
                    
                    val cidades = importedData.mapNotNull { it.ocorrencia.cidade }.filter { it.isNotBlank() }.distinct().sorted()
                    val naturezas = importedData.mapNotNull { it.ocorrencia.natureza }.filter { it.isNotBlank() }.distinct().sorted()
                    val resultados = importedData.mapNotNull { it.ocorrencia.resultado }.filter { it.isNotBlank() }.distinct().sorted()
                    val prontidoes = importedData.mapNotNull { it.ocorrencia.prontidao }.filter { it.isNotBlank() }.distinct().sorted()

                    _state.value = _state.value.copy(
                        ocorrenciasTotais = importedData,
                        mapOccurrencesTotais = unifiedMapList,
                        cidadesDisponiveis = cidades,
                        naturezasDisponiveis = naturezas,
                        resultadosDisponiveis = resultados,
                        prontidoesDisponiveis = prontidoes,
                        totalRegistros = importedData.size
                    )
                    aplicarFiltrosLocais()
                    android.widget.Toast.makeText(context, "Dados importados com sucesso", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Erro ao importar JSON", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun buscarOcorrencias() {
        val inicio = _state.value.filtroDataInicio?.format(dateFormatterOut)
        val fim = _state.value.filtroDataFim?.format(dateFormatterOut)

        val militarSelected = _state.value.todosMilitares.find { it.id == _state.value.filtroMilitarId }
        val reBusca = militarSelected?.re

        val viaturaSelected = _state.value.todasViaturas.find { it.id == _state.value.filtroViaturaId }
        val vtrBusca = viaturaSelected?.prefixo

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val todasOcorrenciasRaw = mutableListOf<com.andrefdias.dailynote.domain.model.Ocorrencia>()
                var currentPagina = 1
                var totalPags = 1
                var records = 0
                
                do {
                    val result = ocorrenciaRepository.getOcorrencias(
                        dataInicio = inicio,
                        dataFim = fim,
                        re = reBusca,
                        vtr = vtrBusca,
                        limite = 500,
                        pagina = currentPagina
                    )
                    val response = result.getOrThrow()
                    if (response.sucesso) {
                        if (response.ocorrencias.isNotEmpty()) {
                            todasOcorrenciasRaw.addAll(response.ocorrencias)
                            records += response.ocorrencias.size
                            currentPagina++
                        }
                        totalPags = maxOf(response.totalPaginas, if (response.ocorrencias.size == 500) currentPagina else 0)
                    } else {
                        break
                    }
                } while (currentPagina <= totalPags)

                val equipes = equipeRepository.getAllEquipesServico().first()
                val associadas = associacaoUseCase.associar(
                    todasOcorrenciasRaw, 
                    equipes, 
                    _state.value.todasViaturas, 
                    _state.value.todosMilitares
                ).sortedWith(
                    compareByDescending<OcorrenciaComMilitares> { 
                        parseDate(it.ocorrencia.data) 
                    }.thenByDescending { 
                        it.ocorrencia.qtrSaida
                    }
                )
                
                val cidades = associadas.mapNotNull { it.ocorrencia.cidade }.filter { it.isNotBlank() }.distinct().sorted()
                val naturezas = associadas.mapNotNull { it.ocorrencia.natureza }.filter { it.isNotBlank() }.distinct().sorted()
                val resultados = associadas.mapNotNull { it.ocorrencia.resultado }.filter { it.isNotBlank() }.distinct().sorted()
                val prontidoes = associadas.mapNotNull { it.ocorrencia.prontidao }.filter { it.isNotBlank() }.distinct().sorted()

                val tarefas = calendarRepository.getAllTarefas()
                val eventos = calendarRepository.getAllEventos()

                val unifiedMapList = unifiedMapUseCase.execute(
                    ocorrenciasEtl = associadas,
                    tarefasApp = tarefas,
                    eventosApp = eventos
                )

                _state.value = _state.value.copy(
                    ocorrenciasTotais = associadas,
                    mapOccurrencesTotais = unifiedMapList,
                    totalRegistros = records,
                    totalPaginas = totalPags,
                    cidadesDisponiveis = cidades,
                    naturezasDisponiveis = naturezas,
                    resultadosDisponiveis = resultados,
                    prontidoesDisponiveis = prontidoes,
                    isLoading = false
                )
                aplicarFiltrosLocais()

            } catch (e: Exception) {
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
                val content = "${o.talao} ${o.endereco} ${o.natureza} ${o.cidade} ${o.cmtVtr}".lowercase()
                content.contains(txt)
            }
        }
        
        // Prontidao Filter
        estadoAtual.filtroProntidao?.let { pr ->
            if (pr.isNotBlank()) {
                filtradas = filtradas.filter { it.ocorrencia.prontidao.equals(pr, ignoreCase = true) }
            }
        }

        // Resultado Filter
        estadoAtual.filtroResultado?.let { res ->
            if (res.isNotBlank()) {
                filtradas = filtradas.filter { it.ocorrencia.resultado.equals(res, ignoreCase = true) }
            }
        }

        // Horário Filter
        val horaInicio = estadoAtual.filtroHoraInicio
        val horaFim = estadoAtual.filtroHoraFim
        if (!horaInicio.isNullOrBlank() && !horaFim.isNullOrBlank()) {
            filtradas = filtradas.filter { occ ->
                val qtr = occ.ocorrencia.qtrSaida
                if (qtr.isNotBlank() && qtr.contains(":")) {
                    qtr >= horaInicio && qtr <= horaFim
                } else true // Se não tem hora, mantemos ou não? Mantemos.
            }
        }

        // --- Filtros no MapOccurrence (Mesma Lógica) ---
        var mapFiltradas = estadoAtual.mapOccurrencesTotais
        estadoAtual.filtroMilitarId?.let { mId ->
            val mil = estadoAtual.todosMilitares.find { it.id == mId }
            if (mil != null) {
                mapFiltradas = mapFiltradas.filter { it.militaryPersonnel.contains(mil.nomeGuerra) }
            }
        }
        estadoAtual.filtroViaturaId?.let { vId ->
            val vtr = estadoAtual.todasViaturas.find { it.id == vId }
            if (vtr != null) {
                val prefixoNorm = vtr.prefixo.replace(Regex("[^a-zA-Z0-9]"), "").uppercase()
                mapFiltradas = mapFiltradas.filter { occ ->
                    occ.vtr?.replace(Regex("[^a-zA-Z0-9]"), "")?.uppercase() == prefixoNorm
                }
            }
        }
        estadoAtual.filtroNatureza?.let { nat ->
            if (nat.isNotBlank()) {
                mapFiltradas = mapFiltradas.filter { it.nature?.contains(nat, ignoreCase = true) == true }
            }
        }
        estadoAtual.filtroCidade?.let { cid ->
            if (cid.isNotBlank()) {
                mapFiltradas = mapFiltradas.filter { it.city?.contains(cid, ignoreCase = true) == true }
            }
        }
        if (txt.isNotEmpty()) {
            mapFiltradas = mapFiltradas.filter { occ ->
                val content = "${occ.talao} ${occ.address} ${occ.nature} ${occ.city} ${occ.commander}".lowercase()
                content.contains(txt)
            }
        }
        if (!horaInicio.isNullOrBlank() && !horaFim.isNullOrBlank()) {
            mapFiltradas = mapFiltradas.filter { occ ->
                // MapOccurrence doesn't store qtrSaida explicitly. 
                // As it's derived from Ocorrencia, we might not have it in MapOccurrence without modifying its model.
                // For now, we will skip time filter for MapOccurrence since Map is not focused on time.
                true 
            }
        }

        _state.value = estadoAtual.copy(
            ocorrenciasFiltradas = filtradas,
            mapOccurrencesFiltradas = mapFiltradas
        )
    }
}
