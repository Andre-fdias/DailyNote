package com.andrefdias.dailynote.ui.designsystem.components.inputs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.andrefdias.dailynote.ui.designsystem.colors.FireColor
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.shapes.FireShapes
import com.andrefdias.dailynote.ui.designsystem.icons.FireIcons
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import java.util.*

@Composable
fun FireTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        enabled = enabled,
        singleLine = singleLine,
        shape = FireShapes.Medium,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    )
}


@Composable
fun FireOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    error: Boolean = false,
    placeholder: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = FireColors.Primary,
        unfocusedBorderColor = FireColors.OnSurfaceVariant.copy(alpha = 0.5f),
        focusedLabelColor = FireColors.Primary,
        unfocusedLabelColor = FireColors.OnSurfaceVariant
    ),
    shape: androidx.compose.foundation.shape.RoundedCornerShape = FireShapes.Medium
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = FireTypography.BodyMedium) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        enabled = enabled,
        readOnly = readOnly,
        isError = error,
        placeholder = placeholder,
        suffix = suffix,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        colors = colors,
        shape = shape
    )
}

@Composable
fun FireSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder) },
        singleLine = true,
        shape = FireShapes.Large,
        leadingIcon = { Icon(imageVector = FireIcons.Search, contentDescription = "Pesquisar") },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun FireDatePicker(
    value: String,
    onDateSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val currentOnDateSelected by rememberUpdatedState(onDateSelected)

    fun showDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val dateString = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                currentOnDateSelected(dateString)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePicker() }
    ) {
        FireOutlinedTextField(
            value = value,
            onValueChange = {},
            label = label,
            readOnly = true,
            enabled = true,
            trailingIcon = { Icon(imageVector = FireIcons.Calendar, contentDescription = "Escolha a data", tint = FireColors.Primary) }
        )
        // Overlay to capture clicks safely and prevent keyboard focus
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDatePicker() }
        )
    }
}

@Composable
fun FireTimePicker(
    value: String,
    onTimeSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val currentOnTimeSelected by rememberUpdatedState(onTimeSelected)

    fun showTimePicker() {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val timeString = String.format("%02d:%02d", hourOfDay, minute)
                currentOnTimeSelected(timeString)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showTimePicker() }
    ) {
        FireOutlinedTextField(
            value = value,
            onValueChange = {},
            label = label,
            readOnly = true,
            enabled = true,
            trailingIcon = { Icon(imageVector = FireIcons.Time, contentDescription = "Escolha a hora", tint = FireColors.Primary) }
        )
        // Overlay to capture clicks safely and prevent keyboard focus
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showTimePicker() }
        )
    }
}

@Composable
fun FireDropdown(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FireOutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = label,
            enabled = false,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(imageVector = FireIcons.ArrowDropDown, contentDescription = "Dropdown")
                }
            },
            modifier = Modifier.clickable { expanded = !expanded }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option, style = FireTypography.Body) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FireCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = FireSpacing.Small)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = FireColor.Primary)
        )
        Spacer(modifier = Modifier.width(FireSpacing.Small))
        Text(text = label, style = FireTypography.Body)
    }
}

@Composable
fun FireRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = FireSpacing.Small)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = FireColor.Primary)
        )
        Spacer(modifier = Modifier.width(FireSpacing.Small))
        Text(text = label, style = FireTypography.Body)
    }
}

@Composable
fun FireSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = FireSpacing.Small)
    ) {
        Text(text = label, style = FireTypography.Title)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = FireColor.Primary)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = FireTypography.Label) },
        shape = FireShapes.Small,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireSearchableDropdown(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    optionIcons: Map<String, String> = emptyMap(),
    placeholder: String = "Pesquisar..."
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredOptions = remember(searchQuery, options) {
        if (searchQuery.isBlank()) {
            options
        } else {
            options.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        FireOutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = label,
            readOnly = true,
            leadingIcon = optionIcons[selectedOption]?.let { emoji ->
                { Text(emoji, modifier = Modifier.padding(start = 12.dp), style = FireTypography.Title) }
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { 
                expanded = false
                searchQuery = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(placeholder, style = FireTypography.Body) },
                leadingIcon = { Icon(imageVector = FireIcons.Search, contentDescription = "Buscar") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FireSpacing.Small)
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = FireSpacing.ExtraSmall))

            if (filteredOptions.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Nenhum resultado encontrado", style = FireTypography.Body) },
                    onClick = {},
                    enabled = false
                )
            } else {
                filteredOptions.forEach { option ->
                    val emoji = optionIcons[option]
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                if (emoji != null) {
                                    Text(emoji, style = FireTypography.Title)
                                    Spacer(modifier = Modifier.width(FireSpacing.Small))
                                }
                                Text(text = option, style = FireTypography.Body)
                            }
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                            searchQuery = ""
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireSearchableDropdownPremium(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    categories: Map<String, List<String>>,
    favorites: List<String> = emptyList(),
    recents: List<String> = emptyList(),
    optionIcons: Map<String, String> = emptyMap(),
    placeholder: String = "Pesquisar natureza..."
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val allOptions = remember(categories) {
        categories.values.flatten()
    }

    val filteredOptions = remember(searchQuery, categories) {
        try {
            if (searchQuery.isBlank()) {
                emptyList()
            } else {
                val subNatureKeywordsMap = mapOf(
                    "Incêndio em residência" to listOf("casa", "fogo", "residencia", "lar", "domestico"),
                    "Incêndio em comércio" to listOf("loja", "fogo", "estabelecimento", "predio"),
                    "Incêndio em veículo" to listOf("carro", "fogo", "veiculo", "moto", "caminhao"),
                    "Incêndio florestal" to listOf("mato", "fogo", "arvore", "floresta", "queimada", "vegetacao"),
                    "Incêndio industrial" to listOf("galpao", "fogo", "industria", "fabrica", "quimico"),
                    "Mal súbito" to listOf("desmaio", "pressao", "passando mal", "infarto"),
                    "Queda" to listOf("altura", "propria altura", "chao", "queda"),
                    "Trauma" to listOf("fratura", "corte", "sangramento", "ferimento"),
                    "PCR" to listOf("parada", "cardio", "respiratoria", "reanimacao"),
                    "Parto" to listOf("nascimento", "bebe", "gravida", "gestante"),
                    "Afogamento" to listOf("agua", "piscina", "rio", "mar"),
                    "Altura" to listOf("rapel", "ponte", "predio", "elevado"),
                    "Aquático" to listOf("rio", "mar", "represa", "afogamento"),
                    "Estrutural" to listOf("desabamento", "escombros", "colapso"),
                    "Animal" to listOf("cachorro", "gato", "cobra", "resgate", "bicho"),
                    "Busca" to listOf("desaparecido", "floresta", "resgate", "perdido"),
                    "Colisão" to listOf("batida", "carro", "veiculo", "transito"),
                    "Capotamento" to listOf("tombamento", "carro", "veiculo", "transito"),
                    "Atropelamento" to listOf("pedestre", "carro", "veiculo", "atropelar"),
                    "Moto" to listOf("colisao moto", "queda moto", "motocicleta"),
                    "Caminhão" to listOf("carreta", "caminhao", "veiculo pesado"),
                    "Queda de árvore" to listOf("arvore", "via", "bloqueio", "vento"),
                    "Choque elétrico" to listOf("energia", "fio", "poste", "eletrocussao"),
                    "Vazamento" to listOf("gas", "agua", "produto", "vazando"),
                    "Produtos perigosos" to listOf("quimico", "gas", "carga", "explosivo")
                )
                allOptions.filter { option ->
                    option.contains(searchQuery, ignoreCase = true) ||
                    (subNatureKeywordsMap[option]?.any { it.contains(searchQuery, ignoreCase = true) } ?: false) ||
                    categories.entries.any { entry ->
                        entry.value.contains(option) && entry.key.contains(searchQuery, ignoreCase = true)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FireNotes", "NatureDropdown error filtering: ${e.message}", e)
            emptyList()
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        FireOutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = label,
            readOnly = true,
            leadingIcon = optionIcons[selectedOption]?.let { emoji ->
                { Text(emoji, modifier = Modifier.padding(start = 12.dp), style = FireTypography.Title) }
            },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) FireIcons.ArrowDropUp else FireIcons.ArrowDropDown,
                    contentDescription = "Dropdown"
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                searchQuery = ""
            },
            properties = androidx.compose.ui.window.PopupProperties(focusable = true),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 400.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(placeholder, style = FireTypography.Body) },
                leadingIcon = { Icon(imageVector = FireIcons.Search, contentDescription = "Buscar") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FireSpacing.Small)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = FireSpacing.ExtraSmall))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(scrollState)
            ) {
                if (searchQuery.isNotBlank()) {
                    if (filteredOptions.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma natureza encontrada", style = FireTypography.Body) },
                            onClick = {},
                            enabled = false
                        )
                    } else {
                        filteredOptions.forEach { option ->
                            val emoji = optionIcons[option]
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        if (emoji != null) {
                                            Text(emoji, style = FireTypography.Title)
                                            Spacer(modifier = Modifier.width(FireSpacing.Small))
                                        }
                                        Text(text = option, style = FireTypography.Body)
                                    }
                                },
                                onClick = {
                                    onOptionSelected(option)
                                    expanded = false
                                    searchQuery = ""
                                }
                            )
                        }
                    }
                } else {
                    if (favorites.isNotEmpty()) {
                        Text(
                            text = "⭐ NATUREZAS MAIS UTILIZADAS",
                            style = FireTypography.LabelMedium,
                            fontWeight = FontWeight.Bold,
                            color = FireColors.Primary,
                            modifier = Modifier.padding(horizontal = FireSpacing.Medium, vertical = FireSpacing.Small)
                        )
                        favorites.forEach { option ->
                            val emoji = optionIcons[option]
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        if (emoji != null) {
                                            Text(emoji, style = FireTypography.Title)
                                            Spacer(modifier = Modifier.width(FireSpacing.Small))
                                        }
                                        Text(text = option, style = FireTypography.Body)
                                    }
                                },
                                onClick = {
                                    onOptionSelected(option)
                                    expanded = false
                                }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = FireSpacing.ExtraSmall))
                    }

                    if (recents.isNotEmpty()) {
                        Text(
                            text = "🕒 ÚLTIMAS UTILIZADAS",
                            style = FireTypography.LabelMedium,
                            fontWeight = FontWeight.Bold,
                            color = FireColors.OnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = FireSpacing.Medium, vertical = FireSpacing.Small)
                        )
                        recents.forEach { option ->
                            val emoji = optionIcons[option]
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        if (emoji != null) {
                                            Text(emoji, style = FireTypography.Title)
                                            Spacer(modifier = Modifier.width(FireSpacing.Small))
                                        }
                                        Text(text = option, style = FireTypography.Body)
                                    }
                                },
                                onClick = {
                                    onOptionSelected(option)
                                    expanded = false
                                }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = FireSpacing.ExtraSmall))
                    }

                    categories.forEach { (category, optionsList) ->
                        Text(
                            text = category,
                            style = FireTypography.LabelMedium,
                            fontWeight = FontWeight.Bold,
                            color = FireColors.Primary,
                            modifier = Modifier.padding(horizontal = FireSpacing.Medium, vertical = FireSpacing.Small)
                        )
                        optionsList.forEach { option ->
                            val emoji = optionIcons[option]
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        if (emoji != null) {
                                            Text(emoji, style = FireTypography.Title)
                                            Spacer(modifier = Modifier.width(FireSpacing.Small))
                                        }
                                        Text(text = option, style = FireTypography.Body)
                                    }
                                },
                                onClick = {
                                    onOptionSelected(option)
                                    expanded = false
                                }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = FireSpacing.ExtraSmall))
                    }
                }
            }
        }
    }
}
