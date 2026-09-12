package com.andrefdias.dailynote.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.andrefdias.dailynote.data.worker.BackupScheduler
import com.andrefdias.dailynote.util.LogHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var configuracaoDao: com.andrefdias.dailynote.data.local.dao.ConfiguracaoDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            LogHelper.init(context)
            LogHelper.i("BootReceiver", "Dispositivo reiniciado. Reagendando rotinas de background.")
            
            // Reagendar backup caso exista configuração
            // Workers removidos, notificações rodarão direto no ViewModel da Homeista configuração
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val config = configuracaoDao.getConfiguracao()
                    if (config != null) {
                        BackupScheduler.scheduleNextBackup(context, config.backupAutomatico, config.backupSomenteWifi)
                        LogHelper.i("BootReceiver", "Rotina de Backup reagendada: ${config.backupAutomatico}")
                    }
                } catch (e: Exception) {
                    LogHelper.e("BootReceiver", "Erro ao reagendar backup", e)
                }
            }
        }
    }
}
