package com.andrefdias.dailynote.data.local.dao

import androidx.room.*
import com.andrefdias.dailynote.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {

    // --- Escalas ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEscala(escala: RoomEscalaConfig)

    @Update
    suspend fun updateEscala(escala: RoomEscalaConfig)

    @Query("DELETE FROM escala_config WHERE id = :id")
    suspend fun deleteEscala(id: String)

    @Query("SELECT * FROM escala_config WHERE id = :id")
    suspend fun getEscalaById(id: String): RoomEscalaConfig?

    @Query("SELECT * FROM escala_config ORDER BY nome ASC")
    fun getEscalasFlow(): Flow<List<RoomEscalaConfig>>

    @Query("SELECT * FROM escala_config ORDER BY nome ASC")
    suspend fun getEscalas(): List<RoomEscalaConfig>

    // --- Equipes ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipe(equipe: RoomEquipe)

    @Update
    suspend fun updateEquipe(equipe: RoomEquipe)

    @Query("DELETE FROM equipes WHERE id = :id")
    suspend fun deleteEquipe(id: String)

    @Query("SELECT * FROM equipes ORDER BY nome ASC")
    fun getEquipesFlow(): Flow<List<RoomEquipe>>

    @Query("SELECT * FROM equipes ORDER BY nome ASC")
    suspend fun getEquipes(): List<RoomEquipe>

    // --- Turnos ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurno(turno: RoomTurno)

    @Update
    suspend fun updateTurno(turno: RoomTurno)

    @Query("SELECT * FROM turnos ORDER BY nome ASC")
    suspend fun getTurnos(): List<RoomTurno>

    @Query("SELECT * FROM turnos ORDER BY nome ASC")
    fun getTurnosFlow(): Flow<List<RoomTurno>>

    // --- Eventos ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvento(evento: RoomCalendarEvento)

    @Update
    suspend fun updateEvento(evento: RoomCalendarEvento)

    @Query("DELETE FROM calendar_eventos WHERE id = :id")
    suspend fun deleteEvento(id: String)

    @Query("SELECT * FROM calendar_eventos WHERE id = :id")
    suspend fun getEventoById(id: String): RoomCalendarEvento?

    @Query("SELECT * FROM calendar_eventos WHERE data = :data ORDER BY hora ASC")
    fun getEventosForDayFlow(data: String): Flow<List<RoomCalendarEvento>>

    @Query("SELECT * FROM calendar_eventos WHERE data = :data ORDER BY hora ASC")
    suspend fun getEventosForDay(data: String): List<RoomCalendarEvento>

    @Query("SELECT * FROM calendar_eventos ORDER BY data ASC, hora ASC")
    fun getAllEventosFlow(): Flow<List<RoomCalendarEvento>>

    @Query("SELECT * FROM calendar_eventos ORDER BY data ASC, hora ASC")
    suspend fun getAllEventos(): List<RoomCalendarEvento>

    // --- Tarefas ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarefa(tarefa: RoomCalendarTarefa)

    @Update
    suspend fun updateTarefa(tarefa: RoomCalendarTarefa)

    @Query("DELETE FROM calendar_tarefas WHERE id = :id")
    suspend fun deleteTarefa(id: String)

    @Query("SELECT * FROM calendar_tarefas WHERE id = :id")
    suspend fun getTarefaById(id: String): RoomCalendarTarefa?

    @Query("SELECT * FROM calendar_tarefas WHERE data = :data ORDER BY hora ASC")
    fun getTarefasForDayFlow(data: String): Flow<List<RoomCalendarTarefa>>

    @Query("SELECT * FROM calendar_tarefas WHERE data = :data ORDER BY hora ASC")
    suspend fun getTarefasForDay(data: String): List<RoomCalendarTarefa>

    @Query("SELECT * FROM calendar_tarefas ORDER BY data ASC, hora ASC")
    fun getAllTarefasFlow(): Flow<List<RoomCalendarTarefa>>

    @Query("SELECT * FROM calendar_tarefas ORDER BY data ASC, hora ASC")
    suspend fun getAllTarefas(): List<RoomCalendarTarefa>

    // --- Notificações ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificacao(notificacao: RoomNotificacao)

    @Update
    suspend fun updateNotificacao(notificacao: RoomNotificacao)

    @Query("SELECT * FROM notificacoes_historico ORDER BY data DESC, hora DESC")
    fun getNotificacoesFlow(): Flow<List<RoomNotificacao>>

    @Query("SELECT * FROM notificacoes_historico ORDER BY data DESC, hora DESC")
    suspend fun getNotificacoes(): List<RoomNotificacao>

    @Query("SELECT COUNT(*) FROM notificacoes_historico WHERE lida = 0")
    fun getUnreadNotificacoesCountFlow(): Flow<Int>

    @Query("UPDATE notificacoes_historico SET lida = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notificacoes_historico")
    suspend fun clearAllNotificacoes()

    @Query("DELETE FROM notificacoes_historico WHERE id = :notificacaoId")
    suspend fun deleteNotificacao(notificacaoId: String)

    // --- Configurações ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: RoomCalendarSettings)

    @Query("SELECT * FROM calendar_settings WHERE id = 'global_calendar_settings'")
    suspend fun getSettings(): RoomCalendarSettings?

    @Query("SELECT * FROM calendar_settings WHERE id = 'global_calendar_settings'")
    fun getSettingsFlow(): Flow<RoomCalendarSettings?>
}
