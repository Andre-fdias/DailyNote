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
    // --- MAIN ROOTS ---
    object Home : Screen(route = "home", title = "Início", icon = Icons.Outlined.Home, selectedIcon = Icons.Filled.Home, showInBottomBar = true)
    object AgendaRoot : Screen(route = "agenda_root", title = "Agenda", icon = Icons.Outlined.CalendarToday, selectedIcon = Icons.Filled.CalendarToday, showInBottomBar = true)
    object MapaForcaRoot : Screen(route = "mapa_forca_root", title = "Mapa Força", icon = Icons.Outlined.Map, selectedIcon = Icons.Filled.Map, showInBottomBar = true)
    object OcorrenciasRoot : Screen(route = "ocorrencias_root", title = "Ocorrências", icon = Icons.Outlined.LocalPolice, selectedIcon = Icons.Filled.LocalPolice, showInBottomBar = true)
    object Settings : Screen(route = "settings", title = "Configurações", icon = Icons.Outlined.Settings, selectedIcon = Icons.Filled.Settings, showInBottomBar = true)

    // --- AGENDA CONTEXT ---
    object AgendaCalendario : Screen("agenda_calendario", "Calendário", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth, true)
    object AgendaTarefas : Screen("agenda_tarefas", "Tarefas", Icons.Outlined.Checklist, Icons.Filled.Checklist, true)
    object AgendaEventos : Screen("agenda_eventos", "Eventos", Icons.Outlined.Event, Icons.Filled.Event, true)

    // --- MAPA CONTEXT ---
    object MapaDia : Screen("mapa_dia", "Mapa", Icons.Outlined.Today, Icons.Filled.Today, true)
    object EquipeServico : Screen("equipe_servico", "Compor", Icons.Outlined.Assignment, Icons.Filled.Assignment, true)
    object HistoricoDashboard : Screen("historico_dashboard", "Histórico Ocorrências", Icons.Outlined.LocalPolice, Icons.Filled.LocalPolice, true)
    object HistoricoMapaForca : Screen("historico_mapa_forca", "Histórico Mapa Força", Icons.Outlined.Analytics, Icons.Filled.Analytics, true)
    object Militar : Screen("militar", "Militares", Icons.Outlined.People, Icons.Filled.People, true)
    object Viatura : Screen("viatura", "Viaturas", Icons.Outlined.DirectionsCar, Icons.Filled.DirectionsCar, true)
    object Quartel : Screen("quartel", "Postos", Icons.Outlined.Domain, Icons.Filled.Domain, true)
    
    object ResumoOperacional : Screen("resumo_operacional", "Resumo Operacional", Icons.Outlined.Description, Icons.Filled.Description, false)

    // --- OCORRENCIAS CONTEXT ---
    object OcorrenciasEmConstrucao : Screen("ocorrencias_em_construcao", "Em Construção", Icons.Outlined.Construction, Icons.Filled.Construction, false)

    // --- CONFIG CONTEXT (e misc) ---
    object SettingsCalendar : Screen("settings_calendar", "Configuração de Calendário", Icons.Outlined.Settings, Icons.Filled.Settings, false)
    object GoogleSync : Screen("google_sync", "Sincronização do Google", Icons.Outlined.Sync, Icons.Filled.Sync, false)

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
