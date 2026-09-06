package com.andrefdias.dailynote.ui.screens.militar

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
import com.andrefdias.dailynote.domain.model.GraduacaoMilitar
import com.andrefdias.dailynote.domain.model.situacoesMilitar
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image

fun getInsigniaDrawable(graduacao: String): Int {
    return when (graduacao) {
        GraduacaoMilitar.CEL_PM.display -> com.andrefdias.dailynote.R.drawable.insignia_cel_pm
        GraduacaoMilitar.TEN_CEL_PM.display -> com.andrefdias.dailynote.R.drawable.insignia_ten_cel_pm
        GraduacaoMilitar.MAJOR_PM.display -> com.andrefdias.dailynote.R.drawable.insignia_major_pm
        GraduacaoMilitar.CAPITAO_PM.display -> com.andrefdias.dailynote.R.drawable.insignia_capitao_pm
        GraduacaoMilitar.PRIMEIRO_TEN_PM.display,
        GraduacaoMilitar.PRIMEIRO_TEN_QAPM.display -> com.andrefdias.dailynote.R.drawable.insignia_primeiro_ten_pm
        GraduacaoMilitar.SEGUNDO_TEN_PM.display,
        GraduacaoMilitar.SEGUNDO_TEN_QAPM.display -> com.andrefdias.dailynote.R.drawable.insignia_segundo_ten_pm
        GraduacaoMilitar.ASPIRANTE_PM.display -> com.andrefdias.dailynote.R.drawable.insignia_aspirante_pm
        GraduacaoMilitar.SUBTEN_PM.display -> com.andrefdias.dailynote.R.drawable.insignia_subten_pm
        GraduacaoMilitar.PRIMEIRO_SGT_PM.display -> com.andrefdias.dailynote.R.drawable.insignia_primeiro_sgt_pm
        GraduacaoMilitar.SEGUNDO_SGT_PM.display -> com.andrefdias.dailynote.R.drawable.insignia_segundo_sgt_pm
        GraduacaoMilitar.TERCEIRO_SGT_PM.display -> com.andrefdias.dailynote.R.drawable.insignia_terceiro_sgt_pm
        GraduacaoMilitar.CABO_PM.display -> com.andrefdias.dailynote.R.drawable.insignia_cabo_pm
        GraduacaoMilitar.SOLDADO_PM.display -> com.andrefdias.dailynote.R.drawable.insignia_soldado_pm
        else -> com.andrefdias.dailynote.R.drawable.insignia_soldado_pm
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilitarScreen(
    viewModel: MilitarViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val militares by viewModel.militares.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    var expandedGraduacao by remember { mutableStateOf(false) }
    var expandedSituacao by remember { mutableStateOf(false) }

    val isDark = com.andrefdias.dailynote.ui.designsystem.colors.FireColors.isDarkState
    val topBarColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF1E1E1E) else androidx.compose.ui.graphics.Color(0xFFFAFAFA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastro de Militares") },
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
                            viewModel.selectMilitar(null)
                            showDialog = true
                        },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .scale(scale)
                            .size(36.dp)
                            .background(Color(0xFFFF9800), androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Militar", tint = Color.White, modifier = Modifier.size(20.dp))
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
                items(militares, key = { it.id }) { militar ->
                    var expandedMenu by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            val situacaoColor = if (militar.situacao == "Ativo" || militar.situacao == "Pronto") Color(0xFF4CAF50) else Color(0xFFEF5350)
                            val bordaColor = when {
                                militar.graduacao.contains("CEL", ignoreCase = true) ||
                                militar.graduacao.contains("MAJOR", ignoreCase = true) ||
                                militar.graduacao.contains("CAPITÃO", ignoreCase = true) ||
                                militar.graduacao.contains("TENENTE", ignoreCase = true) ||
                                militar.graduacao.contains("ASPIRANTE", ignoreCase = true) ||
                                militar.graduacao.contains("COMANDANTE", ignoreCase = true) -> Color(0xFF1976D2) // Azul
                                
                                militar.graduacao.contains("SUBTEN", ignoreCase = true) ||
                                militar.graduacao.contains("SGT", ignoreCase = true) ||
                                militar.graduacao.contains("SUBTENENTE", ignoreCase = true) ||
                                militar.graduacao.contains("SARGENTO", ignoreCase = true) -> Color(0xFFD32F2F) // Vermelha
                                
                                militar.graduacao.contains("CABO", ignoreCase = true) ||
                                militar.graduacao.contains("SOLDADO", ignoreCase = true) ||
                                militar.graduacao.contains("ALUNO", ignoreCase = true) -> Color(0xFF212121) // Preta
                                
                                else -> Color(0xFF757575)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(8.dp)
                                    .background(bordaColor)
                            )
                            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${militar.graduacao} ${militar.nomeGuerra}", 
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "RE: ${militar.re}", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = militar.nomeCompleto, 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(situacaoColor.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = militar.situacao,
                                                    color = situacaoColor,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            
                                            if (militar.mergulhador) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFF2196F3).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "Merg",
                                                        color = Color(0xFF2196F3),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            if (militar.ovb != "Não Habilitado" && militar.ovb.isNotBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFFFF9800).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "OVB ${militar.ovb}",
                                                        color = Color(0xFFFF9800),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
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
                                                        viewModel.selectMilitar(militar)
                                                        showDialog = true 
                                                    },
                                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Excluir", color = Color(0xFFEF5350)) },
                                                    onClick = { 
                                                        expandedMenu = false
                                                        viewModel.deleteMilitar(militar) 
                                                    },
                                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF5350)) }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Image(
                                            painter = painterResource(id = getInsigniaDrawable(militar.graduacao)),
                                            contentDescription = "Insígnia ${militar.graduacao}",
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Fit
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
                        text = if (uiState.isEditing) "Editar Militar" else "Novo Militar",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.re,
                            onValueChange = { viewModel.updateForm(it, uiState.digito, uiState.nomeCompleto, uiState.nomeGuerra, uiState.graduacao, uiState.situacao, uiState.mergulhador, uiState.ovb) },
                            label = { Text("RE (6 núm)") },
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                        OutlinedTextField(
                            value = uiState.digito,
                            onValueChange = { viewModel.updateForm(uiState.re, it, uiState.nomeCompleto, uiState.nomeGuerra, uiState.graduacao, uiState.situacao, uiState.mergulhador, uiState.ovb) },
                            label = { Text("Dígito") },
                            singleLine = true,
                            modifier = Modifier.weight(0.3f)
                        )
                    }

                    OutlinedTextField(
                        value = uiState.nomeCompleto,
                        onValueChange = { viewModel.updateForm(uiState.re, uiState.digito, it, uiState.nomeGuerra, uiState.graduacao, uiState.situacao, uiState.mergulhador, uiState.ovb) },
                        label = { Text("Nome Completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.nomeGuerra,
                        onValueChange = { viewModel.updateForm(uiState.re, uiState.digito, uiState.nomeCompleto, it, uiState.graduacao, uiState.situacao, uiState.mergulhador, uiState.ovb) },
                        label = { Text("Nome de Guerra") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Select Graduação
                    ExposedDropdownMenuBox(
                        expanded = expandedGraduacao,
                        onExpandedChange = { expandedGraduacao = !expandedGraduacao }
                    ) {
                        OutlinedTextField(
                            value = uiState.graduacao,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Graduação/Posto") },
                            leadingIcon = {
                                if (uiState.graduacao.isNotBlank()) {
                                    Image(
                                        painter = painterResource(id = getInsigniaDrawable(uiState.graduacao)),
                                        contentDescription = "Insígnia",
                                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGraduacao) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedGraduacao,
                            onDismissRequest = { expandedGraduacao = false }
                        ) {
                            GraduacaoMilitar.values().forEach { grad ->
                                DropdownMenuItem(
                                    text = { Text(grad.display) },
                                    leadingIcon = {
                                        Image(
                                            painter = painterResource(id = getInsigniaDrawable(grad.display)),
                                            contentDescription = "Insígnia ${grad.display}",
                                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateForm(uiState.re, uiState.digito, uiState.nomeCompleto, uiState.nomeGuerra, grad.display, uiState.situacao, uiState.mergulhador, uiState.ovb)
                                        expandedGraduacao = false
                                    }
                                )
                            }
                        }
                    }

                    // Select Situação
                    ExposedDropdownMenuBox(
                        expanded = expandedSituacao,
                        onExpandedChange = { expandedSituacao = !expandedSituacao }
                    ) {
                        OutlinedTextField(
                            value = uiState.situacao,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Situação") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSituacao) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSituacao,
                            onDismissRequest = { expandedSituacao = false }
                        ) {
                            situacoesMilitar.forEach { situacao ->
                                DropdownMenuItem(
                                    text = { Text(situacao) },
                                    onClick = {
                                        viewModel.updateForm(uiState.re, uiState.digito, uiState.nomeCompleto, uiState.nomeGuerra, uiState.graduacao, situacao, uiState.mergulhador, uiState.ovb)
                                        expandedSituacao = false
                                    }
                                )
                            }
                        }
                    }

                    // Mergulhador Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mergulhador")
                        Switch(
                            checked = uiState.mergulhador,
                            onCheckedChange = { viewModel.updateForm(uiState.re, uiState.digito, uiState.nomeCompleto, uiState.nomeGuerra, uiState.graduacao, uiState.situacao, it, uiState.ovb) }
                        )
                    }

                    // Select OVB
                    var expandedOvb by remember { mutableStateOf(false) }
                    val ovbOptions = listOf("Não Habilitado", "Leve", "Pesado")
                    ExposedDropdownMenuBox(
                        expanded = expandedOvb,
                        onExpandedChange = { expandedOvb = !expandedOvb }
                    ) {
                        OutlinedTextField(
                            value = uiState.ovb,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("OVB") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedOvb) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedOvb,
                            onDismissRequest = { expandedOvb = false }
                        ) {
                            ovbOptions.forEach { ovbOption ->
                                DropdownMenuItem(
                                    text = { Text(ovbOption) },
                                    onClick = {
                                        viewModel.updateForm(uiState.re, uiState.digito, uiState.nomeCompleto, uiState.nomeGuerra, uiState.graduacao, uiState.situacao, uiState.mergulhador, ovbOption)
                                        expandedOvb = false
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
                            viewModel.saveMilitar()
                            if (uiState.error == null && uiState.re.isNotBlank()) {
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
