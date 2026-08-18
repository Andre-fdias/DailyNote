package com.andrefdias.dailynote.ui.screens.resumo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.domain.model.EquipeViatura
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumoOperacionalScreen(
    viewModel: ResumoOperacionalViewModel = hiltViewModel()
) {
    val equipes by viewModel.equipesHistorico.collectAsState()

    val viaturasEscaladas = remember(equipes) {
        equipes.flatMap { equipe -> 
            equipe.viaturas.map { viatura -> Pair(equipe, viatura) }
        }.sortedByDescending { it.first.data }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumo Operacional", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (viaturasEscaladas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhuma viatura escalada.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(viaturasEscaladas, key = { it.second.id }) { (equipe, viatura) ->
                    ResumoViaturaCard(equipe, viatura)
                }
            }
        }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Data: $dateFormatted", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(text = "${viatura.viatura?.prefixo ?: "Prefixo Desconhecido"} - ${viatura.viatura?.tipo ?: ""}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expandir"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Equipe: ${equipe.tipoEscala}", fontSize = 13.sp)
                Text("Posto: ${equipe.unidade} / ${equipe.posto}", fontSize = 13.sp)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    
                    Text("Efetivo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 4.dp))
                    
                    if (viatura.comandante != null) {
                        Text("Comandante: ${viatura.comandante.graduacao} ${viatura.comandante.nomeGuerra}", fontSize = 14.sp)
                    } else {
                        Text("Comandante: Não definido", fontSize = 14.sp, color = Color.Gray)
                    }

                    if (viatura.motorista != null) {
                        Text("Motorista: ${viatura.motorista.graduacao} ${viatura.motorista.nomeGuerra}", fontSize = 14.sp)
                    } else {
                        Text("Motorista: Não definido", fontSize = 14.sp, color = Color.Gray)
                    }

                    viatura.auxiliares.forEachIndexed { index, aux ->
                        Text("Auxiliar ${index + 1}: ${aux.graduacao} ${aux.nomeGuerra}", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
