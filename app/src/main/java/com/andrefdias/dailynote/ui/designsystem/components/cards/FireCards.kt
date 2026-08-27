package com.andrefdias.dailynote.ui.designsystem.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import com.andrefdias.dailynote.ui.designsystem.colors.FireColor
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.shapes.FireShapes
import com.andrefdias.dailynote.ui.designsystem.elevation.FireElevation
import com.andrefdias.dailynote.ui.designsystem.icons.FireIcons
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import com.andrefdias.dailynote.ui.designsystem.components.buttons.FireIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    elevation: Dp = 2.dp,
    shape: RoundedCornerShape = FireShapes.Medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(FireSpacing.Medium),
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .shadow(elevation, shape, clip = false),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(FireSpacing.Small),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier
                .shadow(elevation, shape, clip = false),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(FireSpacing.Small),
                content = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireElevatedCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            shape = FireShapes.Large,
            colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = FireElevation.Level1),
            modifier = modifier,
            content = content
        )
    } else {
        ElevatedCard(
            shape = FireShapes.Large,
            colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = FireElevation.Level1),
            modifier = modifier,
            content = content
        )
    }
}

@Composable
fun FireInfoCard(
    message: String,
    modifier: Modifier = Modifier
) {
    FireCard(
        containerColor = FireColor.Info.copy(alpha = 0.12f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(FireSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = FireIcons.Info, contentDescription = "Info", tint = FireColor.Info)
            Spacer(modifier = Modifier.width(FireSpacing.MediumSmall))
            Text(text = message, style = FireTypography.Body, color = FireColor.Info)
        }
    }
}

@Composable
fun FireWarningCard(
    message: String,
    modifier: Modifier = Modifier
) {
    FireCard(
        containerColor = FireColor.Warning.copy(alpha = 0.12f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(FireSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = FireIcons.Warning, contentDescription = "Aviso", tint = FireColor.Warning)
            Spacer(modifier = Modifier.width(FireSpacing.MediumSmall))
            Text(text = message, style = FireTypography.Body, color = FireColor.Warning)
        }
    }
}

@Composable
fun FireErrorCard(
    message: String,
    modifier: Modifier = Modifier
) {
    FireCard(
        containerColor = FireColor.Error.copy(alpha = 0.12f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(FireSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = FireIcons.Error, contentDescription = "Erro", tint = FireColor.Error)
            Spacer(modifier = Modifier.width(FireSpacing.MediumSmall))
            Text(text = message, style = FireTypography.Body, color = FireColor.Error)
        }
    }
}

@Composable
fun FireSuccessCard(
    message: String,
    modifier: Modifier = Modifier
) {
    FireCard(
        containerColor = FireColor.Success.copy(alpha = 0.12f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(FireSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = FireIcons.CheckCircle, contentDescription = "Sucesso", tint = FireColor.Success)
            Spacer(modifier = Modifier.width(FireSpacing.MediumSmall))
            Text(text = message, style = FireTypography.Body, color = FireColor.Success)
        }
    }
}

@Composable
fun FireSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    FireCard(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(FireSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(FireSpacing.Medium)
        ) {
            Text(text = title, style = FireTypography.Title, color = FireColor.Primary)
            content()
        }
    }
}

@Composable
fun FireStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    FireCard(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(FireSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(FireSpacing.Small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = FireTypography.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = icon, contentDescription = title, tint = FireColor.Primary, modifier = Modifier.size(20.dp))
            }
            Text(value, style = FireTypography.Display, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun FireDashboardCard(
    title: String,
    value: String,
    icon: ImageVector,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    FireCard(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(FireSpacing.Medium)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FireSpacing.Small)
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = FireColor.Primary)
                Text(text = title, style = FireTypography.Title)
            }
            Spacer(modifier = Modifier.height(FireSpacing.Small))
            Text(text = value, style = FireTypography.Headline)
            if (description != null) {
                Spacer(modifier = Modifier.height(FireSpacing.ExtraSmall))
                Text(text = description, style = FireTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FireOccurrenceCard(
    protocolo: String,
    natureza: String,
    dataHora: String,
    cidade: String?,
    natureColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FireCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(natureColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(FireSpacing.Medium),
                verticalArrangement = Arrangement.spacedBy(FireSpacing.Small)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Talão: $protocolo", style = FireTypography.Title, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = dataHora, style = FireTypography.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(FireSpacing.Small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(natureColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = FireSpacing.Small, vertical = FireSpacing.ExtraSmall)
                    ) {
                        Text(text = natureza, style = FireTypography.Caption, color = natureColor)
                    }
                    if (!cidade.isNullOrBlank()) {
                        Text(text = "📍 $cidade", style = FireTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = FireIcons.ChevronRight,
                    contentDescription = "Detalhes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = FireSpacing.Small)
                )
            }
        }
    }
}

@Composable
fun FireVehicleCard(
    placa: String,
    marcaModelo: String?,
    proprietario: String?,
    condutor: String?,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FireCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(FireSpacing.Medium), verticalArrangement = Arrangement.spacedBy(FireSpacing.Small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Placa: $placa", style = FireTypography.Title)
                if (onDelete != null) {
                    FireIconButton(icon = FireIcons.Delete, onClick = onDelete, tint = FireColor.Error)
                }
            }
            if (!marcaModelo.isNullOrBlank()) {
                Text(text = "Veículo: $marcaModelo", style = FireTypography.Body)
            }
            if (!proprietario.isNullOrBlank()) {
                Text(text = "Proprietário: $proprietario", style = FireTypography.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!condutor.isNullOrBlank()) {
                Text(text = "Condutor: $condutor", style = FireTypography.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FireVictimCard(
    nome: String,
    idade: String?,
    severidade: String?,
    sinaisVitais: String? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FireCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(FireSpacing.Medium), verticalArrangement = Arrangement.spacedBy(FireSpacing.Small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = nome, style = FireTypography.Title)
                if (onDelete != null) {
                    FireIconButton(icon = FireIcons.Delete, onClick = onDelete, tint = FireColor.Error)
                }
            }
            if (!idade.isNullOrBlank()) {
                Text(text = "Idade: $idade anos", style = FireTypography.Body)
            }
            if (!severidade.isNullOrBlank()) {
                Text(text = "Gravidade: $severidade", style = FireTypography.Label, color = FireColor.Primary)
            }
            if (!sinaisVitais.isNullOrBlank()) {
                Text(text = "Sinais: $sinaisVitais", style = FireTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FireDocumentCard(
    tipo: String,
    numero: String?,
    nomePessoa: String?,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FireCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(FireSpacing.Medium), verticalArrangement = Arrangement.spacedBy(FireSpacing.Small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = tipo, style = FireTypography.Title)
                if (onDelete != null) {
                    FireIconButton(icon = FireIcons.Delete, onClick = onDelete, tint = FireColor.Error)
                }
            }
            if (!numero.isNullOrBlank()) {
                Text(text = "Nº: $numero", style = FireTypography.Body)
            }
            if (!nomePessoa.isNullOrBlank()) {
                Text(text = "Nome: $nomePessoa", style = FireTypography.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FireMilitaryCard(
    re: String,
    nome: String,
    funcao: String?,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FireCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(FireSpacing.Medium), verticalArrangement = Arrangement.spacedBy(FireSpacing.Small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = nome, style = FireTypography.Title)
                if (onDelete != null) {
                    FireIconButton(icon = FireIcons.Delete, onClick = onDelete, tint = FireColor.Error)
                }
            }
            Text(text = "RE: $re", style = FireTypography.Body)
            if (!funcao.isNullOrBlank()) {
                Text(text = "Função: $funcao", style = FireTypography.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FireSupportCard(
    orgao: String,
    viatura: String?,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FireCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(FireSpacing.Medium), verticalArrangement = Arrangement.spacedBy(FireSpacing.Small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = orgao, style = FireTypography.Title)
                if (onDelete != null) {
                    FireIconButton(icon = FireIcons.Delete, onClick = onDelete, tint = FireColor.Error)
                }
            }
            if (!viatura.isNullOrBlank()) {
                Text(text = "Viatura: $viatura", style = FireTypography.Body)
            }
        }
    }
}

@Composable
fun FireImageCard(
    path: String,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FireCard(
        onClick = onClick,
        modifier = modifier.size(120.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Imagem",
                style = FireTypography.Caption,
                modifier = Modifier.align(Alignment.Center)
            )
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(imageVector = FireIcons.Close, contentDescription = "Delete", tint = FireColor.Error)
                }
            }
        }
    }
}

@Composable
fun FireAttachmentCard(
    fileName: String,
    fileType: String,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FireCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(FireSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = FireIcons.Description, contentDescription = "Attachment", tint = FireColor.Primary)
                Spacer(modifier = Modifier.width(FireSpacing.Small))
                Column {
                    Text(text = fileName, style = FireTypography.Title)
                    Text(text = fileType, style = FireTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (onDelete != null) {
                FireIconButton(icon = FireIcons.Delete, onClick = onDelete, tint = FireColor.Error)
            }
        }
    }
}
