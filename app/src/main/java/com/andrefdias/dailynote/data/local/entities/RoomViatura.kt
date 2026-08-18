package com.andrefdias.dailynote.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andrefdias.dailynote.domain.model.Viatura

@Entity(tableName = "viaturas")
data class RoomViatura(
    @PrimaryKey val id: String,
    val prefixo: String,
    val tipo: String,
    val tipoAtendimento: String,
    val unidade: String,
    val posto: String
)

fun RoomViatura.toDomainModel() = Viatura(
    id = id,
    prefixo = prefixo,
    tipo = tipo,
    tipoAtendimento = tipoAtendimento,
    unidade = unidade,
    posto = posto
)

fun Viatura.toRoomEntity() = RoomViatura(
    id = id,
    prefixo = prefixo,
    tipo = tipo,
    tipoAtendimento = tipoAtendimento,
    unidade = unidade,
    posto = posto
)
