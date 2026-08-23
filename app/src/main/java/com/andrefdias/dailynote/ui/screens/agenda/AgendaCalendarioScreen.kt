package com.andrefdias.dailynote.ui.screens.agenda

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.ui.screens.home.parseHexColor
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import java.util.Locale
import com.andrefdias.dailynote.ui.screens.home.EventTaskDetailBottomSheet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AgendaCalendarioScreen(
    viewModel: AgendaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    
    val startMonth = YearMonth.now()
    var selectedTab by remember { mutableStateOf("Mês") }
    val pagerState = rememberPagerState(
        initialPage = 5000,
        pageCount = { 10000 }
    )
    
    var showAddSheet by remember { mutableStateOf(false) }
    
    var eventoSelecionado by remember { mutableStateOf<com.andrefdias.dailynote.domain.model.CalendarEvento?>(null) }
    var tarefaSelecionada by remember { mutableStateOf<com.andrefdias.dailynote.domain.model.CalendarTarefa?>(null) }
    
    // When month changes, precompute scales
    LaunchedEffect(pagerState.currentPage) {
        val currentDisplayMonth = startMonth.plusMonths((pagerState.currentPage - 5000).toLong())
        viewModel.precomputeScales(currentDisplayMonth.atDay(1))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Agenda", fontWeight = FontWeight.Bold) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (selectedTab != "Mês") {
                FloatingActionButton(
                    onClick = { 
                        eventoSelecionado = null
                        tarefaSelecionada = null
                        showAddSheet = true 
                    },
                    containerColor = Color(0xFF1E88E5), // Blue FAB
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar")
                }
            }
        },
        containerColor = Color(0xFF131722)
    ) { padding ->
        if (showAddSheet) {
            EventTaskDetailBottomSheet(
                evento = eventoSelecionado,
                tarefa = tarefaSelecionada,
                onDismiss = { 
                    showAddSheet = false
                    eventoSelecionado = null
                    tarefaSelecionada = null
                },
                onDelete = {
                    eventoSelecionado?.let { viewModel.deleteEvento(it) }
                    tarefaSelecionada?.let { viewModel.deleteTarefa(it) }
                    showAddSheet = false
                    eventoSelecionado = null
                    tarefaSelecionada = null
                },
                onSaveEvento = { id, titulo, data, hora, local, descricao, cor ->
                    viewModel.saveEvento(id = id, titulo = titulo, data = data, hora = hora.ifBlank { null }, local = local, descricao = descricao, cor = cor)
                },
                onSaveTarefa = { id, titulo, data, hora, descricao, cor, subtarefas ->
                    viewModel.saveTarefa(id = id, titulo = titulo, data = data, hora = hora.ifBlank { null }, descricao = descricao, cor = cor, checklist = subtarefas)
                }
            )
        }
    
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs Row Mock (Dia, Semana, Mês, Agenda)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color(0xFF1E2633), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Dia", "Semana", "Mês", "Agenda").forEach { tab ->
                    val isSelected = tab == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF90CAF9).copy(alpha=0.2f) else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF90CAF9) else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            when (selectedTab) {
                "Mês" -> {
                    // Calendar Container
                    val currentDisplayMonth = startMonth.plusMonths((pagerState.currentPage - 5000).toLong())
                            
                    CalendarDynamicHeader(
                        currentMonth = currentDisplayMonth,
                        onPreviousMonth = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        },
                        onNextMonth = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        },
                        onToday = {
                            scope.launch {
                                pagerState.animateScrollToPage(5000)
                                viewModel.selectDate(LocalDate.now())
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E2633))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth().animateContentSize(),
                            verticalAlignment = Alignment.Top
                        ) { page ->
                            val monthForPage = startMonth.plusMonths((page - 5000).toLong())
                            CalendarPremiumGrid(
                                currentMonth = monthForPage,
                                selectedDate = state.selectedDate,
                                onDateSelected = { viewModel.selectDate(it) },
                                onLongClickDate = { 
                                    viewModel.selectDate(it)
                                    selectedTab = "Dia" 
                                },
                                state = state
                            )
                        }
                    }
                }
                "Agenda" -> {
                    val allItems = (state.todosEventos + state.todasTarefas).sortedBy { 
                        when(it) {
                            is com.andrefdias.dailynote.domain.model.CalendarEvento -> it.data
                            is com.andrefdias.dailynote.domain.model.CalendarTarefa -> it.data
                            else -> ""
                        }
                    }
                    val groupedItems = allItems.groupBy { item ->
                        val date = when(item) {
                            is com.andrefdias.dailynote.domain.model.CalendarEvento -> item.data
                            is com.andrefdias.dailynote.domain.model.CalendarTarefa -> item.data
                            else -> ""
                        }
                        try {
                            val parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                            val mesStr = parsedDate.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("pt", "BR"))
                            "${mesStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} de ${parsedDate.year}"
                        } catch (e: Exception) {
                            "Desconhecido"
                        }
                    }

                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        groupedItems.forEach { (monthYear, items) ->
                            item {
                                Text(
                                    text = monthYear,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            items(items) { item ->
                                when(item) {
                                    is com.andrefdias.dailynote.domain.model.CalendarEvento -> EventoCard(evento = item, onClick = {
                                        eventoSelecionado = item
                                        tarefaSelecionada = null
                                        showAddSheet = true
                                    })
                                    is com.andrefdias.dailynote.domain.model.CalendarTarefa -> TarefaCard(
                                        tarefa = item, 
                                        onToggle = { viewModel.toggleTarefaStatus(item) },
                                        onClick = {
                                            tarefaSelecionada = item
                                            eventoSelecionado = null
                                            showAddSheet = true
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        if (allItems.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                    Text("Nenhum evento ou tarefa cadastrado.", color = Color.White.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
                "Dia" -> {
                    val dateStr = state.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val allItems = (state.todosEventos.filter { it.data == dateStr } + state.todasTarefas.filter { it.data == dateStr }).sortedBy {
                        when(it) {
                            is com.andrefdias.dailynote.domain.model.CalendarEvento -> it.hora ?: "23:59"
                            is com.andrefdias.dailynote.domain.model.CalendarTarefa -> it.hora ?: "23:59"
                            else -> ""
                        }
                    }
                    Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)) {
                        Text("Eventos de ${state.selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
                        if (allItems.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Agenda livre neste dia.", color = Color.White.copy(alpha = 0.5f))
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(allItems) { item ->
                                    val time = when(item) {
                                        is com.andrefdias.dailynote.domain.model.CalendarEvento -> item.hora ?: "O dia todo"
                                        is com.andrefdias.dailynote.domain.model.CalendarTarefa -> item.hora ?: "O dia todo"
                                        else -> ""
                                    }
                                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                        Column(
                                            modifier = Modifier.width(60.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = time, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.White.copy(alpha=0.1f)).padding(vertical = 4.dp))
                                        }
                                        Box(modifier = Modifier.weight(1f).padding(bottom = 16.dp)) {
                                            when(item) {
                                                is com.andrefdias.dailynote.domain.model.CalendarEvento -> EventoCard(evento = item, onClick = {
                                                    eventoSelecionado = item
                                                    tarefaSelecionada = null
                                                    showAddSheet = true
                                                })
                                                is com.andrefdias.dailynote.domain.model.CalendarTarefa -> TarefaCard(
                                                    tarefa = item, 
                                                    onToggle = { viewModel.toggleTarefaStatus(item) },
                                                    onClick = {
                                                        tarefaSelecionada = item
                                                        eventoSelecionado = null
                                                        showAddSheet = true
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "Semana" -> {
                    val startOfWeek = state.selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                    
                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Text(
                            "Semana: ${startOfWeek.format(DateTimeFormatter.ofPattern("dd/MM"))} a ${startOfWeek.plusDays(6).format(DateTimeFormatter.ofPattern("dd/MM"))}", 
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        )
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(7) { dayOffset ->
                                val currentDay = startOfWeek.plusDays(dayOffset.toLong())
                                val dateStr = currentDay.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val dayItems = (state.todosEventos.filter { it.data == dateStr } + state.todasTarefas.filter { it.data == dateStr }).sortedBy {
                                    when(it) {
                                        is com.andrefdias.dailynote.domain.model.CalendarEvento -> it.hora ?: "23:59"
                                        is com.andrefdias.dailynote.domain.model.CalendarTarefa -> it.hora ?: "23:59"
                                        else -> ""
                                    }
                                }
                                
                                val diaDaSemana = currentDay.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("pt", "BR"))
                                val diaFormatado = currentDay.format(DateTimeFormatter.ofPattern("dd/MM"))
                                
                                Column(
                                    modifier = Modifier
                                        .width(280.dp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E2633))
                                        .border(1.dp, Color.White.copy(alpha=0.05f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "${diaDaSemana.uppercase()} - $diaFormatado",
                                        color = if (currentDay == LocalDate.now()) Color(0xFF90CAF9) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    
                                    if (dayItems.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("Livre", color = Color.White.copy(alpha = 0.3f))
                                        }
                                    } else {
                                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(dayItems) { item ->
                                                when(item) {
                                                    is com.andrefdias.dailynote.domain.model.CalendarEvento -> EventoCard(evento = item, onClick = {
                                                        eventoSelecionado = item
                                                        tarefaSelecionada = null
                                                        showAddSheet = true
                                                    })
                                                    is com.andrefdias.dailynote.domain.model.CalendarTarefa -> TarefaCard(
                                                        tarefa = item, 
                                                        onToggle = { viewModel.toggleTarefaStatus(item) },
                                                        onClick = {
                                                            tarefaSelecionada = item
                                                            eventoSelecionado = null
                                                            showAddSheet = true
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CalendarDynamicHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit
) {
    val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }
    val year = currentMonth.year

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$monthName $year",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior", tint = Color.White)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .clickable { onToday() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Hoje", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Próximo", tint = Color.White)
            }
        }
    }
}

@Composable
fun CalendarPremiumGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onLongClickDate: (LocalDate) -> Unit,
    state: AgendaState
) {
    val daysOfWeek = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB")
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOffset = if (firstDayOfMonth.dayOfWeek.value == 7) 0 else firstDayOfMonth.dayOfWeek.value
    val daysInMonth = currentMonth.lengthOfMonth()

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        // Week days header
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Divider(color = Color.White.copy(alpha = 0.05f))

        var currentDay = 1
        var weekCount = 0
        
        while (currentDay <= daysInMonth) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (i in 0..6) {
                    if (weekCount == 0 && i < firstDayOffset) {
                        // Empty cell
                        Box(modifier = Modifier.weight(1f).height(100.dp).border(0.5.dp, Color.White.copy(alpha = 0.02f))) {
                            val previousMonth = currentMonth.minusMonths(1)
                            val dayNum = previousMonth.lengthOfMonth() - firstDayOffset + 1 + i
                            Text(
                                text = dayNum.toString(),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
                            )
                        }
                    } else if (currentDay <= daysInMonth) {
                        val date = currentMonth.atDay(currentDay)
                        CalendarPremiumCell(
                            date = date,
                            isSelected = date == selectedDate,
                            isToday = date == LocalDate.now(),
                            state = state,
                            onClick = { onDateSelected(date) },
                            onLongClick = { onLongClickDate(date) },
                            modifier = Modifier.weight(1f)
                        )
                        currentDay++
                    } else {
                        // Empty cell
                        Box(modifier = Modifier.weight(1f).height(100.dp).border(0.5.dp, Color.White.copy(alpha = 0.02f))) {
                            val dayNum = currentDay - daysInMonth
                            Text(
                                text = dayNum.toString(),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
                            )
                            currentDay++
                        }
                    }
                }
            }
            weekCount++
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarPremiumCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    state: AgendaState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val eventosHoje = state.todosEventos.filter { it.data == dateStr }
    val tarefasHoje = state.todasTarefas.filter { it.data == dateStr }
    
    val hasEvent = eventosHoje.isNotEmpty()
    val hasTarefa = tarefasHoje.isNotEmpty()
    val isOcorrencia = false // Mock para ocorrencias

    val activeTeams = state.escalasPorDia[date]?.values?.flatten()
    val bgEquipeHex = activeTeams?.firstOrNull()?.corFundo
    val bgEquipeColor = if (bgEquipeHex != null) parseHexColor(bgEquipeHex, Color.Transparent).copy(alpha = 0.15f) else Color.Transparent

    Box(
        modifier = modifier
            .height(100.dp) // Taller cell as requested
            .zIndex(if (isSelected) 1f else 0f)
            .border(0.5.dp, Color.White.copy(alpha = 0.05f))
            .background(if (isSelected) Color(0xFF283546) else Color.Transparent)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Team Banner
            if (activeTeams?.isNotEmpty() == true) {
                val equipe = activeTeams.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgEquipeColor.copy(alpha = 0.8f))
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = equipe.nome,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(18.dp))
            }
            
            // Date Number
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 13.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) Color(0xFF90CAF9) else Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .padding(start = 4.dp, top = 2.dp)
                    .background(if(isToday) Color(0xFF90CAF9).copy(alpha=0.2f) else Color.Transparent, CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )

            // Events List in Cell
            Column(modifier = Modifier.padding(2.dp).fillMaxWidth()) {
                val allItems = (eventosHoje.map { it.titulo } + tarefasHoje.map { it.titulo }).take(3)
                allItems.forEach { title ->
                    Text(
                        text = "• $title",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        if (isSelected) {
            // Highlight Border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, Color(0xFF90CAF9))
            )
        }
    }
}



@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
    }
}
