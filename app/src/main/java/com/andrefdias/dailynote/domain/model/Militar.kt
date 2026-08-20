package com.andrefdias.dailynote.domain.model

data class Militar(
    val id: String = java.util.UUID.randomUUID().toString(),
    val re: String,                 // 2 a 7 dígitos
    val nomeCompleto: String,
    val nomeGuerra: String,
    val graduacao: String,
    val situacao: String,
    val mergulhador: Boolean = false,
    val ovb: String = "Não Habilitado"
)

enum class GraduacaoMilitar(val display: String) {
    CEL_PM("CEL PM"),
    TEN_CEL_PM("TEN CEL PM"),
    MAJOR_PM("MAJ PM"),
    CAPITAO_PM("CAP PM"),
    PRIMEIRO_TEN_PM("1º TEN PM"),
    PRIMEIRO_TEN_QAPM("1º TEN QAPM"),
    SEGUNDO_TEN_PM("2º TEN PM"),
    SEGUNDO_TEN_QAPM("2º TEN QAPM"),
    ASPIRANTE_PM("ASP OF PM"),
    SUBTEN_PM("SUBTEN PM"),
    PRIMEIRO_SGT_PM("1º SGT PM"),
    SEGUNDO_SGT_PM("2º SGT PM"),
    TERCEIRO_SGT_PM("3º SGT PM"),
    CABO_PM("CB PM"),
    SOLDADO_PM("SD PM")
}

val situacoesMilitar = listOf("Ativo", "Férias", "Licença", "Afastado")
