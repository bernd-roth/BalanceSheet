package at.co.netconsulting.balancesheet.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.co.netconsulting.balancesheet.IncomeExpense
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun EntryListDialog(
    entries: List<IncomeExpense>,
    onDismiss: () -> Unit,
    onEntrySelected: (IncomeExpense) -> Unit
) {
    val currentMonth = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$currentMonth Entries") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No entries for this month")
                    }
                } else {
                    LazyColumn {
                        items(entries) { entry ->
                            EntryListItem(
                                entry = entry,
                                onClick = {
                                    onEntrySelected(entry)
                                    onDismiss()
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun EntryListItem(
    entry: IncomeExpense,
    onClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formattedDate = entry.orderdate.format(dateFormatter)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ID: ${entry.id}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = entry.who,
                fontWeight = FontWeight.Bold
            )
            Text(text = entry.location.toString())
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (entry.income > 0) {
                Text(
                    text = "Income: ${entry.income}",
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "Expense: ${entry.expense}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            Text(text = entry.position.toString())
        }

        if (entry.comment.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Comment: ${entry.comment}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}