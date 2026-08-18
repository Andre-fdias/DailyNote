package com.andrefdias.dailynote.ui.screens.calendar

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrefdias.dailynote.domain.model.EquipeConfig
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.components.buttons.FireButton
import com.andrefdias.dailynote.ui.designsystem.components.cards.FireCard
import com.andrefdias.dailynote.ui.designsystem.components.inputs.FireDatePicker
import com.andrefdias.dailynote.ui.designsystem.components.inputs.FireTextField
import com.andrefdias.dailynote.ui.designsystem.components.topbar.FireTopBar
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarWizardScreen(
    viewModel: CalendarWizardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    // Monitora sucesso ao salvar para fechar a tela
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            FireTopBar(
                title = "✨ Configuração de Calendário",
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
            contentPadding = PaddingValues(bottom = FireSpacing.Large)
        ) {
            // Card de Introdução
            item {
                FireCard {
                    Text(
                        text = "Assistente Operacional",
                        style = FireTypography.Title,
                        fontWeight = FontWeight.Bold,
                        color = FireColors.Primary
                    )
                    Text(
                        text = "Defina sua escala operacional e suas equipes de serviço para iniciar o calendário operacional parametrizado.",
                        style = FireTypography.BodyMedium,
                        color = FireColors.OnSurfaceVariant
                    )
                }
            }

            // Etapa 1: Seleção de Escala
            item {
                Text(
                    text = "Etapa 1: Selecione a Escala Operacional",
                    style = FireTypography.Title,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                FireCard {
                    val options = listOf(
                        Pair("24x48", "24x48 (1 Turno, 3 Equipes)"),
                        Pair("24x72", "24x72 (1 Turno, 4 Equipes)"),
                        Pair("12x36", "12x36 (2 Turnos, 4 Equipes)"),
                        Pair("5x2", "5x2 (Escala Administrativa, 7 dias)"),
                        Pair("6x1", "6x1 (Escala Operacional 6 dias, 7 dias)"),
                        Pair("12x24x12x48", "12x24x12x48 (2 Turnos, 4 Equipes)"),
                        Pair("CUSTOM", "Escala Personalizada...")
                    )

                    options.forEach { (optionKey, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onOptionSelected(optionKey) }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = state.selectedOption == optionKey,
                                onClick = { viewModel.onOptionSelected(optionKey) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = FireTypography.BodyLarge)
                        }
                    }
                }
            }

            // Se for custom, exibir os campos de customização
            if (state.selectedOption == "CUSTOM") {
                item {
                    FireCard {
                        Text(
                            text = "Parametrizar Escala Personalizada",
                            style = FireTypography.BodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = FireColors.Secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        FireTextField(
                            value = state.customNome,
                            onValueChange = { viewModel.updateCustomFields(nome = it) },
                            label = "Nome da Escala (ex: 12x24x12x36)"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FireTextField(
                                value = state.customTrabalhoHoras.toString(),
                                onValueChange = { viewModel.updateCustomFields(trabalho = it.toIntOrNull() ?: 0) },
                                label = "Horas Trabalho",
                                modifier = Modifier.weight(1f)
                            )
                            FireTextField(
                                value = state.customDescansoHoras.toString(),
                                onValueChange = { viewModel.updateCustomFields(descanso = it.toIntOrNull() ?: 0) },
                                label = "Horas Descanso",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FireTextField(
                                value = state.customQuantidadeEquipes.toString(),
                                onValueChange = { viewModel.updateCustomFields(equipesCount = it.toIntOrNull() ?: 1) },
                                label = "Qtd Equipes",
                                modifier = Modifier.weight(1f)
                            )
                            FireTextField(
                                value = state.customQuantidadeTurnos.toString(),
                                onValueChange = { viewModel.updateCustomFields(turnosCount = it.toIntOrNull() ?: 1) },
                                label = "Qtd Turnos (1 ou 2)",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        FireTextField(
                            value = state.customDescricao,
                            onValueChange = { viewModel.updateCustomFields(descricao = it) },
                            label = "Descrição Adicional"
                        )
                    }
                }
            }

            // Etapa 2: Personalização das Equipes
            item {
                Text(
                    text = "Etapa 2: Cadastro das Equipes",
                    style = FireTypography.Title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = FireSpacing.Small)
                )
            }

            itemsIndexed(state.equipes) { index, equipe ->
                var showColorDialog by remember { mutableStateOf(false) }

                FireCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Configuração de ${equipe.nome}",
                            style = FireTypography.BodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = FireColors.Primary
                        )

                        // Preview da Cor da Equipe
                        val backColor = runCatching { Color(parseColor(equipe.corFundo)) }.getOrDefault(FireColors.Primary)
                        val textColor = runCatching { Color(parseColor(equipe.corTexto)) }.getOrDefault(Color.White)
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(backColor)
                                .border(1.dp, FireColors.OnSurfaceVariant.copy(alpha = 0.2f), CircleShape)
                                .clickable { showColorDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = equipe.sigla.ifBlank { "?" },
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FireTextField(
                        value = equipe.nome,
                        onValueChange = { viewModel.updateEquipe(index, equipe.copy(nome = it)) },
                        label = "Nome da Equipe"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FireTextField(
                            value = equipe.sigla,
                            onValueChange = { viewModel.updateEquipe(index, equipe.copy(sigla = it)) },
                            label = "Sigla",
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Dropdown de Turno (Diurno / Noturno) se a escala exigir 2 turnos
                        val isTwoShifts = state.selectedOption == "12x36" || 
                                          state.selectedOption == "12x24x12x48" || 
                                          (state.selectedOption == "CUSTOM" && state.customQuantidadeTurnos == 2)
                        
                        if (isTwoShifts) {
                            var showTurnoDropdown by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1.5f).padding(top = 8.dp)) {
                                OutlinedButton(
                                    onClick = { showTurnoDropdown = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (equipe.ordemTurno == 0) "☀️ Diurno" else "🌙 Noturno")
                                }
                                DropdownMenu(
                                    expanded = showTurnoDropdown,
                                    onDismissRequest = { showTurnoDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("☀️ Diurno") },
                                        onClick = {
                                            viewModel.updateEquipe(index, equipe.copy(ordemTurno = 0))
                                            showTurnoDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🌙 Noturno") },
                                        onClick = {
                                            viewModel.updateEquipe(index, equipe.copy(ordemTurno = 1))
                                            showTurnoDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horários de Início e Término do Turno
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FireTextField(
                            value = equipe.horaInicio,
                            onValueChange = { newTime ->
                                viewModel.updateEquipe(index, equipe.copy(horaInicio = newTime))
                            },
                            label = "Hora Início (ex: 06:00)",
                            modifier = Modifier.weight(1f)
                        )
                        FireTextField(
                            value = equipe.horaTermino,
                            onValueChange = { newTime ->
                                viewModel.updateEquipe(index, equipe.copy(horaTermino = newTime))
                            },
                            label = "Hora Término (ex: 18:00)",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    var dataStr by remember { mutableStateOf(LocalDate.parse(equipe.dataInicial).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) }
                    FireDatePicker(
                        value = dataStr,
                        onDateSelected = {
                            dataStr = it
                            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            val date = runCatching { LocalDate.parse(it, formatter) }.getOrNull()
                            if (date != null) {
                                viewModel.updateEquipe(index, equipe.copy(dataInicial = date.toString()))
                            }
                        },
                        label = "Data Inicial do Ciclo"
                    )
                }

                // Diálogo de Seleção de Cor
                if (showColorDialog) {
                    ColorPickerDialog(
                        initialBg = equipe.corFundo,
                        onColorSelected = { bg, fg ->
                            viewModel.updateEquipe(index, equipe.copy(corFundo = bg, corTexto = fg))
                            showColorDialog = false
                        },
                        onDismiss = { showColorDialog = false }
                    )
                }
            }

            // Pré-visualização Dinâmica dos Próximos 30 dias
            item {
                Text(
                    text = "Pré-visualização Dinâmica",
                    style = FireTypography.Title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = FireSpacing.Small)
                )
                Text(
                    text = "Mini calendário dos próximos 28 dias atualizado instantaneamente.",
                    style = FireTypography.Caption
                )
                Spacer(modifier = Modifier.height(4.dp))
                FireCard {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val today = LocalDate.now()
                        val days = (0..27).map { today.plusDays(it.toLong()) }
                        items(days) { date ->
                            val activeMap = state.previewDays[date] ?: emptyMap()
                            val firstTeam = activeMap[0]?.firstOrNull() ?: activeMap[1]?.firstOrNull()
                            
                            val bgColor = firstTeam?.let {
                                runCatching { Color(parseColor(it.corFundo)) }.getOrDefault(Color.Transparent)
                            } ?: Color.Transparent
                            val txtColor = firstTeam?.let {
                                runCatching { Color(parseColor(it.corTexto)) }.getOrDefault(FireColors.OnBackground)
                            } ?: FireColors.OnBackground

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (bgColor == Color.Transparent) FireColors.SurfaceVariant.copy(alpha = 0.4f) else bgColor)
                                    .border(
                                        width = 1.dp,
                                        color = if (date == today) FireColors.Primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = FireTypography.LabelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = txtColor
                                    )
                                    if (firstTeam != null) {
                                        Text(
                                            text = firstTeam.sigla,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = txtColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Exibição de Erros
            if (state.error != null) {
                item {
                    Text(
                        text = "Erro: ${state.error}",
                        color = FireColors.Error,
                        style = FireTypography.LabelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Botão Salvar
            item {
                Spacer(modifier = Modifier.height(8.dp))
                FireButton(
                    text = if (state.isSaving) "SALVANDO..." else "SALVAR E CONCLUIR",
                    onClick = { viewModel.saveConfig() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving
                )
            }
        }
    }
}

// Color Picker Dialog Custom com contraste theme-aware para Dark Mode
@Composable
fun ColorPickerDialog(
    initialBg: String,
    onColorSelected: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val predefinedColors = listOf(
        "#D32F2F", "#C2185B", "#7B1FA2", "#512DA8", "#1976D2",
        "#0288D1", "#0097A7", "#00796B", "#388E3C", "#689F38",
        "#AFB42B", "#FBC02D", "#FFA000", "#F57C00", "#E64A19",
        "#5D4037", "#616161", "#455A64", "#00E676", "#FF5252"
    )

    var customHex by remember { mutableStateOf(initialBg) }

    fun calculateTextColor(hexColor: String): String {
        val cleanHex = hexColor.replace("#", "")
        if (cleanHex.length != 6) return "#FFFFFF"
        val r = runCatching { cleanHex.substring(0, 2).toInt(16) }.getOrDefault(0)
        val g = runCatching { cleanHex.substring(2, 4).toInt(16) }.getOrDefault(0)
        val b = runCatching { cleanHex.substring(4, 6).toInt(16) }.getOrDefault(0)
        val luma = 0.299 * r + 0.587 * g + 0.114 * b
        return if (luma > 186) "#000000" else "#FFFFFF"
    }

    val dialogSurface = MaterialTheme.colorScheme.surface
    val dialogOnSurface = MaterialTheme.colorScheme.onSurface
    val dialogOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "🎨 Selecione a Cor da Equipe",
                style = FireTypography.Title,
                color = dialogOnSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Cores sugeridas (toque para escolher):",
                    style = FireTypography.LabelSmall,
                    color = dialogOnSurfaceVariant
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(predefinedColors) { hex ->
                        val color = runCatching { Color(parseColor(hex)) }.getOrDefault(FireColors.Primary)
                        val isSelected = customHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) FireColors.Primary else dialogOnSurfaceVariant.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    customHex = hex
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                val checkColor = runCatching { Color(parseColor(calculateTextColor(hex))) }.getOrDefault(Color.White)
                                Text("✓", color = checkColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Cor Personalizada Hex
                FireTextField(
                    value = customHex,
                    onValueChange = { customHex = it },
                    label = "Cor Hexadecimal (ex: #FF5722)"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val normalizedHex = if (customHex.startsWith("#")) customHex else "#$customHex"
                    if (normalizedHex.length == 7) {
                        val fg = calculateTextColor(normalizedHex)
                        onColorSelected(normalizedHex, fg)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FireColors.Primary)
            ) {
                Text("Confirmar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = FireColors.Primary)
            }
        },
        containerColor = dialogSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
