package com.andrefdias.dailynote.domain.model

data class Militar(
    val id: String = java.util.UUID.randomUUID().toString(),
    val re: String,                 // 2 a 7 dígitos
    val nomeCompleto: String,
    val nomeGuerra: String,
    val graduacao: String,
    val situacao: String
)

enum class GraduacaoMilitar(val display: String) {
    CEL_PM("Coronel PM"),
    TEN_CEL_PM("Tenente Coronel PM"),
    MAJOR_PM("Major PM"),
    CAPITAO_PM("Capitão PM"),
    PRIMEIRO_TEN_PM("1º Tenente PM"),
    SEGUNDO_TEN_PM("2º Tenente PM"),
    ASPIRANTE_PM("Aspirante a Oficial PM"),
    SUBTEN_PM("Subtenente PM"),
    PRIMEIRO_SGT_PM("1º Sargento PM"),
    SEGUNDO_SGT_PM("2º Sargento PM"),
    TERCEIRO_SGT_PM("3º Sargento PM"),
    CABO_PM("Cabo PM"),
    SOLDADO_PM("Soldado PM")
}

val situacoesMilitar = listOf("Ativo", "Férias", "Licença", "Afastado")
