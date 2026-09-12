package com.andrefdias.dailynote.ui.screens.ocorrencias.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.andrefdias.dailynote.R
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Color
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import com.andrefdias.dailynote.data.local.entities.RoomNovaVitima
import com.andrefdias.dailynote.ui.screens.ocorrencias.OcorrenciaOpcoesViewModel
import com.andrefdias.dailynote.ui.screens.ocorrencias.PessoaEnvolvida
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitimasModuleView(
    ocorrencia: RoomNovaOcorrencia,
    viewModel: OcorrenciaOpcoesViewModel,
    onCancel: () -> Unit
) {
    LaunchedEffect(ocorrencia.id) {
        viewModel.loadVitimas(ocorrencia.id)
    }
    
    val vitimas by viewModel.vitimas.collectAsState()
    var isAdding by remember { mutableStateOf(false) }
    var vitimaEditing by remember { mutableStateOf<RoomNovaVitima?>(null) }
    
    val gson = Gson()
    val type = object : TypeToken<List<PessoaEnvolvida>>() {}.type
    val pessoasDisponiveis: List<PessoaEnvolvida> = try {
        gson.fromJson(ocorrencia.pessoasJson, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    if (isAdding || vitimaEditing != null) {
        VitimaFormScreen(
            ocorrenciaId = ocorrencia.id,
            vitima = vitimaEditing,
            pessoasDisponiveis = pessoasDisponiveis,
            viaturaJson = ocorrencia.viatura,
            onSave = { v ->
                if (vitimaEditing != null) {
                    viewModel.updateVitima(v)
                } else {
                    viewModel.addVitima(v)
                }
                isAdding = false
                vitimaEditing = null
            },
            onCancel = {
                isAdding = false
                vitimaEditing = null
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("VOLTAR AO DASHBOARD", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.MedicalServices, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vítimas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Pessoas socorridas e triagem de APH", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        
                        Button(
                            onClick = { isAdding = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Nova")
                        }
                    }
                }
                
                if (vitimas.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.MedicalServices, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(16.dp))
                                Text("Nenhuma vítima registrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Cadastre as pessoas socorridas, triagens de traumas, sinais vitais e hospitais de destino de urgência.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(Modifier.height(24.dp))
                                Button(
                                    onClick = { isAdding = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("ADICIONAR VÍTIMA", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(vitimas) { v ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vitimaEditing = v },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(if (v.resultadoOcorrencia.contains("Óbito", true)) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.PersonOutline,
                                        contentDescription = null,
                                        tint = if (v.resultadoOcorrencia.contains("Óbito", true)) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                
                                Spacer(Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = v.nome.ifEmpty { "NÃO IDENTIFICADA" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    if (v.resultadoOcorrencia.isNotBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = v.resultadoOcorrencia,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (v.resultadoOcorrencia.contains("Óbito", true)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                IconButton(onClick = { viewModel.deleteVitima(v) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitimaFormScreen(
    ocorrenciaId: String,
    vitima: RoomNovaVitima?,
    pessoasDisponiveis: List<PessoaEnvolvida>,
    viaturaJson: String?,
    onSave: (RoomNovaVitima) -> Unit,
    onCancel: () -> Unit
) {
    val gson = Gson()
    
    val paVisualTransformation = androidx.compose.ui.text.input.VisualTransformation { text ->
        val original = text.text
        var out = ""
        for (i in original.indices) {
            out += original[i]
            if (original.length == 4 && i == 1) out += "x"
            if (original.length >= 5 && i == 2) out += "x"
        }
        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (original.length == 4) return if (offset <= 2) offset else offset + 1
                if (original.length >= 5) return if (offset <= 3) offset else offset + 1
                return offset
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (original.length == 4) return if (offset <= 2) offset else offset - 1
                if (original.length >= 5) return if (offset <= 3) offset else offset - 1
                return offset
            }
        }
        androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(out), offsetMapping)
    }
    
    val satVisualTransformation = androidx.compose.ui.text.input.VisualTransformation { text ->
        val original = text.text
        val out = if (original.isEmpty()) "" else "$original%"
        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset
            override fun transformedToOriginal(offset: Int): Int = if (offset > original.length) original.length else offset
        }
        androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(out), offsetMapping)
    }

    // Identificação
    var selectedPessoaId by remember { mutableStateOf(vitima?.pessoaId ?: "") }
    var selectedNome by remember { mutableStateOf(vitima?.nome ?: "") }
    
    // Avaliação Clínica
    var lesoes by remember { mutableStateOf(vitima?.lesoes ?: "") } // Histórico das lesões
    
    // Sinais Vitais JSON parsing
    val initialSinais = try {
        val type = object : TypeToken<Map<String, String>>() {}.type
        gson.fromJson<Map<String, String>>(vitima?.sinaisVitaisJson ?: "{}", type) ?: emptyMap()
    } catch (e: Exception) { emptyMap() }
    
    var pressaoArterial by remember { mutableStateOf(initialSinais["pa"] ?: "") }
    var pulso by remember { mutableStateOf(initialSinais["pulso"] ?: "") } // FC
    var saturacao by remember { mutableStateOf(initialSinais["spo2"] ?: "") }
    var respiracao by remember { mutableStateOf(initialSinais["fr"] ?: "") }
    
    // Glasgow parsing from sinaisVitaisJson (novo padrão) ou lesoesEstruturadasJson (legado)
    val legacyGlasgow = try {
        val type = object : TypeToken<Map<String, Int>>() {}.type
        gson.fromJson<Map<String, Int>>(vitima?.lesoesEstruturadasJson ?: "{}", type) ?: emptyMap()
    } catch (e: Exception) { emptyMap() }
    
    var glasgowAO by remember { mutableStateOf(initialSinais["ao"]?.toIntOrNull() ?: legacyGlasgow["ao"] ?: 0) }
    var glasgowRV by remember { mutableStateOf(initialSinais["rv"]?.toIntOrNull() ?: legacyGlasgow["rv"] ?: 0) }
    var glasgowRM by remember { mutableStateOf(initialSinais["rm"]?.toIntOrNull() ?: legacyGlasgow["rm"] ?: 0) }
    
    val totalGlasgow = if (glasgowAO > 0 && glasgowRV > 0 && glasgowRM > 0) glasgowAO + glasgowRV + glasgowRM else 0
    
    // Resgate e Transporte
    var quemSocorreu by remember { mutableStateOf(vitima?.quemSocorreu ?: "") }
    var viaturaSocorroId by remember { mutableStateOf(vitima?.viaturaSocorroId ?: "") } 
    var destinoSocorro by remember { mutableStateOf(vitima?.destinoSocorro ?: "") } // Hospital/Destino
    var resultadoOcorrencia by remember { mutableStateOf(vitima?.resultadoOcorrencia ?: "") }
    
    // Recepção Médica
    var nomeMedico by remember { mutableStateOf(vitima?.nomeMedico ?: "") }
    var crmMedico by remember { mutableStateOf(vitima?.crmMedico ?: "") }

    var showBodyMap by remember { mutableStateOf(false) }
    
    // Body points parsing
    val initialPoints = try {
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        gson.fromJson<List<Map<String, Any>>>(vitima?.lesoesAparentes ?: "[]", type) ?: emptyList()
    } catch(e:Exception) { emptyList() }
    
    var bodyPoints by remember { mutableStateOf(
        initialPoints.map { 
            BodyPoint(
                x = (it["x"] as? Double)?.toFloat() ?: 0f, 
                y = (it["y"] as? Double)?.toFloat() ?: 0f,
                tipo = it["tipo"] as? String ?: "Lesão",
                regiao = it["regiao"] as? String ?: ""
            ) 
        }
    )}
    
    // Dropdown states
    var pessoaExpanded by remember { mutableStateOf(false) }
    var resultadoExpanded by remember { mutableStateOf(false) }
    var quemSocorreuExpanded by remember { mutableStateOf(false) }
    var viaturaExpanded by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { 
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (vitima == null) "Registrar Vítima" else "Editar Vítima", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancelar")
                    }
                }
            }
            
            // 1. Identificação da Vítima
            item {
                ModernSection(title = "Identificação da Vítima", icon = Icons.Filled.Person) {
                    
                    // Pessoa Dropdown
                    ExposedDropdownMenuBox(
                        expanded = pessoaExpanded,
                        onExpandedChange = { pessoaExpanded = !pessoaExpanded },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        CompactTextField(
                            value = selectedNome.ifEmpty { "Selecionar Pessoa" },
                            onValueChange = {},
                            label = "",
                            placeholder = "Selecionar Pessoa",
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pessoaExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = pessoaExpanded,
                            onDismissRequest = { pessoaExpanded = false }
                        ) {
                            if (pessoasDisponiveis.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Nenhuma pessoa cadastrada na ocorrência") },
                                    onClick = { pessoaExpanded = false }
                                )
                            } else {
                                pessoasDisponiveis.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p.nome.ifEmpty { "Não Identificada" }) },
                                        onClick = {
                                            selectedPessoaId = p.id ?: ""
                                            selectedNome = p.nome
                                            pessoaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Resultado Ocorrencia Dropdown
                    ExposedDropdownMenuBox(
                        expanded = resultadoExpanded,
                        onExpandedChange = { resultadoExpanded = !resultadoExpanded },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        CompactTextField(
                            value = resultadoOcorrencia.ifEmpty { "Resultado da Ocorrência" },
                            onValueChange = {},
                            label = "",
                            placeholder = "Resultado da Ocorrência",
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = resultadoExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = resultadoExpanded,
                            onDismissRequest = { resultadoExpanded = false }
                        ) {
                            val options = listOf("Atendida", "Cancelado", "Recusa")
                            options.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        resultadoOcorrencia = opt
                                        resultadoExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // 2. Lesões e Avaliação
            item {
                ModernSection(title = "Lesões e Avaliação", icon = Icons.Filled.Edit) {
                    CompactTextField(
                        value = lesoes,
                        onValueChange = { lesoes = it },
                        label = "",
                        placeholder = "Histórico das Lesões",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Button(
                        onClick = { showBodyMap = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(Icons.Filled.AccessibilityNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Mapear Lesões no Corpo")
                    }
                    
                    if (bodyPoints.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Lesões Mapeadas:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            bodyPoints.forEach { point ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text("${point.tipo}${if(point.regiao.isNotBlank()) " - " + point.regiao else ""}", color = MaterialTheme.colorScheme.onErrorContainer) },
                                    leadingIcon = { Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp)) },
                                    trailingIcon = { 
                                        Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp).clickable { bodyPoints = bodyPoints - point }) 
                                    },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                                    border = null
                                )
                            }
                        }
                    }
                }
            }
            
            // 3. Sinais Vitais
            item {
                ModernSection(title = "Sinais Vitais", icon = Icons.Filled.MonitorHeart) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CompactTextField(
                            value = pulso,
                            onValueChange = { pulso = it },
                            label = "Freq. Cardíaca",
                            placeholder = "FC (BPM)",
                            modifier = Modifier.weight(1f).padding(bottom = 16.dp)
                        )
                        CompactTextField(
                            value = pressaoArterial,
                            onValueChange = { newValue -> 
                                val filtered = newValue.filter { it.isDigit() }
                                if (filtered.length <= 6) pressaoArterial = filtered
                            },
                            label = "Pressão Arterial",
                            placeholder = "P.A.",
                            visualTransformation = paVisualTransformation,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.weight(1f).padding(bottom = 16.dp)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CompactTextField(
                            value = saturacao,
                            onValueChange = { newValue -> 
                                val filtered = newValue.filter { it.isDigit() }
                                if (filtered.length <= 3) saturacao = filtered
                            },
                            label = "Saturação",
                            placeholder = "Sat. O2 (%)",
                            visualTransformation = satVisualTransformation,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.weight(1f).padding(bottom = 8.dp)
                        )
                        CompactTextField(
                            value = respiracao,
                            onValueChange = { respiracao = it },
                            label = "Freq. Respiratória",
                            placeholder = "Resp. (mpm)",
                            modifier = Modifier.weight(1f).padding(bottom = 8.dp)
                        )
                    }
                }
            }
            
            // 4. Escala de Coma de Glasgow
            item {
                var expandedGlasgow by remember { mutableStateOf(false) }
                ModernSection(title = "Escala de Coma de Glasgow", icon = Icons.Filled.Psychology) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { expandedGlasgow = !expandedGlasgow }
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Total GCS", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = if (totalGlasgow > 0) totalGlasgow.toString() else "--",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (totalGlasgow > 0 && totalGlasgow <= 8) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(if (expandedGlasgow) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = MaterialTheme.colorScheme.outline)
                    }
                    
                    androidx.compose.animation.AnimatedVisibility(visible = expandedGlasgow) {
                        Column {
                            Text("Abertura Ocular (AO)", fontWeight = FontWeight.Bold)
                            Text("Como os olhos respondem", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(bottom = 8.dp))
                            GlasgowOption(1, "Ausente", glasgowAO) { glasgowAO = 1 }
                            GlasgowOption(2, "Ao estímulo doloroso", glasgowAO) { glasgowAO = 2 }
                            GlasgowOption(3, "Ao comando verbal", glasgowAO) { glasgowAO = 3 }
                            GlasgowOption(4, "Espontânea", glasgowAO) { glasgowAO = 4 }
                            Spacer(Modifier.height(16.dp))
                            
                            Text("Resposta Verbal (RV)", fontWeight = FontWeight.Bold)
                            Text("Qualidade da fala/som", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(bottom = 8.dp))
                            GlasgowOption(1, "Ausente", glasgowRV) { glasgowRV = 1 }
                            GlasgowOption(2, "Sons incompreensíveis", glasgowRV) { glasgowRV = 2 }
                            GlasgowOption(3, "Palavras inapropriadas", glasgowRV) { glasgowRV = 3 }
                            GlasgowOption(4, "Confuso", glasgowRV) { glasgowRV = 4 }
                            GlasgowOption(5, "Orientada", glasgowRV) { glasgowRV = 5 }
                            Spacer(Modifier.height(16.dp))
                            
                            Text("Resposta Motora (RM)", fontWeight = FontWeight.Bold)
                            Text("Melhor resposta dos membros", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(bottom = 8.dp))
                            GlasgowOption(1, "Ausente", glasgowRM) { glasgowRM = 1 }
                            GlasgowOption(2, "Extensão anormal", glasgowRM) { glasgowRM = 2 }
                            GlasgowOption(3, "Flexão anormal", glasgowRM) { glasgowRM = 3 }
                            GlasgowOption(4, "Flexão normal (retirada)", glasgowRM) { glasgowRM = 4 }
                            GlasgowOption(5, "Localiza a dor", glasgowRM) { glasgowRM = 5 }
                            GlasgowOption(6, "Obedece a comandos", glasgowRM) { glasgowRM = 6 }
                        }
                    }
                }
            }
            
            // 5. Transporte e Socorro
            item {
                ModernSection(title = "Transporte e Socorro", icon = Icons.Filled.LocalShipping) {
                    
                    // Quem Socorreu Dropdown
                    ExposedDropdownMenuBox(
                        expanded = quemSocorreuExpanded,
                        onExpandedChange = { quemSocorreuExpanded = !quemSocorreuExpanded },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        CompactTextField(
                            value = quemSocorreu.ifEmpty { "Quem Socorreu" },
                            onValueChange = {},
                            label = "",
                            placeholder = "Quem Socorreu",
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = quemSocorreuExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = quemSocorreuExpanded,
                            onDismissRequest = { quemSocorreuExpanded = false }
                        ) {
                            val options = listOf("Corpo de Bombeiros", "SAMU", "Ambulância Municipal", "Populares", "Viatura da PM", "Outros")
                            options.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        quemSocorreu = opt
                                        quemSocorreuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Viatura de Socorro Dropdown
                    ExposedDropdownMenuBox(
                        expanded = viaturaExpanded,
                        onExpandedChange = { viaturaExpanded = !viaturaExpanded },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        CompactTextField(
                            value = viaturaSocorroId.ifEmpty { "Viatura de Socorro" },
                            onValueChange = {},
                            label = "",
                            placeholder = "Viatura de Socorro",
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viaturaExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = viaturaExpanded,
                            onDismissRequest = { viaturaExpanded = false }
                        ) {
                            val viaturaType = object : TypeToken<List<Map<String, String>>>() {}.type
                            val viaturasCadastradas = try {
                                gson.fromJson<List<Map<String, String>>>(viaturaJson ?: "[]", viaturaType) ?: emptyList()
                            } catch(e: Exception) { emptyList() }
                            
                            val options = viaturasCadastradas.mapNotNull { it["prefixo"] }.ifEmpty { listOf("Nenhuma viatura cadastrada") }
                            options.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        viaturaSocorroId = opt
                                        viaturaExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    CompactTextField(
                        value = destinoSocorro,
                        onValueChange = { destinoSocorro = it },
                        label = "",
                        placeholder = "Hospital / Destino",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            
            // 6. Médico Responsável
            item {
                ModernSection(title = "Médico Responsável", icon = Icons.Filled.PersonOutline) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CompactTextField(
                            value = nomeMedico,
                            onValueChange = { nomeMedico = it },
                            label = "",
                            placeholder = "Nome do Médico",
                            modifier = Modifier.weight(1.5f).padding(bottom = 8.dp)
                        )
                        CompactTextField(
                            value = crmMedico,
                            onValueChange = { crmMedico = it },
                            label = "",
                            placeholder = "CRM",
                            modifier = Modifier.weight(1f).padding(bottom = 8.dp)
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        val sinaisVitaisMap = mapOf(
                            "pa" to pressaoArterial,
                            "pulso" to pulso,
                            "spo2" to saturacao,
                            "fr" to respiracao,
                            "glasgow" to totalGlasgow.toString(),
                            "ao" to glasgowAO.toString(),
                            "rv" to glasgowRV.toString(),
                            "rm" to glasgowRM.toString()
                        )
                        
                        val burns = bodyPoints.filter { it.tipo == "Queimadura" }
                        val otherLesions = bodyPoints.filter { it.tipo != "Queimadura" }
                        
                        val lesoesList = mutableListOf<String>()
                        if (burns.isNotEmpty()) {
                            val burnArea = calcularAreaQueimada(bodyPoints)
                            val regioes = burns.map { it.regiao }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                            lesoesList.add("Queimadura em $regioes ($burnArea% SCQ)")
                        }
                        
                        if (otherLesions.isNotEmpty()) {
                            lesoesList.add(
                                otherLesions.groupBy { it.tipo }.map { (tipo, pontos) ->
                                    val regioes = pontos.map { it.regiao }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                                    if (regioes.isNotEmpty()) "$tipo em $regioes" else "${pontos.size}x $tipo"
                                }.joinToString("; ")
                            )
                        }
                        
                        val lesoesStr = lesoesList.joinToString("; ")
                        
                        val nova = RoomNovaVitima(
                            id = vitima?.id ?: UUID.randomUUID().toString(),
                            ocorrenciaId = ocorrenciaId,
                            nome = selectedNome,
                            idade = null,
                            pessoaId = selectedPessoaId.takeIf { it.isNotBlank() },
                            lesoes = if (lesoesStr.isEmpty()) "Nenhuma lesão mapeada" else lesoesStr,
                            lesoesEstruturadasJson = "[]", // Limpando hack antigo do Glasgow
                            destinoSocorro = destinoSocorro,
                            quemSocorreu = quemSocorreu,
                            resultadoOcorrencia = resultadoOcorrencia,
                            viaturaSocorroId = viaturaSocorroId,
                            hospitalDestino = destinoSocorro,
                            nomeMedico = nomeMedico,
                            crmMedico = crmMedico,
                            sinaisVitaisJson = gson.toJson(sinaisVitaisMap),
                            cpf = null,
                            lesoesAparentes = gson.toJson(bodyPoints.map { mapOf("x" to it.x, "y" to it.y, "tipo" to it.tipo, "regiao" to it.regiao) }),
                            transportadoPor = null
                        )
                        onSave(nova)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (vitima == null) "CONCLUIR VÍTIMA" else "SALVAR ALTERAÇÕES", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
    
    // Alerts
    if (showBodyMap) {
        Dialog(onDismissRequest = { showBodyMap = false }) {
            BodyMappingScreen(
                points = bodyPoints,
                onAddPoint = { p -> bodyPoints = bodyPoints + p },
                onRemovePoint = { p -> bodyPoints = bodyPoints - p },
                onClose = { showBodyMap = false }
            )
        }
    }
}

@Composable
fun GlasgowOption(value: Int, label: String, selectedValue: Int, onSelect: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (selectedValue == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                color = if (selectedValue == value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}


data class BodyPoint(val x: Float, val y: Float, val tipo: String, val regiao: String = "")

private fun getAnatomicalRegion(x: Float, y: Float, width: Float, height: Float): String {
    if (width <= 0f || height <= 0f) return "Corpo"
    val nx = x / width
    val ny = y / height
    val isFront = nx < 0.5f
    return when {
        ny < 0.15f -> "Cabeça " + if (isFront) "Frente" else "Costas"
        ny in 0.15f..0.45f -> {
            val localNx = if (isFront) nx else (nx - 0.5f)
            when {
                localNx < 0.15f -> "Braço Direito " + if (isFront) "Frente" else "Costas"
                localNx > 0.35f -> "Braço Esquerdo " + if (isFront) "Frente" else "Costas"
                else -> "Tronco " + if (isFront) "Anterior" else "Posterior"
            }
        }
        ny > 0.45f && ny <= 0.52f -> {
            if (isFront && nx in 0.22f..0.28f) "Genitália"
            else "Tronco " + if (isFront) "Anterior" else "Posterior"
        }
        else -> {
            val localNx = if (isFront) nx else (nx - 0.5f)
            when {
                localNx < 0.25f -> "Perna Direita " + if (isFront) "Frente" else "Costas"
                else -> "Perna Esquerda " + if (isFront) "Frente" else "Costas"
            }
        }
    }
}

private fun calcularAreaQueimada(pontos: List<BodyPoint>): Double {
    val regioesQueimadas = pontos.filter { it.tipo == "Queimadura" }.map { it.regiao }.toSet()
    var total = 0.0
    
    if (regioesQueimadas.contains("Cabeça Frente")) total += 4.5
    if (regioesQueimadas.contains("Cabeça Costas")) total += 4.5
    
    if (regioesQueimadas.contains("Tronco Anterior")) total += 18.0
    if (regioesQueimadas.contains("Tronco Posterior")) total += 18.0
    
    if (regioesQueimadas.contains("Braço Direito Frente")) total += 4.5
    if (regioesQueimadas.contains("Braço Direito Costas")) total += 4.5
    if (regioesQueimadas.contains("Braço Esquerdo Frente")) total += 4.5
    if (regioesQueimadas.contains("Braço Esquerdo Costas")) total += 4.5
    
    if (regioesQueimadas.contains("Perna Direita Frente")) total += 9.0
    if (regioesQueimadas.contains("Perna Direita Costas")) total += 9.0
    if (regioesQueimadas.contains("Perna Esquerda Frente")) total += 9.0
    if (regioesQueimadas.contains("Perna Esquerda Costas")) total += 9.0
    
    if (regioesQueimadas.contains("Genitália")) total += 1.0
    
    return total
}

@Composable
fun BodyMappingScreen(
    points: List<BodyPoint>,
    onAddPoint: (BodyPoint) -> Unit,
    onRemovePoint: (BodyPoint) -> Unit,
    onClose: () -> Unit
) {
    var showTypeDialog by remember { mutableStateOf<Offset?>(null) }
    var boxSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mapear Lesões", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, "Fechar")
                }
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .onGloballyPositioned { coordinates ->
                        boxSize = androidx.compose.ui.geometry.Size(
                            coordinates.size.width.toFloat(), 
                            coordinates.size.height.toFloat()
                        )
                    }
            ) {
                // Corpo humano image
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.body_map_diagram),
                    contentDescription = "Diagrama do Corpo Humano",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                        detectTapGestures { offset ->
                            showTypeDialog = offset
                        }
                    }
                )
                
                // Draw points
                points.forEach { point ->
                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(androidx.compose.ui.platform.LocalDensity.current) { point.x.toDp() } - 12.dp,
                                y = with(androidx.compose.ui.platform.LocalDensity.current) { point.y.toDp() } - 12.dp
                            )
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.7f))
                            .clickable { onRemovePoint(point) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(point.tipo.take(1), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            Text(
                "Toque na imagem para registrar uma lesão. Toque em um marcador para remover.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(16.dp)
            )
            if (points.any { it.tipo == "Queimadura" }) {
                Text(
                    "💡 Regra da Palma da Mão: A palma da vítima (com dedos) = 1% SCQ.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp).padding(bottom = 8.dp)
                )
            }
        }
    }
    
    if (showTypeDialog != null) {
        AlertDialog(
            onDismissRequest = { showTypeDialog = null },
            title = { Text("Selecionar Tipo de Lesão") },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(vertical = 8.dp)
                ) {
                    val lesoes = listOf(
                        "Contusão", "Hematoma", "Equimose", 
                        "Escoriação / Abrasão", "Ferida Incisiva / Cortante", 
                        "Ferida Contusa", "Ferida Perfurante", 
                        "Ferida Perfurocontusa", "Ferida Perfurocortante", 
                        "Ferida Penetrante", "Ferida Transfixante", 
                        "Avulsão / Amputação", "Laceração", 
                        "Fratura", "Queimadura", "Hemorragia", "Outro"
                    )
                    lesoes.forEach { tipo ->
                        Text(
                            text = tipo,
                            modifier = Modifier.fillMaxWidth().clickable {
                                val regiao = getAnatomicalRegion(showTypeDialog!!.x, showTypeDialog!!.y, boxSize.width, boxSize.height)
                                onAddPoint(BodyPoint(showTypeDialog!!.x, showTypeDialog!!.y, tipo, regiao))
                                showTypeDialog = null
                            }.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTypeDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ModernSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
        content()
    }
}

@Composable
private fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    readOnly: Boolean = false,
    label: String? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
        label = label?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        singleLine = true
    )
}
