package com.andrefdias.dailynote.domain.calendar

import android.content.Context
import android.util.Log
import com.andrefdias.dailynote.domain.model.CalendarEvento
import com.andrefdias.dailynote.domain.model.CalendarTarefa
import com.andrefdias.dailynote.domain.model.CategoriaNotificacao
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Centralizador de agendamento de lembretes.
 * Para cada evento/tarefa agenda 3 alarmes:
 *   1. 1 dia antes (às 09:00)
 *   2. 1 hora antes
 *   3. No momento exato do evento
 */
object NotificationScheduler {

    private const val TAG = "NotificationScheduler"
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun scheduleForEvento(context: Context, evento: CalendarEvento) {
        val date = runCatching { LocalDate.parse(evento.data, dateFormatter) }.getOrNull() ?: return
        val time = if (!evento.hora.isNullOrBlank()) {
            runCatching { LocalTime.parse(evento.hora) }.getOrNull() ?: LocalTime.of(8, 0)
        } else {
            LocalTime.of(8, 0)
        }

        val baseTitle = "📅 ${evento.titulo}"
        val baseDesc = if (evento.local.isNullOrBlank()) evento.descricao else "${evento.descricao} — ${evento.local}"

        scheduleThreeAlarms(
            context = context,
            baseId = evento.id,
            date = date,
            time = time,
            titulo = baseTitle,
            descricao = baseDesc,
            categoria = CategoriaNotificacao.EVENTOS
        )
    }

    fun scheduleForTarefa(context: Context, tarefa: CalendarTarefa) {
        val date = runCatching { LocalDate.parse(tarefa.data, dateFormatter) }.getOrNull() ?: return
        val time = if (!tarefa.hora.isNullOrBlank()) {
            runCatching { LocalTime.parse(tarefa.hora) }.getOrNull() ?: LocalTime.of(8, 0)
        } else {
            LocalTime.of(8, 0)
        }

        scheduleThreeAlarms(
            context = context,
            baseId = tarefa.id,
            date = date,
            time = time,
            titulo = "✅ ${tarefa.titulo}",
            descricao = tarefa.descricao,
            categoria = CategoriaNotificacao.TAREFAS
        )
    }

    /**
     * Agenda 3 alarmes para um evento:
     *   - 1 dia antes (às 09:00)
     *   - 1 hora antes
     *   - No horário exato
     */
    private fun scheduleThreeAlarms(
        context: Context,
        baseId: String,
        date: LocalDate,
        time: LocalTime,
        titulo: String,
        descricao: String,
        categoria: CategoriaNotificacao
    ) {
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()

        // 1. Lembrete 1 dia antes às 09:00
        val umDiaAntes = LocalDateTime.of(date.minusDays(1), LocalTime.of(9, 0))
            .atZone(zone).toInstant().toEpochMilli()
        if (umDiaAntes > now) {
            NotificationCenter.scheduleReminder(
                context = context,
                id = "${baseId}_1d",
                titulo = "⏰ Amanhã: $titulo",
                descricao = "Evento amanhã às ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}. $descricao",
                timeInMillis = umDiaAntes,
                categoria = categoria
            )
            Log.d(TAG, "✅ Lembrete 1 dia antes agendado para $titulo em $umDiaAntes")
        }

        // 2. Lembrete 1 hora antes
        val umaHoraAntes = LocalDateTime.of(date, time).minusHours(1)
            .atZone(zone).toInstant().toEpochMilli()
        if (umaHoraAntes > now) {
            NotificationCenter.scheduleReminder(
                context = context,
                id = "${baseId}_1h",
                titulo = "⏰ Em 1 hora: $titulo",
                descricao = "Faltam 60 minutos. $descricao",
                timeInMillis = umaHoraAntes,
                categoria = categoria
            )
            Log.d(TAG, "✅ Lembrete 1h antes agendado para $titulo em $umaHoraAntes")
        }

        // 3. Lembrete no horário exato
        val horarioExato = LocalDateTime.of(date, time)
            .atZone(zone).toInstant().toEpochMilli()
        if (horarioExato > now) {
            NotificationCenter.scheduleReminder(
                context = context,
                id = "${baseId}_now",
                titulo = "🔔 Agora: $titulo",
                descricao = descricao,
                timeInMillis = horarioExato,
                categoria = categoria
            )
            Log.d(TAG, "✅ Lembrete no horário agendado para $titulo em $horarioExato")
        }
    }

    /**
     * Cancela todos os alarmes de um evento/tarefa (para quando ele é deletado ou editado).
     */
    fun cancelRemindersFor(context: Context, id: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = android.content.Intent(context, NotificationReceiver::class.java)

        listOf("${id}_1d", "${id}_1h", "${id}_now").forEach { alarmId ->
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                alarmId.hashCode(),
                intent,
                android.app.PendingIntent.FLAG_NO_CREATE or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                Log.d(TAG, "🗑️ Alarme $alarmId cancelado")
            }
        }
    }
}
