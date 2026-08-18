package com.andrefdias.dailynote.domain.model



data class Viatura(
    val id: String = java.util.UUID.randomUUID().toString(),
    val prefixo: String,
    val tipo: String,
    val tipoAtendimento: String,
    val unidade: String,
    val posto: String
)
