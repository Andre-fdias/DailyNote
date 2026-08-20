package com.andrefdias.dailynote.data.local.dao

import androidx.room.*
import com.andrefdias.dailynote.data.local.entities.RoomMilitar
import kotlinx.coroutines.flow.Flow

@Dao
interface MilitarDao {
    @Query("SELECT * FROM militares ORDER BY nomeGuerra")
    fun getAll(): Flow<List<RoomMilitar>>

    @Query("SELECT * FROM militares WHERE id = :id")
    suspend fun getById(id: String): RoomMilitar?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(militar: RoomMilitar)

    @Update
    suspend fun update(militar: RoomMilitar)

    @Delete
    suspend fun delete(militar: RoomMilitar)
}
