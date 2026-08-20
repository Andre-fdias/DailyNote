package com.andrefdias.dailynote.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andrefdias.dailynote.domain.model.Militar

@Entity(tableName = "militares")
data class RoomMilitar(
    @PrimaryKey val id: String,
    val re: String,
    val nomeCompleto: String,
    val nomeGuerra: String,
    val graduacao: String,
    val situacao: String,
    val mergulhador: Boolean = false,
    val ovb: String = "Não Habilitado"
)

fun RoomMilitar.toDomainModel() = Militar(
    id = id,
    re = re,
    nomeCompleto = nomeCompleto,
    nomeGuerra = nomeGuerra,
    graduacao = graduacao,
    situacao = situacao,
    mergulhador = mergulhador,
    ovb = ovb
)

fun Militar.toRoomEntity() = RoomMilitar(
    id = id,
    re = re,
    nomeCompleto = nomeCompleto,
    nomeGuerra = nomeGuerra,
    graduacao = graduacao,
    situacao = situacao,
    mergulhador = mergulhador,
    ovb = ovb
)
