package com.example.expensetracker.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.example.expensetracker.AppContainer
import com.example.expensetracker.ui.screens.AddExpenseScreen
import com.example.expensetracker.ui.screens.HistoryScreen
import com.example.expensetracker.ui.screens.HomeScreen
import com.example.expensetracker.ui.screens.SettingsScreen
import com.example.expensetracker.ui.viewmodels.AddExpenseViewModel
import com.example.expensetracker.ui.viewmodels.HistoryViewModel
import com.example.expensetracker.ui.viewmodels.HomeViewModel
import com.example.expensetracker.ui.viewmodels.SettingsViewModel
import com.example.expensetracker.ui.viewmodels.factoryOf
import com.example.expensetracker.work.ADD_EXPENSE_DEEP_LINK
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object AddExpenseRoute

/** Історія та аналітика; category == null — усі категорії. */
@Serializable
data class ExpenseHistoryRoute(val category: String? = null)

@Serializable
object SettingsRoute

@Composable
fun AppNavigation(container: AppContainer) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val destination = entry?.destination
    val onHome = destination?.hasRoute<HomeRoute>() == true
    val onHistory = destination?.hasRoute<ExpenseHistoryRoute>() == true

    Scaffold(
        bottomBar = {
            if (onHome || onHistory) {
                NavigationBar {
                    NavigationBarItem(
                        selected = onHome,
                        onClick = {
                            navController.navigate(HomeRoute) {
                                popUpTo(HomeRoute) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Text("💰") },
                        label = { Text("Огляд") }
                    )
                    NavigationBarItem(
                        selected = onHistory,
                        onClick = {
                            navController.navigate(ExpenseHistoryRoute()) {
                                popUpTo(HomeRoute) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Text("📊") },
                        label = { Text("Аналітика") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(padding),
            enterTransition = { slideInHorizontally { it / 3 } },
            exitTransition = { slideOutHorizontally { -it / 3 } },
            popEnterTransition = { slideInHorizontally { -it / 3 } },
            popExitTransition = { slideOutHorizontally { it / 3 } }
        ) {
            composable<HomeRoute> {
                val vm: HomeViewModel = viewModel(
                    factory = factoryOf { HomeViewModel(container.expenses, container.rates, container.secure) }
                )
                HomeScreen(
                    viewModel = vm,
                    onAddExpense = { navController.navigate(AddExpenseRoute) },
                    onOpenHistory = { navController.navigate(ExpenseHistoryRoute()) },
                    onOpenSettings = { navController.navigate(SettingsRoute) }
                )
            }
            composable<AddExpenseRoute>(
                deepLinks = listOf(navDeepLink<AddExpenseRoute>(basePath = ADD_EXPENSE_DEEP_LINK))
            ) {
                val vm: AddExpenseViewModel = viewModel(
                    factory = factoryOf { AddExpenseViewModel(container.expenses, container.secure) }
                )
                AddExpenseScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }
            composable<ExpenseHistoryRoute> { backStackEntry ->
                val route: ExpenseHistoryRoute = backStackEntry.toRoute()
                val vm: HistoryViewModel = viewModel(
                    factory = factoryOf { HistoryViewModel(container.expenses, route.category) }
                )
                HistoryScreen(viewModel = vm)
            }
            composable<SettingsRoute> {
                val vm: SettingsViewModel = viewModel(
                    factory = factoryOf { SettingsViewModel(container.secure, container.rates, container.expenses) }
                )
                SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }
        }
    }
}
