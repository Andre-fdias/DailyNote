package com.andrefdias.dailynote.ui.screens.ocorrencias.modules

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo
import com.andrefdias.dailynote.data.local.entities.RoomNovaVitima
import com.andrefdias.dailynote.util.EquipeUtils
import com.andrefdias.dailynote.util.OcorrenciaPdfGenerator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumoModuleView(
    ocorrencia: RoomNovaOcorrencia,
    veiculos: List<RoomNovoVeiculo>,
    vitimas: List<RoomNovaVitima>,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()
    val gson = Gson()
    val context = LocalContext.current
    
    val (equipeLabel, equipeColor) = EquipeUtils.parseEquipeInfo(ocorrencia.equipe)

    // Cores dinâmicas (Light/Dark Mode)
    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    val mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Relatório de Ocorrência",
                            color = onBgColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Visualize todos os detalhes da ocorrência",
                            color = mutedTextColor,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = onBgColor)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Mais opções", tint = onBgColor)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Compartilhar") },
                                onClick = {
                                    menuExpanded = false
                                    val uri = OcorrenciaPdfGenerator.generatePdf(context, ocorrencia, veiculos, vitimas)
                                    uri?.let {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, it)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório"))
                                    }
                                },
                                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Gerar PDF") },
                                onClick = {
                                    menuExpanded = false
                                    val uri = OcorrenciaPdfGenerator.generatePdf(context, ocorrencia, veiculos, vitimas)
                                    uri?.let {
                                        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(it, "application/pdf")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(viewIntent)
                                    }
                                },
                                leadingIcon = { Icon(Icons.Filled.PictureAsPdf, contentDescription = null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // CARD STATUS (Banner Topo)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = surfaceColor,
                border = BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEAB308).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            // Status Chip Em Andamento / Encerrada
                            val isConcluida = ocorrencia.isConcluida
                            val chipBg = if (isConcluida) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEAB308).copy(alpha = 0.2f)
                            val chipTxtColor = if (isConcluida) Color(0xFF10B981) else Color(0xFFEAB308)
                            
                            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(chipBg).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text(if (isConcluida) "Encerrada" else "Em Andamento", color = chipTxtColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            // Data do Lado Direito
                            Text(ocorrencia.data, color = mutedTextColor, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(ocorrencia.natureza, color = onSurfaceColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            
            // CHIPS INFO LADO A LADO (2 Colunas)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CalendarToday, 
                    title = "Data e Hora", 
                    value = "${ocorrencia.data} às ${ocorrencia.hora}",
                    iconTint = Color(0xFF60A5FA),
                    surfaceColor = surfaceColor,
                    onSurfaceColor = onSurfaceColor,
                    mutedTextColor = mutedTextColor
                )
                InfoChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.People, 
                    title = "Equipe", 
                    value = equipeLabel,
                    iconTint = equipeColor,
                    surfaceColor = surfaceColor,
                    onSurfaceColor = onSurfaceColor,
                    mutedTextColor = mutedTextColor
                )
            }

            // DADOS DA OCORRÊNCIA (Endereço)
            ExpandableResumoCard(
                title = "Dados da Ocorrência",
                icon = Icons.Filled.Info,
                themeColor = Color(0xFF3B82F6), // Azul
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor,
                mutedTextColor = mutedTextColor
            ) {
                val enderecoCompleto = listOfNotNull(
                    ocorrencia.rua.takeIf { !it.isNullOrBlank() },
                    ocorrencia.numero.takeIf { !it.isNullOrBlank() },
                    ocorrencia.bairro.takeIf { !it.isNullOrBlank() },
                    ocorrencia.cidade.takeIf { !it.isNullOrBlank() }
                ).joinToString(", ")
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color(0xFF3B82F6))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Endereço", color = mutedTextColor, fontSize = 12.sp)
                        Text(enderecoCompleto.ifEmpty { "Nenhum endereço informado" }, color = onSurfaceColor, fontSize = 14.sp)
                    }
                }
                
                if (enderecoCompleto.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3B82F6).copy(alpha = 0.1f))
                            .clickable {
                                val uri = Uri.parse("geo:0,0?q=${Uri.encode(enderecoCompleto)}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Map, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Visualizar no Google Maps", color = Color(0xFF3B82F6), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF3B82F6))
                        }
                    }
                }
            }

            // EQUIPE E RECURSOS
            ExpandableResumoCard(
                title = "Equipe e Recursos",
                icon = Icons.Filled.DirectionsCar,
                themeColor = Color(0xFF10B981), // Verde
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor,
                mutedTextColor = mutedTextColor
            ) {
                // Parse viaturas e militares
                val vtrType = object : TypeToken<List<Map<String, String>>>() {}.type
                val viaturasList: List<Map<String, String>> = try { gson.fromJson(ocorrencia.viatura, vtrType) ?: emptyList() } catch(e: Exception) { emptyList() }
                
                val milType = object : TypeToken<List<String>>() {}.type
                val militaresList: List<String> = try { gson.fromJson(ocorrencia.guarnicaoJson, milType) ?: emptyList() } catch(e: Exception) { emptyList() }
                
                val militaresMap = militaresList.map { m ->
                    val parts = m.split(" - ")
                    if (parts.size >= 3) {
                        val viatura = parts[0]
                        val funcao = parts[1]
                        val dados = parts.drop(2).joinToString(" - ")
                        mapOf("viatura" to viatura, "funcao" to funcao, "dados" to dados)
                    } else {
                        mapOf("viatura" to "Sem Viatura", "funcao" to "", "dados" to m)
                    }
                }.groupBy { it["viatura"] ?: "Sem Viatura" }

                val viaturasAgrupadas = viaturasList.groupBy { 
                    val posto = it["posto"]?.takeIf { p -> p.isNotBlank() } ?: "Posto N/I"
                    val unidade = it["unidade"]?.takeIf { u -> u.isNotBlank() } ?: "Unidade N/I"
                    "$posto - $unidade"
                }

                if (viaturasAgrupadas.isNotEmpty()) {
                    viaturasAgrupadas.forEach { (postoUnidade, viaturasDoPosto) ->
                        Text(
                            text = postoUnidade, 
                            color = onSurfaceColor, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 14.sp, 
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp).fillMaxWidth().background(surfaceColor.copy(alpha=0.5f))
                        )
                        viaturasDoPosto.forEach { vtr ->
                            val prefixo = vtr["prefixo"] ?: "Desconhecido"
                            val kmSaida = vtr["kmSaida"]?.takeIf { it.isNotBlank() } ?: "N/I"
                            val kmQuartel = vtr["kmQuartel"]?.takeIf { it.isNotBlank() } ?: "N/I"
                            val guarnicaoDaVtr = militaresMap[prefixo]
                            
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, start = 8.dp), verticalAlignment = Alignment.Top) {
                                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEF4444).copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = Color(0xFFEF4444))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Viatura $prefixo", color = onSurfaceColor, fontWeight = FontWeight.Bold)
                                    Text("KM Saída: $kmSaida • KM Final: $kmQuartel", color = mutedTextColor, fontSize = 12.sp)
                                    if (!guarnicaoDaVtr.isNullOrEmpty()) {
                                        guarnicaoDaVtr.forEach { m ->
                                            val funcao = m["funcao"] ?: ""
                                            val dados = m["dados"] ?: ""
                                            Text("• $funcao: $dados", color = mutedTextColor, fontSize = 12.sp)
                                        }
                                    } else {
                                        Text("Sem guarnição vinculada.", color = mutedTextColor, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                
                val semVtr = militaresMap["Sem Viatura"]
                if (!semVtr.isNullOrEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF6366F1).copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Group, contentDescription = null, tint = Color(0xFF6366F1))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Outros Militares (Sem Viatura)", color = onSurfaceColor, fontWeight = FontWeight.Bold)
                            semVtr.forEach { m ->
                                val funcao = m["funcao"] ?: ""
                                val dados = m["dados"] ?: ""
                                val separator = if (funcao.isNotEmpty()) "$funcao: " else ""
                                Text("• $separator$dados", color = mutedTextColor, fontSize = 12.sp)
                            }
                        }
                    }
                }
                
                if (viaturasList.isEmpty() && semVtr.isNullOrEmpty()) {
                    Text("Nenhum recurso registrado.", color = mutedTextColor, fontSize = 14.sp)
                }
            }

            // VEÍCULOS
            ExpandableResumoCard(
                title = "Veículos Cadastrados",
                icon = Icons.Filled.DirectionsCar,
                themeColor = Color(0xFF14B8A6), // Teal
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor,
                mutedTextColor = mutedTextColor
            ) {
                if (veiculos.isNotEmpty()) {
                    veiculos.forEach { v ->
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.background).padding(12.dp).padding(bottom = 8.dp)) {
                            Text("${v.marca} ${v.modelo}", color = onSurfaceColor, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Placa: ${v.placa.ifEmpty { "N/A" }}", color = mutedTextColor, fontSize = 12.sp)
                                Text("Cor: ${v.cor.ifEmpty { "N/A" }}", color = mutedTextColor, fontSize = 12.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Ano: ${v.ano.ifEmpty { "N/A" }}", color = mutedTextColor, fontSize = 12.sp)
                                Text("Renavam: ${v.renavam?.ifEmpty { "N/A" } ?: "N/A"}", color = mutedTextColor, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    Text("Nenhum veículo registrado.", color = mutedTextColor, fontSize = 14.sp)
                }
            }

            // VÍTIMAS E ENVOLVIDOS
            ExpandableResumoCard(
                title = "Vítimas e Envolvidos",
                icon = Icons.Filled.MedicalServices,
                themeColor = Color(0xFF8B5CF6), // Purple
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor,
                mutedTextColor = mutedTextColor
            ) {
                val pType = object : TypeToken<List<Map<String, Any>>>() {}.type
                val pessoasList: List<Map<String, Any>> = try { gson.fromJson(ocorrencia.pessoasJson, pType) ?: emptyList() } catch(e: Exception) { emptyList() }
                
                if (pessoasList.isNotEmpty() || vitimas.isNotEmpty()) {
                    if (pessoasList.isNotEmpty()) {
                        Text("Pessoas Cadastradas", color = onSurfaceColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        pessoasList.forEach { p ->
                            val nome = p["nome"] as? String ?: "Desconhecido"
                            val condicao = p["condicao"] as? String ?: "Envolvido"
                            val doc = p["cpf"] as? String ?: p["rg"] as? String ?: ""
                            
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = mutedTextColor)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("$nome ($condicao)", color = onSurfaceColor, fontSize = 14.sp)
                                    if (doc.isNotEmpty()) Text("Doc: $doc", color = mutedTextColor, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    
                    if (vitimas.isNotEmpty()) {
                        Text("Detalhamento de Vítimas", color = onSurfaceColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        vitimas.forEach { vitima ->
                            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFEF4444).copy(alpha = 0.15f)).padding(12.dp).padding(bottom = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFFEF4444))
                                    Spacer(Modifier.width(8.dp))
                                    Text(vitima.nome, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Idade: ${vitima.idade ?: "N/A"} anos | CPF: ${vitima.cpf ?: "N/A"}", color = mutedTextColor, fontSize = 12.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("Destino: ${vitima.hospitalDestino.ifEmpty { "Não informado" }}", color = mutedTextColor, fontSize = 12.sp)
                                if (vitima.nomeMedico.isNotEmpty()) {
                                    Text("Médico: ${vitima.nomeMedico} (CRM: ${vitima.crmMedico})", color = mutedTextColor, fontSize = 12.sp)
                                }
                                Text("Socorrido por: ${vitima.quemSocorreu} ${vitima.viaturaSocorroId?.let { "($it)" } ?: ""}", color = mutedTextColor, fontSize = 12.sp)
                                if (vitima.lesoes.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("Histórico/Lesões: ${vitima.lesoes}", color = onSurfaceColor, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    Text("Nenhum envolvido ou vítima registrado.", color = mutedTextColor, fontSize = 14.sp)
                }
            }

            // HISTÓRICO
            ExpandableResumoCard(
                title = "Histórico de Ocorrência",
                icon = Icons.Filled.Description,
                themeColor = Color(0xFFF97316), // Orange
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor,
                mutedTextColor = mutedTextColor
            ) {
                Text(
                    text = ocorrencia.historico.takeIf { !it.isNullOrBlank() } ?: "Nenhum histórico relatado.",
                    color = mutedTextColor,
                    fontSize = 14.sp
                )
            }

            // EVIDÊNCIAS
            ExpandableResumoCard(
                title = "Evidências Fotográficas",
                icon = Icons.Filled.CameraAlt,
                themeColor = Color(0xFFD946EF), // Pink
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor,
                mutedTextColor = mutedTextColor
            ) {
                val fType = object : TypeToken<List<String>>() {}.type
                val fotosList: List<String> = try { gson.fromJson(ocorrencia.fotosUrisJson, fType) ?: emptyList() } catch(e: Exception) { emptyList() }
                
                if (fotosList.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(fotosList) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "Foto da ocorrência",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }
                    }
                } else {
                    Text("Nenhuma imagem anexada.", color = mutedTextColor, fontSize = 14.sp)
                }
            }
            
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
fun ExpandableResumoCard(
    title: String,
    icon: ImageVector,
    themeColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    mutedTextColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(themeColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = themeColor, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(title, color = onSurfaceColor, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = mutedTextColor)
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
fun InfoChip(modifier: Modifier = Modifier, icon: ImageVector, title: String, value: String, iconTint: Color, surfaceColor: Color, onSurfaceColor: Color, mutedTextColor: Color) {
    Surface(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        color = surfaceColor, // surfaceColor
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, color = mutedTextColor, fontSize = 10.sp)
                Text(value, color = onSurfaceColor, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 2)
            }
        }
    }
}
