package com.andrefdias.dailynote.data.local.entities

import androidx.room.*
import com.andrefdias.dailynote.domain.model.EquipeServico
import com.andrefdias.dailynote.domain.model.EquipeViatura
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.andrefdias.dailynote.domain.model.MilitarEscalado

@Entity(tableName = "equipe_servico")
data class RoomEquipeServico(
    @PrimaryKey val id: String,
    val data: String,
    val unidade: String,
    val posto: String,
    val escalaId: String?,
    val tipoEscala: String,
    val dejemHorarioInicio: String?,
    val dejemHorarioFim: String?
)

@Entity(
    tableName = "equipe_viatura",
    foreignKeys = [
        ForeignKey(
            entity = RoomEquipeServico::class,
            parentColumns = ["id"],
            childColumns = ["equipeServicoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["equipeServicoId"])]
)
data class RoomEquipeViatura(
    @PrimaryKey val id: String,
    val equipeServicoId: String,
    val viaturaId: String,
    val militaresEscaladosJson: String = "[]"
)

data class RoomEquipeServicoWithViaturas(
    @Embedded val equipeServico: RoomEquipeServico,
    @Relation(
        parentColumn = "id",
        entityColumn = "equipeServicoId"
    )
    val viaturas: List<RoomEquipeViatura>
)

fun RoomEquipeServicoWithViaturas.toDomainModel(): EquipeServico {
    val json = Json { ignoreUnknownKeys = true }
    return EquipeServico(
        id = equipeServico.id,
        data = equipeServico.data,
        unidade = equipeServico.unidade,
        posto = equipeServico.posto,
        escalaId = equipeServico.escalaId,
        tipoEscala = equipeServico.tipoEscala,
        dejemHorarioInicio = equipeServico.dejemHorarioInicio,
        dejemHorarioFim = equipeServico.dejemHorarioFim,
        viaturas = viaturas.map {
            EquipeViatura(
                id = it.id,
                equipeServicoId = it.equipeServicoId,
                viaturaId = it.viaturaId,
                militaresEscalados = try { json.decodeFromString(it.militaresEscaladosJson) } catch (e: Exception) { emptyList() }
            )
        }
    )
}

fun EquipeServico.toRoomEntity(): RoomEquipeServico {
    return RoomEquipeServico(
        id = id,
        data = data,
        unidade = unidade,
        posto = posto,
        escalaId = escalaId,
        tipoEscala = tipoEscala,
        dejemHorarioInicio = dejemHorarioInicio,
        dejemHorarioFim = dejemHorarioFim
    )
}

fun EquipeViatura.toRoomEntity(): RoomEquipeViatura {
    return RoomEquipeViatura(
        id = id,
        equipeServicoId = equipeServicoId,
        viaturaId = viaturaId,
        militaresEscaladosJson = Json.encodeToString(militaresEscalados)
    )
}
