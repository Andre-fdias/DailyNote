package com.andrefdias.dailynote.ui.screens.ocorrencias

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import kotlinx.coroutines.launch
import com.andrefdias.dailynote.util.OcorrenciaPdfGenerator
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.ui.screens.ocorrencias.modules.VeiculosModuleView
import com.andrefdias.dailynote.ui.screens.ocorrencias.modules.EvidenciasModuleView
import com.andrefdias.dailynote.ui.screens.ocorrencias.modules.PessoasModuleView

enum class OccurrenceModule {
    ENDERECO, PESSOAS, VIATURAS, MILITARES, VEICULOS, VITIMAS, APOIOS, HISTORICO, EVIDENCIAS, RESUMO
}

data class ModuleData(
    val type: OccurrenceModule,
    val title: String,
    val icon: ImageVector,
    val subtitle: String,
    val status: ModuleStatus
)

enum class ModuleStatus { CONCLUIDO, PENDENTE, NAO_INICIADO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcorrenciaOpcoesScreen(
    ocorrenciaId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCadastrarViatura: () -> Unit = {},
    onNavigateToCadastrarMilitar: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: OcorrenciaOpcoesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var activeModule by remember { mutableStateOf<OccurrenceModule?>(null) }
    
    val ocorrencia by viewModel.ocorrencia.collectAsState()
    val viaturasDisponiveis by viewModel.viaturasDisponiveis.collectAsState()
    val militaresDisponiveis by viewModel.militaresDisponiveis.collectAsState()

    LaunchedEffect(ocorrenciaId) {
        viewModel.loadOcorrencia(ocorrenciaId)
        viewModel.loadVeiculos(ocorrenciaId)
        viewModel.loadVitimas(ocorrenciaId)
    }
    
    val veiculos by viewModel.veiculos.collectAsState()
    val vitimas by viewModel.vitimas.collectAsState()

    Scaffold(
        topBar = {
            if (activeModule != OccurrenceModule.RESUMO) {
                TopAppBar(
                title = {
                    Column {
                        Text(
                            if (activeModule == null) "Ocorrência" else moduleName(activeModule!!),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (activeModule == null && ocorrencia != null) {
                            Text(
                                "Talão: ${ocorrencia!!.talao}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (activeModule != null) activeModule = null
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (activeModule == null) {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Mais opções")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Gerar PDF") },
                                onClick = {
                                    expanded = false
                                    ocorrencia?.let { oc ->
                                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                            val uri = OcorrenciaPdfGenerator.generatePdf(context, oc, veiculos, vitimas)
                                            if (uri != null) {
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "application/pdf")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                try {
                                                    context.startActivity(Intent.createChooser(intent, "Visualizar PDF"))
                                                } catch(e: Exception) { e.printStackTrace() }
                                            }
                                        }
                                    }
                                },
                                leadingIcon = { Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Compartilhar") },
                                onClick = {
                                    expanded = false
                                    ocorrencia?.let { oc ->
                                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                            val uri = OcorrenciaPdfGenerator.generatePdf(context, oc, veiculos, vitimas)
                                            if (uri != null) {
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "application/pdf"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                try {
                                                    context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório"))
                                                } catch(e: Exception) { e.printStackTrace() }
                                            }
                                        }
                                    }
                                },
                                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
                )
            }
        },
        bottomBar = {
            if (activeModule == null) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = {
                            viewModel.concluirOcorrencia()
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "CONCLUIR OCORRÊNCIA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) { innerPadding ->
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (activeModule == null) {
                ModularDashboardView(
                    ocorrenciaId = ocorrenciaId,
                    ocorrencia = ocorrencia,
                    hasVeiculos = veiculos.isNotEmpty(),
                    hasVitimas = vitimas.isNotEmpty(),
                    onModuleSelected = { activeModule = it }
                )
            } else {
                if (ocorrencia != null) {
                    when (activeModule) {
                        OccurrenceModule.ENDERECO -> EnderecoModuleView(
                            ocorrencia = ocorrencia!!,
                            onSave = { talao, natureza, rua, numero, bairro, cidade ->
                                viewModel.updateOcorrenciaBase(talao, natureza, rua, numero, bairro, cidade)
                                activeModule = null
                            },
                            onCancel = { activeModule = null }
                        )
                        OccurrenceModule.PESSOAS -> PessoasModuleView(
                            ocorrencia = ocorrencia!!,
                            onSave = { pessoasJson ->
                                viewModel.updatePessoas(pessoasJson)
                            },
                            onSaveGlobalPhotos = { json ->
                                viewModel.updateFotos(json)
                            },
                            onCancel = { activeModule = null }
                        )
                        OccurrenceModule.VEICULOS -> VeiculosModuleView(
                            ocorrenciaId = ocorrenciaId,
                            viewModel = viewModel,
                            onCancel = { activeModule = null }
                        )
                        OccurrenceModule.VITIMAS -> com.andrefdias.dailynote.ui.screens.ocorrencias.modules.VitimasModuleView(
                            ocorrencia = ocorrencia!!,
                            viewModel = viewModel,
                            onCancel = { activeModule = null }
                        )
                        OccurrenceModule.VIATURAS -> ViaturasModuleView(
                            ocorrencia = ocorrencia!!,
                            viaturasDisponiveis = viaturasDisponiveis,
                            onSave = { vtrs ->
                                viewModel.updateViaturas(vtrs)
                                activeModule = null
                            },
                            onCancel = { activeModule = null },
                            onNavigateToCadastrarViatura = onNavigateToCadastrarViatura
                        )
                        OccurrenceModule.MILITARES -> MilitaresModuleView(
                            ocorrencia = ocorrencia!!,
                            militaresDisponiveis = militaresDisponiveis,
                            onSave = { json ->
                                viewModel.updateMilitares(json)
                                activeModule = null
                            },
                            onCancel = { activeModule = null },
                            onNavigateToCadastrarMilitar = onNavigateToCadastrarMilitar
                        )
                        OccurrenceModule.HISTORICO -> HistoricoModuleView(
                            ocorrencia = ocorrencia!!,
                            onSave = { historico ->
                                viewModel.updateHistorico(historico)
                                activeModule = null
                            },
                            onCancel = { activeModule = null }
                        )
                        OccurrenceModule.APOIOS -> ApoiosModuleView(
                            ocorrencia = ocorrencia!!,
                            onSave = { apoiosJson ->
                                viewModel.updateApoios(apoiosJson)
                                activeModule = null
                            },
                            onCancel = { activeModule = null }
                        )
                        OccurrenceModule.EVIDENCIAS -> EvidenciasModuleView(
                            ocorrencia = ocorrencia!!,
                            onSave = { fotosJson ->
                                viewModel.updateFotos(fotosJson)
                                activeModule = null
                            },
                            onCancel = { activeModule = null }
                        )
                        OccurrenceModule.RESUMO -> com.andrefdias.dailynote.ui.screens.ocorrencias.modules.ResumoModuleView(
                            ocorrencia = ocorrencia!!,
                            veiculos = veiculos,
                            vitimas = vitimas,
                            onCancel = { activeModule = null }
                        )
                        else -> EmptyModuleView(moduleName(activeModule!!))
                    }
                }
            }
        }
    }
}

fun moduleName(module: OccurrenceModule): String = when (module) {
    OccurrenceModule.ENDERECO -> "Endereço"
    OccurrenceModule.PESSOAS -> "Pessoas"
    OccurrenceModule.VIATURAS -> "Viaturas"
    OccurrenceModule.MILITARES -> "Militares"
    OccurrenceModule.VEICULOS -> "Veículos"
    OccurrenceModule.VITIMAS -> "Vítimas"
    OccurrenceModule.APOIOS -> "Apoios"
    OccurrenceModule.HISTORICO -> "Histórico"
    OccurrenceModule.EVIDENCIAS -> "Evidências"
    OccurrenceModule.RESUMO -> "Resumo"
}

@Composable
fun ModularDashboardView(
    ocorrenciaId: String,
    ocorrencia: com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia?,
    hasVeiculos: Boolean = false,
    hasVitimas: Boolean = false,
    onModuleSelected: (OccurrenceModule) -> Unit
) {
    val scrollState = rememberScrollState()

    val addressStatus = if (ocorrencia?.rua != null && ocorrencia.rua.isNotBlank()) ModuleStatus.CONCLUIDO else ModuleStatus.NAO_INICIADO
    val pessoasStatus = if (ocorrencia?.pessoasJson != null && ocorrencia.pessoasJson != "[]") ModuleStatus.CONCLUIDO else ModuleStatus.NAO_INICIADO
    val viaturasStatus = if (ocorrencia?.viatura != null && ocorrencia.viatura.isNotBlank() && ocorrencia.viatura != "[]") ModuleStatus.CONCLUIDO else ModuleStatus.NAO_INICIADO
    val militaresStatus = if (ocorrencia?.guarnicaoJson != null && ocorrencia.guarnicaoJson != "[]") ModuleStatus.CONCLUIDO else ModuleStatus.NAO_INICIADO
    val historicoStatus = if (ocorrencia?.historico != null && ocorrencia.historico.isNotBlank()) ModuleStatus.CONCLUIDO else ModuleStatus.PENDENTE
    val apoiosStatus = if (ocorrencia?.apoios != null && ocorrencia.apoios != "[]") ModuleStatus.CONCLUIDO else ModuleStatus.NAO_INICIADO
    val veiculosStatus = if (hasVeiculos) ModuleStatus.CONCLUIDO else ModuleStatus.NAO_INICIADO
    val evidenciasStatus = if (ocorrencia?.fotosUrisJson != null && ocorrencia.fotosUrisJson != "[]" && ocorrencia.fotosUrisJson != "") ModuleStatus.CONCLUIDO else ModuleStatus.NAO_INICIADO
    val vitimasStatus = if (hasVitimas) ModuleStatus.CONCLUIDO else ModuleStatus.NAO_INICIADO

    val modules = listOf(
        ModuleData(OccurrenceModule.ENDERECO, "Endereço", Icons.Filled.LocationOn, "Local da ocorrência", addressStatus),
        ModuleData(OccurrenceModule.PESSOAS, "Pessoas", Icons.Filled.Person, "Envolvidos", pessoasStatus),
        ModuleData(OccurrenceModule.VEICULOS, "Veículos", Icons.Filled.DirectionsCar, "Veículos envolvidos", veiculosStatus),
        ModuleData(OccurrenceModule.VITIMAS, "Vítimas", Icons.Filled.MedicalServices, "Registro de vítimas", vitimasStatus),
        ModuleData(OccurrenceModule.VIATURAS, "Viaturas", Icons.Filled.LocalFireDepartment, "Recursos BM", viaturasStatus),
        ModuleData(OccurrenceModule.MILITARES, "Militares", Icons.Filled.Group, "Guarnição", militaresStatus),
        ModuleData(OccurrenceModule.EVIDENCIAS, "Evidências", Icons.Filled.CameraAlt, "Fotos e registros", evidenciasStatus),
        ModuleData(OccurrenceModule.APOIOS, "Apoios", Icons.Filled.Handshake, "Outros órgãos", apoiosStatus),
        ModuleData(OccurrenceModule.HISTORICO, "Histórico", Icons.Filled.Description, "Relato da ocorrência", historicoStatus),
        ModuleData(OccurrenceModule.RESUMO, "Resumo", Icons.Filled.Summarize, "Dados consolidados", ModuleStatus.NAO_INICIADO)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Summarize,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Resumo da Ocorrência",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                if (ocorrencia != null) {
                    val (equipeLabel, equipeColor) = com.andrefdias.dailynote.util.EquipeUtils.parseEquipeInfo(ocorrencia.equipe)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Prontidão Ativa", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(equipeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(equipeLabel, color = equipeColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    SummaryRow("Talão", ocorrencia.talao.ifEmpty { "Sem Talão" })
                    SummaryRow("Data / Hora", "${ocorrencia.data} às ${ocorrencia.hora}")
                } else {
                    SummaryRow("Carregando...", "")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Module status counters
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val concluidos = modules.count { it.status == ModuleStatus.CONCLUIDO && it.type != OccurrenceModule.RESUMO }
                    val pendentes = modules.count { it.status == ModuleStatus.PENDENTE && it.type != OccurrenceModule.RESUMO }
                    val naoIniciados = modules.count { it.status == ModuleStatus.NAO_INICIADO && it.type != OccurrenceModule.RESUMO }

                    StatusBadge("$concluidos Concluído(s)", Color(0xFF2E7D32))
                    StatusBadge("$pendentes Pendente(s)", Color(0xFFE65100))
                    StatusBadge("$naoIniciados Não iniciado(s)", MaterialTheme.colorScheme.outline)
                }
            }
        }

        // Checklist Card
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Checklist Operacional", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Talão ✅  •  Endereço ✅  •  Histórico ⬜  •  Viaturas ⬜",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Grid title
        Text(
            "Módulos da Ocorrência",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // 2-column Grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (i in modules.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModuleCard(
                        module = modules[i],
                        modifier = Modifier.weight(1f),
                        onClick = { onModuleSelected(modules[i].type) }
                    )
                    if (i + 1 < modules.size) {
                        ModuleCard(
                            module = modules[i + 1],
                            modifier = Modifier.weight(1f),
                            onClick = { onModuleSelected(modules[i + 1].type) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ModuleCard(module: ModuleData, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val statusColor = when (module.status) {
        ModuleStatus.CONCLUIDO -> Color(0xFF2E7D32)
        ModuleStatus.PENDENTE -> Color(0xFFE65100)
        ModuleStatus.NAO_INICIADO -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    val statusLabel = when (module.status) {
        ModuleStatus.CONCLUIDO -> "CONCLUÍDO"
        ModuleStatus.PENDENTE -> "PENDENTE"
        ModuleStatus.NAO_INICIADO -> "NÃO INICIADO"
    }
    val iconTint = when (module.type) {
        OccurrenceModule.VIATURAS, OccurrenceModule.VEICULOS -> Color(0xFFC62828)
        OccurrenceModule.VITIMAS -> Color(0xFF1B5E20)
        OccurrenceModule.HISTORICO -> Color(0xFFE65100)
        OccurrenceModule.EVIDENCIAS -> Color(0xFF4527A0)
        OccurrenceModule.ENDERECO -> Color(0xFF1565C0)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(module.icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                // Status Badge Icon
                val badgeIcon = when (module.status) {
                    ModuleStatus.CONCLUIDO -> Icons.Filled.CheckCircle
                    ModuleStatus.PENDENTE -> Icons.Filled.Warning
                    ModuleStatus.NAO_INICIADO -> Icons.Filled.RadioButtonUnchecked
                }
                Icon(badgeIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
            }

            Column {
                Text(module.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(module.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    statusLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun EmptyModuleView(moduleName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Construction, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Módulo: $moduleName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Em desenvolvimento", color = MaterialTheme.colorScheme.outline)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnderecoModuleView(
    ocorrencia: com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia,
    onSave: (talao: String, natureza: String, rua: String, numero: String, bairro: String, cidade: String) -> Unit,
    onCancel: () -> Unit
) {
    var talao by remember { mutableStateOf(ocorrencia.talao) }
    var natureza by remember { mutableStateOf(ocorrencia.natureza) }
    var rua by remember { mutableStateOf(ocorrencia.rua ?: "") }
    var numero by remember { mutableStateOf(ocorrencia.numero ?: "") }
    var bairro by remember { mutableStateOf(ocorrencia.bairro ?: "") }
    var cidade by remember { mutableStateOf(ocorrencia.cidade ?: "") }
    
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Classificação Card
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Category, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Classificação", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                OutlinedTextField(
                    value = talao,
                    onValueChange = { talao = it },
                    label = { Text("Número do Talão") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null) },
                    singleLine = true
                )

                var expandedNatureza by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedNatureza,
                    onExpandedChange = { expandedNatureza = !expandedNatureza }
                ) {
                    OutlinedTextField(
                        value = natureza,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Natureza da Ocorrência") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNatureza) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedNatureza,
                        onDismissRequest = { expandedNatureza = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        com.andrefdias.dailynote.util.Naturezas.listaCompleta.forEach { nat ->
                            DropdownMenuItem(
                                text = { Text(nat) },
                                onClick = {
                                    natureza = nat
                                    expandedNatureza = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Localização Card
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Localização", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = {
                        val query = listOfNotNull(
                            rua.takeIf { it.isNotBlank() },
                            numero.takeIf { it.isNotBlank() },
                            bairro.takeIf { it.isNotBlank() },
                            cidade.takeIf { it.isNotBlank() }
                        ).joinToString(", ")
                        if (query.isNotBlank()) {
                            val uri = android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(query)}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }) {
                        Icon(Icons.Filled.Map, contentDescription = "Abrir no Mapa", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                OutlinedTextField(
                    value = rua,
                    onValueChange = { rua = it },
                    label = { Text("Rua/Avenida") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Filled.Signpost, contentDescription = null) },
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = numero,
                        onValueChange = { numero = it },
                        label = { Text("NÂº") },
                        modifier = Modifier.weight(0.4f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bairro,
                        onValueChange = { bairro = it },
                        label = { Text("Bairro") },
                        modifier = Modifier.weight(0.6f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = cidade,
                    onValueChange = { cidade = it },
                    label = { Text("Cidade") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Filled.LocationCity, contentDescription = null) },
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = { onSave(talao, natureza, rua, numero, bairro, cidade) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar")
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViaturasModuleView(
    ocorrencia: com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia,
    viaturasDisponiveis: List<com.andrefdias.dailynote.domain.model.Viatura>,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    onNavigateToCadastrarViatura: () -> Unit
) {
    data class ViaturaEnvolvida(
        val prefixo: String,
        val kmSaida: String,
        val kmQuartel: String,
        val posto: String,
        val unidade: String
    )

    val viaturasList = remember { 
        mutableStateListOf<ViaturaEnvolvida>().apply {
            if (ocorrencia.viatura.isNotBlank()) {
                try {
                    if (ocorrencia.viatura.trim().startsWith("[")) {
                        val type = object : com.google.gson.reflect.TypeToken<List<ViaturaEnvolvida>>() {}.type
                        val parsed: List<ViaturaEnvolvida> = com.google.gson.Gson().fromJson(ocorrencia.viatura, type)
                        addAll(parsed)
                    } else {
                        val parts = ocorrencia.viatura.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        addAll(parts.map { ViaturaEnvolvida(it, "", "", "", "") })
                    }
                } catch(e: Exception) { 
                    val parts = ocorrencia.viatura.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    addAll(parts.map { ViaturaEnvolvida(it, "", "", "", "") })
                }
            }
        }
    }
    
    var expanded by remember { mutableStateOf(false) }
    var selectedViatura by remember { mutableStateOf("") }
    
    var kmSaida by remember { mutableStateOf("") }
    var kmQuartel by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Viaturas Envolvidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedViatura,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Selecione Viatura") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                if (viaturasDisponiveis.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Nenhuma viatura cadastrada.") },
                                        onClick = { expanded = false }
                                    )
                                } else {
                                    viaturasDisponiveis.forEach { vtr ->
                                        DropdownMenuItem(
                                            text = { Text(vtr.prefixo) },
                                            onClick = {
                                                selectedViatura = vtr.prefixo
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = kmSaida,
                        onValueChange = { kmSaida = it },
                        label = { Text("Km Saída") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = kmQuartel,
                        onValueChange = { kmQuartel = it },
                        label = { Text("Km Quartel") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
                
                Button(
                    onClick = {
                        if (selectedViatura.isNotBlank()) {
                            val vtr = viaturasDisponiveis.find { it.prefixo == selectedViatura }
                            viaturasList.add(
                                ViaturaEnvolvida(
                                    prefixo = selectedViatura,
                                    kmSaida = kmSaida,
                                    kmQuartel = kmQuartel,
                                    posto = vtr?.posto ?: "",
                                    unidade = vtr?.unidade ?: ""
                                )
                            )
                            selectedViatura = ""
                            kmSaida = ""
                            kmQuartel = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedViatura.isNotBlank()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar Viatura")
                }
                
                TextButton(
                    onClick = onNavigateToCadastrarViatura,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cadastrar Nova Viatura", style = MaterialTheme.typography.labelLarge)
                }

                if (viaturasList.isNotEmpty()) {
                    Text("Viaturas Cadastradas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        viaturasList.forEach { vtr ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(vtr.prefixo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("${vtr.unidade} • ${vtr.posto}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Km Saída: ${if(vtr.kmSaida.isEmpty()) "N/I" else vtr.kmSaida} • Km Quartel: ${if(vtr.kmQuartel.isEmpty()) "N/I" else vtr.kmQuartel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { viaturasList.remove(vtr) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text("Nenhuma viatura cadastrada.", color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = { onSave(com.google.gson.Gson().toJson(viaturasList.toList())) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilitaresModuleView(
    ocorrencia: com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia,
    militaresDisponiveis: List<com.andrefdias.dailynote.domain.model.Militar>,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    onNavigateToCadastrarMilitar: () -> Unit
) {
    val initialList = try {
        val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        com.google.gson.Gson().fromJson<List<String>>(ocorrencia.guarnicaoJson, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    val militaresList = remember { mutableStateListOf(*initialList.toTypedArray()) }
    val viaturasOcorrencia = try {
        if (ocorrencia.viatura.trim().startsWith("[")) {
            val type = object : com.google.gson.reflect.TypeToken<List<Map<String, String>>>() {}.type
            val parsed: List<Map<String, String>> = com.google.gson.Gson().fromJson(ocorrencia.viatura, type)
            parsed.mapNotNull { it["prefixo"] }.filter { it.isNotBlank() }
        } else {
            ocorrencia.viatura.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    } catch (e: Exception) {
        ocorrencia.viatura.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    
    var expandedViatura by remember { mutableStateOf(false) }
    var selectedViatura by remember { mutableStateOf("") }
    
    var expandedMilitar by remember { mutableStateOf(false) }
    var selectedMilitar by remember { mutableStateOf("") }
    
    val funcoes = listOf("Comandante", "Motorista", "Auxiliar", "Resgatista", "Telegrafista")
    var expandedFuncao by remember { mutableStateOf(false) }
    var selectedFuncao by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Group, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Efetivo (Militares)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Dropdown Viatura
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedViatura,
                            onExpandedChange = { expandedViatura = !expandedViatura }
                        ) {
                            OutlinedTextField(
                                value = selectedViatura,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Selecione a Viatura") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedViatura) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedViatura,
                                onDismissRequest = { expandedViatura = false }
                            ) {
                                if (viaturasOcorrencia.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Nenhuma viatura na ocorrência.") },
                                        onClick = { expandedViatura = false }
                                    )
                                } else {
                                    viaturasOcorrencia.forEach { vtr ->
                                        DropdownMenuItem(
                                            text = { Text(vtr) },
                                            onClick = {
                                                selectedViatura = vtr
                                                expandedViatura = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Dropdown Função
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedFuncao,
                            onExpandedChange = { expandedFuncao = !expandedFuncao }
                        ) {
                            OutlinedTextField(
                                value = selectedFuncao,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Selecione a Função") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFuncao) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedFuncao,
                                onDismissRequest = { expandedFuncao = false }
                            ) {
                                funcoes.forEach { func ->
                                    DropdownMenuItem(
                                        text = { Text(func) },
                                        onClick = {
                                            selectedFuncao = func
                                            expandedFuncao = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Dropdown Militar
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = expandedMilitar,
                                onExpandedChange = { expandedMilitar = !expandedMilitar }
                            ) {
                                OutlinedTextField(
                                    value = selectedMilitar,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Selecione Militar") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMilitar) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedMilitar,
                                    onDismissRequest = { expandedMilitar = false }
                                ) {
                                    if (militaresDisponiveis.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Nenhum militar cadastrado.") },
                                            onClick = { expandedMilitar = false }
                                        )
                                    } else {
                                        militaresDisponiveis.forEach { militar ->
                                            val displayStr = "${militar.graduacao} ${militar.nomeGuerra} - RE: ${militar.re} - ${militar.nomeCompleto}"
                                            DropdownMenuItem(
                                                text = { Text(displayStr) },
                                                onClick = {
                                                    selectedMilitar = displayStr
                                                    expandedMilitar = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        IconButton(
                            onClick = {
                                if (selectedViatura.isNotBlank() && selectedMilitar.isNotBlank() && selectedFuncao.isNotBlank()) {
                                    val formattedStr = "$selectedViatura - $selectedFuncao - $selectedMilitar"
                                    if (!militaresList.contains(formattedStr)) {
                                        militaresList.add(formattedStr)
                                    }
                                    selectedMilitar = ""
                                    selectedFuncao = ""
                                }
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Adicionar", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                
                TextButton(
                    onClick = onNavigateToCadastrarMilitar,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cadastrar Novo Militar", style = MaterialTheme.typography.labelLarge)
                }

                if (militaresList.isNotEmpty()) {
                    Text("Guarnição Atual", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    val grouped = militaresList.groupBy { if (it.contains(" - ")) it.substringBefore(" - ") else "Sem Viatura" }
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        grouped.forEach { (viatura, list) ->
                            val viaturaColor = when {
                                viatura.startsWith("AT", ignoreCase = true) -> Color(0xFFE53935)
                                viatura.startsWith("UR", ignoreCase = true) -> Color(0xFF1E88E5)
                                viatura.startsWith("ABS", ignoreCase = true) -> Color(0xFFFB8C00)
                                viatura.startsWith("COM", ignoreCase = true) -> Color(0xFF8E24AA)
                                else -> Color(0xFF757575)
                            }
                            val viaturaDrawable = when {
                                viatura.startsWith("AT", ignoreCase = true) -> com.andrefdias.dailynote.R.drawable.viatura_at
                                viatura.startsWith("UR", ignoreCase = true) -> com.andrefdias.dailynote.R.drawable.viatura_ur
                                viatura.startsWith("ABS", ignoreCase = true) -> com.andrefdias.dailynote.R.drawable.viatura_abs
                                viatura.startsWith("COM", ignoreCase = true) -> com.andrefdias.dailynote.R.drawable.viatura_com
                                else -> com.andrefdias.dailynote.R.drawable.viatura_ur
                            }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(6.dp)
                                            .background(viaturaColor)
                                    )
                                    Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .background(viaturaColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    androidx.compose.foundation.Image(
                                                        painter = androidx.compose.ui.res.painterResource(id = viaturaDrawable),
                                                        contentDescription = "Viatura",
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = viatura, 
                                                    style = MaterialTheme.typography.titleMedium, 
                                                    fontWeight = FontWeight.ExtraBold, 
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            list.forEach { item ->
                                                val displayItem = if (item.contains(" - ")) item.substringAfter(" - ") else item
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f), RoundedCornerShape(8.dp)).padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(displayItem, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    IconButton(onClick = { militaresList.remove(item) }, modifier = Modifier.size(28.dp)) {
                                                        Icon(Icons.Filled.Close, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text("Nenhum militar cadastrado.", color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = { onSave(com.google.gson.Gson().toJson(militaresList.toList())) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar")
            }
        }
    }
}


data class PessoaEnvolvida(
    val id: String? = java.util.UUID.randomUUID().toString(),
    val nome: String = "",
    val tipoDocumento: String? = "RG",
    val documento: String = "", 
    val rg: String = "",
    val cpf: String = "",
    val nascimento: String = "",
    val idade: String = "",
    val mae: String = "",
    val pai: String = "",
    val naturalidade: String = "",
    val orgaoExpedidor: String = "",
    val dataExpedicao: String = "",
    val uf: String = "",
    val sexo: String = "",
    val registro: String = "",
    val categoria: String = "",
    val primeiraHabilitacao: String = "",
    val validade: String = "",
    val papel: String = "",
    val telefone: String = "",
    val email: String = "",
    val fotosUrisJson: String = "[]"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoModuleView(
    ocorrencia: com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var historico by remember { mutableStateOf(ocorrencia.historico ?: "") }
    val maxChar = 1000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Histórico da Ocorrência", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                Text(
                    "Descreva com detalhes a ocorrência. Você pode usar a digitação por voz do seu teclado para facilitar o registro.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = historico,
                    onValueChange = {
                        if (it.length <= maxChar) historico = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    placeholder = { Text("Digite ou dite o histórico aqui...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences
                    ),
                    maxLines = 15
                )

                Text(
                    text = "${historico.length} / $maxChar",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (historico.length >= maxChar) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = { onSave(historico) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar")
            }
        }
    }
}


data class ApoioEnvolvido(
    val instituicao: String,
    val prefixo: String,
    val responsavel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApoiosModuleView(
    ocorrencia: com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    val initialList = try {
        val type = object : com.google.gson.reflect.TypeToken<List<ApoioEnvolvido>>() {}.type
        com.google.gson.Gson().fromJson<List<ApoioEnvolvido>>(ocorrencia.apoios, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    val apoiosList = remember { mutableStateListOf(*initialList.toTypedArray()) }
    
    var expandedInstituicao by remember { mutableStateOf(false) }
    var selectedInstituicao by remember { mutableStateOf("") }
    val instituicoes = listOf("Polícia Militar", "SAMU", "Defesa Civil", "Polícia Civil", "GCM", "Concessionária Elétrica", "Concessionária de Água", "DER", "Outros")
    
    var prefixo by remember { mutableStateOf("") }
    var responsavel by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Adicionar Apoio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Dropdown de Instituição
                ExposedDropdownMenuBox(
                    expanded = expandedInstituicao,
                    onExpandedChange = { expandedInstituicao = it }
                ) {
                    OutlinedTextField(
                        value = selectedInstituicao,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Instituição") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInstituicao) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedInstituicao,
                        onDismissRequest = { expandedInstituicao = false }
                    ) {
                        instituicoes.forEach { inst ->
                            DropdownMenuItem(
                                text = { Text(inst) },
                                onClick = {
                                    selectedInstituicao = inst
                                    expandedInstituicao = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = prefixo,
                    onValueChange = { prefixo = it },
                    label = { Text("Prefixo / Identificação") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = responsavel,
                    onValueChange = { responsavel = it },
                    label = { Text("Responsável / Comandante") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Button(
                    onClick = {
                        if (selectedInstituicao.isNotBlank()) {
                            apoiosList.add(ApoioEnvolvido(selectedInstituicao, prefixo, responsavel))
                            selectedInstituicao = ""
                            prefixo = ""
                            responsavel = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedInstituicao.isNotBlank()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Adicionar Apoio")
                }
            }
        }
        
        if (apoiosList.isNotEmpty()) {
            Text("Apoios Presentes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                apoiosList.forEach { apoio ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(apoio.instituicao, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                if (apoio.prefixo.isNotBlank()) {
                                    Text("Prefixo: ${apoio.prefixo}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (apoio.responsavel.isNotBlank()) {
                                    Text("Resp: ${apoio.responsavel}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = { apoiosList.remove(apoio) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = { onSave(com.google.gson.Gson().toJson(apoiosList.toList())) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar")
            }
        }

    }
}





