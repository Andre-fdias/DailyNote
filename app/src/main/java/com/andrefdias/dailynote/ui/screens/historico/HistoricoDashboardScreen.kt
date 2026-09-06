package com.andrefdias.dailynote.ui.screens.historico

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
    viewModel: HistoricoDashboardViewModel = hiltViewModel(),
    initialTab: Int = 0
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
        containerColor = MaterialTheme.colorScheme.background
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
                modifier = Modifier.padding(padding),
                initialTab = initialTab
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
fun DashboardContent(state: HistoricoDashboardState, viewModel: HistoricoDashboardViewModel, modifier: Modifier = Modifier, initialTab: Int = 0) {
    val ocorrencias = state.ocorrenciasFiltradas
    val mapOccurrences = state.mapOccurrencesFiltradas
    var selectedTab by remember { mutableStateOf(initialTab) }
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
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
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
            
            monthlyGroups.forEach { (keys, list) ->
                val label = keys.second
                val total = list.size.toFloat()
                
                // Volume
                volumeData[label] = total
                
                // Efetividade (% Atendidas)
                val atendidas = list.count { it.third.resultado?.equals("Atendida", ignoreCase = true) == true }
                efetividadeData[label] = if (total > 0) (atendidas.toFloat() / total) * 100f else 0f
            }

            val parsedDailyOccs = ocorrencias.mapNotNull { occ ->
                val dataStr = occ.ocorrencia.data
                try {
                    val parts = dataStr.split("/", "-")
                    if (parts.size == 3) {
                        val isIso = parts[0].length == 4 // yyyy-mm-dd
                        val year = if (isIso) parts[0] else parts[2]
                        val month = parts[1]
                        val day = if (isIso) parts[2] else parts[0]
                        val sortKey = "$year$month$day"
                        val displayKey = "$day/$month"
                        Triple(sortKey, displayKey, occ.ocorrencia)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            
            val dailyGroups = parsedDailyOccs.groupBy { Pair(it.first, it.second) }.toSortedMap(compareBy { it.first })
            val letalidadeData = mutableMapOf<String, Float>()
            
            dailyGroups.forEach { (keys, list) ->
                val label = keys.second
                val vitimasFatais = list.sumOf { it.third.vitimasFatais }.toFloat()
                val vitimasTotal = list.sumOf { it.third.vitimas }.toFloat()
                letalidadeData[label] = if (vitimasTotal > 0) (vitimasFatais / vitimasTotal) * 100f else 0f
            }

            val calcDiff = { values: List<Float>, invertColors: Boolean ->
                if (values.size < 2) null
                else {
                    val current = values.last()
                    val previous = values[values.size - 2]
                    if (previous == 0f) null
                    else {
                        val diff = ((current - previous) / previous) * 100f
                        val isPositive = diff > 0
                        val symbol = if (isPositive) "↑" else "↓"
                        val goodColor = Color(0xFF43A047)
                        val badColor = Color(0xFFE53935)
                        val color = if (invertColors) {
                            if (isPositive) badColor else goodColor
                        } else {
                            if (isPositive) goodColor else badColor
                        }
                        Pair("$symbol ${String.format(Locale.US, "%.0f", kotlin.math.abs(diff))}%", color)
                    }
                }
            }

            val volumeDiff = calcDiff(volumeData.values.toList(), false)
            val efetividadeDiff = calcDiff(efetividadeData.values.toList(), false)
            val letalidadeDiff = calcDiff(letalidadeData.values.toList(), true)

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Volume Mensal de Ocorrências", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            LineChartWidget(
                                data = volumeData, 
                                lineColor = Color(0xFF64B5F6), 
                                gradientColor = Color(0xFF1E88E5),
                                badgeText = volumeDiff?.first,
                                badgeColor = volumeDiff?.second
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Taxa de Efetividade Mensal (%)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            LineChartWidget(
                                data = efetividadeData, 
                                isPercentage = true, 
                                lineColor = Color(0xFF81C784), 
                                gradientColor = Color(0xFF43A047),
                                badgeText = efetividadeDiff?.first,
                                badgeColor = efetividadeDiff?.second
                            )
                        }
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Índice de Letalidade Operacional Diário (%)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        LineChartWidget(
                            data = letalidadeData, 
                            isPercentage = true, 
                            lineColor = Color(0xFFE57373), 
                            gradientColor = Color(0xFFE53935),
                            badgeText = letalidadeDiff?.first,
                            badgeColor = letalidadeDiff?.second,
                            badgeSubText = "vs dia anterior"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        DistributionCard(title = "Distribuição por Natureza", data = ocorrencias.groupBy { it.ocorrencia.natureza }.mapValues { it.value.size })
                    }
                    item {
                        val addressNatureGroups = ocorrencias
                            .groupBy { "${it.ocorrencia.endereco} - ${it.ocorrencia.natureza}" }
                            .mapValues { it.value.size }
                        DistributionCard(title = "Top 5 Endereços", data = addressNatureGroups)
                    }
                    item {
                        DistributionCard(title = "Cidades com mais Ocorrências", data = ocorrencias.groupBy { it.ocorrencia.cidade }.mapValues { it.value.size })
                    }
                }
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
                title = "Total de\nOcorrências",
                value = ocorrencias.size.toString(),
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Bookmark,
                iconTint = Color(0xFF1976D2)
            )
            KpiCard(
                title = "Atendidas",
                value = atendidas.toString(),
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF43A047)
            )
            KpiCard(
                title = "QTA",
                value = qtas.toString(),
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Group,
                iconTint = Color(0xFF8E24AA)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                title = "Distância\nMédia",
                value = distanciaMedia,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Route,
                iconTint = Color(0xFF1976D2)
            )
            KpiCard(
                title = "Pico de\nAtendimento",
                value = peakHour,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.AccessTime,
                iconTint = Color(0xFFF57C00)
            )
            KpiCard(
                title = "Cidades\nAtendidas",
                value = cidades.toString(),
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocationCity,
                iconTint = Color(0xFF43A047)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                title = "VTRs\nEnvolvidas",
                value = vtrs.toString(),
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocalShipping,
                iconTint = Color(0xFFE53935)
            )
            KpiCard(
                title = "Vítimas / Fatais",
                value = "$totalVitimas / $fatais",
                modifier = Modifier.weight(1.5f),
                icon = Icons.Default.MonitorHeart,
                iconTint = Color(0xFFE53935),
                isGradient = true
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String, 
    value: String, 
    modifier: Modifier = Modifier, 
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    isGradient: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isGradient) {
                val gradientColor = if (isDark) Color(0xFFE53935).copy(alpha = 0.2f) else Color(0xFFE53935).copy(alpha = 0.1f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, gradientColor)
                            )
                        )
                )
            }
            
            Column(
                modifier = Modifier.padding(12.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
                Text(
                    text = value, 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(iconTint.copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.CircleShape)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = icon, 
                        contentDescription = null, 
                        tint = iconTint, 
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
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
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(curFraction)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
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

    val prontidaoColor = when (occ.prontidao?.lowercase()) {
        "azul" -> Color(0xFF1976D2)
        "verde" -> Color(0xFF43A047)
        "amarela", "amarelo" -> Color(0xFFF57C00)
        "vermelha", "vermelho" -> Color(0xFFE53935)
        else -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Barra lateral colorida de identificação
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(prontidaoColor)
            )
            
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Talão: ${occ.talao}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${occ.data} ${occ.qtrSaida}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "VTR: ${occ.vtr} | ${occ.natureza}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "${occ.endereco}, ${occ.cidade}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (expanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row {
                            Text(text = "Prontidão: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(text = "${occ.prontidao ?: "-"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = prontidaoColor)
                        }
                        Text(text = "Resultado: ${occ.resultado ?: "-"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(text = "Distância: ${occ.distancia?.let { "$it km" } ?: "-"}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Detalhes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Text(text = "Vítimas: ${occ.vitimas} | Fatais: ${occ.vitimasFatais}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "CMT VTR (Histórico): ${occ.cmtVtr}", style = MaterialTheme.typography.bodySmall)
                    if (!occ.observacoes.isNullOrBlank()) {
                        Text(text = "Obs: ${occ.observacoes}", style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Guarnição Associada (${ocorrenciaComMilitares.militares.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    if (ocorrenciaComMilitares.militares.isEmpty()) {
                        Text(text = "Nenhuma guarnição encontrada no banco local.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    } else {
                        ocorrenciaComMilitares.militares.forEach { mil ->
                            Text(text = "• ${mil.graduacao} ${mil.nomeGuerra} (RE ${mil.re})", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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
    var postoSelection by remember { mutableStateOf(state.filtroPosto ?: "") }
    var viaturaSelection by remember { mutableStateOf(state.filtroViaturaId ?: "") }

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

    // Helper for Cascading Filters
    fun matchesFilters(occ: com.andrefdias.dailynote.domain.model.OcorrenciaComMilitares, skipField: String): Boolean {
        if (skipField != "cidade" && cidade.isNotBlank() && occ.ocorrencia.cidade != cidade) return false
        if (skipField != "natureza" && natureza.isNotBlank() && occ.ocorrencia.natureza != natureza) return false
        if (skipField != "prontidao" && prontidao.isNotBlank() && occ.ocorrencia.prontidao != prontidao) return false
        if (skipField != "resultado" && resultado.isNotBlank() && occ.ocorrencia.resultado != resultado) return false
        return true
    }

    val availableCidades = remember(state.ocorrenciasTotais, natureza, prontidao, resultado, militarSelection) {
        state.ocorrenciasTotais.filter { matchesFilters(it, "cidade") }.mapNotNull { it.ocorrencia.cidade }.filter { it.isNotBlank() }.distinct().sorted()
    }
    
    val availableNaturezas = remember(state.ocorrenciasTotais, cidade, prontidao, resultado, militarSelection) {
        state.ocorrenciasTotais.filter { matchesFilters(it, "natureza") }.mapNotNull { it.ocorrencia.natureza }.filter { it.isNotBlank() }.distinct().sorted()
    }
    
    val availableProntidoes = remember(state.ocorrenciasTotais, cidade, natureza, resultado, militarSelection) {
        state.ocorrenciasTotais.filter { matchesFilters(it, "prontidao") }.mapNotNull { it.ocorrencia.prontidao }.filter { it.isNotBlank() }.distinct().sorted()
    }
    
    val availableResultados = remember(state.ocorrenciasTotais, cidade, natureza, prontidao, militarSelection) {
        state.ocorrenciasTotais.filter { matchesFilters(it, "resultado") }.mapNotNull { it.ocorrencia.resultado }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val militarOptions = listOf("Todos") + state.todosMilitares.map { "${it.graduacao} ${it.nomeGuerra} (RE: ${it.re})" }
    val initialMilitarOption = if (militarSelection.isBlank()) "Todos" else state.todosMilitares.find { it.id == militarSelection }?.let { "${it.graduacao} ${it.nomeGuerra} (RE: ${it.re})" } ?: "Todos"
    var selectedMilitarString by remember { mutableStateOf(initialMilitarOption) }
    
    val postoOptions = listOf("Todos") + state.todasViaturas.map { it.posto }.distinct().sorted()
    val viaturaOptions = listOf("Todas") + state.todasViaturas.map { it.prefixo }.distinct().sorted()
    val initialViaturaOption = if (viaturaSelection.isBlank()) "Todas" else state.todasViaturas.find { it.id == viaturaSelection }?.prefixo ?: "Todas"
    var selectedViaturaString by remember { mutableStateOf(initialViaturaOption) }

    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = { TextButton(onClick = { startDateMillis = startDatePickerState.selectedDateMillis; showStartDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = startDatePickerState) }
    }
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = { TextButton(onClick = { endDateMillis = endDatePickerState.selectedDateMillis; showEndDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = endDatePickerState) }
    }
    if (showStartTimePicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = { TextButton(onClick = { timeStart = String.format(Locale.getDefault(), "%02d:%02d", startTimePickerState.hour, startTimePickerState.minute); showStartTimePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showStartTimePicker = false }) { Text("Cancelar") } },
            text = { TimePicker(state = startTimePickerState) }
        )
    }
    if (showEndTimePicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = { TextButton(onClick = { timeEnd = String.format(Locale.getDefault(), "%02d:%02d", endTimePickerState.hour, endTimePickerState.minute); showEndTimePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showEndTimePicker = false }) { Text("Cancelar") } },
            text = { TimePicker(state = endTimePickerState) }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Filtros Dinâmicos", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold, 
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )

            // Período e Hora Card
            androidx.compose.material3.ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Período", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val startText = startDateMillis?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: "Início"
                        val endText = endDateMillis?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: "Fim"
                        
                        OutlinedButton(onClick = { showStartDatePicker = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(startText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(onClick = { showEndDatePicker = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(endText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showStartTimePicker = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(timeStart, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(onClick = { showEndTimePicker = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(timeEnd, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // Prontidão (Chips)
            Text("Prontidão", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.material3.FilterChip(
                    selected = prontidao.isBlank(),
                    onClick = { prontidao = "" },
                    label = { Text("Todas") }
                )
                availableProntidoes.forEach { opt ->
                    val color = when (opt.lowercase()) {
                        "azul" -> Color(0xFF1976D2)
                        "verde" -> Color(0xFF43A047)
                        "amarela", "amarelo" -> Color(0xFFF57C00)
                        "vermelha", "vermelho" -> Color(0xFFE53935)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    androidx.compose.material3.FilterChip(
                        selected = prontidao == opt,
                        onClick = { prontidao = if (prontidao == opt) "" else opt },
                        label = { Text(opt, color = if (prontidao == opt) Color.Unspecified else color, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Resultado (Chips)
            Text("Resultado", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.material3.FilterChip(
                    selected = resultado.isBlank(),
                    onClick = { resultado = "" },
                    label = { Text("Todos") }
                )
                availableResultados.forEach { opt ->
                    androidx.compose.material3.FilterChip(
                        selected = resultado == opt,
                        onClick = { resultado = if (resultado == opt) "" else opt },
                        label = { Text(opt) }
                    )
                }
            }

            // Dropdowns
            DropdownFilter(
                label = "Cidade",
                options = listOf("Todas") + availableCidades,
                selectedValue = if (cidade.isBlank()) "Todas" else cidade,
                onValueChange = { cidade = if (it == "Todas") "" else it }
            )

            DropdownFilter(
                label = "Natureza (Tipo)",
                options = listOf("Todas") + availableNaturezas,
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
                label = "Posto (Quartel)",
                options = postoOptions,
                selectedValue = if (postoSelection.isBlank()) "Todos" else postoSelection,
                onValueChange = { postoSelection = if (it == "Todos") "" else it }
            )

            DropdownFilter(
                label = "Viatura",
                options = viaturaOptions,
                selectedValue = selectedViaturaString,
                onValueChange = { sel -> 
                    selectedViaturaString = sel
                    if (sel == "Todas") {
                        viaturaSelection = ""
                    } else {
                        val vtr = state.todasViaturas.find { it.prefixo == sel }
                        viaturaSelection = vtr?.id ?: ""
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Sticky Bottom Buttons
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { 
                        viewModel.limparFiltros()
                        onClose() 
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Limpar", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
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
                        viewModel.setFiltroPosto(postoSelection)
                        viewModel.atualizarFiltroViatura(viaturaSelection.takeIf { it.isNotBlank() })
                        viewModel.setFiltroHorario(timeStart, timeEnd)
                        onClose()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Aplicar Filtros", fontWeight = FontWeight.Bold)
                }
            }
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
    lineColor: Color = Color(0xFF64B5F6),
    gradientColor: Color = Color(0xFF1E88E5),
    badgeText: String? = null,
    badgeColor: Color? = null,
    badgeSubText: String = "vs mês anterior"
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
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxWidth().height(180.dp).padding(vertical = 4.dp)) {
        if (badgeText != null && badgeColor != null) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = badgeText, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(badgeSubText, fontSize = 8.sp, color = badgeColor.copy(alpha = 0.7f), modifier = Modifier.padding(top = 2.dp))
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(start = 24.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
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
                    color = onSurfaceColor.copy(alpha = 0.1f),
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
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { index, label ->
                if (labels.size <= 6 || index % (labels.size / 6).coerceAtLeast(1) == 0 || index == labels.size - 1) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                } else if (labels.size <= 6) {
                     Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                } else {
                     Spacer(modifier = Modifier.width(1.dp))
                }
            }
        }
        }
    }
}

@Composable
fun DistributionCard(title: String, data: Map<String, Int>) {
    Card(
        modifier = Modifier.width(260.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            val sorted = data.toList().sortedByDescending { it.second }.take(5)
            val maxVal = sorted.maxOfOrNull { it.second } ?: 1
            
            sorted.forEach { (label, count) ->
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
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp
                    )
                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Text(
                        text = count.toString(),
                        modifier = Modifier.weight(0.2f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Ver todas", 
                color = MaterialTheme.colorScheme.primary, 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally).clickable { /* TODO */ }
            )
        }
    }
}
