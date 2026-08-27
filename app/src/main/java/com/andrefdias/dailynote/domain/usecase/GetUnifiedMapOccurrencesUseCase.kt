package com.andrefdias.dailynote.domain.usecase

import com.andrefdias.dailynote.domain.model.CalendarEvento
import com.andrefdias.dailynote.domain.model.CalendarTarefa
import com.andrefdias.dailynote.domain.model.MapOccurrence
import com.andrefdias.dailynote.domain.model.OcorrenciaComMilitares
import com.andrefdias.dailynote.domain.model.OccurrenceSource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

class GetUnifiedMapOccurrencesUseCase @Inject constructor() {

    fun execute(
        ocorrenciasEtl: List<OcorrenciaComMilitares>,
        tarefasApp: List<CalendarTarefa>,
        eventosApp: List<CalendarEvento>
    ): List<MapOccurrence> {
        val unifiedList = mutableListOf<MapOccurrence>()

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        // 1. Mapear ocorrências do ETL
        for (occMil in ocorrenciasEtl) {
            val occ = occMil.ocorrencia
            val date = try { LocalDate.parse(occ.data, formatter) } catch (e: Exception) { null }
            
            unifiedList.add(
                MapOccurrence(
                    id = occ.id,
                    source = OccurrenceSource.ETL,
                    date = date,
                    time = occ.qtrSaida,
                    talao = occ.talao,
                    vtr = occ.vtr,
                    commander = occ.cmtVtr,
                    militaryPersonnel = occMil.militares.map { it.nomeGuerra }, // Apenas nomes para filtro simples
                    nature = occ.natureza,
                    victims = occ.vitimas,
                    fatalVictims = occ.vitimasFatais,
                    address = occ.endereco,
                    city = occ.cidade,
                    latitude = occ.latitude ?: 0.0,
                    longitude = occ.longitude ?: 0.0,
                    color = getMarkerColorForNature(occ.natureza)
                )
            )
        }

        // 2. Mapear Tarefas do APP
        for (tarefa in tarefasApp) {
            // Regra simples: Tarefas do App normalmente não têm coordenadas no Room nativo ainda,
            // mas vamos mapear os campos para não duplicar se futuramente adicionarem.
            // Para prevenir duplicidade com o ETL (simplificado), checamos título = natureza.
            val date = try { LocalDate.parse(tarefa.data, isoFormatter) } catch (e: Exception) { null }
            
            // Aqui verificamos se já existe uma ocorrência parecida no ETL para essa data/título
            val isDuplicate = unifiedList.any { 
                it.date == date && it.nature?.equals(tarefa.titulo, ignoreCase = true) == true
            }

            if (!isDuplicate) {
                unifiedList.add(
                    MapOccurrence(
                        id = tarefa.id,
                        source = OccurrenceSource.APP,
                        date = date,
                        time = tarefa.hora,
                        talao = null,
                        vtr = null,
                        commander = tarefa.responsavel,
                        militaryPersonnel = listOfNotNull(tarefa.responsavel),
                        nature = tarefa.titulo,
                        victims = 0,
                        fatalVictims = 0,
                        address = null, // Sem endereço por enquanto no CalendarTarefa
                        city = null,
                        latitude = 0.0,
                        longitude = 0.0,
                        color = tarefa.cor ?: "#10B981"
                    )
                )
            }
        }

        return unifiedList
    }

    private fun getMarkerColorForNature(nature: String?): String {
        if (nature == null) return "#757575"
        val n = nature.lowercase()
        return when {
            n.contains("incêndio") || n.contains("incendio") || n.contains("fogo") -> "#EF4444" // Vermelho
            n.contains("resgate") || n.contains("atendimento") -> "#3B82F6" // Azul
            n.contains("salvamento") -> "#F59E0B" // Laranja
            n.contains("produto perigoso") || n.contains("vazamento") -> "#8B5CF6" // Roxo
            n.contains("acidente") || n.contains("trânsito") -> "#EAB308" // Amarelo
            else -> "#6B7280" // Cinza padrão
        }
    }
}
