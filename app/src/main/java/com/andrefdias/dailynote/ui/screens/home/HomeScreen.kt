package com.andrefdias.dailynote.ui.screens.home

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.domain.model.*
import com.andrefdias.dailynote.ui.screens.calendar.CalendarUiState
import com.andrefdias.dailynote.ui.screens.calendar.CalendarViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.andrefdias.dailynote.ui.screens.calendar.NotificationBottomSheet
import com.andrefdias.dailynote.ui.screens.calendar.NotificationCenterViewModel

fun parseHexColor(hex: String, fallback: Color = Color(0xFFFF5252)): Color {
    return try { Color(AndroidColor.parseColor(hex)) } catch (e: Exception) { fallback }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CalendarViewModel,
    onNavigateToWizard: () -> Unit,
    onNavigateToConsult: () -> Unit,
    onNavigateToHistoricoMapa: () -> Unit,
    onNavigateToViaturas: () -> Unit,
    onNavigateToNova: () -> Unit,
    onNavigateToRelatorios: () -> Unit,
    onNavigateToMapaForca: () -> Unit
) {
    val calendarUiState by viewModel.uiState.collectAsState()
    val homeViewModel: HomeViewModel = hiltViewModel()

    val occurrencesToday     by homeViewModel.occurrencesToday.collectAsState()
    val occurrencesThisMonth by homeViewModel.occurrencesThisMonth.collectAsState()
    val occurrencesTotal     by homeViewModel.occurrencesTotal.collectAsState()
    val viaturasEmProntidao  by homeViewModel.viaturasEmProntidao.collectAsState()
    val evolutionData        by homeViewModel.evolutionData.collectAsState()
    val unreadCount          by homeViewModel.unreadNotificationCount.collectAsState()
    val isRefreshing         by homeViewModel.isRefreshing.collectAsState()

    var showNotificationSheet by remember { mutableStateOf(false) }

    LaunchedEffect(calendarUiState.settingsLoaded, calendarUiState.settings) {
        if (!calendarUiState.settingsLoaded) return@LaunchedEffect
        if (!calendarUiState.settings.calendarioConfigurado) onNavigateToWizard()
    }

    val notifViewModel: NotificationCenterViewModel = hiltViewModel()
    if (showNotificationSheet) {
        NotificationBottomSheet(
            viewModel = notifViewModel,
            onDismiss = { showNotificationSheet = false },
            onNotificationClick = { }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DashboardTopBar(
                unreadCount = unreadCount,
                isRefreshing = isRefreshing,
                onNotificationClick = { showNotificationSheet = true },
                onRefreshClick = { homeViewModel.refreshAll() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item { DashboardWelcomeHeader(calendarUiState = calendarUiState) }
            item {
                DashboardStatsRow(
                    occurrencesToday = occurrencesToday,
                    occurrencesMonth = occurrencesThisMonth,
                    victimsMonth = occurrencesTotal,
                    evolutionData = evolutionData
                )
            }
            item {
                QuickActionsSection(
                    onNova = onNavigateToNova,
                    onMapaOperacional = onNavigateToHistoricoMapa,
                    onRelatorios = onNavigateToRelatorios,
                    onMapaForca = onNavigateToMapaForca,
                    onConsultar = onNavigateToConsult
                )
            }
            item { MapaOperacionalSection(onVerMapa = onNavigateToHistoricoMapa) }
            item {
                ViaturasProntidaoSection(
                    viaturas = viaturasEmProntidao,
                    onVerTodas = onNavigateToViaturas
                )
            }
            item { EvolucaoTemporalSection(evolutionData = evolutionData) }
            item { AgendaDiaSection(calendarUiState = calendarUiState) }
        }
    }
}

@Composable
private fun DashboardTopBar(
    unreadCount: Int,
    isRefreshing: Boolean,
    onNotificationClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.97f),
        tonalElevation = 0.dp, shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = "DailyNotes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFFFF7043)))
                    )
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.size(40.dp).clickable { onNotificationClick() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Notifications, "Notificacoes", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier.align(Alignment.TopEnd).padding(top = 2.dp, end = 2.dp).size(16.dp).background(MaterialTheme.colorScheme.error, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (unreadCount > 9) "9+" else unreadCount.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                IconButton(onClick = onRefreshClick, modifier = Modifier.size(40.dp)) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(Icons.Outlined.Refresh, "Atualizar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardWelcomeHeader(calendarUiState: CalendarUiState) {
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))).replaceFirstChar { it.lowercase() }
    }
    val hour = remember { LocalTime.now().hour }
    val greeting = when {
        hour < 12 -> "Bom dia"
        hour < 18 -> "Boa tarde"
        else -> "Boa noite"
    }
    val allActiveTeams = calendarUiState.activeTeamsRightNow.values.flatten()

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "$greeting, Andre \uD83D\uDC4B", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(3.dp))
            Text(text = "Sao Roque \u2022 $today", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        if (allActiveTeams.isNotEmpty()) {
            val equipe = allActiveTeams.first()
            val c = parseHexColor(equipe.corFundo)
            Surface(shape = RoundedCornerShape(20.dp), color = c.copy(alpha = 0.15f)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.size(10.dp).background(c, CircleShape))
                    Text(equipe.sigla.ifBlank { equipe.nome.take(2) }, fontSize = 12.sp, color = c, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DashboardStatsRow(
    occurrencesToday: Int,
    occurrencesMonth: Int,
    victimsMonth: Int,
    evolutionData: Map<String, Int>
) {
    val sparkPoints = remember(evolutionData) { evolutionData.values.toList().takeLast(10).map { v -> v.toFloat() } }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "OCORRENCIAS\n(HOJE)",
            value = occurrencesToday.toString(),
            trend = "-2 em relacao a ontem",
            accentColor = Color(0xFFE53935),
            sparkData = sparkPoints
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "OCORRENCIAS\n(MES)",
            value = occurrencesMonth.toString(),
            trend = "+9% em relacao ao mes anterior",
            accentColor = Color(0xFFFFA000),
            sparkData = sparkPoints
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "VITIMAS\n(MES)",
            value = victimsMonth.toString(),
            trend = "+9% em relacao ao mes anterior",
            accentColor = Color(0xFF1E88E5),
            sparkData = sparkPoints
        )
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, label: String, value: String, trend: String, accentColor: Color, sparkData: List<Float>) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = accentColor, lineHeight = 12.sp)
            Text(text = value, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = trend, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 11.sp, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            SparklineChart(data = sparkData, color = accentColor, modifier = Modifier.fillMaxWidth().height(32.dp))
        }
    }
}

@Composable
private fun SparklineChart(data: List<Float>, color: Color, modifier: Modifier = Modifier) {
    if (data.size < 2) return
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val maxVal = data.max().coerceAtLeast(1f)
        val minVal = data.min()
        val range = (maxVal - minVal).coerceAtLeast(1f)
        val w = size.width; val h = size.height; val step = w / (data.size - 1)
        val path = Path()
        data.forEachIndexed { i, v ->
            val x = i * step; val y = h - ((v - minVal) / range) * h
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        val fillPath = Path().apply { addPath(path); lineTo(w, h); lineTo(0f, h); close() }
        drawPath(fillPath, color.copy(alpha = 0.15f))
        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun QuickActionsSection(onNova: () -> Unit, onMapaOperacional: () -> Unit, onRelatorios: () -> Unit, onMapaForca: () -> Unit, onConsultar: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "Acoes rapidas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            QuickActionButton(icon = Icons.Filled.AddCircle, label = "Nova\nOcorrencia", iconColor = Color(0xFFE53935), onClick = onNova)
            QuickActionButton(icon = Icons.Filled.Map, label = "Mapa\nOperacional", iconColor = Color(0xFF43A047), onClick = onMapaOperacional)
            QuickActionButton(icon = Icons.Filled.Search, label = "Consultar\nOcorrencia", iconColor = Color(0xFF039BE5), onClick = onConsultar)
            QuickActionButton(icon = Icons.Filled.BarChart, label = "Relatorios", iconColor = Color(0xFFFFA000), onClick = onRelatorios)
            QuickActionButton(icon = Icons.Filled.Analytics, label = "Mapa\nForca", iconColor = Color(0xFF8E24AA), onClick = onMapaForca)
        }
    }
}

@Composable
private fun QuickActionButton(icon: ImageVector, label: String, iconColor: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(8.dp).width(60.dp)
    ) {
        Box(modifier = Modifier.size(48.dp).background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(26.dp))
        }
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, lineHeight = 13.sp)
    }
}

@Composable
private fun MapaOperacionalSection(onVerMapa: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Mapa Operacional", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = onVerMapa, contentPadding = PaddingValues(0.dp)) {
                    Text("Ver mapa completo \u2192", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8F5E9)).clickable { onVerMapa() }) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridColor = Color(0xFF9E9E9E).copy(alpha = 0.2f)
                    for (i in 1..4) { val y = size.height * i / 5f; drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx()) }
                    for (i in 1..5) { val x = size.width * i / 6f; drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx()) }
                }
                Text("Jardim Marmeleiro", fontSize = 9.sp, color = Color(0xFF555555), modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp))
                Text("Sao Roque", fontSize = 9.sp, color = Color(0xFF555555), modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp))
                Text("Campinha", fontSize = 9.sp, color = Color(0xFF555555), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp, end = 20.dp))
                val dots = listOf(
                    Triple(0.50f, 0.22f, Color(0xFFE53935)),
                    Triple(0.60f, 0.45f, Color(0xFFFFA000)),
                    Triple(0.42f, 0.48f, Color(0xFF1E88E5)),
                    Triple(0.68f, 0.55f, Color(0xFF43A047)),
                    Triple(0.35f, 0.65f, Color(0xFFE53935))
                )
                dots.forEach { (xRatio, yRatio, dotColor) ->
                    Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.TopStart).offset(x = (xRatio * 300).dp, y = (yRatio * 150).dp)) {
                        Box(modifier = Modifier.size(18.dp).background(dotColor.copy(alpha = 0.25f), CircleShape).align(Alignment.Center))
                        Box(modifier = Modifier.size(10.dp).background(dotColor, CircleShape).align(Alignment.Center))
                    }
                }
                Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.White.copy(alpha = 0.75f)).padding(horizontal = 8.dp, vertical = 5.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("Incendio" to Color(0xFFE53935), "Resgate" to Color(0xFFFFA000), "Salvamento" to Color(0xFF43A047), "Acidente" to Color(0xFF1E88E5)).forEach { (lbl, c) ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(Modifier.size(8.dp).background(c, CircleShape))
                            Text(lbl, fontSize = 9.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViaturasProntidaoSection(viaturas: List<Viatura>, onVerTodas: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Viaturas em Prontidao", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = onVerTodas, contentPadding = PaddingValues(0.dp)) {
                    Text("Ver todas \u2192", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
            if (viaturas.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Nenhuma viatura em prontidao hoje", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(viaturas) { viatura -> ViaturaCard(viatura = viatura) }
                }
            }
        }
    }
}

@Composable
private fun ViaturaCard(viatura: Viatura) {
    val statusColor = when (viatura.status.lowercase()) {
        "operacional" -> Color(0xFF43A047)
        "manutencao" -> Color(0xFFFFA000)
        else -> Color(0xFFE53935)
    }
    Card(modifier = Modifier.width(120.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Text(text = viatura.prefixo, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(text = viatura.tipo, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(7.dp).background(statusColor, CircleShape))
                Text(text = viatura.status, fontSize = 9.sp, color = statusColor, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun EvolucaoTemporalSection(evolutionData: Map<String, Int>) {
    val chartData = remember(evolutionData) { evolutionData.values.toList().takeLast(15).map { v -> v.toFloat() } }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.TrendingUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Column {
                    Text("Evolucao Temporal", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Ocorrencias por dia (ultimos registros)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (chartData.size < 2) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    Text("Sem dados suficientes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                EvolutionLineChart(data = chartData, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth().height(90.dp))
            }
        }
    }
}

@Composable
private fun EvolutionLineChart(data: List<Float>, color: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val maxVal = data.max().coerceAtLeast(1f); val minVal = data.min(); val range = (maxVal - minVal).coerceAtLeast(1f)
        val w = size.width; val h = size.height; val step = w / (data.size - 1); val padding = 8.dp.toPx()
        val path = Path()
        val points = data.mapIndexed { i, v -> Offset(i * step, h - padding - ((v - minVal) / range) * (h - padding * 2)) }
        points.forEachIndexed { i, pt -> if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y) }
        val fillPath = Path().apply { addPath(path); lineTo(w, h); lineTo(0f, h); close() }
        drawPath(fillPath, Brush.verticalGradient(listOf(color.copy(alpha = 0.25f), color.copy(alpha = 0.02f))))
        drawPath(path, color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
        points.forEach { pt ->
            drawCircle(Color.White, radius = 4.dp.toPx(), center = pt)
            drawCircle(color, radius = 3.dp.toPx(), center = pt)
        }
    }
}

@Composable
private fun AgendaDiaSection(calendarUiState: CalendarUiState) {
    val todayStr = LocalDate.now().toString()
    val todayEvents = calendarUiState.events.filter { it.data == todayStr }.sortedBy { it.hora ?: "23:59" }
    val todayTasks = calendarUiState.tasks.filter { it.data == todayStr }.sortedBy { it.hora ?: "23:59" }
    
    val hasItems = todayEvents.isNotEmpty() || todayTasks.isNotEmpty()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Agenda de Hoje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!hasItems) {
                Text(
                    text = "Nenhum evento ou tarefa para hoje.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                todayEvents.forEach { event ->
                    AgendaItemRow(
                        title = event.titulo,
                        time = event.hora,
                        type = "Evento",
                        colorHex = event.cor,
                        icon = Icons.Default.Event
                    )
                }
                todayTasks.forEach { task ->
                    AgendaItemRow(
                        title = task.titulo,
                        time = task.hora,
                        type = "Tarefa",
                        colorHex = "#2196F3",
                        icon = Icons.Default.CheckCircleOutline
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaItemRow(
    title: String,
    time: String?,
    type: String,
    colorHex: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(parseHexColor(colorHex).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = parseHexColor(colorHex),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = type,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (time != null) {
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
