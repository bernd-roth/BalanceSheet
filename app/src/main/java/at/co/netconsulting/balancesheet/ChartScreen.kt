package at.co.netconsulting.balancesheet

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.max
import at.co.netconsulting.balancesheet.viewmodel.ChartViewModel
import at.co.netconsulting.balancesheet.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    viewModel: ChartViewModel,
    onNavigateBack: () -> Unit
) {
    // val uiState by viewModel.uiState.collectAsState() // Not needed for current implementation

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Financial Overview",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Cards Row
            FinancialSummaryCards(
                income = viewModel.totalIncome,
                expense = viewModel.totalExpense,
                savings = viewModel.totalSavings,
                food = viewModel.totalFood
            )

            // Modern Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Interactive Bar Chart",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ModernBarChart(
                        income = viewModel.totalIncome,
                        expense = viewModel.totalExpense,
                        savings = viewModel.totalSavings,
                        food = viewModel.totalFood,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            }

            // Modern Legend
            ModernChartLegend()
        }
    }
}

@Composable
fun FinancialSummaryCards(
    income: Float,
    expense: Float,
    savings: Float,
    food: Float
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            SummaryCard(
                title = "Income",
                value = "€${String.format("%.2f", income)}",
                icon = Icons.Default.Add,
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            )
        }
        item {
            SummaryCard(
                title = "Expense",
                value = "€${String.format("%.2f", expense)}",
                icon = Icons.Default.ShoppingCart,
                containerColor = Color(0xFFF44336),
                contentColor = Color.White
            )
        }
        item {
            SummaryCard(
                title = "Savings",
                value = "€${String.format("%.2f", savings)}",
                icon = Icons.Default.AccountCircle,
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            )
        }
        item {
            SummaryCard(
                title = "Food",
                value = "€${String.format("%.2f", food)}",
                icon = Icons.Default.Star,
                containerColor = Color(0xFFFF9800),
                contentColor = Color.White
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedElevation by animateFloatAsState(
        targetValue = 8f,
        animationSpec = tween(300),
        label = "elevation"
    )

    Card(
        modifier = modifier
            .width(160.dp)
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ModernBarChart(
    income: Float,
    expense: Float,
    savings: Float,
    food: Float,
    modifier: Modifier = Modifier
) {
    val maxValue = max(max(income, expense), max(savings, food)).takeIf { it > 0 } ?: 1f

    // Animation for chart bars
    val animatedIncome by animateFloatAsState(
        targetValue = income,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "income"
    )
    val animatedExpense by animateFloatAsState(
        targetValue = expense,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "expense"
    )
    val animatedSavings by animateFloatAsState(
        targetValue = savings,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "savings"
    )
    val animatedFood by animateFloatAsState(
        targetValue = food,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "food"
    )

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height - 40.dp.toPx() // Leave space for labels

            val barWidth = canvasWidth / 6f // More space between bars
            val spacing = barWidth * 0.5f

            // Calculate heights proportionally
            val incomeHeight = (animatedIncome / maxValue) * canvasHeight * 0.85f
            val expenseHeight = (animatedExpense / maxValue) * canvasHeight * 0.85f
            val savingsHeight = (animatedSavings / maxValue) * canvasHeight * 0.85f
            val foodHeight = (animatedFood / maxValue) * canvasHeight * 0.85f

            // Define modern colors with gradients
            val incomeGradient = Brush.verticalGradient(
                colors = listOf(Color(0xFF81C784), Color(0xFF4CAF50))
            )
            val expenseGradient = Brush.verticalGradient(
                colors = listOf(Color(0xFFE57373), Color(0xFFF44336))
            )
            val savingsGradient = Brush.verticalGradient(
                colors = listOf(Color(0xFF64B5F6), Color(0xFF2196F3))
            )
            val foodGradient = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFB74D), Color(0xFFFF9800))
            )

            // Draw bars with rounded corners
            drawRoundedBar(spacing, canvasHeight, barWidth, incomeHeight, incomeGradient)
            drawRoundedBar(spacing * 3 + barWidth, canvasHeight, barWidth, expenseHeight, expenseGradient)
            drawRoundedBar(spacing * 5 + barWidth * 2, canvasHeight, barWidth, savingsHeight, savingsGradient)
            drawRoundedBar(spacing * 7 + barWidth * 3, canvasHeight, barWidth, foodHeight, foodGradient)

            // Draw value labels on top of bars
            drawValueLabel(this, spacing + barWidth/2, canvasHeight - incomeHeight - 20f, animatedIncome)
            drawValueLabel(this, spacing * 3 + barWidth * 1.5f, canvasHeight - expenseHeight - 20f, animatedExpense)
            drawValueLabel(this, spacing * 5 + barWidth * 2.5f, canvasHeight - savingsHeight - 20f, animatedSavings)
            drawValueLabel(this, spacing * 7 + barWidth * 3.5f, canvasHeight - foodHeight - 20f, animatedFood)
        }

        // Modern labels below chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ChartLabel("Income", Color(0xFF4CAF50))
            ChartLabel("Expense", Color(0xFFF44336))
            ChartLabel("Savings", Color(0xFF2196F3))
            ChartLabel("Food", Color(0xFFFF9800))
        }
    }
}

fun DrawScope.drawRoundedBar(x: Float, canvasHeight: Float, width: Float, height: Float, brush: Brush) {
    drawRoundRect(
        brush = brush,
        topLeft = Offset(x, canvasHeight - height),
        size = Size(width, height),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )
}

fun drawValueLabel(drawScope: DrawScope, x: Float, y: Float, value: Float) {
    // This would require more complex text drawing - simplified for now
    // In a real implementation, you'd use drawText with proper text measurements
}

@Composable
fun ChartLabel(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ModernChartLegend() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Legend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    listOf(
                        LegendData("Income", Color(0xFF4CAF50), Icons.Default.Add),
                        LegendData("Expense", Color(0xFFF44336), Icons.Default.ShoppingCart),
                        LegendData("Savings", Color(0xFF2196F3), Icons.Default.AccountCircle),
                        LegendData("Food", Color(0xFFFF9800), Icons.Default.Star)
                    )
                ) { legendData ->
                    ModernLegendItem(
                        color = legendData.color,
                        text = legendData.text,
                        icon = legendData.icon
                    )
                }
            }
        }
    }
}

data class LegendData(
    val text: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun ModernLegendItem(
    color: Color,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}