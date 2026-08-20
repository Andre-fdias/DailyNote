package com.andrefdias.dailynote

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.andrefdias.dailynote.ui.screens.settings.SettingsScreen
import com.andrefdias.dailynote.ui.screens.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Bottom navigation items
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Início", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.ResumoOperacional, "Equipe", Icons.Filled.Assignment, Icons.Outlined.Assignment),
    BottomNavItem(Screen.Settings, "Config.", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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

            FireNotesTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Show bottom bar only on top-level screens
                val showBottomBar = bottomNavItems.any {
                    currentDestination?.hierarchy?.any { dest -> dest.route == it.screen.route } == true
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp
                            ) {
                                bottomNavItems.forEach { item ->
                                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            navController.navigate(item.screen.route) {
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
                                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)) },
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
                        composable(Screen.Quartel.route) {
                            QuartelScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
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
                                onNavigateToCalendarSettings = {
                                    navController.navigate(Screen.SettingsCalendar.route)
                                },
                                onNavigateToGoogleSync = {
                                    navController.navigate(Screen.GoogleSync.route)
                                }
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
                }
            }
        }
    }
}
