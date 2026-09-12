package com.andrefdias.dailynote.ui.screens.calendar

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andrefdias.dailynote.domain.calendar.GoogleCalendarSyncManager
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.components.buttons.FireButton
import com.andrefdias.dailynote.ui.designsystem.components.cards.FireCard
import com.andrefdias.dailynote.ui.designsystem.components.topbar.FireTopBar
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun GoogleSyncScreen(
    syncManager: GoogleCalendarSyncManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(syncManager.getLastSignedInAccount() != null) }
    var accountEmail by remember { mutableStateOf(syncManager.getLastSignedInAccount()?.email ?: "") }
    var syncStatus by remember { mutableStateOf("") }
    var isSyncing by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                coroutineScope.launch {
                    isSyncing = true
                    syncStatus = "Autenticando..."
                    val connectResult = syncManager.connectAccount(account)
                    isSyncing = false
                    if (connectResult.isSuccess) {
                        isConnected = true
                        accountEmail = account.email ?: ""
                        syncStatus = "Conta conectada com sucesso!"
                    } else {
                        syncStatus = "Falha ao obter permissão: ${connectResult.exceptionOrNull()?.localizedMessage}"
                    }
                }
            } catch (e: ApiException) {
                syncStatus = "Falha ao autenticar: ${e.statusCode}"
            }
        } else {
            syncStatus = "Login cancelado."
        }
    }

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
                            val client = syncManager.getGoogleSignInClient()
                            client.signOut().addOnCompleteListener {
                                isConnected = false
                                accountEmail = ""
                                syncStatus = "Desconectado."
                            }
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
                                val intent = syncManager.getGoogleSignInClient().signInIntent
                                googleSignInLauncher.launch(intent)
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
                                text = "Sincronizar Agora",
                                onClick = {
                                    coroutineScope.launch {
                                        isSyncing = true
                                        syncStatus = "Sincronizando agenda..."
                                        val account = syncManager.getLastSignedInAccount()
                                        if (account != null) {
                                            val tokenResult = syncManager.connectAccount(account)
                                            if (tokenResult.isSuccess) {
                                                val token = tokenResult.getOrThrow()
                                                val syncResult = syncManager.syncEvents(token)
                                                if (syncResult.isSuccess) {
                                                    syncStatus = "Agenda sincronizada com sucesso!"
                                                } else {
                                                    syncStatus = "Falha: ${syncResult.exceptionOrNull()?.localizedMessage}"
                                                }
                                            } else {
                                                syncStatus = "Erro de autenticação."
                                            }
                                        } else {
                                            syncStatus = "Conta desconectada."
                                        }
                                        isSyncing = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
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
