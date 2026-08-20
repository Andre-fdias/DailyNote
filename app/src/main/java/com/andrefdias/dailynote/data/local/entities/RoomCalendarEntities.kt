package com.andrefdias.dailynote.data.local.entities

import androidx.room.*

@Entity(tableName = "escala_config")
data class RoomEscalaConfig(
    @PrimaryKey val id: String,
    val nome: String,
    val trabalhoHoras: Int,
    val descansoHoras: Int,
    val quantidadeTurnos: Int,
    val ativa: Boolean,
    val descricao: String
)

@Entity(
    tableName = "equipes",
    foreignKeys = [
        ForeignKey(
            entity = RoomEscalaConfig::class,
            parentColumns = ["id"],
            childColumns = ["escalaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["escalaId"])]
)
data class RoomEquipe(
    @PrimaryKey val id: String,
    val nome: String,
    val sigla: String,
    val corFundo: String, // Hex string
    val corTexto: String,  // Hex string
    val corBorda: String?, // Hex string
    val escalaId: String?,
    val dataInicial: String, // YYYY-MM-DD
    val ordemTurno: Int, // 0 para diurno, 1 para noturno
    val ativa: Boolean,
    val horaInicio: String = "07:00", // HH:MM
    val horaTermino: String = "07:00"  // HH:MM
)

@Entity(tableName = "turnos")
data class RoomTurno(
    @PrimaryKey val id: String,
    val nome: String,
    val horaInicio: String, // HH:MM
    val horaTermino: String  // HH:MM
)

@Entity(tableName = "calendar_eventos")
data class RoomCalendarEvento(
    @PrimaryKey val id: String,
    val titulo: String,
    val descricao: String,
    val data: String, // YYYY-MM-DD
    val hora: String?, // HH:MM
    val local: String?,
    val categoria: String, // e.g. CURSO, TREINAMENTO, etc.
    val cor: String, // Hex string
    val recorrencia: String, // NUNCA, DIARIA, SEMANAL, MENSAL, ANUAL, PERSONALIZADA
    val lembreteMinutos: Int?,
    val escalaId: String? = null
)

@Entity(tableName = "calendar_tarefas")
data class RoomCalendarTarefa(
    @PrimaryKey val id: String,
    val titulo: String,
    val descricao: String,
    val data: String, // YYYY-MM-DD
    val hora: String?, // HH:MM
    val prioridade: String, // ALTA, MEDIA, BAIXA
    val status: String, // PENDENTE, CONCLUIDA
    val categoria: String,
    val responsavel: String?,
    val anexos: String?, // Comma separated list of paths
    val checklistJson: String?, // JSON string
    val escalaId: String? = null
)

@Entity(tableName = "notificacoes_historico")
data class RoomNotificacao(
    @PrimaryKey val id: String,
    val categoria: String, // ESCALAS, EVENTOS, etc.
    val titulo: String,
    val descricao: String,
    val data: String, // YYYY-MM-DD
    val hora: String, // HH:MM
    val prioridade: String, // ALTA, MEDIA, BAIXA
    val lida: Boolean,
    val origem: String
)

@Entity(tableName = "calendar_settings")
data class RoomCalendarSettings(
    @PrimaryKey val id: String = "global_calendar_settings",
    val mostrarPopupInicial: Boolean,
    val badgeHabilitado: Boolean,
    val somHabilitado: Boolean,
    val vibracaoHabilitada: Boolean,
    val lembretesAntecipadosMinutos: Int,
    val popupExibidoHoje: String? = null, // YYYY-MM-DD
    val calendarioConfigurado: Boolean
)
