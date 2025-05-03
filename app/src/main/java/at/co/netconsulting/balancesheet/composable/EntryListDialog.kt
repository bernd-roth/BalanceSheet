import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items  // Add this import
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.co.netconsulting.balancesheet.data.IncomeExpense  // Make sure you use the correct import
import java.time.format.DateTimeFormatter

@Composable
fun EntryListDialog(
    entries: List<IncomeExpense>,
    onDismiss: () -> Unit,
    onEntrySelected: (IncomeExpense) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("May Entries") },
        text = {
            LazyColumn {
                items(entries) { entry ->
                    EntryItem(entry, onEntrySelected)
                    Divider(modifier = Modifier.padding(top = 8.dp))
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
private fun EntryItem(
    entry: IncomeExpense,  // Correct parameter type
    onEntrySelected: (IncomeExpense) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onEntrySelected(entry) }
    ) {
        // ID field
        Text(
            text = "ID: ${entry.id}",
            fontWeight = FontWeight.Bold
        )

        // Name field
        Text(
            text = "Name: ${entry.who}",
            fontWeight = FontWeight.Bold
        )

        // Order date field
        val formattedDate = entry.orderdate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        Text(
            text = "Order Date: $formattedDate",
            fontWeight = FontWeight.Normal
        )

        // Income or expense field
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

        // Position field
        Text(
            text = "Position: ${entry.position}",
            fontWeight = FontWeight.Normal
        )

        // Location field
        Text(
            text = "Location: ${entry.location}",
            fontWeight = FontWeight.Normal
        )

        // Comment field (only if not empty)
        if (entry.comment.isNotEmpty()) {
            Text(
                text = "Comment: ${entry.comment}",
                fontWeight = FontWeight.Normal
            )
        }

        // Created at field
        val createdAtFormatted = if (entry.createdAt != null) {
            entry.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        } else {
            "Not available"
        }

        Text(
            text = "Created At: $createdAtFormatted",
            fontWeight = FontWeight.Normal
        )
    }
}