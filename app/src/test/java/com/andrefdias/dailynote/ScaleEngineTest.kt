package com.andrefdias.dailynote

import com.andrefdias.dailynote.domain.calendar.ScaleEngine
import com.andrefdias.dailynote.domain.model.EscalaConfig
import com.andrefdias.dailynote.domain.model.EquipeConfig
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ScaleEngineTest {

    @Test
    fun testScale24x48() {
        val escala24x48 = EscalaConfig(
            id = "escala_24x48",
            nome = "Escala 24x48",
            trabalhoHoras = 24,
            descansoHoras = 48,
            quantidadeTurnos = 1,
            ativa = true,
            descricao = "24 horas de serviço por 48 de folga"
        )

        // Equipe Alfa começa em 2026-07-01
        val equipeAlfa = EquipeConfig(
            id = "eq_alfa",
            nome = "Equipe Alfa",
            sigla = "A",
            corFundo = "#FF0000",
            corTexto = "#FFFFFF",
            corBorda = null,
            escalaId = "escala_24x48",
            dataInicial = "2026-07-01",
            ordemTurno = 0,
            ativa = true
        )

        val escalas = listOf(escala24x48)
        val equipes = listOf(equipeAlfa)

        // 2026-07-01 (Dia 0) -> Alfa está ativa no turno 0
        val activeDay0 = ScaleEngine.getActiveTeamsForDate(LocalDate.parse("2026-07-01"), escalas, equipes)
        assertTrue(activeDay0[0]?.any { it.id == "eq_alfa" } == true)

        // 2026-07-02 (Dia 1) -> Alfa está de folga
        val activeDay1 = ScaleEngine.getActiveTeamsForDate(LocalDate.parse("2026-07-02"), escalas, equipes)
        assertFalse(activeDay1[0]?.any { it.id == "eq_alfa" } == true)

        // 2026-07-03 (Dia 2) -> Alfa está de folga
        val activeDay2 = ScaleEngine.getActiveTeamsForDate(LocalDate.parse("2026-07-03"), escalas, equipes)
        assertFalse(activeDay2[0]?.any { it.id == "eq_alfa" } == true)

        // 2026-07-04 (Dia 3) -> Alfa volta a estar ativa
        val activeDay3 = ScaleEngine.getActiveTeamsForDate(LocalDate.parse("2026-07-04"), escalas, equipes)
        assertTrue(activeDay3[0]?.any { it.id == "eq_alfa" } == true)
    }

    @Test
    fun testScale12x36TwoShifts() {
        val escala12x36 = EscalaConfig(
            id = "escala_12x36",
            nome = "Escala 12x36",
            trabalhoHoras = 12,
            descansoHoras = 36,
            quantidadeTurnos = 2,
            ativa = true,
            descricao = "12 horas de serviço por 36 de folga"
        )

        // Equipe Alfa começa em 2026-07-01 no turno Diurno (ordemTurno = 0)
        val equipeAlfaDiurno = EquipeConfig(
            id = "eq_alfa_d",
            nome = "Alfa Diurno",
            sigla = "AD",
            corFundo = "#00FF00",
            corTexto = "#000000",
            corBorda = null,
            escalaId = "escala_12x36",
            dataInicial = "2026-07-01",
            ordemTurno = 0,
            ativa = true
        )

        // Equipe Bravo começa em 2026-07-01 no turno Noturno (ordemTurno = 1)
        val equipeBravoNoturno = EquipeConfig(
            id = "eq_bravo_n",
            nome = "Bravo Noturno",
            sigla = "BN",
            corFundo = "#0000FF",
            corTexto = "#FFFFFF",
            corBorda = null,
            escalaId = "escala_12x36",
            dataInicial = "2026-07-01",
            ordemTurno = 1,
            ativa = true
        )

        val escalas = listOf(escala12x36)
        val equipes = listOf(equipeAlfaDiurno, equipeBravoNoturno)

        // 2026-07-01 (Dia 1 do ciclo)
        // Turno Diurno (index 0) -> Alfa Diurno deve trabalhar
        // Turno Noturno (index 1) -> Bravo Noturno deve trabalhar
        val activeDay1 = ScaleEngine.getActiveTeamsForDate(LocalDate.parse("2026-07-01"), escalas, equipes)
        assertTrue(activeDay1[0]?.any { it.id == "eq_alfa_d" } == true)
        assertTrue(activeDay1[1]?.any { it.id == "eq_bravo_n" } == true)

        // 2026-07-02 (Dia 2 do ciclo) -> Ambos de folga
        val activeDay2 = ScaleEngine.getActiveTeamsForDate(LocalDate.parse("2026-07-02"), escalas, equipes)
        assertFalse(activeDay2[0]?.any { it.id == "eq_alfa_d" } == true)
        assertFalse(activeDay2[1]?.any { it.id == "eq_bravo_n" } == true)

        // 2026-07-03 (Dia 3 do ciclo / Dia 1 de volta) -> Ambos voltam a trabalhar
        val activeDay3 = ScaleEngine.getActiveTeamsForDate(LocalDate.parse("2026-07-03"), escalas, equipes)
        assertTrue(activeDay3[0]?.any { it.id == "eq_alfa_d" } == true)
        assertTrue(activeDay3[1]?.any { it.id == "eq_bravo_n" } == true)
    }

    @Test
    fun testAdministrativeScale5x2() {
        val escala5x2 = EscalaConfig(
            id = "escala_5x2",
            nome = "Escala 5x2 Administrativa",
            trabalhoHoras = 8,
            descansoHoras = 16,
            quantidadeTurnos = 1,
            ativa = true,
            descricao = "Escala administrativa de segunda a sexta"
        )

        val equipeAdm = EquipeConfig(
            id = "eq_adm",
            nome = "Equipe Adm",
            sigla = "ADM",
            corFundo = "#CCCCCC",
            corTexto = "#000000",
            corBorda = null,
            escalaId = "escala_5x2",
            dataInicial = "2026-07-01",
            ordemTurno = 0,
            ativa = true
        )

        val escalas = listOf(escala5x2)
        val equipes = listOf(equipeAdm)

        // 2026-07-20 (Segunda-feira) -> Deve estar ativa
        val activeMon = ScaleEngine.getActiveTeamsForDate(LocalDate.parse("2026-07-20"), escalas, equipes)
        assertTrue(activeMon[0]?.any { it.id == "eq_adm" } == true)

        // 2026-07-25 (Sábado) -> Deve estar de folga
        val activeSat = ScaleEngine.getActiveTeamsForDate(LocalDate.parse("2026-07-25"), escalas, equipes)
        assertFalse(activeSat[0]?.any { it.id == "eq_adm" } == true)
    }
}
