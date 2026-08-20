package com.andrefdias.dailynote.data.repository

import com.andrefdias.dailynote.data.local.dao.ViaturaDao
import com.andrefdias.dailynote.data.local.entities.toDomainModel
import com.andrefdias.dailynote.data.local.entities.toRoomEntity
import com.andrefdias.dailynote.domain.model.Viatura
import com.andrefdias.dailynote.domain.repository.ViaturaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ViaturaRepositoryImpl @Inject constructor(
    private val dao: ViaturaDao
) : ViaturaRepository {
    override fun getAll(): Flow<List<Viatura>> {
        return dao.getAll().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getById(id: String): Viatura? {
        return dao.getById(id)?.toDomainModel()
    }

    override fun getByLocal(unidade: String, posto: String): Flow<List<Viatura>> {
        return dao.getByLocal(unidade, posto).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun insert(viatura: Viatura) {
        dao.insert(viatura.toRoomEntity())
    }

    override suspend fun update(viatura: Viatura) {
        dao.update(viatura.toRoomEntity())
    }

    override suspend fun delete(viatura: Viatura) {
        dao.delete(viatura.toRoomEntity())
    }
}
