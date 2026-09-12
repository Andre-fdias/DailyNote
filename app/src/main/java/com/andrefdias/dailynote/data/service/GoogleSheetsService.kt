package com.andrefdias.dailynote.data.service

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSheetsService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val api: GoogleSheetsApi = Retrofit.Builder()
        .baseUrl("https://sheets.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GoogleSheetsApi::class.java)

    suspend fun getSpreadsheetValues(spreadsheetId: String, range: String): Result<ValueRange> = withContext(Dispatchers.IO) {
        runCatching {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: throw IllegalStateException("Usuário não está logado no Google.")
            
            val email = account.account?.name
                ?: throw IllegalStateException("Conta Google sem e-mail.")

            val token = GoogleAuthUtil.getToken(
                context,
                account.account!!,
                "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/calendar.events https://www.googleapis.com/auth/spreadsheets.readonly"
            )

            api.getSpreadsheetValues(
                spreadsheetId = spreadsheetId,
                range = range,
                authorization = "Bearer $token"
            )
        }
    }
}
