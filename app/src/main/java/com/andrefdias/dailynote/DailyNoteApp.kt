package com.andrefdias.dailynote

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.andrefdias.dailynote.data.worker.BackupScheduler
import com.andrefdias.dailynote.domain.calendar.NotificationCenter
import com.andrefdias.dailynote.util.LogHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DailyNoteApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        LogHelper.init(this)
        // Inicializa canais de notificação do Android
        NotificationCenter.initNotificationChannels(this)
    }
}
