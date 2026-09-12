package com.andrefdias.dailynote.domain.model

// ============================================
// LESÕES CORPORAIS ESTRUTURADAS
// ============================================

enum class RegiaoCorporal(val label: String, val emoji: String) {
    CABECA_FRENTE("Cabeça (Frente)", "🧠"),
    CABECA_DORSO("Cabeça (Dorso)", "🧠"),
    TORAX_FRENTE("Tórax (Frente)", "🫁"),
    TORAX_DORSO("Tórax (Dorso)", "🫁"),
    ABDOME_FRENTE("Abdome (Frente)", "🫃"),
    ABDOME_DORSO("Abdome (Dorso)", "🫃"),
    MEMBRO_SUP_DIREITO("M.S. Direito", "💪"),
    MEMBRO_SUP_ESQUERDO("M.S. Esquerdo", "💪"),
    MEMBRO_INF_DIREITO("M.I. Direito", "🦵"),
    MEMBRO_INF_ESQUERDO("M.I. Esquerdo", "🦵"),
    GENITALIA("Genitália", "👖")
}

enum class TipoFerimento(val label: String) {
    PERFURANTE("Perfurante"),
    CORTANTE("Cortante"),
    CONTUNDENTE("Contundente"),
    PERFURO_CORTANTE("Perfuro-cortante"),
    PERFURO_CONTUNDENTE("Perfuro-contundente"),
    CORTO_CONTUNDENTE("Corto-contundente"),
    QUEIMADURA("Queimadura"),
}

data class Lesao(
    val regiao: RegiaoCorporal,
    val tipo: TipoFerimento,
    val percentualQueimadura: Double? = null // Usado apenas quando tipo == QUEIMADURA
)

// ============================================
// SINAIS VITAIS
// ============================================

data class SinaisVitais(
    val pulso: Int? = null,                    // FC em BPM (3-300)
    val pressaoArterial: String = "",          // "120x80"
    val saturacaoO2: Int? = null,              // 0-100
    val escalaGCS: Int? = null,                // 3-15 (total AO + RV + RM)
    val aberturaOcular: Int? = null,           // 1-4
    val respostaVerbal: Int? = null,           // 1-5
    val respostaMotora: Int? = null,           // 1-6
    val respiracao: Int? = null,               // Movimentos respiratórios por minuto (0-60)
    val temperatura: Double? = null,
    val observacoesMedicas: String? = null
)

// ============================================
// VÍTIMA
// ============================================

data class Vitima(
    val id: String? = null,
    val ocorrenciaId: String,
    val nome: String = "",
    val idade: Int? = null,                    // Calculado automaticamente
    val pessoaId: String? = null,              // Vinculado à Pessoa
    val lesoes: String = "",                   // Breve histórico das lesões (texto livre)
    val lesoesEstruturadas: List<Lesao> = emptyList(), // Lesões do infográfico corporal
    val destinoSocorro: String = "",           // Texto livre (mantido para compatibilidade)
    val quemSocorreu: String = "",             // Select: SAMU, Corpo de Bombeiros, Concessionária, Ambulância, PM, GCM, Outros
    val resultadoOcorrencia: String = "",      // Select: Atendida, Cancelado, Recusa
    val viaturaSocorroId: String? = null,      // Referência à Viatura
    val hospitalDestino: String = "",
    val nomeMedico: String = "",
    val crmMedico: String = "",
    val sinaisVitais: SinaisVitais = SinaisVitais(),
    val cpf: String? = null,
    val lesoesAparentes: String? = null,
    val transportadoPor: String? = null
)
