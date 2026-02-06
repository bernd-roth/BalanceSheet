package at.co.netconsulting.balancesheet.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import at.co.netconsulting.balancesheet.data.IncomeExpense
import at.co.netconsulting.balancesheet.ui.components.common.EntryListItem
import at.co.netconsulting.balancesheet.ui.theme.ExpenseRed
import at.co.netconsulting.balancesheet.ui.theme.IncomeGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class EntryFilter {
    ALL, INCOME, EXPENSE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntriesScreen(
    entries: List<IncomeExpense>,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onEntryClick: (IncomeExpense) -> Unit,
    onRefresh: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(EntryFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    val filteredEntries = remember(entries, selectedFilter, searchQuery) {
        entries
            .filter { entry ->
                when (selectedFilter) {
                    EntryFilter.ALL -> true
                    EntryFilter.INCOME -> entry.income > 0
                    EntryFilter.EXPENSE -> entry.expense > 0
                }
            }
            .filter { entry ->
                if (searchQuery.isBlank()) true
                else {
                    val query = searchQuery.lowercase().trim()
                    // Search in text fields
                    entry.who.lowercase().contains(query) ||
                    entry.position.displayName.lowercase().contains(query) ||
                    entry.comment.lowercase().contains(query) ||
                    entry.location.displayName.lowercase().contains(query) ||
                    // Search in amounts
                    entry.income.toString().contains(query) ||
                    entry.expense.toString().contains(query) ||
                    // Search in formatted date
                    entry.orderdate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).contains(query) ||
                    entry.orderdate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).contains(query)
                }
            }
            .sortedByDescending { it.orderdate }
    }

    val groupedEntries = remember(filteredEntries) {
        groupEntriesByDate(filteredEntries)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Entries") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                        Icon(
                            imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchVisible) "Close search" else "Search"
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
        ) {
            // Search bar
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search entries...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    singleLine = true
                )
            }

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == EntryFilter.ALL,
                    onClick = { selectedFilter = EntryFilter.ALL },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedFilter == EntryFilter.INCOME,
                    onClick = { selectedFilter = EntryFilter.INCOME },
                    label = { Text("Income") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                        selectedLabelColor = IncomeGreen
                    )
                )
                FilterChip(
                    selected = selectedFilter == EntryFilter.EXPENSE,
                    onClick = { selectedFilter = EntryFilter.EXPENSE },
                    label = { Text("Expense") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExpenseRed.copy(alpha = 0.2f),
                        selectedLabelColor = ExpenseRed
                    )
                )
            }

            // Entries count
            Text(
                text = "${filteredEntries.size} entries",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Loading indicator
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Content
            if (filteredEntries.isEmpty() && !isLoading) {
                EmptyState(
                    filter = selectedFilter,
                    hasSearchQuery = searchQuery.isNotEmpty()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedEntries.forEach { (dateGroup, entriesInGroup) ->
                        item(key = "header_$dateGroup") {
                            DateGroupHeader(dateGroup)
                        }

                        items(
                            items = entriesInGroup,
                            key = { it.id.ifEmpty { "${it.orderdate}_${it.hashCode()}" } }
                        ) { entry ->
                            EntryCard(
                                entry = entry,
                                onClick = { onEntryClick(entry) }
                            )
                        }

                        item(key = "spacer_$dateGroup") {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateGroupHeader(dateGroup: String) {
    Text(
        text = dateGroup,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryCard(
    entry: IncomeExpense,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        EntryListItem(
            entry = entry,
            onClick = onClick,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyState(
    filter: EntryFilter,
    hasSearchQuery: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = when {
                    hasSearchQuery -> "No matching entries"
                    filter == EntryFilter.INCOME -> "No income entries"
                    filter == EntryFilter.EXPENSE -> "No expense entries"
                    else -> "No entries yet"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    hasSearchQuery -> "Try adjusting your search terms"
                    filter != EntryFilter.ALL -> "Try selecting a different filter"
                    else -> "Add your first entry from the dashboard"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun groupEntriesByDate(entries: List<IncomeExpense>): List<Pair<String, List<IncomeExpense>>> {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val weekAgo = today.minusWeeks(1)
    val monthAgo = today.minusMonths(1)

    val grouped = entries.groupBy { entry ->
        when {
            entry.orderdate == today -> "Today"
            entry.orderdate == yesterday -> "Yesterday"
            entry.orderdate.isAfter(weekAgo) -> "This Week"
            entry.orderdate.isAfter(monthAgo) -> "This Month"
            entry.orderdate.year == today.year -> entry.orderdate.format(DateTimeFormatter.ofPattern("MMMM"))
            else -> entry.orderdate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        }
    }

    // Define the order of groups
    val order = listOf("Today", "Yesterday", "This Week", "This Month")

    return grouped.entries
        .sortedWith(compareBy {
            val index = order.indexOf(it.key)
            if (index >= 0) index else Int.MAX_VALUE
        })
        .map { it.key to it.value }
}
