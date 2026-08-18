package com.andrefdias.dailynote.data.repository

import com.andrefdias.dailynote.data.local.dao.EquipeServicoDao
import com.andrefdias.dailynote.data.local.entities.toDomainModel
import com.andrefdias.dailynote.data.local.entities.toRoomEntity
import com.andrefdias.dailynote.domain.model.EquipeServico
import com.andrefdias.dailynote.domain.repository.EquipeServicoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EquipeServicoRepositoryImpl @Inject constructor(
    private val dao: EquipeServicoDao
) : EquipeServicoRepository {

    override fun getAllEquipesServico(): Flow<List<EquipeServico>> {
        return dao.getAllEquipesServico().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getEquipeServico(data: String, unidade: String, posto: String): Flow<EquipeServico?> {
        return dao.getEquipeServico(data, unidade, posto).map { it?.toDomainModel() }
    }

    override suspend fun saveEquipeServico(equipeServico: EquipeServico) {
        val roomEquipe = equipeServico.toRoomEntity()
        val roomViaturas = equipeServico.viaturas.map { it.toRoomEntity() }
        dao.saveEquipeCompleta(roomEquipe, roomViaturas)
    }
}
