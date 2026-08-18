package com.andrefdias.dailynote.data.local.dao

import androidx.room.*
import com.andrefdias.dailynote.data.local.entities.RoomViatura
import kotlinx.coroutines.flow.Flow

@Dao
interface ViaturaDao {
    @Query("SELECT * FROM viaturas ORDER BY prefixo")
    fun getAll(): Flow<List<RoomViatura>>

    @Query("SELECT * FROM viaturas WHERE id = :id")
    suspend fun getById(id: String): RoomViatura?

    @Query("SELECT * FROM viaturas WHERE unidade = :unidade AND posto = :posto ORDER BY prefixo")
    fun getByLocal(unidade: String, posto: String): Flow<List<RoomViatura>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(viatura: RoomViatura)

    @Update
    suspend fun update(viatura: RoomViatura)

    @Delete
    suspend fun delete(viatura: RoomViatura)
}
