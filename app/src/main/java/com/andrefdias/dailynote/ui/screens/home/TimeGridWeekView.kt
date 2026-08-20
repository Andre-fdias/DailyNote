package com.andrefdias.dailynote.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrefdias.dailynote.domain.model.CalendarEvento
import com.andrefdias.dailynote.domain.model.CalendarTarefa
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TimeGridWeekView(
    startDate: LocalDate,
    eventsMap: Map<LocalDate, List<CalendarEvento>>,
    tasksMap: Map<LocalDate, List<CalendarTarefa>>,
    onEventClick: (CalendarEvento) -> Unit = {},
    onTaskClick: (CalendarTarefa) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()
    
    val days = (0..6).map { startDate.plusDays(it.toLong()) }
    
    // Auto scroll to current hour or 08:00
    LaunchedEffect(startDate) {
        val hour = if (days.contains(LocalDate.now())) LocalTime.now().hour else 8
        listState.scrollToItem(hour.coerceAtLeast(0))
    }

    Column(modifier = Modifier.fillMaxWidth().height(450.dp)) {
        // Header with days
        Row(
            modifier = Modifier.fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
                .padding(start = 50.dp, bottom = 8.dp) // Offset for the time column
        ) {
            days.forEach { day ->
                val isToday = day == LocalDate.now()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(100.dp) // Fixed width per day column
                ) {
                    Text(
                        text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR")).uppercase(),
                        fontSize = 11.sp,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = day.dayOfMonth.toString(),
                        fontSize = 16.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(24) { hour ->
                    val hourString = String.format("%02d:00", hour)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                            .horizontalScroll(horizontalScrollState) // horizontal scroll on the row
                    ) {
                        // Time Column (scrolls with content horizontally in this basic setup, but better than nothing)
                        Text(
                            text = hourString,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(50.dp).padding(end = 8.dp, top = 8.dp),
                            textAlign = TextAlign.End
                        )
                        
                        days.forEach { day ->
                            val hourEvents = eventsMap[day]?.filter { it.hora?.startsWith(String.format("%02d", hour)) == true } ?: emptyList()
                            val hourTasks = tasksMap[day]?.filter { it.hora?.startsWith(String.format("%02d", hour)) == true } ?: emptyList()

                            Box(modifier = Modifier.width(100.dp).fillMaxHeight().border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))) {
                                // Events / Tasks
                                Column(modifier = Modifier.fillMaxSize().padding(1.dp)) {
                                    hourEvents.take(2).forEach { event ->
                                        val color = try { Color(android.graphics.Color.parseColor(event.cor)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                                        Box(
                                            modifier = Modifier.fillMaxWidth().weight(1f)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(color.copy(alpha = 0.2f))
                                                .clickable { onEventClick(event) }
                                                .padding(2.dp)
                                        ) {
                                            Text(event.titulo, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    hourTasks.take(1).forEach { task ->
                                        Box(
                                            modifier = Modifier.fillMaxWidth().weight(1f)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFE6A23C).copy(alpha = 0.2f))
                                                .clickable { onTaskClick(task) }
                                                .padding(2.dp)
                                        ) {
                                            Text(task.titulo, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE6A23C), maxLines = 1, overflow = TextOverflow.Ellipsis)
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
}
