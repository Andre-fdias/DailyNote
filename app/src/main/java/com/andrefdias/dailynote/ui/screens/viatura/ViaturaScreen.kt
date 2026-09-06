package com.andrefdias.dailynote.ui.screens.viatura

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.ui.unit.sp
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
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

    val isDark = com.andrefdias.dailynote.ui.designsystem.colors.FireColors.isDarkState
    val topBarColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF1E1E1E) else androidx.compose.ui.graphics.Color(0xFFFAFAFA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastro de Viaturas") },
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
                            viewModel.selectViatura(null)
                            showDialog = true
                        },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .padding(end = 16.dp)
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        val viaturaColor = when (viatura.tipo) {
                            "AT" -> Color(0xFFE53935)
                            "UR" -> Color(0xFF1E88E5)
                            "ABS" -> Color(0xFFFB8C00)
                            "COM" -> Color(0xFF8E24AA)
                            else -> Color(0xFF757575)
                        }
                        val viaturaEmoji = when (viatura.tipo) {
                            "AT" -> "🔥"
                            "UR" -> "⚕️"
                            "ABS" -> "🚘"
                            "COM" -> "📡"
                            else -> "🚒"
                        }
                        val tipoFull = when (viatura.tipo) {
                            "AT" -> "Auto Tanque"
                            "UR" -> "Unidade de Resgate"
                            "ABS" -> "Auto Bomba Salvamento"
                            "COM" -> "Centro de Comunicação"
                            else -> "Outros"
                        }

                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(6.dp)
                                    .background(viaturaColor)
                            )
                            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Left side info
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(viaturaColor, RoundedCornerShape(12.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = viaturaEmoji, fontSize = 28.sp)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = viatura.prefixo, 
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(viaturaColor, RoundedCornerShape(16.dp))
                                                            .padding(horizontal = 8.dp, vertical = 2.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = viatura.tipo, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = tipoFull, 
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = "Unidade: ${viatura.unidade}", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Posto: ${viatura.posto}", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Atendimento: ${viatura.tipoAtendimento}", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // Right side image & 3-dots
                                    Column(horizontalAlignment = Alignment.End) {
                                        var expandedMenu by remember { mutableStateOf(false) }
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
                                                        viewModel.selectViatura(viatura)
                                                        showDialog = true 
                                                    },
                                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Excluir", color = Color(0xFFEF5350)) },
                                                    onClick = { 
                                                        expandedMenu = false
                                                        viewModel.deleteViatura(viatura) 
                                                    },
                                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF5350)) }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        val imgRes = when(viatura.tipoAtendimento) {
                                            "Resgate" -> com.andrefdias.dailynote.R.drawable.viatura_ur
                                            "Incêndio" -> com.andrefdias.dailynote.R.drawable.viatura_at
                                            "Salvamento" -> com.andrefdias.dailynote.R.drawable.viatura_abs
                                            "Administrativa" -> {
                                                if (viatura.status.equals("Telegrafia", ignoreCase = true)) {
                                                    com.andrefdias.dailynote.R.drawable.viatura_telegrafia
                                                } else if (viatura.tipo.equals("VO", ignoreCase = true)) {
                                                    com.andrefdias.dailynote.R.drawable.viatura_abs
                                                } else {
                                                    com.andrefdias.dailynote.R.drawable.viatura_com
                                                }
                                            }
                                            else -> com.andrefdias.dailynote.R.drawable.viatura_at
                                        }
                                        Image(
                                            painter = painterResource(id = imgRes),
                                            contentDescription = "Imagem da Viatura",
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(80.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val statusColor = when(viatura.status) {
                                        "Operacional" -> Color(0xFF4CAF50)
                                        "Manutenção", "Baixada" -> Color(0xFFEF5350)
                                        "Em ocorrência" -> Color(0xFFFF9800)
                                        "Reserva" -> Color(0xFF9E9E9E)
                                        "Ativo" -> Color(0xFF2196F3)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(statusColor, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = viatura.status,
                                                color = statusColor,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                
                                }
                            }
                        }
                    }
                }
                
                // Add the Legend at the bottom of the list
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "AT" to Color(0xFFE53935),
                            "UR" to Color(0xFF1E88E5),
                            "ABS" to Color(0xFFFB8C00),
                            "COM" to Color(0xFF8E24AA),
                            "Outros" to Color(0xFF757575)
                        ).forEach { (label, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(8.dp).background(color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
