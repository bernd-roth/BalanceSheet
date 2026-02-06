package at.co.netconsulting.balancesheet.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.co.netconsulting.balancesheet.data.IncomeExpense
import at.co.netconsulting.balancesheet.PersonalFoodSummary
import at.co.netconsulting.balancesheet.Summary
import at.co.netconsulting.balancesheet.data.MainUiState
import at.co.netconsulting.balancesheet.enums.ExportTo
import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.enums.Position
import at.co.netconsulting.balancesheet.network.BalanceSheetRepository
import at.co.netconsulting.balancesheet.location.LocationService
import at.co.netconsulting.balancesheet.currency.CurrencyConversionService
import at.co.netconsulting.general.StaticFields
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit

class MainViewModel(
    private val repository: BalanceSheetRepository,
    private val defaultReserve: String,
    private val context: Context,
    private val sharedPrefs: SharedPreferences,
    private val openCageApiKey: String,
    private val exchangeRateApiKey: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val locationService = LocationService(context, openCageApiKey)
    private val currencyService = CurrencyConversionService(exchangeRateApiKey)

    // Using getter/setter pattern to avoid JVM signature clash with updatePersons
    private val _personsList = mutableStateOf(listOf<String>())
    val personsList: List<String> get() = _personsList.value

    private val defaultCurrency get() = sharedPrefs.getString(StaticFields.SP_DEFAULT_CURRENCY, "EUR") ?: "EUR"

    init {
        // Set current date in the input field
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        // Load default settings for position and location
        loadDefaultSettings()

        // Initialize available positions and locations
        val initialLocations = getAllLocations()
        val initialPositions = Position.values().toList()

        // Validate that the selected position is valid for the current location
        val validPosition = if (initialPositions.contains(_uiState.value.selectedPosition)) {
            _uiState.value.selectedPosition
        } else {
            initialPositions.firstOrNull() ?: Position.essen
        }

        _uiState.update { it.copy(
            inputDate = currentDate,
            availablePositions = initialPositions,
            availableLocations = initialLocations,
            selectedPosition = validPosition
        ) }

        refreshData()
    }

    private fun loadDefaultSettings() {
        val defaultPosition = sharedPrefs.getString(StaticFields.SP_DEFAULT_POSITION, "") ?: ""
        val defaultLocation = sharedPrefs.getString(StaticFields.SP_DEFAULT_LOCATION, "") ?: ""

        // Apply default position if it exists and is valid
        if (defaultPosition.isNotEmpty()) {
            try {
                val positionEnum = Position.valueOf(defaultPosition)
                _uiState.update { it.copy(
                    selectedPosition = positionEnum,
                    selectedPositionString = defaultPosition
                )}
            } catch (e: IllegalArgumentException) {
                // Handle custom position
                _uiState.update { it.copy(
                    selectedPosition = Position.essen,
                    selectedPositionString = defaultPosition
                )}
            }
        } else {
            // Set initial string value to match enum
            _uiState.update { it.copy(selectedPositionString = it.selectedPosition.displayName) }
        }

        // Apply default location if it exists and is valid
        if (defaultLocation.isNotEmpty()) {
            try {
                val locationEnum = Location.valueOf(defaultLocation)
                _uiState.update { it.copy(
                    selectedLocation = locationEnum,
                    selectedLocationString = defaultLocation
                )}
            } catch (e: IllegalArgumentException) {
                // Handle custom location
                _uiState.update { it.copy(
                    selectedLocation = Location.Hollgasse_1_1,
                    selectedLocationString = defaultLocation
                )}
            }
        } else {
            // Set initial string value to match enum
            _uiState.update { it.copy(selectedLocationString = it.selectedLocation.displayName) }
        }
    }

    fun reloadDefaultSettings() {
        println("DEBUG MainVM.reloadDefaultSettings(): Called")
        loadDefaultSettings()

        // Add a small delay to ensure SharedPreferences are fully persisted
        Thread.sleep(50)

        // Force UI refresh to update dropdowns with new custom values
        val newPositions = getAllPositions()
        val newLocations = getAllLocations()
        val newTrigger = System.currentTimeMillis()

        println("DEBUG MainVM.reloadDefaultSettings(): About to update UI state")
        println("DEBUG MainVM.reloadDefaultSettings(): New positions: $newPositions")
        println("DEBUG MainVM.reloadDefaultSettings(): New trigger: $newTrigger")

        // Get current selected values to check if they are still valid
        val currentSelectedPosition = getCurrentPositionValue()
        val currentSelectedLocation = getCurrentLocationValue()

        println("DEBUG MainVM.reloadDefaultSettings(): Current selected position: '$currentSelectedPosition'")
        println("DEBUG MainVM.reloadDefaultSettings(): Current selected location: '$currentSelectedLocation'")

        // Check if current selections are still valid, if not, reset to defaults
        val currentPosition = _uiState.value.selectedPosition
        val validPosition = if (newPositions.contains(currentPosition)) {
            currentPosition
        } else if (newPositions.isNotEmpty()) {
            newPositions.first()
        } else {
            Position.essen
        }

        val validLocation = if (newLocations.contains(currentSelectedLocation)) {
            currentSelectedLocation
        } else if (newLocations.isNotEmpty()) {
            newLocations.first()
        } else {
            Location.Hollgasse_1_1.displayName
        }

        println("DEBUG MainVM.reloadDefaultSettings(): Using valid position: '$validPosition'")
        println("DEBUG MainVM.reloadDefaultSettings(): Using valid location: '$validLocation'")

        _uiState.update { it.copy(
            customDataRefreshTrigger = newTrigger,
            availablePositions = newPositions,
            availableLocations = newLocations,
            selectedPosition = validPosition,
            selectedLocationString = validLocation
        )}
        println("DEBUG MainVM.reloadDefaultSettings(): UI state updated")
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

                // Get recent entries (last 5, sorted by date descending)
                val recentEntries = entries
                    .sortedByDescending { it.orderdate }
                    .take(5)

                val personalSummaries = personsList.map { person ->
                    val budget = repository.getPersonFoodSummary(person, defaultReserve)
                    PersonalFoodSummary(person, budget)
                }

                _uiState.update { it.copy(
                    summary = summary,
                    entries = entries,
                    recentEntries = recentEntries,
                    personalFoodSummaries = personalSummaries,
                    isLoading = false,
                    errorMessage = null,
                    showRetryButton = false
                )}
            } catch (e: Exception) {
                val isNetworkError = e.message?.contains("Unable to connect") == true ||
                                   e.message?.contains("Network error") == true ||
                                   e.message?.contains("Unable to resolve host") == true

                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load data: ${e.localizedMessage}",
                    showRetryButton = isNetworkError
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

    fun onPositionChanged(value: Position) {
        _uiState.update { it.copy(selectedPosition = value) }
    }

    fun onLocationChanged(value: Location) {
        _uiState.update { it.copy(selectedLocation = value) }
    }

    fun onCommentChanged(value: String) {
        _uiState.update { it.copy(inputComment = value) }
    }

    fun onTaxableChanged(value: Boolean) {
        _uiState.update { it.copy(inputTaxable = value) }
    }

    fun onExportToChanged(value: ExportTo) {
        _uiState.update { it.copy(inputExportTo = value) }
    }

    fun onInfoOnlyChanged(value: Boolean) {
        _uiState.update { it.copy(inputInfoOnly = value) }
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

                // Get current location
                val location = locationService.getCurrentLocation()

                if (location != null) {
                    val (latitude, longitude) = location

                    // Get country and currency based on location
                    val countryCurrency = locationService.getCountryCurrencyFromLocation(latitude, longitude)

                    if (countryCurrency != null) {
                        val (country, localCurrency) = countryCurrency

                        // Check if currencies are different and show confirmation dialog
                        if (localCurrency.isNotEmpty() && localCurrency != defaultCurrency) {
                            val originalAmount = if (income > 0) income else expense
                            val isIncome = income > 0

                            val convertedAmount = currencyService.convertCurrency(
                                originalAmount,
                                localCurrency,
                                defaultCurrency
                            )

                            if (convertedAmount != null) {
                                val rate = convertedAmount / originalAmount

                                _uiState.update { it.copy(
                                    isLoading = false,
                                    showCurrencyConversionDialog = true,
                                    detectedCurrency = localCurrency,
                                    detectedCountry = country,
                                    originalAmount = originalAmount,
                                    convertedAmount = convertedAmount,
                                    exchangeRate = rate,
                                    isIncome = isIncome
                                )}
                                return@launch
                            }
                        }
                    }
                }

                // If no conversion needed, proceed directly
                proceedWithAddEntry()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to add entry: ${e.localizedMessage}"
                )}
            }
        }
    }

    fun confirmCurrencyConversion() {
        proceedWithAddEntry()
        hideCurrencyConversionDialog()
    }

    fun hideCurrencyConversionDialog() {
        _uiState.update { it.copy(
            showCurrencyConversionDialog = false,
            detectedCurrency = "",
            detectedCountry = "",
            originalAmount = 0.0,
            convertedAmount = 0.0,
            exchangeRate = 0.0
        )}
    }

    private fun proceedWithAddEntry() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val state = _uiState.value
                var income = state.inputIncome.toDoubleOrNull() ?: 0.0
                var expense = state.inputExpense.toDoubleOrNull() ?: 0.0

                // Get current location
                val location = locationService.getCurrentLocation()

                if (location != null) {
                    val (latitude, longitude) = location

                    // Get country and currency based on location
                    val countryCurrency = locationService.getCountryCurrencyFromLocation(latitude, longitude)

                    if (countryCurrency != null) {
                        val (country, localCurrency) = countryCurrency

                        // Convert income/expense if currencies are different
                        if (localCurrency.isNotEmpty() && localCurrency != defaultCurrency) {
                            if (income > 0) {
                                val convertedIncome = currencyService.convertCurrency(income, localCurrency, defaultCurrency)
                                if (convertedIncome != null) {
                                    income = convertedIncome
                                }
                            }

                            if (expense > 0) {
                                val convertedExpense = currencyService.convertCurrency(expense, localCurrency, defaultCurrency)
                                if (convertedExpense != null) {
                                    expense = convertedExpense
                                }
                            }
                        }
                    }
                }

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
                    comment = state.inputComment,
                    taxable = state.inputTaxable,
                    exportTo = state.inputExportTo,
                    isInfoOnly = state.inputInfoOnly
                )

                val transactionId = "${System.currentTimeMillis()}-${UUID.randomUUID().toString().substring(0, 8)}"
                val success = repository.addEntry(entry, transactionId)

                if (success) {
                    // Reset form
                    _uiState.update { it.copy(
                        inputIncome = "0",
                        inputExpense = "0",
                        inputComment = "",
                        inputTaxable = true,
                        inputInfoOnly = false,
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
                    dialogTitle = "Current Month Entries",
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
        comment: String,
        taxable: Boolean,
        exportTo: String,
        isInfoOnly: Boolean
    ) {
        viewModelScope.launch {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val orderDate = LocalDate.parse(date, formatter)

                val entry = IncomeExpense(
                    id = id,
                    orderdate = orderDate,
                    who = person,
                    position = Position.valueOf(position),
                    income = income.toDoubleOrNull() ?: 0.0,
                    expense = expense.toDoubleOrNull() ?: 0.0,
                    location = Location.valueOf(location),
                    comment = comment,
                    taxable = taxable,
                    exportTo = ExportTo.valueOf(exportTo),
                    isInfoOnly = isInfoOnly
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

    fun showAllEntries() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Fetch all entries without date filtering
                val allEntries = repository.getAllEntriesWithoutDateFilter()
                println("DEBUG: All entries count (no date filter): ${allEntries.size}")

                _uiState.update { it.copy(
                    entries = allEntries,
                    showEntriesListDialog = true,
                    isLoading = false,
                    dialogTitle = "All Entries"  // Set a custom title
                )}
            } catch (e: Exception) {
                println("DEBUG: Exception in showAllEntries: ${e.message}")
                e.printStackTrace()
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load all entries: ${e.localizedMessage}"
                )}
            }
        }
    }
    fun debugConnection() {
        viewModelScope.launch(Dispatchers.IO) {  // Use Dispatchers.IO
            try {
                val sharedPrefs = context.getSharedPreferences("BalanceSheetPrefs", Context.MODE_PRIVATE)
                val serverIp = sharedPrefs.getString(StaticFields.SP_INTERNET_ADDRESS, "balancesheet.duckdns.org") ?: "balancesheet.duckdns.org"
                val serverPort = sharedPrefs.getString(StaticFields.SP_PORT, "8080") ?: "8080"

                val baseUrl = "${StaticFields.PROTOCOL}$serverIp${StaticFields.COLON}$serverPort"
                println("DEBUG: Attempting to connect to $baseUrl")

                // Try a simple connection test
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url(baseUrl)
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    println("DEBUG: Connection test result: ${response.code} ${response.message}")
                } catch (e: Exception) {
                    println("DEBUG: Connection test failed: ${e.message}")
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                println("DEBUG: Debug connection failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null, showRetryButton = false) }
    }

    fun loadData() {
        refreshData()
    }

    fun toggleEntryForm() {
        _uiState.update { it.copy(isEntryFormExpanded = !it.isEntryFormExpanded) }
    }

    // Get all available positions
    fun getAllPositions(): List<Position> {
        return Position.values().toList()
    }

    // Get all available locations from enum
    fun getAllLocations(): List<String> {
        return Location.values().map { it.displayName }
    }

    // Updated position change handler
    fun onPositionChangedString(value: String) {
        try {
            val positionEnum = Position.valueOf(value)
            _uiState.update { it.copy(
                selectedPosition = positionEnum,
                selectedPositionString = value
            )}
        } catch (e: IllegalArgumentException) {
            // If not a valid enum, try matching by display name
            val matchingPosition = Position.values().find { it.displayName == value }
            if (matchingPosition != null) {
                _uiState.update { it.copy(
                    selectedPosition = matchingPosition,
                    selectedPositionString = matchingPosition.name
                )}
            }
        }
    }

    // Updated location change handler to support custom values
    fun onLocationChangedString(value: String) {
        try {
            val locationEnum = Location.valueOf(value)
            _uiState.update { it.copy(
                selectedLocation = locationEnum,
                selectedLocationString = value
            )}
        } catch (e: IllegalArgumentException) {
            // Handle custom location
            _uiState.update { it.copy(
                selectedLocation = Location.Hollgasse_1_1, // Default enum value
                selectedLocationString = value // Store the custom value
            )}
        }
    }

    // Helper method to get the current position display value
    fun getCurrentPositionValue(): String {
        val state = _uiState.value
        return if (state.selectedPositionString.isNotEmpty()) {
            state.selectedPositionString
        } else {
            state.selectedPosition.displayName
        }
    }

    // Helper method to get the current location display value
    fun getCurrentLocationValue(): String {
        val state = _uiState.value
        return if (state.selectedLocationString.isNotEmpty()) {
            state.selectedLocationString
        } else {
            state.selectedLocation.displayName
        }
    }
}