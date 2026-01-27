package at.co.netconsulting.balancesheet.data

import at.co.netconsulting.balancesheet.PersonalFoodSummary
import at.co.netconsulting.balancesheet.Summary
import at.co.netconsulting.balancesheet.enums.ExportTo
import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.enums.Position
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class MainUiState(
    val summary: Summary = Summary(),
    val entries: List<IncomeExpense> = emptyList(),
    val personalFoodSummaries: List<PersonalFoodSummary> = emptyList(),
    val selectedPerson: String = "",
    val selectedPosition: Position = Position.essen,
    val selectedLocation: Location = Location.Hollgasse_1_1,
    val selectedPositionString: String = "", // For custom positions
    val selectedLocationString: String = "", // For custom locations
    val inputIncome: String = "0",
    val inputExpense: String = "0",
    val inputDate: String = "",
    val inputComment: String = "",
    val inputTaxable: Boolean = true,
    val inputExportTo: ExportTo = ExportTo.auto,
    val inputInfoOnly: Boolean = false,
    val isAddButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showRetryButton: Boolean = false,
    val selectedEntry: IncomeExpense? = null,
    val showDialog: Boolean = false,
    val showEntriesListDialog: Boolean = false,
    val dialogTitle: String = "All Entries",
    val customDataRefreshTrigger: Long = 0L, // Trigger to refresh custom positions/locations
    val availablePositions: List<Position> = emptyList(), // Filtered positions based on location
    val availableLocations: List<String> = emptyList(), // Cached locations list
    val showCurrencyConversionDialog: Boolean = false,
    val detectedCurrency: String = "",
    val detectedCountry: String = "",
    val originalAmount: Double = 0.0,
    val convertedAmount: Double = 0.0,
    val exchangeRate: Double = 0.0,
    val isIncome: Boolean = false // Track if it's income or expense
)