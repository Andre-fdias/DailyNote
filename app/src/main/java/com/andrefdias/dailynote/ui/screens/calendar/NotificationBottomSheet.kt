package com.andrefdias.dailynote.ui.screens.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrefdias.dailynote.domain.model.CalendarNotificacao
import com.andrefdias.dailynote.domain.model.CategoriaNotificacao
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationBottomSheet(
    viewModel: NotificationCenterViewModel,
    onDismiss: () -> Unit,
    onNotificationClick: (CalendarNotificacao) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf<CategoriaNotificacao?>(null) }

    // Cores 100% theme-aware (dark/light mode)
    val sheetBackground = FireColors.Surface
    val onSurface = FireColors.OnSurface
    val onSurfaceVariant = FireColors.OnSurfaceVariant
    val unreadBackground = FireColors.PrimaryLight
    val readBackground = FireColors.SurfaceVariant

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetBackground,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FireSpacing.Medium)
                .padding(bottom = FireSpacing.Large)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = FireSpacing.Small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🔔 Central de Notificações",
                        style = FireTypography.Title,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )
                    Text(
                        text = "${state.unreadCount} não lidas",
                        style = FireTypography.Caption,
                        color = onSurfaceVariant
                    )
                }
                TextButton(onClick = { viewModel.clearAllNotifications() }) {
                    Text("Limpar Tudo", color = FireColors.Primary, fontWeight = FontWeight.Bold)
                }
            }

            // Filtros de categoria em linha horizontal com scroll
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = FireSpacing.ExtraSmall),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("Todas", color = if (selectedFilter == null) Color.White else onSurface) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FireColors.Primary,
                            selectedLabelColor = Color.White,
                            containerColor = FireColors.SurfaceVariant,
                            labelColor = onSurface
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CategoriaNotificacao.ESCALAS,
                        onClick = { selectedFilter = CategoriaNotificacao.ESCALAS },
                        label = { Text("Escalas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FireColors.Primary,
                            selectedLabelColor = Color.White,
                            containerColor = FireColors.SurfaceVariant,
                            labelColor = onSurface
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CategoriaNotificacao.TAREFAS,
                        onClick = { selectedFilter = CategoriaNotificacao.TAREFAS },
                        label = { Text("Tarefas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FireColors.Primary,
                            selectedLabelColor = Color.White,
                            containerColor = FireColors.SurfaceVariant,
                            labelColor = onSurface
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CategoriaNotificacao.EVENTOS,
                        onClick = { selectedFilter = CategoriaNotificacao.EVENTOS },
                        label = { Text("Eventos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FireColors.Primary,
                            selectedLabelColor = Color.White,
                            containerColor = FireColors.SurfaceVariant,
                            labelColor = onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de notificações
            val filteredNotifications = if (selectedFilter == null) {
                state.notifications
            } else {
                state.notifications.filter { it.categoria == selectedFilter }
            }

            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma notificação por aqui.",
                        style = FireTypography.BodyMedium,
                        color = onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(FireSpacing.Small),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 420.dp)
                ) {
                    items(filteredNotifications) { notif ->
                        val badgeColor = when (notif.categoria) {
                            CategoriaNotificacao.ESCALAS   -> Color(0xFF1976D2)
                            CategoriaNotificacao.EVENTOS   -> Color(0xFF388E3C)
                            CategoriaNotificacao.TAREFAS   -> Color(0xFFF57C00)
                            CategoriaNotificacao.SISTEMA   -> Color(0xFFD32F2F)
                            else                           -> Color(0xFF7B1FA2)
                        }

                        var expanded by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onNotificationClick(notif) },
                                    onLongClick = { expanded = true }
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (notif.lida) readBackground else unreadBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(FireSpacing.Small),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Dot de categoria
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(badgeColor)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notif.titulo,
                                            style = FireTypography.BodyMedium,
                                            fontWeight = if (notif.lida) FontWeight.Normal else FontWeight.Bold,
                                            color = onSurface
                                        )
                                        Text(
                                            text = notif.descricao,
                                            style = FireTypography.Caption,
                                            color = onSurfaceVariant
                                        )
                                        Text(
                                            text = "${notif.data} às ${notif.hora} | ${notif.origem}",
                                            fontSize = 10.sp,
                                            color = onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    // Indicador de não lida
                                    if (!notif.lida) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(FireColors.Primary)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Ver Detalhes") },
                                        onClick = { expanded = false; onNotificationClick(notif) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Marcar como lida") },
                                        onClick = { expanded = false; viewModel.toggleNotificationLida(notif) }
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
