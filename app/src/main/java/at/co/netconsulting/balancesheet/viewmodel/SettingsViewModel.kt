package at.co.netconsulting.balancesheet.viewmodel

import androidx.lifecycle.ViewModel
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
    val isChanged: Boolean = false
)

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadSettings(
        ipAddress: String,
        port: String,
        persons: String,
        foodBudget: String,
        defaultPosition: String,
        defaultLocation: String
    ) {
        _uiState.update {
            it.copy(
                ipAddress = ipAddress,
                port = port,
                persons = persons,
                foodBudget = foodBudget,
                defaultPosition = defaultPosition,
                defaultLocation = defaultLocation,
                isChanged = false
            )
        }
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

    fun resetChangedFlag() {
        _uiState.update { it.copy(isChanged = false) }
    }
}