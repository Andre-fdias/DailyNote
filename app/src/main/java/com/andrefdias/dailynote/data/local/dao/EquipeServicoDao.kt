package com.andrefdias.dailynote.data.local.dao

import androidx.room.*
import com.andrefdias.dailynote.data.local.entities.RoomEquipeServico
import com.andrefdias.dailynote.data.local.entities.RoomEquipeServicoWithViaturas
import com.andrefdias.dailynote.data.local.entities.RoomEquipeViatura
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipeServicoDao {
    
    @Transaction
    @Query("SELECT * FROM equipe_servico ORDER BY data DESC")
    fun getAllEquipesServico(): Flow<List<RoomEquipeServicoWithViaturas>>

    @Transaction
    @Query("SELECT * FROM equipe_servico WHERE data = :data AND unidade = :unidade AND posto = :posto LIMIT 1")
    fun getEquipeServico(data: String, unidade: String, posto: String): Flow<RoomEquipeServicoWithViaturas?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipeServico(equipeServico: RoomEquipeServico)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViaturas(viaturas: List<RoomEquipeViatura>)

    @Query("DELETE FROM equipe_viatura WHERE equipeServicoId = :equipeServicoId")
    suspend fun deleteViaturasByEquipeId(equipeServicoId: String)

    @Transaction
    suspend fun saveEquipeCompleta(
        equipeServico: RoomEquipeServico,
        viaturas: List<RoomEquipeViatura>
    ) {
        insertEquipeServico(equipeServico)
        deleteViaturasByEquipeId(equipeServico.id)
        if (viaturas.isNotEmpty()) {
            insertViaturas(viaturas)
        }
    }
}
