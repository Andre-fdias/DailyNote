package com.andrefdias.dailynote.domain.calendar

import com.andrefdias.dailynote.domain.model.EscalaConfig
import com.andrefdias.dailynote.domain.model.EquipeConfig
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object ScaleEngine {

    /**
     * Calcula quais equipes estão de serviço em uma determinada data.
     * Retorna um mapa de turnoIndex (0=Diurno, 1=Noturno) para lista de equipes ativas.
     */
    fun getActiveTeamsForDate(
        date: LocalDate,
        escalaList: List<EscalaConfig>,
        equipeList: List<EquipeConfig>
    ): Map<Int, List<EquipeConfig>> {
        val result = mutableMapOf<Int, MutableList<EquipeConfig>>()

        equipeList.filter { it.ativa }.forEach { equipe ->
            val escala = escalaList.find { it.id == equipe.escalaId && it.ativa } ?: return@forEach
            val dataInicial = runCatching { LocalDate.parse(equipe.dataInicial) }.getOrNull() ?: return@forEach

            if (isWorkingOnDate(date, escala, equipe, dataInicial)) {
                result.getOrPut(equipe.ordemTurno) { mutableListOf() }.add(equipe)
            }
        }
        return result
    }

    /**
     * Calcula quais equipes estão DE SERVIÇO AGORA (data + hora atual).
     * Combina lógica de data com verificação de horário do turno.
     * Trata turnos que cruzam meia-noite (ex: 18:00 → 06:00 do dia seguinte).
     */
    fun getActiveTeamsRightNow(
        dateTime: LocalDateTime,
        escalaList: List<EscalaConfig>,
        equipeList: List<EquipeConfig>
    ): Map<Int, List<EquipeConfig>> {
        val result = mutableMapOf<Int, MutableList<EquipeConfig>>()
        val today = dateTime.toLocalDate()
        val yesterday = today.minusDays(1)
        val currentTime = dateTime.toLocalTime()

        equipeList.filter { it.ativa }.forEach { equipe ->
            val escala = escalaList.find { it.id == equipe.escalaId && it.ativa } ?: return@forEach
            val dataInicial = runCatching { LocalDate.parse(equipe.dataInicial) }.getOrNull() ?: return@forEach
            val horaInicio = parseTime(equipe.horaInicio) ?: return@forEach
            val horaTermino = parseTime(equipe.horaTermino) ?: return@forEach

            val isCrossNight = !horaTermino.isAfter(horaInicio) // Turno cruza meia-noite
            val is24h = horaInicio == horaTermino // Turno de 24h

            val isOnDutyNow = when {
                // Turno de 24h: apenas verifica se trabalha hoje
                is24h -> isWorkingOnDate(today, escala, equipe, dataInicial)

                // Turno diurno simples (ex: 06:00 → 18:00)
                !isCrossNight -> {
                    isWorkingOnDate(today, escala, equipe, dataInicial) &&
                        !currentTime.isBefore(horaInicio) && currentTime.isBefore(horaTermino)
                }

                // Turno noturno que cruza meia-noite (ex: 18:00 → 06:00)
                // Está de serviço se:
                //   - Hoje é dia de trabalho E hora >= horaInicio
                //   - OU ontem foi dia de trabalho E hora < horaTermino (continuação do turno de ontem)
                else -> {
                    val workingToday = isWorkingOnDate(today, escala, equipe, dataInicial)
                    val workedYesterday = isWorkingOnDate(yesterday, escala, equipe, dataInicial)

                    (workingToday && !currentTime.isBefore(horaInicio)) ||
                        (workedYesterday && currentTime.isBefore(horaTermino))
                }
            }

            if (isOnDutyNow) {
                result.getOrPut(equipe.ordemTurno) { mutableListOf() }.add(equipe)
            }
        }
        return result
    }

    /**
     * Verifica se uma equipe está trabalhando em uma data específica,
     * baseado na lógica de ciclo da escala.
     */
    private fun isWorkingOnDate(
        date: LocalDate,
        escala: EscalaConfig,
        equipe: EquipeConfig,
        dataInicial: LocalDate
    ): Boolean {
        val daysElapsed = date.toEpochDay() - dataInicial.toEpochDay()
        val totalHoras = escala.trabalhoHoras + escala.descansoHoras
        val cicloDias = (totalHoras / 24).coerceAtLeast(1)

        var dayInCycle = (daysElapsed % cicloDias).toInt()
        if (dayInCycle < 0) dayInCycle += cicloDias

        val diasTrabalho = (escala.trabalhoHoras / 24).coerceAtLeast(1)

        return when {
            // Escala 12x36 e similares com 2 turnos: cada equipe entra no turno correto
            escala.quantidadeTurnos == 2 -> dayInCycle == 0

            // Escalas gerais cíclicas (24x48, 24x72, etc.)
            else -> dayInCycle < diasTrabalho
        }
    }

    /**
     * Pré-calcula as escalas do mês inteiro para cache e alta performance.
     */
    fun getPrecomputedMonthScales(
        startMonthDate: LocalDate,
        escalaList: List<EscalaConfig>,
        equipeList: List<EquipeConfig>
    ): Map<LocalDate, Map<Int, List<EquipeConfig>>> {
        val cache = mutableMapOf<LocalDate, Map<Int, List<EquipeConfig>>>()
        var current = startMonthDate.withDayOfMonth(1)
        val end = current.plusMonths(1)
        while (current.isBefore(end)) {
            cache[current] = getActiveTeamsForDate(current, escalaList, equipeList)
            current = current.plusDays(1)
        }
        return cache
    }

    /**
     * Calcula a quantidade de dias consecutivos trabalhados de uma equipe até uma determinada data.
     */
    fun getConsecutiveWorkDays(
        targetDate: LocalDate,
        escalaList: List<EscalaConfig>,
        equipeList: List<EquipeConfig>,
        targetEquipeId: String
    ): Int {
        var consecutive = 0
        var checkDate = targetDate

        while (true) {
            val activeTeams = getActiveTeamsForDate(checkDate, escalaList, equipeList)
            val isWorking = activeTeams.values.flatten().any { it.id == targetEquipeId }
            if (isWorking) {
                consecutive++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return consecutive
    }

    private fun parseTime(hhmm: String): LocalTime? = runCatching {
        val parts = hhmm.split(":")
        LocalTime.of(parts[0].toInt(), parts[1].toInt())
    }.getOrNull()
}
