package com.andrefdias.dailynote.data.service.ocr

object RegexExtractor {
    val CPF_PATTERN = Regex("\\b[0-9]{3}\\.?[0-9]{3}\\.?[0-9]{3}-?[0-9]{2}\\b")
    val RG_PATTERN = Regex("\\b[0-9]{1,2}\\.?[0-9]{3}\\.?[0-9]{3}-?[0-9X]\\b", RegexOption.IGNORE_CASE)
    val DATE_PATTERN = Regex("\\b([0-9]{2})/([0-9]{2})/([0-9]{4})\\b")
    val PLACA_PATTERN = Regex("\\b[A-Z]{3}[0-9][A-Z0-9][0-9]{2}\\b")
    val RENAVAM_PATTERN = Regex("\\b[0-9]{9,11}\\b")
    val CHASSI_PATTERN = Regex("\\b[A-HJ-NPR-Z0-9]{17}\\b")
    val REGISTRO_CNH_PATTERN = Regex("\\b[0-9]{9,11}\\b")
    val CATEGORIA_CNH_PATTERN = Regex("\\b(A|B|C|D|E|AB|AC|AD|AE)\\b")
    val NUMERO_OAB_PATTERN = Regex("\\b[0-9]{3,6}\\b")
    val UF_PATTERN = Regex("\\b(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)\\b", RegexOption.IGNORE_CASE)

    fun findPattern(text: String, regex: Regex): String {
        return regex.find(text)?.value ?: ""
    }

    fun findCpf(text: String): String {
        return findPattern(text, CPF_PATTERN).replace(Regex("[.-]"), "")
    }

    fun findRg(text: String): String {
        return findPattern(text, RG_PATTERN)
    }

    fun findDates(text: String): List<String> {
        return DATE_PATTERN.findAll(text).map { it.value }.toList()
    }

    fun findPlaca(text: String): String {
        return findPattern(text, PLACA_PATTERN)
    }

    fun findRenavam(text: String): String {
        return findPattern(text, RENAVAM_PATTERN)
    }

    fun findChassi(text: String): String {
        return findPattern(text, CHASSI_PATTERN)
    }

    fun findLineAfter(lines: List<String>, keyword: String): String? {
        lines.forEachIndexed { index, line ->
            if (line.contains(keyword, ignoreCase = true) && index + 1 < lines.size) {
                val value = lines[index + 1].trim()
                if (value.isNotBlank() && !value.contains(":") && value.length > 2) {
                    return value
                }
            }
        }
        return null
    }

    fun findFirstProperName(lines: List<String>): String {
        return lines.find { line ->
            line.length > 8 && line.all { it.isLetter() || it.isWhitespace() } && line == line.uppercase()
        } ?: lines.firstOrNull { it.length > 5 } ?: ""
    }
}
