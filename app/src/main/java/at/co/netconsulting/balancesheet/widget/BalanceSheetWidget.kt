package at.co.netconsulting.balancesheet.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import at.co.netconsulting.balancesheet.MainActivity
import at.co.netconsulting.balancesheet.data.IncomeExpense
import at.co.netconsulting.balancesheet.network.BalanceSheetRepository
import at.co.netconsulting.general.StaticFields
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class BalanceSheetWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences("BalanceSheetPrefs", Context.MODE_PRIVATE)
        val serverIp = prefs.getString(StaticFields.SP_INTERNET_ADDRESS, "balancesheet.duckdns.org") ?: "balancesheet.duckdns.org"
        val serverPort = prefs.getString(StaticFields.SP_PORT, "8080") ?: "8080"
        val personsString = prefs.getString(StaticFields.SP_PERSON, "") ?: ""
        val defaultReserve = prefs.getString(StaticFields.SP_MONEY_FOOD, "0") ?: "0"

        val baseUrl = "${StaticFields.PROTOCOL}${serverIp}${StaticFields.COLON}${serverPort}"
        val repository = BalanceSheetRepository(baseUrl)

        var income = 0.0
        var expense = 0.0
        var savings = 0.0
        var food = 0.0
        var recentEntries = emptyList<IncomeExpense>()
        var personSummaries = emptyList<Pair<String, Double>>()
        var errorMessage: String? = null

        try {
            withContext(Dispatchers.IO) {
                income = repository.getTotalIncome()
                expense = repository.getTotalExpense()
                savings = repository.getTotalSavings()
                food = repository.getTotalFood()

                val entries = repository.getAllEntries()
                recentEntries = entries
                    .sortedByDescending { it.orderdate }
                    .take(10)

                if (personsString.isNotEmpty()) {
                    val persons = personsString.split(" ")
                    personSummaries = persons.map { person ->
                        val budget = repository.getPersonFoodSummary(person, defaultReserve)
                        person to budget
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = "Unable to load data"
        }

        provideContent {
            GlanceTheme {
                WidgetContent(
                    income = income,
                    expense = expense,
                    savings = savings,
                    food = food,
                    personSummaries = personSummaries,
                    recentEntries = recentEntries,
                    errorMessage = errorMessage
                )
            }
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")

private fun formatCurrency(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.GERMAN).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "\u20AC${formatter.format(value)}"
}

// Widget colors
private val widgetBackground = ColorProvider(Color(0xFF1A1A2E))
private val cardBackground = ColorProvider(Color(0xFF16213E))
private val textPrimary = ColorProvider(Color.White)
private val textSecondary = ColorProvider(Color(0xFFB0B0B0))
private val incomeColor = ColorProvider(Color(0xFF4CAF50))
private val expenseColor = ColorProvider(Color(0xFFF44336))
private val savingsColor = ColorProvider(Color(0xFF2196F3))
private val foodColor = ColorProvider(Color(0xFFFF9800))

@Composable
private fun WidgetContent(
    income: Double,
    expense: Double,
    savings: Double,
    food: Double,
    personSummaries: List<Pair<String, Double>>,
    recentEntries: List<IncomeExpense>,
    errorMessage: String?
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(widgetBackground)
            .clickable(actionStartActivity<MainActivity>())
            .padding(8.dp)
    ) {
        if (errorMessage != null) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = errorMessage,
                    style = TextStyle(color = textSecondary, fontSize = 14.sp)
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "Tap to open app",
                    style = TextStyle(color = textSecondary, fontSize = 12.sp)
                )
            }
        } else {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // Summary grid: 2x2
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    SummaryCell(
                        label = "INCOME",
                        value = formatCurrency(income),
                        color = incomeColor,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    SummaryCell(
                        label = "SPENDING",
                        value = formatCurrency(expense),
                        color = expenseColor,
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    SummaryCell(
                        label = "SAVINGS",
                        value = formatCurrency(savings),
                        color = savingsColor,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    val foodSubtitle = personSummaries.joinToString(", ") {
                        "${it.first}: ${formatCurrency(it.second)}"
                    }
                    SummaryCell(
                        label = "FOOD/MONTH",
                        value = formatCurrency(food),
                        color = foodColor,
                        subtitle = foodSubtitle.ifEmpty { null },
                        modifier = GlanceModifier.defaultWeight()
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Recent entries header
                Text(
                    text = "RECENT ENTRIES",
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.padding(start = 4.dp, bottom = 2.dp)
                )

                // Recent entries list
                if (recentEntries.isEmpty()) {
                    Text(
                        text = "No entries this month",
                        style = TextStyle(color = textSecondary, fontSize = 12.sp),
                        modifier = GlanceModifier.padding(start = 4.dp)
                    )
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                        items(recentEntries) { entry ->
                            EntryRow(entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCell(
    label: String,
    value: String,
    color: ColorProvider,
    modifier: GlanceModifier = GlanceModifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .background(cardBackground)
            .padding(8.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = value,
            style = TextStyle(
                color = textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = TextStyle(
                    color = textSecondary,
                    fontSize = 9.sp
                ),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun EntryRow(entry: IncomeExpense) {
    val isIncome = entry.income > 0
    val amount = if (isIncome) entry.income else entry.expense
    val sign = if (isIncome) "+" else "-"
    val amountColor = if (isIncome) incomeColor else expenseColor
    val dateStr = entry.orderdate.format(dateFormatter)

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateStr,
            style = TextStyle(color = textSecondary, fontSize = 11.sp),
            modifier = GlanceModifier.width(40.dp)
        )
        Text(
            text = entry.position.displayName,
            style = TextStyle(color = textPrimary, fontSize = 11.sp),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
        Text(
            text = "$sign${formatCurrency(amount)}",
            style = TextStyle(
                color = amountColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
