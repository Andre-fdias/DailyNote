package com.andrefdias.dailynote.ui.screens.efetivo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andrefdias.dailynote.domain.model.EfetivoMilitar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EfetivoListScreen(
    onNavigateBack: () -> Unit,
    onMilitarClick: (EfetivoMilitar) -> Unit,
    viewModel: EfetivoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        // Header
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shadowElevation = 4.dp) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Efetivo Militar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "${uiState.efetivoList.size} registro(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${uiState.efetivoList.size}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    placeholder = { Text("Pesquisar por nome ou RE") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
            }
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Carregando efetivo...")
                    }
                }
            }
            uiState.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Erro: ${uiState.errorMessage}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchData() }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            }
            uiState.efetivoList.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Filled.SearchOff, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Nenhum militar cadastrado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            else -> {
                val filteredList = uiState.efetivoList.filter {
                    it.nomePadrao.contains(searchQuery, ignoreCase = true) || 
                    it.re.contains(searchQuery, ignoreCase = true) ||
                    it.nomeDeGuerra.contains(searchQuery, ignoreCase = true)
                }
                
                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum resultado para \"$searchQuery\"", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredList) { militar ->
                            EfetivoCard(
                                militar = militar,
                                onClick = { onMilitarClick(militar) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EfetivoCard(militar: EfetivoMilitar, onClick: () -> Unit) {
    val prontidaoColor = if (militar.prontidao.equals("SIM", true)) Color(0xFF16A34A) else Color(0xFFDC2626)

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar (prontidão color)
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(IntrinsicSize.Min)
                    .background(prontidaoColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Talão + prontidão badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${militar.graduacao} ${militar.nomeDeGuerra}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(prontidaoColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(militar.status.ifEmpty { "STATUS INDISP." }, color = prontidaoColor, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("RE: ${militar.re}-${militar.digito}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("MVM: ${militar.mvm}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Prontidão: ${militar.prontidao}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("CNH: ${militar.categoria}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
