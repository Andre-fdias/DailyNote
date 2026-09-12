package com.andrefdias.dailynote.data.service

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

data class ValueRange(
    val range: String,
    val majorDimension: String,
    val values: List<List<String>>?
)

interface GoogleSheetsApi {

    @GET("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun getSpreadsheetValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Header("Authorization") authorization: String
    ): ValueRange
}
