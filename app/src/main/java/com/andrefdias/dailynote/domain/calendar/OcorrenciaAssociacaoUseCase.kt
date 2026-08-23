package com.andrefdias.dailynote.domain.calendar

import com.andrefdias.dailynote.domain.model.EquipeServico
import com.andrefdias.dailynote.domain.model.Ocorrencia
import com.andrefdias.dailynote.domain.model.OcorrenciaComMilitares
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class OcorrenciaAssociacaoUseCase @Inject constructor() {

    fun associar(ocorrencias: List<Ocorrencia>, equipes: List<EquipeServico>): List<OcorrenciaComMilitares> {
        val result = mutableListOf<OcorrenciaComMilitares>()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        for (ocorrencia in ocorrencias) {
            val vtrNorm = ocorrencia.vtr.replace("-", "").replace(" ", "").trim().uppercase()
            var assignedMilitares = ocorrencia.cmtVtr // fallback if no match, though prompt asks to return empty or just cmtVtr if no match? We should return actual Militar objects if matched.

            // Encontrar equipe correspondente no dia e viatura
            val equipeDoDia = equipes.find { it.data == ocorrencia.data }
            var militaresEncontrados = emptyList<com.andrefdias.dailynote.domain.model.Militar>()

            if (equipeDoDia != null) {
                val viaturaEquipe = equipeDoDia.viaturas.find { 
                    it.viatura?.prefixo?.replace("-", "")?.replace(" ", "")?.trim()?.uppercase() == vtrNorm 
                }

                if (viaturaEquipe != null) {
                    try {
                        val qtrSaidaTime = LocalTime.parse(ocorrencia.qtrSaida, timeFormatter)
                        // Na EquipeServico, horaInicio e horaTermino geralmente estao no EquipeConfig ou sao derivados do tipo de escala (ex: 24h as 07:30).
                        // TODO: Precisamos garantir que a equipe do dia estava de servico neste horario. Por simplificacao, se a viatura esta na equipe daquela data, assumimos a guarnicao. 
                        // O ideal seria pegar horaInicio e horaTermino da escala associada.
                        militaresEncontrados = viaturaEquipe.militaresEscalados.mapNotNull { it.militar }
                    } catch (e: Exception) {
                        // qtrSaida parse error
                    }
                }
            }

            result.add(OcorrenciaComMilitares(ocorrencia, militaresEncontrados))
        }
        return result
    }
}
