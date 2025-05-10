package at.co.netconsulting.balancesheet.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.co.netconsulting.balancesheet.network.BalanceSheetRepository
import at.co.netconsulting.general.StaticFields

class MainViewModelFactory(
    private val application: Application,
    private val openCageApiKey: String,
    private val exchangeRateApiKey: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val sharedPrefs = application.getSharedPreferences("BalanceSheetPrefs", Context.MODE_PRIVATE)
            val serverIp = sharedPrefs.getString(StaticFields.SP_INTERNET_ADDRESS, "localhost") ?: "localhost"
            val serverPort = sharedPrefs.getString(StaticFields.SP_PORT, "8080") ?: "8080"
            val defaultReserve = sharedPrefs.getString(StaticFields.SP_MONEY_FOOD, "0") ?: "0"

            val baseUrl = "${StaticFields.PROTOCOL}$serverIp${StaticFields.COLON}$serverPort"
            val repository = BalanceSheetRepository(baseUrl)

            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                repository,
                defaultReserve,
                application,
                sharedPrefs,
                openCageApiKey,
                exchangeRateApiKey
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SettingsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            val sharedPrefs = application.getSharedPreferences("BalanceSheetPrefs", Context.MODE_PRIVATE)

            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(sharedPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Add the ChartViewModelFactory
class ChartViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChartViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}