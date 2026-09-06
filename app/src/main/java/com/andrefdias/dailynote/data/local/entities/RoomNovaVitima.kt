package com.andrefdias.dailynote.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nova_vitimas")
data class RoomNovaVitima(
    @PrimaryKey val id: String,
    val ocorrenciaId: String,
    val nome: String,
    val idade: Int?,
    val pessoaId: String?,
    val lesoes: String,
    val lesoesEstruturadasJson: String, // Serialize List<Lesao>
    val destinoSocorro: String,
    val quemSocorreu: String,
    val resultadoOcorrencia: String,
    val viaturaSocorroId: String?,
    val hospitalDestino: String,
    val nomeMedico: String,
    val crmMedico: String,
    val sinaisVitaisJson: String, // Serialize SinaisVitais
    val cpf: String?,
    val lesoesAparentes: String?,
    val transportadoPor: String?
)
