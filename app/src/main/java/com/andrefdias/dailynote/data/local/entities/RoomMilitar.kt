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
    val situacao: String
)

fun RoomMilitar.toDomainModel() = Militar(
    id = id,
    re = re,
    nomeCompleto = nomeCompleto,
    nomeGuerra = nomeGuerra,
    graduacao = graduacao,
    situacao = situacao
)

fun Militar.toRoomEntity() = RoomMilitar(
    id = id,
    re = re,
    nomeCompleto = nomeCompleto,
    nomeGuerra = nomeGuerra,
    graduacao = graduacao,
    situacao = situacao
)
