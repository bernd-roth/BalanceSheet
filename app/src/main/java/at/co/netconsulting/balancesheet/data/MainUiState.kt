package at.co.netconsulting.balancesheet.data

import at.co.netconsulting.balancesheet.IncomeExpense
import at.co.netconsulting.balancesheet.PersonalFoodSummary
import at.co.netconsulting.balancesheet.Summary
import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.enums.Spending
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class MainUiState(
    val summary: at.co.netconsulting.balancesheet.Summary = Summary(),
    val entries: List<at.co.netconsulting.balancesheet.IncomeExpense> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val personalFoodSummaries: List<PersonalFoodSummary> = emptyList(),
    val selectedEntry: IncomeExpense? = null,
    val showDialog: Boolean = false,
    val showEntriesListDialog: Boolean = false,
    val inputIncome: String = "0",
    val inputExpense: String = "0",
    val inputDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
    val selectedPerson: String = "",
    val selectedPosition: Spending = Spending.Expense,
    val selectedLocation: Location = Location.Hollgasse_1_1,
    val inputComment: String = "",
    val isAddButtonEnabled: Boolean = false
)