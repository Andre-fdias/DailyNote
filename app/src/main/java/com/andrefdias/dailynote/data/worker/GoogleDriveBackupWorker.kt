package com.andrefdias.dailynote.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.andrefdias.dailynote.R
import com.andrefdias.dailynote.data.local.dao.ConfiguracaoDao
import com.andrefdias.dailynote.data.service.GoogleDriveBackupService
import com.google.android.gms.auth.GoogleAuthUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class GoogleDriveBackupWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val googleDriveBackupService: GoogleDriveBackupService,
    private val configuracaoDao: ConfiguracaoDao
) : CoroutineWorker(appContext, workerParams) {

    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "backup_channel_id"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val account = googleDriveBackupService.getLastSignedInAccount()
                ?: return@withContext Result.failure()

            val config = configuracaoDao.getConfiguracao() ?: return@withContext Result.success()

            createChannel()
            setForeground(createForegroundInfo(0, "Iniciando backup..."))

            val token = GoogleAuthUtil.getToken(
                appContext,
                account.account ?: throw IllegalStateException("Conta sem e-mail do sistema"),
                "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/calendar.events https://www.googleapis.com/auth/spreadsheets.readonly"
            )

            googleDriveBackupService.uploadBackupToDrive(token) { progress, status ->
                val info = createForegroundInfo(progress, status)
                notificationManager.notify(NOTIFICATION_ID, info.notification)
            }.getOrThrow()

            val finishedNotification = NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setContentTitle("Backup do Sistema")
                .setContentText("Backup concluído com sucesso.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(NOTIFICATION_ID + 1, finishedNotification)

            BackupScheduler.scheduleNextBackup(appContext, config.backupAutomatico, config.backupSomenteWifi)

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("BackupWorker", "Erro ao executar backup em segundo plano", e)
            
            val errorNotification = NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setContentTitle("Backup do Sistema")
                .setContentText("Falha ao realizar backup.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(NOTIFICATION_ID + 1, errorNotification)

            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun createForegroundInfo(progress: Int, status: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setContentTitle("Backup do Sistema")
            .setContentText("$status ($progress%)")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
            
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificações de Backup",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}
