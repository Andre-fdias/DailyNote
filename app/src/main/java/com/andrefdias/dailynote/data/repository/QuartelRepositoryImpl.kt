package com.andrefdias.dailynote.data.repository

import com.andrefdias.dailynote.data.local.dao.QuartelDao
import com.andrefdias.dailynote.data.local.entities.toDomainModel
import com.andrefdias.dailynote.data.local.entities.toRoomEntity
import com.andrefdias.dailynote.domain.model.Quartel
import com.andrefdias.dailynote.domain.repository.QuartelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuartelRepositoryImpl @Inject constructor(
    private val dao: QuartelDao
) : QuartelRepository {

    override fun getAll(): Flow<List<Quartel>> {
        return dao.getAll().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getById(id: String): Quartel? {
        return dao.getById(id)?.toDomainModel()
    }

    override fun getUnidades(): Flow<List<String>> {
        return dao.getUnidades()
    }

    override fun getPostosByUnidade(unidade: String): Flow<List<String>> {
        return dao.getPostosByUnidade(unidade)
    }

    override suspend fun insert(quartel: Quartel) {
        dao.insert(quartel.toRoomEntity())
    }

    override suspend fun update(quartel: Quartel) {
        dao.update(quartel.toRoomEntity())
    }

    override suspend fun delete(quartel: Quartel) {
        dao.delete(quartel.toRoomEntity())
    }
}
