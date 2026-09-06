package com.andrefdias.dailynote.ui.screens.ocorrencias

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.util.Naturezas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroOcorrenciaScreen(
    viewModel: CadastroOcorrenciaViewModel = hiltViewModel(),
    onNavigateToOpcoes: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                          permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                viewModel.buscarLocalizacao(context)
            }
        }
    )

    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            viewModel.buscarLocalizacao(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    if (state.savedSuccess) {
        LaunchedEffect(Unit) {
            val navId = state.savedOcorrenciaId ?: ""
            onNavigateToOpcoes(navId)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Nova Ocorrência", fontWeight = FontWeight.Bold) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Dados Iniciais Automáticos e Talão
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp), 
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Informações Básicas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = state.data,
                            onValueChange = {},
                            label = { Text("Data") },
                            readOnly = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = state.hora,
                            onValueChange = {},
                            label = { Text("Hora") },
                            readOnly = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    if (state.equipeServicoNome.isNotEmpty()) {
                        val (equipeLabel, equipeColor) = com.andrefdias.dailynote.util.EquipeUtils.parseEquipeInfo(state.equipeServicoNome)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = equipeColor.copy(alpha = 0.1f),
                            contentColor = equipeColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(equipeColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Prontidão Ativa: $equipeLabel", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.talao,
                        onValueChange = { viewModel.updateTalao(it) },
                        label = { Text("Número do Talão") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null) }
                    )
                }
            }

            // 2. Classificação
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp), 
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Category, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Classificação", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }

                    // Natureza
                    var expandedNatureza by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedNatureza,
                        onExpandedChange = { expandedNatureza = !expandedNatureza }
                    ) {
                        OutlinedTextField(
                            value = state.naturezaSelecionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Natureza da Ocorrência") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNatureza) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedNatureza,
                            onDismissRequest = { expandedNatureza = false },
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            Naturezas.listaCompleta.forEach { natureza ->
                                DropdownMenuItem(
                                    text = { Text(natureza) },
                                    onClick = {
                                        viewModel.updateNatureza(natureza)
                                        expandedNatureza = false
                                    }
                                )
                            }
                        }
                    }

                    // Viatura
                    var expandedViatura by remember { mutableStateOf(false) }
                    val selectedVtrName = state.viaturasDisponiveis.find { it.viatura?.id == state.viaturaSelecionadaId }?.viatura?.prefixo ?: ""

                    ExposedDropdownMenuBox(
                        expanded = expandedViatura,
                        onExpandedChange = { expandedViatura = !expandedViatura }
                    ) {
                        OutlinedTextField(
                            value = selectedVtrName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Viatura Responsável") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedViatura) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedViatura,
                            onDismissRequest = { expandedViatura = false },
                            modifier = Modifier.heightIn(max = 250.dp)
                        ) {
                            if (state.viaturasDisponiveis.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Nenhuma viatura cadastrada") },
                                    onClick = { expandedViatura = false }
                                )
                            } else {
                                state.viaturasDisponiveis.forEach { equipeVtr ->
                                    val prefixo = equipeVtr.viatura?.prefixo ?: "Desconhecida"
                                    DropdownMenuItem(
                                        text = { Text(prefixo) },
                                        onClick = {
                                            viewModel.updateViatura(equipeVtr.viatura?.id ?: "")
                                            expandedViatura = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // 3. Localização (GPS)
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp), 
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Map, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Localização", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        
                        FilledTonalButton(
                            onClick = { viewModel.travarGps(!state.gpsTravado) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (state.gpsTravado) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (state.gpsTravado) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = if (state.gpsTravado) Icons.Filled.GpsOff else Icons.Filled.GpsFixed,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (state.gpsTravado) "Destravar" else "Travar GPS")
                        }
                    }

                    OutlinedTextField(
                        value = state.rua,
                        onValueChange = { viewModel.updateEndereco(it, state.numero, state.bairro, state.cidade) },
                        label = { Text("Rua/Via") },
                        readOnly = !state.gpsTravado,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = state.numero,
                            onValueChange = { viewModel.updateEndereco(state.rua, it, state.bairro, state.cidade) },
                            label = { Text("Número") },
                            readOnly = !state.gpsTravado,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = state.bairro,
                            onValueChange = { viewModel.updateEndereco(state.rua, state.numero, it, state.cidade) },
                            label = { Text("Bairro") },
                            readOnly = !state.gpsTravado,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = state.cidade,
                        onValueChange = { viewModel.updateEndereco(state.rua, state.numero, state.bairro, it) },
                        label = { Text("Cidade") },
                        readOnly = !state.gpsTravado,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Button placed at the bottom of the scrollable column
            Button(
                onClick = { viewModel.salvarOcorrencia() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Save, contentDescription = "Salvar")
                Spacer(modifier = Modifier.width(12.dp))
                Text("CADASTRAR OCORRÊNCIA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
