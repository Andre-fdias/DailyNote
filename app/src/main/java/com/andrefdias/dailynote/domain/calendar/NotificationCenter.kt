package com.andrefdias.dailynote.domain.calendar

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.andrefdias.dailynote.MainActivity
import com.andrefdias.dailynote.domain.model.CalendarNotificacao
import com.andrefdias.dailynote.domain.model.CategoriaNotificacao
import com.andrefdias.dailynote.domain.model.PrioridadeTarefa
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

object NotificationCenter {
    private const val TAG = "NotificationCenter"
    const val CHANNEL_ESCALAS = "firenotes_escalas"
    const val CHANNEL_EVENTOS = "firenotes_eventos"
    const val CHANNEL_AGENDA = "firenotes_agenda"
    const val CHANNEL_TAREFAS = "firenotes_tarefas"
    const val CHANNEL_SISTEMA = "firenotes_sistema"
    const val CHANNEL_BACKUP = "firenotes_backup"
    const val CHANNEL_OCORRENCIAS = "firenotes_ocorrencias"
    const val CHANNEL_TREINAMENTOS = "firenotes_treinamentos"

    /**
     * Cria todos os canais de notificação exigidos no Android O+.
     */
    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val channels = listOf(
                Pair(CHANNEL_ESCALAS, "Escalas"),
                Pair(CHANNEL_EVENTOS, "Eventos"),
                Pair(CHANNEL_AGENDA, "Agenda"),
                Pair(CHANNEL_TAREFAS, "Tarefas"),
                Pair(CHANNEL_SISTEMA, "Sistema"),
                Pair(CHANNEL_BACKUP, "Backup"),
                Pair(CHANNEL_OCORRENCIAS, "Ocorrências"),
                Pair(CHANNEL_TREINAMENTOS, "Treinamentos")
            )

            channels.forEach { (id, name) ->
                val channel = NotificationChannel(
                    id,
                    name,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Canal de notificações para $name"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
            Log.d(TAG, "✅ Canais de notificação inicializados com sucesso.")
        }
    }

    /**
     * Dispara uma notificação local imediatamente (exibe no sistema e salva no histórico do banco).
     */
    fun dispatchNotification(
        context: Context,
        repository: CalendarRepository,
        categoria: CategoriaNotificacao,
        titulo: String,
        descricao: String,
        prioridade: PrioridadeTarefa = PrioridadeTarefa.MEDIA,
        origem: String = "SISTEMA"
    ) {
        val id = UUID.randomUUID().toString()
        val now = LocalDate.now().toString()
        val time = LocalTime.now().toString().take(5)

        val notificacao = CalendarNotificacao(
            id = id,
            categoria = categoria,
            titulo = titulo,
            descricao = descricao,
            data = now,
            hora = time,
            prioridade = prioridade,
            lida = false,
            origem = origem
        )

        // Salva no banco de dados local de forma assíncrona
        CoroutineScope(Dispatchers.IO).launch {
            repository.saveNotificacao(notificacao)
        }

        // Envia alerta visual nativo
        triggerSystemNotification(context, notificacao)
    }

    /**
     * Agenda um lembrete antecipado no AlarmManager do Android.
     */
    fun scheduleReminder(
        context: Context,
        id: String,
        titulo: String,
        descricao: String,
        timeInMillis: Long,
        categoria: CategoriaNotificacao
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("EXTRA_ID", id)
            putExtra("EXTRA_TITLE", titulo)
            putExtra("EXTRA_DESC", descricao)
            putExtra("EXTRA_CAT", categoria.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
        Log.d(TAG, "⏰ Lembrete agendado para $titulo em $timeInMillis ms")
    }

    /**
     * Auxiliar para disparar o alerta nativo visual do Android.
     */
    fun triggerSystemNotification(context: Context, notificacao: CalendarNotificacao) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = when (notificacao.categoria) {
            CategoriaNotificacao.ESCALAS -> CHANNEL_ESCALAS
            CategoriaNotificacao.EVENTOS -> CHANNEL_EVENTOS
            CategoriaNotificacao.AGENDA -> CHANNEL_AGENDA
            CategoriaNotificacao.TAREFAS -> CHANNEL_TAREFAS
            CategoriaNotificacao.SISTEMA -> CHANNEL_SISTEMA
            CategoriaNotificacao.BACKUP -> CHANNEL_BACKUP
            CategoriaNotificacao.OCORRENCIAS -> CHANNEL_OCORRENCIAS
            CategoriaNotificacao.TREINAMENTOS -> CHANNEL_TREINAMENTOS
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("ROUTE", "home")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificacao.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Ações Rápidas (Marcar como lida e Concluir)
        val readIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_MARK_AS_READ"
            putExtra("EXTRA_ID", notificacao.id)
        }
        val readPendingIntent = PendingIntent.getBroadcast(
            context,
            notificacao.id.hashCode() + 1,
            readIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notificacao.titulo)
            .setContentText(notificacao.descricao)
            .setPriority(
                when (notificacao.prioridade) {
                    PrioridadeTarefa.ALTA -> NotificationCompat.PRIORITY_MAX
                    PrioridadeTarefa.MEDIA -> NotificationCompat.PRIORITY_DEFAULT
                    PrioridadeTarefa.BAIXA -> NotificationCompat.PRIORITY_LOW
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Lida", readPendingIntent)

        notificationManager.notify(notificacao.id.hashCode(), builder.build())
    }
}

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: CalendarRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val id = intent.getStringExtra("EXTRA_ID") ?: return

        if (action == "ACTION_MARK_AS_READ") {
            // Ação rápida: marcar notificação como lida
            CoroutineScope(Dispatchers.IO).launch {
                repository.getNotificacoes().find { it.id == id }?.let { notif ->
                    repository.saveNotificacao(notif.copy(lida = true))
                }
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(id.hashCode())
            return
        }

        // Disparo normal de alarme agendado
        val titulo = intent.getStringExtra("EXTRA_TITLE") ?: "Lembrete FireNotes"
        val descricao = intent.getStringExtra("EXTRA_DESC") ?: ""
        val catName = intent.getStringExtra("EXTRA_CAT") ?: CategoriaNotificacao.SISTEMA.name
        val categoria = runCatching { CategoriaNotificacao.valueOf(catName) }.getOrDefault(CategoriaNotificacao.SISTEMA)

        NotificationCenter.dispatchNotification(
            context = context,
            repository = repository,
            categoria = categoria,
            titulo = titulo,
            descricao = descricao,
            prioridade = PrioridadeTarefa.MEDIA,
            origem = "CALENDARIO"
        )
    }
}
