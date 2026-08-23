package com.andrefdias.dailynote.ui.screens.equipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.domain.model.EquipeViatura
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipeServicoScreen(
    viewModel: EquipeServicoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCadastrarViatura: () -> Unit,
    onNavigateToCadastrarMilitar: () -> Unit,
    onNavigateToCadastrarQuartel: () -> Unit
) {
    val data by viewModel.data.collectAsState()
    val unidade by viewModel.unidade.collectAsState()
    val posto by viewModel.posto.collectAsState()
    
    val unidades by viewModel.unidades.collectAsState()
    val postos by viewModel.postos.collectAsState()
    val viaturasDisponiveis by viewModel.viaturasDisponiveis.collectAsState()
    val militares by viewModel.militares.collectAsState()
    
    val equipeServico by viewModel.equipeServico.collectAsState()
    val equipesHoje by viewModel.equipesTrabalhandoHoje.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(viewModel.validationEvent) {
        viewModel.validationEvent.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    var expandedUnidade by remember { mutableStateOf(false) }
    var expandedPosto by remember { mutableStateOf(false) }
    var expandedEscala by remember { mutableStateOf(false) }
    var expandedAddViatura by remember { mutableStateOf(false) }
    var showCadastroBottomSheet by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val tiposEscala by viewModel.opcoesTipoEscala.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            LocalDate.parse(data)
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        } catch(e: Exception) { null }
    )

    val displayData = try {
        LocalDate.parse(data).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        data
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.setFiltros(localDate.toString(), unidade, posto)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCadastroBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showCadastroBottomSheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Cadastros Auxiliares", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                ListItem(
                    headlineContent = { Text("Unidade/Posto") },
                    leadingContent = { Icon(Icons.Outlined.Domain, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showCadastroBottomSheet = false
                        onNavigateToCadastrarQuartel()
                    }
                )
                ListItem(
                    headlineContent = { Text("Viatura") },
                    leadingContent = { Icon(Icons.Outlined.DirectionsCar, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showCadastroBottomSheet = false
                        onNavigateToCadastrarViatura()
                    }
                )
                ListItem(
                    headlineContent = { Text("Militar (Efetivo)") },
                    leadingContent = { Icon(Icons.Outlined.People, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showCadastroBottomSheet = false
                        onNavigateToCadastrarMilitar()
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equipe de Serviço") },
                actions = {
                    if (equipeServico != null) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Limpar Escala", tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { 
                            viewModel.saveEquipe()
                            onNavigateBack() 
                        }) {
                            Icon(Icons.Filled.Check, contentDescription = "Salvar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        
        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Limpar Escala") },
                text = { Text("Tem certeza que deseja apagar toda a escala do dia para esta unidade? Esta ação não pode ser desfeita.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.limparEscala()
                        showClearDialog = false
                        onNavigateBack()
                    }) {
                        Text("Sim, Apagar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CABEÇALHO
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF232D42)),
                    border = BorderStroke(0.5.dp, Color(0xFF37474F))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Configuração Diária", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        
                        OutlinedTextField(
                            value = displayData,
                            onValueChange = { },
                            label = { Text("Data (DD/MM/YYYY)") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Selecionar Data")
                                }
                            }
                        )
                        
                        if (equipesHoje.isNotEmpty()) {
                            Text("Escalas Hoje: ${equipesHoje.joinToString { it.nome }}", color = MaterialTheme.colorScheme.primary)
                        } else {
                            Text("Nenhuma escala mapeada para hoje.", color = MaterialTheme.colorScheme.secondary)
                        }

                        // Select Unidade
                        ExposedDropdownMenuBox(
                            expanded = expandedUnidade,
                            onExpandedChange = { expandedUnidade = !expandedUnidade }
                        ) {
                            OutlinedTextField(
                                value = unidade,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unidade") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnidade) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedUnidade,
                                onDismissRequest = { expandedUnidade = false }
                            ) {
                                unidades.forEach { uni ->
                                    DropdownMenuItem(
                                        text = { Text(uni) },
                                        onClick = {
                                            viewModel.setFiltros(data, uni, "")
                                            expandedUnidade = false
                                        }
                                    )
                                }
                            }
                        }

                        // Select Posto
                        ExposedDropdownMenuBox(
                            expanded = expandedPosto,
                            onExpandedChange = { expandedPosto = !expandedPosto }
                        ) {
                            OutlinedTextField(
                                value = posto,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Posto") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPosto) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                enabled = postos.isNotEmpty()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedPosto,
                                onDismissRequest = { expandedPosto = false }
                            ) {
                                postos.forEach { pst ->
                                    DropdownMenuItem(
                                        text = { Text(pst) },
                                        onClick = {
                                            viewModel.setFiltros(data, unidade, pst)
                                            expandedPosto = false
                                        }
                                    )
                                }
                            }
                        }

                        equipeServico?.let { eq ->
                            ExposedDropdownMenuBox(
                                expanded = expandedEscala,
                                onExpandedChange = { expandedEscala = !expandedEscala }
                            ) {
                                OutlinedTextField(
                                    value = eq.tipoEscala,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Tipo de Escala") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEscala) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedEscala,
                                    onDismissRequest = { expandedEscala = false }
                                ) {
                                    val tiposEquipe = tiposEscala.filter { it != "DEJEM" && it != "12 Horas" && it != "24 Horas" }
                                    tiposEquipe.forEach { tipo ->
                                        DropdownMenuItem(
                                            text = { Text(tipo) },
                                            onClick = {
                                                viewModel.setTipoEscala(tipo)
                                                expandedEscala = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // CORPO
            if (equipeServico != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Viaturas Empenhadas", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        
                        ExposedDropdownMenuBox(
                            expanded = expandedAddViatura,
                            onExpandedChange = { expandedAddViatura = !expandedAddViatura }
                        ) {
                            Button(
                                onClick = { expandedAddViatura = true },
                                modifier = Modifier.menuAnchor()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Adicionar")
                                Spacer(Modifier.width(4.dp))
                                Text("Viatura")
                            }
                            ExposedDropdownMenu(
                                expanded = expandedAddViatura,
                                onDismissRequest = { expandedAddViatura = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("+ CADASTRAR NOVA VIATURA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                    onClick = {
                                        expandedAddViatura = false
                                        onNavigateToCadastrarViatura()
                                    }
                                )
                                Divider()
                                viaturasDisponiveis.forEach { v ->
                                    DropdownMenuItem(
                                        text = { Text("${v.prefixo} - ${v.tipoAtendimento}") },
                                        onClick = {
                                            viewModel.addViatura(v.id)
                                            expandedAddViatura = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                items(equipeServico!!.viaturas, key = { it.id }) { eqViatura ->
                    ViaturaCard(
                        eqViatura = eqViatura,
                        militares = militares,
                        tiposEscala = tiposEscala,
                        onUpdateViatura = { viewModel.updateViatura(it) },
                        onRemoveViatura = { viewModel.removeViatura(eqViatura.id) },
                        onCadastrarMilitar = onNavigateToCadastrarMilitar,
                        onSaveEquipe = { 
                            val ctx = context
                            viewModel.saveEquipe() 
                            android.widget.Toast.makeText(ctx, "Equipe salva com sucesso!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilitarSelectField(
    label: String,
    selectedId: String?,
    militares: List<com.andrefdias.dailynote.domain.model.Militar>,
    onSelect: (String) -> Unit,
    onCadastrar: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedMilitar = militares.find { it.id == selectedId }
    val display = selectedMilitar?.let { "${it.graduacao} ${it.nomeGuerra}" } ?: "Selecionar..."

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("+ CADASTRAR NOVO MILITAR", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                onClick = {
                    expanded = false
                    onCadastrar()
                }
            )
            Divider()
            militares.forEach { m ->
                DropdownMenuItem(
                    text = { Text("${m.graduacao} ${m.nomeGuerra} (RE: ${m.re})") },
                    onClick = {
                        onSelect(m.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViaturaCard(
    eqViatura: com.andrefdias.dailynote.domain.model.EquipeViatura,
    militares: List<com.andrefdias.dailynote.domain.model.Militar>,
    tiposEscala: List<String>,
    onUpdateViatura: (com.andrefdias.dailynote.domain.model.EquipeViatura) -> Unit,
    onRemoveViatura: () -> Unit,
    onCadastrarMilitar: () -> Unit,
    onSaveEquipe: () -> Unit
) {
    var showAddMilitar by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF232D42)),
        border = BorderStroke(0.5.dp, Color(0xFF37474F))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = eqViatura.viatura?.prefixo ?: "Viatura Desconhecida",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRemoveViatura) {
                    Icon(Icons.Default.Close, contentDescription = "Remover", tint = Color(0xFFEF5350))
                }
            }

            if (eqViatura.militaresEscalados.isNotEmpty()) {
                Divider(color = Color(0xFF37474F), thickness = 0.5.dp)
            }

            // Lista de militares
            eqViatura.militaresEscalados.forEach { me ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${me.funcao}: ${me.militar?.graduacao ?: ""} ${me.militar?.nomeGuerra ?: ""}", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        val details = if (me.tipoEscala == "DEJEM") "DEJEM (${me.dejemHorarioInicio} - ${me.dejemHorarioFim})" else me.tipoEscala
                        Text(text = details, style = MaterialTheme.typography.bodySmall, color = Color(0xFF90A4AE), modifier = Modifier.padding(start = 16.dp))
                    }
                    IconButton(onClick = {
                        val novaLista = eqViatura.militaresEscalados.filter { it.id != me.id }
                        onUpdateViatura(eqViatura.copy(militaresEscalados = novaLista))
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Remover Militar", tint = Color(0xFF90A4AE))
                    }
                }
            }

            if (showAddMilitar) {
                MilitarEscaladoForm(
                    militares = militares,
                    tiposEscala = tiposEscala,
                    onSave = { me ->
                        val novaLista = eqViatura.militaresEscalados + me
                        onUpdateViatura(eqViatura.copy(militaresEscalados = novaLista))
                        showAddMilitar = false
                    },
                    onCancel = { showAddMilitar = false },
                    onCadastrarMilitar = onCadastrarMilitar
                )
            } else {
                TextButton(
                    onClick = { showAddMilitar = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Adicionar Militar")
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { 
                    onSaveEquipe() 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar Viatura")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilitarEscaladoForm(
    militares: List<com.andrefdias.dailynote.domain.model.Militar>,
    tiposEscala: List<String>,
    onSave: (com.andrefdias.dailynote.domain.model.MilitarEscalado) -> Unit,
    onCancel: () -> Unit,
    onCadastrarMilitar: () -> Unit
) {
    var militarId by remember { mutableStateOf("") }
    var funcao by remember { mutableStateOf("Motorista") }
    var tipoEscala by remember { mutableStateOf("") }
    var dejemHorarioInicio by remember { mutableStateOf("") }
    var dejemHorarioFim by remember { mutableStateOf("") }

    LaunchedEffect(tiposEscala) {
        if (tipoEscala.isBlank() && tiposEscala.isNotEmpty()) {
            tipoEscala = tiposEscala.firstOrNull { it != "DEJEM" } ?: tiposEscala.first()
        }
    }

    var expandedFuncao by remember { mutableStateOf(false) }
    val funcoes = listOf("Comandante", "Motorista", "Auxiliar", "Telegrafista")

    var expandedEscala by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Novo Militar", style = MaterialTheme.typography.titleSmall)

        MilitarSelectField(
            label = "Militar",
            selectedId = militarId.takeIf { it.isNotBlank() },
            militares = militares,
            onSelect = { militarId = it },
            onCadastrar = onCadastrarMilitar
        )

        ExposedDropdownMenuBox(
            expanded = expandedFuncao,
            onExpandedChange = { expandedFuncao = !expandedFuncao }
        ) {
            OutlinedTextField(
                value = funcao,
                onValueChange = {},
                readOnly = true,
                label = { Text("Função") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFuncao) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedFuncao,
                onDismissRequest = { expandedFuncao = false }
            ) {
                funcoes.forEach { f ->
                    DropdownMenuItem(
                        text = { Text(f) },
                        onClick = {
                            funcao = f
                            expandedFuncao = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = expandedEscala,
            onExpandedChange = { expandedEscala = !expandedEscala }
        ) {
            OutlinedTextField(
                value = tipoEscala,
                onValueChange = {},
                readOnly = true,
                label = { Text("Escala / Equipe") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEscala) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedEscala,
                onDismissRequest = { expandedEscala = false }
            ) {
                tiposEscala.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo) },
                        onClick = {
                            tipoEscala = tipo
                            if (tipo != "DEJEM") {
                                dejemHorarioInicio = ""
                                dejemHorarioFim = ""
                            }
                            expandedEscala = false
                        }
                    )
                }
            }
        }

        if (tipoEscala == "DEJEM") {
            OutlinedTextField(
                value = dejemHorarioInicio,
                onValueChange = { newValue ->
                    val digits = newValue.filter { it.isDigit() }
                    var formatted = ""
                    for (i in digits.indices) {
                        if (i == 2) formatted += ":"
                        formatted += digits[i]
                        if (formatted.length == 5) break
                    }
                    dejemHorarioInicio = formatted

                    if (formatted.length == 5) {
                        try {
                            val time = java.time.LocalTime.parse(formatted)
                            dejemHorarioFim = time.plusHours(8).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                        } catch (e: Exception) {
                            dejemHorarioFim = ""
                        }
                    } else {
                        dejemHorarioFim = ""
                    }
                },
                label = { Text("Horário Início (HH:MM)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
            if (dejemHorarioFim.isNotBlank()) {
                Text("Término calculado: $dejemHorarioFim", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text("Cancelar") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    if (militarId.isNotBlank()) {
                        val parts = tipoEscala.split(" - ")
                        onSave(
                            com.andrefdias.dailynote.domain.model.MilitarEscalado(
                                militarId = militarId,
                                funcao = funcao,
                                tipoEscala = tipoEscala,
                                dejemHorarioInicio = dejemHorarioInicio.takeIf { it.isNotBlank() },
                                dejemHorarioFim = dejemHorarioFim.takeIf { it.isNotBlank() }
                            )
                        )
                    }
                },
                enabled = militarId.isNotBlank() && (tipoEscala != "DEJEM" || (dejemHorarioInicio.length == 5))
            ) {
                Text("Salvar Militar")
            }
        }
    }
}
