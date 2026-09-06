package com.andrefdias.dailynote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import kotlinx.coroutines.flow.Flow

@Dao
interface OcorrenciaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOcorrencia(ocorrencia: RoomNovaOcorrencia)

    @Update
    suspend fun updateOcorrencia(ocorrencia: RoomNovaOcorrencia)

    @Query("SELECT * FROM nova_ocorrencias ORDER BY createdAt DESC")
    fun getAllOcorrenciasFlow(): Flow<List<RoomNovaOcorrencia>>

    @Query("SELECT * FROM nova_ocorrencias WHERE id = :id")
    fun getOcorrenciaByIdFlow(id: String): Flow<RoomNovaOcorrencia?>

    // ==================
    // Vítimas
    // ==================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitima(vitima: com.andrefdias.dailynote.data.local.entities.RoomNovaVitima)

    @Update
    suspend fun updateVitima(vitima: com.andrefdias.dailynote.data.local.entities.RoomNovaVitima)

    @Delete
    suspend fun deleteVitima(vitima: com.andrefdias.dailynote.data.local.entities.RoomNovaVitima)

    @Query("SELECT * FROM nova_vitimas WHERE ocorrenciaId = :ocorrenciaId")
    fun getVitimasByOcorrenciaIdFlow(ocorrenciaId: String): Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovaVitima>>

    @Query("SELECT * FROM nova_vitimas")
    fun getAllVitimasFlow(): Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovaVitima>>
    
    // ==================
    // Veículos Envolvidos
    // ==================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVeiculoEnvolvido(veiculo: com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo)

    @Update
    suspend fun updateVeiculoEnvolvido(veiculo: com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo)

    @Delete
    suspend fun deleteVeiculoEnvolvido(veiculo: com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo)

    @Query("SELECT * FROM novo_veiculos_envolvidos")
    fun getAllVeiculosFlow(): Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo>>

    @Query("SELECT * FROM novo_veiculos_envolvidos WHERE ocorrenciaId = :ocorrenciaId")
    fun getVeiculosByOcorrenciaIdFlow(ocorrenciaId: String): Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo>>

    @Query("DELETE FROM nova_ocorrencias WHERE id = :id")
    suspend fun deleteOcorrencia(id: String)

    @Query("SELECT * FROM ocorrencias ORDER BY dataHora DESC")
    fun getAllLegacyOcorrenciasFlow(): Flow<List<com.andrefdias.dailynote.data.local.entities.RoomOcorrencia>>
}
