package at.co.netconsulting.balancesheet.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ChartUiState(
    val totalIncome: Float = 0f,
    val totalExpense: Float = 0f,
    val totalSavings: Float = 0f,
    val totalFood: Float = 0f
)

class ChartViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    fun setChartData(totalIncome: Float, totalExpense: Float, totalSavings: Float, totalFood: Float) {
        _uiState.update {
            it.copy(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                totalSavings = totalSavings,
                totalFood = totalFood
            )
        }
    }
}