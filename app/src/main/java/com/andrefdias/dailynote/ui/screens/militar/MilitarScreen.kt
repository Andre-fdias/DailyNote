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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastro de Militares") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.selectMilitar(null)
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Militar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(militares, key = { it.id }) { militar ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "${militar.graduacao} ${militar.nomeGuerra}", style = MaterialTheme.typography.titleMedium)
                                Text(text = "RE: ${militar.re}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = militar.nomeCompleto, style = MaterialTheme.typography.bodySmall)
                                Text(text = "Situação: ${militar.situacao}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                IconButton(onClick = {
                                    viewModel.selectMilitar(militar)
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { viewModel.deleteMilitar(militar) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir")
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
                            onValueChange = { viewModel.updateForm(it, uiState.digito, uiState.nomeCompleto, uiState.nomeGuerra, uiState.graduacao, uiState.situacao) },
                            label = { Text("RE (6 núm)") },
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                        OutlinedTextField(
                            value = uiState.digito,
                            onValueChange = { viewModel.updateForm(uiState.re, it, uiState.nomeCompleto, uiState.nomeGuerra, uiState.graduacao, uiState.situacao) },
                            label = { Text("Dígito") },
                            singleLine = true,
                            modifier = Modifier.weight(0.3f)
                        )
                    }

                    OutlinedTextField(
                        value = uiState.nomeCompleto,
                        onValueChange = { viewModel.updateForm(uiState.re, uiState.digito, it, uiState.nomeGuerra, uiState.graduacao, uiState.situacao) },
                        label = { Text("Nome Completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.nomeGuerra,
                        onValueChange = { viewModel.updateForm(uiState.re, uiState.digito, uiState.nomeCompleto, it, uiState.graduacao, uiState.situacao) },
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
                                    onClick = {
                                        viewModel.updateForm(uiState.re, uiState.digito, uiState.nomeCompleto, uiState.nomeGuerra, grad.display, uiState.situacao)
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
                                        viewModel.updateForm(uiState.re, uiState.digito, uiState.nomeCompleto, uiState.nomeGuerra, uiState.graduacao, situacao)
                                        expandedSituacao = false
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
