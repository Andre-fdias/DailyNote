package com.andrefdias.dailynote.domain.repository

import com.andrefdias.dailynote.domain.model.Quartel
import kotlinx.coroutines.flow.Flow

interface QuartelRepository {
    fun getAll(): Flow<List<Quartel>>
    suspend fun getById(id: String): Quartel?
    fun getUnidades(): Flow<List<String>>
    fun getPostosByUnidade(unidade: String): Flow<List<String>>
    suspend fun insert(quartel: Quartel)
    suspend fun update(quartel: Quartel)
    suspend fun delete(quartel: Quartel)
}
