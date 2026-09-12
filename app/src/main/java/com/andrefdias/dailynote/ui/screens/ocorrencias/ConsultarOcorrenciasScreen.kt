package com.andrefdias.dailynote.ui.screens.ocorrencias

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia

// Resolve equipe string → display label + color
// Removed local parseEquipeInfo


@Composable
fun ConsultarOcorrenciasScreen(
    viewModel: ConsultarOcorrenciasViewModel = hiltViewModel(),
    onNavigateToOpcoes: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        // Header
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shadowElevation = 4.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Ocorrências Registradas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "${state.ocorrencias.size} registro(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${state.ocorrencias.size}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Carregando ocorrências...")
                    }
                }
            }
            state.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.errorMessage ?: "Erro", color = MaterialTheme.colorScheme.error)
                }
            }
            state.ocorrencias.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Filled.SearchOff, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Nenhuma ocorrência cadastrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Use \"Nova Ocorrência\" para registrar.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.ocorrencias, key = { it.id }) { ocorrencia ->
                        OcorrenciaCard(
                            ocorrencia = ocorrencia,
                            viaturaMap = state.viaturaMap,
                            veiculosMap = state.veiculosMap,
                            vitimasMap = state.vitimasMap,
                            onEditar = { onNavigateToOpcoes(ocorrencia.id) },
                            onExcluir = { viewModel.excluirOcorrencia(ocorrencia) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OcorrenciaCard(
    ocorrencia: RoomNovaOcorrencia,
    viaturaMap: Map<String, String>,
    veiculosMap: Map<String, List<com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo>>,
    vitimasMap: Map<String, List<com.andrefdias.dailynote.data.local.entities.RoomNovaVitima>>,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    // Resolve equipe info
    val (equipeLabel, prontidaoColor) = com.andrefdias.dailynote.util.EquipeUtils.parseEquipeInfo(ocorrencia.equipe)

    val viaturaRaw = ocorrencia.viatura.trim()
    val allViaturasPrefixes: List<String> = if (viaturaRaw.startsWith("[")) {
        try {
            val typeMap = object : com.google.gson.reflect.TypeToken<List<Map<String, String>>>() {}.type
            val parsed: List<Map<String, String>> = com.google.gson.Gson().fromJson(viaturaRaw, typeMap)
            parsed.mapNotNull { it["prefixo"] }.filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    } else {
        if (viaturaRaw.isNotEmpty()) listOf(viaturaMap[viaturaRaw]?.takeIf { it.isNotEmpty() } ?: viaturaRaw) else emptyList()
    }
    val viaturaHeaderLabel = allViaturasPrefixes.firstOrNull() ?: "Sem VTR"
    val viaturaAllLabel = if (allViaturasPrefixes.isNotEmpty()) allViaturasPrefixes.joinToString(", ") else "Sem VTR"

    // Natureza is stored as-is
    val naturezaLabel = ocorrencia.natureza.ifEmpty { "Não informada" }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Excluir Ocorrência") },
            text = { Text("Excluir o Talão ${ocorrencia.talao}? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(onClick = { onExcluir(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Excluir")
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Left accent bar (prontidão color)
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(106.dp)
                        .background(prontidaoColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 2.dp))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 8.dp)
                ) {
                    // Talão + prontidão badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Talão ${ocorrencia.talao.ifEmpty { "s/n" }}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }
                        // Prontidão badge - always shown
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(prontidaoColor.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(equipeLabel, color = prontidaoColor, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${ocorrencia.data}  •  ${ocorrencia.hora}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Natureza + VTR chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(naturezaLabel, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            icon = { Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("VTR: $viaturaHeaderLabel", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            icon = { Icon(Icons.Filled.DirectionsCar, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }

                // Actions (Expand + Options)
                Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                    val context = LocalContext.current
                    var showDropdown by remember { mutableStateOf(false) }

                    IconButton(onClick = { showDropdown = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opções", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Compartilhar (WhatsApp)") },
                            onClick = {
                                showDropdown = false
                                com.andrefdias.dailynote.util.OcorrenciaExportHelper.shareTextWhatsApp(
                                    context, ocorrencia, vitimasMap[ocorrencia.id] ?: emptyList(), veiculosMap[ocorrencia.id] ?: emptyList()
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Compartilhar PDF") },
                            onClick = {
                                showDropdown = false
                                com.andrefdias.dailynote.util.OcorrenciaExportHelper.shareReportPdf(
                                    context, ocorrencia, veiculosMap[ocorrencia.id] ?: emptyList(), vitimasMap[ocorrencia.id] ?: emptyList()
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Compartilhar PDF + Imagens") },
                            onClick = {
                                showDropdown = false
                                com.andrefdias.dailynote.util.OcorrenciaExportHelper.shareReportAndImages(
                                    context, ocorrencia, veiculosMap[ocorrencia.id] ?: emptyList(), vitimasMap[ocorrencia.id] ?: emptyList()
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Exportar / Sincronizar (JSON)") },
                            onClick = {
                                showDropdown = false
                                com.andrefdias.dailynote.util.OcorrenciaExportHelper.exportToJson(
                                    context, ocorrencia, veiculosMap[ocorrencia.id] ?: emptyList(), vitimasMap[ocorrencia.id] ?: emptyList()
                                )
                            }
                        )
                    }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Expanded details
            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // === Contadores ===
                        val pessoasCount = try {
                            val type = object : com.google.gson.reflect.TypeToken<List<Any>>() {}.type
                            val list: List<Any>? = com.google.gson.Gson().fromJson(ocorrencia.pessoasJson, type)
                            list?.size ?: 0
                        } catch (e: Exception) { 0 }
                        
                        val militaresCount = try {
                            val gJson = ocorrencia.guarnicaoJson
                            if (gJson.startsWith("[")) {
                                val type = object : com.google.gson.reflect.TypeToken<List<Any>>() {}.type
                                val list: List<Any>? = com.google.gson.Gson().fromJson(gJson, type)
                                list?.size ?: 0
                            } else 0
                        } catch (e: Exception) { 0 }
                        
                        val viaturasCount = allViaturasPrefixes.size
                        
                        val apoiosCount = try {
                            val type = object : com.google.gson.reflect.TypeToken<List<Any>>() {}.type
                            val list: List<Any>? = com.google.gson.Gson().fromJson(ocorrencia.apoios, type)
                            list?.size ?: 0
                        } catch (e: Exception) { 0 }
                        
                        val veiculosDaOcorrencia = veiculosMap[ocorrencia.id] ?: emptyList()
                        val veiculosCount = veiculosDaOcorrencia.size
                        
                        val fotos = mutableListOf<String>()
                        try {
                            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                            val ocorrenciaFotos = com.google.gson.Gson().fromJson<List<String>>(ocorrencia.fotosUrisJson, type) ?: emptyList()
                            fotos.addAll(ocorrenciaFotos)
                            
                            veiculosDaOcorrencia.forEach { veiculo ->
                                val veiculoFotos = com.google.gson.Gson().fromJson<List<String>>(veiculo.fotosVeiculoUrisJson, type) ?: emptyList()
                                fotos.addAll(veiculoFotos)
                            }
                        } catch (e: Exception) { /* ignore */ }
                        val anexosCount = fotos.size

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CounterBadge(Icons.Filled.Person, "Pessoas", pessoasCount, modifier = Modifier.weight(1f))
                                CounterBadge(Icons.Filled.LocalFireDepartment, "Viaturas", viaturasCount, modifier = Modifier.weight(1f))
                                CounterBadge(Icons.Filled.Shield, "Militares", militaresCount, modifier = Modifier.weight(1f))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CounterBadge(Icons.Filled.Handshake, "Apoios", apoiosCount, modifier = Modifier.weight(1f))
                                CounterBadge(Icons.Filled.DirectionsCar, "Veículos", veiculosCount, modifier = Modifier.weight(1f))
                                CounterBadge(Icons.Filled.AttachFile, "Anexos", anexosCount, modifier = Modifier.weight(1f))
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // === Grid de Detalhes ===
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Linha de Viaturas
                            InfoRow(Icons.Filled.DirectionsCar, "Viaturas Envolvidas", viaturaAllLabel)
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            
                            // Linha de Endereço
                            val endereco = buildString {
                                if (!ocorrencia.rua.isNullOrEmpty()) append(ocorrencia.rua)
                                if (!ocorrencia.numero.isNullOrEmpty()) append(", ${ocorrencia.numero}")
                                if (!ocorrencia.bairro.isNullOrEmpty()) append(" - ${ocorrencia.bairro}")
                                if (isEmpty()) append("Não informado")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    InfoRow(Icons.Filled.LocationOn, "Endereço", endereco)
                                }
                                val context = LocalContext.current
                                IconButton(onClick = {
                                    val query = listOfNotNull(
                                        ocorrencia.rua?.takeIf { it.isNotBlank() },
                                        ocorrencia.numero?.takeIf { it.isNotBlank() },
                                        ocorrencia.bairro?.takeIf { it.isNotBlank() },
                                        ocorrencia.cidade?.takeIf { it.isNotBlank() }
                                    ).joinToString(", ")
                                    if (query.isNotBlank()) {
                                        val uri = android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(query)}")
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) { e.printStackTrace() }
                                    }
                                }) {
                                    Icon(Icons.Filled.Map, contentDescription = "Abrir no Mapa", tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            // Linha de Cidade e Geolocation
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    InfoRow(Icons.Filled.LocationCity, "Cidade", ocorrencia.cidade?.ifEmpty { "—" } ?: "—")
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    if (ocorrencia.latitude != null && ocorrencia.longitude != null) {
                                        val coords = "${"%.5f".format(ocorrencia.latitude)}, ${"%.5f".format(ocorrencia.longitude)}"
                                        val clipboardManager = LocalClipboardManager.current
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                InfoRow(Icons.Filled.GpsFixed, "Coordenadas GPS", coords)
                                            }
                                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(coords)) }, modifier = Modifier.size(24.dp).padding(start = 4.dp)) {
                                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    } else {
                                        InfoRow(Icons.Filled.GpsFixed, "Coordenadas GPS", "—")
                                    }
                                }
                            }
                        }

                        // === Imagens da Ocorrência ===
                        if (fotos.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Imagens da Ocorrência", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(minOf(fotos.size, 4)) { index ->
                                        Box(
                                            modifier = Modifier
                                                .size(width = 90.dp, height = 64.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.DarkGray)
                                                .clickable { fullScreenImageUrl = fotos[index] }
                                        ) {
                                            AsyncImage(
                                                model = fotos[index],
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                    if (fotos.size > 4) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 90.dp, height = 64.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.DarkGray)
                                                    .clickable { fullScreenImageUrl = fotos[4] },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = fotos[4],
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(alpha = 0.6f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "+${fotos.size - 4}",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (fullScreenImageUrl != null) {
                            Dialog(
                                onDismissRequest = { fullScreenImageUrl = null },
                                properties = DialogProperties(usePlatformDefaultWidth = false)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable { fullScreenImageUrl = null }) {
                                    AsyncImage(
                                        model = fullScreenImageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(16.dp)
                                    )
                                    IconButton(
                                        onClick = { fullScreenImageUrl = null },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = Color.White)
                                    }
                                }
                            }
                        }

                        // === Botões de Ação ===
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = onEditar,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8AA6FF), contentColor = Color(0xFF0F1A3A))
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Módulos", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE57373)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE57373).copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Excluir", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun CounterBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
