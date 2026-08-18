package com.andrefdias.dailynote.data.service

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.Instant

object ProntidaoService {
    // July 7, 2026 is fixed as the base date for 🟡 AMARELA
    private val START_DATE = LocalDate.of(2026, 7, 7)

    enum class Prontidao(val nome: String, val cor: Color, val corTexto: Color) {
        AMARELA("Prontidão Amarela", Color(0xFFFFB300), Color.Black),
        AZUL("Prontidão Azul", Color(0xFF1E88E5), Color.White),
        VERDE("Prontidão Verde", Color(0xFF43A047), Color.White)
    }

    data class ProntidaoInfo(
        val cor: String,
        val corHex: Long,
        val inicio: LocalDateTime,
        val fim: LocalDateTime,
        val horaInicio: String,
        val horaFim: String,
        val dataInicio: String,
        val dataFim: String
    )

    fun calcularProntidao(): ProntidaoInfo {
        val agora = LocalDateTime.now()
        val dataBase = if (agora.toLocalTime().isBefore(LocalTime.of(7, 30))) {
            agora.minusDays(1)
        } else {
            agora
        }

        val dias = ChronoUnit.DAYS.between(START_DATE, dataBase.toLocalDate())
        val index = ((dias % 3 + 3) % 3).toInt()

        val corNome = when (index) {
            0 -> "AMARELA"
            1 -> "AZUL"
            else -> "VERDE"
        }

        val corHex = when (index) {
            0 -> 0xFFFFC107 // Yellow
            1 -> 0xFF2196F3 // Blue
            else -> 0xFF4CAF50 // Green
        }

        val inicioProntidao = dataBase
            .withHour(7)
            .withMinute(30)
            .withSecond(0)
            .withNano(0)

        val fimProntidao = dataBase
            .plusDays(1)
            .withHour(7)
            .withMinute(29)
            .withSecond(0)
            .withNano(0)

        return ProntidaoInfo(
            cor = corNome,
            corHex = corHex,
            inicio = inicioProntidao,
            fim = fimProntidao,
            horaInicio = inicioProntidao.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
            horaFim = fimProntidao.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
            dataInicio = inicioProntidao.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            dataFim = fimProntidao.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        )
    }

    fun getProntidaoForDateTime(dateTime: LocalDateTime): Prontidao {
        val dataBase = if (dateTime.toLocalTime().isBefore(LocalTime.of(7, 30))) {
            dateTime.toLocalDate().minusDays(1)
        } else {
            dateTime.toLocalDate()
        }
        val daysBetween = ChronoUnit.DAYS.between(START_DATE, dataBase)
        val index = ((daysBetween % 3 + 3) % 3).toInt()
        return when (index) {
            0 -> Prontidao.AMARELA
            1 -> Prontidao.AZUL
            else -> Prontidao.VERDE
        }
    }

    fun getProntidaoForDate(date: LocalDate): Prontidao {
        val daysBetween = ChronoUnit.DAYS.between(START_DATE, date)
        val index = ((daysBetween % 3 + 3) % 3).toInt()
        return when (index) {
            0 -> Prontidao.AMARELA
            1 -> Prontidao.AZUL
            else -> Prontidao.VERDE
        }
    }

    fun getProntidaoForInstant(instant: Instant): Prontidao {
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return getProntidaoForDateTime(dateTime)
    }
}
