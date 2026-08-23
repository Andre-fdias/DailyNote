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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.domain.model.CalendarTarefa
import com.andrefdias.dailynote.domain.model.StatusTarefa
import com.andrefdias.dailynote.ui.screens.home.EventTaskDetailBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaTarefasScreen(
    viewModel: AgendaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    var showAddSheet by remember { mutableStateOf(false) }
    var tarefaSelecionada by remember { mutableStateOf<CalendarTarefa?>(null) }
    
    // Sort so that PENDENTE comes first, then CONCLUIDA
    val tarefasSorted = state.todasTarefas.sortedBy { it.status == StatusTarefa.CONCLUIDA }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tarefas") },
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
                    tarefaSelecionada = null
                    showAddSheet = true 
                },
                interactionSource = interactionSource,
                modifier = Modifier.scale(scale),
                containerColor = Color(0xFFFF9800),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Tarefa")
            }
        },
        containerColor = Color(0xFF1E2633)
    ) { padding ->
        if (showAddSheet) {
            EventTaskDetailBottomSheet(
                evento = null,
                tarefa = tarefaSelecionada,
                onDismiss = { 
                    showAddSheet = false 
                    tarefaSelecionada = null
                },
                onDelete = {
                    tarefaSelecionada?.let { viewModel.deleteTarefa(it) }
                    showAddSheet = false
                    tarefaSelecionada = null
                },
                onSaveEvento = { _, _, _, _, _, _, _ -> },
                onSaveTarefa = { id, titulo, data, hora, descricao, cor, subtarefas ->
                    viewModel.saveTarefa(id = id, titulo = titulo, data = data, hora = hora.ifBlank { null }, descricao = descricao, cor = cor, checklist = subtarefas)
                }
            )
        }
        
        if (tarefasSorted.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhuma tarefa encontrada.", color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tarefasSorted, key = { it.id }) { tarefa ->
                    TarefaCard(
                        tarefa = tarefa,
                        onToggle = { viewModel.toggleTarefaStatus(tarefa) },
                        onClick = {
                            tarefaSelecionada = tarefa
                            showAddSheet = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TarefaCard(
    tarefa: CalendarTarefa,
    onToggle: () -> Unit,
    onClick: () -> Unit = {}
) {
    val isConcluida = tarefa.status == StatusTarefa.CONCLUIDA
    val corTarefa = com.andrefdias.dailynote.ui.screens.home.parseHexColor(tarefa.cor ?: "#2196F3", Color(0xFF2196F3))

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
                    .background(if (isConcluida) Color.Gray else corTarefa)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = if (isConcluida) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = "Status",
                    tint = if (isConcluida) Color.Gray else corTarefa,
                    modifier = Modifier.size(28.dp).clickable { onToggle() }
                )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tarefa.titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isConcluida) Color.Gray else Color.White,
                    textDecoration = if (isConcluida) TextDecoration.LineThrough else null
                )
                if (tarefa.descricao.isNotBlank()) {
                    Text(
                        text = tarefa.descricao,
                        fontSize = 14.sp,
                        color = if (isConcluida) Color.Gray.copy(alpha=0.7f) else Color.White.copy(alpha = 0.7f),
                        textDecoration = if (isConcluida) TextDecoration.LineThrough else null,
                        maxLines = 1
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    if (tarefa.data.isNotBlank()) {
                        Text(
                            text = tarefa.data,
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier
                                .background(Color(0xFF4CAF50).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (!tarefa.hora.isNullOrBlank()) {
                        Text(
                            text = tarefa.hora,
                            fontSize = 12.sp,
                            color = Color(0xFF2196F3),
                            modifier = Modifier
                                .background(Color(0xFF2196F3).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                }
            }
        }
    }
}
