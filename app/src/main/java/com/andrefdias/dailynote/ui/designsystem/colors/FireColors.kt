package com.andrefdias.dailynote.ui.designsystem.colors

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object FireColors {
    // Light values
    private val lightPrimary = Color(0xFF1A73E8)      // Azul principal original
    private val lightPrimaryLight = Color(0xFFE8F0FE)
    private val lightPrimaryDark = Color(0xFF1557B0)
    private val lightSecondary = Color(0xFF34A853)    // Verde
    private val lightSecondaryLight = Color(0xFFE6F4EA)
    private val lightSecondaryDark = Color(0xFF1E7E34)
    private val lightTertiary = Color(0xFFFBBC04)     // Amarelo
    private val lightTertiaryLight = Color(0xFFFEF7E0)
    private val lightSuccess = Color(0xFF34A853)
    private val lightWarning = Color(0xFFFBBC04)
    private val lightError = Color(0xFFEA4335)
    private val lightInfo = Color(0xFF1A73E8)
    private val lightBackground = Color(0xFFF8F9FA)
    private val lightSurface = Color(0xFFFFFFFF)
    private val lightSurfaceVariant = Color(0xFFF1F3F4)
    private val lightOnBackground = Color(0xFF202124)
    private val lightOnSurface = Color(0xFF3C4043)
    private val lightOnSurfaceVariant = Color(0xFF5F6368)
 
    // Dark values
    private val darkPrimary = Color(0xFF8AB4F8)
    private val darkPrimaryLight = Color(0xFF185ABC)
    private val darkPrimaryDark = Color(0xFF1557B0)
    private val darkSecondary = Color(0xFF81C995)
    private val darkSecondaryLight = Color(0xFF137333)
    private val darkSecondaryDark = Color(0xFF1E7E34)
    private val darkTertiary = Color(0xFFFDE293)
    private val darkTertiaryLight = Color(0xFFB06000)
    private val darkSuccess = Color(0xFF81C995)
    private val darkWarning = Color(0xFFFDE293)
    private val darkError = Color(0xFFF28B82)
    private val darkInfo = Color(0xFF8AB4F8)
    private val darkBackground = Color(0xFF202124)
    private val darkSurface = Color(0xFF2D2E30)
    private val darkSurfaceVariant = Color(0xFF3C4043)
    private val darkOnBackground = Color(0xFFE8EAED)
    private val darkOnSurface = Color(0xFFF1F3F4)
    private val darkOnSurfaceVariant = Color(0xFFBCC1C6)

    // Compose State to make color lookup reactive
    var isDarkState by mutableStateOf(false)

    // Getters that return light or dark color based on isDarkState
    val Primary: Color get() = if (isDarkState) darkPrimary else lightPrimary
    val PrimaryLight: Color get() = if (isDarkState) darkPrimaryLight else lightPrimaryLight
    val PrimaryDark: Color get() = if (isDarkState) darkPrimaryDark else lightPrimaryDark
    val Secondary: Color get() = if (isDarkState) darkSecondary else lightSecondary
    val SecondaryLight: Color get() = if (isDarkState) darkSecondaryLight else lightSecondaryLight
    val SecondaryDark: Color get() = if (isDarkState) darkSecondaryDark else lightSecondaryDark
    val Tertiary: Color get() = if (isDarkState) darkTertiary else lightTertiary
    val TertiaryLight: Color get() = if (isDarkState) darkTertiaryLight else lightTertiaryLight
    val Success: Color get() = if (isDarkState) darkSuccess else lightSuccess
    val Warning: Color get() = if (isDarkState) darkWarning else lightWarning
    val Error: Color get() = if (isDarkState) darkError else lightError
    val Info: Color get() = if (isDarkState) darkInfo else lightInfo
    val Background: Color get() = if (isDarkState) darkBackground else lightBackground
    val Surface: Color get() = if (isDarkState) darkSurface else lightSurface
    val SurfaceVariant: Color get() = if (isDarkState) darkSurfaceVariant else lightSurfaceVariant
    val OnBackground: Color get() = if (isDarkState) darkOnBackground else lightOnBackground
    val OnSurface: Color get() = if (isDarkState) darkOnSurface else lightOnSurface
    val OnSurfaceVariant: Color get() = if (isDarkState) darkOnSurfaceVariant else lightOnSurfaceVariant

    // Static category colors (adjusted for better contrast in both light/dark if needed, but kept distinct)
    val NaturezaIncendio = Color(0xFFEA4335)
    val NaturezaSalvamento = Color(0xFF34A853)
    val NaturezaAcidente = Color(0xFFFBBC04)
    val NaturezaQueda = Color(0xFF8B5A2B)
    val NaturezaPessoal = Color(0xFF9C27B0)

    val Purple = Color(0xFF9C27B0)
    val Brown = Color(0xFF8B5A2B)
}
