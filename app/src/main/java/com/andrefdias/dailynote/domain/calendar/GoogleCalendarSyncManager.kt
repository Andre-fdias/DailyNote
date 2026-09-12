package com.andrefdias.dailynote.domain.calendar

import android.content.Context
import android.util.Log
import com.andrefdias.dailynote.data.local.dao.CalendarDao
import com.andrefdias.dailynote.data.local.entities.RoomCalendarEvento
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleCalendarSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calendarDao: CalendarDao
) {
    private val TAG = "GoogleCalendarSync"
    private val httpClient = OkHttpClient()

    /**
     * Cliente do Google Sign-In configurado para pedir permissão da Agenda
     */
    fun getGoogleSignInClient() = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
        context,
        com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.file"),
                com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/calendar.events"),
                com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/spreadsheets.readonly")
            )
            .build()
    )

    /**
     * Retorna a conta atual logada no Google
     */
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
    }

    /**
     * Tenta obter o token usando a conta atual do GoogleSignIn.
     */
    suspend fun connectAccount(account: GoogleSignInAccount): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d(TAG, "Obtendo token da conta Google para Calendar...")
            val email = account.account ?: throw IllegalStateException("Conta sem e-mail do sistema")
            // Scope para Google Calendar
            val token = GoogleAuthUtil.getToken(
                context,
                account.account!!,
                "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/calendar.events https://www.googleapis.com/auth/spreadsheets.readonly"
            )
            token
        }.onFailure {
            if (it is kotlinx.coroutines.CancellationException) throw it
        }
    }

    /**
     * Sincroniza agenda baixando eventos do Google Calendar.
     */
    suspend fun syncAgenda(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d(TAG, "Baixando eventos do Google Agenda...")
            val timeMin = java.time.ZonedDateTime.now().minusMonths(1).format(java.time.format.DateTimeFormatter.ISO_INSTANT)
            val timeMax = java.time.ZonedDateTime.now().plusMonths(3).format(java.time.format.DateTimeFormatter.ISO_INSTANT)
            
            val url = "https://www.googleapis.com/calendar/v3/calendars/primary/events?timeMin=$timeMin&timeMax=$timeMax&singleEvents=true&orderBy=startTime"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Erro ao baixar eventos: ${response.message}")
                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                val items = jsonResponse.optJSONArray("items") ?: return@runCatching

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val googleId = item.getString("id")
                    val summary = item.optString("summary", "Evento sem título")
                    val description = item.optString("description", "")
                    
                    val start = item.optJSONObject("start")
                    val end = item.optJSONObject("end")
                    
                    // Lida com datas de dia inteiro ou com horário
                    var data = ""
                    var hora: String? = null
                    
                    if (start != null) {
                        if (start.has("dateTime")) {
                            val dt = start.getString("dateTime") // e.g. 2026-09-06T10:00:00-03:00
                            data = dt.substring(0, 10)
                            hora = dt.substring(11, 16)
                        } else if (start.has("date")) {
                            data = start.getString("date") // e.g. 2026-09-06
                        }
                    }
                    
                    if (data.isBlank()) continue

                    // Parse da cor do evento Google (ou fallback)
                    val colorId = item.optString("colorId", "")
                    // Algumas cores padrão do Google: "1" = Lavanda, "2" = Sálvia, "3" = Uva, "4" = Flamingo, "5" = Banana, "6" = Tangerina, "7" = Pavão, "8" = Grafite, "9" = Mirtilo, "10" = Manjericão, "11" = Tomate
                    val hexColor = when(colorId) {
                        "1" -> "#7986cb"
                        "2" -> "#33b679"
                        "3" -> "#8e24aa"
                        "4" -> "#e67c73"
                        "5" -> "#f6c026"
                        "6" -> "#f5511d"
                        "7" -> "#039be5"
                        "8" -> "#616161"
                        "9" -> "#3f51b5"
                        "10" -> "#0b8043"
                        "11" -> "#d60000"
                        else -> "#4285F4" // Azul padrão do Google
                    }

                    // Checa se o evento já existe
                    val existing = calendarDao.getEventoByGoogleId(googleId)
                    if (existing == null) {
                        val novoEvento = RoomCalendarEvento(
                            id = UUID.randomUUID().toString(),
                            titulo = summary,
                            descricao = description,
                            data = data,
                            hora = hora,
                            local = item.optString("location", null),
                            categoria = "GOOGLE",
                            cor = hexColor,
                            recorrencia = "NUNCA",
                            lembreteMinutos = null,
                            googleEventId = googleId
                        )
                        calendarDao.insertEvento(novoEvento)
                    } else {
                        // Atualiza se mudou algo? Sim, podemos atualizar titulo/data
                        calendarDao.updateEvento(existing.copy(
                            titulo = summary,
                            descricao = description,
                            data = data,
                            hora = hora,
                            local = item.optString("location", null),
                            cor = hexColor
                        ))
                    }
                }
            }
        }.onFailure {
            if (it is kotlinx.coroutines.CancellationException) throw it
        }
    }

    suspend fun syncEvents(token: String): Result<Unit> = syncAgenda(token)
    suspend fun syncScales(token: String): Result<Unit> = Result.success(Unit) // Implementar envio para Google depois
    suspend fun syncTasks(token: String): Result<Unit> = Result.success(Unit)
}

