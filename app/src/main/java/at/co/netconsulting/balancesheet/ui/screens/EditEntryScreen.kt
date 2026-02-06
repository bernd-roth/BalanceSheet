package at.co.netconsulting.balancesheet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.heightIn
import at.co.netconsulting.balancesheet.composable.DatePickerDialog
import at.co.netconsulting.balancesheet.data.IncomeExpense
import at.co.netconsulting.balancesheet.enums.ExportTo
import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.enums.Position
import at.co.netconsulting.balancesheet.ui.theme.ExpenseRed
import at.co.netconsulting.balancesheet.ui.theme.ExpenseRedLight
import at.co.netconsulting.balancesheet.ui.theme.IncomeGreen
import at.co.netconsulting.balancesheet.ui.theme.IncomeGreenLight
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    entry: IncomeExpense,
    persons: List<String>,
    onNavigateBack: () -> Unit,
    onSave: (String, String, String, String, String, String, String, String, Boolean, String, Boolean) -> Unit
) {
    // Form state
    var date by remember {
        mutableStateOf(entry.orderdate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    }
    var person by remember { mutableStateOf(entry.who) }
    var location by remember { mutableStateOf(entry.location.name) }
    var income by remember { mutableStateOf(entry.income.toString()) }
    var expense by remember { mutableStateOf(entry.expense.toString()) }
    var position by remember { mutableStateOf(entry.position.name) }
    var comment by remember { mutableStateOf(entry.comment) }
    var taxable by remember { mutableStateOf(entry.taxable) }
    var exportTo by remember { mutableStateOf(entry.exportTo.name) }
    var isInfoOnly by remember { mutableStateOf(entry.isInfoOnly) }

    var showDatePicker by remember { mutableStateOf(false) }

    val isIncome = entry.income > 0
    val displayAmount = if (isIncome) entry.income else entry.expense

    val localDate = remember(date) {
        try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Card
            AmountCard(
                isIncome = isIncome,
                income = income,
                expense = expense,
                onIncomeChange = { income = it },
                onExpenseChange = { expense = it }
            )

            // Details Card
            DetailsCard(
                date = date,
                person = person,
                position = position,
                location = location,
                comment = comment,
                persons = persons,
                onDateClick = { showDatePicker = true },
                onPersonChange = { person = it },
                onPositionChange = { position = it },
                onLocationChange = { location = it },
                onCommentChange = { comment = it }
            )

            // Options Card
            OptionsCard(
                taxable = taxable,
                isInfoOnly = isInfoOnly,
                exportTo = exportTo,
                onTaxableChange = { taxable = it },
                onInfoOnlyChange = { isInfoOnly = it },
                onExportToChange = { exportTo = it }
            )

            // ID display (read-only info)
            Text(
                text = "ID: ${entry.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onSave(
                            entry.id,
                            date,
                            person,
                            location,
                            income,
                            expense,
                            position,
                            comment,
                            taxable,
                            exportTo,
                            isInfoOnly
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Changes")
                }
            }
        }

        // Date picker dialog
        if (showDatePicker) {
            DatePickerDialog(
                initialDate = localDate,
                onDateSelected = { selectedDate ->
                    date = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
private fun AmountCard(
    isIncome: Boolean,
    income: String,
    expense: String,
    onIncomeChange: (String) -> Unit,
    onExpenseChange: (String) -> Unit
) {
    val accentColor = if (isIncome) IncomeGreen else ExpenseRed
    val backgroundColor = if (isIncome) IncomeGreenLight else ExpenseRedLight

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "AMOUNT",
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = income,
                    onValueChange = onIncomeChange,
                    label = { Text("Income") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = expense,
                    onValueChange = onExpenseChange,
                    label = { Text("Expense") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DetailsCard(
    date: String,
    person: String,
    position: String,
    location: String,
    comment: String,
    persons: List<String>,
    onDateClick: () -> Unit,
    onPersonChange: (String) -> Unit,
    onPositionChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onCommentChange: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "DETAILS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Divider()

            // Date field
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formatDateForDisplay(date),
                    onValueChange = { },
                    label = { Text("Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date"
                        )
                    }
                )
                Spacer(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { onDateClick() }
                )
            }

            // Person dropdown
            DropdownFieldCard(
                label = "Person",
                selectedValue = person,
                options = persons,
                onValueChange = onPersonChange
            )

            // Position dropdown
            DropdownFieldCard(
                label = "Position",
                selectedValue = position,
                options = Position.values().map { it.name },
                onValueChange = onPositionChange
            )

            // Location dropdown
            DropdownFieldCard(
                label = "Location",
                selectedValue = location,
                options = Location.values().map { it.name },
                onValueChange = onLocationChange
            )

            // Comment field
            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                label = { Text("Comment") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}

@Composable
private fun OptionsCard(
    taxable: Boolean,
    isInfoOnly: Boolean,
    exportTo: String,
    onTaxableChange: (Boolean) -> Unit,
    onInfoOnlyChange: (Boolean) -> Unit,
    onExportToChange: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "OPTIONS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Divider()

            // Taxable checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTaxableChange(!taxable) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = taxable,
                    onCheckedChange = onTaxableChange
                )
                Text(
                    text = "Taxable",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Info Only checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInfoOnlyChange(!isInfoOnly) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isInfoOnly,
                    onCheckedChange = onInfoOnlyChange
                )
                Text(
                    text = "Info Only (exclude from balance)",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Export To dropdown
            DropdownFieldCard(
                label = "Export To",
                selectedValue = exportTo,
                options = ExportTo.values().map { it.name },
                onValueChange = onExportToChange
            )
        }
    }
}

@Composable
private fun DropdownFieldCard(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown"
                )
            }
        )

        // Make the entire field clickable
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp)
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

private fun formatDateForDisplay(date: String): String {
    return try {
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        date
    }
}
