package com.andrefdias.dailynote.ui.designsystem.colors

import androidx.compose.ui.graphics.Color

@Deprecated("Use FireColors instead", ReplaceWith("FireColors", "com.andrefdias.dailynote.ui.designsystem.colors.FireColors"))
object FireColor {
    val Primary = FireColors.Primary
    val Secondary = FireColors.Secondary
    val SecondaryLight = FireColors.SecondaryLight
    val Error = FireColors.Error
    val Warning = FireColors.Warning
    val Success = FireColors.Success
    val Info = FireColors.Info
    
    // Utility Colors
    val Divider = Color(0xFFCCCCCC)
    val Disabled = Color(0xFF888888)
    val Outline = Color(0xFF777777)
    
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkCard = Color(0xFF252525)
    val DarkText = Color(0xFFFFFFFF)
    
    val LightBackground = FireColors.Background
    val LightSurface = FireColors.Surface
    val LightCard = FireColors.SurfaceVariant
    val LightText = FireColors.OnBackground
}
