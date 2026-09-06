package com.andrefdias.dailynote.domain.repository

import com.andrefdias.dailynote.domain.model.OcorrenciaResponse

interface OcorrenciaRepository {
    suspend fun getOcorrencias(
        dataInicio: String? = null,
        dataFim: String? = null,
        data: String? = null,
        re: String? = null,
        militar: String? = null,
        vtr: String? = null,
        talao: String? = null,
        limite: Int? = 500,
        pagina: Int? = 1
    ): Result<OcorrenciaResponse>

    suspend fun createOcorrenciaLocal(ocorrencia: com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia)
    suspend fun updateOcorrenciaLocal(ocorrencia: com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia)
    suspend fun deleteOcorrenciaLocal(ocorrencia: com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia)
    fun getAllLocalOcorrenciasFlow(): kotlinx.coroutines.flow.Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia>>
    fun getAllLegacyOcorrenciasFlow(): kotlinx.coroutines.flow.Flow<List<com.andrefdias.dailynote.data.local.entities.RoomOcorrencia>>
    
    // Veículos
    suspend fun insertVeiculoEnvolvido(veiculo: com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo)
    suspend fun updateVeiculoEnvolvido(veiculo: com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo)
    suspend fun deleteVeiculoEnvolvido(veiculo: com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo)
    fun getVeiculosByOcorrenciaIdFlow(ocorrenciaId: String): kotlinx.coroutines.flow.Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo>>
    fun getAllVeiculosFlow(): kotlinx.coroutines.flow.Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo>>
    
    // Vítimas
    suspend fun insertVitima(vitima: com.andrefdias.dailynote.data.local.entities.RoomNovaVitima)
    suspend fun updateVitima(vitima: com.andrefdias.dailynote.data.local.entities.RoomNovaVitima)
    suspend fun deleteVitima(vitima: com.andrefdias.dailynote.data.local.entities.RoomNovaVitima)
    fun getVitimasByOcorrenciaIdFlow(ocorrenciaId: String): kotlinx.coroutines.flow.Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovaVitima>>
    fun getAllVitimasFlow(): kotlinx.coroutines.flow.Flow<List<com.andrefdias.dailynote.data.local.entities.RoomNovaVitima>>
}
