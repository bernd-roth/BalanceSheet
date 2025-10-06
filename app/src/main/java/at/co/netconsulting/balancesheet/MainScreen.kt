package at.co.netconsulting.balancesheet

import EntryListDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import at.co.netconsulting.balancesheet.composable.*
import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.enums.Position
import at.co.netconsulting.balancesheet.enums.TaxCategory
import at.co.netconsulting.balancesheet.data.MainUiState
import at.co.netconsulting.balancesheet.viewmodel.MainViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
                viewModel = viewModel,
                onIncomeChanged = viewModel::onIncomeChanged,
                onExpenseChanged = viewModel::onExpenseChanged,
                onDateChanged = viewModel::onDateChanged,
                onPersonChanged = viewModel::onPersonChanged,
                onPositionChanged = viewModel::onPositionChanged,
                onTaxCategoryChanged = viewModel::onTaxCategoryChanged,
                onLocationChanged = viewModel::onLocationChanged,
                onCommentChanged = viewModel::onCommentChanged,
                onAddClicked = viewModel::addEntry,
                onShowEntriesClicked = viewModel::showEntriesList,
                onShowAllEntriesClicked = viewModel::showAllEntries
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
                title = uiState.dialogTitle,  // Use the title from UI state
                onDismiss = viewModel::hideEntriesList,
                onEntrySelected = viewModel::showEntryDetails
            )
        }

        // Show currency conversion dialog if needed
        if (uiState.showCurrencyConversionDialog) {
            CurrencyConversionDialog(
                country = uiState.detectedCountry,
                localCurrency = uiState.detectedCurrency,
                defaultCurrency = "EUR", // You could also get this from shared prefs
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
                            viewModel.loadData() // Retry loading data
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

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    uiState: MainUiState,
    persons: List<String>,
    viewModel: MainViewModel,
    onIncomeChanged: (String) -> Unit,
    onExpenseChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onPersonChanged: (String) -> Unit,
    onPositionChanged: (Position) -> Unit,
    onTaxCategoryChanged: (TaxCategory) -> Unit,
    onLocationChanged: (Location) -> Unit,
    onCommentChanged: (String) -> Unit,
    onAddClicked: () -> Unit,
    onShowEntriesClicked: () -> Unit,
    onShowAllEntriesClicked: () -> Unit
) {
    // Define button colors
    val addButtonContainerColor = if (uiState.isAddButtonEnabled)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val addButtonContentColor = if (uiState.isAddButtonEnabled)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)

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

        // Position dropdown - use positions from UI state
        println("DEBUG MainScreen: ======== POSITION DROPDOWN RENDERING ========")
        println("DEBUG MainScreen: availablePositions.size = ${uiState.availablePositions.size}")
        println("DEBUG MainScreen: availablePositions = ${uiState.availablePositions}")
        println("DEBUG MainScreen: Current selected position: ${viewModel.getCurrentPositionValue()}")
        println("DEBUG MainScreen: customDataRefreshTrigger: ${uiState.customDataRefreshTrigger}")
        println("DEBUG MainScreen: uiState hashCode: ${uiState.hashCode()}")

        // Force recomposition by using key with trigger
        key("position-dropdown-${uiState.customDataRefreshTrigger}-${uiState.availablePositions.size}") {
            DropdownRow(
                label = stringResource(R.string.hint_position),
                selectedValue = uiState.selectedPosition.displayName,
                options = uiState.availablePositions.map { it.displayName },
                onValueChange = { displayName ->
                    // Find the position enum by display name and call onPositionChanged
                    val position = uiState.availablePositions.find { it.displayName == displayName }
                    position?.let { onPositionChanged(it) }
                }
            )
        }
        println("DEBUG MainScreen: ======== POSITION DROPDOWN RENDERED ========")

        // Location dropdown - use locations from UI state
        DropdownRow(
            label = stringResource(R.string.hint_location),
            selectedValue = uiState.selectedLocation.displayName,
            options = Location.values().map { it.displayName },
            onValueChange = { displayName ->
                // Find the location enum by display name and call onLocationChanged
                val location = Location.values().find { it.displayName == displayName }
                location?.let { onLocationChanged(it) }
            }
        )

        // Tax Category dropdown
        DropdownRow(
            label = "Tax Category",
            selectedValue = uiState.selectedTaxCategory.displayName,
            options = TaxCategory.values().map { it.displayName },
            onValueChange = { displayName ->
                val taxCategory = TaxCategory.values().find { it.displayName == displayName }
                taxCategory?.let { onTaxCategoryChanged(it) }
            }
        )

        // Date input
        InputRow(
            label = stringResource(R.string.hint_date),
            value = uiState.inputDate,
            onValueChange = onDateChanged,
            placeholder = stringResource(R.string.hint_date_dd_mm_yyyy),
            isDateField = true
        )

        // Comment input
        InputRow(
            label = stringResource(R.string.hint_comment),
            value = uiState.inputComment,
            onValueChange = onCommentChanged,
            keyboardType = KeyboardType.Text
        )

        // Action buttons - single row with proper layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Add button
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

            // Column for the list button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Current month entries button - COMMENTED OUT
                /*
                ExtendedFloatingActionButton(
                    onClick = onShowEntriesClicked,
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    text = { Text(stringResource(R.string.button_overview_current_month)) }
                )
                */

                // All entries button
                ExtendedFloatingActionButton(
                    onClick = onShowAllEntriesClicked,
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    text = { Text("All Entries") }
                )
            }
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
    keyboardType: KeyboardType = KeyboardType.Text,
    isDateField: Boolean = false
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

        if (isDateField) {
            DatePickerField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder,
                modifier = Modifier.weight(1f)
            )
        } else {
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
}

@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    // Parse the date or use current date as fallback
    val date = if (value.isNotEmpty()) {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate.parse(value, formatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
    } else {
        LocalDate.now()
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value.ifEmpty {
                // Show current date by default
                date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            },
            onValueChange = { /* Read-only */ },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(placeholder)
                }
            },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date"
                    )
                }
            }
        )

        // Make the entire field clickable
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDatePicker = true }
        )
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            initialDate = date,
            onDateSelected = { selectedDate ->
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                onValueChange(selectedDate.format(formatter))
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
    )

    // Use a custom Dialog instead of AlertDialog for better control
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false // This allows full width control
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()  // Fill the available width
                .padding(horizontal = 8.dp), // Add some padding from screen edges
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title
                Text(
                    text = "Select Date",
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Date picker with explicit width control
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp) // Add a small horizontal padding
                ) {
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                onDateSelected(selectedDate)
                            }
                            onDismiss()
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
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
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 300.dp) // Limit height to enable proper scrolling
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1
                            )
                        },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                }
            }
        }
    }
}