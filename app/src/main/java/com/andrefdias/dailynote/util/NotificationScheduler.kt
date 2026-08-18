package com.andrefdias.dailynote.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationScheduler {
    fun schedule(context: Context, idString: String, title: String, text: String, dateStr: String, timeStr: String, minutosAntes: Int = 30) {
        try {
            android.util.Log.d("NotificationScheduler", "Attempting to schedule alarm for $dateStr $timeStr (Id: $idString) - 30 minutes before")
            val date = LocalDate.parse(dateStr)
            val time = LocalTime.parse(timeStr)
            val dateTime = LocalDateTime.of(date, time)
            
            // Subtract minutosAntes for notification time
            val alertDateTime = dateTime.minusMinutes(minutosAntes.toLong())
            val triggerTimeMs = alertDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            if (triggerTimeMs <= System.currentTimeMillis()) {
                android.util.Log.w("NotificationScheduler", "Notification time (30 min before) has already passed: $dateStr $timeStr (Trigger: $triggerTimeMs, Now: ${System.currentTimeMillis()})")
                return
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", title)
                putExtra("text", text)
                putExtra("id", idString.hashCode() + minutosAntes)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                idString.hashCode() + minutosAntes,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        pendingIntent
                    )
                    android.util.Log.d("NotificationScheduler", "Scheduled EXACT alarm for $dateStr $timeStr")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        pendingIntent
                    )
                    android.util.Log.d("NotificationScheduler", "Scheduled INEXACT alarm for $dateStr $timeStr (Exact denied)")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
                android.util.Log.d("NotificationScheduler", "Scheduled EXACT alarm for $dateStr $timeStr (older SDK)")
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationScheduler", "Erro ao agendar alarme: ${e.message}", e)
        }
    }

    fun cancel(context: Context, idString: String, lembretes: List<Int> = listOf(0, 5, 10, 15, 30, 60, 120, 180, 360, 720, 1440, 2880)) {
        try {
            android.util.Log.d("NotificationScheduler", "Cancelling alarm for Id: $idString")
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            
            for (m in lembretes) {
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    idString.hashCode() + m,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                    android.util.Log.d("NotificationScheduler", "Alarm cancelled successfully for hash: ${idString.hashCode() + m}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationScheduler", "Erro ao cancelar alarme: ${e.message}", e)
        }
    }
}
