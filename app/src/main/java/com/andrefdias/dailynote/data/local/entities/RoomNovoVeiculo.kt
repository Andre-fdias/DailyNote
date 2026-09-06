package com.andrefdias.dailynote.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "novo_veiculos_envolvidos")
data class RoomNovoVeiculo(
    @PrimaryKey val id: String,
    val ocorrenciaId: String,
    val placa: String,
    val modelo: String,
    val cor: String,
    val chassi: String,
    val anoFabricacao: Int?,
    val anoModelo: Int?,
    val ano: String,
    val proprietarioId: String?,
    val marca: String,
    val versao: String,
    val exercicio: String,
    val urlCrlv: String?,
    val ocrTextoCrlv: String?,
    val ocrDadosEstruturadosJson: String,
    val veiculoMasterId: String?,
    val condutorId: String?,
    val dadosMotoristaJson: String?,
    val renavam: String?,
    val monobloco: String?,
    val especie: String?,
    val tipoVeiculo: String?,
    val carroceria: String?,
    val categoriaVeiculo: String?,
    val fotosVeiculoUrisJson: String = "[]"
)
