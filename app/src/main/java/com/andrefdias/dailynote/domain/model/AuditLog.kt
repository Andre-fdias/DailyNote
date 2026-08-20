package com.andrefdias.dailynote.domain.model

data class AuditLog(
    val id: String? = null,
    val ocorrenciaId: String?,
    val usuario: String,
    val dataHora: String,
    val tabelaAlterada: String,
    val campoAlterado: String,
    val valorAnterior: String?,
    val valorNovo: String?
)
