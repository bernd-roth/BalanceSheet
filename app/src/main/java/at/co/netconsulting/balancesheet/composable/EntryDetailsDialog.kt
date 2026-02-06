package at.co.netconsulting.balancesheet.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import at.co.netconsulting.balancesheet.composable.DatePickerDialog
import at.co.netconsulting.balancesheet.enums.ExportTo
import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.enums.Position
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EntryDetailsDialog(
    entry: at.co.netconsulting.balancesheet.data.IncomeExpense,
    persons: List<String>,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String, String, String, String, String, String, Boolean, String, Boolean) -> Unit
) {
    var id by remember { mutableStateOf(entry.id) }

    // Format the date to display in yyyy-MM-dd format
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

    // Add state for the date picker
    var showDatePicker by remember { mutableStateOf(false) }

    // Parse the date for the date picker
    val localDate = remember(date) {
        try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Entry") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // ID (disabled)
                OutlinedTextField(
                    value = id,
                    onValueChange = { /* Read-only */ },
                    label = { Text("ID") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Date Field with Date Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
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

                // Person dropdown
                DropdownField(
                    label = "Person",
                    selectedValue = person,
                    options = persons,
                    onValueChange = { person = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Location dropdown
                DropdownField(
                    label = "Location",
                    selectedValue = location,
                    options = Location.values().map { it.name },
                    onValueChange = { location = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Income
                OutlinedTextField(
                    value = income,
                    onValueChange = { income = it },
                    label = { Text("Income") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Expense
                OutlinedTextField(
                    value = expense,
                    onValueChange = { expense = it },
                    label = { Text("Expense") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Position dropdown
                DropdownField(
                    label = "Position",
                    selectedValue = position,
                    options = Position.values().map { it.name },
                    onValueChange = { position = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Comment
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Taxable checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Taxable",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    androidx.compose.material3.Checkbox(
                        checked = taxable,
                        onCheckedChange = { taxable = it }
                    )
                }

                // Export To dropdown
                DropdownField(
                    label = "Export To",
                    selectedValue = exportTo,
                    options = ExportTo.values().map { it.name },
                    onValueChange = { exportTo = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Info Only checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Info Only (exclude from balance)",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    androidx.compose.material3.Checkbox(
                        checked = isInfoOnly,
                        onCheckedChange = { isInfoOnly = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdate(id, date, person, location, income, expense, position, comment, taxable, exportTo, isInfoOnly)
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            initialDate = localDate,  // Use localDate instead of the string date
            onDateSelected = { selectedDate ->
                // Use yyyy-MM-dd format for the backend
                date = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun DropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = { /* Read only */ },
            readOnly = true,
            label = { Text(label) },
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
                .heightIn(max = 300.dp)
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