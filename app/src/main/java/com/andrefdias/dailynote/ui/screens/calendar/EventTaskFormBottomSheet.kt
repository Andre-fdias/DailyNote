package com.andrefdias.dailynote.ui.screens.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import com.andrefdias.dailynote.domain.model.SubtarefaInput
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.components.inputs.FireTextField
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

val PALETA_12_CORES = listOf(
    "#1976D2", "#D32F2F", "#388E3C", "#FBC02D",
    "#7B1FA2", "#0097A7", "#E91E63", "#F57C00",
    "#4E342E", "#00796B", "#0288D1", "#37474F"
)

val OPCOES_LEMBRETE = listOf(
    "Nenhum" to 0,
    "5 minutos antes" to 5,
    "15 minutos antes" to 15,
    "30 minutos antes" to 30,
    "1 hora antes" to 60,
    "2 horas antes" to 120,
    "1 dia antes" to 1440
)

val OPCOES_LEMBRETE_DROPDOWN = listOf(
    "No horário" to 0,
    "1 hora antes" to 60,
    "2 horas antes" to 120,
    "6 horas antes" to 360,
    "12 horas antes" to 720,
    "1 dia antes" to 1440,
    "2 dias antes" to 2880
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTaskFormBottomSheet(
    initialIsTask: Boolean = false,
    initialDate: LocalDate = LocalDate.now(),
    initialTitulo: String = "",
    initialDescricao: String = "",
    initialLocal: String = "",
    initialHoraInicio: LocalTime? = null,
    initialHoraFim: LocalTime? = null,
    initialLembretesMinutos: List<Int> = emptyList(),
    initialSubtarefas: List<SubtarefaInput> = emptyList(),
    availableEscalas: List<com.andrefdias.dailynote.domain.model.EscalaConfig> = emptyList(),
    initialEscalaId: String? = null,
    initialCorHex: String? = null,
    onDismiss: () -> Unit,
    onSaveEvento: (titulo: String, data: String, horaInicio: String?, horaFim: String?, desc: String, corHex: String, lembretesMinutos: List<Int>, local: String, escalaId: String?) -> Unit,
    onSaveTarefa: (titulo: String, data: String, desc: String, corHex: String, lembretesMinutos: List<Int>, subtarefas: List<SubtarefaInput>, escalaId: String?) -> Unit
) {
    val context = LocalContext.current

    var isTask by remember(initialIsTask) { mutableStateOf(initialIsTask) }
    var titulo by remember(initialTitulo) { mutableStateOf(initialTitulo) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var horaInicio by remember(initialHoraInicio) { mutableStateOf<LocalTime?>(initialHoraInicio) }
    var horaFim by remember(initialHoraFim) { mutableStateOf<LocalTime?>(initialHoraFim) }
    var descricao by remember(initialDescricao) { mutableStateOf(initialDescricao) }
    var local by remember(initialLocal) { mutableStateOf(initialLocal) }
    var selectedColorHex by remember(initialCorHex) { 
        mutableStateOf(initialCorHex ?: PALETA_12_CORES.first()) 
    }
    var selectedReminders by remember(initialLembretesMinutos) { mutableStateOf(initialLembretesMinutos) }
    var subtarefas by remember(initialSubtarefas) { mutableStateOf(initialSubtarefas) }
    var selectedEscalaId by remember(initialEscalaId) { mutableStateOf(initialEscalaId) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormated = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    var showConfirmCancelDialog by remember { mutableStateOf(false) }
    var allowDismiss by remember { mutableStateOf(false) }

    val hasChanges = remember(
        isTask, titulo, selectedDate, horaInicio, horaFim, descricao, local, selectedColorHex, selectedReminders, subtarefas, selectedEscalaId
    ) {
        isTask != initialIsTask ||
        titulo != initialTitulo ||
        selectedDate != initialDate ||
        horaInicio != initialHoraInicio ||
        horaFim != initialHoraFim ||
        descricao != initialDescricao ||
        local != initialLocal ||
        selectedColorHex != (initialCorHex ?: PALETA_12_CORES.first()) ||
        selectedReminders != initialLembretesMinutos ||
        subtarefas != initialSubtarefas ||
        selectedEscalaId != initialEscalaId
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            if (newState == SheetValue.Hidden) allowDismiss else true
        }
    )

    val scope = rememberCoroutineScope()
    
    val attemptDismiss = {
        if (hasChanges) {
            showConfirmCancelDialog = true
        } else {
            allowDismiss = true
            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
        }
    }

    BackHandler {
        attemptDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = { attemptDismiss() }, // Intercept dismisses
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        if (showConfirmCancelDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmCancelDialog = false },
                title = { Text("Existem alterações não salvas") },
                text = { Text("Deseja realmente cancelar este cadastro?") },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmCancelDialog = false
                        allowDismiss = true
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    }) {
                        Text("Descartar alterações")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmCancelDialog = false }) {
                        Text("Continuar editando")
                    }
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Titulo + Botão Fechar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTask) "📋 Nova Tarefa" else "📅 Novo Evento",
                    style = FireTypography.Title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { attemptDismiss() }) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "É uma tarefa?",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isTask,
                        onCheckedChange = { isTask = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = FireColors.Primary)
                    )
                }
            }

            // Mensagem de Erro se houver
            errorMessage?.let { err ->
                Surface(
                    color = FireColors.Error.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = err,
                        color = FireColors.Error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Campo Título (máx 100)
            FireTextField(
                value = titulo,
                onValueChange = { if (it.length <= 100) titulo = it },
                label = "Título * (máx. 100 caracteres)"
            )

            // Seletor de Data
            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                        },
                        selectedDate.year,
                        selectedDate.monthValue - 1,
                        selectedDate.dayOfMonth
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("📅 Data: $dateFormated", fontWeight = FontWeight.Medium)
            }

            // Horários (Apenas se for Evento)
            if (!isTask) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, m -> horaInicio = LocalTime.of(h, m) },
                                horaInicio?.hour ?: 9,
                                horaInicio?.minute ?: 0,
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = horaInicio?.let { "⏰ Início: %02d:%02d".format(it.hour, it.minute) } ?: "⏰ Início",
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, m -> horaFim = LocalTime.of(h, m) },
                                horaFim?.hour ?: 10,
                                horaFim?.minute ?: 0,
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = horaFim?.let { "⏰ Fim: %02d:%02d".format(it.hour, it.minute) } ?: "⏰ Fim",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (!isTask) {
                FireTextField(
                    value = local,
                    onValueChange = { local = it },
                    label = "Local (Endereço)",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = if (local.isNotBlank()) {
                        {
                            IconButton(onClick = {
                                val uri = Uri.parse("geo:0,0?q=${Uri.encode(local)}")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(local)}")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                }
                            }) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Abrir no Maps",
                                    tint = FireColors.Primary
                                )
                            }
                        }
                    } else null
                )
            }

            // Descrição (máx 500)
            FireTextField(
                value = descricao,
                onValueChange = { if (it.length <= 500) descricao = it },
                label = "Descrição (opcional, máx. 500 caracteres)",
                modifier = Modifier.heightIn(min = 80.dp)
            )

            // Paleta de 12 Cores
            Text("🎨 Selecionar Cor:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PALETA_12_CORES.forEach { hex ->
                    val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(FireColors.Primary)
                    val isSelected = hex.equals(selectedColorHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColorHex = hex }
                    )
                }
            }

            
            // Seleção de Escopo (Global vs Específico)
            if (availableEscalas.isNotEmpty()) {
                var scopeSelection by remember { mutableStateOf(if (selectedEscalaId == null) 0 else 1) }
                
                            Text("Escopo:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            androidx.compose.material3.ScrollableTabRow(
                                selectedTabIndex = scopeSelection,
                                modifier = Modifier.fillMaxWidth(),
                                edgePadding = 0.dp
                            ) {
                                androidx.compose.material3.Tab(
                                    selected = scopeSelection == 0,
                                    onClick = {
                                        scopeSelection = 0
                                        selectedEscalaId = null
                                    },
                                    text = { Text("Global") }
                                )
                                androidx.compose.material3.Tab(
                                    selected = scopeSelection == 1,
                                    onClick = {
                                        scopeSelection = 1
                                        if (availableEscalas.isNotEmpty()) {
                                            selectedEscalaId = availableEscalas.first().id
                                        }
                                    },
                                    text = { Text("Especifico") }
                                )
                            }

                
                if (scopeSelection == 1) {
                    var expandEscala by remember { mutableStateOf(false) }
                    val escalaName = availableEscalas.find { it.id == selectedEscalaId }?.nome ?: "Selecione a Escala"
                    ExposedDropdownMenuBox(
                        expanded = expandEscala,
                        onExpandedChange = { expandEscala = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = escalaName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione a Escala") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandEscala) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandEscala,
                            onDismissRequest = { expandEscala = false }
                        ) {
                            availableEscalas.forEach { esc ->
                                DropdownMenuItem(
                                    text = { Text(esc.nome) },
                                    onClick = { selectedEscalaId = esc.id; expandEscala = false }
                                )
                            }
                        }
                    }
                }
            }
                         // Lembretes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔔 Lembretes:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                TextButton(onClick = { selectedReminders = selectedReminders + 0 }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Adicionar Lembrete")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedReminders.isEmpty()) {
                    Text("Nenhum lembrete configurado (sem aviso)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    selectedReminders.forEachIndexed { idx, min ->
                        var expandedDropdown by remember { mutableStateOf(false) }
                        val currentOptionLabel = OPCOES_LEMBRETE_DROPDOWN.find { it.second == min }?.first ?: "$min minutos antes"
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { expandedDropdown = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(currentOptionLabel)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                
                                DropdownMenu(
                                    expanded = expandedDropdown,
                                    onDismissRequest = { expandedDropdown = false }
                                ) {
                                    OPCOES_LEMBRETE_DROPDOWN.forEach { (label, value) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                val copy = selectedReminders.toMutableList()
                                                copy[idx] = value
                                                selectedReminders = copy
                                                expandedDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            IconButton(onClick = {
                                val copy = selectedReminders.toMutableList()
                                copy.removeAt(idx)
                                selectedReminders = copy
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = FireColors.Error)
                            }
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
                            Icon(Icons.Default.Delete, contentDescription = "Remover Subtarefa", tint = FireColors.Error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botão Salvar
            Button(
                onClick = {
                    // Validações
                    if (titulo.isBlank()) {
                        errorMessage = "O título é obrigatório"
                        return@Button
                    }
                    if (!isTask) {
                        if (horaInicio != null && horaFim == null) {
                            errorMessage = "Se o horário de início for preenchido, o horário de fim é obrigatório"
                            return@Button
                        }
                        if (horaFim != null && horaInicio == null) {
                            errorMessage = "Se o horário de fim for preenchido, o horário de início é obrigatório"
                            return@Button
                        }
                        if (horaInicio != null && horaFim != null && !horaFim!!.isAfter(horaInicio)) {
                            errorMessage = "O horário de fim deve ser posterior ao horário de início"
                            return@Button
                        }
                    }

                    val dateStr = selectedDate.toString()

                    if (isTask) {
                        onSaveTarefa(
                            titulo.trim(),
                            dateStr,
                            descricao.trim(),
                            selectedColorHex,
                            selectedReminders,
                            subtarefas.filter { it.titulo.isNotBlank() },
                            selectedEscalaId
                        )
                    } else {
                        val horaInicioStr = horaInicio?.let { "%02d:%02d".format(it.hour, it.minute) }
                        val horaFimStr = horaFim?.let { "%02d:%02d".format(it.hour, it.minute) }
                        onSaveEvento(
                            titulo.trim(),
                            dateStr,
                            horaInicioStr,
                            horaFimStr,
                            descricao.trim(),
                            selectedColorHex,
                            selectedReminders,
                            local.trim(),
                            selectedEscalaId
                        )
                    }
                    allowDismiss = true
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FireColors.Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
