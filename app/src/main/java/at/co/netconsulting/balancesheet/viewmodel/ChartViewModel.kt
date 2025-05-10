package at.co.netconsulting.balancesheet.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Create a data class to hold chart UI state
data class ChartUiState(
    val dummy: Boolean = true // Just a placeholder to avoid empty data class
)

class ChartViewModel : ViewModel() {

    // Add UI state flow that ChartScreen expects
    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    private val _totalIncome = MutableStateFlow(0f)
    val totalIncome: Float get() = _totalIncome.value

    private val _totalExpense = MutableStateFlow(0f)
    val totalExpense: Float get() = _totalExpense.value

    private val _totalSavings = MutableStateFlow(0f)
    val totalSavings: Float get() = _totalSavings.value

    private val _totalFood = MutableStateFlow(0f)
    val totalFood: Float get() = _totalFood.value

    fun setChartData(
        totalIncome: Float,
        totalExpense: Float,
        totalSavings: Float,
        totalFood: Float
    ) {
        _totalIncome.value = totalIncome
        _totalExpense.value = totalExpense
        _totalSavings.value = totalSavings
        _totalFood.value = totalFood
    }
}