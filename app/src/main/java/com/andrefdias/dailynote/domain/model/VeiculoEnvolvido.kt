package com.andrefdias.dailynote.domain.model

data class VeiculoMaster(
    val id: String? = null,
    val placa: String,
    val renavam: String? = null,
    val chassi: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val versao: String? = null,
    val tipo: String? = null,
    val categoria: String? = null,
    val cor: String? = null,
    val anoFabricacao: Int? = null,
    val anoModelo: Int? = null,
    val proprietarioId: String? = null,
    val status: String = "Ativo"
)

data class VeiculoEnvolvido(
    val id: String? = null,
    val ocorrenciaId: String,
    val placa: String = "",                    // Formatado Mercosul
    val modelo: String = "",
    val cor: String = "",
    val chassi: String = "",
    val anoFabricacao: Int? = null,            // mantido para veículos antigos
    val anoModelo: Int? = null,                // mantido para veículos antigos
    val ano: String = "",                      // "2024/2025" - formato exibição
    val proprietarioId: String? = null,        // Referência à Pessoa
    
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
    val categoriaVeiculo: String? = null
)

data class Motorista(
    val nome: String? = null,
    val cnh: String? = null,
    val categoriaCnh: String? = null,
    val dataNascimento: String? = null,
    val telefone: String? = null
)
