package at.co.netconsulting.balancesheet.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.enums.Spending
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
    val customPositions: List<String> = emptyList(),
    val customLocations: List<String> = emptyList(),
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

        val customPositionsString = sharedPrefs.getString(StaticFields.SP_CUSTOM_POSITIONS, "") ?: ""
        val customPositions = if (customPositionsString.isNotEmpty()) {
            customPositionsString.split(",").map { it.trim() }
        } else {
            emptyList()
        }

        val customLocationsString = sharedPrefs.getString(StaticFields.SP_CUSTOM_LOCATIONS, "") ?: ""
        val customLocations = if (customLocationsString.isNotEmpty()) {
            customLocationsString.split(",").map { it.trim() }
        } else {
            emptyList()
        }

        _uiState.update { it.copy(
            ipAddress = ipAddress,
            port = port,
            persons = persons,
            foodBudget = foodBudget,
            defaultPosition = defaultPosition,
            defaultLocation = defaultLocation,
            defaultCurrency = defaultCurrency,
            customPositions = customPositions,
            customLocations = customLocations,
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

    // Custom positions management
    fun addCustomPosition(position: String) {
        if (position.isNotBlank() && !_uiState.value.customPositions.contains(position)) {
            val updatedPositions = _uiState.value.customPositions + position
            _uiState.update { it.copy(customPositions = updatedPositions, isChanged = true) }
            saveCustomPositions(updatedPositions)
        }
    }

    fun removeCustomPosition(position: String) {
        val updatedPositions = _uiState.value.customPositions.filter { it != position }
        _uiState.update { it.copy(customPositions = updatedPositions, isChanged = true) }
        saveCustomPositions(updatedPositions)
    }

    private fun saveCustomPositions(positions: List<String>) {
        val editor = sharedPrefs.edit()
        editor.putString(StaticFields.SP_CUSTOM_POSITIONS, positions.joinToString(","))
        editor.apply()
    }

    // Custom locations management
    fun addCustomLocation(location: String) {
        if (location.isNotBlank() && !_uiState.value.customLocations.contains(location)) {
            val updatedLocations = _uiState.value.customLocations + location
            _uiState.update { it.copy(customLocations = updatedLocations, isChanged = true) }
            saveCustomLocations(updatedLocations)
        }
    }

    fun removeCustomLocation(location: String) {
        val updatedLocations = _uiState.value.customLocations.filter { it != location }
        _uiState.update { it.copy(customLocations = updatedLocations, isChanged = true) }
        saveCustomLocations(updatedLocations)
    }

    private fun saveCustomLocations(locations: List<String>) {
        val editor = sharedPrefs.edit()
        editor.putString(StaticFields.SP_CUSTOM_LOCATIONS, locations.joinToString(","))
        editor.apply()
    }

    // Helper methods to get all available options (enum + custom)
    fun getAllPositions(): List<String> {
        val enumPositions = Spending.values().map { it.toString() }
        return enumPositions + _uiState.value.customPositions
    }

    fun getAllLocations(): List<String> {
        val enumLocations = Location.values().map { it.toString() }
        return enumLocations + _uiState.value.customLocations
    }
}