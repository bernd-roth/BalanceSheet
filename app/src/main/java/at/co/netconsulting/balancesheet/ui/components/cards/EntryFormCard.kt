package at.co.netconsulting.balancesheet.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.co.netconsulting.balancesheet.R
import at.co.netconsulting.balancesheet.enums.ExportTo
import at.co.netconsulting.balancesheet.enums.Position
import at.co.netconsulting.balancesheet.ui.components.common.AmountField
import at.co.netconsulting.balancesheet.ui.components.common.CheckboxField
import at.co.netconsulting.balancesheet.ui.components.common.CommentField
import at.co.netconsulting.balancesheet.ui.components.common.DateField
import at.co.netconsulting.balancesheet.ui.components.common.DropdownField

@Composable
fun EntryFormCard(
    expanded: Boolean,
    onExpandChange: () -> Unit,
    inputIncome: String,
    inputExpense: String,
    selectedPerson: String,
    selectedPosition: Position,
    selectedLocationString: String,
    inputDate: String,
    inputComment: String,
    inputTaxable: Boolean,
    inputExportTo: ExportTo,
    inputInfoOnly: Boolean,
    isAddButtonEnabled: Boolean,
    persons: List<String>,
    availablePositions: List<Position>,
    availableLocations: List<String>,
    onIncomeChanged: (String) -> Unit,
    onExpenseChanged: (String) -> Unit,
    onPersonChanged: (String) -> Unit,
    onPositionChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onDatePickerClick: () -> Unit,
    onCommentChanged: (String) -> Unit,
    onTaxableChanged: (Boolean) -> Unit,
    onExportToChanged: (ExportTo) -> Unit,
    onInfoOnlyChanged: (Boolean) -> Unit,
    onAddClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header - always visible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandChange() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.Input),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            // Animated content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider()

                    // Income and Expense fields in a row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AmountField(
                            value = inputIncome,
                            onValueChange = onIncomeChanged,
                            label = stringResource(R.string.hint_income),
                            modifier = Modifier.weight(1f)
                        )
                        AmountField(
                            value = inputExpense,
                            onValueChange = onExpenseChanged,
                            label = stringResource(R.string.hint_spending),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Person dropdown
                    DropdownField(
                        label = stringResource(R.string.hint_person),
                        selectedValue = selectedPerson,
                        options = persons,
                        onValueChange = onPersonChanged
                    )

                    // Position dropdown
                    DropdownField(
                        label = stringResource(R.string.hint_position),
                        selectedValue = selectedPosition.displayName,
                        options = availablePositions.map { it.displayName },
                        onValueChange = onPositionChanged
                    )

                    // Location dropdown
                    DropdownField(
                        label = stringResource(R.string.hint_location),
                        selectedValue = selectedLocationString,
                        options = availableLocations,
                        onValueChange = onLocationChanged
                    )

                    // Date field
                    DateField(
                        value = inputDate,
                        onValueChange = onDateChanged,
                        label = stringResource(R.string.hint_date),
                        onDatePickerClick = onDatePickerClick
                    )

                    // Comment field
                    CommentField(
                        value = inputComment,
                        onValueChange = onCommentChanged,
                        label = stringResource(R.string.hint_comment)
                    )

                    // Checkboxes and Export dropdown
                    CheckboxField(
                        label = stringResource(R.string.hint_taxable),
                        checked = inputTaxable,
                        onCheckedChange = onTaxableChanged
                    )

                    // Export To dropdown
                    DropdownField(
                        label = "Export To",
                        selectedValue = inputExportTo.displayName,
                        options = ExportTo.values().map { it.displayName },
                        onValueChange = { displayName ->
                            val exportTo = ExportTo.values().find { it.displayName == displayName }
                            exportTo?.let { onExportToChanged(it) }
                        }
                    )

                    CheckboxField(
                        label = "Info Only (exclude from balance)",
                        checked = inputInfoOnly,
                        onCheckedChange = onInfoOnlyChanged
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Add button
                    Button(
                        onClick = onAddClicked,
                        enabled = isAddButtonEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Entry")
                    }
                }
            }
        }
    }
}
