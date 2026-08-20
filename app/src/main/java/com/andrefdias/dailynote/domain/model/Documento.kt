package com.andrefdias.dailynote.domain.model

data class Documento(
    val id: String? = null,
    val ocorrenciaId: String,
    val pessoaId: String? = null,
    val tipo: String, // CNH, CIN, RG, OAB, etc.
    val numero: String? = null,
    val urlImagem: String? = null,
    val textoOcr: String? = null,
    val dadosEstruturados: Map<String, String> = emptyMap(),
    val hashArquivo: String? = null,
    val dataUpload: String? = null,
    val usuario: String? = null
)
