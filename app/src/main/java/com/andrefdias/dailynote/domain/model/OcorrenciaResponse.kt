package com.andrefdias.dailynote.domain.model

import com.google.gson.annotations.SerializedName

data class OcorrenciaResponse(
    val sucesso: Boolean,
    val total: Int,
    val pagina: Int,
    val limite: Int,
    @SerializedName("total_paginas") val totalPaginas: Int,
    val filtros: Map<String, String>?,
    val ocorrencias: List<Ocorrencia>
)
