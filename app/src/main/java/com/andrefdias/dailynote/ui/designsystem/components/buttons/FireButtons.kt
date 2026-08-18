package com.andrefdias.dailynote.ui.designsystem.components.buttons

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.shapes.FireShapes
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.andrefdias.dailynote.ui.designsystem.colors.FireColor

@Composable
fun FireButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = FireColors.Primary,
    contentColor: Color = Color.White,
    icon: ImageVector? = null,
    shape: androidx.compose.foundation.shape.RoundedCornerShape = FireShapes.Medium,
    height: Dp = 48.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(height)
            .shadow(
                elevation = if (enabled) 2.dp else 0.dp,
                shape = shape,
                clip = false
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = shape
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(FireSpacing.Small))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun FireOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color = FireColor.Primary,
    contentColor: Color = FireColor.Primary,
    icon: ImageVector? = null,
    height: Dp = 48.dp
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = FireShapes.Medium,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
        modifier = modifier.height(height)
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(FireSpacing.Small))
            }
            Text(text = text, style = FireTypography.Title)
        }
    }
}

@Composable
fun FireTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = FireColor.Primary
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
        modifier = modifier
    ) {
        Text(text = text, style = FireTypography.Title)
    }
}

@Composable
fun FireIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = FireColor.Primary
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
    }
}

@Composable
fun FireFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = FireColor.Primary,
    contentColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = contentColor,
        shape = FireShapes.Large,
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun FireFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = FireColor.Primary,
    contentColor: Color = Color.White
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = contentColor,
        shape = FireShapes.Large,
        modifier = modifier
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
    }
}
