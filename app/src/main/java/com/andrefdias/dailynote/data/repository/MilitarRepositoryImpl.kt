package com.andrefdias.dailynote.data.repository

import com.andrefdias.dailynote.data.local.dao.MilitarDao
import com.andrefdias.dailynote.data.local.entities.toDomainModel
import com.andrefdias.dailynote.data.local.entities.toRoomEntity
import com.andrefdias.dailynote.domain.model.Militar
import com.andrefdias.dailynote.domain.repository.MilitarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MilitarRepositoryImpl @Inject constructor(
    private val dao: MilitarDao
) : MilitarRepository {
    override fun getAll(): Flow<List<Militar>> {
        return dao.getAll().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getById(id: String): Militar? {
        return dao.getById(id)?.toDomainModel()
    }

    override suspend fun insert(militar: Militar) {
        dao.insert(militar.toRoomEntity())
    }

    override suspend fun update(militar: Militar) {
        dao.update(militar.toRoomEntity())
    }

    override suspend fun delete(militar: Militar) {
        dao.delete(militar.toRoomEntity())
    }
}
