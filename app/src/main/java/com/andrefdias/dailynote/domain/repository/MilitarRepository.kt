package com.andrefdias.dailynote.domain.repository

import com.andrefdias.dailynote.domain.model.Militar
import kotlinx.coroutines.flow.Flow

interface MilitarRepository {
    fun getAll(): Flow<List<Militar>>
    suspend fun getById(id: String): Militar?
    suspend fun insert(militar: Militar)
    suspend fun update(militar: Militar)
    suspend fun delete(militar: Militar)
}
