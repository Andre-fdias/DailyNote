package com.andrefdias.dailynote.ui.screens.calendar

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrefdias.dailynote.domain.model.EquipeConfig
import com.andrefdias.dailynote.domain.model.CalendarEvento
import com.andrefdias.dailynote.domain.model.CalendarTarefa
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.components.buttons.FireButton
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySummaryPopup(
    activeTeams: Map<Int, List<EquipeConfig>>,
    todayEvents: List<CalendarEvento>,
    todayTasks: List<CalendarTarefa>,
    consecutiveDays: Int,
    onDismiss: () -> Unit,
    onDontShowAgainToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Usa as cores do tema do sistema (dark/light mode aware)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📋 Resumo Operacional Diário",
                    style = FireTypography.Title,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = onSurfaceColor
                )
                Text(
                    text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM")),
                    style = FireTypography.Caption,
                    color = onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(FireSpacing.Medium),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Plantão e Escala do Dia
                Text(
                    text = "Serviço e Prontidão",
                    style = FireTypography.BodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = FireColors.Primary
                )

                if (activeTeams.isEmpty()) {
                    Text(
                        text = "Nenhuma equipe escalada para agora no sistema.",
                        style = FireTypography.BodyMedium,
                        color = onSurfaceVariant
                    )
                } else {
                    activeTeams.forEach { (_, teams) ->
                        teams.forEach { team ->
                            val backColor = runCatching { Color(parseColor(team.corFundo)) }.getOrDefault(FireColors.Primary)
                            val textColor = runCatching { Color(parseColor(team.corTexto)) }.getOrDefault(Color.White)

                            // Constrói o label do horário a partir dos dados reais da equipe
                            val turnoLabel = buildString {
                                append(if (team.ordemTurno == 0) "Turno Diurno" else "Turno Noturno")
                                if (team.horaInicio.isNotBlank() && team.horaTermino.isNotBlank()) {
                                    append(" (${team.horaInicio} – ${team.horaTermino})")
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = surfaceVariantColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(FireSpacing.Small),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(backColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(team.sigla, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(team.nome, style = FireTypography.BodyMedium, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                                        Text(turnoLabel, style = FireTypography.Caption, color = onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    if (consecutiveDays > 1) {
                        Text(
                            text = "⚠️ Atenção: equipe em plantão há $consecutiveDays dias consecutivos.",
                            style = FireTypography.Caption,
                            color = FireColors.Warning,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = dividerColor)

                // 2. Eventos e Instruções do Dia
                Text(
                    text = "Eventos / Instruções",
                    style = FireTypography.BodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = FireColors.Primary
                )

                if (todayEvents.isEmpty()) {
                    Text(
                        text = "Sem eventos ou instruções marcadas para hoje.",
                        style = FireTypography.BodyMedium,
                        color = onSurfaceVariant
                    )
                } else {
                    todayEvents.forEach { ev ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(runCatching { Color(parseColor(ev.cor)) }.getOrDefault(FireColors.Primary))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(ev.titulo, style = FireTypography.BodyMedium, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                                if (!ev.hora.isNullOrBlank()) {
                                    Text("Hora: ${ev.hora} | Local: ${ev.local ?: "Quartel"}", style = FireTypography.Caption, color = onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = dividerColor)

                // 3. Tarefas de Prontidão
                Text(
                    text = "Tarefas pendentes hoje",
                    style = FireTypography.BodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = FireColors.Primary
                )

                if (todayTasks.isEmpty()) {
                    Text(
                        text = "Sem tarefas de prontidão pendentes.",
                        style = FireTypography.BodyMedium,
                        color = onSurfaceVariant
                    )
                } else {
                    todayTasks.forEach { task ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = false, onCheckedChange = {})
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(task.titulo, style = FireTypography.BodyMedium, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                                Text("Prioridade: ${task.prioridade.name}", style = FireTypography.Caption, color = onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            FireButton(
                text = "ENTRAR NO SISTEMA",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDontShowAgainToday()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Não mostrar novamente hoje",
                    color = onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        containerColor = surfaceColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    )
}
