package at.co.netconsulting.balancesheet.data

import at.co.netconsulting.balancesheet.PersonalFoodSummary
import at.co.netconsulting.balancesheet.Summary
import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.enums.Spending
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class MainUiState(
    val summary: Summary = Summary(),
    val entries: List<IncomeExpense> = emptyList(),
    val personalFoodSummaries: List<PersonalFoodSummary> = emptyList(),
    val selectedPerson: String = "",
    val selectedPosition: Spending = Spending.Expense,
    val selectedLocation: Location = Location.Hollgasse_1_1,
    val selectedPositionString: String = "", // For custom positions
    val selectedLocationString: String = "", // For custom locations
    val inputIncome: String = "0",
    val inputExpense: String = "0",
    val inputDate: String = "",
    val inputComment: String = "",
    val isAddButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showRetryButton: Boolean = false,
    val selectedEntry: IncomeExpense? = null,
    val showDialog: Boolean = false,
    val showEntriesListDialog: Boolean = false,
    val dialogTitle: String = "All Entries"
)