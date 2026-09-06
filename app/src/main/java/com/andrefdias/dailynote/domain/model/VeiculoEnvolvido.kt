package com.andrefdias.dailynote.domain.model

data class VeiculoEnvolvido(
    val id: String? = null,
    val ocorrenciaId: String,
    val placa: String = "",
    val modelo: String = "",
    val cor: String = "",
    val chassi: String = "",
    val anoFabricacao: Int? = null,
    val anoModelo: Int? = null,
    val ano: String = "",
    val proprietarioId: String? = null,
    
    // Campos OCR do CRLV (mantidos para referência)
    val marca: String = "",
    val versao: String = "",
    val exercicio: String = "",
    val urlCrlv: String? = null,
    val ocrTextoCrlv: String? = null,
    val ocrDadosEstruturados: Map<String, String> = emptyMap(),

    // Database compatibility fields
    val veiculoMasterId: String? = null,
    val condutorId: String? = null,
    val dadosMotorista: Motorista? = null,
    val renavam: String? = null,
    val monobloco: String? = null,
    val especie: String? = null,
    val tipoVeiculo: String? = null,
    val carroceria: String? = null,
    val categoriaVeiculo: String? = null,
    val fotosVeiculoUris: List<String> = emptyList()
)

data class Motorista(
    val nome: String? = null,
    val cnh: String? = null,
    val categoriaCnh: String? = null,
    val dataNascimento: String? = null,
    val telefone: String? = null
)
