package com.andrefdias.dailynote.ui.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors

private val LightColorScheme = lightColorScheme(
    primary = FireColors.Primary,
    primaryContainer = FireColors.PrimaryLight,
    secondary = FireColors.Secondary,
    secondaryContainer = FireColors.SecondaryLight,
    tertiary = FireColors.Tertiary,
    tertiaryContainer = FireColors.TertiaryLight,
    background = FireColors.Background,
    surface = FireColors.Surface,
    surfaceVariant = FireColors.SurfaceVariant,
    onBackground = FireColors.OnBackground,
    onSurface = FireColors.OnSurface,
    onSurfaceVariant = FireColors.OnSurfaceVariant,
    error = FireColors.Error
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    primaryContainer = Color(0xFF185ABC),
    secondary = Color(0xFF81C995),
    secondaryContainer = Color(0xFF137333),
    tertiary = Color(0xFFFDE293),
    tertiaryContainer = Color(0xFFB06000),
    background = Color(0xFF202124),
    surface = Color(0xFF2D2E30),
    surfaceVariant = Color(0xFF3C4043),
    onBackground = Color(0xFFE8EAED),
    onSurface = Color(0xFFF1F3F4),
    onSurfaceVariant = Color(0xFFBCC1C6),
    error = Color(0xFFF28B82)
)

@Composable
fun FireNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    FireColors.isDarkState = darkTheme
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
