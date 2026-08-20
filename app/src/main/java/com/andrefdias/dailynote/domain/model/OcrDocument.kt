package com.andrefdias.dailynote.domain.model

sealed class OcrDocument {
    abstract val tipo: String
    abstract val confianca: Map<String, Int>
    abstract val textoOriginal: String
    
    data class RG(
        override val tipo: String = "RG",
        val nome: String?,
        val rg: String?,
        val cpf: String?,
        val dataNascimento: String?,
        val filiacao: String?,
        override val confianca: Map<String, Int> = emptyMap(),
        override val textoOriginal: String = ""
    ) : OcrDocument()
    
    data class CIN(
        override val tipo: String = "CIN",
        val cpf: String?,
        val nome: String?,
        val dataNascimento: String?,
        val filiacao: String?,
        override val confianca: Map<String, Int> = emptyMap(),
        override val textoOriginal: String = ""
    ) : OcrDocument()
    
    data class CNH(
        override val tipo: String = "CNH",
        val nome: String?,
        val cpf: String?,
        val rg: String?,
        val registro: String?,
        val categorias: String?,
        val dataNascimento: String?,
        val filiacao: String?,
        val validade: String?,
        override val confianca: Map<String, Int> = emptyMap(),
        override val textoOriginal: String = ""
    ) : OcrDocument()
    
    data class CPF(
        override val tipo: String = "CPF",
        val nome: String?,
        val cpf: String?,
        val dataNascimento: String?,
        override val confianca: Map<String, Int> = emptyMap(),
        override val textoOriginal: String = ""
    ) : OcrDocument()
    
    data class CRLV(
        override val tipo: String = "CRLV",
        val placa: String?,
        val anoFabricacao: String?,
        val anoModelo: String?,
        val marca: String?,
        val modelo: String?,
        val versao: String?,
        val cor: String?,
        val motor: String?,
        val proprietarioNome: String?,
        val proprietarioCpf: String?,
        override val confianca: Map<String, Int> = emptyMap(),
        override val textoOriginal: String = ""
    ) : OcrDocument()
    
    data class OAB(
        override val tipo: String = "OAB",
        val nome: String?,
        val numero: String?,
        val uf: String?,
        override val confianca: Map<String, Int> = emptyMap(),
        override val textoOriginal: String = ""
    ) : OcrDocument()
}
