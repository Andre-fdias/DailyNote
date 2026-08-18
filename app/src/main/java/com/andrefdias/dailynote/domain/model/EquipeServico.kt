package com.andrefdias.dailynote.domain.model

data class EquipeServico(
    val id: String = java.util.UUID.randomUUID().toString(),
    val data: String, // YYYY-MM-DD
    val unidade: String,
    val posto: String,
    val escalaId: String? = null,
    val tipoEscala: String = "24h", // "24h", "12h", "DEJEM"
    val dejemHorarioInicio: String? = null,
    val dejemHorarioFim: String? = null,
    val viaturas: List<EquipeViatura> = emptyList()
)

data class EquipeViatura(
    val id: String = java.util.UUID.randomUUID().toString(),
    val equipeServicoId: String,
    val viaturaId: String,
    val motoristaId: String? = null,
    val comandanteId: String? = null,
    val auxiliaresIds: List<String> = emptyList(),
    // These objects can be populated in the UI/ViewModel for display
    val viatura: Viatura? = null,
    val motorista: Militar? = null,
    val comandante: Militar? = null,
    val auxiliares: List<Militar> = emptyList()
)
