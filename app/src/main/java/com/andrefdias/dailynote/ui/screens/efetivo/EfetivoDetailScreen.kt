package com.andrefdias.dailynote.ui.screens.efetivo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.domain.model.EfetivoMilitar
import com.andrefdias.dailynote.domain.model.FolgaMensal
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector

data class InfoItem(
    val label: String, 
    val value: String, 
    val icon: ImageVector? = null,
    val onClick: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EfetivoDetailScreen(
    militarId: String,
    onNavigateBack: () -> Unit,
    viewModel: EfetivoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val militar = uiState.efetivoList.find { it.id == militarId }
    val folgaMensal = uiState.folgasList.find { it.re == militar?.re }
    val afastamentosDoMilitar = uiState.afastamentosList.filter { 
        militar != null && (
            (militar.re.isNotBlank() && it.militar.contains(militar.re, ignoreCase = true)) ||
            (militar.nomePadrao.isNotBlank() && it.militar.contains(militar.nomePadrao, ignoreCase = true)) ||
            (militar.nomeDeGuerra.isNotBlank() && it.militar.contains(militar.nomeDeGuerra, ignoreCase = true)) ||
            (militar.nomeCompleto.isNotBlank() && it.militar.contains(militar.nomeCompleto, ignoreCase = true))
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Militar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (militar == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Dados não encontrados.")
            }
            return@Scaffold
        }

        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderSection(militar)
            InfoSection(title = "Dados Pessoais", items = listOf(
                InfoItem("Nome Completo", militar.nomeCompleto),
                InfoItem("Nome Padrão", militar.nomePadrao),
                InfoItem("CPF", militar.cpf),
                InfoItem("Aniversário", militar.aniversario),
                InfoItem(
                    label = "Endereço", 
                    value = militar.endereco,
                    icon = Icons.Default.LocationOn,
                    onClick = {
                        val uri = Uri.parse("geo:0,0?q=${Uri.encode(militar.endereco)}")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }
                ),
                InfoItem(
                    label = "Contato", 
                    value = militar.contato,
                    icon = Icons.Default.Phone,
                    onClick = {
                        val number = militar.contato.replace(Regex("\\D"), "")
                        if (number.isNotBlank()) {
                            val uri = Uri.parse("https://wa.me/55$number")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                    }
                )
            ))
            InfoSection(title = "Dados Profissionais", items = listOf(
                InfoItem(
                    label = "Email Funcional", 
                    value = militar.emailFuncional,
                    icon = Icons.Default.Email,
                    onClick = {
                        if (militar.emailFuncional.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${militar.emailFuncional}"))
                            context.startActivity(intent)
                        }
                    }
                ),
                InfoItem(
                    label = "Email Particular", 
                    value = militar.emailParticular,
                    icon = Icons.Default.Email,
                    onClick = {
                        if (militar.emailParticular.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${militar.emailParticular}"))
                            context.startActivity(intent)
                        }
                    }
                ),
                InfoItem("Admissão", militar.admissao),
                InfoItem("Aniversário de Admissão", calculateAniversarioAdmissao(militar.admissao)),
                InfoItem("Última Promoção", militar.ultimaPromocao),
                InfoItem("EB", militar.eb)
            ))
            InfoSection(title = "Documentação Militar e CNH", items = listOf(
                InfoItem("Categoria CNH", militar.categoria),
                InfoItem("Número CNH", militar.numeroCnh),
                InfoItem("Validade CNH", militar.validadeCnh),
                InfoItem("Validade Toxicológico", militar.validadeToxicologico),
                InfoItem("Validade IAS", militar.validadeIas),
                InfoItem("Último EAP", militar.ultimoEap),
                InfoItem("Turma", militar.turma2026),
                InfoItem("Voucher", militar.voucher)
            ))
            
            val mvmStatusList = mutableListOf(
                InfoItem("MVM", militar.mvm),
                InfoItem("Status MVM", militar.statusMvm)
            )
            if (militar.grau1.isNotBlank()) mvmStatusList.add(InfoItem("1º Grau", militar.grau1))
            if (militar.grau2.isNotBlank()) mvmStatusList.add(InfoItem("2º Grau", militar.grau2))
            if (militar.grau3.isNotBlank()) mvmStatusList.add(InfoItem("3º Grau", militar.grau3))
            if (militar.grau4.isNotBlank()) mvmStatusList.add(InfoItem("4º Grau", militar.grau4))
            if (militar.grau5.isNotBlank()) mvmStatusList.add(InfoItem("5º Grau", militar.grau5))
            mvmStatusList.add(InfoItem("Observação", militar.observacao))

            InfoSection(title = "MVM e Status", items = mvmStatusList)
            
            // Férias
            // Férias
            InfoSection(title = "Férias", items = listOf(
                InfoItem("1ª Quinzena", militar.ferias1Quinzena),
                InfoItem("2ª Quinzena", militar.ferias2Quinzena)
            ))

            // Folgas Mensais
            if (folgaMensal != null) {
                InfoSection(title = "Folgas Mensais", items = listOf(
                    InfoItem("Janeiro", folgaMensal.janeiro),
                    InfoItem("Fevereiro", folgaMensal.fevereiro),
                    InfoItem("Março", folgaMensal.marco),
                    InfoItem("Abril", folgaMensal.abril),
                    InfoItem("Maio", folgaMensal.maio),
                    InfoItem("Junho", folgaMensal.junho),
                    InfoItem("Julho", folgaMensal.julho),
                    InfoItem("Agosto", folgaMensal.agosto),
                    InfoItem("Setembro", folgaMensal.setembro),
                    InfoItem("Outubro", folgaMensal.outubro),
                    InfoItem("Novembro", folgaMensal.novembro),
                    InfoItem("Dezembro", folgaMensal.dezembro)
                ))
            }

            // Afastamentos
            if (afastamentosDoMilitar.isNotEmpty()) {
                val itemsAfastamentos = afastamentosDoMilitar.flatMap { af ->
                    listOf(
                        InfoItem("Tipo", af.tipoAfastamento),
                        InfoItem("Período", "${af.dataInicio} a ${af.dataTermino} (${af.diasAfastamento} dias)"),
                        InfoItem("Obs", af.observacoes)
                    )
                }
                InfoSection(title = "Afastamentos", items = itemsAfastamentos)
            }

            // Cursos
            val validCursos = militar.cursos.filter { it.value.equals("TRUE", ignoreCase = true) || it.value.equals("SIM", ignoreCase = true) }
            if (validCursos.isNotEmpty()) {
                InfoSection(title = "Cursos e Especializações", items = validCursos.map { InfoItem(it.key, "Sim") })
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderSection(militar: EfetivoMilitar) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${militar.graduacao} ${militar.nomeDeGuerra}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "RE: ${militar.re}-${militar.digito}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(militar.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                if (militar.prontidao.equals("SIM", true)) {
                    Badge(containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)) {
                        Text("Prontidão", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoSection(title: String, items: List<InfoItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { item ->
                if (item.value.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = item.onClick != null) { item.onClick?.invoke() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            modifier = Modifier.weight(1.5f),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (item.onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                            if (item.icon != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 0.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                }
            }
        }
    }
}

private fun calculateAniversarioAdmissao(admissaoStr: String): String {
    if (admissaoStr.isBlank() || admissaoStr == "-") return "-"
    try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        // fallback to two digit year if needed
        val cleaned = admissaoStr.trim()
        val date = if (cleaned.length == 8) {
            java.time.LocalDate.parse(cleaned, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"))
        } else {
            java.time.LocalDate.parse(cleaned, formatter)
        }
        val hoje = java.time.LocalDate.now()
        val anos = java.time.temporal.ChronoUnit.YEARS.between(date, hoje)
        
        val mesNome = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("pt", "BR"))
        val nextAnniversary = date.withYear(hoje.year).let {
            if (it.isBefore(hoje) || it.isEqual(hoje)) it.plusYears(1) else it
        }
        val ageOnNext = anos + if (date.withYear(hoje.year).isBefore(hoje) || date.withYear(hoje.year).isEqual(hoje)) 1 else 0
        
        return "Dia ${date.dayOfMonth} de ${mesNome.replaceFirstChar { it.uppercase() }} ($ageOnNext anos)"
    } catch (e: Exception) {
        return admissaoStr
    }
}
