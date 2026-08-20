package com.andrefdias.dailynote.domain.model

data class TimelineEvent(
    val id: String? = null,
    val ocorrenciaId: String,
    val evento: String, // Despacho, Chegada, Atendimento, Apoio, Hospital, Encerramento
    val descricao: String?,
    val dataHora: String
)
