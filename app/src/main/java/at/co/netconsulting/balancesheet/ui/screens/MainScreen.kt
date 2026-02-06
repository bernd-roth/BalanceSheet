package at.co.netconsulting.balancesheet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import at.co.netconsulting.balancesheet.R
import at.co.netconsulting.balancesheet.composable.CurrencyConversionDialog
import at.co.netconsulting.balancesheet.composable.DatePickerDialog
import at.co.netconsulting.balancesheet.data.IncomeExpense
import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.ui.components.cards.EntryFormCard
import at.co.netconsulting.balancesheet.ui.components.cards.RecentEntriesCard
import at.co.netconsulting.balancesheet.ui.components.cards.SummaryGrid
import at.co.netconsulting.balancesheet.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToChart: () -> Unit,
    onNavigateToEntries: () -> Unit,
    onNavigateToEditEntry: (IncomeExpense) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val persons = viewModel.personsList

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.action_settings)
                        )
                    }
                    IconButton(onClick = onNavigateToChart) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = stringResource(R.string.action_barChart)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Grid (2x2)
                SummaryGrid(
                    summary = uiState.summary,
                    personalFoodSummaries = uiState.personalFoodSummaries
                )

                // Expandable Entry Form Card
                EntryFormCard(
                    expanded = uiState.isEntryFormExpanded,
                    onExpandChange = viewModel::toggleEntryForm,
                    inputIncome = uiState.inputIncome,
                    inputExpense = uiState.inputExpense,
                    selectedPerson = uiState.selectedPerson,
                    selectedPosition = uiState.selectedPosition,
                    selectedLocationString = uiState.selectedLocation.displayName,
                    inputDate = uiState.inputDate,
                    inputComment = uiState.inputComment,
                    inputTaxable = uiState.inputTaxable,
                    inputExportTo = uiState.inputExportTo,
                    inputInfoOnly = uiState.inputInfoOnly,
                    isAddButtonEnabled = uiState.isAddButtonEnabled,
                    persons = persons,
                    availablePositions = uiState.availablePositions,
                    availableLocations = uiState.availableLocations,
                    onIncomeChanged = viewModel::onIncomeChanged,
                    onExpenseChanged = viewModel::onExpenseChanged,
                    onPersonChanged = viewModel::onPersonChanged,
                    onPositionChanged = { displayName ->
                        val position = uiState.availablePositions.find { it.displayName == displayName }
                        position?.let { viewModel.onPositionChanged(it) }
                    },
                    onLocationChanged = { displayName ->
                        val location = Location.values().find { it.displayName == displayName }
                        location?.let { viewModel.onLocationChanged(it) }
                    },
                    onDateChanged = viewModel::onDateChanged,
                    onDatePickerClick = { showDatePicker = true },
                    onCommentChanged = viewModel::onCommentChanged,
                    onTaxableChanged = viewModel::onTaxableChanged,
                    onExportToChanged = viewModel::onExportToChanged,
                    onInfoOnlyChanged = viewModel::onInfoOnlyChanged,
                    onAddClicked = viewModel::addEntry
                )

                // Recent Entries Card
                RecentEntriesCard(
                    entries = uiState.recentEntries,
                    onEntryClick = { entry -> onNavigateToEditEntry(entry) },
                    onViewAllClick = onNavigateToEntries
                )
            }
        }

        // Date picker dialog
        if (showDatePicker) {
            val date = if (uiState.inputDate.isNotEmpty()) {
                try {
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    LocalDate.parse(uiState.inputDate, formatter)
                } catch (e: Exception) {
                    LocalDate.now()
                }
            } else {
                LocalDate.now()
            }

            DatePickerDialog(
                initialDate = date,
                onDateSelected = { selectedDate ->
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    viewModel.onDateChanged(selectedDate.format(formatter))
                },
                onDismiss = { showDatePicker = false }
            )
        }

        // Show currency conversion dialog if needed
        if (uiState.showCurrencyConversionDialog) {
            CurrencyConversionDialog(
                country = uiState.detectedCountry,
                localCurrency = uiState.detectedCurrency,
                defaultCurrency = "EUR",
                originalAmount = uiState.originalAmount,
                convertedAmount = uiState.convertedAmount,
                exchangeRate = uiState.exchangeRate,
                isIncome = uiState.isIncome,
                onConfirm = viewModel::confirmCurrencyConversion,
                onDismiss = viewModel::hideCurrencyConversionDialog
            )
        }

        // Show error message if needed
        uiState.errorMessage?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.clearErrorMessage() },
                title = { Text("Connection Error") },
                text = { Text(error) },
                confirmButton = {
                    if (uiState.showRetryButton) {
                        TextButton(onClick = {
                            viewModel.clearErrorMessage()
                            viewModel.loadData()
                        }) {
                            Text("Retry")
                        }
                    } else {
                        TextButton(onClick = { viewModel.clearErrorMessage() }) {
                            Text("OK")
                        }
                    }
                },
                dismissButton = if (uiState.showRetryButton) {
                    {
                        TextButton(onClick = { viewModel.clearErrorMessage() }) {
                            Text("Cancel")
                        }
                    }
                } else null
            )
        }
    }
}
