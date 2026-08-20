package com.andrefdias.dailynote.ui.screens.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andrefdias.dailynote.domain.calendar.GoogleCalendarSyncManager
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.components.buttons.FireButton
import com.andrefdias.dailynote.ui.designsystem.components.cards.FireCard
import com.andrefdias.dailynote.ui.designsystem.components.topbar.FireTopBar
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import kotlinx.coroutines.launch

@Composable
fun GoogleSyncScreen(
    syncManager: GoogleCalendarSyncManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isConnected by remember { mutableStateOf(false) }
    var accountEmail by remember { mutableStateOf("") }
    var syncStatus by remember { mutableStateOf("") }
    var isSyncing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FireTopBar(
                title = "🔌 Google Agenda Sync",
                onBackClick = onNavigateBack,
                backgroundColor = FireColors.Surface
            )
        },
        containerColor = FireColors.Background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(FireSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(FireSpacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FireCard {
                Text(
                    text = "Integração Bidirecional",
                    style = FireTypography.Title,
                    fontWeight = FontWeight.Bold,
                    color = FireColors.Primary
                )
                Text(
                    text = "Vincule sua conta operacional do Google para espelhar as escalas e tarefas de serviço do Fire Notes diretamente na sua Agenda oficial do celular.",
                    style = FireTypography.BodyMedium,
                    color = FireColors.OnSurfaceVariant
                )
            }

            FireCard {
                Text(
                    text = "Status da Conexão",
                    style = FireTypography.BodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(FireSpacing.Small))

                if (isConnected) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CONECTADO", color = FireColors.Primary, fontWeight = FontWeight.Bold, style = FireTypography.BodyMedium)
                            Text(accountEmail, style = FireTypography.Caption, color = FireColors.OnSurfaceVariant)
                        }
                        TextButton(onClick = {
                            isConnected = false
                            accountEmail = ""
                            syncStatus = ""
                        }) {
                            Text("Desconectar", color = FireColors.Error)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DESCONECTADO", color = FireColors.OnSurfaceVariant, fontWeight = FontWeight.Bold, style = FireTypography.BodyMedium)
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isSyncing = true
                                    syncStatus = "Conectando à conta Google..."
                                    val result = syncManager.connectAccount()
                                    isSyncing = false
                                    if (result.isSuccess) {
                                        isConnected = true
                                        accountEmail = result.getOrThrow()
                                        syncStatus = "Conta conectada com sucesso!"
                                    } else {
                                        syncStatus = "Falha ao conectar conta."
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FireColors.Primary)
                        ) {
                            Text("Conectar", color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isConnected) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(FireSpacing.Medium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FireCard {
                        Text(
                            text = "Ações de Sincronização",
                            style = FireTypography.BodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(FireSpacing.Small))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(FireSpacing.Small),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FireButton(
                                text = "Sincronizar Escala",
                                onClick = {
                                    coroutineScope.launch {
                                        isSyncing = true
                                        syncStatus = "Sincronizando escala..."
                                        val result = syncManager.syncScales()
                                        isSyncing = false
                                        syncStatus = if (result.isSuccess) "Escala sincronizada!" else "Falha ao sincronizar."
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FireButton(
                                text = "Sincronizar Eventos",
                                onClick = {
                                    coroutineScope.launch {
                                        isSyncing = true
                                        syncStatus = "Sincronizando eventos..."
                                        val result = syncManager.syncEvents()
                                        isSyncing = false
                                        syncStatus = if (result.isSuccess) "Eventos sincronizados!" else "Falha ao sincronizar."
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(FireSpacing.Small))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(FireSpacing.Small),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FireButton(
                                text = "Sincronizar Tarefas",
                                onClick = {
                                    coroutineScope.launch {
                                        isSyncing = true
                                        syncStatus = "Sincronizando tarefas..."
                                        val result = syncManager.syncTasks()
                                        isSyncing = false
                                        syncStatus = if (result.isSuccess) "Tarefas sincronizadas!" else "Falha ao sincronizar."
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FireButton(
                                text = "Sincronizar Agenda",
                                onClick = {
                                    coroutineScope.launch {
                                        isSyncing = true
                                        syncStatus = "Sincronizando agenda..."
                                        val result = syncManager.syncAgenda()
                                        isSyncing = false
                                        syncStatus = if (result.isSuccess) "Agenda sincronizada!" else "Falha ao sincronizar."
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (isSyncing || syncStatus.isNotBlank()) {
                FireCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = FireColors.Primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(syncStatus, style = FireTypography.BodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
