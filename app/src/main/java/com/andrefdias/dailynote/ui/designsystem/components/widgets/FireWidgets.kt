package com.andrefdias.dailynote.ui.designsystem.components.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.andrefdias.dailynote.ui.designsystem.colors.FireColor
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.shapes.FireShapes
import com.andrefdias.dailynote.ui.designsystem.icons.FireIcons
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign

@Composable
fun FireWizardProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { currentStep.toFloat() / totalSteps.toFloat() },
        color = FireColor.Primary,
        trackColor = FireColor.Divider,
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(FireShapes.Small)
    )
}

@Composable
fun FireStepper(
    currentStep: Int,
    steps: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(FireSpacing.Small)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, step ->
                val color = if (index <= currentStep) FireColor.Primary else Color.Gray
                Text(
                    text = "${index + 1}. $step",
                    style = FireTypography.Caption,
                    color = color,
                    fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        FireWizardProgress(currentStep = currentStep + 1, totalSteps = steps.size)
    }
}

@Composable
fun FireProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { progress },
        color = FireColor.Primary,
        trackColor = FireColor.Divider,
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(FireShapes.Small)
    )
}

@Composable
fun FirePhotoPreview(
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .background(Color.LightGray, FireShapes.Medium)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Foto", style = FireTypography.Caption)
        if (onDelete != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(imageVector = FireIcons.Close, contentDescription = "Excluir", tint = FireColor.Error)
            }
        }
    }
}

@Composable
fun FireImageViewer(
    path: String,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color.Black, FireShapes.Large)
        ) {
            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(imageVector = FireIcons.Close, contentDescription = "Fechar", tint = Color.White)
            }
            Text(
                text = "Visualização de Imagem\n$path",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun FireStatusChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(backgroundColor, FireShapes.Small)
            .padding(horizontal = FireSpacing.Small, vertical = FireSpacing.ExtraSmall)
    ) {
        Text(text = text, style = FireTypography.Caption, color = textColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FireBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(FireColor.Primary, CircleShape)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, style = FireTypography.Caption, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FireAvatar(
    initials: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(FireColor.Secondary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = initials, style = FireTypography.Title, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FireDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier,
        color = FireColor.Divider.copy(alpha = 0.5f),
        thickness = 1.dp
    )
}

@Composable
fun FireTag(
    text: String,
    color: Color = FireColor.Primary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), FireShapes.Small)
            .padding(horizontal = FireSpacing.Small, vertical = FireSpacing.ExtraSmall)
    ) {
        Text(text = text, style = FireTypography.Caption, color = color, fontWeight = FontWeight.Bold)
    }
}


@Composable
fun FireTooltip(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
    }
}

@Composable
fun FireSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    icon: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = FireSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(FireSpacing.ExtraSmall)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Text(icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(FireSpacing.Small))
            }
            Text(
                text = title,
                style = FireTypography.HeadlineSmall,
                color = FireColors.OnBackground
            )
        }
        subtitle?.let {
            Text(
                text = it,
                style = FireTypography.BodyMedium,
                color = FireColors.OnSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireStatItem(
    label: String,
    value: Int,
    icon: String,
    color: Color = FireColors.Primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(1.dp, FireShapes.Small, clip = false),
        shape = FireShapes.Small,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FireSpacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Text(
                text = value.toString(),
                style = FireTypography.HeadlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = FireTypography.LabelMedium,
                color = FireColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
fun FireRankingItem(
    rank: Int,
    primaryText: String,
    secondaryText: String,
    value: Int,
    color: Color = FireColors.Primary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = FireSpacing.ExtraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    if (rank <= 3) color.copy(alpha = 0.15f) else Color.Transparent,
                    FireShapes.Circle
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (rank) {
                    1 -> "🥇🥇"
                    2 -> "🥈🥈"
                    3 -> "🥉🥉"
                    else -> rank.toString()
                },
                fontSize = if (rank <= 3) 18.sp else 14.sp,
                fontWeight = if (rank <= 3) FontWeight.Bold else FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.width(FireSpacing.Medium))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = primaryText,
                style = FireTypography.BodyMedium,
                fontWeight = FontWeight.Medium,
                color = FireColors.OnBackground
            )
            Text(
                text = secondaryText,
                style = FireTypography.BodySmall,
                color = FireColors.OnSurfaceVariant
            )
        }
        
        Surface(
            shape = FireShapes.Small,
            color = color.copy(alpha = 0.15f)
        ) {
            Text(
                text = value.toString(),
                style = FireTypography.LabelLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(
                    horizontal = FireSpacing.Medium,
                    vertical = FireSpacing.ExtraSmall
                )
            )
        }
    }
}
