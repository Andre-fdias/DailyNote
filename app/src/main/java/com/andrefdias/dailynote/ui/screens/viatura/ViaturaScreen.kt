package com.andrefdias.dailynote.ui.screens.viatura

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke

val tiposAtendimento = listOf("Resgate", "Incêndio", "Salvamento", "Administrativa")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViaturaScreen(
    viewModel: ViaturaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val viaturas by viewModel.viaturas.collectAsState()
    val unidades by viewModel.unidades.collectAsState()
    val postos by viewModel.postos.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    var expandedUnidade by remember { mutableStateOf(false) }
    var expandedPosto by remember { mutableStateOf(false) }
    var expandedTipoAtendimento by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastro de Viaturas") },
                actions = {
                    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isPressed) 0.8f else 1f, label = "scale")
                    
                    IconButton(
                        onClick = {
                            viewModel.selectViatura(null)
                            showDialog = true
                        },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .scale(scale)
                            .size(36.dp)
                            .background(Color(0xFFFF9800), androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Viatura", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viaturas, key = { it.id }) { viatura ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF232D42)),
                        border = BorderStroke(0.5.dp, Color(0xFF37474F))
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${viatura.prefixo} (${viatura.tipo})", 
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Atendimento: ${viatura.tipoAtendimento}", 
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF90A4AE)
                                    )
                                    Text(
                                        text = "Local: ${viatura.unidade} - ${viatura.posto}", 
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF90A4AE)
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        viewModel.selectViatura(viatura)
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF90A4AE))
                                    }
                                    IconButton(onClick = { viewModel.deleteViatura(viatura) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFEF5350))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val statusColor = when(viatura.status) {
                                "Operacional" -> Color(0xFF4CAF50)
                                "Manutenção", "Baixada" -> Color(0xFFEF5350)
                                "Em ocorrência" -> Color(0xFFFF9800)
                                else -> Color(0xFF90A4AE)
                            }
                            Box(
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = viatura.status,
                                    color = statusColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            ModalBottomSheet(
                onDismissRequest = { showDialog = false },
                modifier = Modifier.windowInsetsPadding(WindowInsets.ime)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (uiState.isEditing) "Editar Viatura" else "Nova Viatura",
                        style = MaterialTheme.typography.titleLarge
                    )
                    OutlinedTextField(
                        value = uiState.prefixo,
                        onValueChange = { viewModel.updateForm(it, uiState.tipoAtendimento, uiState.unidade, uiState.posto, uiState.status) },
                        label = { Text("Prefixo (Ex: ABSR-15101)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.tipo,
                        onValueChange = {},
                        label = { Text("Tipo (Automático)") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Select Unidade
                    ExposedDropdownMenuBox(
                        expanded = expandedUnidade,
                        onExpandedChange = { expandedUnidade = !expandedUnidade }
                    ) {
                        OutlinedTextField(
                            value = uiState.unidade,
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
                            unidades.forEach { unidade ->
                                DropdownMenuItem(
                                    text = { Text(unidade) },
                                    onClick = {
                                        viewModel.updateForm(uiState.prefixo, uiState.tipoAtendimento, unidade, "", uiState.status)
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
                            value = uiState.posto,
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
                            postos.forEach { posto ->
                                DropdownMenuItem(
                                    text = { Text(posto) },
                                    onClick = {
                                        viewModel.updateForm(uiState.prefixo, uiState.tipoAtendimento, uiState.unidade, posto, uiState.status)
                                        expandedPosto = false
                                    }
                                )
                            }
                        }
                    }

                    // Select Tipo Atendimento
                    ExposedDropdownMenuBox(
                        expanded = expandedTipoAtendimento,
                        onExpandedChange = { expandedTipoAtendimento = !expandedTipoAtendimento }
                    ) {
                        OutlinedTextField(
                            value = uiState.tipoAtendimento,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Atendimento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoAtendimento) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTipoAtendimento,
                            onDismissRequest = { expandedTipoAtendimento = false }
                        ) {
                            tiposAtendimento.forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo) },
                                    onClick = {
                                        viewModel.updateForm(uiState.prefixo, tipo, uiState.unidade, uiState.posto, uiState.status)
                                        expandedTipoAtendimento = false
                                    }
                                )
                            }
                        }
                    }

                    // Select Status
                    var expandedStatus by remember { mutableStateOf(false) }
                    val statusOptions = listOf("Operacional", "Indisponível", "Manutenção", "Reserva", "Em ocorrência", "Baixada", "Aguardando...", "Telegrafia")
                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = !expandedStatus }
                    ) {
                        OutlinedTextField(
                            value = uiState.status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            statusOptions.forEach { statusOption ->
                                DropdownMenuItem(
                                    text = { Text(statusOption) },
                                    onClick = {
                                        viewModel.updateForm(uiState.prefixo, uiState.tipoAtendimento, uiState.unidade, uiState.posto, statusOption)
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }

                    if (uiState.error != null) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            if (viewModel.saveViatura()) {
                                showDialog = false
                            }
                        }) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }
}
