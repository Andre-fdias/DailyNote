package com.andrefdias.dailynote.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nova_ocorrencias")
data class RoomNovaOcorrencia(
    @PrimaryKey val id: String,
    val talao: String,
    val data: String, // dd/MM/yyyy
    val hora: String, // HH:mm
    val equipe: String, // Azul, Verde, etc.
    val natureza: String, // A 05 - Agressão
    val viatura: String,
    val guarnicaoJson: String, // JSON array of military personnel names
    val latitude: Double?,
    val longitude: Double?,
    val rua: String?,
    val numero: String?,
    val bairro: String?,
    val cidade: String?,
    val fotosUrisJson: String, // JSON array of picture URIs
    val pessoasJson: String = "[]", // JSON array of people
    val historico: String? = null,
    val apoios: String = "[]", // JSON array of apoios
    val isConcluida: Boolean = false,
    @androidx.room.ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
)
