package com.andrefdias.dailynote.data.repository

import com.andrefdias.dailynote.data.local.dao.CalendarDao
import com.andrefdias.dailynote.data.local.entities.*
import com.andrefdias.dailynote.domain.model.*
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class ChecklistItemDto(
    val id: String,
    val titulo: String,
    val concluido: Boolean,
    val level: Int = 0
)

@Singleton
class RoomCalendarRepository @Inject constructor(
    private val calendarDao: CalendarDao
) : CalendarRepository {

    private val json = Json { ignoreUnknownKeys = true }

    // --- Escalas ---
    override suspend fun saveEscala(escala: EscalaConfig): Result<Unit> = runCatching {
        calendarDao.insertEscala(
            RoomEscalaConfig(
                id = escala.id,
                nome = escala.nome,
                trabalhoHoras = escala.trabalhoHoras,
                descansoHoras = escala.descansoHoras,
                quantidadeTurnos = escala.quantidadeTurnos,
                ativa = escala.ativa,
                descricao = escala.descricao
            )
        )
    }

    override suspend fun deleteEscala(id: String): Result<Unit> = runCatching {
        calendarDao.deleteEscala(id)
    }

    override suspend fun getEscalaById(id: String): Result<EscalaConfig?> = runCatching {
        calendarDao.getEscalaById(id)?.let {
            EscalaConfig(
                id = it.id,
                nome = it.nome,
                trabalhoHoras = it.trabalhoHoras,
                descansoHoras = it.descansoHoras,
                quantidadeTurnos = it.quantidadeTurnos,
                ativa = it.ativa,
                descricao = it.descricao
            )
        }
    }

    override fun getEscalasFlow(): Flow<List<EscalaConfig>> {
        return calendarDao.getEscalasFlow().map { list ->
            list.map {
                EscalaConfig(
                    id = it.id,
                    nome = it.nome,
                    trabalhoHoras = it.trabalhoHoras,
                    descansoHoras = it.descansoHoras,
                    quantidadeTurnos = it.quantidadeTurnos,
                    ativa = it.ativa,
                    descricao = it.descricao
                )
            }
        }
    }

    override suspend fun getEscalas(): List<EscalaConfig> {
        return calendarDao.getEscalas().map {
            EscalaConfig(
                id = it.id,
                nome = it.nome,
                trabalhoHoras = it.trabalhoHoras,
                descansoHoras = it.descansoHoras,
                quantidadeTurnos = it.quantidadeTurnos,
                ativa = it.ativa,
                descricao = it.descricao
            )
        }
    }

    // --- Equipes ---
    override suspend fun saveEquipe(equipe: EquipeConfig): Result<Unit> = runCatching {
        calendarDao.insertEquipe(
            RoomEquipe(
                id = equipe.id,
                nome = equipe.nome,
                sigla = equipe.sigla,
                corFundo = equipe.corFundo,
                corTexto = equipe.corTexto,
                corBorda = equipe.corBorda,
                escalaId = equipe.escalaId,
                dataInicial = equipe.dataInicial,
                ordemTurno = equipe.ordemTurno,
                ativa = equipe.ativa,
                horaInicio = equipe.horaInicio,
                horaTermino = equipe.horaTermino
            )
        )
    }

    override suspend fun deleteEquipe(id: String): Result<Unit> = runCatching {
        calendarDao.deleteEquipe(id)
    }

    override fun getEquipesFlow(): Flow<List<EquipeConfig>> {
        return calendarDao.getEquipesFlow().map { list ->
            list.map {
                EquipeConfig(
                    id = it.id,
                    nome = it.nome,
                    sigla = it.sigla,
                    corFundo = it.corFundo,
                    corTexto = it.corTexto,
                    corBorda = it.corBorda,
                    escalaId = it.escalaId,
                    dataInicial = it.dataInicial,
                    ordemTurno = it.ordemTurno,
                    ativa = it.ativa,
                    horaInicio = it.horaInicio,
                    horaTermino = it.horaTermino
                )
            }
        }
    }

    override suspend fun getEquipes(): List<EquipeConfig> {
        return calendarDao.getEquipes().map {
            EquipeConfig(
                id = it.id,
                nome = it.nome,
                sigla = it.sigla,
                corFundo = it.corFundo,
                corTexto = it.corTexto,
                corBorda = it.corBorda,
                escalaId = it.escalaId,
                dataInicial = it.dataInicial,
                ordemTurno = it.ordemTurno,
                ativa = it.ativa,
                horaInicio = it.horaInicio,
                horaTermino = it.horaTermino
            )
        }
    }

    // --- Turnos ---
    override suspend fun saveTurno(turno: TurnoConfig): Result<Unit> = runCatching {
        calendarDao.insertTurno(
            RoomTurno(
                id = turno.id,
                nome = turno.nome,
                horaInicio = turno.horaInicio,
                horaTermino = turno.horaTermino
            )
        )
    }

    override suspend fun getTurnos(): List<TurnoConfig> {
        return calendarDao.getTurnos().map {
            TurnoConfig(
                id = it.id,
                nome = it.nome,
                horaInicio = it.horaInicio,
                horaTermino = it.horaTermino
            )
        }
    }

    override fun getTurnosFlow(): Flow<List<TurnoConfig>> {
        return calendarDao.getTurnosFlow().map { list ->
            list.map {
                TurnoConfig(
                    id = it.id,
                    nome = it.nome,
                    horaInicio = it.horaInicio,
                    horaTermino = it.horaTermino
                )
            }
        }
    }

    // --- Eventos ---
    override suspend fun saveEvento(evento: CalendarEvento): Result<Unit> = runCatching {
        calendarDao.insertEvento(
            RoomCalendarEvento(
                id = evento.id,
                titulo = evento.titulo,
                descricao = evento.descricao,
                data = evento.data,
                hora = evento.hora,
                local = evento.local,
                categoria = evento.categoria.name,
                cor = evento.cor,
                recorrencia = evento.recorrencia.name,
                lembreteMinutos = evento.lembreteMinutos,
                escalaId = evento.escalaId
            )
        )
    }

    override suspend fun deleteEvento(id: String): Result<Unit> = runCatching {
        calendarDao.deleteEvento(id)
    }

    override suspend fun getEventoById(id: String): Result<CalendarEvento?> = runCatching {
        calendarDao.getEventoById(id)?.let {
            CalendarEvento(
                id = it.id,
                titulo = it.titulo,
                descricao = it.descricao,
                data = it.data,
                hora = it.hora,
                local = it.local,
                categoria = runCatching { CategoriaEvento.valueOf(it.categoria) }.getOrDefault(CategoriaEvento.PERSONALIZADO),
                cor = it.cor,
                recorrencia = runCatching { RecorrenciaTipo.valueOf(it.recorrencia) }.getOrDefault(RecorrenciaTipo.NUNCA),
                lembreteMinutos = it.lembreteMinutos,
                escalaId = it.escalaId
            )
        }
    }

    override fun getEventosForDayFlow(data: String): Flow<List<CalendarEvento>> {
        return calendarDao.getEventosForDayFlow(data).map { list ->
            list.map {
                CalendarEvento(
                    id = it.id,
                    titulo = it.titulo,
                    descricao = it.descricao,
                    data = it.data,
                    hora = it.hora,
                    local = it.local,
                    categoria = runCatching { CategoriaEvento.valueOf(it.categoria) }.getOrDefault(CategoriaEvento.PERSONALIZADO),
                    cor = it.cor,
                    recorrencia = runCatching { RecorrenciaTipo.valueOf(it.recorrencia) }.getOrDefault(RecorrenciaTipo.NUNCA),
                    lembreteMinutos = it.lembreteMinutos
                )
            }
        }
    }

    override suspend fun getEventosForDay(data: String): List<CalendarEvento> {
        return calendarDao.getEventosForDay(data).map {
            CalendarEvento(
                id = it.id,
                titulo = it.titulo,
                descricao = it.descricao,
                data = it.data,
                hora = it.hora,
                local = it.local,
                categoria = runCatching { CategoriaEvento.valueOf(it.categoria) }.getOrDefault(CategoriaEvento.PERSONALIZADO),
                cor = it.cor,
                recorrencia = runCatching { RecorrenciaTipo.valueOf(it.recorrencia) }.getOrDefault(RecorrenciaTipo.NUNCA),
                lembreteMinutos = it.lembreteMinutos,
                escalaId = it.escalaId
            )
        }
    }

    override fun getAllEventosFlow(): Flow<List<CalendarEvento>> {
        return calendarDao.getAllEventosFlow().map { list ->
            list.map {
                CalendarEvento(
                    id = it.id,
                    titulo = it.titulo,
                    descricao = it.descricao,
                    data = it.data,
                    hora = it.hora,
                    local = it.local,
                    categoria = runCatching { CategoriaEvento.valueOf(it.categoria) }.getOrDefault(CategoriaEvento.PERSONALIZADO),
                    cor = it.cor,
                    recorrencia = runCatching { RecorrenciaTipo.valueOf(it.recorrencia) }.getOrDefault(RecorrenciaTipo.NUNCA),
                    lembreteMinutos = it.lembreteMinutos
                )
            }
        }
    }

    override suspend fun getAllEventos(): List<CalendarEvento> {
        return calendarDao.getAllEventos().map {
            CalendarEvento(
                id = it.id,
                titulo = it.titulo,
                descricao = it.descricao,
                data = it.data,
                hora = it.hora,
                local = it.local,
                categoria = runCatching { CategoriaEvento.valueOf(it.categoria) }.getOrDefault(CategoriaEvento.PERSONALIZADO),
                cor = it.cor,
                recorrencia = runCatching { RecorrenciaTipo.valueOf(it.recorrencia) }.getOrDefault(RecorrenciaTipo.NUNCA),
                lembreteMinutos = it.lembreteMinutos,
                escalaId = it.escalaId
            )
        }
    }

    // --- Tarefas ---
    override suspend fun saveTarefa(tarefa: CalendarTarefa): Result<Unit> = runCatching {
        val checklistJsonStr = json.encodeToString(
            tarefa.checklist.map { ChecklistItemDto(it.id, it.titulo, it.concluido, it.level) }
        )
        val anexosStr = if (tarefa.anexos.isEmpty()) null else tarefa.anexos.joinToString(",")

        calendarDao.insertTarefa(
            RoomCalendarTarefa(
                id = tarefa.id,
                titulo = tarefa.titulo,
                descricao = tarefa.descricao,
                data = tarefa.data,
                hora = tarefa.hora,
                prioridade = tarefa.prioridade.name,
                status = tarefa.status.name,
                categoria = tarefa.categoria,
                responsavel = tarefa.responsavel,
                anexos = anexosStr,
                checklistJson = checklistJsonStr,
                escalaId = tarefa.escalaId
            )
        )
    }

    override suspend fun deleteTarefa(id: String): Result<Unit> = runCatching {
        calendarDao.deleteTarefa(id)
    }

    override suspend fun getTarefaById(id: String): Result<CalendarTarefa?> = runCatching {
        calendarDao.getTarefaById(id)?.let {
            val checklist = if (it.checklistJson.isNullOrBlank()) {
                emptyList()
            } else {
                runCatching {
                    json.decodeFromString<List<ChecklistItemDto>>(it.checklistJson).map { dto ->
                        ChecklistItem(dto.id, dto.titulo, dto.concluido, dto.level)
                    }
                }.getOrDefault(emptyList())
            }
            val anexos = if (it.anexos.isNullOrBlank()) emptyList() else it.anexos.split(",")

            CalendarTarefa(
                id = it.id,
                titulo = it.titulo,
                descricao = it.descricao,
                data = it.data,
                hora = it.hora,
                prioridade = runCatching { PrioridadeTarefa.valueOf(it.prioridade) }.getOrDefault(PrioridadeTarefa.MEDIA),
                status = runCatching { StatusTarefa.valueOf(it.status) }.getOrDefault(StatusTarefa.PENDENTE),
                categoria = it.categoria,
                responsavel = it.responsavel,
                anexos = anexos,
                checklist = checklist,
                escalaId = it.escalaId
            )
        }
    }

    override fun getTarefasForDayFlow(data: String): Flow<List<CalendarTarefa>> {
        return calendarDao.getTarefasForDayFlow(data).map { list ->
            list.map {
                val checklist = if (it.checklistJson.isNullOrBlank()) {
                    emptyList()
                } else {
                    runCatching {
                        json.decodeFromString<List<ChecklistItemDto>>(it.checklistJson).map { dto ->
                            ChecklistItem(dto.id, dto.titulo, dto.concluido, dto.level)
                        }
                    }.getOrDefault(emptyList())
                }
                val anexos = if (it.anexos.isNullOrBlank()) emptyList() else it.anexos.split(",")

                CalendarTarefa(
                    id = it.id,
                    titulo = it.titulo,
                    descricao = it.descricao,
                    data = it.data,
                    hora = it.hora,
                    prioridade = runCatching { PrioridadeTarefa.valueOf(it.prioridade) }.getOrDefault(PrioridadeTarefa.MEDIA),
                    status = runCatching { StatusTarefa.valueOf(it.status) }.getOrDefault(StatusTarefa.PENDENTE),
                    categoria = it.categoria,
                    responsavel = it.responsavel,
                    anexos = anexos,
                    checklist = checklist
                )
            }
        }
    }

    override suspend fun getTarefasForDay(data: String): List<CalendarTarefa> {
        return calendarDao.getTarefasForDay(data).map {
            val checklist = if (it.checklistJson.isNullOrBlank()) {
                emptyList()
            } else {
                runCatching {
                    json.decodeFromString<List<ChecklistItemDto>>(it.checklistJson).map { dto ->
                        ChecklistItem(dto.id, dto.titulo, dto.concluido, dto.level)
                    }
                }.getOrDefault(emptyList())
            }
            val anexos = if (it.anexos.isNullOrBlank()) emptyList() else it.anexos.split(",")

            CalendarTarefa(
                id = it.id,
                titulo = it.titulo,
                descricao = it.descricao,
                data = it.data,
                hora = it.hora,
                prioridade = runCatching { PrioridadeTarefa.valueOf(it.prioridade) }.getOrDefault(PrioridadeTarefa.MEDIA),
                status = runCatching { StatusTarefa.valueOf(it.status) }.getOrDefault(StatusTarefa.PENDENTE),
                categoria = it.categoria,
                responsavel = it.responsavel,
                anexos = anexos,
                checklist = checklist,
                escalaId = it.escalaId
            )
        }
    }

    override fun getAllTarefasFlow(): Flow<List<CalendarTarefa>> {
        return calendarDao.getAllTarefasFlow().map { list ->
            list.map {
                val checklist = if (it.checklistJson.isNullOrBlank()) {
                    emptyList()
                } else {
                    runCatching {
                        json.decodeFromString<List<ChecklistItemDto>>(it.checklistJson).map { dto ->
                            ChecklistItem(dto.id, dto.titulo, dto.concluido, dto.level)
                        }
                    }.getOrDefault(emptyList())
                }
                val anexos = if (it.anexos.isNullOrBlank()) emptyList() else it.anexos.split(",")

                CalendarTarefa(
                    id = it.id,
                    titulo = it.titulo,
                    descricao = it.descricao,
                    data = it.data,
                    hora = it.hora,
                    prioridade = runCatching { PrioridadeTarefa.valueOf(it.prioridade) }.getOrDefault(PrioridadeTarefa.MEDIA),
                    status = runCatching { StatusTarefa.valueOf(it.status) }.getOrDefault(StatusTarefa.PENDENTE),
                    categoria = it.categoria,
                    responsavel = it.responsavel,
                    anexos = anexos,
                    checklist = checklist
                )
            }
        }
    }

    override suspend fun getAllTarefas(): List<CalendarTarefa> {
        return calendarDao.getAllTarefas().map {
            val checklist = if (it.checklistJson.isNullOrBlank()) {
                emptyList()
            } else {
                runCatching {
                    json.decodeFromString<List<ChecklistItemDto>>(it.checklistJson).map { dto ->
                        ChecklistItem(dto.id, dto.titulo, dto.concluido, dto.level)
                    }
                }.getOrDefault(emptyList())
            }
            val anexos = if (it.anexos.isNullOrBlank()) emptyList() else it.anexos.split(",")

            CalendarTarefa(
                id = it.id,
                titulo = it.titulo,
                descricao = it.descricao,
                data = it.data,
                hora = it.hora,
                prioridade = runCatching { PrioridadeTarefa.valueOf(it.prioridade) }.getOrDefault(PrioridadeTarefa.MEDIA),
                status = runCatching { StatusTarefa.valueOf(it.status) }.getOrDefault(StatusTarefa.PENDENTE),
                categoria = it.categoria,
                responsavel = it.responsavel,
                anexos = anexos,
                checklist = checklist,
                escalaId = it.escalaId
            )
        }
    }

    // --- Notificações ---
    override suspend fun saveNotificacao(notificacao: CalendarNotificacao): Result<Unit> = runCatching {
        calendarDao.insertNotificacao(
            RoomNotificacao(
                id = notificacao.id,
                categoria = notificacao.categoria.name,
                titulo = notificacao.titulo,
                descricao = notificacao.descricao,
                data = notificacao.data,
                hora = notificacao.hora,
                prioridade = notificacao.prioridade.name,
                lida = notificacao.lida,
                origem = notificacao.origem
            )
        )
    }

    override fun getNotificacoesFlow(): Flow<List<CalendarNotificacao>> {
        return calendarDao.getNotificacoesFlow().map { list ->
            list.map {
                CalendarNotificacao(
                    id = it.id,
                    categoria = runCatching { CategoriaNotificacao.valueOf(it.categoria) }.getOrDefault(CategoriaNotificacao.SISTEMA),
                    titulo = it.titulo,
                    descricao = it.descricao,
                    data = it.data,
                    hora = it.hora,
                    prioridade = runCatching { PrioridadeTarefa.valueOf(it.prioridade) }.getOrDefault(PrioridadeTarefa.MEDIA),
                    lida = it.lida,
                    origem = it.origem
                )
            }
        }
    }

    override suspend fun getNotificacoes(): List<CalendarNotificacao> {
        return calendarDao.getNotificacoes().map {
            CalendarNotificacao(
                id = it.id,
                categoria = runCatching { CategoriaNotificacao.valueOf(it.categoria) }.getOrDefault(CategoriaNotificacao.SISTEMA),
                titulo = it.titulo,
                descricao = it.descricao,
                data = it.data,
                hora = it.hora,
                prioridade = runCatching { PrioridadeTarefa.valueOf(it.prioridade) }.getOrDefault(PrioridadeTarefa.MEDIA),
                lida = it.lida,
                origem = it.origem
            )
        }
    }

    override fun getUnreadNotificacoesCountFlow(): Flow<Int> {
        return calendarDao.getUnreadNotificacoesCountFlow()
    }

    override suspend fun markAllAsRead(): Result<Unit> = runCatching {
        calendarDao.markAllAsRead()
    }

    override suspend fun clearAllNotificacoes(): Result<Unit> = runCatching {
        calendarDao.clearAllNotificacoes()
    }

    override suspend fun deleteNotificacao(id: String): Result<Unit> = runCatching {
        calendarDao.deleteNotificacao(id)
    }

    // --- Configurações ---
    override suspend fun saveSettings(settings: CalendarSettings): Result<Unit> = runCatching {
        calendarDao.insertSettings(
            RoomCalendarSettings(
                id = settings.id,
                mostrarPopupInicial = settings.mostrarPopupInicial,
                badgeHabilitado = settings.badgeHabilitado,
                somHabilitado = settings.somHabilitado,
                vibracaoHabilitada = settings.vibracaoHabilitada,
                lembretesAntecipadosMinutos = settings.lembretesAntecipadosMinutos,
                popupExibidoHoje = settings.popupExibidoHoje,
                calendarioConfigurado = settings.calendarioConfigurado
            )
        )
    }

    override suspend fun getSettings(): Result<CalendarSettings?> = runCatching {
        calendarDao.getSettings()?.let {
            CalendarSettings(
                id = it.id,
                mostrarPopupInicial = it.mostrarPopupInicial,
                badgeHabilitado = it.badgeHabilitado,
                somHabilitado = it.somHabilitado,
                vibracaoHabilitada = it.vibracaoHabilitada,
                lembretesAntecipadosMinutos = it.lembretesAntecipadosMinutos,
                popupExibidoHoje = it.popupExibidoHoje,
                calendarioConfigurado = it.calendarioConfigurado
            )
        }
    }

    override fun getSettingsFlow(): Flow<CalendarSettings?> {
        return calendarDao.getSettingsFlow().map {
            it?.let {
                CalendarSettings(
                    id = it.id,
                    mostrarPopupInicial = it.mostrarPopupInicial,
                    badgeHabilitado = it.badgeHabilitado,
                    somHabilitado = it.somHabilitado,
                    vibracaoHabilitada = it.vibracaoHabilitada,
                    lembretesAntecipadosMinutos = it.lembretesAntecipadosMinutos,
                    popupExibidoHoje = it.popupExibidoHoje,
                    calendarioConfigurado = it.calendarioConfigurado
                )
            }
        }
    }
}
