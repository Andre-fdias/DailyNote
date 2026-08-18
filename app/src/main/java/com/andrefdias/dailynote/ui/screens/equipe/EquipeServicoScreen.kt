package com.andrefdias.dailynote.ui.screens.equipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
                        IconButton(onClick = { 
                            viewModel.saveEquipe()
                            onNavigateBack() 
                        }) {
                            Icon(Icons.Filled.Check, contentDescription = "Salvar")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCadastroBottomSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Cadastros")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CABEÇALHO
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Configuração Diária", style = MaterialTheme.typography.titleMedium)
                        
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
                                    tiposEscala.forEach { tipo ->
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

                            if (eq.tipoEscala == "DEJEM") {
                                OutlinedTextField(
                                    value = eq.dejemHorarioInicio ?: "",
                                    onValueChange = { viewModel.setDejemHorario(it) },
                                    label = { Text("Horário de Início (HH:MM)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (eq.dejemHorarioFim != null) {
                                    Text("Término calculado: ${eq.dejemHorarioFim}", color = MaterialTheme.colorScheme.primary)
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
                        Text("Viaturas Empenhadas", style = MaterialTheme.typography.titleMedium)
                        
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
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = eqViatura.viatura?.prefixo ?: "Viatura Desconhecida",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = { viewModel.removeViatura(eqViatura.id) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remover")
                                }
                            }

                            // Comandante
                            MilitarSelectField(
                                label = "Comandante",
                                selectedId = eqViatura.comandanteId,
                                militares = militares,
                                onSelect = { id -> viewModel.updateViatura(eqViatura.copy(comandanteId = id, comandante = militares.find { it.id == id })) },
                                onCadastrar = onNavigateToCadastrarMilitar
                            )

                            // Motorista
                            MilitarSelectField(
                                label = "Motorista",
                                selectedId = eqViatura.motoristaId,
                                militares = militares,
                                onSelect = { id -> viewModel.updateViatura(eqViatura.copy(motoristaId = id, motorista = militares.find { it.id == id })) },
                                onCadastrar = onNavigateToCadastrarMilitar
                            )

                            // Auxiliares
                            eqViatura.auxiliaresIds.forEachIndexed { index, auxId ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        MilitarSelectField(
                                            label = "Auxiliar ${index + 1}",
                                            selectedId = auxId.takeIf { it.isNotBlank() },
                                            militares = militares,
                                            onSelect = { id -> 
                                                val newAux = eqViatura.auxiliaresIds.toMutableList()
                                                newAux[index] = id
                                                viewModel.updateViatura(eqViatura.copy(auxiliaresIds = newAux))
                                            },
                                            onCadastrar = onNavigateToCadastrarMilitar
                                        )
                                    }
                                    IconButton(onClick = {
                                        val newAux = eqViatura.auxiliaresIds.toMutableList()
                                        newAux.removeAt(index)
                                        viewModel.updateViatura(eqViatura.copy(auxiliaresIds = newAux))
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remover Auxiliar")
                                    }
                                }
                            }

                            TextButton(
                                onClick = {
                                    val newAux = eqViatura.auxiliaresIds.toMutableList()
                                    newAux.add("") // Add empty placeholder
                                    viewModel.updateViatura(eqViatura.copy(auxiliaresIds = newAux))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Adicionar Auxiliar")
                            }

                            Spacer(Modifier.height(8.dp))
                            val context = androidx.compose.ui.platform.LocalContext.current
                            Button(
                                onClick = { 
                                    viewModel.saveEquipe() 
                                    android.widget.Toast.makeText(context, "Equipe salva com sucesso!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Salvar Viatura")
                            }
                        }
                    }
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
