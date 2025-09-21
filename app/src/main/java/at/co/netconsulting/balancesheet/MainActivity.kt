package at.co.netconsulting.balancesheet

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.co.netconsulting.balancesheet.ui.theme.BalanceSheetTheme
import at.co.netconsulting.balancesheet.viewmodel.ChartViewModel
import at.co.netconsulting.balancesheet.viewmodel.ChartViewModelFactory
import at.co.netconsulting.balancesheet.viewmodel.MainViewModel
import at.co.netconsulting.balancesheet.viewmodel.SettingsViewModel
import at.co.netconsulting.general.StaticFields

class MainActivity : ComponentActivity() {

    // API keys for location and currency services
    private val openCageApiKey = "4071ef0ec5b64b6aaf56777a93db9d8b"
    private val exchangeRateApiKey = "6a3d3e949ff3cfaba27f8f44"

    // View models
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(application, openCageApiKey, exchangeRateApiKey)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(application)
    }

    // Add the ChartViewModel
    private val chartViewModel: ChartViewModel by viewModels {
        ChartViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("BalanceSheetPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putString(StaticFields.SP_INTERNET_ADDRESS, "balancesheet.duckdns.org")
        editor.putString(StaticFields.SP_PORT, "8080")
        editor.commit()

        // Request necessary permissions
        requestPermissions()

        // Load persons from settings
        loadPersons()

        //mainViewModel.debugConnection()

        setContent {
            BalanceSheetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        mainViewModel = mainViewModel,
                        settingsViewModel = settingsViewModel,
                        chartViewModel = chartViewModel,
                        onSaveSettings = { ip, port, persons, foodBudget, defaultPosition, defaultLocation, defaultCurrency ->
                            saveSettings(ip, port, persons, foodBudget, defaultPosition, defaultLocation, defaultCurrency)
                        }
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        // Internet permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.INTERNET),
                StaticFields.INTERNET_PERMISSION_CODE
            )
        }

        // Wi-Fi state permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_WIFI_STATE),
                StaticFields.WIFI_PERMISSION_CODE
            )
        }

        // Location permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    private fun loadPersons() {
        val sharedPrefs = getSharedPreferences("BalanceSheetPrefs", Context.MODE_PRIVATE)
        val personsString = sharedPrefs.getString(StaticFields.SP_PERSON, "") ?: ""

        if (personsString.isNotEmpty()) {
            val personsList = personsString.split(" ")
            mainViewModel.updatePersons(personsList)
        }
    }

    private fun saveSettings(
        ip: String,
        port: String,
        persons: String,
        foodBudget: String,
        defaultPosition: String,
        defaultLocation: String,
        defaultCurrency: String
    ) {
        val sharedPrefs = getSharedPreferences("BalanceSheetPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        editor.putString(StaticFields.SP_INTERNET_ADDRESS, ip)
        editor.putString(StaticFields.SP_PORT, port)
        editor.putString(StaticFields.SP_PERSON, persons)
        editor.putString(StaticFields.SP_MONEY_FOOD, foodBudget)
        editor.putString(StaticFields.SP_DEFAULT_POSITION, defaultPosition)
        editor.putString(StaticFields.SP_DEFAULT_LOCATION, defaultLocation)
        editor.putString(StaticFields.SP_DEFAULT_CURRENCY, defaultCurrency)
        editor.apply()

        // Update persons list in MainViewModel
        if (persons.isNotEmpty()) {
            val personsList = persons.split(" ")
            mainViewModel.updatePersons(personsList)
        }

        // Reload default settings and refresh data to apply new settings
        mainViewModel.reloadDefaultSettings()
        mainViewModel.refreshData()
    }

    companion object {
        private const val LOCATION_PERMISSION_CODE = 104
    }
}

@Composable
fun BalanceSheetNavigation(
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    onSaveSettings: (String, String, String, String, String, String, String) -> Unit
) {
    val navController = rememberNavController()

    // Create a ChartViewModel for the chart screen
    val chartViewModel: ChartViewModel = viewModel(factory = ChartViewModelFactory())

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToChart = {
                    // Update chart data from main view model before navigating
                    chartViewModel.setChartData(
                        totalIncome = mainViewModel.uiState.value.summary.totalIncome.toFloat(),
                        totalExpense = mainViewModel.uiState.value.summary.totalExpense.toFloat(),
                        totalSavings = mainViewModel.uiState.value.summary.totalSavings.toFloat(),
                        totalFood = mainViewModel.uiState.value.summary.totalFood.toFloat()
                    )
                    navController.navigate("chart")
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSaveSettings = onSaveSettings
            )
        }

        composable("chart") {
            ChartScreen(
                viewModel = chartViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}