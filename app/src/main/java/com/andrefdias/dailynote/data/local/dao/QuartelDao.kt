package com.andrefdias.dailynote.data.local.dao

import androidx.room.*
import com.andrefdias.dailynote.data.local.entities.RoomQuartel
import kotlinx.coroutines.flow.Flow

@Dao
interface QuartelDao {
    @Query("SELECT * FROM quartel ORDER BY unidade, posto")
    fun getAll(): Flow<List<RoomQuartel>>

    @Query("SELECT * FROM quartel WHERE id = :id")
    suspend fun getById(id: String): RoomQuartel?

    @Query("SELECT DISTINCT unidade FROM quartel ORDER BY unidade")
    fun getUnidades(): Flow<List<String>>

    @Query("SELECT posto FROM quartel WHERE unidade = :unidade ORDER BY posto")
    fun getPostosByUnidade(unidade: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quartel: RoomQuartel)

    @Update
    suspend fun update(quartel: RoomQuartel)

    @Delete
    suspend fun delete(quartel: RoomQuartel)
}
