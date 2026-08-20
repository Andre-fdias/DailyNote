package com.andrefdias.dailynote.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrefdias.dailynote.domain.model.CalendarEvento
import com.andrefdias.dailynote.domain.model.CalendarTarefa

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTaskDetailBottomSheet(
    evento: CalendarEvento? = null,
    tarefa: CalendarTarefa? = null,
    onDismiss: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (evento != null) "📅 Detalhes do Evento" else "✅ Detalhes da Tarefa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            if (evento != null) {
                val corEvento = parseHexColor(evento.cor, Color(0xFF9C27B0))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(16.dp).background(corEvento, CircleShape))
                        Text(evento.titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    if (evento.descricao.isNotBlank()) {
                        Text(evento.descricao, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Event, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Data: ${evento.data}", fontSize = 14.sp)
                    }
                    if (evento.hora != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Schedule, null, tint = MaterialTheme.colorScheme.primary)
                            Text("Hora: ${evento.hora}", fontSize = 14.sp)
                        }
                    }
                    if (!evento.local.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                            Text("Local: ${evento.local}", fontSize = 14.sp)
                        }
                    }
                }
            } else if (tarefa != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.TaskAlt, null, tint = MaterialTheme.colorScheme.secondary)
                        Text(tarefa.titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    if (tarefa.descricao.isNotBlank()) {
                        Text(tarefa.descricao, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Event, null, tint = MaterialTheme.colorScheme.secondary)
                        Text("Data Prevista: ${tarefa.data}", fontSize = 14.sp)
                    }
                    if (tarefa.hora != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Schedule, null, tint = MaterialTheme.colorScheme.secondary)
                            Text("Hora: ${tarefa.hora}", fontSize = 14.sp)
                        }
                    }

                    if (tarefa.checklist.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Subtarefas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            tarefa.checklist.forEach { item ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        if (item.concluido) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (item.concluido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        item.titulo,
                                        fontSize = 14.sp,
                                        color = if (item.concluido) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (item.concluido) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Excluir")
                }
            }
        }
    }
}
