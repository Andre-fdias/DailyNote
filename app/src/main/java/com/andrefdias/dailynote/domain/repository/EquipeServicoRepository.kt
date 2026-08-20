package com.andrefdias.dailynote.domain.repository

import com.andrefdias.dailynote.domain.model.EquipeServico
import kotlinx.coroutines.flow.Flow

interface EquipeServicoRepository {
    fun getAllEquipesServico(): Flow<List<EquipeServico>>
    fun getEquipeServico(data: String, unidade: String, posto: String): Flow<EquipeServico?>
    suspend fun saveEquipeServico(equipeServico: EquipeServico)
    suspend fun deleteEquipeServico(id: String)
}
