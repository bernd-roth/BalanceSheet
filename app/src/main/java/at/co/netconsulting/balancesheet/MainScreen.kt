package at.co.netconsulting.balancesheet

import EntryListDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.co.netconsulting.balancesheet.composable.*
import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.enums.Spending
import at.co.netconsulting.balancesheet.data.MainUiState
import at.co.netconsulting.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToChart: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val persons = viewModel.personsList

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
            MainContent(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                persons = persons,
                onIncomeChanged = viewModel::onIncomeChanged,
                onExpenseChanged = viewModel::onExpenseChanged,
                onDateChanged = viewModel::onDateChanged,
                onPersonChanged = viewModel::onPersonChanged,
                onPositionChanged = viewModel::onPositionChanged,
                onLocationChanged = viewModel::onLocationChanged,
                onCommentChanged = viewModel::onCommentChanged,
                onAddClicked = viewModel::addEntry,
                onShowEntriesClicked = viewModel::showEntriesList
            )
        }

        // Show dialog for entry details if needed
        if (uiState.showDialog && uiState.selectedEntry != null) {
            EntryDetailsDialog(
                entry = uiState.selectedEntry!!,
                onDismiss = viewModel::hideEntryDetails,
                onUpdate = viewModel::updateEntry
            )
        }

        // Show entries list dialog if needed
        if (uiState.showEntriesListDialog) {
            EntryListDialog(
                entries = uiState.entries,
                onDismiss = viewModel::hideEntriesList,
                onEntrySelected = viewModel::showEntryDetails
            )
        }

        // Show error message if needed
        uiState.errorMessage?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.refreshData() },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.refreshData() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    uiState: MainUiState,
    persons: List<String>,
    onIncomeChanged: (String) -> Unit,
    onExpenseChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onPersonChanged: (String) -> Unit,
    onPositionChanged: (Spending) -> Unit,
    onLocationChanged: (Location) -> Unit,
    onCommentChanged: (String) -> Unit,
    onAddClicked: () -> Unit,
    onShowEntriesClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Overview section
        Text(
            text = stringResource(R.string.overview),
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Total Income
        SummaryRow(
            label = stringResource(R.string.sum_income),
            value = uiState.summary.totalIncome.toString(),
            backgroundColor = Color.Green.copy(alpha = 0.2f)
        )

        // Total Expense
        SummaryRow(
            label = stringResource(R.string.sum_spending),
            value = uiState.summary.totalExpense.toString(),
            backgroundColor = Color.Red.copy(alpha = 0.2f)
        )

        // Total Savings
        SummaryRow(
            label = stringResource(R.string.sum_savings),
            value = uiState.summary.totalSavings.toString(),
            backgroundColor = Color.Blue.copy(alpha = 0.2f)
        )

        // Total Food
        SummaryRow(
            label = stringResource(R.string.sum_food_spending_month),
            value = uiState.summary.totalFood.toString(),
            backgroundColor = Color(0xFFFFA500).copy(alpha = 0.2f) // Orange
        )

        // Average Food Per Day
        SummaryRow(
            label = stringResource(R.string.average_food_spending_day_month),
            value = uiState.summary.averageFoodPerDay.toString(),
            backgroundColor = Color.Cyan.copy(alpha = 0.2f)
        )

        // Reserved Food Per Day
        SummaryRow(
            label = stringResource(R.string.average_food_reserved_day),
            value = uiState.summary.reservedFoodPerDay.toString(),
            backgroundColor = Color.Magenta.copy(alpha = 0.2f)
        )

        // Total Year Food
        SummaryRow(
            label = stringResource(R.string.sum_food_year),
            value = uiState.summary.totalYearFood.toString(),
            backgroundColor = Color.Red.copy(alpha = 0.2f)
        )

        // Total Year Income
        SummaryRow(
            label = stringResource(R.string.sum_income_year),
            value = uiState.summary.totalYearIncome.toString(),
            backgroundColor = Color.Green.copy(alpha = 0.2f)
        )

        // Personal Food Summaries
        uiState.personalFoodSummaries.forEach { summary ->
            val labelRes = if (summary.person == "Julia") {
                R.string.sum_left_food_julia_month
            } else if (summary.person == "Bernd") {
                R.string.sum_left_food_bernd_month
            } else {
                0 // Use a default or custom label
            }

            if (labelRes != 0) {
                SummaryRow(
                    label = stringResource(labelRes),
                    value = summary.remainingBudget.toString(),
                    backgroundColor = Color.Gray.copy(alpha = 0.2f)
                )
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        // Input section
        Text(
            text = stringResource(R.string.Input),
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Income input
        InputRow(
            label = stringResource(R.string.hint_income),
            value = uiState.inputIncome,
            onValueChange = onIncomeChanged,
            keyboardType = KeyboardType.Decimal
        )

        // Expense input
        InputRow(
            label = stringResource(R.string.hint_spending),
            value = uiState.inputExpense,
            onValueChange = onExpenseChanged,
            keyboardType = KeyboardType.Decimal
        )

        // Person dropdown
        DropdownRow(
            label = stringResource(R.string.hint_person),
            selectedValue = uiState.selectedPerson,
            options = persons,
            onValueChange = onPersonChanged
        )

        // Position dropdown
        DropdownRow(
            label = stringResource(R.string.hint_position),
            selectedValue = uiState.selectedPosition.toString(),
            options = Spending.values().map { it.toString() },
            onValueChange = { onPositionChanged(Spending.valueOf(it)) }
        )

        // Location dropdown
        DropdownRow(
            label = stringResource(R.string.hint_location),
            selectedValue = uiState.selectedLocation.toString(),
            options = Location.values().map { it.toString() },
            onValueChange = { onLocationChanged(Location.valueOf(it)) }
        )

        // Date input
        InputRow(
            label = stringResource(R.string.hint_date),
            value = uiState.inputDate,
            onValueChange = onDateChanged,
            placeholder = stringResource(R.string.hint_date_dd_mm_yyyy),
            keyboardType = KeyboardType.Text
        )

        // Comment input
        InputRow(
            label = stringResource(R.string.hint_comment),
            value = uiState.inputComment,
            onValueChange = onCommentChanged,
            keyboardType = KeyboardType.Text
        )

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Fixed FloatingActionButton without 'enabled' parameter
            val addButtonContainerColor = if (uiState.isAddButtonEnabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant

            val addButtonContentColor = if (uiState.isAddButtonEnabled)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)

            FloatingActionButton(
                onClick = {
                    if (uiState.isAddButtonEnabled) {
                        onAddClicked()
                    }
                },
                containerColor = addButtonContainerColor,
                contentColor = addButtonContentColor
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }

            ExtendedFloatingActionButton(
                onClick = onShowEntriesClicked,
                icon = { Icon(Icons.Default.List, contentDescription = null) },
                text = { Text(stringResource(R.string.button_overview_current_month)) }
            )
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    backgroundColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )
        Text(
            text = value,
            modifier = Modifier
                .weight(1f)
                .background(backgroundColor)
                .padding(8.dp),
            fontSize = 16.sp
        )
    }
}

@Composable
fun InputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(placeholder)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    }
}

@Composable
fun DropdownRow(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )
        Box(
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = { /* Read only */ },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Dropdown"
                        )
                    }
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f) // Slightly narrower than the parent to avoid overflow
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}