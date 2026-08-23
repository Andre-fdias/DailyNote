package com.andrefdias.dailynote.ui.screens.historico

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Brush
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoDashboardScreen(
    viewModel: HistoricoDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Bottom Sheet State for Filters
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Operacional") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtros")
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
                1 -> MapaTab(ocorrencias)
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
            item {
                Text("Distribuição por Natureza", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                BarChartWidget(ocorrencias.groupBy { it.ocorrencia.natureza })
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
fun MapaTab(ocorrencias: List<OcorrenciaComMilitares>) {
    var mapType by remember { mutableStateOf("Normal") } // Normal, Satelite, Calor

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
                }
            },
            update = { mapView ->
                val googleSat = org.osmdroid.tileprovider.tilesource.XYTileSource(
                    "GoogleSat", 0, 19, 256, ".png",
                    arrayOf("https://mt1.google.com/vt/lyrs=s&x={x}&y={y}&z={z}")
                )
                when (mapType) {
                    "Satélite" -> mapView.setTileSource(googleSat)
                    else -> mapView.setTileSource(TileSourceFactory.MAPNIK)
                }
                
                mapView.overlays.clear()
                val points = mutableListOf<GeoPoint>()
                
                ocorrencias.forEach { occMil ->
                    val lat = occMil.ocorrencia.latitude
                    val lon = occMil.ocorrencia.longitude
                    if (lat != null && lon != null) {
                        val point = GeoPoint(lat, lon)
                        points.add(point)
                        
                        if (mapType == "Calor") {
                            // Heatmap approximation: semi-transparent red circle
                            val circle = org.osmdroid.views.overlay.Polygon(mapView)
                            circle.points = org.osmdroid.views.overlay.Polygon.pointsAsCircle(point, 2000.0) // 2km radius
                            circle.fillPaint.color = android.graphics.Color.argb(100, 255, 0, 0) // Semi-transparent red
                            circle.outlinePaint.color = android.graphics.Color.TRANSPARENT
                            mapView.overlays.add(circle)
                        } else {
                            val marker = Marker(mapView)
                            marker.position = point
                            marker.title = "${occMil.ocorrencia.natureza} - ${occMil.ocorrencia.cidade}"
                            marker.snippet = "Talão: ${occMil.ocorrencia.talao} | VTR: ${occMil.ocorrencia.vtr}"
                            mapView.overlays.add(marker)
                        }
                    }
                }
                if (points.isNotEmpty()) {
                    val bb = org.osmdroid.util.BoundingBox.fromGeoPoints(points)
                    mapView.post {
                        mapView.zoomToBoundingBox(bb, false)
                    }
                } else {
                    mapView.controller.setZoom(7.0)
                    mapView.controller.setCenter(GeoPoint(-23.5505, -46.6333)) // SP
                }
                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )
        
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color(0xAA000000), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val types = listOf("Normal", "Satélite", "Calor")
            types.forEach { type ->
                val selected = mapType == type
                Text(
                    text = type,
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.White,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.clickable { mapType = type }.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun KpiSection(ocorrencias: List<OcorrenciaComMilitares>) {
    val totalVitimas = ocorrencias.sumOf { it.ocorrencia.vitimas }
    val fatais = ocorrencias.sumOf { it.ocorrencia.vitimasFatais }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(
            title = "Total",
            value = ocorrencias.size.toString(),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.primaryContainer
        )
        KpiCard(
            title = "Vítimas",
            value = totalVitimas.toString(),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.secondaryContainer
        )
        KpiCard(
            title = "Fatais",
            value = fatais.toString(),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.errorContainer
        )
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
                
                Text(text = "Detalhes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text(text = "Vítimas: ${occ.vitimas} | Fatais: ${occ.vitimasFatais}", style = MaterialTheme.typography.bodySmall)
                Text(text = "CMT VTR (Histórico): ${occ.cmtVtr}", style = MaterialTheme.typography.bodySmall)
                
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
    val datePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = state.filtroDataInicio?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        initialSelectedEndDateMillis = state.filtroDataFim?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    )
    
    var cidade by remember { mutableStateOf(state.filtroCidade ?: "") }
    var natureza by remember { mutableStateOf(state.filtroNatureza ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Filtros", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp))

        DateRangePicker(
            state = datePickerState,
            modifier = Modifier.fillMaxWidth().height(400.dp),
            title = null,
            headline = null,
            showModeToggle = false
        )

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
            
            // Note: Para Militar, a API retorna todosMilitares. 
            // Vamos usar o ViewModel para filtrar por Militar também, caso deseje, 
            // mas o foco principal são Cidade e Natureza agora conforme solicitado.

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
                    val start = datePickerState.selectedStartDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() }
                    val end = datePickerState.selectedEndDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() }
                    
                    if (start != null && end != null) {
                        viewModel.atualizarFiltroData(start, end)
                    }
                    viewModel.setFiltroCidade(cidade)
                    viewModel.setFiltroNatureza(natureza)
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
