package at.co.netconsulting.balancesheet.data

import at.co.netconsulting.balancesheet.enums.Position
import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.enums.TaxCategory
import java.time.LocalDate
import java.time.LocalDateTime

data class IncomeExpense(
    val id: String = "",
    val orderdate: LocalDate = LocalDate.now(),
    val who: String = "",
    val position: Position = Position.essen,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val location: Location = Location.Hollgasse_1_1,
    val taxCategory: TaxCategory = TaxCategory.gemeinsam,
    val comment: String = "",
    val createdAt: LocalDateTime? = null
)