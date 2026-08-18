package com.andrefdias.dailynote.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TimeGridDayView(
    date: LocalDate,
    events: List<CalendarEvento>,
    tasks: List<CalendarTarefa>,
    onEventClick: (CalendarEvento) -> Unit = {},
    onTaskClick: (CalendarTarefa) -> Unit = {}
) {
    val listState = rememberLazyListState()
    
    // Auto scroll to current hour or 08:00
    LaunchedEffect(date) {
        val hour = if (date == LocalDate.now()) LocalTime.now().hour else 8
        listState.scrollToItem(hour.coerceAtLeast(0))
    }

    Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(24) { hour ->
                val hourString = String.format("%02d:00", hour)
                val hourEvents = events.filter { it.hora?.startsWith(String.format("%02d", hour)) == true }
                val hourTasks = tasks.filter { it.hora?.startsWith(String.format("%02d", hour)) == true }
                
                Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                    // Time Column
                    Text(
                        text = hourString,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(50.dp).padding(end = 8.dp, top = 8.dp),
                        textAlign = TextAlign.End
                    )
                    
                    // Grid Line and content
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        // Horizontal divider for the hour
                        Box(
                            modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                .align(Alignment.TopStart).offset(y = 14.dp)
                        )
                        
                        // Events / Tasks blocks
                        Row(modifier = Modifier.fillMaxSize().padding(top = 16.dp, bottom = 2.dp, end = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            hourEvents.forEach { event ->
                                val color = try { Color(android.graphics.Color.parseColor(event.cor)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color.copy(alpha = 0.2f))
                                        .border(1.dp, color, RoundedCornerShape(4.dp))
                                        .clickable { onEventClick(event) }
                                        .padding(4.dp)
                                ) {
                                    Text(event.titulo, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            hourTasks.forEach { task ->
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE6A23C).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFFE6A23C), RoundedCornerShape(4.dp))
                                        .clickable { onTaskClick(task) }
                                        .padding(4.dp)
                                ) {
                                    Text(task.titulo, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE6A23C), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
