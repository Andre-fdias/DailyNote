package com.andrefdias.dailynote.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

enum class TipoEvento(val descricao: String, val icon: ImageVector) {
    REUNIAO("Reunião", Icons.Default.MeetingRoom),
    TREINAMENTO("Treinamento", Icons.Default.School),
    INSTRUCAO("Instrução", Icons.Default.School),
    FORMATURA("Formatura", Icons.Default.SportsScore),
    EXAME_MEDICO("Exame Médico", Icons.Default.LocalHospital),
    SERVICO("Serviço Extra", Icons.Default.Work),
    OUTRO("Outro", Icons.Default.Event)
}
