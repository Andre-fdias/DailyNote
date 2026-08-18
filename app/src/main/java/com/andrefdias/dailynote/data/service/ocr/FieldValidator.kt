package com.andrefdias.dailynote.data.service.ocr

object FieldValidator {
    fun isValidCpf(cpf: String): Boolean {
        val cleanCpf = cpf.replace(Regex("[^0-9]"), "")
        if (cleanCpf.length != 11) return false
        if (cleanCpf.all { it == cleanCpf[0] }) return false

        return try {
            val d1 = cleanCpf.substring(0, 9).map { it - '0' }
            val d2 = cleanCpf.substring(0, 10).map { it - '0' }

            var sum1 = 0
            for (i in 0..8) sum1 += d1[i] * (10 - i)
            val r1 = (sum1 * 10) % 11
            val check1 = if (r1 == 10) 0 else r1

            var sum2 = 0
            for (i in 0..9) sum2 += d2[i] * (11 - i)
            val r2 = (sum2 * 10) % 11
            val check2 = if (r2 == 10) 0 else r2

            check1 == (cleanCpf[9] - '0') && check2 == (cleanCpf[10] - '0')
        } catch (e: Exception) {
            false
        }
    }

    fun isValidPlaca(placa: String): Boolean {
        val clean = placa.uppercase().replace(Regex("[^A-Z0-9]"), "")
        if (clean.length != 7) return false
        val mercosulRegex = Regex("^[A-Z]{3}[0-9][A-Z][0-9]{2}$")
        val antigaRegex = Regex("^[A-Z]{3}[0-9]{4}$")
        return mercosulRegex.matches(clean) || antigaRegex.matches(clean)
    }

    fun isValidDate(dateStr: String): Boolean {
        val clean = dateStr.trim()
        val dateRegex = Regex("^\\d{2}/\\d{2}/\\d{4}$")
        if (!dateRegex.matches(clean)) return false
        return try {
            val parts = clean.split("/")
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()
            if (month < 1 || month > 12) return false
            if (day < 1 || day > 31) return false
            if (year < 1900 || year > 2100) return false
            true
        } catch (e: Exception) {
            false
        }
    }
}
