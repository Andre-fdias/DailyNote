package com.andrefdias.dailynote.domain.model

data class AvaliacaoClinica(
    val id: String? = null,
    val vitimaId: String,
    val glasgow: Int?,
    val pressao: String?,
    val frequenciaCardiaca: Int?,
    val frequenciaRespiratoria: Int?,
    val temperatura: Double?,
    val oximetria: Int?,
    val lesoes: String?,
    val hospitalDestino: String?,
    val viaturaSocorroId: String?,
    val resultado: String?
)
