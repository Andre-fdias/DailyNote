package com.andrefdias.dailynote.data.remote

import com.andrefdias.dailynote.domain.model.OcorrenciaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OcorrenciasApi {
    @GET("exec")
    suspend fun getOcorrencias(
        @Query("token") token: String = "davidesouzamartins",
        @Query("inicio") dataInicio: String? = null,
        @Query("fim") dataFim: String? = null,
        @Query("data") data: String? = null,
        @Query("re") re: String? = null,
        @Query("militar") militar: String? = null,
        @Query("vtr") vtr: String? = null,
        @Query("talao") talao: String? = null,
        @Query("limite") limite: Int? = 500,
        @Query("pagina") pagina: Int? = 1
    ): OcorrenciaResponse

    @GET("exec")
    suspend fun healthCheck(
        @Query("rota") rota: String = "health"
    ): Any // You might want a specific model for this if it returns JSON
}
