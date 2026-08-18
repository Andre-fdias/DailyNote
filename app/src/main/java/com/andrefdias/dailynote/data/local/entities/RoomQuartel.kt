package com.andrefdias.dailynote.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andrefdias.dailynote.domain.model.Quartel

@Entity(tableName = "quartel")
data class RoomQuartel(
    @PrimaryKey
    val id: String,
    val unidade: String,
    val posto: String
)

fun RoomQuartel.toDomainModel() = Quartel(
    id = id,
    unidade = unidade,
    posto = posto
)

fun Quartel.toRoomEntity() = RoomQuartel(
    id = id,
    unidade = unidade,
    posto = posto
)
