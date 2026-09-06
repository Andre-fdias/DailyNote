package com.andrefdias.dailynote.ui.screens.ocorrencias

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Cores Tematicas mantidas para graficos e icones independentemente do dark/light
private val BrandBlue = Color(0xFF2563EB)
private val BrandRed = Color(0xFFDC2626)
private val BrandGreen = Color(0xFF16A34A)
private val BrandYellow = Color(0xFFD97706)
private val BrandPurple = Color(0xFF7C3AED)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardOcorrenciasScreen(
    onNavigateToNova: () -> Unit,
    onNavigateToConsultar: () -> Unit,
    onNavigateToOpcoes: (String) -> Unit,
    viewModel: ConsultarOcorrenciasViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Temas dinamicos
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Geral", "Recursos", "Logística", "Geografia")
    var selectedFilter by remember { mutableStateOf("30 Dias") }
    val filters = listOf("Hoje", "7 Dias", "30 Dias", "Mês Atual", "Personalizado")

    // Filtrar ocorrencias baseado no selectedFilter
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val filteredOcorrencias = remember(state.ocorrencias, selectedFilter) {
        val today = LocalDate.now()
        state.ocorrencias.filter { ocorrencia ->
            try {
                val date = try {
                    LocalDate.parse(ocorrencia.data, formatter)
                } catch (e: Exception) {
                    try {
                        LocalDate.parse(ocorrencia.data)
                    } catch (e2: Exception) {
                        null
                    }
                }
                
                if (date != null) {
                    when (selectedFilter) {
                    "Hoje" -> date.isEqual(today)
                    "7 Dias" -> date.isAfter(today.minusDays(7)) || date.isEqual(today.minusDays(7))
                    "30 Dias" -> date.isAfter(today.minusDays(30)) || date.isEqual(today.minusDays(30))
                    "Mês Atual" -> date.monthValue == today.monthValue && date.year == today.year
                    else -> true
                }
                } else {
                    false
                }
            } catch (e: Exception) {
                false // Se nao der parse (ex: data vazia), exclui do filtro
            }
        }
    }

    // Calcula totais
    val totalOcorrencias = filteredOcorrencias.size
    
    // Categorizacao baseada na string de natureza
    val incendios = filteredOcorrencias.count { it.natureza.contains("incêndio", true) || it.natureza.contains("fogo", true) }
    val acidentes = filteredOcorrencias.count { it.natureza.contains("acidente", true) || it.natureza.contains("veículo", true) || it.natureza.contains("atropelamento", true) }
    val clinicos = filteredOcorrencias.count { it.natureza.contains("clínico", true) || it.natureza.contains("mal súbito", true) || it.natureza.contains("médica", true) || it.natureza.contains("gestante", true) }
    val outros = totalOcorrencias - incendios - acidentes - clinicos
    
    // Calcula Pessoas Envolvidas reais
    val gson = Gson()
    var totalPessoas = 0
    filteredOcorrencias.forEach { oco ->
        try {
            val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
            val pessoas: List<Map<String, Any>> = gson.fromJson(oco.pessoasJson, listType) ?: emptyList()
            totalPessoas += pessoas.size
        } catch(e: Exception) {}
    }
    
    // Calcula Grafico de Evolucao (Frequencia diaria)
    val evolutionPoints = remember(filteredOcorrencias) {
        val groups = filteredOcorrencias.mapNotNull { 
            try { 
                try { LocalDate.parse(it.data, formatter) } catch(e: Exception) { LocalDate.parse(it.data) } 
            } catch(e: Exception) { null } 
        }.groupBy { it }.mapValues { it.value.size }
        
        if (groups.isEmpty()) {
            emptyList<Float>()
        } else {
            val sortedKeys = groups.keys.sorted()
            val minDate = sortedKeys.first()
            val maxDate = sortedKeys.last().let { if (it.isEqual(minDate)) it.plusDays(1) else it }
            val daysBetween = ChronoUnit.DAYS.between(minDate, maxDate).toInt().coerceAtLeast(1)
            
            val points = mutableListOf<Float>()
            for (i in 0..daysBetween) {
                val d = minDate.plusDays(i.toLong())
                points.add((groups[d] ?: 0).toFloat())
            }
            if (points.size == 1) { points.add(points[0]) } // Duplica para formar uma reta caso tenha apenas 1 dia
            points
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Logo mock
                        Row(modifier = Modifier.padding(end = 8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(modifier = Modifier.size(6.dp, 16.dp).background(BrandYellow, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(6.dp, 20.dp).background(BrandRed, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(6.dp, 12.dp).background(BrandBlue, RoundedCornerShape(2.dp)))
                        }
                        Text("Dashboard Operacional", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary)
                    }
                },
                navigationIcon = { },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Filled.Refresh, "Atualizar", tint = textPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // FILTROS DE PERIODO
            item {
                Column {
                    Text("Filtrar por Período", color = textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEach { filter ->
                            val isSelected = filter == selectedFilter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandBlue else cardColor)
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = filter,
                                    color = if (isSelected) Color.White else textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // INDICADORES RAPIDOS
            item {
                Column {
                    Text("Indicadores Rápidos", color = textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiCard(
                            modifier = Modifier.weight(1f),
                            title = "Ocorrências",
                            value = totalOcorrencias.toString(),
                            iconColor = BrandBlue,
                            cardColor = cardColor,
                            textColor = textPrimary
                        )
                        KpiCard(
                            modifier = Modifier.weight(1f),
                            title = "Veículos Env.",
                            value = filteredOcorrencias.sumOf { oco -> 
                                // Parse veiculos json se houvesse, mas usaremos uma media ou outro dado real
                                // Vamos deixar veiculosEnv = 0 e atualizar depois
                                0
                            }.toString().let { if (it == "0") "-" else it }, // Placeholder para veiculos ou algo real
                            iconColor = BrandGreen,
                            cardColor = cardColor,
                            textColor = textPrimary
                        )
                        KpiCard(
                            modifier = Modifier.weight(1f),
                            title = "Pessoas Env.",
                            value = totalPessoas.toString(),
                            iconColor = BrandPurple,
                            cardColor = cardColor,
                            textColor = textPrimary
                        )
                    }
                }
            }

            // TABS
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = BrandBlue,
                    indicator = { tabPositions ->
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .height(3.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(BrandBlue)
                        )
                    },
                    divider = { HorizontalDivider(color = borderColor) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title, 
                                    color = if (selectedTab == index) BrandBlue else textSecondary,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }

            // GERAL CONTENT
            if (selectedTab == 0) {
                // 4 GRID CARDS
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NaturezaCard(modifier = Modifier.weight(1f), title = "Incêndios", count = incendios, total = totalOcorrencias, color = BrandRed, icon = Icons.Filled.LocalFireDepartment, cardColor = cardColor, textPrimary = textPrimary, textSecondary = textSecondary)
                            NaturezaCard(modifier = Modifier.weight(1f), title = "Trânsito/Acidentes", count = acidentes, total = totalOcorrencias, color = BrandGreen, icon = Icons.Filled.MinorCrash, cardColor = cardColor, textPrimary = textPrimary, textSecondary = textSecondary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NaturezaCard(modifier = Modifier.weight(1f), title = "Atend. Clínicos", count = clinicos, total = totalOcorrencias, color = BrandYellow, icon = Icons.Filled.MonitorHeart, cardColor = cardColor, textPrimary = textPrimary, textSecondary = textSecondary)
                            NaturezaCard(modifier = Modifier.weight(1f), title = "Outros", count = outros, total = totalOcorrencias, color = BrandPurple, icon = Icons.Filled.Category, cardColor = cardColor, textPrimary = textPrimary, textSecondary = textSecondary)
                        }
                    }
                }

                // EVOLUCAO TEMPORAL
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.BarChart, null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Evolução Temporal", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Text("Frequência de chamadas diárias", color = textSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 24.dp, start = 26.dp))
                            
                            LineChart(points = evolutionPoints, borderColor = borderColor, modifier = Modifier.fillMaxWidth().height(160.dp))
                        }
                    }
                }

                // DISTRIBUICAO POR NATUREZA
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PieChart, null, tint = BrandYellow, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Distribuição por Natureza", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Text("Percentual de ocorrências por tipo", color = textSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 24.dp, start = 26.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Donut Chart
                                Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                                    DonutChart(
                                        modifier = Modifier.fillMaxSize(),
                                        values = listOf(incendios.toFloat(), acidentes.toFloat(), clinicos.toFloat(), outros.toFloat()),
                                        colors = listOf(BrandRed, BrandGreen, BrandYellow, BrandPurple),
                                        borderColor = borderColor
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("TOTAL", color = textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("$totalOcorrencias", color = textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                        Text("Ocorrências", color = textSecondary, fontSize = 10.sp)
                                    }
                                }
                                
                                Spacer(Modifier.width(24.dp))
                                
                                // Legend
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                                    LegendItem("Incêndio", incendios, totalOcorrencias, BrandRed, textPrimary)
                                    LegendItem("Trânsito/Acid.", acidentes, totalOcorrencias, BrandGreen, textPrimary)
                                    LegendItem("A. Clínico", clinicos, totalOcorrencias, BrandYellow, textPrimary)
                                    LegendItem("Outros", outros, totalOcorrencias, BrandPurple, textPrimary)
                                }
                            }
                        }
                    }
                }

                // ATALHOS RAPIDOS
                item {
                    Column {
                        Text("Atalhos Rápidos", color = textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ShortcutCard(modifier = Modifier.weight(1f), title = "Nova\nOcorrência", icon = Icons.Filled.Add, color = BrandBlue, cardColor = cardColor, textSecondary = textSecondary, onClick = onNavigateToNova)
                            ShortcutCard(modifier = Modifier.weight(1f), title = "Consultar\nOcorrências", icon = Icons.Filled.Map, color = BrandGreen, cardColor = cardColor, textSecondary = textSecondary, onClick = onNavigateToConsultar)
                            ShortcutCard(modifier = Modifier.weight(1f), title = "Relatórios\nGerenciais", icon = Icons.Filled.Description, color = BrandYellow, cardColor = cardColor, textSecondary = textSecondary, onClick = {})
                            ShortcutCard(modifier = Modifier.weight(1f), title = "Mais\nOpções", icon = Icons.Filled.GridView, color = BrandPurple, cardColor = cardColor, textSecondary = textSecondary, onClick = {})
                        }
                    }
                }
                
                item { Spacer(Modifier.height(30.dp)) }
            } else {
                // Placeholder para outras abas
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Construction, null, tint = textSecondary, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Aba em Desenvolvimento", color = textSecondary, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(modifier: Modifier, title: String, value: String, iconColor: Color, cardColor: Color, textColor: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Box(modifier = Modifier.size(24.dp).background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Assessment, null, tint = iconColor, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(title, color = textColor.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1)
            }
            Text(value, color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NaturezaCard(modifier: Modifier, title: String, count: Int, total: Int, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, cardColor: Color, textPrimary: Color, textSecondary: Color) {
    val pct = if (total > 0) (count * 100) / total else 0
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(count.toString(), color = textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(title, color = textSecondary, fontSize = 12.sp)
                Text("$pct% do total", color = textSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun ShortcutCard(modifier: Modifier, title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, cardColor: Color, textSecondary: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier.aspectRatio(0.8f).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(title.replace("\\n", "\n"), color = textSecondary, fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 12.sp)
        }
    }
}

@Composable
fun LegendItem(label: String, count: Int, total: Int, color: Color, textPrimary: Color) {
    val pct = if (total > 0) (count * 100) / total else 0
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, color = textPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1)
        Text("$count", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Text("$pct%", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), textAlign = TextAlign.End)
    }
}

@Composable
fun LineChart(modifier: Modifier = Modifier, points: List<Float>, borderColor: Color) {
    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Sem dados no período", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Canvas(modifier = modifier) {
        val maxPoint = points.maxOrNull()?.coerceAtLeast(10f) ?: 10f
        
        val width = size.width
        val height = size.height
        val padding = 20f
        val usableHeight = height - padding * 2
        val usableWidth = width - padding * 2
        
        val stepX = if (points.size > 1) usableWidth / (points.size - 1) else usableWidth
        
        // Draw grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = padding + (usableHeight / gridLines) * i
            drawLine(
                color = borderColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }
        
        // Path
        val path = Path()
        val fillPath = Path()
        
        var currentX = padding
        
        points.forEachIndexed { index, value ->
            val y = padding + usableHeight - ((value / maxPoint) * usableHeight)
            if (index == 0) {
                path.moveTo(currentX, y)
                fillPath.moveTo(currentX, y)
            } else {
                path.lineTo(currentX, y)
                fillPath.lineTo(currentX, y)
            }
            
            // Pontos
            drawCircle(
                color = BrandBlue,
                radius = 6f,
                center = Offset(currentX, y)
            )
            drawCircle(
                color = Color.White,
                radius = 3f,
                center = Offset(currentX, y)
            )
            
            // Tooltip no ultimo
            if (index == points.size - 1) {
                drawRoundRect(
                    color = BrandBlue,
                    topLeft = Offset(currentX - 25f, y - 60f),
                    size = Size(50f, 40f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawLine(
                    color = BrandBlue,
                    start = Offset(currentX, y - 20f),
                    end = Offset(currentX, y),
                    strokeWidth = 2f
                )
            }
            
            if (points.size > 1) currentX += stepX
        }
        
        fillPath.lineTo(currentX - (if(points.size > 1) stepX else 0f), height - padding)
        fillPath.lineTo(padding, height - padding)
        fillPath.close()
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(BrandBlue.copy(alpha = 0.3f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )
        
        drawPath(
            path = path,
            color = BrandBlue,
            style = Stroke(width = 3f)
        )
    }
}

@Composable
fun DonutChart(modifier: Modifier = Modifier, values: List<Float>, colors: List<Color>, borderColor: Color) {
    val total = values.sum().takeIf { it > 0 } ?: 1f
    
    Canvas(modifier = modifier) {
        var startAngle = -90f
        val strokeWidth = 30f
        
        if (values.sum() == 0f) {
            drawArc(
                color = borderColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            return@Canvas
        }
        
        values.forEachIndexed { index, value ->
            val sweepAngle = (value / total) * 360f
            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweepAngle
        }
    }
}
