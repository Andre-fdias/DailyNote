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
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
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

data class BackupFile(val file: File, val relativePath: String)

class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType,
    private val onProgress: (Int) -> Unit
) : RequestBody() {
    override fun contentType() = contentType
    override fun contentLength() = file.length()
    override fun writeTo(sink: okio.BufferedSink) {
        val length = file.length()
        var uploaded = 0L
        val buffer = ByteArray(8192)
        FileInputStream(file).use { fis ->
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                uploaded += read
                sink.write(buffer, 0, read)
                if (length > 0) {
                    val progress = ((uploaded.toDouble() / length) * 100).toInt()
                    onProgress(progress)
                }
            }
        }
    }
}

@Singleton
class GoogleDriveBackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configuracaoDao: ConfiguracaoDao
) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    fun getGoogleSignInClient() = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope("https://www.googleapis.com/auth/drive.file"),
                Scope("https://www.googleapis.com/auth/calendar.events"),
                Scope("https://www.googleapis.com/auth/spreadsheets.readonly")
            )
            .build()
    )

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    private fun buildBackupFilesList(): List<BackupFile> {
        val list = mutableListOf<BackupFile>()
        
        // 1. Databases
        val dbDir = context.getDatabasePath("dailynote.db").parentFile
        dbDir?.listFiles()?.forEach { file ->
            if (file.isFile) list.add(BackupFile(file, "db/${file.name}"))
        }
        
        // 2. Shared Prefs
        val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        if (sharedPrefsDir.exists()) {
            sharedPrefsDir.listFiles()?.forEach { file ->
                if (file.isFile) list.add(BackupFile(file, "sp/${file.name}"))
            }
        }
        
        // 3. Internal files
        val internalDir = context.filesDir
        internalDir?.let { dir ->
            dir.walkTopDown().filter { it.isFile }.forEach { file ->
                val relPath = file.toRelativeString(dir)
                list.add(BackupFile(file, "files/$relPath"))
            }
        }
        
        // 4. External files
        val externalDir = context.getExternalFilesDir(null)
        externalDir?.let { dir ->
            dir.walkTopDown().filter { it.isFile }.forEach { file ->
                val relPath = file.toRelativeString(dir)
                list.add(BackupFile(file, "ext/$relPath"))
            }
        }
        
        return list
    }

    suspend fun createBackupZip(outputFile: File, onProgress: ((Int) -> Unit)? = null): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            // Checkpoint WAL para garantir que os dados estão no arquivo principal
            AppDatabase.getDatabase(context).openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
            
            val files = buildBackupFilesList()
            val totalBytes = files.sumOf { it.file.length() }.coerceAtLeast(1L)
            var copiedBytes = 0L

            ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zos ->
                files.forEach { backupFile ->
                    FileInputStream(backupFile.file).use { fis ->
                        zos.putNextEntry(ZipEntry(backupFile.relativePath))
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (fis.read(buffer).also { read = it } != -1) {
                            zos.write(buffer, 0, read)
                            copiedBytes += read
                            val progress = ((copiedBytes.toDouble() / totalBytes) * 100).toInt()
                            onProgress?.invoke(progress)
                        }
                        zos.closeEntry()
                    }
                }
            }
            outputFile.length()
        }
    }

    suspend fun uploadBackupToDrive(accessToken: String, onProgress: ((Int, String) -> Unit)? = null): Result<RoomBackupLog> = withContext(Dispatchers.IO) {
        android.util.Log.d("DailyBackup", "Iniciando upload de backup para o Google Drive")
        android.util.Log.d("DailyBackup", "Access token (primeiros 20 chars): ${accessToken.take(20)}...")
        runCatching {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm"))
            val backupFileName = "Backup_$timestamp.zip"
            val tempZipFile = File(context.cacheDir, backupFileName)
            if (tempZipFile.exists()) tempZipFile.delete()

            onProgress?.invoke(0, "Compactando...")
            val size = createBackupZip(tempZipFile) { prog ->
                onProgress?.invoke(prog, "Compactando...")
            }.getOrThrow()
            android.util.Log.d("DailyBackup", "ZIP criado: ${tempZipFile.length()} bytes em ${tempZipFile.absolutePath}")

            onProgress?.invoke(0, "Preparando pastas...")
            val dailyNotesFolderId = getOrCreateFolder(accessToken, "DailyNotes")
            val backupFolderId = getOrCreateFolder(accessToken, "Backup", dailyNotesFolderId)

            onProgress?.invoke(0, "Enviando...")
            val metadataJson = """{"name":"$backupFileName","parents":["$backupFolderId"]}"""
            android.util.Log.d("DailyBackup", "Metadata: $metadataJson")

            // Construir multipart/related com boundary explícito
            val boundary = "backup_boundary_${System.currentTimeMillis()}"
            val metadataBytes = metadataJson.toByteArray(Charsets.UTF_8)
            val zipBytes = tempZipFile.readBytes()

            // Montar corpo manualmente para garantir conformidade com a API do Drive
            val bodyStream = ByteArrayOutputStream()
            bodyStream.write("--$boundary\r\n".toByteArray())
            bodyStream.write("Content-Type: application/json; charset=UTF-8\r\n\r\n".toByteArray())
            bodyStream.write(metadataBytes)
            bodyStream.write("\r\n--$boundary\r\n".toByteArray())
            bodyStream.write("Content-Type: application/zip\r\n\r\n".toByteArray())

            val totalUpload = (metadataBytes.size + zipBytes.size).toLong()
            var uploaded = 0L
            val bufSize = 65536
            var offset = 0
            while (offset < zipBytes.size) {
                val end = minOf(offset + bufSize, zipBytes.size)
                bodyStream.write(zipBytes, offset, end - offset)
                uploaded += (end - offset)
                val prog = ((uploaded.toDouble() / totalUpload) * 100).toInt()
                onProgress?.invoke(prog.coerceIn(0, 99), "Enviando...")
                offset = end
            }
            bodyStream.write("\r\n--$boundary--\r\n".toByteArray())

            val bodyBytes = bodyStream.toByteArray()
            android.util.Log.d("DailyBackup", "Corpo total da requisição: ${bodyBytes.size} bytes")

            val requestBody = bodyBytes.toRequestBody("multipart/related; boundary=$boundary".toMediaType())

            val request = Request.Builder()
                .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                .header("Authorization", "Bearer $accessToken")
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: "(sem corpo)"
                android.util.Log.d("DailyBackup", "Drive response code: ${response.code}")
                android.util.Log.d("DailyBackup", "Drive response body: $responseBody")
                if (!response.isSuccessful) {
                    throw IOException("Falha no upload HTTP ${response.code}: $responseBody")
                }
                android.util.Log.d("DailyBackup", "Upload concluído com sucesso!")
            }

            tempZipFile.delete()
            
            // Clean up old backups, keep only 3
            onProgress?.invoke(95, "Limpando backups antigos...")
            cleanOldBackups(accessToken, backupFolderId)

            onProgress?.invoke(100, "Concluído!")

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
            val dailyNotesFolderId = getOrCreateFolder(accessToken, "DailyNotes")
            val backupFolderId = getOrCreateFolder(accessToken, "Backup", dailyNotesFolderId)
            
            val q = "'$backupFolderId' in parents and trashed=false"
            val url = "https://www.googleapis.com/drive/v3/files?q=${java.net.URLEncoder.encode(q, "UTF-8")}&fields=files(id,name,size,createdTime)&orderBy=createdTime%20desc"
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

    suspend fun restoreBackupFromDrive(accessToken: String, fileId: String, onProgress: ((Int, String) -> Unit)? = null): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .get()
                .build()

            onProgress?.invoke(0, "Baixando backup...")
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Erro ao baixar arquivo do Drive: ${response.message}")
                val contentLength = response.body?.contentLength() ?: -1L
                val bodyStream = response.body?.byteStream() ?: throw IOException("Corpo do arquivo vazio")

                AppDatabase.closeDatabase()

                // Calculate progress during download/unzip by tracking bytes read
                var totalRead = 0L
                val countingInputStream = object : FilterInputStream(bodyStream) {
                    override fun read(b: ByteArray, off: Int, len: Int): Int {
                        val read = super.read(b, off, len)
                        if (read != -1) {
                            totalRead += read
                            if (contentLength > 0) {
                                val progress = ((totalRead.toDouble() / contentLength) * 100).toInt()
                                onProgress?.invoke(progress, "Restaurando...")
                            }
                        }
                        return read
                    }
                }

                ZipInputStream(BufferedInputStream(countingInputStream)).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val targetFile = when {
                            entry.name.startsWith("db/") -> {
                                val name = entry.name.removePrefix("db/")
                                File(context.getDatabasePath("dailynote.db").parentFile, name)
                            }
                            entry.name.startsWith("sp/") -> {
                                val name = entry.name.removePrefix("sp/")
                                File(context.applicationInfo.dataDir, "shared_prefs/$name")
                            }
                            entry.name.startsWith("files/") -> {
                                val relPath = entry.name.removePrefix("files/")
                                File(context.filesDir, relPath)
                            }
                            entry.name.startsWith("ext/") -> {
                                val relPath = entry.name.removePrefix("ext/")
                                File(context.getExternalFilesDir(null), relPath)
                            }
                            else -> null
                        }

                        if (targetFile != null) {
                            targetFile.parentFile?.mkdirs()
                            FileOutputStream(targetFile).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                        
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
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

    private fun getOrCreateFolder(accessToken: String, folderName: String, parentId: String? = null): String {
        var q = "mimeType='application/vnd.google-apps.folder' and name='$folderName' and trashed=false"
        if (parentId != null) {
            q += " and '$parentId' in parents"
        }
        val url = "https://www.googleapis.com/drive/v3/files?q=${java.net.URLEncoder.encode(q, "UTF-8")}&fields=files(id)"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()
            
        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                val files = jsonResponse.optJSONArray("files")
                if (files != null && files.length() > 0) {
                    return files.getJSONObject(0).getString("id")
                }
            }
        }
        
        val metadata = JSONObject()
        metadata.put("name", folderName)
        metadata.put("mimeType", "application/vnd.google-apps.folder")
        if (parentId != null) {
            metadata.put("parents", org.json.JSONArray().put(parentId))
        }
        
        val createReq = Request.Builder()
            .url("https://www.googleapis.com/drive/v3/files")
            .header("Authorization", "Bearer $accessToken")
            .post(metadata.toString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
            .build()
            
        httpClient.newCall(createReq).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Falha ao criar pasta $folderName: ${response.body?.string()}")
            val json = JSONObject(response.body?.string() ?: "{}")
            return json.getString("id")
        }
    }

    private fun cleanOldBackups(accessToken: String, folderId: String) {
        val q = "'$folderId' in parents and trashed=false"
        val url = "https://www.googleapis.com/drive/v3/files?q=${java.net.URLEncoder.encode(q, "UTF-8")}&fields=files(id,createdTime)&orderBy=createdTime%20asc"
        
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()
            
        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                val filesArray = jsonResponse.optJSONArray("files") ?: return
                
                if (filesArray.length() > 3) {
                    val toDeleteCount = filesArray.length() - 3
                    for (i in 0 until toDeleteCount) {
                        val fileId = filesArray.getJSONObject(i).getString("id")
                        val delReq = Request.Builder()
                            .url("https://www.googleapis.com/drive/v3/files/$fileId")
                            .header("Authorization", "Bearer $accessToken")
                            .delete()
                            .build()
                        httpClient.newCall(delReq).execute().close()
                    }
                }
            }
        }
    }
}
