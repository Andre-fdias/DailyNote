package com.andrefdias.dailynote.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andrefdias.dailynote.domain.model.EscalaConfig
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.components.buttons.FireButton
import com.andrefdias.dailynote.ui.designsystem.components.cards.FireCard
import com.andrefdias.dailynote.ui.designsystem.components.topbar.FireTopBar
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.icons.FireIcons
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCalendarScreen(
    viewModel: SettingsCalendarViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToGoogleSync: () -> Unit,
    onNavigateToWizard: (escalaId: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            FireTopBar(
                title = "⚙️ Configurações do Calendário",
                onBackClick = onNavigateBack,
                backgroundColor = FireColors.Surface
            )
        },
        containerColor = FireColors.Background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = FireSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(FireSpacing.Medium),
            contentPadding = PaddingValues(bottom = FireSpacing.Large, top = FireSpacing.Medium)
        ) {
            // Seção de Integração com Google Agenda
            item {
                FireCard {
                    Text(
                        text = "🔌 Integração Google Agenda",
                        style = FireTypography.Title,
                        fontWeight = FontWeight.Bold,
                        color = FireColors.Primary
                    )
                    Text(
                        text = "Prepare a sincronização automática dos seus plantões, compromissos e escalas com sua conta do Google.",
                        style = FireTypography.BodyMedium,
                        color = FireColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FireButton(
                        text = "CONFIGURAR SINCRONIZAÇÃO",
                        onClick = onNavigateToGoogleSync,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Seção de Escalas Operacionais
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Escalas Disponíveis",
                        style = FireTypography.Title,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { onNavigateToWizard(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = FireColors.Primary)
                    ) {
                        Text("+ Nova", color = Color.White)
                    }
                }
            }

            if (state.scales.isEmpty()) {
                item {
                    Text("Nenhuma escala cadastrada.", style = FireTypography.BodyMedium, color = FireColors.OnSurfaceVariant)
                }
            } else {
                items(state.scales) { escala ->
                    FireCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(escala.nome, style = FireTypography.BodyLarge, fontWeight = FontWeight.Bold)
                                Text("${escala.trabalhoHoras}h trabalho x ${escala.descansoHoras}h descanso", style = FireTypography.Caption)
                                Text(escala.descricao, style = FireTypography.BodyMedium, color = FireColors.OnSurfaceVariant)
                            }
                            Row {
                                IconButton(onClick = { onNavigateToWizard(escala.id) }) {
                                    Icon(
                                        imageVector = FireIcons.Edit,
                                        contentDescription = "Editar",
                                        tint = FireColors.Primary
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteEscala(escala.id) }) {
                                    Icon(
                                        imageVector = FireIcons.Delete,
                                        contentDescription = "Excluir",
                                        tint = FireColors.Error
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
