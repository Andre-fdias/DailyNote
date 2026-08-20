package com.andrefdias.dailynote.data.service.ocr

object DocumentClassifier {
    fun classify(text: String): String {
        val upperText = text.uppercase()
        return when {
            upperText.contains("RENAVAM") || upperText.contains("CRLV") || upperText.contains("LICENCIAMENTO") || upperText.contains("CERTIFICADO DE REGISTRO E LICENCIAMENTO") -> "CRLV"
            upperText.contains("CNH") || upperText.contains("HABILITAÇÃO") || upperText.contains("CARTEIRA NACIONAL DE HABILITACAO") -> "CNH"
            upperText.contains("CIN") || upperText.contains("IDENTIDADE NACIONAL") || upperText.contains("CARTEIRA DE IDENTIDADE NACIONAL") -> "CIN"
            upperText.contains("RG") || upperText.contains("REGISTRO GERAL") || upperText.contains("SSP/") || upperText.contains("SECRETARIA DE SEGURANCA") -> "RG"
            upperText.contains("CPF") || upperText.contains("CADASTRO DE PESSOAS FISICAS") -> "CPF"
            upperText.contains("OAB") || upperText.contains("ADVOGADO") || upperText.contains("ORDEM DOS ADVOGADOS") -> "OAB"
            else -> "OUTROS"
        }
    }
}
