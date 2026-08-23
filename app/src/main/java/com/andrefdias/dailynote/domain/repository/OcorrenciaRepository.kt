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
}
