package com.andrefdias.dailynote.ui.screens.settings

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.ui.designsystem.components.topbar.FireTopBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToWizard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Geral", "Segurança", "Backup", "Logs")
    val context = LocalContext.current

    // Launcher para Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.connectGoogleDrive(account)
            } catch (e: ApiException) {
                viewModel.setError("Falha ao autenticar: ${e.statusCode}")
            }
        }
    }

    // Launcher para recuperação de auth
    val authRecoveryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.clearRecoveryIntent()
            viewModel.fetchDriveBackups() // Tenta novamente após a permissão
        } else {
            viewModel.setError("Permissão para acessar o Drive foi negada.")
        }
    }

    LaunchedEffect(uiState.authRecoveryIntent) {
        uiState.authRecoveryIntent?.let {
            authRecoveryLauncher.launch(it)
        }
    }

    LaunchedEffect(uiState.infoMessage, uiState.errorMessage) {
        if (uiState.infoMessage != null || uiState.errorMessage != null) {
            delay(4000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            FireTopBar(
                title = "Configurações",
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mensagens de Feedback
            uiState.infoMessage?.let {
                InfoBanner(message = it, type = "success", onDismiss = { viewModel.clearMessages() })
            }
            uiState.errorMessage?.let {
                InfoBanner(message = it, type = "error", onDismiss = { viewModel.clearMessages() })
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                divider = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Conteúdo das Tabs
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> GeneralTab(
                        theme = uiState.config.tema,
                        onThemeSelected = viewModel::updateTheme,
                        onNavigateToWizard = onNavigateToWizard
                    )
                    1 -> SecurityTab(
                        pinEnabled = uiState.pinEnabled,
                        biometricEnabled = uiState.biometricEnabled,
                        lastAccessTime = uiState.lastAccessTime ?: "Nenhum acesso registrado",
                        onPinEnabledChange = { enabled ->
                            if (!enabled) viewModel.updatePin("", false)
                            else viewModel.updatePinErrorAndShowDialog() // Mostra diálogo de PIN
                        },
                        onBiometricEnabledChange = viewModel::updateBiometric,
                        pinCode = uiState.pinCode,
                        pinConfirmValue = uiState.pinConfirmValue ?: "",
                        pinError = uiState.pinError ?: "",
                        onPinChange = viewModel::updatePinCode,
                        onPinConfirmChange = viewModel::updatePinConfirm,
                        onSavePin = viewModel::savePin,
                        onClearPinError = viewModel::clearPinError,
                        onEraseDataClick = { viewModel.eraseAllData { onNavigateBack() } }
                    )
                    2 -> BackupTab(
                        isGoogleConnected = uiState.isGoogleConnected,
                        googleAccountName = uiState.googleAccountName,
                        isProcessing = uiState.isProcessing,
                        lastBackupTime = uiState.config.ultimoBackupData ?: "Nunca realizado",
                        backupFrequency = uiState.config.backupAutomatico,
                        wifiOnly = uiState.config.backupSomenteWifi,
                        onConnectClick = {
                            val signInIntent = viewModel.googleDriveBackupService.getGoogleSignInClient().signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        },
                        onDisconnectClick = viewModel::disconnectGoogleDrive,
                        onBackupClick = viewModel::performDriveBackup,
                        onRestoreClick = viewModel::fetchDriveBackups,
                        onFrequencyChange = viewModel::updateBackupFrequency,
                        onWifiOnlyChange = viewModel::updateBackupWifiOnly
                    )
                    3 -> LogsTab(
                        logLevel = uiState.logLevel,
                        logSize = uiState.logSize,
                        logContent = uiState.logContent,
                        onLevelSelected = viewModel::updateLogLevel,
                        onLoadLogs = viewModel::loadLogContent,
                        onClearLogView = viewModel::clearLogContent,
                        onClearLogsFile = viewModel::clearLogs,
                        onExportLogs = {
                            viewModel.exportLogs { uri ->
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Exportar Logs"))
                            }
                        }
                    )
                }
            }
        }

        if (uiState.showRestoreDialog) {
            RestoreDialog(
                backups = uiState.driveBackups,
                isProcessing = uiState.isProcessing,
                onDismiss = viewModel::dismissRestoreDialog,
                onRestore = { fileId ->
                    viewModel.restoreDriveBackup(fileId) {
                        // Restauração concluída
                    }
                }
            )
        }
    }
}

// ============================================
// ABAS
// ============================================

@Composable
private fun GeneralTab(
    theme: String,
    onThemeSelected: (String) -> Unit,
    onNavigateToWizard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PreferenceCard(
            title = "Aparência",
            subtitle = "Personalize o tema do aplicativo"
        ) {
            TemaSelector(
                selectedTheme = theme,
                onThemeSelected = onThemeSelected
            )
        }

        PreferenceCard(
            title = "Google Agenda",
            subtitle = "Integração e sincronização de eventos"
        ) {
            Button(
                onClick = onNavigateToWizard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Configurar Google Agenda")
            }
        }
    }
}

@Composable
private fun SecurityTab(
    pinEnabled: Boolean,
    biometricEnabled: Boolean,
    lastAccessTime: String,
    onPinEnabledChange: (Boolean) -> Unit,
    onBiometricEnabledChange: (Boolean) -> Unit,
    pinCode: String,
    pinConfirmValue: String,
    pinError: String,
    onPinChange: (String) -> Unit,
    onPinConfirmChange: (String) -> Unit,
    onSavePin: () -> Unit,
    onClearPinError: () -> Unit,
    onEraseDataClick: () -> Unit
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var showEraseConfirm by remember { mutableStateOf(false) }

    if (showEraseConfirm) {
        EraseConfirmationDialog(
            onConfirm = {
                showEraseConfirm = false
                onEraseDataClick()
            },
            onDismiss = { showEraseConfirm = false }
        )
    }

    // Gambiarra necessária para mostrar o dialog do ViewModel
    LaunchedEffect(pinError) {
        if (pinError == "SHOW_DIALOG") {
            showPinDialog = true
            onClearPinError()
        }
    }

    if (showPinDialog) {
        PinSetupDialog(
            pinValue = pinCode,
            pinConfirmValue = pinConfirmValue,
            pinError = pinError,
            onPinChange = onPinChange,
            onPinConfirmChange = onPinConfirmChange,
            onSave = {
                onSavePin()
                if (pinCode.length == 4 && pinCode == pinConfirmValue) {
                    showPinDialog = false
                }
            },
            onDismiss = {
                showPinDialog = false
                onClearPinError()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SecurityStatusCard(
            isEncrypted = pinEnabled || biometricEnabled,
            lastAccess = lastAccessTime
        )

        PreferenceCard(
            title = "Autenticação",
            subtitle = "Camadas adicionais de segurança para acessar o app"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SwitchPreferenceCard(
                    title = "Código PIN",
                    subtitle = "Exigir PIN de 4 dígitos para abrir o aplicativo",
                    checked = pinEnabled,
                    onCheckedChange = {
                        if (it) showPinDialog = true
                        else onPinEnabledChange(false)
                    }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
                
                SwitchPreferenceCard(
                    title = "Biometria",
                    subtitle = "Usar impressão digital ou reconhecimento facial",
                    checked = biometricEnabled,
                    onCheckedChange = onBiometricEnabledChange
                )
            }
        }

        PreferenceCard(
            title = "Zona de Perigo",
            subtitle = "Ações destrutivas e irreversíveis"
        ) {
            OutlinedButton(
                onClick = { showEraseConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Apagar Todos os Dados")
            }
        }
    }
}

@Composable
private fun BackupTab(
    isGoogleConnected: Boolean,
    googleAccountName: String?,
    isProcessing: Boolean,
    lastBackupTime: String,
    backupFrequency: String,
    wifiOnly: Boolean,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onFrequencyChange: (String) -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PreferenceCard(
            title = "Google Drive",
            subtitle = "Vincule sua conta para backup na nuvem"
        ) {
            if (isGoogleConnected) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Conta Vinculada",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = googleAccountName ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        OutlinedButton(
                            onClick = onDisconnectClick,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Text("Sair")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onBackupClick,
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Fazer Backup")
                        }
                        OutlinedButton(
                            onClick = onRestoreClick,
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Restaurar")
                        }
                    }
                }
            } else {
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Conectar ao Google Drive")
                }
            }
        }

        PreferenceCard(
            title = "Opções de Backup",
            subtitle = "Como os backups automáticos devem ocorrer"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Frequência Automática", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                FrequencySelector(
                    selectedFrequency = backupFrequency,
                    onFrequencySelected = onFrequencyChange
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
                
                SwitchPreferenceCard(
                    title = "Apenas em Wi-Fi",
                    subtitle = "Economiza dados móveis durante envios",
                    checked = wifiOnly,
                    onCheckedChange = onWifiOnlyChange,
                    elevation = 0.dp
                )
                
                Text(
                    text = "Último backup: $lastBackupTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun LogsTab(
    logLevel: String,
    logSize: String,
    logContent: String?,
    onLevelSelected: (String) -> Unit,
    onLoadLogs: () -> Unit,
    onClearLogView: () -> Unit,
    onClearLogsFile: () -> Unit,
    onExportLogs: () -> Unit
) {
    var showLogViewer by remember { mutableStateOf(false) }

    LaunchedEffect(showLogViewer) {
        if (showLogViewer) onLoadLogs()
        else onClearLogView()
    }

    if (showLogViewer && logContent != null) {
        LogViewerDialog(
            logContent = logContent,
            onDismiss = { showLogViewer = false },
            onExport = onExportLogs,
            onClear = {
                onClearLogsFile()
                showLogViewer = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PreferenceCard(
            title = "Nível de Registro",
            subtitle = "Define a verbosidade dos logs gravados"
        ) {
            LogLevelSelector(
                selectedLevel = logLevel,
                onLevelSelected = onLevelSelected
            )
        }

        PreferenceCard(
            title = "Arquivos de Log",
            subtitle = "Gerencie os registros do sistema (Tamanho: $logSize)"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showLogViewer = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Visualizar")
                }
                OutlinedButton(
                    onClick = onExportLogs,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Exportar")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Diagnóstico",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Os logs contêm informações de auditoria e diagnósticos para auxiliar na resolução de problemas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ============================================
// COMPONENTES AUXILIARES E DIÁLOGOS
// ============================================

@Composable
private fun PreferenceCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

@Composable
private fun SwitchPreferenceCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    elevation: androidx.compose.ui.unit.Dp = 1.dp
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun InfoBanner(
    message: String,
    type: String,
    onDismiss: () -> Unit
) {
    val (bgColor, textColor) = when (type) {
        "success" -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        else -> Pair(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar",
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SecurityStatusCard(
    isEncrypted: Boolean,
    lastAccess: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEncrypted)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isEncrypted)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isEncrypted) "🔒" else "🔓",
                        fontSize = 18.sp
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isEncrypted) "Dispositivo Seguro" else "Modo de Acesso Livre",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEncrypted)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Último acesso: $lastAccess",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEncrypted)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TemaSelector(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf("Automático", "Claro", "Escuro")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        themes.forEach { theme ->
            val isSelected = theme == selectedTheme
            FilterChip(
                selected = isSelected,
                onClick = { onThemeSelected(theme) },
                label = { Text(theme, style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FrequencySelector(
    selectedFrequency: String,
    onFrequencySelected: (String) -> Unit
) {
    val frequencies = listOf("Nunca", "Diário", "Semanal", "Mensal")
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedFrequency)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            frequencies.forEach { freq ->
                DropdownMenuItem(
                    text = { Text(freq) },
                    onClick = {
                        onFrequencySelected(freq)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LogLevelSelector(
    selectedLevel: String,
    onLevelSelected: (String) -> Unit
) {
    val levels = listOf("DEBUG", "INFO", "WARN", "ERROR")
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nível: $selectedLevel")
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            levels.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level) },
                    onClick = {
                        onLevelSelected(level)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PinSetupDialog(
    pinValue: String,
    pinConfirmValue: String,
    pinError: String,
    onPinChange: (String) -> Unit,
    onPinConfirmChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Configurar PIN",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Digite um PIN de 4 dígitos para proteger o aplicativo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pinValue,
                    onValueChange = {
                        if (it.length <= 4) onPinChange(it)
                    },
                    label = { Text("Novo PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(
                            text = "${pinValue.length}/4 dígitos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                OutlinedTextField(
                    value = pinConfirmValue,
                    onValueChange = {
                        if (it.length <= 4) onPinConfirmChange(it)
                    },
                    label = { Text("Confirmar PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = pinError.isNotEmpty() && pinError != "SHOW_DIALOG",
                    supportingText = {
                        if (pinError.isNotEmpty() && pinError != "SHOW_DIALOG") {
                            Text(
                                text = pinError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun EraseConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Excluir Todos os Dados?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Esta ação irá apagar permanentemente:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val items = listOf(
                        "Todas as ocorrências registradas",
                        "Dados de militares e viaturas",
                        "Configurações e banco de dados local"
                    )
                    items.forEach { item ->
                        Row {
                            Text("• ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(item, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Text(
                    text = "Esta ação não poderá ser desfeita.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Excluir Tudo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun RestoreDialog(
    backups: List<com.andrefdias.dailynote.data.service.DriveFile>,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Restaurar Backup",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
            ) {
                Text(
                    text = "Selecione um backup para restaurar:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isProcessing) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (backups.isEmpty()) {
                    Text("Nenhum backup encontrado.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(backups) { file ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onRestore(file.id) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(file.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        Text("${file.size / 1024} KB - ${file.createdTime}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Icon(Icons.Default.Restore, contentDescription = "Restaurar", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
private fun LogViewerDialog(
    logContent: String,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    onClear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Visualizar Logs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onExport, modifier = Modifier.weight(1f)) { Text("Exportar") }
                    OutlinedButton(
                        onClick = onClear,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Limpar") }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (logContent.isEmpty() || logContent == "Nenhum log disponível") {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhum log disponível")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), reverseLayout = true) {
                            item {
                                Text(
                                    text = logContent,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Fechar") }
        }
    )
}
