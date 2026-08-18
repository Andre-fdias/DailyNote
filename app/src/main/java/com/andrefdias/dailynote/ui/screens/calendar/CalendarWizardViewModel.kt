package com.andrefdias.dailynote.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.domain.calendar.ScaleEngine
import com.andrefdias.dailynote.domain.model.*
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

data class WizardUiState(
    val selectedOption: String = "24x48", // 12x36, 24x48, 24x72, 5x2, 6x1, 12x24x12x48, CUSTOM
    val customNome: String = "",
    val customTrabalhoHoras: Int = 24,
    val customDescansoHoras: Int = 48,
    val customQuantidadeEquipes: Int = 3,
    val customQuantidadeTurnos: Int = 1,
    val customDescricao: String = "",
    val equipes: List<EquipeConfig> = emptyList(),
    val previewDays: Map<LocalDate, Map<Int, List<EquipeConfig>>> = emptyMap(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CalendarWizardViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(WizardUiState())
    val uiState: StateFlow<WizardUiState> = _uiState.asStateFlow()

    private val editEscalaId: String? = savedStateHandle.get<String>("escalaId")

    init {
        if (editEscalaId != null) {
            loadExistingEscala(editEscalaId)
        } else {
            regenerateEquipes()
        }
    }

    private fun loadExistingEscala(escalaId: String) {
        viewModelScope.launch {
            try {
                val escalas = calendarRepository.getEscalas()
                val escala = escalas.find { it.id == escalaId } ?: return@launch
                val allEquipes = calendarRepository.getEquipes()
                val teams = allEquipes.filter { it.escalaId == escalaId }
                
                // Determinamos o selectedOption de forma simples pelo nome ou horas, 
                // caso contrario cai em custom
                val option = when {
                    escala.nome.contains("24x48") -> "24x48"
                    escala.nome.contains("24x72") -> "24x72"
                    escala.nome.contains("12x36") -> "12x36"
                    escala.nome.contains("5x2") -> "5x2"
                    escala.nome.contains("6x1") -> "6x1"
                    escala.nome.contains("12x24") -> "12x24x12x48"
                    else -> "CUSTOM"
                }

                _uiState.update { state ->
                    state.copy(
                        selectedOption = option,
                        customNome = escala.nome,
                        customTrabalhoHoras = escala.trabalhoHoras,
                        customDescansoHoras = escala.descansoHoras,
                        customQuantidadeEquipes = teams.size,
                        customQuantidadeTurnos = escala.quantidadeTurnos,
                        customDescricao = escala.descricao,
                        equipes = teams
                    )
                }
                updatePreview()
            } catch (e: Exception) {
                // do nothing or handle error
            }
        }
    }


    fun onOptionSelected(option: String) {
        _uiState.update { it.copy(selectedOption = option) }
        regenerateEquipes()
    }

    fun updateCustomFields(
        nome: String? = null,
        trabalho: Int? = null,
        descanso: Int? = null,
        equipesCount: Int? = null,
        turnosCount: Int? = null,
        descricao: String? = null
    ) {
        _uiState.update { state ->
            state.copy(
                customNome = nome ?: state.customNome,
                customTrabalhoHoras = trabalho ?: state.customTrabalhoHoras,
                customDescansoHoras = descanso ?: state.customDescansoHoras,
                customQuantidadeEquipes = equipesCount ?: state.customQuantidadeEquipes,
                customQuantidadeTurnos = turnosCount ?: state.customQuantidadeTurnos,
                customDescricao = descricao ?: state.customDescricao
            )
        }
        if (nome != null || trabalho != null || descanso != null || equipesCount != null || turnosCount != null) {
            regenerateEquipes()
        }
    }

    fun updateEquipe(index: Int, updated: EquipeConfig) {
        _uiState.update { state ->
            val list = state.equipes.toMutableList()
            if (index in list.indices) {
                list[index] = updated
            }
            state.copy(equipes = list)
        }
        updatePreview()
    }

    fun regenerateEquipes() {
        val state = _uiState.value
        val numEquipes: Int
        val turnos: Int
        val trabalho: Int
        val descanso: Int
        val escalaId = editEscalaId ?: UUID.randomUUID().toString()

        when (state.selectedOption) {
            "24x48" -> {
                numEquipes = 3
                turnos = 1
                trabalho = 24
                descanso = 48
            }
            "24x72" -> {
                numEquipes = 4
                turnos = 1
                trabalho = 24
                descanso = 72
            }
            "12x36" -> {
                numEquipes = 4
                turnos = 2
                trabalho = 12
                descanso = 36
            }
            "5x2" -> {
                numEquipes = 7 // 7 posições
                turnos = 1
                trabalho = 8
                descanso = 16
            }
            "6x1" -> {
                numEquipes = 7 // 7 posições
                turnos = 1
                trabalho = 8
                descanso = 16
            }
            "12x24x12x48" -> {
                numEquipes = 4
                turnos = 2
                trabalho = 12
                descanso = 24 // virtual, pois é custom
            }
            else -> {
                // Custom
                numEquipes = state.customQuantidadeEquipes.coerceAtLeast(1)
                turnos = state.customQuantidadeTurnos.coerceIn(1, 2)
                trabalho = state.customTrabalhoHoras
                descanso = state.customDescansoHoras
            }
        }

        // Paleta expandida: 16 cores vibrantes e distintas para as equipes
        val defaultColors = listOf(
            Pair("#D32F2F", "#FFFFFF"), // Vermelho
            Pair("#1565C0", "#FFFFFF"), // Azul escuro
            Pair("#2E7D32", "#FFFFFF"), // Verde escuro
            Pair("#F9A825", "#000000"), // Amarelo
            Pair("#6A1B9A", "#FFFFFF"), // Roxo escuro
            Pair("#E65100", "#FFFFFF"), // Laranja escuro
            Pair("#00695C", "#FFFFFF"), // Teal escuro
            Pair("#AD1457", "#FFFFFF"), // Pink escuro
            Pair("#0277BD", "#FFFFFF"), // Azul claro
            Pair("#558B2F", "#FFFFFF"), // Verde oliva
            Pair("#4527A0", "#FFFFFF"), // Roxo profundo
            Pair("#BF360C", "#FFFFFF"), // Vermelho tijolo
            Pair("#00838F", "#FFFFFF"), // Ciano escuro
            Pair("#283593", "#FFFFFF"), // Azul índigo
            Pair("#37474F", "#FFFFFF"), // Cinza azulado
            Pair("#4E342E", "#FFFFFF")  // Marrom
        )

        val newEquipes = (0 until numEquipes).map { i ->
            val colorPair = defaultColors[i % defaultColors.size]
            val defaultName = when (state.selectedOption) {
                "5x2", "6x1" -> "Posição ${i + 1}"
                else -> {
                    val letters = listOf("A", "B", "C", "D", "E", "F", "G", "H")
                    "Equipe ${letters.getOrElse(i) { (i + 1).toString() }}"
                }
            }
            val sigla = when (state.selectedOption) {
                "5x2", "6x1" -> "${i + 1}"
                else -> {
                    val letters = listOf("A", "B", "C", "D", "E", "F", "G", "H")
                    letters.getOrElse(i) { (i + 1).toString() }
                }
            }

            // Define turno para escalas com dois turnos
            val ordemTurno = if (turnos == 2) i % 2 else 0

            // Horários padrão por tipo de escala e turno
            val (horaInicio, horaTermino) = defaultShiftTimes(state.selectedOption, ordemTurno)

            EquipeConfig(
                id = java.util.UUID.randomUUID().toString(),
                nome = defaultName,
                sigla = sigla,
                corFundo = colorPair.first,
                corTexto = colorPair.second,
                corBorda = null,
                escalaId = escalaId,
                dataInicial = LocalDate.now().toString(),
                ordemTurno = ordemTurno,
                ativa = true,
                horaInicio = horaInicio,
                horaTermino = horaTermino
            )
        }

        _uiState.update { it.copy(equipes = newEquipes) }
        updatePreview()
    }

    private fun updatePreview() {
        val state = _uiState.value
        val escalaId = editEscalaId ?: UUID.randomUUID().toString()
        val trabalho: Int
        val descanso: Int
        val turnos: Int
        val nome: String

        when (state.selectedOption) {
            "24x48" -> {
                trabalho = 24
                descanso = 48
                turnos = 1
                nome = "Escala 24x48"
            }
            "24x72" -> {
                trabalho = 24
                descanso = 72
                turnos = 1
                nome = "Escala 24x72"
            }
            "12x36" -> {
                trabalho = 12
                descanso = 36
                turnos = 2
                nome = "Escala 12x36"
            }
            "5x2" -> {
                trabalho = 8
                descanso = 16
                turnos = 1
                nome = "Escala 5x2"
            }
            "6x1" -> {
                trabalho = 8
                descanso = 16
                turnos = 1
                nome = "Escala 6x1"
            }
            "12x24x12x48" -> {
                trabalho = 12
                descanso = 24
                turnos = 2
                nome = "Escala 12x24x12x48"
            }
            else -> {
                trabalho = state.customTrabalhoHoras
                descanso = state.customDescansoHoras
                turnos = state.customQuantidadeTurnos
                nome = state.customNome.ifBlank { "Escala Personalizada" }
            }
        }

        val tempEscala = EscalaConfig(
            id = escalaId,
            nome = nome,
            trabalhoHoras = trabalho,
            descansoHoras = descanso,
            quantidadeTurnos = turnos,
            ativa = true,
            descricao = "Escala configurada no assistente inicial."
        )

        val preview = ScaleEngine.getPrecomputedMonthScales(
            LocalDate.now(),
            listOf(tempEscala),
            state.equipes
        )
        _uiState.update { it.copy(previewDays = preview) }
    }

    fun saveConfig() {
        val state = _uiState.value
        if (state.equipes.isEmpty()) return

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val escalaId = UUID.randomUUID().toString()
                val trabalho: Int
                val descanso: Int
                val turnos: Int
                val nome: String
                val desc: String

                when (state.selectedOption) {
                    "24x48" -> {
                        trabalho = 24
                        descanso = 48
                        turnos = 1
                        nome = "Escala 24x48"
                        desc = "Plantão de 24 horas de serviço por 48 horas de folga"
                    }
                    "24x72" -> {
                        trabalho = 24
                        descanso = 72
                        turnos = 1
                        nome = "Escala 24x72"
                        desc = "Plantão de 24 horas de serviço por 72 horas de folga"
                    }
                    "12x36" -> {
                        trabalho = 12
                        descanso = 36
                        turnos = 2
                        nome = "Escala 12x36"
                        desc = "Plantão de 12 horas diurno/noturno por 36 horas de folga"
                    }
                    "5x2" -> {
                        trabalho = 8
                        descanso = 16
                        turnos = 1
                        nome = "Escala 5x2"
                        desc = "Escala administrativa semanal de 5 dias de trabalho por 2 dias de folga"
                    }
                    "6x1" -> {
                        trabalho = 8
                        descanso = 16
                        turnos = 1
                        nome = "Escala 6x1"
                        desc = "Escala semanal de 6 dias de trabalho por 1 dia de folga"
                    }
                    "12x24x12x48" -> {
                        trabalho = 12
                        descanso = 24
                        turnos = 2
                        nome = "Escala 12x24x12x48"
                        desc = "Serviço de 12 horas diurno, 24 horas de folga, 12 horas noturno, 48 horas de folga"
                    }
                    else -> {
                        trabalho = state.customTrabalhoHoras
                        descanso = state.customDescansoHoras
                        turnos = state.customQuantidadeTurnos
                        nome = state.customNome.ifBlank { "Escala Personalizada" }
                        desc = state.customDescricao.ifBlank { "Escala personalizada parametrizada pelo usuário" }
                    }
                }

                // 1. Salvar a Escala
                val escala = EscalaConfig(
                    id = escalaId,
                    nome = nome,
                    trabalhoHoras = trabalho,
                    descansoHoras = descanso,
                    quantidadeTurnos = turnos,
                    ativa = true,
                    descricao = desc
                )
                calendarRepository.saveEscala(escala)

                // 2. Salvar as equipes vinculadas
                state.equipes.forEach { eq ->
                    val finalEquipe = eq.copy(escalaId = escalaId)
                    calendarRepository.saveEquipe(finalEquipe)
                }

                // 3. Salvar Turnos padrão se não existirem
                // Apenas para garantir que o banco tenha os turnos mínimos
                calendarRepository.saveTurno(TurnoConfig("turno_diurno", "Turno Diurno", "07:00", "19:00"))
                calendarRepository.saveTurno(TurnoConfig("turno_noturno", "Turno Noturno", "19:00", "07:00"))

                // 4. Salvar configurações com calendarioConfigurado = true
                val currentSettings = calendarRepository.getSettings().getOrNull() ?: CalendarSettings()
                calendarRepository.saveSettings(currentSettings.copy(calendarioConfigurado = true))

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.localizedMessage ?: "Erro ao salvar a configuração.") }
            }
        }
    }

    /**
     * Retorna o par (horaInicio, horaTermino) padrão por tipo de escala e turno.
     * Turno 0 = Diurno, Turno 1 = Noturno
     */
    private fun defaultShiftTimes(option: String, ordemTurno: Int): Pair<String, String> {
        return when (option) {
            "12x36"        -> if (ordemTurno == 0) Pair("06:00", "18:00") else Pair("18:00", "06:00")
            "12x24x12x48"  -> if (ordemTurno == 0) Pair("06:00", "18:00") else Pair("18:00", "06:00")
            "24x48"        -> Pair("07:00", "07:00") // Plantão de 24h
            "24x72"        -> Pair("07:00", "07:00") // Plantão de 24h
            "5x2"          -> Pair("08:00", "17:00") // Administrativo
            "6x1"          -> Pair("08:00", "17:00") // Administrativo
            else           -> Pair("07:00", "07:00") // Custom: padrão 24h
        }
    }
}
