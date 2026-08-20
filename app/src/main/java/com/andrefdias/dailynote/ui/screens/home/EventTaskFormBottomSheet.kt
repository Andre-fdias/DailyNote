package com.andrefdias.dailynote.ui.screens.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrefdias.dailynote.domain.model.EscalaConfig
import com.andrefdias.dailynote.domain.model.SubtarefaInput
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

val PALETA_12_CORES = listOf(
    "#1976D2", "#D32F2F", "#388E3C", "#FBC02D",
    "#7B1FA2", "#0097A7", "#E91E63", "#F57C00",
    "#4E342E", "#00796B", "#0288D1", "#37474F"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTaskFormBottomSheet(
    initialIsTask: Boolean = false,
    initialDate: LocalDate = LocalDate.now(),
    availableEscalas: List<EscalaConfig> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (isTask: Boolean, titulo: String, data: LocalDate, hora: String?, desc: String, corHex: String, escalaId: String?, subtarefas: List<SubtarefaInput>) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { false }
    )

    var isTask by remember { mutableStateOf(initialIsTask) }
    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var horaInicio by remember { mutableStateOf<LocalTime?>(null) }
    var selectedColorHex by remember { mutableStateOf(PALETA_12_CORES.first()) }
    var selectedEscala by remember { mutableStateOf<EscalaConfig?>(null) }
    var subtarefas by remember { mutableStateOf<List<SubtarefaInput>>(emptyList()) }

    val dateFormated = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        properties = androidx.compose.material3.ModalBottomSheetProperties(
            shouldDismissOnBackPress = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTask) "✅ Nova Tarefa" else "📅 Novo Evento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            // Switch "É uma tarefa?"
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("É uma tarefa?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Switch(
                        checked = isTask,
                        onCheckedChange = { isTask = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { if (it.length <= 100) titulo = it },
                label = { Text("Título *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, y, m, d -> selectedDate = LocalDate.of(y, m + 1, d) },
                        selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("📅 Data: $dateFormated", fontWeight = FontWeight.Medium)
            }

            OutlinedButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, h, m -> horaInicio = LocalTime.of(h, m) },
                        horaInicio?.hour ?: 9, horaInicio?.minute ?: 0, true
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = horaInicio?.let { "⏰ Horário: %02d:%02d".format(it.hour, it.minute) } ?: "⏰ Definir Horário (Opcional)",
                    fontSize = 14.sp
                )
            }

            OutlinedTextField(
                value = descricao,
                onValueChange = { if (it.length <= 500) descricao = it },
                label = { Text("Descrição (opcional)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp)
            )

            Text("🎨 Cor de Fundo:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PALETA_12_CORES.forEach { hex ->
                    val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                    val isSelected = hex.equals(selectedColorHex, ignoreCase = true)
                    Box(
                        modifier = Modifier.size(24.dp).clip(CircleShape).background(color)
                            .border(if (isSelected) 3.dp else 0.dp, if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape)
                            .clickable { selectedColorHex = hex }
                    )
                }
            }

            if (availableEscalas.isNotEmpty()) {
                var expEscala by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expEscala, onExpandedChange = { expEscala = it }) {
                    OutlinedTextField(
                        value = selectedEscala?.nome ?: "Nenhuma (Global)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Escopo / Escala") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expEscala) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expEscala, onDismissRequest = { expEscala = false }) {
                        DropdownMenuItem(text = { Text("Global (Sem escala)") }, onClick = { selectedEscala = null; expEscala = false })
                        availableEscalas.forEach { esc ->
                            DropdownMenuItem(text = { Text(esc.nome) }, onClick = { selectedEscala = esc; expEscala = false })
                        }
                    }
                }
            }

            // Subtarefas (Se for Tarefa)
            if (isTask) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("☑️ Subtarefas (${subtarefas.size}/10):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (subtarefas.size < 10) {
                        TextButton(onClick = { subtarefas = subtarefas + SubtarefaInput(titulo = "", level = 0) }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Adicionar")
                        }
                    }
                }

                subtarefas.forEachIndexed { idx, sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (sub.level * 16).dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (sub.level > 0) {
                            IconButton(
                                onClick = {
                                    val copy = subtarefas.toMutableList()
                                    copy[idx] = sub.copy(level = sub.level - 1)
                                    subtarefas = copy
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Recuar", modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Spacer(modifier = Modifier.size(36.dp))
                        }

                        if (sub.level < 2) {
                            IconButton(
                                onClick = {
                                    val copy = subtarefas.toMutableList()
                                    copy[idx] = sub.copy(level = sub.level + 1)
                                    subtarefas = copy
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Avançar", modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Spacer(modifier = Modifier.size(36.dp))
                        }

                        OutlinedTextField(
                            value = sub.titulo,
                            onValueChange = { text ->
                                if (text.length <= 80) {
                                    val copy = subtarefas.toMutableList()
                                    copy[idx] = sub.copy(titulo = text)
                                    subtarefas = copy
                                }
                            },
                            placeholder = { Text("Subtarefa ${idx + 1}") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            val copy = subtarefas.toMutableList()
                            copy.removeAt(idx)
                            subtarefas = copy
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover Subtarefa", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (titulo.isNotBlank()) {
                        val horaStr = horaInicio?.let { String.format("%02d:%02d", it.hour, it.minute) }
                        onSave(isTask, titulo, selectedDate, horaStr, descricao, selectedColorHex, selectedEscala?.id, subtarefas.filter { it.titulo.isNotBlank() })
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp),
                enabled = titulo.isNotBlank()
            ) {
                Text("Salvar", fontWeight = FontWeight.Bold)
            }
        }
    }
}
