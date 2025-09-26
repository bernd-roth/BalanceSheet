package at.co.netconsulting.balancesheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.co.netconsulting.balancesheet.viewmodel.ChartViewModel
import at.co.netconsulting.balancesheet.viewmodel.SettingsViewModel
import at.co.netconsulting.balancesheet.viewmodel.MainViewModel

object Destinations {
    const val MAIN_ROUTE = "main"
    const val SETTINGS_ROUTE = "settings"
    const val CHART_ROUTE = "chart"
}

@Composable
fun AppNavigation(
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    chartViewModel: ChartViewModel,
    onSaveSettings: (String, String, String, String, String, String, String) -> Unit
) {
    val navController = rememberNavController()
    val navigationActions = remember(navController) {
        AppNavigationActions(navController)
    }

    NavHost(
        navController = navController,
        startDestination = Destinations.MAIN_ROUTE
    ) {
        composable(Destinations.MAIN_ROUTE) {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToSettings = { navigationActions.navigateToSettings() },
                onNavigateToChart = {
                    // Update chart data from main view model
                    chartViewModel.setChartData(
                        totalIncome = mainViewModel.uiState.value.summary.totalIncome.toFloat(),
                        totalExpense = mainViewModel.uiState.value.summary.totalExpense.toFloat(),
                        totalSavings = mainViewModel.uiState.value.summary.totalSavings.toFloat(),
                        totalFood = mainViewModel.uiState.value.summary.totalFood.toFloat()
                    )
                    navigationActions.navigateToChart()
                }
            )
        }

        composable(Destinations.SETTINGS_ROUTE) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    // Refresh MainViewModel to update custom positions/locations when returning from settings
                    println("DEBUG Navigation: onNavigateBack from Settings called")
                    mainViewModel.reloadDefaultSettings()
                    navigationActions.navigateBack()
                },
                onSaveSettings = onSaveSettings
            )
        }

        composable(Destinations.CHART_ROUTE) {
            ChartScreen(
                viewModel = chartViewModel,
                onNavigateBack = { navigationActions.navigateBack() }
            )
        }
    }
}

class AppNavigationActions(private val navController: NavHostController) {
    fun navigateToSettings() {
        navController.navigate(Destinations.SETTINGS_ROUTE)
    }

    fun navigateToChart() {
        navController.navigate(Destinations.CHART_ROUTE)
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}