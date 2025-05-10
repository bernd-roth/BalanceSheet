package at.co.netconsulting.balancesheet.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
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
import at.co.netconsulting.balancesheet.DatePickerDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EntryDetailsDialog(
    entry: at.co.netconsulting.balancesheet.data.IncomeExpense,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String, String, String, String, String, String) -> Unit
) {
    var id by remember { mutableStateOf(entry.id) }

    // Format the date to display in yyyy-MM-dd format
    var date by remember {
        mutableStateOf(entry.orderdate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    }

    var person by remember { mutableStateOf(entry.who) }
    var location by remember { mutableStateOf(entry.location.toString()) }
    var income by remember { mutableStateOf(entry.income.toString()) }
    var expense by remember { mutableStateOf(entry.expense.toString()) }
    var position by remember { mutableStateOf(entry.position.toString()) }
    var comment by remember { mutableStateOf(entry.comment) }

    // Format created_at for display using the correct property name
    val createdAtFormatted = remember {
        if (entry.createdAt != null) {
            entry.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        } else {
            "Not available"
        }
    }

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

                // Person
                OutlinedTextField(
                    value = person,
                    onValueChange = { person = it },
                    label = { Text("Person") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
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

                // Position
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Position") },
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

                // Created At (read-only)
                OutlinedTextField(
                    value = createdAtFormatted,
                    onValueChange = { /* Read-only */ },
                    label = { Text("Created At") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdate(id, date, person, location, income, expense, position, comment)
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