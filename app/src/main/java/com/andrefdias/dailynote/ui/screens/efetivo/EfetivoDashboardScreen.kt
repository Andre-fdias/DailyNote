package com.andrefdias.dailynote.ui.screens.efetivo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Cores Temáticas
private val BrandBlue = Color(0xFF2563EB)
private val BrandRed = Color(0xFFDC2626)
private val BrandGreen = Color(0xFF16A34A)
private val BrandYellow = Color(0xFFD97706)
private val BrandPurple = Color(0xFF7C3AED)
private val BrandTeal = Color(0xFF0D9488)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EfetivoDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToList: () -> Unit,
    viewModel: EfetivoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showCursoModal by remember { mutableStateOf(false) }
    var selectedCurso by remember { mutableStateOf("") }
    var selectedCursoMilitaries by remember { mutableStateOf<List<com.andrefdias.dailynote.domain.model.EfetivoMilitar>>(emptyList()) }

    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(modifier = Modifier.padding(end = 8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(modifier = Modifier.size(6.dp, 16.dp).background(BrandYellow, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(6.dp, 20.dp).background(BrandRed, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.size(6.dp, 12.dp).background(BrandBlue, RoundedCornerShape(2.dp)))
                        }
                        Text("Dashboard Efetivo", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchData() }) { Icon(Icons.Filled.Refresh, "Atualizar", tint = textPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text("Erro ao carregar dados", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uiState.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val isAuthError = uiState.errorMessage?.contains("remoteconsent", ignoreCase = true) == true ||
                                      uiState.errorMessage?.contains("UserRecoverableAuth", ignoreCase = true) == true ||
                                      uiState.errorMessage?.contains("SIGN_IN_REQUIRED", ignoreCase = true) == true

                    if (isAuthError) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(
                                com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.appdata"),
                                com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/calendar.events"),
                                com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/spreadsheets.readonly")
                            )
                            .build()
                        val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                        
                        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                        ) { result ->
                            viewModel.fetchData()
                        }
                        
                        Text("As novas permissões do Google (Planilhas e Drive) precisam ser aceitas para prosseguir.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { launcher.launch(googleSignInClient.signInIntent) }) {
                            Text("Autorizar Acesso Google")
                        }
                    } else {
                        Button(onClick = { viewModel.fetchData() }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            }
        } else {
            val totalEfetivo = uiState.efetivoList.size
            val emProntidao = uiState.efetivoList.count { it.prontidao.equals("SIM", true) }
            val emFeriasCount = uiState.efetivoList.count { it.status.contains("FÉRIAS", true) }
            val afastados = uiState.efetivoList.count { it.status.contains("LTS", true) || it.status.contains("DISP", true) }

            val hojeKpi = LocalDate.now()
            val dateFormatterKpi = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val formatterTwoDigitYearKpi = DateTimeFormatter.ofPattern("dd/MM/yy")
            fun parseDateKpi(dateStr: String): LocalDate? {
                if (dateStr.isBlank()) return null
                val cleaned = dateStr.trim()
                return try {
                    if (cleaned.length == 8) LocalDate.parse(cleaned, formatterTwoDigitYearKpi)
                    else LocalDate.parse(cleaned, dateFormatterKpi)
                } catch (e: Exception) { null }
            }

            val idades = uiState.efetivoList.mapNotNull { 
                parseDateKpi(it.aniversario)?.let { date -> ChronoUnit.YEARS.between(date, hojeKpi).toInt() }
            }
            val idadeMedia = if (idades.isNotEmpty()) idades.average().toInt() else 0

            val temposServico = uiState.efetivoList.mapNotNull {
                parseDateKpi(it.admissao)?.let { date -> ChronoUnit.YEARS.between(date, hojeKpi).toInt() }
            }
            val tempoServicoMedia = if (temposServico.isNotEmpty()) temposServico.average().toInt() else 0

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // INDICADORES RAPIDOS
                item {
                    Column {
                        Text("Indicadores Rápidos", color = textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiCard(modifier = Modifier.weight(1f), title = "Efetivo", value = totalEfetivo.toString(), icon = Icons.Filled.People, iconColor = BrandBlue, cardColor = cardColor, textColor = textPrimary)
                            KpiCard(modifier = Modifier.weight(1f), title = "Prontidão", value = emProntidao.toString(), icon = Icons.Filled.VerifiedUser, iconColor = BrandGreen, cardColor = cardColor, textColor = textPrimary)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiCard(modifier = Modifier.weight(1f), title = "Férias", value = emFeriasCount.toString(), icon = Icons.Filled.BeachAccess, iconColor = BrandYellow, cardColor = cardColor, textColor = textPrimary)
                            KpiCard(modifier = Modifier.weight(1f), title = "Afastados", value = afastados.toString(), icon = Icons.Filled.MedicalServices, iconColor = BrandRed, cardColor = cardColor, textColor = textPrimary)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiCard(modifier = Modifier.weight(1f), title = "Idade Média", value = "$idadeMedia anos", icon = Icons.Filled.Cake, iconColor = BrandPurple, cardColor = cardColor, textColor = textPrimary)
                            KpiCard(modifier = Modifier.weight(1f), title = "T. Serviço", value = "$tempoServicoMedia anos", icon = Icons.Filled.WorkHistory, iconColor = BrandTeal, cardColor = cardColor, textColor = textPrimary)
                        }
                    }
                }

                // ALERTAS DE VENCIMENTO
                item {
                    val hoje = LocalDate.now()
                    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val formatterTwoDigitYear = DateTimeFormatter.ofPattern("dd/MM/yy")
                    fun parseDate(dateStr: String): LocalDate? {
                        if (dateStr.isBlank()) return null
                        val cleaned = dateStr.trim()
                        return try {
                            if (cleaned.length == 8) LocalDate.parse(cleaned, formatterTwoDigitYear)
                            else LocalDate.parse(cleaned, dateFormatter)
                        } catch (e: Exception) { null }
                    }

                    val alertasIas = uiState.efetivoList.filter { militar ->
                        val date = parseDate(militar.validadeIas)
                        date != null && ChronoUnit.DAYS.between(hoje, date) <= 90
                    }
                    val alertasTox = uiState.efetivoList.filter { militar ->
                        val date = parseDate(militar.validadeToxicologico)
                        date != null && ChronoUnit.DAYS.between(hoje, date) <= 90
                    }

                    if (alertasIas.isNotEmpty() || alertasTox.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Warning, null, tint = BrandRed, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Alertas de Vencimento (90 dias)", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Spacer(Modifier.height(16.dp))
                                alertasIas.forEach { m ->
                                    val date = parseDate(m.validadeIas)
                                    val days = date?.let { ChronoUnit.DAYS.between(hoje, it) } ?: 0
                                    val statusMsg = if (days < 0) "VENCIDO há ${-days} dias" else "Vence em $days dias"
                                    ListItemView(title = "${m.graduacao} ${m.nomeDeGuerra}", subtitle = "IAS: ${m.validadeIas} ($statusMsg)", isAlert = true)
                                }
                                alertasTox.forEach { m ->
                                    val date = parseDate(m.validadeToxicologico)
                                    val days = date?.let { ChronoUnit.DAYS.between(hoje, it) } ?: 0
                                    val statusMsg = if (days < 0) "VENCIDO há ${-days} dias" else "Vence em $days dias"
                                    ListItemView(title = "${m.graduacao} ${m.nomeDeGuerra}", subtitle = "Toxicológico: ${m.validadeToxicologico} ($statusMsg)", isAlert = true)
                                }
                            }
                        }
                    }
                }

                // DISTRIBUICAO DE CNH
                item {
                    val cnhCounts = uiState.efetivoList.map { it.categoria.trim() }.filter { it.isNotBlank() && it != "-" }
                        .groupingBy { it }.eachCount()
                    
                    val sortedCnh = cnhCounts.entries.sortedByDescending { it.value }
                    val colors = listOf(BrandBlue, BrandGreen, BrandYellow, BrandRed, BrandPurple, BrandTeal, Color.Gray, Color.Magenta, Color.Cyan, Color.DarkGray)
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DriveEta, null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Categorias CNH", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Text("Distribuição de habilitação da tropa", color = textSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 24.dp, start = 26.dp))
                            
                            if (sortedCnh.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Donut Chart
                                    Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                                        DonutChart(
                                            modifier = Modifier.fillMaxSize(),
                                            values = sortedCnh.map { it.value.toFloat() },
                                            colors = colors.take(sortedCnh.size),
                                            borderColor = borderColor
                                        )
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("TOTAL", color = textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("${sortedCnh.sumOf { it.value }}", color = textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    Spacer(Modifier.width(24.dp))
                                    
                                    // Legend
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                        sortedCnh.take(6).forEachIndexed { index, entry ->
                                            LegendItem(entry.key, entry.value, sortedCnh.sumOf { it.value }, colors[index], textPrimary)
                                        }
                                    }
                                }
                            } else {
                                Text("Sem dados de CNH", color = textSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // TOP 10 CURSOS
                item {
                    val cursosCount = mutableMapOf<String, Int>()
                    uiState.efetivoList.forEach { militar ->
                        militar.cursos.forEach { (curso, status) ->
                            if (status.equals("SIM", true) || status.equals("TRUE", true)) {
                                cursosCount[curso] = cursosCount.getOrDefault(curso, 0) + 1
                            }
                        }
                    }
                    val topCursos = cursosCount.entries.sortedByDescending { it.value }.take(10)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.School, null, tint = BrandPurple, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Top 10 Cursos e Especializações", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            
                            if (topCursos.isNotEmpty()) {
                                topCursos.forEachIndexed { index, entry ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                            selectedCurso = entry.key
                                            selectedCursoMilitaries = uiState.efetivoList.filter { 
                                                it.cursos[entry.key]?.equals("SIM", true) == true || it.cursos[entry.key]?.equals("TRUE", true) == true 
                                            }
                                            showCursoModal = true
                                        }
                                    ) {
                                        Text("${index + 1}º", color = BrandPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.width(24.dp))
                                        Text(entry.key, color = textPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                        Text("${entry.value} militares", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                    if (index < topCursos.size - 1) {
                                        HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                                    }
                                }
                            } else {
                                Text("Sem dados de cursos", color = textSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // ANIVERSARIANTES E FOLGAS
                item {
                    val hoje = LocalDate.now()
                    val aniversariantes = uiState.efetivoList.filter { militar ->
                        val parts = militar.aniversario.split("/")
                        parts.size >= 2 && parts[1].toIntOrNull() == hoje.monthValue
                    }.sortedBy { it.aniversario.split("/").firstOrNull()?.toIntOrNull() ?: 0 }
                    
                    if (aniversariantes.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Cake, null, tint = BrandYellow, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Aniversariantes do Mês", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Spacer(Modifier.height(16.dp))
                                aniversariantes.forEach { m ->
                                    ListItemView(title = "${m.graduacao} ${m.nomeDeGuerra}", subtitle = "Dia ${m.aniversario.split("/").firstOrNull()}", isAlert = false)
                                }
                            }
                        }
                    }
                }

                // AFASTAMENTOS ATIVOS E PRÓXIMOS
                item {
                    val hoje = LocalDate.now()
                    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val formatterTwoDigitYear = DateTimeFormatter.ofPattern("dd/MM/yy")
                    fun parseDate(dateStr: String): LocalDate? {
                        if (dateStr.isBlank()) return null
                        val cleaned = dateStr.trim()
                        return try {
                            if (cleaned.length == 8) LocalDate.parse(cleaned, formatterTwoDigitYear)
                            else LocalDate.parse(cleaned, dateFormatter)
                        } catch (e: Exception) { null }
                    }

                    val afastamentosAtivos = uiState.afastamentosList.filter { af ->
                        val inicio = parseDate(af.dataInicio)
                        val fim = parseDate(af.dataTermino)
                        if (inicio != null && fim != null) {
                            !hoje.isBefore(inicio) && !hoje.isAfter(fim)
                        } else false
                    }

                    val proximosAfastamentos = uiState.afastamentosList.filter { af ->
                        val inicio = parseDate(af.dataInicio)
                        if (inicio != null) {
                            inicio.isAfter(hoje) && ChronoUnit.DAYS.between(hoje, inicio) <= 30
                        } else false
                    }

                    if (afastamentosAtivos.isNotEmpty() || proximosAfastamentos.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.MedicalServices, null, tint = BrandRed, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Afastamentos e Dispensas", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Spacer(Modifier.height(16.dp))
                                
                                if (afastamentosAtivos.isNotEmpty()) {
                                    Text("Ativos Hoje:", color = textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                                    afastamentosAtivos.forEach { af ->
                                        ListItemView(title = af.militar, subtitle = "${af.tipoAfastamento} (até ${af.dataTermino})", isAlert = true)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }

                                if (proximosAfastamentos.isNotEmpty()) {
                                    Text("Próximos 30 dias:", color = textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                                    proximosAfastamentos.forEach { af ->
                                        ListItemView(title = af.militar, subtitle = "${af.tipoAfastamento} (${af.dataInicio})", isAlert = false)
                                    }
                                }
                            }
                        }
                    }
                }

                // FOLGAS DO MÊS ATUAL
                item {
                    val hoje = LocalDate.now()
                    val mesAtual = hoje.monthValue
                    val nomeMesAtual = java.time.format.TextStyle.FULL.let { 
                        hoje.month.getDisplayName(it, java.util.Locale("pt", "BR")) 
                    }.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale("pt", "BR")) else it.toString() }

                    fun extrairFolga(folga: com.andrefdias.dailynote.domain.model.FolgaMensal, mes: Int): String {
                        return when (mes) {
                            1 -> folga.janeiro
                            2 -> folga.fevereiro
                            3 -> folga.marco
                            4 -> folga.abril
                            5 -> folga.maio
                            6 -> folga.junho
                            7 -> folga.julho
                            8 -> folga.agosto
                            9 -> folga.setembro
                            10 -> folga.outubro
                            11 -> folga.novembro
                            12 -> folga.dezembro
                            else -> ""
                        }
                    }

                    val folgasFiltradas = uiState.folgasList.filter { 
                        extrairFolga(it, mesAtual).isNotBlank() && extrairFolga(it, mesAtual) != "-" 
                    }.sortedBy { it.nomePadrao }

                    if (folgasFiltradas.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.EventAvailable, null, tint = BrandGreen, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Folgas de $nomeMesAtual", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Spacer(Modifier.height(16.dp))
                                folgasFiltradas.forEach { f ->
                                    val diasFolga = extrairFolga(f, mesAtual)
                                    ListItemView(title = f.nomePadrao, subtitle = "Dias: $diasFolga", isAlert = false)
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(30.dp)) }
            }

            if (showCursoModal) {
                AlertDialog(
                    onDismissRequest = { showCursoModal = false },
                    title = { Text("Militares com $selectedCurso", style = MaterialTheme.typography.titleMedium) },
                    text = {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(selectedCursoMilitaries) { m ->
                                ListItemView(title = "${m.graduacao} ${m.nomeDeGuerra}", subtitle = "RE: ${m.re}")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showCursoModal = false }) { Text("Fechar") }
                    }
                )
            }
        }
    }
}

@Composable
fun KpiCard(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, cardColor: Color, textColor: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Box(modifier = Modifier.size(24.dp).background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(title, color = textColor.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1)
            }
            Text(value, color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ShortcutCard(modifier: Modifier, title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, cardColor: Color, textSecondary: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier.aspectRatio(1.5f).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(title.replace("\\n", "\n"), color = textSecondary, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
fun LegendItem(label: String, count: Int, total: Int, color: Color, textPrimary: Color) {
    val pct = if (total > 0) (count * 100) / total else 0
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, color = textPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1)
        Text("$count", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Text("$pct%", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), textAlign = TextAlign.End)
    }
}

@Composable
fun ListItemView(title: String, subtitle: String, isAlert: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isAlert) FontWeight.Bold else FontWeight.Normal,
            color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
}

@Composable
fun DonutChart(modifier: Modifier = Modifier, values: List<Float>, colors: List<Color>, borderColor: Color) {
    val total = values.sum().takeIf { it > 0 } ?: 1f
    
    Canvas(modifier = modifier) {
        var startAngle = -90f
        val strokeWidth = 30f
        
        if (values.sum() == 0f) {
            drawArc(
                color = borderColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            return@Canvas
        }
        
        values.forEachIndexed { index, value ->
            val sweepAngle = (value / total) * 360f
            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweepAngle
        }
    }
}
