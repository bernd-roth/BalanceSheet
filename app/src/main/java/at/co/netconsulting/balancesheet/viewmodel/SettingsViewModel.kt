package at.co.netconsulting.balancesheet.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import at.co.netconsulting.general.StaticFields
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val ipAddress: String = "",
    val port: String = "",
    val persons: String = "",
    val foodBudget: String = "",
    val defaultPosition: String = "",
    val defaultLocation: String = "",
    val defaultCurrency: String = "EUR",
    val isChanged: Boolean = false
)

class SettingsViewModel(
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val ipAddress = sharedPrefs.getString(StaticFields.SP_INTERNET_ADDRESS, "") ?: ""
        val port = sharedPrefs.getString(StaticFields.SP_PORT, "") ?: ""
        val persons = sharedPrefs.getString(StaticFields.SP_PERSON, "") ?: ""
        val foodBudget = sharedPrefs.getString(StaticFields.SP_MONEY_FOOD, "") ?: ""
        val defaultPosition = sharedPrefs.getString(StaticFields.SP_DEFAULT_POSITION, "") ?: ""
        val defaultLocation = sharedPrefs.getString(StaticFields.SP_DEFAULT_LOCATION, "") ?: ""
        val defaultCurrency = sharedPrefs.getString(StaticFields.SP_DEFAULT_CURRENCY, "EUR") ?: "EUR"

        _uiState.update { it.copy(
            ipAddress = ipAddress,
            port = port,
            persons = persons,
            foodBudget = foodBudget,
            defaultPosition = defaultPosition,
            defaultLocation = defaultLocation,
            defaultCurrency = defaultCurrency,
            isChanged = false
        )}
    }

    fun onIpAddressChanged(value: String) {
        _uiState.update { it.copy(ipAddress = value, isChanged = true) }
    }

    fun onPortChanged(value: String) {
        _uiState.update { it.copy(port = value, isChanged = true) }
    }

    fun onPersonsChanged(value: String) {
        _uiState.update { it.copy(persons = value, isChanged = true) }
    }

    fun onFoodBudgetChanged(value: String) {
        _uiState.update { it.copy(foodBudget = value, isChanged = true) }
    }

    fun onDefaultPositionChanged(value: String) {
        _uiState.update { it.copy(defaultPosition = value, isChanged = true) }
    }

    fun onDefaultLocationChanged(value: String) {
        _uiState.update { it.copy(defaultLocation = value, isChanged = true) }
    }

    fun onDefaultCurrencyChanged(value: String) {
        _uiState.update { it.copy(defaultCurrency = value, isChanged = true) }
    }

    fun resetChangedFlag() {
        _uiState.update { it.copy(isChanged = false) }
    }
}