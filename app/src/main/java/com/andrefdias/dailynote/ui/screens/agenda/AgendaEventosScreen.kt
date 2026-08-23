package com.andrefdias.dailynote.ui.screens.agenda

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.domain.model.CalendarEvento
import com.andrefdias.dailynote.ui.screens.home.parseHexColor
import com.andrefdias.dailynote.ui.screens.home.EventTaskDetailBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaEventosScreen(
    viewModel: AgendaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    var showAddSheet by remember { mutableStateOf(false) }
    var eventoSelecionado by remember { mutableStateOf<CalendarEvento?>(null) }
    
    // Sort events by date and time
    val eventosSorted = state.todosEventos.sortedWith(compareBy({ it.data }, { it.hora ?: "23:59" }))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(targetValue = if (isPressed) 0.8f else 1f, label = "scale")

            FloatingActionButton(
                onClick = { 
                    eventoSelecionado = null
                    showAddSheet = true 
                },
                interactionSource = interactionSource,
                modifier = Modifier.scale(scale),
                containerColor = Color(0xFFE91E63), // Pink for events
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Evento")
            }
        },
        containerColor = Color(0xFF1E2633)
    ) { padding ->
        if (showAddSheet) {
            EventTaskDetailBottomSheet(
                evento = eventoSelecionado,
                tarefa = null,
                onDismiss = { 
                    showAddSheet = false 
                    eventoSelecionado = null
                },
                onDelete = {
                    eventoSelecionado?.let { viewModel.deleteEvento(it) }
                    showAddSheet = false
                    eventoSelecionado = null
                },
                onSaveEvento = { id, titulo, data, hora, local, descricao, cor ->
                    viewModel.saveEvento(id = id, titulo = titulo, data = data, hora = hora.ifBlank { null }, local = local, descricao = descricao, cor = cor)
                },
                onSaveTarefa = { _, _, _, _, _, _, _ -> }
            )
        }
        
        if (eventosSorted.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhum evento encontrado.", color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(eventosSorted, key = { it.id }) { evento ->
                    EventoCard(
                        evento = evento,
                        onClick = { 
                            eventoSelecionado = evento
                            showAddSheet = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EventoCard(
    evento: CalendarEvento,
    onClick: () -> Unit
) {
    val corEvento = parseHexColor(evento.cor, Color(0xFF9C27B0))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ) {
            // Left color bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(corEvento)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = evento.titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (evento.descricao.isNotBlank()) {
                    Text(
                        text = evento.descricao,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (evento.data.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.Event, contentDescription = null, tint = corEvento, modifier = Modifier.size(16.dp))
                            Text(text = evento.data, fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                    if (!evento.hora.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = corEvento, modifier = Modifier.size(16.dp))
                            Text(text = evento.hora, fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                    if (!evento.local.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = corEvento, modifier = Modifier.size(16.dp))
                            Text(text = evento.local, fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }
            }
        }
    }
}
