package at.co.netconsulting.balancesheet.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import at.co.netconsulting.balancesheet.IncomeExpense

@Composable
fun EntryDetailsDialog(
    entry: IncomeExpense,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String, String, String, String, String, String) -> Unit
) {
    var id by remember { mutableStateOf(entry.id) }
    var date by remember { mutableStateOf(entry.orderdate.toString()) }
    var person by remember { mutableStateOf(entry.who) }
    var location by remember { mutableStateOf(entry.location.toString()) }
    var income by remember { mutableStateOf(entry.income.toString()) }
    var expense by remember { mutableStateOf(entry.expense.toString()) }
    var position by remember { mutableStateOf(entry.position.toString()) }
    var comment by remember { mutableStateOf(entry.comment) }

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

                // Date
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

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
}