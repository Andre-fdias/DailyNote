package com.andrefdias.dailynote.domain.repository

import com.andrefdias.dailynote.domain.model.Viatura
import kotlinx.coroutines.flow.Flow

interface ViaturaRepository {
    fun getAll(): Flow<List<Viatura>>
    suspend fun getById(id: String): Viatura?
    fun getByLocal(unidade: String, posto: String): Flow<List<Viatura>>
    suspend fun insert(viatura: Viatura)
    suspend fun update(viatura: Viatura)
    suspend fun delete(viatura: Viatura)
}
