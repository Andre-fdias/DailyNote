package com.andrefdias.dailynote.domain.repository

import com.andrefdias.dailynote.domain.model.*
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {

    // --- Escalas ---
    suspend fun saveEscala(escala: EscalaConfig): Result<Unit>
    suspend fun deleteEscala(id: String): Result<Unit>
    suspend fun getEscalaById(id: String): Result<EscalaConfig?>
    fun getEscalasFlow(): Flow<List<EscalaConfig>>
    suspend fun getEscalas(): List<EscalaConfig>

    // --- Equipes ---
    suspend fun saveEquipe(equipe: EquipeConfig): Result<Unit>
    suspend fun deleteEquipe(id: String): Result<Unit>
    fun getEquipesFlow(): Flow<List<EquipeConfig>>
    suspend fun getEquipes(): List<EquipeConfig>

    // --- Turnos ---
    suspend fun saveTurno(turno: TurnoConfig): Result<Unit>
    suspend fun getTurnos(): List<TurnoConfig>
    fun getTurnosFlow(): Flow<List<TurnoConfig>>

    // --- Eventos ---
    suspend fun saveEvento(evento: CalendarEvento): Result<Unit>
    suspend fun deleteEvento(id: String): Result<Unit>
    suspend fun getEventoById(id: String): Result<CalendarEvento?>
    fun getEventosForDayFlow(data: String): Flow<List<CalendarEvento>>
    suspend fun getEventosForDay(data: String): List<CalendarEvento>
    fun getAllEventosFlow(): Flow<List<CalendarEvento>>
    suspend fun getAllEventos(): List<CalendarEvento>

    // --- Tarefas ---
    suspend fun saveTarefa(tarefa: CalendarTarefa): Result<Unit>
    suspend fun deleteTarefa(id: String): Result<Unit>
    suspend fun getTarefaById(id: String): Result<CalendarTarefa?>
    fun getTarefasForDayFlow(data: String): Flow<List<CalendarTarefa>>
    suspend fun getTarefasForDay(data: String): List<CalendarTarefa>
    fun getAllTarefasFlow(): Flow<List<CalendarTarefa>>
    suspend fun getAllTarefas(): List<CalendarTarefa>

    // --- Notificações ---
    suspend fun saveNotificacao(notificacao: CalendarNotificacao): Result<Unit>
    fun getNotificacoesFlow(): Flow<List<CalendarNotificacao>>
    suspend fun getNotificacoes(): List<CalendarNotificacao>
    fun getUnreadNotificacoesCountFlow(): Flow<Int>
    suspend fun markAllAsRead(): Result<Unit>
    suspend fun clearAllNotificacoes(): Result<Unit>
    suspend fun deleteNotificacao(id: String): Result<Unit>

    // --- Configurações ---
    suspend fun saveSettings(settings: CalendarSettings): Result<Unit>
    suspend fun getSettings(): Result<CalendarSettings?>
    fun getSettingsFlow(): Flow<CalendarSettings?>
}
