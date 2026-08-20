package com.andrefdias.dailynote.data.service.ocr

object OCRConfidenceCalculator {
    fun calculate(key: String, value: String): Float {
        if (value.isBlank()) return 0.0f
        return when (key) {
            "cpf" -> {
                if (FieldValidator.isValidCpf(value)) 1.00f else if (value.length == 11) 0.60f else 0.30f
            }
            "placa" -> {
                if (FieldValidator.isValidPlaca(value)) 0.95f else 0.40f
            }
            "nascimento", "validade", "data_expedicao" -> {
                if (FieldValidator.isValidDate(value)) 0.90f else 0.40f
            }
            "chassi" -> {
                if (value.length == 17) 0.90f else 0.50f
            }
            "renavam" -> {
                if (value.length in 9..11) 0.88f else 0.50f
            }
            "nome", "proprietario" -> {
                val words = value.split(" ")
                if (words.size >= 2 && value.all { it.isLetter() || it.isWhitespace() }) 0.95f else 0.70f
            }
            else -> 0.85f // Default safe confidence
        }
    }
}
