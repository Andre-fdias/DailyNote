package com.andrefdias.dailynote.data.repository

import com.andrefdias.dailynote.data.remote.OcorrenciasApi
import com.andrefdias.dailynote.domain.model.OcorrenciaResponse
import com.andrefdias.dailynote.domain.repository.OcorrenciaRepository
import javax.inject.Inject

class OcorrenciaRepositoryImpl @Inject constructor(
    private val api: OcorrenciasApi
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
            val response = api.getOcorrencias(
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
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
