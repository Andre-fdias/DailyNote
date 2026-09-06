package com.andrefdias.dailynote.data.repository

import com.andrefdias.dailynote.data.remote.OcorrenciasApi
import com.andrefdias.dailynote.domain.model.OcorrenciaResponse
import com.andrefdias.dailynote.domain.repository.OcorrenciaRepository
import javax.inject.Inject

import com.andrefdias.dailynote.data.local.dao.OcorrenciaDao
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class OcorrenciaRepositoryImpl @Inject constructor(
    private val api: OcorrenciasApi,
    private val dao: OcorrenciaDao
) : OcorrenciaRepository {

    override suspend fun getOcorrencias(
        dataInicio: String?,
        dataFim: String?,
        data: String?,
        re: String?,
        militar: String?,
        vtr: String?,
        talao: String?,
        limite: Int?,
        pagina: Int?
    ): Result<OcorrenciaResponse> {
        return try {
            val apiResponse = try {
                api.getOcorrencias(
                    dataInicio = dataInicio,
                    dataFim = dataFim,
                    data = data,
                    re = re,
                    militar = militar,
                    vtr = vtr,
                    talao = talao,
                    limite = limite,
                    pagina = pagina
                )
            } catch (e: Exception) {
                // Se falhar a API, retorna vazio para juntar com o local
                OcorrenciaResponse(
                    sucesso = false,
                    total = 0,
                    pagina = 1,
                    limite = 100,
                    totalPaginas = 1,
                    filtros = null,
                    ocorrencias = emptyList()
                )
            }

            // Apenas retorna os dados da API (Google Sheets)
            Result.success(apiResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createOcorrenciaLocal(ocorrencia: RoomNovaOcorrencia) {
        dao.insertOcorrencia(ocorrencia)
    }

    override suspend fun updateOcorrenciaLocal(ocorrencia: RoomNovaOcorrencia) {
        dao.updateOcorrencia(ocorrencia)
    }

    override suspend fun deleteOcorrenciaLocal(ocorrencia: RoomNovaOcorrencia) {
        dao.deleteOcorrencia(ocorrencia.id)
    }
    
    override fun getAllLocalOcorrenciasFlow(): Flow<List<RoomNovaOcorrencia>> {
        return dao.getAllOcorrenciasFlow()
    }
    
    override suspend fun insertVeiculoEnvolvido(veiculo: com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo) {
        dao.insertVeiculoEnvolvido(veiculo)
    }

    override suspend fun updateVeiculoEnvolvido(veiculo: com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo) {
        dao.updateVeiculoEnvolvido(veiculo)
    }

    override suspend fun deleteVeiculoEnvolvido(veiculo: com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo) {
        dao.deleteVeiculoEnvolvido(veiculo)
    }

    override fun getVeiculosByOcorrenciaIdFlow(ocorrenciaId: String): Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo>> {
        return dao.getVeiculosByOcorrenciaIdFlow(ocorrenciaId)
    }

    override fun getAllVeiculosFlow(): Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo>> {
        return dao.getAllVeiculosFlow()
    }

    override suspend fun insertVitima(vitima: com.andrefdias.dailynote.data.local.entities.RoomNovaVitima) {
        dao.insertVitima(vitima)
    }

    override suspend fun updateVitima(vitima: com.andrefdias.dailynote.data.local.entities.RoomNovaVitima) {
        dao.updateVitima(vitima)
    }

    override suspend fun deleteVitima(vitima: com.andrefdias.dailynote.data.local.entities.RoomNovaVitima) {
        dao.deleteVitima(vitima)
    }

    override fun getVitimasByOcorrenciaIdFlow(ocorrenciaId: String): Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovaVitima>> {
        return dao.getVitimasByOcorrenciaIdFlow(ocorrenciaId)
    }

    override fun getAllVitimasFlow(): Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovaVitima>> {
        return dao.getAllVitimasFlow()
    }


    override fun getAllLegacyOcorrenciasFlow(): Flow<List<com.andrefdias.dailynote.data.local.entities.RoomOcorrencia>> {
        return dao.getAllLegacyOcorrenciasFlow()
    }
}