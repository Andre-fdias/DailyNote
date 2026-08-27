package com.andrefdias.dailynote.ui.screens.quartel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuartelScreen(
    viewModel: QuartelViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val quarteis by viewModel.quarteis.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastro de Quartel") },
                actions = {
                    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isPressed) 0.8f else 1f, label = "scale")
                    
                    IconButton(
                        onClick = {
                            viewModel.selectQuartel(null)
                            showDialog = true
                        },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .scale(scale)
                            .size(36.dp)
                            .background(Color(0xFFFF9800), androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Quartel", tint = Color.White, modifier = Modifier.size(20.dp))
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
                items(quarteis, key = { it.id }) { quartel ->
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
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    androidx.compose.foundation.layout.Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFF90A4AE).copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Build, 
                                            contentDescription = null, 
                                            tint = Color(0xFF90A4AE)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = quartel.unidade, 
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${quartel.posto} - ${quartel.municipio}", 
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF90A4AE)
                                        )
                                    }
                                }
                                Row {
                                    IconButton(onClick = {
                                        viewModel.selectQuartel(quartel)
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF90A4AE))
                                    }
                                    IconButton(onClick = { viewModel.deleteQuartel(quartel) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFEF5350))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            ModalBottomSheet(
                onDismissRequest = { showDialog = false },
                modifier = Modifier.windowInsetsPadding(WindowInsets.ime) // So keyboard pushes it up properly
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (uiState.isEditing) "Editar Quartel" else "Novo Quartel",
                        style = MaterialTheme.typography.titleLarge
                    )
                    OutlinedTextField(
                        value = uiState.unidade,
                        onValueChange = { viewModel.updateForm(it, uiState.posto, uiState.municipio) },
                        label = { Text("Unidade (Ex: 15º GB)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.posto,
                        onValueChange = { viewModel.updateForm(uiState.unidade, it, uiState.municipio) },
                        label = { Text("Posto (Ex: São Roque)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.municipio,
                        onValueChange = { viewModel.updateForm(uiState.unidade, uiState.posto, it) },
                        label = { Text("Município (Ex: Sorocaba)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                            viewModel.saveQuartel()
                            if (uiState.unidade.isNotBlank() && uiState.posto.isNotBlank()) {
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
