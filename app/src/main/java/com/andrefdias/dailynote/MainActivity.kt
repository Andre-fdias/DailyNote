package com.andrefdias.dailynote

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.andrefdias.dailynote.domain.calendar.GoogleCalendarSyncManager
import com.andrefdias.dailynote.domain.calendar.NotificationCenter
import com.andrefdias.dailynote.ui.designsystem.theme.FireNotesTheme
import com.andrefdias.dailynote.ui.navigation.Screen
import com.andrefdias.dailynote.ui.screens.calendar.CalendarViewModel
import com.andrefdias.dailynote.ui.screens.calendar.CalendarWizardScreen
import com.andrefdias.dailynote.ui.screens.calendar.CalendarWizardViewModel
import com.andrefdias.dailynote.ui.screens.calendar.GoogleSyncScreen
import com.andrefdias.dailynote.ui.screens.calendar.SettingsCalendarScreen
import com.andrefdias.dailynote.ui.screens.calendar.SettingsCalendarViewModel
import com.andrefdias.dailynote.ui.screens.home.HomeScreen
import com.andrefdias.dailynote.ui.screens.militar.MilitarScreen
import com.andrefdias.dailynote.ui.screens.quartel.QuartelScreen
import com.andrefdias.dailynote.ui.screens.viatura.ViaturaScreen
import com.andrefdias.dailynote.ui.screens.equipe.EquipeServicoScreen
import com.andrefdias.dailynote.ui.screens.historico.HistoricoDashboardScreen
import com.andrefdias.dailynote.ui.screens.settings.SettingsScreen
import com.andrefdias.dailynote.ui.screens.settings.SettingsViewModel
import com.andrefdias.dailynote.ui.screens.ocorrencias.OcorrenciasEmConstrucaoScreen
import com.andrefdias.dailynote.ui.screens.resumo.ResumoOperacionalScreen
import com.andrefdias.dailynote.ui.screens.agenda.AgendaCalendarioScreen
import com.andrefdias.dailynote.ui.screens.agenda.AgendaTarefasScreen
import com.andrefdias.dailynote.ui.screens.agenda.AgendaEventosScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val mainNavItems = listOf(
    BottomNavItem(Screen.Home, "Início", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.AgendaRoot, "Agenda", Icons.Filled.CalendarToday, Icons.Outlined.CalendarToday),
    BottomNavItem(Screen.MapaForcaRoot, "Mapa Força", Icons.Filled.Map, Icons.Outlined.Map),
    BottomNavItem(Screen.OcorrenciasRoot, "Ocorrências", Icons.Filled.LocalPolice, Icons.Outlined.LocalPolice),
    BottomNavItem(Screen.Settings, "Config.", Icons.Filled.Settings, Icons.Outlined.Settings)
)

private val agendaNavItems = listOf(
    BottomNavItem(Screen.Home, "Início", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.AgendaCalendario, "Calendário", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    BottomNavItem(Screen.AgendaTarefas, "Tarefas", Icons.Filled.Checklist, Icons.Outlined.Checklist),
    BottomNavItem(Screen.AgendaEventos, "Eventos", Icons.Filled.Event, Icons.Outlined.Event)
)

private val mapaForcaNavItems = listOf(
    BottomNavItem(Screen.Home, "Início", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.MapaDia, "Mapa", Icons.Filled.Today, Icons.Outlined.Today),
    BottomNavItem(Screen.EquipeServico, "Compor", Icons.Filled.Assignment, Icons.Outlined.Assignment),
    BottomNavItem(Screen.HistoricoMapaForca, "Histórico", Icons.Filled.History, Icons.Outlined.History),
    BottomNavItem(Screen.Militar, "Militares", Icons.Filled.People, Icons.Outlined.People),
    BottomNavItem(Screen.Viatura, "Viaturas", Icons.Filled.DirectionsCar, Icons.Outlined.DirectionsCar),
    BottomNavItem(Screen.Quartel, "Postos", Icons.Filled.Domain, Icons.Outlined.Domain)
)

private val historicoNavItems = listOf(
    BottomNavItem(Screen.MapaForcaRoot, "Voltar", Icons.Filled.ArrowBack, Icons.Outlined.ArrowBack),
    BottomNavItem(Screen.HistoricoMapaForca, "Mapa Força", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    BottomNavItem(Screen.HistoricoDashboard, "Ocorrência", Icons.Filled.LocalPolice, Icons.Outlined.LocalPolice)
)

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var settingsRepository: com.andrefdias.dailynote.domain.repository.SettingsRepository

    @Inject
    lateinit var googleCalendarSyncManager: GoogleCalendarSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.andrefdias.dailynote.util.LogHelper.init(applicationContext)

        NotificationCenter.initNotificationChannels(this)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            val theme by settingsRepository.themeFlow.collectAsState(initial = "Automático")
            val isDarkTheme = when (theme) {
                "Claro" -> false
                "Escuro" -> true
                else -> isSystemInDarkTheme()
            }
            
            val pinEnabled by settingsRepository.pinEnabledFlow.collectAsState(initial = false)
            val biometricEnabled by settingsRepository.biometricEnabledFlow.collectAsState(initial = false)
            val savedPin by settingsRepository.pinCodeFlow.collectAsState(initial = "")
            
            var isAuthenticated by remember { mutableStateOf(false) }
            val needsAuth = pinEnabled || biometricEnabled
            
            LaunchedEffect(needsAuth) {
                if (!needsAuth) isAuthenticated = true
            }

            FireNotesTheme(darkTheme = isDarkTheme) {
                if (!isAuthenticated && needsAuth) {
                    com.andrefdias.dailynote.ui.screens.auth.AuthScreen(
                        activity = this@MainActivity,
                        pinEnabled = pinEnabled,
                        biometricEnabled = biometricEnabled,
                        savedPin = savedPin,
                        onAuthenticated = { isAuthenticated = true }
                    )
                } else {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val currentRoute = currentDestination?.route

                    val activeNavItems = when {
                        currentRoute in listOf(
                            Screen.AgendaCalendario.route,
                            Screen.AgendaTarefas.route,
                            Screen.AgendaEventos.route
                        ) -> agendaNavItems
                    
                    currentRoute in listOf(
                        Screen.HistoricoMapaForca.route,
                        Screen.HistoricoDashboard.route
                    ) -> historicoNavItems
                    
                    currentRoute in listOf(
                        Screen.MapaDia.route,
                        Screen.EquipeServico.route,
                        Screen.Militar.route,
                        Screen.Viatura.route,
                        Screen.Quartel.route
                    ) -> mapaForcaNavItems
                    
                    currentRoute in listOf(
                        Screen.Home.route, 
                        Screen.Settings.route, 
                        Screen.OcorrenciasEmConstrucao.route
                    ) -> mainNavItems
                    
                    else -> null
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (activeNavItems != null) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp
                            ) {
                                activeNavItems.forEach { item ->
                                    val mappedRoute = when (item.screen) {
                                        Screen.AgendaRoot -> Screen.AgendaCalendario.route
                                        Screen.MapaForcaRoot -> Screen.MapaDia.route
                                        Screen.OcorrenciasRoot -> Screen.OcorrenciasEmConstrucao.route
                                        else -> item.screen.route
                                    }
                                    
                                    val isSelected = currentDestination?.hierarchy?.any { it.route == mappedRoute } == true
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            navController.navigate(mappedRoute) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = { 
                                            // Reduce font size even more if we have many items (Mapa Força has 7)
                                            val fontSize = if (activeNavItems.size > 5) 8.sp else 10.sp
                                            Text(
                                                text = item.label, 
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = fontSize),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            ) 
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            val calendarViewModel: CalendarViewModel = hiltViewModel()
                            HomeScreen(
                                viewModel = calendarViewModel,
                                onNavigateToWizard = { navController.navigate(Screen.CalendarWizard.route) },
                                onNavigateToConsult = { navController.navigate(Screen.Settings.route) }
                            )
                        }
                        composable(Screen.AgendaCalendario.route) {
                            AgendaCalendarioScreen()
                        }
                        composable(Screen.AgendaTarefas.route) {
                            AgendaTarefasScreen()
                        }
                        composable(Screen.AgendaEventos.route) {
                            AgendaEventosScreen()
                        }
                        composable(Screen.MapaDia.route) {
                            ResumoOperacionalScreen(
                                onNavigateToEquipe = { navController.navigate(Screen.EquipeServico.route) }
                            )
                        }
                        composable(Screen.OcorrenciasEmConstrucao.route) {
                            OcorrenciasEmConstrucaoScreen()
                        }
                        composable(Screen.Quartel.route) {
                            QuartelScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.HistoricoDashboard.route) {
                            HistoricoDashboardScreen()
                        }
                        composable(Screen.HistoricoMapaForca.route) {
                            com.andrefdias.dailynote.ui.screens.historico.HistoricoMapaForcaScreen()
                        }
                        composable(Screen.Viatura.route) {
                            ViaturaScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Militar.route) {
                            MilitarScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.EquipeServico.route) {
                            EquipeServicoScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToCadastrarViatura = { navController.navigate(Screen.Viatura.route) },
                                onNavigateToCadastrarMilitar = { navController.navigate(Screen.Militar.route) },
                                onNavigateToCadastrarQuartel = { navController.navigate(Screen.Quartel.route) }
                            )
                        }
                        composable(Screen.ResumoOperacional.route) {
                            com.andrefdias.dailynote.ui.screens.resumo.ResumoOperacionalScreen(
                                onNavigateToEquipe = { navController.navigate(Screen.EquipeServico.route) }
                            )
                        }
                        composable(Screen.Settings.route) {
                            val settingsViewModel: SettingsViewModel = hiltViewModel()
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToWizard = { navController.navigate(Screen.CalendarWizard.route) }
                            )
                        }
                        composable(Screen.SettingsCalendar.route) {
                            val settingsCalendarViewModel: SettingsCalendarViewModel = hiltViewModel()
                            SettingsCalendarScreen(
                                viewModel = settingsCalendarViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToGoogleSync = {
                                    navController.navigate(Screen.GoogleSync.route)
                                },
                                onNavigateToWizard = { escalaId ->
                                    navController.navigate(Screen.CalendarWizard.createRoute(escalaId))
                                }
                            )
                        }
                        composable(
                            route = Screen.CalendarWizard.route,
                            arguments = listOf(androidx.navigation.navArgument("escalaId") {
                                type = androidx.navigation.NavType.StringType
                                nullable = true
                                defaultValue = null
                            })
                        ) {
                            val wizardViewModel: CalendarWizardViewModel = hiltViewModel()
                            CalendarWizardScreen(
                                viewModel = wizardViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.GoogleSync.route) {
                            GoogleSyncScreen(
                                syncManager = googleCalendarSyncManager,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        }
                    } // Fechamento do NavHost
                } // Fechamento do else
            } // Fechamento do FireNotesTheme
        } // Fechamento do setContent
    }
}
