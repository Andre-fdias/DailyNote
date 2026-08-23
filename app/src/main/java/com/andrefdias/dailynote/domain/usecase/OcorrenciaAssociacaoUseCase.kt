package com.andrefdias.dailynote.domain.usecase

import com.andrefdias.dailynote.domain.model.EquipeServico
import com.andrefdias.dailynote.domain.model.Ocorrencia
import com.andrefdias.dailynote.domain.model.OcorrenciaComMilitares
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class OcorrenciaAssociacaoUseCase @Inject constructor() {

    fun associar(
        ocorrencias: List<Ocorrencia>,
        equipes: List<EquipeServico>,
        todasViaturas: List<com.andrefdias.dailynote.domain.model.Viatura>,
        todosMilitares: List<com.andrefdias.dailynote.domain.model.Militar>
    ): List<OcorrenciaComMilitares> {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        return ocorrencias.map { ocorrencia ->
            val vtrOcorrencia = normalizarVtr(ocorrencia.vtr)
            
            val qtrSaida = try {
                LocalTime.parse(ocorrencia.qtrSaida, timeFormatter)
            } catch (e: Exception) {
                null
            }

            var militaresDaOcorrencia = emptyList<com.andrefdias.dailynote.domain.model.Militar>()

            for (equipe in equipes) {
                val dataEquipe = normalizarDataParaDdMmYyyy(equipe.data)
                if (dataEquipe != ocorrencia.data) continue

                val isMesmoHorario = if (qtrSaida != null) {
                    val horaInicio = try { LocalTime.parse(equipe.dejemHorarioInicio ?: "", timeFormatter) } catch (e: Exception) { null }
                    val horaFim = try { LocalTime.parse(equipe.dejemHorarioFim ?: "", timeFormatter) } catch (e: Exception) { null }

                    if (horaInicio != null && horaFim != null) {
                        if (horaFim.isBefore(horaInicio)) {
                            qtrSaida.isAfter(horaInicio) || qtrSaida.isBefore(horaFim) || qtrSaida == horaInicio || qtrSaida == horaFim
                        } else {
                            (qtrSaida.isAfter(horaInicio) || qtrSaida == horaInicio) && 
                            (qtrSaida.isBefore(horaFim) || qtrSaida == horaFim)
                        }
                    } else {
                        false
                    }
                } else {
                    false
                }

                if (!isMesmoHorario) continue

                // Check viaturas in this equipe
                for (equipeViatura in equipe.viaturas) {
                    val vtrObj = todasViaturas.find { it.id == equipeViatura.viaturaId }
                    if (vtrObj != null) {
                        val vtrEquipe = normalizarVtr(vtrObj.prefixo)
                        if (vtrEquipe == vtrOcorrencia) {
                            // Encontrou!
                            militaresDaOcorrencia = equipeViatura.militaresEscalados.mapNotNull { esc ->
                                todosMilitares.find { it.id == esc.militarId }
                            }
                            break
                        }
                    }
                }

                if (militaresDaOcorrencia.isNotEmpty()) break
            }

            OcorrenciaComMilitares(
                ocorrencia = ocorrencia,
                militares = militaresDaOcorrencia
            )
        }
    }

    private fun normalizarVtr(vtr: String): String {
        return vtr.replace(Regex("[^a-zA-Z0-9]"), "").uppercase()
    }

    private fun normalizarDataParaDdMmYyyy(data: String): String {
        // Se a data já contiver barras, assumimos que está correta ou dd/MM/yyyy
        if (data.contains("/")) return data
        
        // Se contiver hífen e tiver 10 caracteres, pode ser yyyy-MM-dd
        if (data.contains("-") && data.length == 10) {
            return try {
                val parsed = LocalDate.parse(data, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } catch (e: Exception) {
                data
            }
        }
        return data
    }
}
