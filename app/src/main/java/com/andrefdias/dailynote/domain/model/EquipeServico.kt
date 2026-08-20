package com.andrefdias.dailynote.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MilitarEscalado(
    val id: String = java.util.UUID.randomUUID().toString(),
    val militarId: String,
    val funcao: String, // e.g. "Comandante", "Motorista", "Auxiliar", "Telegrafista"
    val tipoEscala: String, // "DEJEM", "ESCALA", or specific team name
    val escalaId: String? = null,
    val dejemHorarioInicio: String? = null,
    val dejemHorarioFim: String? = null,
    // Transient objects for UI
    @kotlinx.serialization.Transient val militar: Militar? = null,
    @kotlinx.serialization.Transient val equipeConfig: EquipeConfig? = null
)

data class EquipeServico(
    val id: String = java.util.UUID.randomUUID().toString(),
    val data: String, // YYYY-MM-DD
    val unidade: String,
    val posto: String,
    val escalaId: String? = null,
    val tipoEscala: String = "DEJEM", // Global scale of the post
    val dejemHorarioInicio: String? = null,
    val dejemHorarioFim: String? = null,
    val viaturas: List<EquipeViatura> = emptyList(),
    // Transient field for UI
    val equipeConfig: EquipeConfig? = null
)

data class EquipeViatura(
    val id: String = java.util.UUID.randomUUID().toString(),
    val equipeServicoId: String,
    val viaturaId: String,
    val militaresEscalados: List<MilitarEscalado> = emptyList(),
    // These objects can be populated in the UI/ViewModel for display
    val viatura: Viatura? = null
)
