package com.andrefdias.dailynote.domain.calendar

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleCalendarSyncManager @Inject constructor() {
    private val TAG = "GoogleCalendarSync"

    /**
     * Interface simulada para conectar a conta Google.
     * Retorna o e-mail da conta conectada em caso de sucesso.
     */
    suspend fun connectAccount(): Result<String> {
        Log.d(TAG, "Conectando à conta Google...")
        // Simulação de delay de autenticação
        kotlinx.coroutines.delay(1000)
        return Result.success("bombeiro.operacional@gmail.com")
    }

    /**
     * Sincroniza eventos locais para o Google Calendar.
     */
    suspend fun syncEvents(): Result<Unit> {
        Log.d(TAG, "Sincronizando eventos com Google Agenda...")
        kotlinx.coroutines.delay(800)
        return Result.success(Unit)
    }

    /**
     * Sincroniza escalas locais.
     */
    suspend fun syncScales(): Result<Unit> {
        Log.d(TAG, "Sincronizando escalas com Google Agenda...")
        kotlinx.coroutines.delay(800)
        return Result.success(Unit)
    }

    /**
     * Sincroniza tarefas locais.
     */
    suspend fun syncTasks(): Result<Unit> {
        Log.d(TAG, "Sincronizando tarefas com Google Agenda...")
        kotlinx.coroutines.delay(800)
        return Result.success(Unit)
    }

    /**
     * Sincroniza agenda local.
     */
    suspend fun syncAgenda(): Result<Unit> {
        Log.d(TAG, "Sincronizando agenda com Google Agenda...")
        kotlinx.coroutines.delay(800)
        return Result.success(Unit)
    }
}
