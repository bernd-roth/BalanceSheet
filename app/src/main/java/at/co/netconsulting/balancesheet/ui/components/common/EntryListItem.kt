package at.co.netconsulting.balancesheet.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.co.netconsulting.balancesheet.data.IncomeExpense
import at.co.netconsulting.balancesheet.ui.theme.ExpenseRed
import at.co.netconsulting.balancesheet.ui.theme.IncomeGreen
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EntryListItem(
    entry: IncomeExpense,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")
    val formattedDate = entry.orderdate.format(dateFormatter)

    val isIncome = entry.income > 0
    val amount = if (isIncome) entry.income else entry.expense
    val formattedAmount = formatAmount(amount, isIncome)
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                Text(
                    text = entry.position.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (entry.who.isNotEmpty()) {
                    Text(
                        text = entry.who,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            text = formattedAmount,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

private fun formatAmount(value: Double, isIncome: Boolean): String {
    val formatter = NumberFormat.getNumberInstance(Locale.GERMAN).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val prefix = if (isIncome) "+" else "-"
    return "$prefix€${formatter.format(value)}"
}
