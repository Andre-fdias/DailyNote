package com.andrefdias.dailynote.ui.screens.quartel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
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

    val isDark = com.andrefdias.dailynote.ui.designsystem.colors.FireColors.isDarkState
    val topBarColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF1E1E1E) else androidx.compose.ui.graphics.Color(0xFFFAFAFA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastro de Quartel") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
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
                    var expandedMenu by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(8.dp)
                                    .background(Color(0xFFE53935))
                            )
                            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color(0xFFE53935).copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "🚒", style = MaterialTheme.typography.titleMedium)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = quartel.posto, 
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = "Unidade: ${quartel.unidade}", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Município: ${quartel.municipio}", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Box {
                                            IconButton(onClick = { expandedMenu = true }, modifier = Modifier.size(24.dp).offset(x = 8.dp, y = (-8).dp)) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            DropdownMenu(
                                                expanded = expandedMenu,
                                                onDismissRequest = { expandedMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Editar") },
                                                    onClick = { 
                                                        expandedMenu = false
                                                        viewModel.selectQuartel(quartel)
                                                        showDialog = true 
                                                    },
                                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Excluir", color = Color(0xFFEF5350)) },
                                                    onClick = { 
                                                        expandedMenu = false
                                                        viewModel.deleteQuartel(quartel) 
                                                    },
                                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF5350)) }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Image(
                                            painter = painterResource(id = com.andrefdias.dailynote.R.drawable.quartel),
                                            contentDescription = "Imagem do Quartel",
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(80.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
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
