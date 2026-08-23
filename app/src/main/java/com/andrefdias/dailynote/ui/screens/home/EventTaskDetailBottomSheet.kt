package com.andrefdias.dailynote.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrefdias.dailynote.domain.model.CalendarEvento
import com.andrefdias.dailynote.domain.model.CalendarTarefa
import com.andrefdias.dailynote.domain.model.ChecklistItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTaskDetailBottomSheet(
    evento: CalendarEvento? = null,
    tarefa: CalendarTarefa? = null,
    onDismiss: () -> Unit,
    onDelete: () -> Unit = {},
    onSaveEvento: (id: String?, titulo: String, data: String, hora: String, local: String, descricao: String, cor: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onSaveTarefa: (id: String?, titulo: String, data: String, hora: String, descricao: String, cor: String, subtarefas: List<ChecklistItem>) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    // State is no longer needed since we will use a Dialog

    
    var isEditMode by remember(evento, tarefa) { mutableStateOf(evento == null && tarefa == null) }

    var title by remember { mutableStateOf("") }
    var isEvent by remember { mutableStateOf(true) }
    var date by remember { mutableStateOf("") } // stored as yyyy-MM-dd
    var displayDate by remember { mutableStateOf("") } // displayed as dd/MM/yyyy
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#E91E63") }
    
    val cores = listOf("#E91E63", "#F44336", "#FF9800", "#4CAF50", "#2196F3", "#9C27B0")
    
    // Subtarefas (Hierarchy)
    val subtarefas = remember { mutableStateListOf<ChecklistItem>() }
    var newSubtarefa by remember { mutableStateOf("") }

    // Date Picker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    
    // Time Picker
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

    // Location Dropdown
    val locationOptions = listOf("Quartel Comando Geral", "1º Batalhão", "2º Batalhão", "Posto Avançado", "Hospital Militar", "Centro de Treinamento", "Centro")
    var expandedLocation by remember { mutableStateOf(false) }

    // Init from existing
    LaunchedEffect(evento, tarefa) {
        if (evento != null) {
            title = evento.titulo
            date = evento.data
            time = evento.hora ?: ""
            location = evento.local ?: ""
            description = evento.descricao
            selectedColor = evento.cor
            isEvent = true
            
            try {
                val sdfModel = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfDisplay = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                sdfModel.parse(evento.data)?.let { displayDate = sdfDisplay.format(it) }
            } catch (e: Exception) {}
        } else if (tarefa != null) {
            title = tarefa.titulo
            date = tarefa.data
            time = tarefa.hora ?: ""
            description = tarefa.descricao
            selectedColor = tarefa.cor ?: "#2196F3"
            isEvent = false
            subtarefas.clear()
            subtarefas.addAll(tarefa.checklist)
            
            try {
                val sdfModel = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfDisplay = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                sdfModel.parse(tarefa.data)?.let { displayDate = sdfDisplay.format(it) }
            } catch (e: Exception) {}
        } else {
            val d = Date()
            val sdfModel = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfDisplay = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            date = sdfModel.format(d)
            displayDate = sdfDisplay.format(d)
        }
    }
    
    val toggleCheck: (Int) -> Unit = { index ->
        val item = subtarefas[index]
        val newState = !item.concluido
        subtarefas[index] = item.copy(concluido = newState)

        if (item.level == 0) {
            for (i in index + 1 until subtarefas.size) {
                if (subtarefas[i].level == 0) break
                subtarefas[i] = subtarefas[i].copy(concluido = newState)
            }
        } else {
            var parentIndex = -1
            for (i in index downTo 0) {
                if (subtarefas[i].level == 0) {
                    parentIndex = i
                    break
                }
            }
            if (parentIndex != -1) {
                var allChecked = true
                for (i in parentIndex + 1 until subtarefas.size) {
                    if (subtarefas[i].level == 0) break
                    if (!subtarefas[i].concluido) {
                        allChecked = false
                        break
                    }
                }
                subtarefas[parentIndex] = subtarefas[parentIndex].copy(concluido = allChecked)
            }
        }
        if (tarefa != null && !isEditMode) {
            onSaveTarefa(tarefa.id, title, date, time, description, selectedColor, subtarefas.toList())
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdfDisplay = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                        val sdfModel = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        // Add timezone offset to fix off-by-one day issues
                        val d = Date(millis + TimeZone.getDefault().getOffset(millis))
                        date = sdfModel.format(d)
                        displayDate = sdfDisplay.format(d)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { /* Do nothing to prevent accidental close */ },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                color = MaterialTheme.colorScheme.surface
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
                if (!isEditMode && (evento != null || tarefa != null)) {
                    Text(
                        text = if (evento != null) "📅 Detalhes do Evento" else "✅ Detalhes da Tarefa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else if (isEditMode && (evento != null || tarefa != null)) {
                    Text(
                        text = if (evento != null) "✏️ Editar Evento" else "✏️ Editar Tarefa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            if (isEditMode) {
                // Formulário de Criação/Edição
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    
                    // Título
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = TextStyle(fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Column {
                                if (title.isEmpty()) {
                                    Text("Adicionar título", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    innerTextField()
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 2.dp)
                            }
                        }
                    )

                    // Toggle Evento / Tarefa (Only if creating new)
                    if (evento == null && tarefa == null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                selected = isEvent,
                                onClick = { isEvent = true },
                                label = { Text("Evento") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFBBDEFB),
                                    selectedLabelColor = Color(0xFF1565C0)
                                )
                            )
                            FilterChip(
                                selected = !isEvent,
                                onClick = { isEvent = false },
                                label = { Text("Tarefa") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFBBDEFB),
                                    selectedLabelColor = Color(0xFF1565C0)
                                )
                            )
                        }
                    }

                    // Data e Hora (Modais)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Date field
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                                .clickable { showDatePicker = true }
                                .padding(12.dp)
                            ) {
                                Text(
                                    text = if (displayDate.isNotEmpty()) displayDate else "Selecionar Data",
                                    color = if (displayDate.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Time field
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                                .clickable { showTimePicker = true }
                                .padding(12.dp)
                            ) {
                                Text(
                                    text = if (time.isNotEmpty()) time else "Selecionar Hora",
                                    color = if (time.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Local com Autocomplete (Mock)
                    if (isEvent) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            ExposedDropdownMenuBox(
                                expanded = expandedLocation,
                                onExpandedChange = { expandedLocation = !expandedLocation }
                            ) {
                                OutlinedTextField(
                                    value = location,
                                    onValueChange = { location = it; expandedLocation = true },
                                    placeholder = { Text("Adicionar local") },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                                // Filtra as opções pelo que foi digitado
                                val filteredOptions = locationOptions.filter { it.contains(location, ignoreCase = true) }
                                if (filteredOptions.isNotEmpty() && expandedLocation) {
                                    ExposedDropdownMenu(
                                        expanded = expandedLocation,
                                        onDismissRequest = { expandedLocation = false }
                                    ) {
                                        filteredOptions.forEach { selectionOption ->
                                            DropdownMenuItem(
                                                text = { Text(selectionOption) },
                                                onClick = {
                                                    location = selectionOption
                                                    expandedLocation = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Descrição
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Outlined.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Adicionar descrição") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    // Subtarefas Hierárquicas (Se Tarefa)
                    if (!isEvent) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Outlined.Checklist, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Checklist", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                
                                subtarefas.forEachIndexed { index, item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = if (item.level > 0) (16 * item.level).dp else 0.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Icon(
                                            if (item.level == 0) {
                                                if (item.concluido) Icons.Outlined.CheckCircle else Icons.Outlined.Folder
                                            } else {
                                                if (item.concluido) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp).clickable { toggleCheck(index) },
                                            tint = if (item.concluido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            item.titulo, 
                                            modifier = Modifier.weight(1f).clickable { toggleCheck(index) }, 
                                            fontWeight = if (item.level == 0) FontWeight.Bold else FontWeight.Normal,
                                            textDecoration = if (item.concluido && item.level > 0) TextDecoration.LineThrough else null,
                                            color = if (item.concluido) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                        )
                                        IconButton(onClick = { subtarefas.removeAt(index) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = newSubtarefa,
                                        onValueChange = { newSubtarefa = it },
                                        placeholder = { Text("Novo item...") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                    
                                    // Adicionar Nível 0 (Categoria)
                                    IconButton(onClick = { 
                                        if (newSubtarefa.isNotBlank()) {
                                            subtarefas.add(ChecklistItem(UUID.randomUUID().toString(), newSubtarefa, false, level = 0))
                                            newSubtarefa = ""
                                        }
                                    }) {
                                        Icon(Icons.Outlined.CreateNewFolder, contentDescription = "Adicionar Categoria", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    // Adicionar Nível 1 (Subtarefa)
                                    IconButton(onClick = { 
                                        if (newSubtarefa.isNotBlank()) {
                                            subtarefas.add(ChecklistItem(UUID.randomUUID().toString(), newSubtarefa, false, level = 1))
                                            newSubtarefa = ""
                                        }
                                    }) {
                                        Icon(Icons.Outlined.SubdirectoryArrowRight, contentDescription = "Adicionar Subtarefa", tint = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }

                    // Seletor de Cores
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Outlined.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            cores.forEach { hexColor ->
                                val colorValue = parseHexColor(hexColor, Color.Gray)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(colorValue.copy(alpha = 0.8f))
                                        .border(
                                            width = if (selectedColor == hexColor) 3.dp else 0.dp,
                                            color = if (selectedColor == hexColor) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = hexColor },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedColor == hexColor) {
                                        Icon(Icons.Default.Check, contentDescription = "Selecionada", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Salvar
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = {
                                if (isEvent) {
                                    onSaveEvento(evento?.id, title, date, time, location, description, selectedColor)
                                } else {
                                    onSaveTarefa(tarefa?.id, title, date, time, description, selectedColor, subtarefas.toList())
                                }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                            enabled = title.isNotBlank() && date.isNotBlank()
                        ) {
                            Text("Salvar")
                        }
                    }
                }

            } else if (evento != null) {
                // Modo Visualização de Evento
                val corEvento = parseHexColor(evento.cor, Color(0xFF9C27B0))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(16.dp).background(corEvento, CircleShape))
                        Text(evento.titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    if (evento.descricao.isNotBlank()) {
                        Text(evento.descricao, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Event, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Data: $displayDate", fontSize = 14.sp)
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
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Excluir")
                        }
                        Button(
                            onClick = { isEditMode = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Editar")
                        }
                    }
                }
            } else if (tarefa != null) {
                // Modo Visualização de Tarefa
                val corTarefa = parseHexColor(tarefa.cor ?: "#2196F3", Color(0xFF2196F3))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(16.dp).background(corTarefa, CircleShape))
                        Text(tarefa.titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    if (tarefa.descricao.isNotBlank()) {
                        Text(tarefa.descricao, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Event, null, tint = MaterialTheme.colorScheme.secondary)
                        Text("Data Prevista: $displayDate", fontSize = 14.sp)
                    }
                    if (tarefa.hora != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Schedule, null, tint = MaterialTheme.colorScheme.secondary)
                            Text("Hora: ${tarefa.hora}", fontSize = 14.sp)
                        }
                    }

                    if (subtarefas.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Checklist", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            subtarefas.forEachIndexed { index, item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, 
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { toggleCheck(index) }
                                        .padding(vertical = 4.dp)
                                        .padding(start = if (item.level > 0) (16 * item.level).dp else 0.dp)
                                ) {
                                    if (item.level == 0) {
                                        Icon(
                                            if (item.concluido) Icons.Outlined.CheckCircle else Icons.Outlined.Folder, 
                                            contentDescription = null, 
                                            tint = if (item.concluido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, 
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Icon(
                                            if (item.concluido) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (item.concluido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    Text(
                                        item.titulo,
                                        fontSize = 14.sp,
                                        fontWeight = if (item.level == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (item.concluido) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (item.concluido && item.level > 0) TextDecoration.LineThrough else null
                                    )
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
                        Button(
                            onClick = { isEditMode = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Editar")
                        }
                    }
                }
            }
        }
            }
        }
    }
}

