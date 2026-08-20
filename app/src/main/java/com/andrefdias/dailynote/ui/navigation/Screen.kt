package com.andrefdias.dailynote.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val selectedIcon: ImageVector? = null,
    val showInBottomBar: Boolean = false
) {
    object Home : Screen(
        route = "home",
        title = "Início",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        showInBottomBar = true
    )
    
    object Settings : Screen(
        route = "settings",
        title = "Configurações",
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
        showInBottomBar = true
    )

    object SettingsCalendar : Screen(
        route = "settings_calendar",
        title = "Configuração de Calendário",
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
        showInBottomBar = false
    )

    object GoogleSync : Screen(
        route = "google_sync",
        title = "Sincronização do Google",
        icon = Icons.Outlined.Sync,
        selectedIcon = Icons.Filled.Sync,
        showInBottomBar = false
    )

    object Quartel : Screen(
        route = "quartel",
        title = "Quartel",
        icon = Icons.Outlined.Domain,
        selectedIcon = Icons.Filled.Domain,
        showInBottomBar = false
    )

    object Viatura : Screen(
        route = "viatura",
        title = "Viaturas",
        icon = Icons.Outlined.DirectionsCar,
        selectedIcon = Icons.Filled.DirectionsCar,
        showInBottomBar = false
    )

    object Militar : Screen(
        route = "militar",
        title = "Militares",
        icon = Icons.Outlined.People,
        selectedIcon = Icons.Filled.People,
        showInBottomBar = false
    )

    object EquipeServico : Screen(
        route = "equipe_servico",
        title = "Equipe de Serviço",
        icon = Icons.Outlined.Assignment,
        selectedIcon = Icons.Filled.Assignment,
        showInBottomBar = true
    )

    object ResumoOperacional : Screen(
        route = "resumo_operacional",
        title = "Resumo Operacional",
        icon = Icons.Outlined.Description,
        selectedIcon = Icons.Filled.Description,
        showInBottomBar = true
    )

    object CalendarWizard : Screen(
        route = "calendar_wizard?escalaId={escalaId}",
        title = "Assistente de Calendário",
        icon = Icons.Outlined.CalendarMonth,
        selectedIcon = Icons.Filled.CalendarMonth,
        showInBottomBar = false
    ) {
        fun createRoute(escalaId: String? = null): String {
            return if (escalaId != null) "calendar_wizard?escalaId=$escalaId" else "calendar_wizard"
        }
    }
}
