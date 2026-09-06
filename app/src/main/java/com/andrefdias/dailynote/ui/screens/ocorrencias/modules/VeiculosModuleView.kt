package com.andrefdias.dailynote.ui.screens.ocorrencias.modules

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.andrefdias.dailynote.data.local.entities.RoomNovoVeiculo
import com.andrefdias.dailynote.ui.screens.ocorrencias.OcorrenciaOpcoesViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.Calendar
import java.util.UUID

// Placa Visual Transformation (Mercosul or Antiga)
class PlacaVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(7)
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 2 && trimmed.length > 3 && trimmed[3].isDigit()) {
                out += "-"
            }
        }
        
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset > 2 && text.text.length > 3 && text.text[3].isDigit()) return offset + 1
                return offset
            }
            
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset > 3 && text.text.length > 3 && text.text[3].isDigit()) return offset - 1
                return offset
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

val CoresVeiculos = listOf(
    "Branco" to Color(0xFFF5F5F5),
    "Preto" to Color(0xFF212121),
    "Prata" to Color(0xFFBDBDBD),
    "Cinza" to Color(0xFF757575),
    "Cinza Escuro" to Color(0xFF424242),
    "Vermelho" to Color(0xFFF44336),
    "Azul" to Color(0xFF2196F3),
    "Azul Escuro" to Color(0xFF1565C0),
    "Verde" to Color(0xFF4CAF50),
    "Verde Escuro" to Color(0xFF2E7D32),
    "Amarelo" to Color(0xFFFFEB3B),
    "Marrom" to Color(0xFF795548),
    "Bege" to Color(0xFFD7CCC8),
    "Laranja" to Color(0xFFFF9800),
    "Roxo" to Color(0xFF9C27B0),
    "Rosa" to Color(0xFFE91E63),
    "Dourado" to Color(0xFFFFD700),
    "Vinho" to Color(0xFF880E4F),
    "Champagne" to Color(0xFFF5E6CC),
    "Cobre" to Color(0xFFB87333),
    "Grafite" to Color(0xFF616161),
    "Azul Metálico" to Color(0xFF5472AE),
    "Verde Metálico" to Color(0xFF4E8975),
    "Fantasia" to Color(0xFFAB47BC)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeiculosModuleView(
    ocorrenciaId: String,
    viewModel: OcorrenciaOpcoesViewModel,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(ocorrenciaId) {
        viewModel.loadVeiculos(ocorrenciaId)
    }
    
    val veiculos by viewModel.veiculos.collectAsState()
    var isAdding by remember { mutableStateOf(false) }
    var veiculoEditing by remember { mutableStateOf<RoomNovoVeiculo?>(null) }
    
    if (isAdding || veiculoEditing != null) {
        VeiculoFormScreen(
            ocorrenciaId = ocorrenciaId,
            veiculo = veiculoEditing,
            onSave = { v ->
                if (veiculoEditing != null) {
                    viewModel.updateVeiculo(v)
                } else {
                    viewModel.addVeiculo(v)
                }
                isAdding = false
                veiculoEditing = null
            },
            onCancel = {
                isAdding = false
                veiculoEditing = null
            }
        )
    } else {
        // Listagem de Veículos
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Veículos da Ocorrência", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { isAdding = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Novo Veículo")
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
                if (veiculos.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.DirectionsCar, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                Spacer(Modifier.height(16.dp))
                                Text("Nenhum veículo adicionado", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                } else {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(veiculos) { v ->
                        val uris = try {
                            val type = object : TypeToken<List<String>>() {}.type
                            Gson().fromJson<List<String>>(v.fotosVeiculoUrisJson ?: "[]", type).map { Uri.parse(it) }
                        } catch (e: Exception) { emptyList() }
                        
                        val allPics = listOfNotNull(v.urlCrlv?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }) + uris
                        val imageUri = allPics.firstOrNull()
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { veiculoEditing = v },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                // Imagem ou Placeholder
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (imageUri != null) {
                                        AsyncImage(
                                            model = imageUri,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            Icons.Filled.DirectionsCar,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = v.placa.ifEmpty { "SEM PLACA" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val desc = listOf(v.marca, v.modelo, v.cor, v.ano).filter { !it.isNullOrEmpty() }.joinToString(" • ")
                                    Text(
                                        text = desc.ifEmpty { "Veículo não identificado" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                IconButton(onClick = { viewModel.deleteVeiculo(v) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeiculoFormScreen(
    ocorrenciaId: String,
    veiculo: RoomNovoVeiculo?,
    onSave: (RoomNovoVeiculo) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val gson = Gson()
    
    // Form State
    var placa by remember { mutableStateOf(veiculo?.placa ?: "") }
    var chassi by remember { mutableStateOf(veiculo?.chassi ?: "") }
    var marca by remember { mutableStateOf(veiculo?.marca ?: "") }
    var modelo by remember { mutableStateOf(veiculo?.modelo ?: "") }
    var cor by remember { mutableStateOf(veiculo?.cor ?: "") }
    var ano by remember { mutableStateOf(veiculo?.ano ?: "") }
    
    var fotoCrlvUriString by remember { mutableStateOf(veiculo?.urlCrlv?.takeIf { it.isNotBlank() }) }
    
    val initialVeiculoUris = try {
        val type = object : TypeToken<List<String>>() {}.type
        gson.fromJson<List<String>>(veiculo?.fotosVeiculoUrisJson ?: "[]", type)
    } catch (e: Exception) { emptyList() }
    
    var fotosVeiculoUrisStrings by remember { mutableStateOf(initialVeiculoUris) }
    
    var showAnoModal by remember { mutableStateOf(false) }
    var showCorModal by remember { mutableStateOf(false) }
    
    var tempCameraUriString by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
    
    val cameraLauncherCrlv = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUriString != null) {
            fotoCrlvUriString = tempCameraUriString
        }
    }
    
    val cameraLauncherVeiculo = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUriString != null) {
            fotosVeiculoUrisStrings = fotosVeiculoUrisStrings + tempCameraUriString!!
        }
    }
    
    val galleryLauncherVeiculo = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
        if (uris.isNotEmpty()) {
            fotosVeiculoUrisStrings = fotosVeiculoUrisStrings + uris.map { it.toString() }
        }
    }
    
    fun createTempUri(): Uri {
        val file = File(context.cacheDir, "veiculo_${UUID.randomUUID()}.jpg")
        file.parentFile?.mkdirs()
        file.createNewFile()
        return FileProvider.getUriForFile(context, "com.andrefdias.dailynote.fileprovider", file)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (veiculo == null) "Novo Veículo" else "Editar Veículo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancelar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            
            item {
                FormCard(title = "Identificação do Veículo", icon = Icons.Filled.DirectionsCar) {
                    CustomTextField(
                        value = placa,
                        onValueChange = { 
                            val raw = it.uppercase().replace(Regex("[^A-Z0-9]"), "")
                            if (raw.length <= 7) placa = raw 
                        },
                        label = "Placa",
                        placeholder = "Ex: ABC1D23",
                        modifier = Modifier.padding(bottom = 16.dp),
                        visualTransformation = PlacaVisualTransformation()
                    )
                    
                    CustomTextField(
                        value = chassi,
                        onValueChange = { chassi = it.uppercase() },
                        label = "Chassi",
                        placeholder = "Digite o chassi (se souber)",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CustomTextField(
                            value = marca,
                            onValueChange = { marca = it },
                            label = "Marca",
                            placeholder = "Ex: Fiat",
                            modifier = Modifier.weight(1f).padding(bottom = 16.dp)
                        )
                        CustomTextField(
                            value = modelo,
                            onValueChange = { modelo = it },
                            label = "Modelo",
                            placeholder = "Ex: Palio",
                            modifier = Modifier.weight(1f).padding(bottom = 16.dp)
                        )
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f).clickable { showCorModal = true }) {
                            CustomTextField(
                                value = cor,
                                onValueChange = {},
                                label = "Cor",
                                placeholder = "Selecione...",
                                modifier = Modifier.padding(bottom = 16.dp),
                                trailingIcon = { Icon(Icons.Filled.Palette, null) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f).clickable { showAnoModal = true }) {
                            CustomTextField(
                                value = ano,
                                onValueChange = {},
                                label = "Ano",
                                placeholder = "Selecione...",
                                modifier = Modifier.padding(bottom = 16.dp),
                                trailingIcon = { Icon(Icons.Filled.CalendarToday, null) }
                            )
                        }
                    }
                }
            }
            
            item {
                FormCard(title = "Fotos do Veículo e CRLV", icon = Icons.Filled.CameraAlt) {
                    val strokeColor = MaterialTheme.colorScheme.primary
                    val backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawRoundRect(
                                    color = strokeColor,
                                    style = Stroke(
                                        width = 4f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                                    ),
                                    cornerRadius = CornerRadius(36f, 36f)
                                )
                            }
                            .background(backgroundColor, RoundedCornerShape(12.dp))
                            .clickable {
                                galleryLauncherVeiculo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Adicionar fotos da Galeria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Toque para selecionar imagens", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                val uri = createTempUri()
                                tempCameraUriString = uri.toString()
                                cameraLauncherCrlv.launch(uri)
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.DocumentScanner, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Câmera CRLV", fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = {
                                val uri = createTempUri()
                                tempCameraUriString = uri.toString()
                                cameraLauncherVeiculo.launch(uri)
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Câmera Veículo", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    // Show Photos
                    val allPics = listOfNotNull(fotoCrlvUriString) + fotosVeiculoUrisStrings
                    if (allPics.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Fotos Adicionadas", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(allPics) { uriStr ->
                                Box {
                                    AsyncImage(
                                        model = Uri.parse(uriStr),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                    IconButton(
                                        onClick = { 
                                            if (uriStr == fotoCrlvUriString) fotoCrlvUriString = null
                                            else fotosVeiculoUrisStrings = fotosVeiculoUrisStrings - uriStr
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Remover", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    if (uriStr == fotoCrlvUriString) {
                                        Box(
                                            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.6f)).padding(2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("CRLV", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dicas importantes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val dicas = listOf(
                            "Verifique se a placa está legível nas fotos",
                            "Tire fotos de todos os ângulos do veículo envolvido",
                            "Se houver danos, foque as avarias"
                        )
                        dicas.forEach { dica ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(dica, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            
            // Save Button
            item {
                Button(
                    onClick = {
                        val nova = RoomNovoVeiculo(
                            id = veiculo?.id ?: UUID.randomUUID().toString(),
                            ocorrenciaId = ocorrenciaId,
                            placa = placa,
                            modelo = modelo,
                            cor = cor,
                            chassi = chassi,
                            anoFabricacao = null,
                            anoModelo = null,
                            ano = ano,
                            proprietarioId = null,
                            marca = marca,
                            versao = "",
                            exercicio = "",
                            urlCrlv = fotoCrlvUriString ?: "",
                            ocrTextoCrlv = null,
                            ocrDadosEstruturadosJson = "{}",
                            veiculoMasterId = null,
                            condutorId = null,
                            dadosMotoristaJson = null,
                            renavam = null,
                            monobloco = null,
                            especie = null,
                            tipoVeiculo = null,
                            carroceria = null,
                            categoriaVeiculo = null,
                            fotosVeiculoUrisJson = gson.toJson(fotosVeiculoUrisStrings)
                        )
                        onSave(nova)
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SALVAR VEÍCULO", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
    
    // Modals
    if (showAnoModal) {
        val currYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currYear + 1 downTo 1950).map { it.toString() }
        Dialog(onDismissRequest = { showAnoModal = false }) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Selecione o Ano", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(years) { y ->
                            Text(
                                text = y,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { ano = y; showAnoModal = false }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showCorModal) {
        Dialog(onDismissRequest = { showCorModal = false }) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Selecione a Cor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(CoresVeiculos) { (nome, color) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { cor = nome; showCorModal = false }) {
                                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(color))
                                Spacer(Modifier.height(4.dp))
                                Text(nome, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { cor = "Outra"; showCorModal = false }) {
                                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Outra", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
