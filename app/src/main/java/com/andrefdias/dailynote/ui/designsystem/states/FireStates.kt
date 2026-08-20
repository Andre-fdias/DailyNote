package com.andrefdias.dailynote.ui.designsystem.states

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.shadow
import com.andrefdias.dailynote.ui.designsystem.colors.FireColor
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.shapes.FireShapes
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import com.andrefdias.dailynote.ui.designsystem.components.buttons.FireButton

@Composable
fun FireLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(FireSpacing.Medium),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = FireColors.Primary)
    }
}

@Composable
fun FireEmptyState(
    message: String,
    icon: String = "📭📭",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, FireShapes.Medium, clip = false),
        shape = FireShapes.Medium,
        colors = CardDefaults.cardColors(containerColor = FireColors.Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FireSpacing.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FireSpacing.Medium)
        ) {
            Text(icon, fontSize = 48.sp)
            Text(
                text = message,
                style = FireTypography.BodyLarge,
                color = FireColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FireErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(FireSpacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FireSpacing.Medium)
    ) {
        Text(text = message, style = FireTypography.Title, color = FireColors.Error)
        FireButton(text = "Tentar Novamente", onClick = onRetry)
    }
}
