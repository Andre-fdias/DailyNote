package com.andrefdias.dailynote.domain.model

data class Quartel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val unidade: String,
    val posto: String,
    val municipio: String = ""
)
