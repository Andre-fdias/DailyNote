package com.andrefdias.dailynote.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrefdias.dailynote.data.local.AppDatabase
import com.andrefdias.dailynote.data.local.dao.ConfiguracaoDao
import com.andrefdias.dailynote.data.local.entities.RoomConfiguracao
import com.andrefdias.dailynote.data.service.GoogleDriveBackupService
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class SettingsUiState(
    val config: RoomConfiguracao = RoomConfiguracao(),
    val isGoogleConnected: Boolean = false,
    val googleAccountName: String? = null,
    val driveBackups: List<com.andrefdias.dailynote.data.service.DriveFile> = emptyList(),
    val isProcessing: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val showRestoreDialog: Boolean = false,
    val authRecoveryIntent: android.content.Intent? = null,

    // Security states
    val pinCode: String = "",
    val pinConfirmValue: String? = null,
    val pinError: String? = null,
    val pinEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val lastAccessTime: String? = null,

    // Log Management states
    val logLevel: String = "INFO",
    val logSize: String = "0 KB",
    val logContent: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configuracaoDao: ConfiguracaoDao,
    val googleDriveBackupService: GoogleDriveBackupService,
    private val settingsRepository: com.andrefdias.dailynote.domain.repository.SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        checkGoogleConnection()
        loadPinSettings()
        calculateLogSize()
        loadLastAccessTime()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            configuracaoDao.getConfiguracaoFlow().collect { config ->
                config?.let { c ->
                    _uiState.update { it.copy(config = c) }
                }
            }
        }
    }

    private fun checkGoogleConnection() {
        val account = googleDriveBackupService.getLastSignedInAccount()
        _uiState.update {
            it.copy(
                isGoogleConnected = account != null,
                googleAccountName = account?.email
            )
        }
    }

    private fun loadPinSettings() {
        viewModelScope.launch {
            settingsRepository.pinEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(pinEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.pinCodeFlow.collect { code ->
                _uiState.update { it.copy(pinCode = code) }
            }
        }
        viewModelScope.launch {
            settingsRepository.biometricEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(biometricEnabled = enabled) }
            }
        }
    }

    private fun loadLastAccessTime() {
        val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        val lastAccess = prefs.getString("last_access_time", null)
        _uiState.update { it.copy(lastAccessTime = lastAccess) }
    }

    fun saveLastAccessTime() {
        val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        prefs.edit().putString("last_access_time", timestamp).apply()
        _uiState.update { it.copy(lastAccessTime = timestamp) }
    }

    // --- PIN Management ---
    fun updatePinCode(pin: String) {
        _uiState.update { it.copy(pinCode = pin, pinError = null) }
    }

    fun updatePinConfirm(pin: String) {
        _uiState.update { it.copy(pinConfirmValue = pin, pinError = null) }
    }

    fun updatePinErrorAndShowDialog() {
        _uiState.update { it.copy(pinError = "SHOW_DIALOG") }
    }

    fun savePin() {
        val state = _uiState.value
        if (state.pinCode.length != 4) {
            _uiState.update { it.copy(pinError = "O PIN deve ter exatamente 4 dígitos.") }
            return
        }
        if (state.pinCode != state.pinConfirmValue) {
            _uiState.update { it.copy(pinError = "Os PINs não coincidem.") }
            return
        }
        updatePin(state.pinCode, true)
    }

    fun clearPinError() {
        _uiState.update { it.copy(pinError = null) }
    }

    fun updatePin(pin: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPinCode(pin)
            settingsRepository.setPinEnabled(enabled)
            _uiState.update {
                it.copy(
                    infoMessage = if (enabled) "PIN de segurança ativado com sucesso." else "PIN desativado.",
                    pinCode = pin,
                    pinEnabled = enabled,
                    pinConfirmValue = null,
                    pinError = null
                )
            }
            appendLog("INFO", if (enabled) "PIN ativado" else "PIN desativado")
        }
    }

    fun updateBiometric(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometricEnabled(enabled)
            _uiState.update {
                it.copy(
                    infoMessage = if (enabled) "Autenticação biométrica ativada." else "Biometria desativada.",
                    biometricEnabled = enabled
                )
            }
            appendLog("INFO", if (enabled) "Biometria ativada" else "Biometria desativada")
        }
    }

    // --- Google Drive Actions ---
    fun connectGoogleDrive(account: GoogleSignInAccount) {
        _uiState.update {
            it.copy(
                isGoogleConnected = true,
                googleAccountName = account.email,
                infoMessage = "Google Drive conectado com sucesso!"
            )
        }
        appendLog("INFO", "Google Drive conectado: ${account.email}")
    }

    fun disconnectGoogleDrive() {
        viewModelScope.launch {
            googleDriveBackupService.getGoogleSignInClient().signOut()
            _uiState.update {
                it.copy(
                    isGoogleConnected = false,
                    googleAccountName = null,
                    driveBackups = emptyList(),
                    infoMessage = "Google Drive desconectado."
                )
            }
            appendLog("INFO", "Google Drive desconectado")
        }
    }

    fun clearRecoveryIntent() {
        _uiState.update { it.copy(authRecoveryIntent = null) }
    }

    fun fetchDriveBackups() {
        val account = googleDriveBackupService.getLastSignedInAccount()
        if (account == null) {
            _uiState.update { it.copy(errorMessage = "Conecte sua conta do Google Drive primeiro.") }
            return
        }

        _uiState.update { it.copy(isProcessing = true, infoMessage = null, errorMessage = null) }
        viewModelScope.launch {
            try {
                val token = getGoogleAccessToken(account)
                googleDriveBackupService.listBackupsFromDrive(token)
                    .onSuccess { list ->
                        _uiState.update { it.copy(driveBackups = list, showRestoreDialog = true, isProcessing = false) }
                        appendLog("INFO", "Lista de backups recuperada. Total: ${list.size}")
                    }
                    .onFailure { error ->
                        val msg = "Falha ao listar backups: ${error.localizedMessage}"
                        _uiState.update { it.copy(isProcessing = false, errorMessage = msg) }
                        appendLog("ERROR", msg)
                    }
            } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
                _uiState.update { it.copy(isProcessing = false, authRecoveryIntent = e.intent, errorMessage = "Permissão do Google Drive necessária.") }
            } catch (e: Exception) {
                val msg = "Erro de autenticação do Google: ${e.localizedMessage}"
                _uiState.update { it.copy(isProcessing = false, errorMessage = msg) }
                appendLog("ERROR", msg)
            }
        }
    }

    fun performDriveBackup() {
        val account = googleDriveBackupService.getLastSignedInAccount()
        if (account == null) {
            _uiState.update { it.copy(errorMessage = "Conecte sua conta do Google Drive primeiro.") }
            return
        }

        _uiState.update { it.copy(isProcessing = true, infoMessage = null, errorMessage = null) }
        viewModelScope.launch {
            try {
                val token = getGoogleAccessToken(account)
                googleDriveBackupService.uploadBackupToDrive(token)
                    .onSuccess { log ->
                        val timestamp = java.time.LocalDateTime.now().toString()
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                infoMessage = "Backup enviado para o Drive com sucesso!",
                                config = it.config.copy(ultimoBackupData = timestamp)
                            )
                        }
                        appendLog("INFO", "Backup enviado para o Drive com sucesso.")
                    }
                    .onFailure { error ->
                        val msg = "Falha ao enviar backup: ${error.localizedMessage}"
                        _uiState.update { it.copy(isProcessing = false, errorMessage = msg) }
                        appendLog("ERROR", msg)
                    }
            } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
                _uiState.update { it.copy(isProcessing = false, authRecoveryIntent = e.intent, errorMessage = "Permissão do Google Drive necessária.") }
            } catch (e: Exception) {
                val msg = "Erro ao acessar o Drive: ${e.localizedMessage}"
                _uiState.update { it.copy(isProcessing = false, errorMessage = msg) }
                appendLog("ERROR", msg)
            }
        }
    }

    fun restoreDriveBackup(fileId: String, onRestored: () -> Unit) {
        val account = googleDriveBackupService.getLastSignedInAccount()
        if (account == null) return

        _uiState.update { it.copy(isProcessing = true, showRestoreDialog = false) }
        viewModelScope.launch {
            try {
                val token = getGoogleAccessToken(account)
                googleDriveBackupService.restoreBackupFromDrive(token, fileId)
                    .onSuccess {
                        _uiState.update { it.copy(isProcessing = false, infoMessage = "Restauração concluída!") }
                        appendLog("INFO", "Backup restaurado com sucesso. (ID: $fileId)")
                        onRestored()
                    }
                    .onFailure { error ->
                        val msg = "Falha ao restaurar dados: ${error.localizedMessage}"
                        _uiState.update { it.copy(isProcessing = false, errorMessage = msg) }
                        appendLog("ERROR", msg)
                    }
            } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
                _uiState.update { it.copy(isProcessing = false, authRecoveryIntent = e.intent, errorMessage = "Permissão do Google Drive necessária.") }
            } catch (e: Exception) {
                val msg = "Erro de restauração: ${e.localizedMessage}"
                _uiState.update { it.copy(isProcessing = false, errorMessage = msg) }
                appendLog("ERROR", msg)
            }
        }
    }

    private suspend fun getGoogleAccessToken(account: GoogleSignInAccount): String = withContext(Dispatchers.IO) {
        GoogleAuthUtil.getToken(
            context,
            account.account ?: throw IllegalStateException("Conta sem e-mail do sistema"),
            "oauth2:https://www.googleapis.com/auth/drive.appdata"
        )
    }

    fun dismissRestoreDialog() {
        _uiState.update { it.copy(showRestoreDialog = false) }
    }

    // --- Configurações Gerais ---
    fun updateTheme(tema: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(tema)
            val newConfig = _uiState.value.config.copy(tema = tema)
            configuracaoDao.insertConfiguracao(newConfig)
            _uiState.update { it.copy(infoMessage = "Tema atualizado para: $tema") }
            appendLog("INFO", "Tema alterado para $tema")
        }
    }

    fun updateBackupFrequency(frequency: String) {
        viewModelScope.launch {
            val newConfig = _uiState.value.config.copy(backupAutomatico = frequency)
            configuracaoDao.insertConfiguracao(newConfig)
            _uiState.update { it.copy(infoMessage = "Frequência de backup atualizada.") }
            appendLog("INFO", "Frequência de backup alterada para $frequency")
        }
    }

    fun updateBackupWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch {
            val newConfig = _uiState.value.config.copy(backupSomenteWifi = wifiOnly)
            configuracaoDao.insertConfiguracao(newConfig)
            _uiState.update { it.copy(infoMessage = if (wifiOnly) "Backup apenas em Wi-Fi ativado." else "Backup em qualquer rede ativado.") }
        }
    }

    // --- Log Management ---
    private fun calculateLogSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val logFile = File(context.cacheDir, "dailynotes_logs.txt")
            val sizeStr = if (logFile.exists()) {
                val sizeBytes = logFile.length()
                when {
                    sizeBytes > 1024 * 1024 -> "%.2f MB".format(sizeBytes.toFloat() / (1024 * 1024))
                    sizeBytes > 1024 -> "${sizeBytes / 1024} KB"
                    else -> "$sizeBytes B"
                }
            } else {
                "0 B"
            }
            _uiState.update { it.copy(logSize = sizeStr) }
        }
    }

    fun loadLogContent() {
        viewModelScope.launch(Dispatchers.IO) {
            val logFile = File(context.cacheDir, "dailynotes_logs.txt")
            val content = if (logFile.exists()) {
                logFile.readText().take(50000)
            } else {
                "Nenhum log disponível"
            }
            _uiState.update { it.copy(logContent = content) }
        }
    }

    fun clearLogContent() {
        _uiState.update { it.copy(logContent = null) }
    }

    fun updateLogLevel(level: String) {
        _uiState.update {
            it.copy(
                logLevel = level,
                infoMessage = "Nível de log alterado para: $level"
            )
        }
        appendLog("INFO", "Nível de log alterado para $level")
    }

    fun appendLog(level: String, message: String) {
        val currentLevel = _uiState.value.logLevel
        if (currentLevel == "ERROR" && level != "ERROR") return
        if (currentLevel == "WARN" && (level != "ERROR" && level != "WARN")) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val logFile = File(context.cacheDir, "dailynotes_logs.txt")
                val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                val entry = "[$timestamp] [$level] $message\n"
                logFile.appendText(entry)
                calculateLogSize()
            } catch (_: Exception) { }
        }
    }

    fun clearLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            val logFile = File(context.cacheDir, "dailynotes_logs.txt")
            if (logFile.exists()) logFile.delete()
            _uiState.update {
                it.copy(
                    logSize = "0 B",
                    infoMessage = "Arquivo de logs limpo.",
                    logContent = null
                )
            }
        }
    }

    fun exportLogs(onShared: (Uri) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val logFile = File(context.cacheDir, "dailynotes_logs.txt")
                if (!logFile.exists()) {
                    logFile.writeText("DAILY NOTES - LOG DE AUDITORIA\n")
                    logFile.appendText("Inicializado em: ${java.time.LocalDateTime.now()}\n")
                }
                
                val exportFile = File(context.cacheDir, "dailynotes_logs_export.txt")
                exportFile.writeText("--- METADADOS DO SISTEMA ---\n")
                exportFile.appendText("Nível de Log: ${_uiState.value.logLevel}\n")
                exportFile.appendText("PIN Ativo: ${_uiState.value.pinEnabled}\n")
                exportFile.appendText("Biometria Ativa: ${_uiState.value.biometricEnabled}\n")
                exportFile.appendText("Backup Automático: ${_uiState.value.config.backupAutomatico}\n")
                exportFile.appendText("----------------------------\n\n")
                exportFile.appendText(logFile.readText())

                val logUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "com.andrefdias.dailynote.fileprovider",
                    exportFile
                )
                withContext(Dispatchers.Main) {
                    onShared(logUri)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro ao exportar logs: ${e.localizedMessage}") }
            }
        }
    }

    // --- Erase All Data ---
    fun eraseAllData(onErased: () -> Unit) {
        _uiState.update { it.copy(isProcessing = true) }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                AppDatabase.closeDatabase()
                val dbFile = context.getDatabasePath("dailynote.db")
                if (dbFile.exists()) dbFile.delete()
                File(dbFile.path + "-shm").delete()
                File(dbFile.path + "-wal").delete()

                val baseDir = context.getExternalFilesDir(null)
                if (baseDir != null && baseDir.exists()) {
                    baseDir.deleteRecursively()
                }

                settingsRepository.setPinEnabled(false)
                settingsRepository.setPinCode("")
                settingsRepository.setBiometricEnabled(false)
                settingsRepository.setTheme("Automático")
                context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE).edit().clear().apply()

                appendLog("WARN", "Todos os dados foram apagados por comando do usuário")
            }.onSuccess {
                _uiState.update { it.copy(isProcessing = false, infoMessage = "Todos os dados foram excluídos.") }
                withContext(Dispatchers.Main) { onErased() }
            }.onFailure { error ->
                _uiState.update { it.copy(isProcessing = false, errorMessage = "Falha ao excluir dados: ${error.localizedMessage}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(infoMessage = null, errorMessage = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
        appendLog("ERROR", message)
    }
}
