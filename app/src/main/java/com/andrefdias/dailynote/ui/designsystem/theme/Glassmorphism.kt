package com.andrefdias.dailynote.ui.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphism(
    cornerRadius: Dp = 16.dp,
    blurRadius: Dp = 16.dp, // For blur effect if you implement RenderEffect
    backgroundColor: Color = Color.White.copy(alpha = 0.1f),
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    elevation: Dp = 4.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = RoundedCornerShape(cornerRadius),
        spotColor = Color.Black.copy(alpha = 0.1f),
        ambientColor = Color.Black.copy(alpha = 0.1f)
    )
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                backgroundColor,
                backgroundColor.copy(alpha = backgroundColor.alpha * 0.5f)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                borderColor,
                borderColor.copy(alpha = borderColor.alpha * 0.1f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
