package at.co.netconsulting.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.co.netconsulting.balancesheet.IncomeExpense
import at.co.netconsulting.balancesheet.PersonalFoodSummary
import at.co.netconsulting.balancesheet.Summary
import at.co.netconsulting.balancesheet.data.MainUiState
import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.enums.Spending
import at.co.netconsulting.balancesheet.network.BalanceSheetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class MainViewModel(
    private val repository: BalanceSheetRepository,
    private val defaultReserve: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Using getter/setter pattern to avoid JVM signature clash with updatePersons
    private val _personsList = mutableStateOf(listOf<String>())
    val personsList: List<String> get() = _personsList.value

    init {
        refreshData()
    }

    fun updatePersons(newPersons: List<String>) {
        _personsList.value = newPersons
        if (newPersons.isNotEmpty() && _uiState.value.selectedPerson.isEmpty()) {
            _uiState.update { it.copy(selectedPerson = newPersons.first()) }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val totalIncome = repository.getTotalIncome()
                val totalExpense = repository.getTotalExpense()
                val totalSavings = repository.getTotalSavings()
                val totalFood = repository.getTotalFood()
                val averageFoodPerDay = repository.getAverageFoodPerDay()
                val reservedFoodPerDay = repository.getReservedFoodPerDay()
                val totalYearFood = repository.getTotalYearFood()
                val totalYearIncome = repository.getTotalYearIncome()

                val summary = Summary(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    totalSavings = totalSavings,
                    totalFood = totalFood,
                    averageFoodPerDay = averageFoodPerDay,
                    reservedFoodPerDay = reservedFoodPerDay,
                    totalYearFood = totalYearFood,
                    totalYearIncome = totalYearIncome
                )

                val entries = repository.getAllEntries()

                val personalSummaries = personsList.map { person ->
                    val budget = repository.getPersonFoodSummary(person, defaultReserve)
                    PersonalFoodSummary(person, budget)
                }

                _uiState.update { it.copy(
                    summary = summary,
                    entries = entries,
                    personalFoodSummaries = personalSummaries,
                    isLoading = false,
                    errorMessage = null
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load data: ${e.localizedMessage}"
                )}
            }
        }
    }

    fun onIncomeChanged(value: String) {
        _uiState.update { it.copy(inputIncome = value) }
        validateInputs()
    }

    fun onExpenseChanged(value: String) {
        _uiState.update { it.copy(inputExpense = value) }
        validateInputs()
    }

    fun onDateChanged(value: String) {
        _uiState.update { it.copy(inputDate = value) }
    }

    fun onPersonChanged(value: String) {
        _uiState.update { it.copy(selectedPerson = value) }
    }

    fun onPositionChanged(value: Spending) {
        _uiState.update { it.copy(selectedPosition = value) }
    }

    fun onLocationChanged(value: Location) {
        _uiState.update { it.copy(selectedLocation = value) }
    }

    fun onCommentChanged(value: String) {
        _uiState.update { it.copy(inputComment = value) }
    }

    private fun validateInputs() {
        val state = _uiState.value
        val income = state.inputIncome.toDoubleOrNull() ?: 0.0
        val expense = state.inputExpense.toDoubleOrNull() ?: 0.0

        val isValid = when {
            state.selectedPerson.isEmpty() -> false
            state.inputDate.isEmpty() -> false
            income == expense -> false
            income > 0 && expense > 0 -> false
            income == 0.0 && expense == 0.0 -> false
            else -> true
        }

        _uiState.update { it.copy(isAddButtonEnabled = isValid) }
    }

    fun addEntry() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val state = _uiState.value
                val income = state.inputIncome.toDoubleOrNull() ?: 0.0
                val expense = state.inputExpense.toDoubleOrNull() ?: 0.0

                // Format the date from DD/MM/YYYY to LocalDate
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val orderDate = LocalDate.parse(state.inputDate, formatter)

                val entry = IncomeExpense(
                    orderdate = orderDate,
                    who = state.selectedPerson,
                    position = state.selectedPosition,
                    income = income,
                    expense = expense,
                    location = state.selectedLocation,
                    comment = state.inputComment
                )

                val transactionId = "${System.currentTimeMillis()}-${UUID.randomUUID().toString().substring(0, 8)}"
                val success = repository.addEntry(entry, transactionId)

                if (success) {
                    // Reset form
                    _uiState.update { it.copy(
                        inputIncome = "0",
                        inputExpense = "0",
                        inputComment = "",
                        isAddButtonEnabled = false
                    )}
                    // Refresh data
                    refreshData()
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Failed to add entry"
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to add entry: ${e.localizedMessage}"
                )}
            }
        }
    }

    fun showEntryDetails(entry: IncomeExpense) {
        _uiState.update { it.copy(
            selectedEntry = entry,
            showDialog = true
        )}
    }

    fun hideEntryDetails() {
        _uiState.update { it.copy(
            showDialog = false
        )}
    }

    fun showEntriesList() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Fetch the latest entries
                val allEntries = repository.getAllEntries()
                println("DEBUG: All entries count: ${allEntries.size}")

                // Log all entry dates for debugging
                allEntries.forEach { entry ->
                    println("DEBUG: Entry date: ${entry.orderdate}, month: ${entry.orderdate.monthValue}, year: ${entry.orderdate.year}")
                }

                // Temporarily disable month/year filtering to see if we get any entries
                _uiState.update { it.copy(
                    entries = allEntries,  // Use all entries without filtering
                    showEntriesListDialog = true,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                println("DEBUG: Exception in showEntriesList: ${e.message}")
                e.printStackTrace()
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load entries: ${e.localizedMessage}"
                )}
            }
        }
    }

    fun hideEntriesList() {
        _uiState.update { it.copy(showEntriesListDialog = false) }
    }

    fun updateEntry(
        id: String,
        date: String,
        person: String,
        location: String,
        income: String,
        expense: String,
        position: String,
        comment: String
    ) {
        viewModelScope.launch {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val orderDate = LocalDate.parse(date, formatter)

                val entry = IncomeExpense(
                    id = id,
                    orderdate = orderDate,
                    who = person,
                    position = Spending.valueOf(position),
                    income = income.toDoubleOrNull() ?: 0.0,
                    expense = expense.toDoubleOrNull() ?: 0.0,
                    location = Location.valueOf(location),
                    comment = comment
                )

                val success = repository.updateEntry(entry)

                if (success) {
                    _uiState.update { it.copy(
                        showDialog = false,
                        selectedEntry = null
                    )}
                    refreshData()
                } else {
                    _uiState.update { it.copy(
                        errorMessage = "Failed to update entry"
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Failed to update entry: ${e.localizedMessage}"
                )}
            }
        }
    }
}