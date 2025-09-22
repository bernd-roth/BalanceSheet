package at.co.netconsulting.balancesheet

import at.co.netconsulting.balancesheet.enums.Spending
import at.co.netconsulting.balancesheet.enums.Location
import java.time.LocalDate
import java.time.LocalDateTime

data class IncomeExpense(
    val id: String = "",
    val orderdate: LocalDate = LocalDate.now(),
    val who: String = "",
    val position: Spending = Spending.Food,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val location: Location = Location.Hollgasse_1_1,
    val comment: String = "",
    val createdAt: LocalDateTime? = null
)

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