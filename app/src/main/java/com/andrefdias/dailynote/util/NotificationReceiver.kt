package com.andrefdias.dailynote.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import com.andrefdias.dailynote.data.local.AppDatabase
import com.andrefdias.dailynote.data.local.entities.RoomNotificacao

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            android.util.Log.d("NotificationReceiver", "Alarm fired! Received broadcast.")
            val title = intent.getStringExtra("title") ?: "Compromisso FireNotes"
            val text = intent.getStringExtra("text") ?: "Voce tem uma atividade agendada"
            val notificationId = intent.getIntExtra("id", 1001)

            val channelId = "firenotes_notifications"
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Notificacoes de Compromissos",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifica o usuario sobre eventos e tarefas agendadas"
                }
                manager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            manager.notify(notificationId, builder.build())
            android.util.Log.d("NotificationReceiver", "Notification shown successfully: $title - $text")

            // Inserir registro de notificação no banco de dados local
            val db = AppDatabase.getDatabase(context)
            val calendarDao = db.calendarDao()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val today = LocalDate.now()
                    val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    val roomNotif = RoomNotificacao(
                        id = UUID.randomUUID().toString(),
                        categoria = "EVENTOS",
                        titulo = title,
                        descricao = text,
                        data = today.toString(),
                        hora = nowTime,
                        prioridade = "MEDIA",
                        lida = false,
                        origem = "SISTEMA"
                    )
                    calendarDao.insertNotificacao(roomNotif)
                    android.util.Log.d("NotificationReceiver", "Saved notification in DB: ${roomNotif.titulo}")
                } catch (e: Exception) {
                    android.util.Log.e("NotificationReceiver", "Failed to save notification in DB: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationReceiver", "Error receiving notification: ${e.message}", e)
        }
    }
}

