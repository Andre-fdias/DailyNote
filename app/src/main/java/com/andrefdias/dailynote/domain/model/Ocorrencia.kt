package com.andrefdias.dailynote.domain.model

import com.google.gson.annotations.SerializedName

data class Ocorrencia(
    val data: String, // dd/mm/yyyy
    val prontidao: String,
    val talao: String,
    val vtr: String,
    @SerializedName("qtr_saida") val qtrSaida: String, // HH:MM
    @SerializedName("cmt_vtr") val cmtVtr: String,
    val re: String?, // 6 dígitos
    val natureza: String,
    val vitimas: Int = 0,
    @SerializedName("vitimas_fatais") val vitimasFatais: Int = 0,
    val endereco: String,
    val cidade: String,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    // Helper para gerar id unico localmente caso precise iterar no compose
    val id: String get() = talao + vtr + data + qtrSaida
}

data class OcorrenciaComMilitares(
    val ocorrencia: Ocorrencia,
    val militares: List<Militar>
)
