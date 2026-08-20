package com.andrefdias.dailynote.ui.screens.home

import android.graphics.Color as AndroidColor
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.domain.calendar.ScaleEngine
import com.andrefdias.dailynote.domain.model.*
import com.andrefdias.dailynote.ui.screens.calendar.CalendarUiState
import com.andrefdias.dailynote.ui.screens.calendar.CalendarViewModel
import com.andrefdias.dailynote.ui.screens.calendar.CalendarViewType
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ──────────────────────────────────────────────
// COLOR HELPER
// ──────────────────────────────────────────────
fun parseHexColor(hex: String, fallback: Color = Color(0xFFFF5252)): Color {
    return try { Color(AndroidColor.parseColor(hex)) } catch (e: Exception) { fallback }
}

// ──────────────────────────────────────────────
// TAB TYPE & FILTERS
// ──────────────────────────────────────────────
enum class HomeTabType { AGENDA, CHECKS }
enum class EventTypeFilter { ALL, EVENTS, TASKS }

// ──────────────────────────────────────────────
// HOME SCREEN
// ──────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CalendarViewModel,
    onNavigateToWizard: () -> Unit,
    onNavigateToConsult: () -> Unit
) {
    val calendarUiState by viewModel.uiState.collectAsState()

    val homeViewModel: HomeViewModel = hiltViewModel()

    val allEventos by homeViewModel.allEventos.collectAsState()
    val allTarefas by homeViewModel.allTarefas.collectAsState()
    val allNotifications by homeViewModel.notifications.collectAsState()
    val unreadCount by homeViewModel.unreadNotificationCount.collectAsState()
    val availableEscalas by homeViewModel.availableEscalas.collectAsState()
    val selectedEscalaFilter by homeViewModel.selectedEscalaFilter.collectAsState()
    val selectedDate by homeViewModel.selectedDate.collectAsState()
    val currentMonth by homeViewModel.currentMonth.collectAsState()
    val isRefreshing by homeViewModel.isRefreshing.collectAsState()
    val previewDays by homeViewModel.previewDays.collectAsState()

    var calendarViewType by remember { mutableStateOf(CalendarViewType.MONTH) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    var showAddEventSheet by remember { mutableStateOf(false) }
    var showAddTarefaSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(HomeTabType.AGENDA) }
    var showEventsFilter by remember { mutableStateOf(true) }
    var showTasksFilter by remember { mutableStateOf(true) }
    var eventToView by remember { mutableStateOf<CalendarEvento?>(null) }
    var taskToView by remember { mutableStateOf<CalendarTarefa?>(null) }

    // Navigate to wizard if not configured
    LaunchedEffect(calendarUiState.settingsLoaded, calendarUiState.settings) {
        if (!calendarUiState.settingsLoaded) return@LaunchedEffect
        if (!calendarUiState.settings.calendarioConfigurado) onNavigateToWizard()
    }

    BackHandler {
        if (calendarViewType != CalendarViewType.MONTH) {
            calendarViewType = CalendarViewType.MONTH
        }
    }

    // Build maps filtered by selected escala and event type
    val eventsMap = remember(allEventos, selectedEscalaFilter, showEventsFilter) {
        if (!showEventsFilter) emptyMap()
        else allEventos.filter {
            it.escalaId == null || selectedEscalaFilter == null || it.escalaId == selectedEscalaFilter
        }.groupBy { runCatching { LocalDate.parse(it.data) }.getOrNull() ?: LocalDate.now() }
    }
    val tasksMap = remember(allTarefas, selectedEscalaFilter, showTasksFilter) {
        if (!showTasksFilter) emptyMap()
        else allTarefas.filter {
            it.escalaId == null || selectedEscalaFilter == null || it.escalaId == selectedEscalaFilter
        }.groupBy { runCatching { LocalDate.parse(it.data) }.getOrNull() ?: LocalDate.now() }
    }

    // ── Notification Sheet ──────────────────────
    if (showNotificationSheet) {
        NotificationBottomSheet(
            notifications = allNotifications,
            unreadCount = unreadCount,
            onMarkAllRead = { homeViewModel.markAllNotificacoesAsRead() },
            onDeleteNotification = { homeViewModel.deleteNotificacao(it) },
            onClearAll = { homeViewModel.clearAllNotificacoes() },
            onDismiss = { showNotificationSheet = false }
        )
    }

    // ── Add Event/Task Form Sheet ─────────────────────────
    if (showAddEventSheet || showAddTarefaSheet) {
        val isTaskMode = showAddTarefaSheet
        EventTaskFormBottomSheet(
            initialIsTask = isTaskMode,
            initialDate = selectedDate,
            availableEscalas = availableEscalas,
            onDismiss = {
                showAddEventSheet = false
                showAddTarefaSheet = false
            },
            onSave = { isTask, titulo, data, hora, desc, corHex, escalaId, subtarefas ->
                if (isTask) {
                    homeViewModel.addTarefa(titulo = titulo, data = data, hora = hora, escalaId = escalaId, subtarefas = subtarefas)
                } else {
                    homeViewModel.addEvento(titulo = titulo, data = data, hora = hora, escalaId = escalaId)
                }
                showAddEventSheet = false
                showAddTarefaSheet = false
            }
        )
    }

    if (eventToView != null || taskToView != null) {
        EventTaskDetailBottomSheet(
            evento = eventToView,
            tarefa = taskToView,
            onDismiss = {
                eventToView = null
                taskToView = null
            },
            onDelete = {
                eventToView?.let { homeViewModel.deleteEvento(it.id) }
                taskToView?.let { homeViewModel.deleteTarefa(it.id) }
                eventToView = null
                taskToView = null
            }
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // ── TopBar identical to FireNote ──────
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
                    // Logo + App Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(34.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.LocalFireDepartment, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = "DailyNotes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFFFF7043))
                                )
                            )
                        )
                    }

                    // Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Notification bell with badge
                        Box(
                            modifier = Modifier.size(40.dp).clickable { showNotificationSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Notifications, "Notificações",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier.align(Alignment.TopEnd)
                                        .padding(top = 2.dp, end = 2.dp)
                                        .size(16.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                        color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Refresh
                        IconButton(onClick = { homeViewModel.refreshAll() }, modifier = Modifier.size(40.dp)) {
                            if (isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Icon(Icons.Outlined.Refresh, "Atualizar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            var showFabMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.offset(y = 40.dp)) {
                FloatingActionButton(
                    onClick = { showFabMenu = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, "Novo")
                }
                DropdownMenu(expanded = showFabMenu, onDismissRequest = { showFabMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("📅 Novo Evento") },
                        onClick = { showFabMenu = false; showAddEventSheet = true },
                        leadingIcon = { Icon(Icons.Outlined.Event, null, tint = MaterialTheme.colorScheme.primary) }
                    )
                    DropdownMenuItem(
                        text = { Text("✅ Nova Tarefa") },
                        onClick = { showFabMenu = false; showAddTarefaSheet = true },
                        leadingIcon = { Icon(Icons.Outlined.Checklist, null, tint = MaterialTheme.colorScheme.secondary) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Subtle gradient background (like FireNote)
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f + pulseAnim * 0.02f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f, endY = 600f
                    )
                )
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Welcome Header
                item {
                    WelcomeHeader(calendarUiState = calendarUiState)
                }

                // View Selector + Escala Filters
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalendarViewTypeSelector(
                            currentType = calendarViewType,
                            onTypeSelected = { calendarViewType = it }
                        )
                        // Escala filter chips row
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box {
                                var expanded by remember { mutableStateOf(false) }
                                val filterName = when (selectedEscalaFilter) {
                                    null -> "Escala 24x72"
                                    "NONE" -> "Ocultar"
                                    else -> availableEscalas.find { it.id == selectedEscalaFilter }?.nome ?: "Escala..."
                                }
                                Surface(
                                    color = Color(0xFF155C2B), // Dark green
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { expanded = true }.height(32.dp)
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 12.dp).fillMaxHeight(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                        Text(filterName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Filled.ArrowDropDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Todas Escalas") },
                                        onClick = { homeViewModel.setEscalaFilter(null); expanded = false },
                                        leadingIcon = { if (selectedEscalaFilter == null) Icon(Icons.Default.Check, null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Ocultar Escala") },
                                        onClick = { homeViewModel.setEscalaFilter("NONE"); expanded = false },
                                        leadingIcon = { if (selectedEscalaFilter == "NONE") Icon(Icons.Default.Check, null) }
                                    )
                                    if (availableEscalas.isNotEmpty()) {
                                        HorizontalDivider()
                                        availableEscalas.forEach { esc ->
                                            DropdownMenuItem(
                                                text = { Text(esc.nome) },
                                                onClick = { homeViewModel.setEscalaFilter(esc.id); expanded = false },
                                                leadingIcon = { if (selectedEscalaFilter == esc.id) Icon(Icons.Default.Check, null) }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Surface(
                                color = if (showEventsFilter) Color(0xFF155C2B) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp).clickable {
                                    showEventsFilter = !showEventsFilter
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp)) {
                                    Text("Eventos", color = if (showEventsFilter) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Surface(
                                color = if (showTasksFilter) Color(0xFF155C2B) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp).clickable {
                                    showTasksFilter = !showTasksFilter
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp)) {
                                    Text("Tarefas", color = if (showTasksFilter) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Calendar (animated between view types)
                item {
                    AnimatedContent(
                        targetState = calendarViewType,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                        label = "calView"
                    ) { vt ->
                        when (vt) {
                            CalendarViewType.MONTH -> MonthCalendarSection(
                                selectedDate = selectedDate,
                                currentMonth = currentMonth,
                                calendarUiState = calendarUiState,
                                eventsMap = eventsMap,
                                tasksMap = tasksMap,
                                previewDays = previewDays,
                                onDateClick = { homeViewModel.selectDate(it) },
                                onDateLongClick = { homeViewModel.selectDate(it); calendarViewType = CalendarViewType.DAY },
                                onPrevMonth = { homeViewModel.previousMonth() },
                                onNextMonth = { homeViewModel.nextMonth() }
                            )
                            CalendarViewType.WEEK -> WeekCalendarSection(
                                selectedDate = selectedDate,
                                calendarUiState = calendarUiState,
                                eventsMap = eventsMap,
                                tasksMap = tasksMap,
                                previewDays = previewDays,
                                onDateClick = { homeViewModel.selectDate(it); calendarViewType = CalendarViewType.DAY },
                                onEventClick = { eventToView = it },
                                onTaskClick = { taskToView = it }
                            )
                            CalendarViewType.DAY -> DayCalendarSection(
                                selectedDate = selectedDate,
                                events = eventsMap[selectedDate] ?: emptyList(),
                                tasks = tasksMap[selectedDate] ?: emptyList(),
                                calendarUiState = calendarUiState,
                                onEventClick = { eventToView = it },
                                onTaskClick = { taskToView = it }
                            )
                            else -> MonthCalendarSection(
                                selectedDate = selectedDate,
                                currentMonth = currentMonth,
                                calendarUiState = calendarUiState,
                                eventsMap = eventsMap,
                                tasksMap = tasksMap,
                                previewDays = previewDays,
                                onDateClick = { homeViewModel.selectDate(it) },
                                onDateLongClick = { homeViewModel.selectDate(it); calendarViewType = CalendarViewType.DAY },
                                onPrevMonth = { homeViewModel.previousMonth() },
                                onNextMonth = { homeViewModel.nextMonth() }
                            )
                        }
                    }
                }

                // Tabs: Agenda | Checks
                item {
                    DailyAgendaTabCard(
                        selectedDate = selectedDate,
                        events = eventsMap[selectedDate] ?: emptyList(),
                        tasks = tasksMap[selectedDate] ?: emptyList(),
                        calendarUiState = calendarUiState,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        onEventClick = { eventToView = it },
                        onTaskClick = { taskToView = it },
                        onToggleTask = { homeViewModel.toggleTarefa(it) },
                        onDeleteEvent = { homeViewModel.deleteEvento(it.id) },
                        onDeleteTask = { homeViewModel.deleteTarefa(it.id) }
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ──────────────────────────────────────────────
// WELCOME HEADER
// ──────────────────────────────────────────────
@Composable
fun WelcomeHeader(calendarUiState: CalendarUiState) {
    val today = remember {
        LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
        ).replaceFirstChar { it.lowercase() }
    }
    val hour = remember { LocalTime.now().hour }
    val greeting = when {
        hour < 12 -> "Bom dia"
        hour < 18 -> "Boa tarde"
        else -> "Boa noite"
    }
    val allActiveTeams = calendarUiState.activeTeamsRightNow.values.flatten()

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$greeting, Andre 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(text = "São Roque • $today", fontSize = 12.sp, color = Color(0xFFAAAAAA))
        }
        Spacer(Modifier.width(8.dp))
        // Team on duty badge
        if (allActiveTeams.isNotEmpty()) {
            val equipe = allActiveTeams.first()
            val c = parseHexColor(equipe.corFundo)
            Surface(shape = RoundedCornerShape(20.dp), color = c.copy(alpha = 0.15f)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.size(10.dp).background(c, CircleShape))
                    Text(equipe.sigla.ifBlank { equipe.nome.take(1) }, fontSize = 12.sp, color = c, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// CALENDAR VIEW TYPE SELECTOR
// ──────────────────────────────────────────────
@Composable
fun CalendarViewTypeSelector(
    currentType: CalendarViewType,
    onTypeSelected: (CalendarViewType) -> Unit
) {
    val types = listOf(
        CalendarViewType.MONTH to "📆 Mês",
        CalendarViewType.WEEK to "📅 Semana",
        CalendarViewType.DAY to "📋 Dia"
    )
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2E2E2E), // Dark gray
        modifier = Modifier.fillMaxWidth().height(44.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            types.forEach { (type, label) ->
                val selected = currentType == type
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) Color(0xFF7AA2F7) else Color.Transparent, // Light blue
                    modifier = Modifier.weight(1f).fillMaxHeight().clickable { onTypeSelected(type) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else Color(0xFFAAAAAA)
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// MONTH CALENDAR SECTION (Google-style cells with team color bg, no circles/dots)
// ──────────────────────────────────────────────
@Composable
fun MonthCalendarSection(
    selectedDate: LocalDate,
    currentMonth: LocalDate,
    calendarUiState: CalendarUiState,
    eventsMap: Map<LocalDate, List<CalendarEvento>>,
    tasksMap: Map<LocalDate, List<CalendarTarefa>>,
    previewDays: Map<LocalDate, Map<Int, List<EquipeConfig>>>,
    onDateClick: (LocalDate) -> Unit,
    onDateLongClick: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val ym = YearMonth.from(currentMonth)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)), // Dark gray card
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
            // Month nav header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(Icons.Filled.ChevronLeft, "Anterior", tint = Color.White)
                }
                Text(
                    text = "${ym.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR")).replaceFirstChar { it.uppercase() }} ${ym.year}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Filled.ChevronRight, "Próximo", tint = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Day of week labels
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB").forEachIndexed { i, d ->
                    Text(
                        text = d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF888888),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            // Calendar weeks
            val weeks = buildWeeks(ym)
            weeks.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { (date, isCurrentMonth) ->
                        if (date != null) {
                            GoogleStyleDayCell(
                                date = date,
                                isCurrentMonth = isCurrentMonth,
                                isSelected = date == selectedDate,
                                isToday = date == LocalDate.now(),
                                teamColors = getTeamColorsForDate(date, calendarUiState, previewDays),
                                events = eventsMap[date] ?: emptyList(),
                                tasks = tasksMap[date] ?: emptyList(),
                                modifier = Modifier.weight(1f),
                                onClick = { if (isCurrentMonth) onDateClick(date) },
                                onLongClick = { if (isCurrentMonth) onDateLongClick(date) }
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            // Legend
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                calendarUiState.teams.distinctBy { it.nome }.take(4).forEach { equipe ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp)) {
                        Box(Modifier.size(8.dp).background(parseHexColor(equipe.corFundo), CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text(equipe.sigla.ifBlank { equipe.nome }, fontSize = 10.sp, color = Color(0xFFAAAAAA))
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// GOOGLE STYLE DAY CELL
// ──────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoogleStyleDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    teamColors: List<Color>,
    events: List<CalendarEvento>,
    tasks: List<CalendarTarefa>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val hasDuty = teamColors.isNotEmpty()
    val dutyColor = teamColors.firstOrNull()

    // Base color for the cell. Image shows solid dark team colors, or transparent if no duty.
    val cellBg = when {
        hasDuty && isCurrentMonth -> dutyColor!!.copy(alpha = 0.7f) // solid but a bit muted to match image
        else -> Color.Transparent
    }

    val numColor = when {
        !isCurrentMonth -> Color.Gray.copy(alpha = 0.5f)
        else -> Color.White
    }

    val borderModifier = if (isSelected) Modifier.border(2.dp, Color(0xFF7AA2F7), RoundedCornerShape(8.dp)) else Modifier

    Column(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(3f / 4f)
            .then(borderModifier)
            .clip(RoundedCornerShape(8.dp))
            .background(cellBg)
            .combinedClickable(
                enabled = isCurrentMonth,
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            )
            .padding(top = 4.dp, start = 6.dp, end = 4.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = 13.sp
            ),
            color = if (isToday) Color(0xFFFF7043) else numColor
        )
        
        Spacer(Modifier.weight(1f))
        
        // Event blocks
        if (isCurrentMonth) {
            val allItems = (events.map { it.titulo to parseHexColor(it.cor) } + tasks.map { it.titulo to Color(0xFF9C27B0) }).take(2)
            allItems.forEach { (title, c) ->
                Box(
                    modifier = Modifier.fillMaxWidth().height(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(c)
                ) {
                    Text(
                        text = title, 
                        fontSize = 7.sp, 
                        color = Color.White, 
                        maxLines = 1, 
                        modifier = Modifier.padding(horizontal = 2.dp),
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(2.dp))
            }
        }
    }
}

// ──────────────────────────────────────────────
// WEEK CALENDAR SECTION (Vertical Day-by-Day)
// ──────────────────────────────────────────────
@Composable
fun WeekCalendarSection(
    selectedDate: LocalDate,
    calendarUiState: CalendarUiState,
    eventsMap: Map<LocalDate, List<CalendarEvento>>,
    tasksMap: Map<LocalDate, List<CalendarTarefa>>,
    previewDays: Map<LocalDate, Map<Int, List<EquipeConfig>>>,
    onDateClick: (LocalDate) -> Unit,
    onEventClick: (CalendarEvento) -> Unit,
    onTaskClick: (CalendarTarefa) -> Unit
) {
    val startOfWeek = selectedDate.with(java.time.DayOfWeek.SUNDAY)
    val daysOfWeek = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        daysOfWeek.forEach { date ->
            val eventsForDay = eventsMap[date] ?: emptyList()
            val tasksForDay = tasksMap[date] ?: emptyList()
            val fmt = java.time.format.DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", java.util.Locale.forLanguageTag("pt-BR"))
            val dateLabel = date.format(fmt).replaceFirstChar { it.uppercase() }
            
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onDateClick(date) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(dateLabel, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    if (eventsForDay.isEmpty() && tasksForDay.isEmpty()) {
                        Text("Sem agendamentos", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        if (eventsForDay.isNotEmpty()) {
                            AgendaEventsList(events = eventsForDay.sortedBy { it.hora ?: "" }, onClick = onEventClick, onDelete = {})
                        }
                        if (tasksForDay.isNotEmpty()) {
                            AgendaTasksList(tasks = tasksForDay.sortedBy { it.hora ?: "" }, onClick = onTaskClick, onToggle = {}, onDelete = {})
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// DAY CALENDAR SECTION
// ──────────────────────────────────────────────
@Composable
fun DayCalendarSection(
    selectedDate: LocalDate,
    events: List<CalendarEvento>,
    tasks: List<CalendarTarefa>,
    calendarUiState: CalendarUiState,
    onEventClick: (CalendarEvento) -> Unit,
    onTaskClick: (CalendarTarefa) -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("pt-BR"))
    val dateLabel = selectedDate.format(fmt).replaceFirstChar { it.uppercase() }

    val activeTeams = ScaleEngine.getActiveTeamsForDate(selectedDate, calendarUiState.scales, calendarUiState.teams)
        .values.flatten()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(dateLabel, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            if (activeTeams.isNotEmpty()) {
                activeTeams.forEach { equipe ->
                    val c = parseHexColor(equipe.corFundo)
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(c.copy(alpha = 0.12f)).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(Modifier.width(4.dp).height(28.dp).clip(RoundedCornerShape(2.dp)).background(c))
                        Text(equipe.nome, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.weight(1f))
                        Surface(color = c, shape = RoundedCornerShape(6.dp)) {
                            Text(equipe.sigla.ifEmpty { equipe.nome.take(3).uppercase() }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = parseHexColor(equipe.corTexto, Color.White), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
            }
            
            // Render vertical list of events and tasks
            if (events.isEmpty() && tasks.isEmpty()) {
                Text("Sem agendamentos", fontSize = 12.sp, color = Color.Gray)
            } else {
                if (events.isNotEmpty()) {
                    AgendaEventsList(events = events.sortedBy { it.hora ?: "" }, onClick = onEventClick, onDelete = {})
                }
                if (tasks.isNotEmpty()) {
                    AgendaTasksList(tasks = tasks.sortedBy { it.hora ?: "" }, onClick = onTaskClick, onToggle = {}, onDelete = {})
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// DAILY AGENDA TAB CARD
// ──────────────────────────────────────────────
@Composable
fun DailyAgendaTabCard(
    selectedDate: LocalDate,
    events: List<CalendarEvento>,
    tasks: List<CalendarTarefa>,
    calendarUiState: CalendarUiState,
    selectedTab: HomeTabType,
    onTabSelected: (HomeTabType) -> Unit,
    onEventClick: (CalendarEvento) -> Unit,
    onTaskClick: (CalendarTarefa) -> Unit,
    onToggleTask: (CalendarTarefa) -> Unit,
    onDeleteEvent: (CalendarEvento) -> Unit,
    onDeleteTask: (CalendarTarefa) -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("pt-BR"))
    val dateLabel = selectedDate.format(fmt)

    // Get team color for selected date
    val activeTeams = remember(selectedDate, calendarUiState.scales, calendarUiState.teams) {
        ScaleEngine.getActiveTeamsForDate(selectedDate, calendarUiState.scales, calendarUiState.teams).values.flatten()
    }
    val teamColor = activeTeams.firstOrNull()?.let { parseHexColor(it.corFundo) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)), // Dark gray
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Date label with team color dot
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(12.dp).background(teamColor ?: Color.Gray, CircleShape))
                Text(dateLabel, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            HorizontalDivider(color = Color(0xFF333333))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal, // no more shift for occurrences
                containerColor = Color.Transparent,
                contentColor = Color(0xFF7AA2F7),
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        height = 3.dp,
                        color = Color(0xFF7AA2F7)
                    )
                },
                divider = {}
            ) {
                HomeTabType.values().forEach { tab ->
                    val sel = selectedTab == tab
                    Tab(
                        selected = sel,
                        onClick = { onTabSelected(tab) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = when (tab) { HomeTabType.AGENDA -> "📅 Agenda"; HomeTabType.CHECKS -> "✅ Checks" },
                                    fontSize = 12.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (sel) Color(0xFF7AA2F7) else Color(0xFFAAAAAA)
                                )
                                val count = when (tab) {
                                    HomeTabType.AGENDA -> events.size
                                    HomeTabType.CHECKS -> tasks.count { it.status != StatusTarefa.CONCLUIDA }
                                }
                                if (count > 0) {
                                    Surface(shape = CircleShape, color = if(sel) Color(0xFF7AA2F7).copy(alpha=0.2f) else Color(0xFF333333), modifier = Modifier.size(18.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(count.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if(sel) Color(0xFF7AA2F7) else Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Tab content
            when (selectedTab) {
                HomeTabType.AGENDA -> AgendaEventsList(events = events.sortedBy { it.hora ?: "" }, onClick = onEventClick, onDelete = onDeleteEvent)
                HomeTabType.CHECKS -> AgendaTasksList(tasks = tasks.sortedBy { it.hora ?: "" }, onClick = onTaskClick, onToggle = onToggleTask, onDelete = onDeleteTask)
            }
        }
    }
}

// ──────────────────────────────────────────────
// EVENTS LIST
// ──────────────────────────────────────────────
@Composable
fun AgendaEventsList(events: List<CalendarEvento>, onClick: (CalendarEvento) -> Unit, onDelete: (CalendarEvento) -> Unit) {
    if (events.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
                Text("Nenhum evento agendado", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            events.forEach { event ->
                val ec = parseHexColor(event.cor, Color(0xFF9C27B0))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth().clickable { onClick(event) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left bar
                        Box(Modifier.width(3.dp).height(40.dp).background(ec, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(event.titulo, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(color = Color(0xFF1E3A8A), shape = RoundedCornerShape(4.dp)) {
                                        Text("Azul", color = Color(0xFF60A5FA), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                    Text("111", color = Color(0xFFAAAAAA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Outlined.LocationOn, null, tint = Color(0xFFAAAAAA), modifier = Modifier.size(12.dp))
                                    Text("São Roque", color = Color(0xFFAAAAAA), fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Outlined.Schedule, null, tint = Color(0xFFAAAAAA), modifier = Modifier.size(12.dp))
                                    Text(event.hora ?: "", color = Color(0xFFAAAAAA), fontSize = 11.sp)
                                }
                            }
                        }
                        
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF555555), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// TASKS LIST
// ──────────────────────────────────────────────
@Composable
fun AgendaTasksList(
    tasks: List<CalendarTarefa>,
    onClick: (CalendarTarefa) -> Unit,
    onToggle: (CalendarTarefa) -> Unit,
    onDelete: (CalendarTarefa) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.Checklist, null, tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
                Text("Nenhuma tarefa cadastrada", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val sorted = tasks.sortedWith(compareBy { it.status == StatusTarefa.CONCLUIDA })
            sorted.forEach { task ->
                val done = task.status == StatusTarefa.CONCLUIDA
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(if (done) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .clickable { onClick(task) }
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Checkbox(
                            checked = done,
                            onCheckedChange = { onToggle(task) },
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = task.titulo,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (done) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (task.hora != null && !done) Text(task.hora, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDelete(task) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                        }
                    }
                    if (task.checklist.isNotEmpty()) {
                        Column(modifier = Modifier.padding(start = 30.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            task.checklist.forEach { item ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        if (item.concluido) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (item.concluido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        item.titulo,
                                        fontSize = 12.sp,
                                        color = if (item.concluido) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textDecoration = if (item.concluido) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
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

// ──────────────────────────────────────────────
// NOTIFICATION BOTTOM SHEET
// ──────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
    notifications: List<CalendarNotificacao>,
    unreadCount: Int,
    onMarkAllRead: () -> Unit,
    onDeleteNotification: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Notificações", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                if (unreadCount > 0) {
                    TextButton(onClick = onMarkAllRead) { Text("Marcar todas lidas", fontSize = 12.sp) }
                }
            }
            if (notifications.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.NotificationsNone, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Text("Nenhuma notificação", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            } else {
                if (notifications.size > 1) {
                    TextButton(onClick = onClearAll, modifier = Modifier.align(Alignment.End)) { Text("Limpar todas", fontSize = 11.sp, color = MaterialTheme.colorScheme.error) }
                }
                notifications.take(20).forEach { notif ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(if (notif.lida) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(Modifier.size(8.dp).background(if (notif.lida) Color.Transparent else MaterialTheme.colorScheme.primary, CircleShape))
                        Column(Modifier.weight(1f)) {
                            Text(notif.titulo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${notif.data} • ${notif.hora}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                        IconButton(onClick = { onDeleteNotification(notif.id) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Outlined.Close, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}



// ──────────────────────────────────────────────
// HELPERS
// ──────────────────────────────────────────────
data class CalendarCellDay(val date: LocalDate?, val isCurrentMonth: Boolean)

fun buildWeeks(yearMonth: YearMonth): List<List<Pair<LocalDate?, Boolean>>> {
    val firstDay = yearMonth.atDay(1)
    val totalDays = yearMonth.lengthOfMonth()
    val startOffset = firstDay.dayOfWeek.value % 7 // Sun=0

    val weeks = mutableListOf<List<Pair<LocalDate?, Boolean>>>()
    var day = 1
    var offset = startOffset

    while (day <= totalDays) {
        val week = mutableListOf<Pair<LocalDate?, Boolean>>()
        for (col in 0..6) {
            when {
                offset > 0 -> { week.add(Pair(firstDay.minusDays(offset.toLong()), false)); offset-- }
                day <= totalDays -> { week.add(Pair(yearMonth.atDay(day), true)); day++ }
                else -> { week.add(Pair(null, false)) }
            }
        }
        weeks.add(week)
    }
    return weeks
}

fun getTeamColorsForDate(
    date: LocalDate,
    calendarUiState: CalendarUiState,
    previewDays: Map<LocalDate, Map<Int, List<EquipeConfig>>>
): List<Color> {
    val teamsMap = previewDays[date] ?: ScaleEngine.getActiveTeamsForDate(date, calendarUiState.scales, calendarUiState.teams)
    return teamsMap.values.flatten().mapNotNull {
        try { Color(AndroidColor.parseColor(it.corFundo)) } catch (e: Exception) { null }
    }.distinct()
}
