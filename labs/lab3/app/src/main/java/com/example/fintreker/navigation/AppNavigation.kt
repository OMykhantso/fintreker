package com.example.fintreker.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.fintreker.ui.screens.AddExpenseScreen
import com.example.fintreker.ui.screens.AnalyticsScreen
import com.example.fintreker.ui.screens.HistoryScreen
import com.example.fintreker.ui.screens.HomeScreen
import com.example.fintreker.ui.screens.SettingsScreen
import com.example.fintreker.ui.viewmodels.AddExpenseViewModel
import com.example.fintreker.ui.viewmodels.AnalyticsViewModel
import com.example.fintreker.ui.viewmodels.HistoryViewModel
import com.example.fintreker.ui.viewmodels.HomeViewModel
import com.example.fintreker.ui.viewmodels.SettingsViewModel
import kotlin.reflect.KClass

private const val TAB_ANIMATION_MS = 250
private const val SCREEN_ANIMATION_MS = 320

/** Вкладки нижньої панелі навігації (Лаб 2, AI-завдання: Bottom Navigation Bar). */
private data class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val label: String,
    val icon: ImageVector
)

private val topLevelDestinations = listOf(
    TopLevelDestination(HomeRoute, HomeRoute::class, "Огляд", Icons.Filled.Home),
    TopLevelDestination(ExpenseHistoryRoute(), ExpenseHistoryRoute::class, "Історія", Icons.Filled.History),
    TopLevelDestination(AnalyticsRoute, AnalyticsRoute::class, "Аналітика", Icons.Filled.PieChart),
    TopLevelDestination(SettingsRoute, SettingsRoute::class, "Налаштування", Icons.Filled.Settings)
)

@Composable
fun AppNavigation(
    factory: ViewModelProvider.Factory,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = topLevelDestinations.any { top ->
        currentDestination?.hierarchy?.any { it.hasRoute(top.routeClass) } == true
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                NavigationBar {
                    topLevelDestinations.forEach { top ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.hasRoute(top.routeClass) } == true,
                            onClick = { navController.navigateToTopLevel(top.route) },
                            icon = { Icon(top.icon, contentDescription = null) },
                            label = { Text(top.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            // Вкладки перемикаються плавним fade; "глибокі" екрани мають власний slide нижче.
            enterTransition = { fadeIn(tween(TAB_ANIMATION_MS)) },
            exitTransition = { fadeOut(tween(TAB_ANIMATION_MS)) },
            popEnterTransition = { fadeIn(tween(TAB_ANIMATION_MS)) },
            popExitTransition = { fadeOut(tween(TAB_ANIMATION_MS)) }
        ) {
            composable<HomeRoute> {
                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                HomeScreen(
                    homeViewModel = homeViewModel,
                    onAddExpense = { navController.navigate(AddExpenseRoute) },
                    onOpenHistory = { navController.navigateToTopLevel(ExpenseHistoryRoute()) }
                )
            }

            composable<AddExpenseRoute>(
                enterTransition = { slideInHorizontally(tween(SCREEN_ANIMATION_MS)) { it } },
                exitTransition = { slideOutHorizontally(tween(SCREEN_ANIMATION_MS)) { -it } },
                popEnterTransition = { slideInHorizontally(tween(SCREEN_ANIMATION_MS)) { -it } },
                popExitTransition = { slideOutHorizontally(tween(SCREEN_ANIMATION_MS)) { it } }
            ) {
                val addExpenseViewModel: AddExpenseViewModel = viewModel(factory = factory)
                AddExpenseScreen(
                    viewModel = addExpenseViewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable<ExpenseHistoryRoute> { entry ->
                val route: ExpenseHistoryRoute = entry.toRoute()
                val historyViewModel: HistoryViewModel = viewModel(factory = factory)
                HistoryScreen(
                    viewModel = historyViewModel,
                    initialCategory = route.category
                )
            }

            composable<AnalyticsRoute> {
                val analyticsViewModel: AnalyticsViewModel = viewModel(factory = factory)
                AnalyticsScreen(
                    viewModel = analyticsViewModel,
                    // Типізований перехід з параметром: ExpenseHistoryRoute(category)
                    onCategoryClick = { category -> navController.navigate(ExpenseHistoryRoute(category)) }
                )
            }

            composable<SettingsRoute> {
                val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}

/** Перехід на вкладку нижньої панелі зі збереженням стану та без дублювання в back stack. */
private fun NavHostController.navigateToTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
