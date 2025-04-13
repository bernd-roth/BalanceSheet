package at.co.netconsulting.balancesheet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import at.co.netconsulting.balancesheet.viewmodel.ChartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    viewModel: ChartViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_barchart)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(bottom = 16.dp, start = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = "Financial Overview",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Chart
                BarChart(
                    income = uiState.totalIncome,
                    expense = uiState.totalExpense,
                    savings = uiState.totalSavings,
                    food = uiState.totalFood,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )

                // Legend
                ChartLegend(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                )
            }
        }
    }
}

@Composable
fun BarChart(
    income: Float,
    expense: Float,
    savings: Float,
    food: Float,
    modifier: Modifier = Modifier
) {
    // Find the maximum value for scaling
    val maxValue = maxOf(income, expense, savings, food)

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val barWidth = canvasWidth / 8 // We need space for 4 bars with spacing
        val spacing = barWidth / 2

        // Calculate heights proportionally
        val incomeHeight = (income / maxValue) * canvasHeight * 0.8f
        val expenseHeight = (expense / maxValue) * canvasHeight * 0.8f
        val savingsHeight = (savings / maxValue) * canvasHeight * 0.8f
        val foodHeight = (food / maxValue) * canvasHeight * 0.8f

        // Income bar (green)
        drawRect(
            color = Color.Green,
            topLeft = Offset(spacing, canvasHeight - incomeHeight),
            size = Size(barWidth, incomeHeight)
        )

        // Expense bar (red)
        drawRect(
            color = Color.Red,
            topLeft = Offset(spacing * 3 + barWidth, canvasHeight - expenseHeight),
            size = Size(barWidth, expenseHeight)
        )

        // Savings bar (blue)
        drawRect(
            color = Color.Blue,
            topLeft = Offset(spacing * 5 + barWidth * 2, canvasHeight - savingsHeight),
            size = Size(barWidth, savingsHeight)
        )

        // Food bar (yellow)
        drawRect(
            color = Color.Yellow,
            topLeft = Offset(spacing * 7 + barWidth * 3, canvasHeight - foodHeight),
            size = Size(barWidth, foodHeight)
        )

        // Draw horizontal axis
        drawLine(
            color = Color.Gray,
            start = Offset(0f, canvasHeight),
            end = Offset(canvasWidth, canvasHeight),
            strokeWidth = 2f
        )
    }

    // Labels below chart
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text("Income", color = Color.Green)
        Text("Expense", color = Color.Red)
        Text("Savings", color = Color.Blue)
        Text("Food", color = Color.Yellow)
    }
}

@Composable
fun ChartLegend(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        LegendItem(color = Color.Green, text = "Income")
        LegendItem(color = Color.Red, text = "Expense")
        LegendItem(color = Color.Blue, text = "Savings")
        LegendItem(color = Color.Yellow, text = "Food")
    }
}

@Composable
fun LegendItem(
    color: Color,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}