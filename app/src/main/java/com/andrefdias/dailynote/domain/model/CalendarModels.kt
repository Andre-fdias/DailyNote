package com.andrefdias.dailynote.domain.model

data class EscalaConfig(
    val id: String,
    val nome: String,
    val trabalhoHoras: Int,
    val descansoHoras: Int,
    val quantidadeTurnos: Int,
    val ativa: Boolean,
    val descricao: String
)

data class EquipeConfig(
    val id: String,
    val nome: String,
    val sigla: String,
    val corFundo: String,
    val corTexto: String,
    val corBorda: String?,
    val escalaId: String?,
    val dataInicial: String, // YYYY-MM-DD
    val ordemTurno: Int, // 0 para diurno, 1 para noturno
    val ativa: Boolean,
    val horaInicio: String = "07:00", // HH:MM — início do turno
    val horaTermino: String = "07:00"  // HH:MM — término (pode cruzar meia-noite se <= horaInicio)
)

data class TurnoConfig(
    val id: String,
    val nome: String,
    val horaInicio: String, // HH:MM
    val horaTermino: String // HH:MM
)

enum class CategoriaEvento {
    CURSO, TREINAMENTO, REUNIAO, INSPECAO, VISTORIA, LICENCA, FERIAS, BANCO_HORAS, PERSONALIZADO
}

enum class RecorrenciaTipo {
    NUNCA, DIARIA, SEMANAL, MENSAL, ANUAL, PERSONALIZADA
}

data class CalendarEvento(
    val id: String,
    val titulo: String,
    val descricao: String,
    val data: String, // YYYY-MM-DD
    val hora: String?, // HH:MM
    val local: String?,
    val categoria: CategoriaEvento,
    val cor: String, // Hex string
    val recorrencia: RecorrenciaTipo,
    val lembreteMinutos: Int?,
    val escalaId: String? = null
)

enum class PrioridadeTarefa {
    ALTA, MEDIA, BAIXA
}

enum class StatusTarefa {
    PENDENTE, CONCLUIDA
}

data class ChecklistItem(
    val id: String,
    val titulo: String,
    val concluido: Boolean
)

data class CalendarTarefa(
    val id: String,
    val titulo: String,
    val descricao: String,
    val data: String, // YYYY-MM-DD
    val hora: String?, // HH:MM
    val prioridade: PrioridadeTarefa,
    val status: StatusTarefa,
    val categoria: String,
    val responsavel: String?,
    val anexos: List<String> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val escalaId: String? = null
)

enum class CategoriaNotificacao {
    ESCALAS, EVENTOS, AGENDA, TAREFAS, SISTEMA, BACKUP, OCORRENCIAS, TREINAMENTOS
}

data class CalendarNotificacao(
    val id: String,
    val categoria: CategoriaNotificacao,
    val titulo: String,
    val descricao: String,
    val data: String, // YYYY-MM-DD
    val hora: String, // HH:MM
    val prioridade: PrioridadeTarefa,
    val lida: Boolean,
    val origem: String
)

data class CalendarSettings(
    val id: String = "global_calendar_settings",
    val mostrarPopupInicial: Boolean = true,
    val badgeHabilitado: Boolean = true,
    val somHabilitado: Boolean = true,
    val vibracaoHabilitada: Boolean = true,
    val lembretesAntecipadosMinutos: Int = 15,
    val popupExibidoHoje: String? = null, // YYYY-MM-DD
    val calendarioConfigurado: Boolean = false
)

data class SubtarefaInput(
    val id: String = java.util.UUID.randomUUID().toString(),
    val titulo: String,
    val level: Int,
    val concluida: Boolean = false
)

