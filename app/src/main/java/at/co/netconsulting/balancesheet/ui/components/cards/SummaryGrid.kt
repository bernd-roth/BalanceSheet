package at.co.netconsulting.balancesheet.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import at.co.netconsulting.balancesheet.PersonalFoodSummary
import at.co.netconsulting.balancesheet.R
import at.co.netconsulting.balancesheet.Summary
import at.co.netconsulting.balancesheet.ui.theme.ExpenseRed
import at.co.netconsulting.balancesheet.ui.theme.ExpenseRedLight
import at.co.netconsulting.balancesheet.ui.theme.FoodOrange
import at.co.netconsulting.balancesheet.ui.theme.FoodOrangeLight
import at.co.netconsulting.balancesheet.ui.theme.IncomeGreen
import at.co.netconsulting.balancesheet.ui.theme.IncomeGreenLight
import at.co.netconsulting.balancesheet.ui.theme.SavingsBlue
import at.co.netconsulting.balancesheet.ui.theme.SavingsBlueLight

@Composable
fun SummaryGrid(
    summary: Summary,
    personalFoodSummaries: List<PersonalFoodSummary>,
    modifier: Modifier = Modifier
) {
    val foodSubtitle = buildFoodSubtitle(personalFoodSummaries)

    Column(modifier = modifier.fillMaxWidth()) {
        // First row: Income and Expense
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = stringResource(R.string.sum_income),
                value = summary.totalIncome,
                icon = Icons.Default.KeyboardArrowUp,
                accentColor = IncomeGreen,
                backgroundColor = IncomeGreenLight,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = stringResource(R.string.sum_spending),
                value = summary.totalExpense,
                icon = Icons.Default.KeyboardArrowDown,
                accentColor = ExpenseRed,
                backgroundColor = ExpenseRedLight,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Second row: Savings and Food
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = stringResource(R.string.sum_savings),
                value = summary.totalSavings,
                accentColor = SavingsBlue,
                backgroundColor = SavingsBlueLight,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = stringResource(R.string.sum_food_spending_month),
                value = summary.totalFood,
                subtitle = foodSubtitle,
                accentColor = FoodOrange,
                backgroundColor = FoodOrangeLight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun buildFoodSubtitle(personalFoodSummaries: List<PersonalFoodSummary>): String? {
    if (personalFoodSummaries.isEmpty()) return null

    return personalFoodSummaries
        .filter { it.remainingBudget != 0.0 }
        .joinToString(", ") { "${it.person}: €${String.format("%.0f", it.remainingBudget)}" }
        .takeIf { it.isNotEmpty() }
}
