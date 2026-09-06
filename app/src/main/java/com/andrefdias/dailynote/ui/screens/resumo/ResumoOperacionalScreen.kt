package com.andrefdias.dailynote.ui.screens.resumo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.domain.model.EquipeViatura
import com.andrefdias.dailynote.domain.model.Militar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumoOperacionalScreen(
    viewModel: ResumoOperacionalViewModel = hiltViewModel(),
    onNavigateToEquipe: () -> Unit = {}
) {
    val equipes by viewModel.equipesHistorico.collectAsState()


    val isDark = com.andrefdias.dailynote.ui.designsystem.colors.FireColors.isDarkState
    val topBarColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF1E1E1E) else androidx.compose.ui.graphics.Color(0xFFFAFAFA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumo Operacional", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },

    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            val now = java.time.LocalDateTime.now()
            val todayStr = now.toLocalDate().toString()
            
            val activeShifts = equipes.filter { equipe ->
                val dataStr = equipe.data
                val horaInicio = equipe.equipeConfig?.horaInicio ?: "00:00"
                try {
                    val parts = horaInicio.split(":")
                    val hour = parts[0].toIntOrNull() ?: 0
                    val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    val startDateTime = java.time.LocalDate.parse(dataStr).atTime(hour, min)
                    val endDateTime = startDateTime.plusHours(24)
                    now.isAfter(startDateTime.minusMinutes(1)) && now.isBefore(endDateTime)
                } catch (e: Exception) {
                    dataStr == todayStr
                }
            }
            
            val equipesDoDia = activeShifts.ifEmpty { equipes.filter { it.data == todayStr } }
            
            if (equipesDoDia.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma equipe cadastrada para hoje.", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(equipesDoDia, key = { it.id }) { equipe ->
                        DashboardViaturaCard(equipe, onNavigateToEquipe)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardViaturaCard(
    equipe: com.andrefdias.dailynote.domain.model.EquipeServico,
    onNavigateToEquipe: () -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var bottomSheetTitle by remember { mutableStateOf("") }
    var bottomSheetPms by remember { mutableStateOf<List<com.andrefdias.dailynote.domain.model.MilitarEscalado>>(emptyList()) }

    val pmsMergulhador = mutableListOf<com.andrefdias.dailynote.domain.model.MilitarEscalado>()
    val pmsOvbLeve = mutableListOf<com.andrefdias.dailynote.domain.model.MilitarEscalado>()
    val pmsOvbPesado = mutableListOf<com.andrefdias.dailynote.domain.model.MilitarEscalado>()
    val pmsDejem = mutableListOf<com.andrefdias.dailynote.domain.model.MilitarEscalado>()
    
    var efetivoTotal = 0
    var telegrafistaStatus = "AGUARDANDO..."

    equipe.viaturas.forEach { ev ->
        val militares = ev.militaresEscalados
        efetivoTotal += militares.size

        militares.forEach { me ->
            val pm = me.militar
            if (pm != null) {
                if (pm.mergulhador) pmsMergulhador.add(me)
                if (pm.ovb == "Leve") pmsOvbLeve.add(me)
                if (pm.ovb == "Pesado") pmsOvbPesado.add(me)
            }
            if (me.tipoEscala == "DEJEM") {
                pmsDejem.add(me)
            }
        }

        val isTelegrafia = ev.viatura?.prefixo?.uppercase() == "TELEGRAFIA" || ev.viatura?.tipo?.uppercase() == "TELEGRAFIA" || ev.viatura?.status?.uppercase() == "TELEGRAFIA"
        if (isTelegrafia && ev.militaresEscalados.isNotEmpty()) {
            val encarregado = ev.militaresEscalados.find { it.funcao == "Telegrafista" }?.militar ?: ev.militaresEscalados.firstOrNull()?.militar
            if (encarregado != null) {
                telegrafistaStatus = "${encarregado.graduacao} ${encarregado.nomeGuerra}"
            }
        }
    }

    val totalViaturas = equipe.viaturas.size
    val viaturasOperacionais = equipe.viaturas.count { it.viatura?.status == "Operacional" || it.viatura?.status == "Em ocorrência" }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            val corFundoStr = equipe.equipeConfig?.corFundo
            val badgeColor = if (corFundoStr != null) {
                try { Color(android.graphics.Color.parseColor(corFundoStr)) } catch (e: Exception) { Color(0xFF1976D2) }
            } else {
                Color(0xFF1976D2)
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(badgeColor)
            )
            Column(modifier = Modifier.weight(1f)) {
            // 1. Header Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brasão
                Box(
                    modifier = Modifier.size(48.dp).background(Color(0xFFB71C1C), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Shield, contentDescription = "Brasão", tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${equipe.unidade.uppercase()} / ${equipe.posto.uppercase()}", 
                            color = MaterialTheme.colorScheme.onSurface, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = equipe.data,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Equipe Badge
                        val corFundoStr = equipe.equipeConfig?.corFundo
                        val defaultColor = Color(0xFF1976D2)
                        val badgeColor = if (corFundoStr != null) {
                            try { Color(android.graphics.Color.parseColor(corFundoStr)) } catch (e: Exception) { defaultColor }
                        } else {
                            defaultColor
                        }
                        Box(modifier = Modifier.background(badgeColor, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                            val equipeText = equipe.equipeConfig?.nome ?: equipe.tipoEscala.split(" - ").lastOrNull()?.trim() ?: equipe.tipoEscala
                            Text(text = equipeText, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        // VTRs Badge
                        Box(
                            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = androidx.compose.material.icons.Icons.Default.LocalShipping, contentDescription = "VTRs", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "VTRs: ", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = "$viaturasOperacionais / $totalViaturas", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Tabela de Viaturas
            Column(modifier = Modifier.padding(horizontal = 0.dp)) {
                // Tabela Header
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)).padding(horizontal = 8.dp, vertical = 16.dp), 
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("VTR", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.0f))
                    Text("SITUAÇÃO", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.9f))
                    Text("PM", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.5f))
                    Text("ENCARREGADO", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.6f))
                }

                val viaturasSemTelegrafia = equipe.viaturas.filter { ev ->
                    val prefixo = ev.viatura?.prefixo?.uppercase()
                    val tipo = ev.viatura?.tipo?.uppercase()
                    val status = ev.viatura?.status?.uppercase()
                    prefixo != "TELEGRAFIA" && tipo != "TELEGRAFIA" && status != "TELEGRAFIA"
                }
                
                viaturasSemTelegrafia.forEachIndexed { index, ev ->
                    val isLast = index == viaturasSemTelegrafia.lastIndex
                    val bottomShape = RoundedCornerShape(0.dp)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, bottomShape)
                            .clickable {
                                bottomSheetTitle = ev.viatura?.prefixo ?: "Viatura"
                                bottomSheetPms = ev.militaresEscalados
                                showBottomSheet = true
                            }
                            .padding(horizontal = 8.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ev.viatura?.prefixo ?: "-", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.weight(1.0f))
                        
                        // Status Chip
                        val status = ev.viatura?.status ?: "-"
                        val statusBg = if (status.equals("Operacional", ignoreCase = true)) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer
                        val statusColor = if (status.equals("Operacional", ignoreCase = true)) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        Box(modifier = Modifier.weight(0.9f), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.background(statusBg, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(status, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                        }
                        
                        val pmCount = ev.militaresEscalados.size
                        Text(pmCount.toString().padStart(2, '0'), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.weight(0.5f))
                        val encarregado = ev.militaresEscalados.find { it.funcao == "Comandante" }?.militar ?: ev.militaresEscalados.firstOrNull()?.militar
                        val encarregadoStr = encarregado?.let { "${it.graduacao} ${it.nomeGuerra}" } ?: "-"
                        Text(encarregadoStr, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1.6f))
                    }
                    if (!isLast) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Indicadores Operacionais (2x2 Grid)
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Mergulhador
                    Card(
                        modifier = Modifier.weight(1f).clickable {
                            bottomSheetTitle = "Mergulhadores"
                            bottomSheetPms = pmsMergulhador
                            showBottomSheet = true
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Water, contentDescription = "Mergulhador", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MERGULHADOR", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text(pmsMergulhador.size.toString().padStart(2, '0'), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.error, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    // Total DEJEM
                    Card(
                        modifier = Modifier.weight(1f).clickable {
                            bottomSheetTitle = "Total DEJEM"
                            bottomSheetPms = pmsDejem
                            showBottomSheet = true
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Group, contentDescription = "Total DEJEM", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TOTAL DEJEM", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text(pmsDejem.size.toString().padStart(2, '0'), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.secondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // OVB Leve
                    Card(
                        modifier = Modifier.weight(1f).clickable {
                            bottomSheetTitle = "OVB Leve"
                            bottomSheetPms = pmsOvbLeve
                            showBottomSheet = true
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.LocalShipping, contentDescription = "OVB Leve", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("OVB LEVE", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text(pmsOvbLeve.size.toString().padStart(2, '0'), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    // OVB Pesado
                    Card(
                        modifier = Modifier.weight(1f).clickable {
                            bottomSheetTitle = "OVB Pesado"
                            bottomSheetPms = pmsOvbPesado
                            showBottomSheet = true
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.LocalShipping, contentDescription = "OVB Pesado", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("OVB PESADO", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text(pmsOvbPesado.size.toString().padStart(2, '0'), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Telegrafista
            Box(
                modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Person, contentDescription = "Telegrafista", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("TELEGRAFISTA", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(telegrafistaStatus.uppercase(), color = if (telegrafistaStatus == "AGUARDANDO...") Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // 5. Efetivo Total
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Group, contentDescription = "Efetivo", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EFETIVO TOTAL", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(efetivoTotal.toString().padStart(2, '0'), color = MaterialTheme.colorScheme.onTertiaryContainer, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = onNavigateToEquipe,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha=0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Editar", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        }
    }
    
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) }
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).fillMaxWidth()) {
                Text(bottomSheetTitle.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))
                if (bottomSheetPms.isEmpty()) {
                    Text("Nenhum militar encontrado.", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                } else {
                    val sortedPms = bottomSheetPms.sortedByDescending { me -> 
                        var isAtivo = true
                        if (me.tipoEscala == "DEJEM" && me.dejemHorarioFim != null) {
                            try {
                                val dateParse = java.time.LocalDate.parse(equipe.data)
                                val today = java.time.LocalDate.now()
                                if (dateParse.isBefore(today)) isAtivo = false
                                else if (dateParse.isEqual(today)) {
                                    val timeEnd = java.time.LocalTime.parse(me.dejemHorarioFim)
                                    if (java.time.LocalTime.now().isAfter(timeEnd)) isAtivo = false
                                }
                            } catch (e: Exception) {
                                isAtivo = true
                            }
                        }
                        isAtivo
                    }

                    sortedPms.forEach { me ->
                        val pm = me.militar
                        if (pm != null) {
                            var isAtivo = true
                            if (me.tipoEscala == "DEJEM" && me.dejemHorarioFim != null) {
                                try {
                                    val dateParse = java.time.LocalDate.parse(equipe.data)
                                    val today = java.time.LocalDate.now()
                                    if (dateParse.isBefore(today)) isAtivo = false
                                    else if (dateParse.isEqual(today)) {
                                        val timeEnd = java.time.LocalTime.parse(me.dejemHorarioFim)
                                        if (java.time.LocalTime.now().isAfter(timeEnd)) isAtivo = false
                                    }
                                } catch (e: Exception) {}
                            }
                            
                            val alphaValue = if (isAtivo) 1f else 0.4f

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).let { if(!isAtivo) it.background(Color.Transparent) else it }.graphicsLayer { alpha = alphaValue },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                    val isDejemBar = me.tipoEscala == "DEJEM"
                                    val barColor = if (isDejemBar) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(8.dp)
                                            .background(barColor)
                                    )
                                Row(
                                    modifier = Modifier.padding(16.dp).weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${pm.graduacao} ${pm.nomeGuerra}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                                            Text("RE: ${pm.re}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        
                                        val tags = mutableListOf<String>()
                                        if (pm.mergulhador) tags.add("Mergulhador")
                                        if (!pm.ovb.contains("Habilitado", ignoreCase = true) && pm.ovb.isNotBlank()) tags.add("OVB ${pm.ovb}")
                                        
                                        val isDejem = me.tipoEscala == "DEJEM"
                                        if (isDejem) {
                                            val horario = if (me.dejemHorarioInicio != null) "(${me.dejemHorarioInicio} às ${me.dejemHorarioFim ?: "..."})" else ""
                                            tags.add("DEJEM $horario")
                                        } else {
                                            tags.add(me.tipoEscala)
                                        }
                                        
                                        if (tags.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier.padding(top = 8.dp).horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                tags.forEach { tag ->
                                                    val isTagDejem = tag.startsWith("DEJEM")
                                                    val bgColor = if (isTagDejem) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                                    val textColor = if (isTagDejem) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                                    Box(
                                                        modifier = Modifier.background(bgColor, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(tag, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun IndicatorRow(label: String, value: String, valueColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ResumoViaturaCard(
    equipe: com.andrefdias.dailynote.domain.model.EquipeServico,
    viatura: EquipeViatura
) {
    var expanded by remember { mutableStateOf(false) }

    val dateFormatted = try {
        LocalDate.parse(equipe.data).format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")))
    } catch (e: Exception) {
        equipe.data
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Data: $dateFormatted", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Text(text = "${viatura.viatura?.prefixo ?: "Prefixo Desconhecido"} - ${viatura.viatura?.tipo ?: ""}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expandir",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Equipe: ${equipe.tipoEscala}", fontSize = 13.sp)
                    Text("Posto: ${equipe.unidade} / ${equipe.posto}", fontSize = 13.sp)
                }

                AnimatedVisibility(visible = expanded) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                        
                        Text("Efetivo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 4.dp))
                        
                        viatura.militaresEscalados.forEach { me ->
                            val pm = me.militar
                            if (pm != null) {
                                Text("${me.funcao}: ${pm.graduacao} ${pm.nomeGuerra}", fontSize = 14.sp)
                            } else {
                                Text("${me.funcao}: Não definido", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}
