package com.andrefdias.dailynote.ui.screens.historico

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.domain.model.OcorrenciaComMilitares
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Refresh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoDashboardScreen(
    viewModel: HistoricoDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Bottom Sheet State for Filters
    var showFilterSheet by remember { mutableStateOf(false) }
    var expandedMenu by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importFromJson(uri, context)
        }
    }

    Scaffold(
        topBar = {
            val context = androidx.compose.ui.platform.LocalContext.current
            TopAppBar(
                title = { Text("Dashboard Operacional") },
                actions = {
                    IconButton(onClick = { viewModel.buscarOcorrencias() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                    }
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Exportar PDF") },
                            onClick = {
                                viewModel.exportToPdf(context)
                                expandedMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Exportar Excel") },
                            onClick = {
                                viewModel.exportToExcel(context)
                                expandedMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Importar JSON") },
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                                expandedMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Compartilhar JSON") },
                            onClick = {
                                viewModel.shareAsJson(context)
                                expandedMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFF1E2633)
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Erro: ${state.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.buscarOcorrencias() }) {
                        Text("Tentar Novamente")
                    }
                }
            }
        } else {
            DashboardContent(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }, modifier = Modifier.fillMaxHeight(0.9f)) {
            FilterContent(
                state = state,
                viewModel = viewModel,
                onClose = { showFilterSheet = false }
            )
        }
    }
}

@Composable
fun DashboardContent(state: HistoricoDashboardState, viewModel: HistoricoDashboardViewModel, modifier: Modifier = Modifier) {
    val ocorrencias = state.ocorrenciasFiltradas
    val mapOccurrences = state.mapOccurrencesFiltradas
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Resumo", "Mapa", "Lista")

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.filtroTextoLivre,
            onValueChange = { viewModel.setFiltroTextoLivre(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Buscar no talão, histórico, endereço...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFF37474F),
                focusedContainerColor = Color(0xFF1E2633),
                unfocusedContainerColor = Color(0xFF1E2633)
            )
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> ResumoTab(ocorrencias)
                1 -> MapLibreMapTab(mapOccurrences)
                2 -> ListaTab(ocorrencias)
            }
        }
    }
}

@Composable
fun ResumoTab(ocorrencias: List<OcorrenciaComMilitares>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            KpiSection(ocorrencias)
        }

        if (ocorrencias.isNotEmpty()) {
            
            // Cálculos para Gráficos Temporais (Agrupamento por Mês/Ano)
            val parsedOccs = ocorrencias.mapNotNull { occ ->
                val dataStr = occ.ocorrencia.data
                try {
                    val parts = dataStr.split("/", "-")
                    if (parts.size == 3) {
                        // Assuming dd/MM/yyyy or yyyy-MM-dd
                        val month = if (parts[0].length == 4) parts[1] else parts[1]
                        val year = if (parts[0].length == 4) parts[0].takeLast(2) else parts[2].takeLast(2)
                        val sortKey = "${if (parts[0].length == 4) parts[0] else parts[2]}$month" // yyyyMM
                        val displayKey = "$month/$year"
                        Triple(sortKey, displayKey, occ.ocorrencia)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            
            val monthlyGroups = parsedOccs.groupBy { Pair(it.first, it.second) }.toSortedMap(compareBy { it.first })
            
            val volumeData = mutableMapOf<String, Float>()
            val efetividadeData = mutableMapOf<String, Float>()
            val letalidadeData = mutableMapOf<String, Float>()
            
            monthlyGroups.forEach { (keys, list) ->
                val label = keys.second
                val total = list.size.toFloat()
                
                // Volume
                volumeData[label] = total
                
                // Efetividade (% Atendidas)
                val atendidas = list.count { it.third.resultado?.equals("Atendida", ignoreCase = true) == true }
                efetividadeData[label] = if (total > 0) (atendidas.toFloat() / total) * 100f else 0f
                
                // Letalidade (% Vítimas Fatais / Total Vítimas)
                val vitimasFatais = list.sumOf { it.third.vitimasFatais }.toFloat()
                val vitimasTotal = list.sumOf { it.third.vitimas }.toFloat()
                letalidadeData[label] = if (vitimasTotal > 0) (vitimasFatais / vitimasTotal) * 100f else 0f
            }

            item {
                Text("Volume Mensal de Ocorrências", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LineChartWidget(data = volumeData, lineColor = Color(0xFF64B5F6), gradientColor = Color(0xFF1E88E5))
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Text("Taxa de Efetividade de Atendimento Mensal (%)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LineChartWidget(data = efetividadeData, isPercentage = true, lineColor = Color(0xFF81C784), gradientColor = Color(0xFF43A047))
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Text("Índice de Letalidade Operacional Mensal (%)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LineChartWidget(data = letalidadeData, isPercentage = true, lineColor = Color(0xFFE57373), gradientColor = Color(0xFFE53935))
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Distribuição por Natureza", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                BarChartWidget(ocorrencias.groupBy { it.ocorrencia.natureza })
            }
            item {
                Text("Top 5 Endereços por Natureza", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                val addressNatureGroups = ocorrencias
                    .groupBy { "${it.ocorrencia.endereco} - ${it.ocorrencia.natureza}" }
                    .entries
                    .sortedByDescending { it.value.size }
                    .take(5)
                    .associate { it.key to it.value }
                    
                BarChartWidget(addressNatureGroups)
            }
            item {
                Text("Cidades com mais Ocorrências", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                BarChartWidget(ocorrencias.groupBy { it.ocorrencia.cidade })
            }
        } else {
            item {
                Text(
                    text = "Nenhuma ocorrência encontrada para os filtros aplicados.",
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ListaTab(ocorrencias: List<OcorrenciaComMilitares>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ocorrencias) { occ ->
            OcorrenciaCard(occ)
        }
    }
}

@Composable
fun KpiSection(ocorrencias: List<OcorrenciaComMilitares>) {
    val totalVitimas = ocorrencias.sumOf { it.ocorrencia.vitimas }
    val fatais = ocorrencias.sumOf { it.ocorrencia.vitimasFatais }
    val cidades = ocorrencias.mapNotNull { it.ocorrencia.cidade }.filter { it.isNotBlank() }.distinct().size
    val vtrs = ocorrencias.mapNotNull { it.ocorrencia.vtr }.filter { it.isNotBlank() }.distinct().size
    
    val peakHourGroup = ocorrencias
        .mapNotNull { it.ocorrencia.qtrSaida.takeIf { t -> t.isNotBlank() && t.contains(":") } }
        .map { it.substringBefore(":") + "h" }
        .groupingBy { it }
        .eachCount()
    val peakHour = peakHourGroup.maxByOrNull { it.value }?.key ?: "-"

    val atendidas = ocorrencias.count { it.ocorrencia.resultado?.equals("Atendida", ignoreCase = true) == true }
    val qtas = ocorrencias.count { it.ocorrencia.resultado?.equals("QTA", ignoreCase = true) == true }
    
    val ocorrenciasComDistancia = ocorrencias.mapNotNull { it.ocorrencia.distancia }
    val distanciaMedia = if (ocorrenciasComDistancia.isNotEmpty()) {
        String.format(Locale.US, "%.1f km", ocorrenciasComDistancia.average())
    } else {
        "-"
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                title = "Total Ocorrências",
                value = ocorrencias.size.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primaryContainer
            )
            KpiCard(
                title = "Atendidas",
                value = atendidas.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
            KpiCard(
                title = "QTA",
                value = qtas.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.errorContainer
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                title = "Distância Média",
                value = distanciaMedia,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            KpiCard(
                title = "Pico Atend.",
                value = peakHour,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                title = "Cidades Atend.",
                value = cidades.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            KpiCard(
                title = "VTRs Envolvidas",
                value = vtrs.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondaryContainer
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                title = "Vítimas / Fatais",
                value = "$totalVitimas / $fatais",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.errorContainer
            )
        }
    }
}

@Composable
fun KpiCard(title: String, value: String, modifier: Modifier = Modifier, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BarChartWidget(groupedData: Map<String, List<Any>>) {
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }
    val sorted = groupedData.mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    val maxVal = sorted.maxOfOrNull { it.second } ?: 1

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        sorted.forEachIndexed { index, (label, count) ->
            val fraction = count.toFloat() / maxVal.toFloat()
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    modifier = Modifier.weight(0.4f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val curFraction by animateFloatAsState(
                    targetValue = if (animationPlayed) fraction else 0f,
                    animationSpec = tween(durationMillis = 1000, delayMillis = index * 100)
                )

                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF283550))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(curFraction)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF1E88E5), Color(0xFF64B5F6))
                                )
                            )
                    )
                }
                Text(
                    text = count.toString(),
                    modifier = Modifier.weight(0.1f).padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun OcorrenciaCard(ocorrenciaComMilitares: OcorrenciaComMilitares) {
    var expanded by remember { mutableStateOf(false) }
    val occ = ocorrenciaComMilitares.ocorrencia
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF232D42)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF37474F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Talão: ${occ.talao}", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${occ.data} ${occ.qtrSaida}", style = MaterialTheme.typography.bodySmall)
                    IconButton(
                        onClick = {
                            val uri = if (occ.latitude != null && occ.longitude != null) {
                                android.net.Uri.parse("geo:${occ.latitude},${occ.longitude}?q=${occ.latitude},${occ.longitude}")
                            } else {
                                android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(occ.endereco + ", " + occ.cidade)}")
                            }
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.padding(start = 8.dp).size(24.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Ver no mapa", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "VTR: ${occ.vtr} | ${occ.natureza}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "${occ.endereco}, ${occ.cidade}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Prontidão: ${occ.prontidao ?: "-"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    Text(text = "Resultado: ${occ.resultado ?: "-"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Text(text = "Distância: ${occ.distancia?.let { "$it km" } ?: "-"}", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Detalhes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text(text = "Vítimas: ${occ.vitimas} | Fatais: ${occ.vitimasFatais}", style = MaterialTheme.typography.bodySmall)
                Text(text = "CMT VTR (Histórico): ${occ.cmtVtr}", style = MaterialTheme.typography.bodySmall)
                if (!occ.observacoes.isNullOrBlank()) {
                    Text(text = "Obs: ${occ.observacoes}", style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Guarnição Associada (${ocorrenciaComMilitares.militares.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                if (ocorrenciaComMilitares.militares.isEmpty()) {
                    Text(text = "Nenhuma guarnição encontrada no banco local.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                } else {
                    ocorrenciaComMilitares.militares.forEach { mil ->
                        Text(text = "- ${mil.graduacao} ${mil.nomeGuerra} (RE ${mil.re})", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterContent(
    state: HistoricoDashboardState,
    viewModel: HistoricoDashboardViewModel,
    onClose: () -> Unit
) {
    var startDateMillis by remember { mutableStateOf(state.filtroDataInicio?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()) }
    var endDateMillis by remember { mutableStateOf(state.filtroDataFim?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = startDateMillis)
    val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = endDateMillis)

    var cidade by remember { mutableStateOf(state.filtroCidade ?: "") }
    var natureza by remember { mutableStateOf(state.filtroNatureza ?: "") }
    var prontidao by remember { mutableStateOf(state.filtroProntidao ?: "") }
    var resultado by remember { mutableStateOf(state.filtroResultado ?: "") }
    var militarSelection by remember { mutableStateOf(state.filtroMilitarId ?: "") }

    val militarOptions = listOf("Todos") + state.todosMilitares.map { "${it.graduacao} ${it.nomeGuerra} (RE: ${it.re})" }
    val initialMilitarOption = if (militarSelection.isBlank()) "Todos" else state.todosMilitares.find { it.id == militarSelection }?.let { "${it.graduacao} ${it.nomeGuerra} (RE: ${it.re})" } ?: "Todos"
    var selectedMilitarString by remember { mutableStateOf(initialMilitarOption) }

    var timeStart by remember { mutableStateOf(state.filtroHoraInicio ?: "00:00") }
    var timeEnd by remember { mutableStateOf(state.filtroHoraFim ?: "23:59") }
    
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    val startTimePickerState = rememberTimePickerState(
        initialHour = timeStart.substringBefore(":").toIntOrNull() ?: 0,
        initialMinute = timeStart.substringAfter(":").toIntOrNull() ?: 0
    )
    val endTimePickerState = rememberTimePickerState(
        initialHour = timeEnd.substringBefore(":").toIntOrNull() ?: 23,
        initialMinute = timeEnd.substringAfter(":").toIntOrNull() ?: 59
    )

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDateMillis = startDatePickerState.selectedDateMillis
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDateMillis = endDatePickerState.selectedDateMillis
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
    
    if (showStartTimePicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    timeStart = String.format(Locale.getDefault(), "%02d:%02d", startTimePickerState.hour, startTimePickerState.minute)
                    showStartTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancelar") }
            },
            text = { TimePicker(state = startTimePickerState) }
        )
    }

    if (showEndTimePicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    timeEnd = String.format(Locale.getDefault(), "%02d:%02d", endTimePickerState.hour, endTimePickerState.minute)
                    showEndTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancelar") }
            },
            text = { TimePicker(state = endTimePickerState) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Filtros e Buscas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val startText = startDateMillis?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: "Início"
            val endText = endDateMillis?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: "Fim"
            
            OutlinedButton(onClick = { showStartDatePicker = true }, modifier = Modifier.weight(1f)) {
                Text("De: $startText", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(onClick = { showEndDatePicker = true }, modifier = Modifier.weight(1f)) {
                Text("Até: $endText", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showStartTimePicker = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("De: $timeStart")
            }
            OutlinedButton(onClick = { showEndTimePicker = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Até: $timeEnd")
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            DropdownFilter(
                label = "Cidade",
                options = listOf("Todas") + state.cidadesDisponiveis,
                selectedValue = if (cidade.isBlank()) "Todas" else cidade,
                onValueChange = { cidade = if (it == "Todas") "" else it }
            )

            DropdownFilter(
                label = "Natureza (Tipo)",
                options = listOf("Todas") + state.naturezasDisponiveis,
                selectedValue = if (natureza.isBlank()) "Todas" else natureza,
                onValueChange = { natureza = if (it == "Todas") "" else it }
            )
            
            DropdownFilter(
                label = "Militar (Guarnição/CMT)",
                options = militarOptions,
                selectedValue = selectedMilitarString,
                onValueChange = { sel -> 
                    selectedMilitarString = sel
                    if (sel == "Todos") {
                        militarSelection = ""
                    } else {
                        val re = sel.substringAfter("RE: ").substringBefore(")")
                        val mil = state.todosMilitares.find { it.re == re }
                        militarSelection = mil?.id ?: ""
                    }
                }
            )

            DropdownFilter(
                label = "Prontidão",
                options = listOf("Todas") + state.prontidoesDisponiveis,
                selectedValue = if (prontidao.isBlank()) "Todas" else prontidao,
                onValueChange = { prontidao = if (it == "Todas") "" else it }
            )

            DropdownFilter(
                label = "Resultado",
                options = listOf("Todos") + state.resultadosDisponiveis,
                selectedValue = if (resultado.isBlank()) "Todos" else resultado,
                onValueChange = { resultado = if (it == "Todos") "" else it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { 
                    viewModel.limparFiltros()
                    onClose() 
                }) {
                    Text("Limpar")
                }
                Button(onClick = {
                    val start = startDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() }
                    val end = endDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() }
                    
                    if (start != null && end != null) {
                        viewModel.atualizarFiltroData(start, end)
                    }
                    viewModel.atualizarFiltroMilitar(militarSelection.takeIf { it.isNotBlank() })
                    viewModel.setFiltroCidade(cidade)
                    viewModel.setFiltroNatureza(natureza)
                    viewModel.setFiltroProntidao(prontidao)
                    viewModel.setFiltroResultado(resultado)
                    viewModel.setFiltroHorario(timeStart, timeEnd)
                    onClose()
                }) {
                    Text("Aplicar Filtros")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownFilter(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    androidx.compose.material3.ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
            colors = androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun LineChartWidget(
    data: Map<String, Float>,
    isPercentage: Boolean = false,
    lineColor: Color = Color(0xFF1E88E5),
    gradientColor: Color = Color(0xFF1E88E5)
) {
    if (data.isEmpty()) return

    val labels = data.keys.toList()
    val values = data.values.toList()

    val maxVal = values.maxOrNull() ?: 1f
    val minVal = values.minOrNull() ?: 0f
    
    // Scale slightly above max for headroom
    val yRange = if (maxVal == minVal) 1f else (maxVal - minVal)
    val topPadding = yRange * 0.2f
    val yMax = if (isPercentage) 100f else maxVal + topPadding
    val yMin = 0f 

    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "LineChartAnim"
    )
    
    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(modifier = Modifier.fillMaxWidth().height(220.dp).padding(vertical = 8.dp)) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(start = 32.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            val width = size.width
            val height = size.height
            val xStep = if (labels.size > 1) width / (labels.size - 1) else width

            // Draw Y-axis grid lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = height - (height * (i.toFloat() / gridLines))
                val value = yMin + ((yMax - yMin) * (i.toFloat() / gridLines))
                
                drawLine(
                    color = Color(0xFF37474F),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(width, y),
                    strokeWidth = 1f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                
                // Y-axis labels
                drawContext.canvas.nativeCanvas.drawText(
                    if (isPercentage) "${value.toInt()}%" else value.toInt().toString(),
                    -20f,
                    y + 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }

            val path = androidx.compose.ui.graphics.Path()
            val fillPath = androidx.compose.ui.graphics.Path()
            var firstPoint = true

            val points = mutableListOf<androidx.compose.ui.geometry.Offset>()

            values.forEachIndexed { index, value ->
                if (index.toFloat() / labels.size.coerceAtLeast(1) <= animatedProgress || labels.size == 1) {
                    val x = index * xStep
                    val y = height - ((value - yMin) / (yMax - yMin) * height)
                    points.add(androidx.compose.ui.geometry.Offset(x, y))
                    
                    if (firstPoint) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, y)
                        firstPoint = false
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
            }

            if (points.isNotEmpty()) {
                // Draw Line
                drawPath(
                    path = path,
                    color = lineColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 4f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )

                // Draw gradient fill
                val lastX = points.last().x
                fillPath.lineTo(lastX, height)
                fillPath.lineTo(0f, height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(gradientColor.copy(alpha = 0.4f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw points
                points.forEach { point ->
                    drawCircle(
                        color = surfaceColor,
                        radius = 6f,
                        center = point
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 4f,
                        center = point
                    )
                }
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 32.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { index, label ->
                // Show less labels if too many to fit
                if (labels.size <= 6 || index % (labels.size / 6).coerceAtLeast(1) == 0 || index == labels.size - 1) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                } else if (labels.size <= 6) {
                     Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                } else {
                     Spacer(modifier = Modifier.width(1.dp))
                }
            }
        }
    }
}
