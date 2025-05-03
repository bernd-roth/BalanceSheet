package at.co.netconsulting.balancesheet.data

import at.co.netconsulting.balancesheet.enums.Spending
import at.co.netconsulting.balancesheet.enums.Location
import java.time.LocalDate
import java.time.LocalDateTime

data class IncomeExpense(
    val id: String = "",
    val orderdate: LocalDate = LocalDate.now(),
    val who: String = "",
    val position: Spending = Spending.Expense,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val location: Location = Location.Hollgasse_1_1,
    val comment: String = "",
    val createdAt: LocalDateTime? = null
)