package com.andrefdias.dailynote.domain.model

import java.time.LocalDate

enum class OccurrenceSource {
    APP,
    ETL
}

data class MapOccurrence(
    val id: String,
    val source: OccurrenceSource,
    val date: LocalDate?,
    val time: String?,
    val talao: String?,
    val vtr: String?,
    val commander: String?,
    val militaryPersonnel: List<String>,
    val nature: String?,
    val victims: Int,
    val fatalVictims: Int,
    val address: String?,
    val city: String?,
    val latitude: Double,
    val longitude: Double,
    val color: String = "#FF0000" // Default color for map marker
)

data class MapFilter(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val city: String? = null,
    val nature: String? = null,
    val militaryName: String? = null,
    val vtr: String? = null
)

data class MapStatistics(
    val totalOccurrences: Int = 0,
    val totalVictims: Int = 0,
    val totalFatalVictims: Int = 0,
    val topNatures: Map<String, Int> = emptyMap(),
    val topCities: Map<String, Int> = emptyMap()
)

enum class MapMode {
    STREET,
    DARK,
    SATELLITE
}

enum class VisualizationMode {
    MARKERS,
    CLUSTERS,
    HEATMAP
}
