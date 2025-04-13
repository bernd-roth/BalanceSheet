package at.co.netconsulting.balancesheet

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import at.co.netconsulting.balancesheet.network.BalanceSheetRepository
import at.co.netconsulting.balancesheet.ui.theme.BalanceSheetTheme
import at.co.netconsulting.balancesheet.viewmodel.ChartViewModel
import at.co.netconsulting.balancesheet.viewmodel.SettingsViewModel
import at.co.netconsulting.general.StaticFields
import at.co.netconsulting.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private lateinit var repository: BalanceSheetRepository
    private lateinit var mainViewModel: MainViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var chartViewModel: ChartViewModel

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { entry ->
            val permissionName = entry.key
            val isGranted = entry.value

            if (isGranted) {
                Toast.makeText(
                    this,
                    "Permission $permissionName granted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Permission $permissionName denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check permissions
        checkPermissions()

        // Load settings
        val ipAddress = loadSharedPreference(StaticFields.SP_INTERNET_ADDRESS)
        val port = loadSharedPreference(StaticFields.SP_PORT)
        val persons = loadSharedPreference(StaticFields.SP_PERSON)
        val foodBudget = loadSharedPreference(StaticFields.SP_MONEY_FOOD)
        val defaultPosition = loadSharedPreference(StaticFields.SP_DEFAULT_POSITION)
        val defaultLocation = loadSharedPreference(StaticFields.SP_DEFAULT_LOCATION)

        // Initialize repository with server details
        repository = BalanceSheetRepository(
            baseUrl = "${StaticFields.PROTOCOL}$ipAddress${StaticFields.COLON}$port"
        )

        // Initialize ViewModels using ViewModelProvider
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        chartViewModel = ViewModelProvider(this)[ChartViewModel::class.java]
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(repository, foodBudget)
        )[MainViewModel::class.java]

        // Load settings into ViewModel
        settingsViewModel.loadSettings(
            ipAddress = ipAddress,
            port = port,
            persons = persons,
            foodBudget = foodBudget,
            defaultPosition = defaultPosition,
            defaultLocation = defaultLocation
        )

        // Initialize MainViewModel with persons
        val personsList = persons.split(" ").filter { it.isNotEmpty() }
        mainViewModel.updatePersons(personsList)

        setContent {
            BalanceSheetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Set up navigation within the composable context
                    AppNavigation(
                        mainViewModel = mainViewModel,
                        settingsViewModel = settingsViewModel,
                        chartViewModel = chartViewModel,
                        onSaveSettings = { ip, port, persons, budget, position, location ->
                            saveSharedPreference(StaticFields.SP_INTERNET_ADDRESS, ip)
                            saveSharedPreference(StaticFields.SP_PORT, port)
                            saveSharedPreference(StaticFields.SP_PERSON, persons)
                            saveSharedPreference(StaticFields.SP_MONEY_FOOD, budget)
                            saveSharedPreference(StaticFields.SP_DEFAULT_POSITION, position)
                            saveSharedPreference(StaticFields.SP_DEFAULT_LOCATION, location)

                            // Update repository with new server details
                            repository = BalanceSheetRepository(
                                baseUrl = "${StaticFields.PROTOCOL}$ip${StaticFields.COLON}$port"
                            )

                            // Update MainViewModel with new persons
                            val newPersonsList = persons.split(" ").filter { it.isNotEmpty() }
                            mainViewModel.updatePersons(newPersonsList)

                            // Refresh data
                            mainViewModel.refreshData()

                            Toast.makeText(
                                this@MainActivity,
                                "Settings saved",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.INTERNET)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_WIFI_STATE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun loadSharedPreference(key: String): String {
        val sharedPreferences = getSharedPreferences(key, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, "") ?: ""
    }

    private fun saveSharedPreference(key: String, value: String) {
        val sharedPreferences = getSharedPreferences(key, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }
}