package at.co.netconsulting.balancesheet.data

data class Summary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalSavings: Double = 0.0,
    val totalFood: Double = 0.0,
    val averageFoodPerDay: Double = 0.0,
    val reservedFoodPerDay: Double = 0.0,
    val totalYearFood: Double = 0.0,
    val totalYearIncome: Double = 0.0
)

data class PersonalFoodSummary(
    val person: String,
    val remainingBudget: Double
)