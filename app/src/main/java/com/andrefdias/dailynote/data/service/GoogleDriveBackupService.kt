package com.andrefdias.dailynote.data.service

import android.content.Context
import com.andrefdias.dailynote.data.local.AppDatabase
import com.andrefdias.dailynote.data.local.dao.ConfiguracaoDao
import com.andrefdias.dailynote.data.local.entities.RoomBackupLog
import com.andrefdias.dailynote.data.local.entities.RoomConfiguracao
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class DriveFile(val id: String, val name: String, val size: Long, val createdTime: String)

@Singleton
class GoogleDriveBackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configuracaoDao: ConfiguracaoDao
) {

    private val httpClient = OkHttpClient()

    // --- Google Sign-In Configuration for Drive AppData scope ---
    fun getGoogleSignInClient() = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.appdata"))
            .build()
    )

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    // --- ZIP local files and database ---
    suspend fun createBackupZip(outputFile: File): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            // Close database before zipping to ensure integrity
            AppDatabase.closeDatabase()

            ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zos ->
                // 1. Room encrypted Database file
                val dbFile = context.getDatabasePath("dailynote.db")
                if (dbFile.exists()) {
                    addToZip(zos, dbFile, "database.db")
                }
                val shmFile = File(dbFile.path + "-shm")
                if (shmFile.exists()) addToZip(zos, shmFile, "database.db-shm")
                val walFile = File(dbFile.path + "-wal")
                if (walFile.exists()) addToZip(zos, walFile, "database.db-wal")

                // 2. Images subfolders: documentos, veiculos, evidencias, relatorios
                val baseDir = context.getExternalFilesDir(null)
                if (baseDir != null && baseDir.exists()) {
                    listOf("documentos", "veiculos", "evidencias", "relatorios").forEach { folder ->
                        val dir = File(baseDir, folder)
                        if (dir.exists()) {
                            zipDirectory(zos, dir, folder)
                        }
                    }
                }

                // 3. Config JSON file
                val configJson = File(context.cacheDir, "config.json")
                val config = configuracaoDao.getConfiguracao() ?: RoomConfiguracao()
                val configJsonData = JSONObject().apply {
                    put("tema", config.tema)
                    put("backupAutomatico", config.backupAutomatico)
                    put("backupSomenteWifi", config.backupSomenteWifi)
                }
                FileWriter(configJson).use { it.write(configJsonData.toString()) }
                addToZip(zos, configJson, "config.json")
                configJson.delete()
            }
            outputFile.length()
        }
    }

    private fun addToZip(zos: ZipOutputStream, file: File, entryPath: String) {
        FileInputStream(file).use { fis ->
            zos.putNextEntry(ZipEntry(entryPath))
            fis.copyTo(zos)
            zos.closeEntry()
        }
    }

    private fun zipDirectory(zos: ZipOutputStream, dir: File, parentPath: String) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            val entryPath = "$parentPath/${file.name}"
            if (file.isDirectory) {
                zipDirectory(zos, file, entryPath)
            } else {
                addToZip(zos, file, entryPath)
            }
        }
    }

    // --- Google Drive REST API Actions ---
    suspend fun uploadBackupToDrive(accessToken: String): Result<RoomBackupLog> = withContext(Dispatchers.IO) {
        android.util.Log.d("DailyBackup", "Iniciando upload de backup para o Google Drive")
        runCatching {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm"))
            val backupFileName = "Backup_$timestamp.zip"
            val tempZipFile = File(context.cacheDir, backupFileName)
            if (tempZipFile.exists()) tempZipFile.delete()

            android.util.Log.d("DailyBackup", "Criando arquivo ZIP local para upload: $backupFileName")
            val size = createBackupZip(tempZipFile).getOrThrow()

            // 1. Google Drive Multipart Upload request
            val metadata = JSONObject().apply {
                put("name", backupFileName)
                put("parents", listOf("appDataFolder"))
            }.toString()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                    Headers.Builder().add("Content-Type", "application/json; charset=UTF-8").build(),
                    metadata.toRequestBody("application/json; charset=UTF-8".toMediaType())
                )
                .addPart(
                    Headers.Builder().add("Content-Type", "application/zip").build(),
                    tempZipFile.asRequestBody("application/zip".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                .header("Authorization", "Bearer $accessToken")
                .post(requestBody)
                .build()

            android.util.Log.d("DailyBackup", "Enviando arquivo ZIP para API do Google Drive...")
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errMsg = "Falha no upload do Google Drive: ${response.message}"
                    android.util.Log.e("DailyBackup", errMsg)
                    throw IOException(errMsg)
                }
            }

            tempZipFile.delete()
            android.util.Log.d("DailyBackup", "Upload de backup concluído com sucesso. Tamanho: $size bytes")

            // Log Success
            val log = RoomBackupLog(
                id = UUID.randomUUID().toString(),
                dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                tipo = "Google Drive",
                status = "Sucesso",
                tamanho = size,
                mensagem = "Backup concluído com sucesso."
            )
            configuracaoDao.insertBackupLog(log)

            val currentConfig = configuracaoDao.getConfiguracao() ?: RoomConfiguracao()
            configuracaoDao.insertConfiguracao(
                currentConfig.copy(
                    ultimoBackupData = log.dataHora,
                    ultimoBackupTamanho = log.tamanho,
                    ultimoBackupStatus = "Sucesso"
                )
            )

            log
        }
    }

    suspend fun listBackupsFromDrive(accessToken: String): Result<List<DriveFile>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&fields=files(id,name,size,createdTime)&orderBy=createdTime%20desc"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Erro ao listar arquivos do Drive: ${response.message}")
                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                val filesArray = jsonResponse.optJSONArray("files") ?: return@runCatching emptyList()
                val list = mutableListOf<DriveFile>()
                for (i in 0 until filesArray.length()) {
                    val obj = filesArray.getJSONObject(i)
                    list.add(
                        DriveFile(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            size = obj.optLong("size", 0),
                            createdTime = obj.optString("createdTime", "")
                        )
                    )
                }
                list
            }
        }
    }

    suspend fun restoreBackupFromDrive(accessToken: String, fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        android.util.Log.d("DailyBackup", "Iniciando restauração de backup do Google Drive, ID do arquivo: $fileId")
        runCatching {
            val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .get()
                .build()

            android.util.Log.d("DailyBackup", "Fazendo download do backup do Google Drive...")
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errMsg = "Erro ao baixar arquivo do Drive: ${response.message}"
                    android.util.Log.e("DailyBackup", errMsg)
                    throw IOException(errMsg)
                }
                val bodyStream = response.body?.byteStream() ?: throw IOException("Corpo do arquivo vazio")

                // Close database before restore overwriting
                android.util.Log.d("DailyBackup", "Fechando banco de dados temporariamente para restauração...")
                AppDatabase.closeDatabase()

                android.util.Log.d("DailyBackup", "Descompactando ZIP e restaurando arquivos...")
                ZipInputStream(BufferedInputStream(bodyStream)).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (entry.name.startsWith("database.db")) {
                            val dbFile = when (entry.name) {
                                "database.db-shm" -> File(context.getDatabasePath("dailynote.db").path + "-shm")
                                "database.db-wal" -> File(context.getDatabasePath("dailynote.db").path + "-wal")
                                else -> context.getDatabasePath("dailynote.db")
                            }
                            dbFile.parentFile?.mkdirs()
                            FileOutputStream(dbFile).use { fos ->
                                zis.copyTo(fos)
                            }
                            android.util.Log.d("DailyBackup", "Arquivo de banco de dados restaurado: ${entry.name}")
                        } else if (entry.name.startsWith("documentos/") || entry.name.startsWith("veiculos/") ||
                            entry.name.startsWith("evidencias/") || entry.name.startsWith("relatorios/")
                        ) {
                            val targetFile = File(context.getExternalFilesDir(null), entry.name)
                            targetFile.parentFile?.mkdirs()
                            FileOutputStream(targetFile).use { fos ->
                                zis.copyTo(fos)
                            }
                        } else if (entry.name == "config.json") {
                            val data = zis.readBytes().toString(Charsets.UTF_8)
                            val json = JSONObject(data)
                            val config = configuracaoDao.getConfiguracao() ?: RoomConfiguracao()
                            configuracaoDao.insertConfiguracao(
                                config.copy(
                                    tema = json.optString("tema", "Automático"),
                                    backupAutomatico = json.optString("backupAutomatico", "Desativado"),
                                    backupSomenteWifi = json.optBoolean("backupSomenteWifi", true)
                                )
                            )
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
            android.util.Log.d("DailyBackup", "Restauração de backup concluída com sucesso.")
            Unit
        }
    }

    suspend fun checkAndTriggerAutoDriveBackup(accessToken: String): Result<RoomBackupLog?> = withContext(Dispatchers.IO) {
        runCatching {
            val config = configuracaoDao.getConfiguracao() ?: return@runCatching null
            if (config.backupAutomatico == "Desativado") return@runCatching null

            val lastBackupStr = config.ultimoBackupData
            val lastDate = if (!lastBackupStr.isNullOrBlank()) {
                try {
                    val cleanDate = lastBackupStr.substringBefore(" ")
                    java.time.LocalDate.parse(cleanDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } catch(e: Exception) { null }
            } else null

            val today = java.time.LocalDate.now()
            val shouldBackup = when (config.backupAutomatico) {
                "Diário" -> lastDate == null || lastDate.isBefore(today)
                "Semanal" -> lastDate == null || lastDate.isBefore(today.minusWeeks(1))
                "Mensal" -> lastDate == null || lastDate.isBefore(today.minusMonths(1))
                else -> false
            }

            if (shouldBackup) {
                uploadBackupToDrive(accessToken).getOrThrow()
            } else null
        }
    }
}
