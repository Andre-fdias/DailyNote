package com.andrefdias.dailynote.ui.screens.ocorrencias.modules

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.andrefdias.dailynote.data.local.entities.RoomNovaOcorrencia
import com.andrefdias.dailynote.ui.screens.ocorrencias.PessoaEnvolvida
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// --- Visual Transformations ---
class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val clean = text.text.filter { it.isDigit() }.take(11)
        val out = StringBuilder()
        for (i in clean.indices) {
            out.append(clean[i])
            if (i == 2 || i == 5) out.append('.')
            if (i == 8) out.append('-')
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                var dots = 0
                var hyphens = 0
                if (offset > 2) dots++
                if (offset > 5) dots++
                if (offset > 8) hyphens++
                return minOf(offset + dots + hyphens, out.length)
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                var dots = 0
                var hyphens = 0
                if (offset > 3) dots++
                if (offset > 7) dots++
                if (offset > 11) hyphens++
                return minOf(offset - dots - hyphens, clean.length)
            }
        }
        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val clean = text.text.filter { it.isDigit() }.take(11)
        var out = ""
        for (i in clean.indices) {
            if (i == 0) out += "("
            out += clean[i]
            if (i == 1) out += ") "
            if (clean.length == 11) {
                if (i == 6) out += "-"
            } else {
                if (i == 5) out += "-"
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformed = 0
                for (i in 0 until offset) {
                    if (i == 0) transformed += 1 // (
                    transformed += 1 // digit
                    if (i == 1) transformed += 2 // ) and space
                    if (clean.length == 11) {
                        if (i == 6) transformed += 1 // -
                    } else {
                        if (i == 5) transformed += 1 // -
                    }
                }
                return transformed
            }
            override fun transformedToOriginal(offset: Int): Int {
                var original = 0
                var transformed = 0
                for (i in 0 until clean.length) {
                    if (transformed >= offset) break
                    if (i == 0) transformed += 1
                    transformed += 1
                    if (i == 1) transformed += 2
                    if (clean.length == 11) {
                        if (i == 6) transformed += 1
                    } else {
                        if (i == 5) transformed += 1
                    }
                    if (transformed <= offset) original += 1
                }
                return original
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

fun isValidEmail(email: String): Boolean {
    if (email.isBlank()) return true
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

// --- Common UI Components ---

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    readOnly: Boolean = false,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            isError = isError,
            readOnly = readOnly,
            enabled = enabled,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        val format = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                        onValueChange(format.format(date))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    CustomTextField(
        value = value,
        onValueChange = {}, // Read-only
        label = label,
        placeholder = placeholder,
        modifier = modifier.clickable { showDatePicker = true },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Selecionar data", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun FormCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    trailingIcon()
                }
            }
            content()
        }
    }
}

// --- Main View ---

@Composable
fun PessoasModuleView(
    ocorrencia: RoomNovaOcorrencia,
    onSave: (String) -> Unit,
    onSaveGlobalPhotos: (String) -> Unit,
    onCancel: () -> Unit
) {
    val gson = Gson()
    val typeToken = object : TypeToken<List<String>>() {}.type
    val pTypeToken = object : TypeToken<List<PessoaEnvolvida>>() {}.type
    val pessoasEnvolvidas: List<PessoaEnvolvida> = try {
        if (!ocorrencia.pessoasJson.isNullOrBlank()) gson.fromJson(ocorrencia.pessoasJson, pTypeToken) else emptyList()
    } catch(e: Exception) { emptyList() }

    var isAddingOrEditing by remember { mutableStateOf(false) }
    var pessoaEditing by remember { mutableStateOf<PessoaEnvolvida?>(null) }
    var localPessoas by remember { mutableStateOf(pessoasEnvolvidas) }

    val existingGlobalPhotos: List<String> = try {
        if (!ocorrencia.fotosUrisJson.isNullOrBlank()) {
            gson.fromJson(ocorrencia.fotosUrisJson, typeToken)
        } else emptyList()
    } catch (e: Exception) { emptyList() }

    if (isAddingOrEditing) {
        PessoaFormScreen(
            pessoa = pessoaEditing,
            onSave = { novaPessoa, newPhotos ->
                val novaLista = if (pessoaEditing != null) {
                    localPessoas.map { if (it.id == novaPessoa.id) novaPessoa else it }
                } else {
                    localPessoas + novaPessoa
                }
                localPessoas = novaLista
                onSave(gson.toJson(novaLista))
                
                if (newPhotos.isNotEmpty()) {
                    val updatedGlobal = existingGlobalPhotos + newPhotos
                    onSaveGlobalPhotos(gson.toJson(updatedGlobal))
                }
                
                isAddingOrEditing = false
                pessoaEditing = null
            },
            onCancel = {
                isAddingOrEditing = false
                pessoaEditing = null
            }
        )
    } else {
        // List Screen
        var docToDelete by remember { mutableStateOf<PessoaEnvolvida?>(null) }

        if (docToDelete != null) {
            AlertDialog(
                onDismissRequest = { docToDelete = null },
                title = { Text("Excluir Pessoa") },
                text = { Text("Tem certeza que deseja remover esta pessoa?") },
                confirmButton = {
                    TextButton(onClick = {
                        val novaLista = localPessoas.filter { it.id != docToDelete?.id }
                        localPessoas = novaLista
                        onSave(gson.toJson(novaLista))
                        docToDelete = null
                    }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { docToDelete = null }) { Text("Cancelar") }
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                if (localPessoas.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Nenhuma pessoa adicionada.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(localPessoas) { pessoa ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    pessoaEditing = pessoa
                                    isAddingOrEditing = true
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail (First Photo)
                                val pFotos: List<String> = try { if (!pessoa.fotosUrisJson.isNullOrBlank()) Gson().fromJson(pessoa.fotosUrisJson, object : TypeToken<List<String>>() {}.type) else emptyList() } catch (e: Exception) { emptyList() }
                                if (pFotos.isNotEmpty()) {
                                    AsyncImage(
                                        model = pFotos.first(),
                                        contentDescription = "Foto da pessoa",
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val badgeBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        val badgeText = MaterialTheme.colorScheme.primary
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(badgeBg).padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text((pessoa.tipoDocumento ?: "DOC").uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = badgeText)
                                        }
                                        
                                        IconButton(
                                            onClick = { docToDelete = pessoa },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Text(
                                        text = pessoa.nome.ifBlank { "Sem Nome" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    
                                    val identificador = when(pessoa.tipoDocumento) {
                                        "RG" -> pessoa.rg
                                        "CPF" -> pessoa.cpf
                                        "CNH" -> pessoa.registro
                                        else -> pessoa.documento
                                    }
                                    
                                    if (identificador.isNotBlank()) {
                                        Text(
                                            text = "Nº: $identificador",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${pessoa.papel.ifBlank { "Papel N/I" }} • ${pessoa.telefone.ifBlank { "Sem Fone" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Voltar")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    pessoaEditing = null
                    isAddingOrEditing = true
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PessoaFormScreen(
    pessoa: PessoaEnvolvida?,
    onSave: (PessoaEnvolvida, List<String>) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val papelOptions = listOf("Vítima", "Autor", "Testemunha", "Envolvido", "Condutor")
    var expandedPapel by remember { mutableStateOf(false) }

    var papel by remember { mutableStateOf(pessoa?.papel ?: "") }
    var tipoDocumento by remember { mutableStateOf(pessoa?.tipoDocumento ?: "RG") }
    var nome by remember { mutableStateOf(pessoa?.nome ?: "") }
    var rg by remember { mutableStateOf(pessoa?.rg ?: "") }
    var cpf by remember { mutableStateOf(pessoa?.cpf ?: "") }
    var nascimento by remember { mutableStateOf(pessoa?.nascimento ?: "") }
    var naturalidade by remember { mutableStateOf(pessoa?.naturalidade ?: "") }
    var mae by remember { mutableStateOf(pessoa?.mae ?: "") }
    var orgao by remember { mutableStateOf(pessoa?.orgaoExpedidor ?: "") }
    var uf by remember { mutableStateOf(pessoa?.uf ?: "") }
    var dataExpedicao by remember { mutableStateOf(pessoa?.dataExpedicao ?: "") }
    var validade by remember { mutableStateOf(pessoa?.validade ?: "") }
    var telefone by remember { mutableStateOf(pessoa?.telefone ?: "") }
    var email by remember { mutableStateOf(pessoa?.email ?: "") }
    var registro by remember { mutableStateOf(pessoa?.registro ?: "") }
    
    val gson = Gson()
    val typeToken = object : TypeToken<List<String>>() {}.type
    var fotosUris by remember { mutableStateOf(if (pessoa != null && !pessoa.fotosUrisJson.isNullOrBlank()) gson.fromJson<List<String>>(pessoa.fotosUrisJson, typeToken) else emptyList()) }
    var newPhotos by remember { mutableStateOf<List<String>>(emptyList()) }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempImageUri?.let {
                val uriStr = it.toString()
                fotosUris = fotosUris + uriStr
                newPhotos = newPhotos + uriStr
            }
        }
    }

    val emailError = email.isNotBlank() && !isValidEmail(email)
    val cpfLengthOk = cpf.isBlank() || cpf.length == 11

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Top Card (Nova Pessoa + Salvar)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (pessoa == null) "Nova Pessoa" else "Editar Pessoa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            val nova = PessoaEnvolvida(
                                id = pessoa?.id ?: UUID.randomUUID().toString(),
                                papel = papel,
                                tipoDocumento = tipoDocumento,
                                nome = nome,
                                rg = rg,
                                cpf = cpf,
                                nascimento = nascimento,
                                naturalidade = naturalidade,
                                mae = mae,
                                orgaoExpedidor = orgao,
                                uf = uf,
                                dataExpedicao = dataExpedicao,
                                telefone = telefone,
                                email = email,
                                fotosUrisJson = Gson().toJson(fotosUris),
                                documento = if (tipoDocumento != "RG" && tipoDocumento != "CPF" && tipoDocumento != "CNH") rg else "",
                                validade = validade,
                                registro = registro
                            )
                            onSave(nova, newPhotos)
                        },
                        enabled = !emailError && cpfLengthOk
                    ) {
                        Text("SALVAR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Papel na Ocorrência
        item {
            FormCard(
                title = "Papel na Ocorrência *",
                icon = Icons.Filled.Badge,
                trailingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedPapel,
                    onExpandedChange = { expandedPapel = !expandedPapel }
                ) {
                    OutlinedTextField(
                        value = papel,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Selecione o papel") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPapel,
                        onDismissRequest = { expandedPapel = false }
                    ) {
                        papelOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    papel = option
                                    expandedPapel = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Segmented Buttons
                val tipos = listOf("RG", "CPF", "CNH", "CIN")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    tipos.forEach { tipo ->
                        val isSelected = tipoDocumento == tipo
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { tipoDocumento = tipo }
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = tipo,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dados de Identificação
        item {
            FormCard(title = "Dados de Identificação", icon = Icons.Filled.Badge) {
                CustomTextField(
                    value = nome, onValueChange = { nome = it },
                    label = "Nome Completo *", placeholder = "Digite o nome completo"
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (tipoDocumento) {
                    "RG" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CustomTextField(value = rg, onValueChange = { rg = it.filter { c -> c.isDigit() || c.isLetter() } }, label = "Número RG", placeholder = "Digite o nº do RG", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            CustomTextField(value = cpf, onValueChange = { if (it.length <= 11) cpf = it.filter { c -> c.isDigit() } }, label = "CPF", placeholder = "Digite o CPF", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = CpfVisualTransformation())
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CustomDatePickerField(value = nascimento, onValueChange = { nascimento = it }, label = "Nascimento", placeholder = "DD/MM/AAAA", modifier = Modifier.weight(1f))
                            CustomTextField(value = naturalidade, onValueChange = { naturalidade = it }, label = "Naturalidade", placeholder = "Digite a naturalidade", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomTextField(value = mae, onValueChange = { mae = it }, label = "Nome da Mãe", placeholder = "Digite o nome da mãe")
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CustomTextField(value = orgao, onValueChange = { orgao = it.uppercase() }, label = "Órgão Emissor", placeholder = "Ex.: SSP", modifier = Modifier.weight(1f))
                            // Simple text field for UF to match mockup instead of dropdown for brevity, or we can use dropdown
                            CustomTextField(value = uf, onValueChange = { if (it.length <= 2) uf = it.uppercase() }, label = "UF", placeholder = "Selecione", modifier = Modifier.weight(1f))
                            CustomDatePickerField(value = dataExpedicao, onValueChange = { dataExpedicao = it }, label = "Emissão", placeholder = "DD/MM/AAAA", modifier = Modifier.weight(1.2f))
                        }
                    }
                    "CPF" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CustomTextField(value = cpf, onValueChange = { if (it.length <= 11) cpf = it.filter { c -> c.isDigit() } }, label = "CPF", placeholder = "Digite o CPF", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = CpfVisualTransformation())
                            CustomDatePickerField(value = nascimento, onValueChange = { nascimento = it }, label = "Nascimento", placeholder = "DD/MM/AAAA", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomTextField(value = mae, onValueChange = { mae = it }, label = "Nome da Mãe", placeholder = "Digite o nome da mãe")
                    }
                    "CNH" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CustomTextField(value = cpf, onValueChange = { if (it.length <= 11) cpf = it.filter { c -> c.isDigit() } }, label = "CPF", placeholder = "Digite o CPF", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = CpfVisualTransformation())
                            CustomTextField(value = registro, onValueChange = { registro = it.filter { c -> c.isDigit() } }, label = "Registro", placeholder = "Digite o registro", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CustomDatePickerField(value = nascimento, onValueChange = { nascimento = it }, label = "Nascimento", placeholder = "DD/MM/AAAA", modifier = Modifier.weight(1f))
                            CustomDatePickerField(value = validade, onValueChange = { validade = it }, label = "Validade", placeholder = "DD/MM/AAAA", modifier = Modifier.weight(1f))
                        }
                    }
                    "CIN" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CustomTextField(value = cpf, onValueChange = { if (it.length <= 11) cpf = it.filter { c -> c.isDigit() } }, label = "CPF / CIN", placeholder = "Digite o CPF", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = CpfVisualTransformation())
                            CustomDatePickerField(value = nascimento, onValueChange = { nascimento = it }, label = "Nascimento", placeholder = "DD/MM/AAAA", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomDatePickerField(value = dataExpedicao, onValueChange = { dataExpedicao = it }, label = "Emissão", placeholder = "DD/MM/AAAA")
                    }
                }
            }
        }

        // Contato
        item {
            FormCard(title = "Contato", icon = Icons.Filled.ContactPhone) {
                CustomTextField(
                    value = telefone, onValueChange = { if (it.length <= 11) telefone = it.filter { c -> c.isDigit() } },
                    label = "Telefone / Celular", placeholder = "(00) 00000-0000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    visualTransformation = PhoneVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomTextField(
                    value = email, onValueChange = { email = it },
                    label = "Email", placeholder = "exemplo@email.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError
                )
            }
        }

        // Fotos dos Documentos
        item {
            FormCard(title = "Fotos dos Documentos", icon = Icons.Filled.CameraAlt) {
                val dashedStroke = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
                val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .drawBehind {
                            drawRoundRect(color = borderColor, style = dashedStroke, cornerRadius = CornerRadius(12.dp.toPx()))
                        }
                        .background(backgroundColor, RoundedCornerShape(12.dp))
                        .clickable {
                            val photoFile = File(context.cacheDir, "pessoa_${UUID.randomUUID()}.jpg")
                            photoFile.parentFile?.mkdirs()
                            photoFile.createNewFile()
                            tempImageUri = FileProvider.getUriForFile(context, "com.andrefdias.dailynote.fileprovider", photoFile)
                            cameraLauncher.launch(tempImageUri!!)
                        }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Adicionar Foto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Tire ou selecione uma foto", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nenhuma foto anexada. Registre CNH, RG ou o rosto do envolvido.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                if (fotosUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(fotosUris) { uri ->
                            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Foto",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dicas Importantes
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
                        "Verifique se os dados estão corretos antes de salvar",
                        "Fotos nítidas e com boa iluminação",
                        "Os dados são protegidos e criptografados"
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

        // Save Button at the Bottom
        item {
            Button(
                onClick = {
                    val nova = PessoaEnvolvida(
                        id = pessoa?.id ?: UUID.randomUUID().toString(),
                        papel = papel,
                        tipoDocumento = tipoDocumento,
                        nome = nome,
                        rg = rg,
                        cpf = cpf,
                        nascimento = nascimento,
                        naturalidade = naturalidade,
                        mae = mae,
                        orgaoExpedidor = orgao,
                        uf = uf,
                        dataExpedicao = dataExpedicao,
                        telefone = telefone,
                        email = email,
                        fotosUrisJson = gson.toJson(fotosUris),
                        documento = if (tipoDocumento != "RG" && tipoDocumento != "CPF" && tipoDocumento != "CNH") rg else "",
                        validade = validade,
                        registro = registro
                    )
                    onSave(nova, newPhotos)
                },
                enabled = !emailError && cpfLengthOk,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "SALVAR PESSOA",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
