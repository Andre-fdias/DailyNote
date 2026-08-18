package com.andrefdias.dailynote.domain.model

enum class DocumentType(val displayName: String, val icon: String, val description: String) {
    RG("RG", "📄", "Registro Geral - Identidade tradicional estadual"),
    CIN("CIN", "🆔", "Carteira de Identidade Nacional - Novo modelo único"),
    CNH("CNH", "🚗", "Carteira Nacional de Habilitação - Habilitação de trânsito"),
    CPF("CPF", "🪪", "Cadastro de Pessoa Física - Registro individual da Receita"),
    CRLV("CRLV", "🚘", "Registro do Veículo - Certificado de licenciamento"),
    OAB("OAB", "⚖", "Ordem dos Advogados do Brasil - Registro profissional")
}
