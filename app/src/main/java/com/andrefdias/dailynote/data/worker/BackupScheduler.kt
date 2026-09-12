package com.andrefdias.dailynote.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

object BackupScheduler {

    const val WORK_NAME = "GoogleDriveBackup"

    fun scheduleNextBackup(context: Context, frequency: String, wifiOnly: Boolean, forceReplace: Boolean = false) {
        val workManager = WorkManager.getInstance(context)

        if (frequency == "Desativado") {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(8, 0)
        
        var nextRun = now

        when (frequency) {
            "Diário" -> {
                nextRun = now.with(targetTime)
                if (now.isAfter(nextRun) || now.isEqual(nextRun)) {
                    nextRun = nextRun.plusDays(1)
                }
            }
            "Semanal" -> {
                nextRun = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)).with(targetTime)
                if (now.isAfter(nextRun) || now.isEqual(nextRun)) {
                    nextRun = nextRun.plusWeeks(1).with(TemporalAdjusters.next(DayOfWeek.MONDAY)).with(targetTime)
                }
            }
            "Mensal" -> {
                // First business day of the month
                val firstDayOfNextMonth = now.plusMonths(1).withDayOfMonth(1)
                var firstBusinessDay = firstDayOfNextMonth
                
                // If it's a weekend, move to next Monday
                if (firstBusinessDay.dayOfWeek == DayOfWeek.SATURDAY || firstBusinessDay.dayOfWeek == DayOfWeek.SUNDAY) {
                    firstBusinessDay = firstBusinessDay.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                }
                
                // Let's also check if we are in the current month but before the first business day at 6 AM
                val firstDayOfCurrentMonth = now.withDayOfMonth(1)
                var currentMonthFirstBusinessDay = firstDayOfCurrentMonth
                if (currentMonthFirstBusinessDay.dayOfWeek == DayOfWeek.SATURDAY || currentMonthFirstBusinessDay.dayOfWeek == DayOfWeek.SUNDAY) {
                    currentMonthFirstBusinessDay = currentMonthFirstBusinessDay.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                }
                currentMonthFirstBusinessDay = currentMonthFirstBusinessDay.with(targetTime)
                
                if (now.isBefore(currentMonthFirstBusinessDay)) {
                    nextRun = currentMonthFirstBusinessDay
                } else {
                    nextRun = firstBusinessDay.with(targetTime)
                }
            }
            else -> {
                // Default fallback
                workManager.cancelUniqueWork(WORK_NAME)
                return
            }
        }

        val delaySeconds = java.time.Duration.between(now, nextRun).seconds

        val networkType = if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<GoogleDriveBackupWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .build()

        val policy = if (forceReplace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
        workManager.enqueueUniqueWork(
            WORK_NAME,
            policy,
            workRequest
        )
    }
}
