package com.andrefdias.dailynote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andrefdias.dailynote.data.local.entities.RoomConfiguracao
import com.andrefdias.dailynote.data.local.entities.RoomBackupLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracaoDao {

    @Query("SELECT * FROM configuracoes WHERE id = 'global_config' LIMIT 1")
    fun getConfiguracaoFlow(): Flow<RoomConfiguracao?>

    @Query("SELECT * FROM configuracoes WHERE id = 'global_config' LIMIT 1")
    suspend fun getConfiguracao(): RoomConfiguracao?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguracao(configuracao: RoomConfiguracao)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackupLog(log: RoomBackupLog)

    @Query("SELECT * FROM backup_log ORDER BY dataHora DESC")
    fun getBackupLogsFlow(): Flow<List<RoomBackupLog>>
}
