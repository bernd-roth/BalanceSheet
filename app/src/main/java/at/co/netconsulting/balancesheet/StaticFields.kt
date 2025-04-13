package at.co.netconsulting.general

object StaticFields {
    // Permission codes
    const val CAMERA_PERMISSION_CODE = 100
    const val STORAGE_PERMISSION_CODE = 101
    const val INTERNET_PERMISSION_CODE = 102
    const val WIFI_PERMISSION_CODE = 103

    // Settings Activity
    const val SP_PORT = "PORT"
    const val SP_INTERNET_ADDRESS = "INTERNET_ADDRESS"
    const val SP_PERSON = "PERSON"
    const val SP_MONEY_FOOD = "MONEY_FOOD"
    const val SP_DEFAULT_POSITION = "DEFAULT_POSITION"
    const val SP_DEFAULT_LOCATION = "DEFAULT_LOCATION"

    // Network
    const val PROTOCOL = "http://"
    const val REST_URL_ADD = "/incomeexpense/add"
    const val REST_URL_GET_ALL = "/incomeexpense/all"
    const val REST_URL_GET_SUM_INCOME = "/incomeexpense/sum_income"
    const val REST_URL_GET_SUM_EXPENSE = "/incomeexpense/sum_expense"
    const val REST_URL_GET_SUM_SAVINGS = "/incomeexpense/sum_savings"
    const val REST_URL_GET_SUM_FOOD = "/incomeexpense/sum_food"
    const val REST_URL_GET_AVERAGE_FOOD_DAY_OF_MONTH = "/incomeexpense/sum_average_spending_day_of_month"
    const val REST_URL_GET_SUM_RESERVED_PER_DAY_UNTIL_END_OF_MONTH = "/incomeexpense/sum_reserved_per_day_until_end_of_month"
    const val REST_URL_GET_SUM_SPENDING_FOOD_BEGINNING_OF_YEAR = "/incomeexpense/sum_spending_food_since_beginning_of_year"
    const val REST_URL_GET_SUM_INCOME_YEAR = "/incomeexpense/sum_income_year"
    const val REST_URL_GET_SUM_FOOD_SPEND_PER_PERSON = "/incomeexpense/sum_spending_food_per_person_per_month?person="
    const val REST_URL_GET_SUM_FOOD_SPEND_BERND = "/incomeexpense/sum_spending_food_by_bernd_current_month?bernd_food="
    const val REST_URL_PUT = "/incomeexpense/put/"
    const val COLON = ":"

    // Data types
    const val INCOME = "income"
    const val EXPENSE = "expense"
    const val SAVINGS = "savings"
    const val FOOD = "food"
    const val ALL = "all"
    const val AVERAGE_FOOD = "averageDayPerMonth"
    const val AVERAGE_FOOD_UNTIL_END_OF_MONTH = "averageDayUntilEndOfMonth"
    const val SUM_SPENDING_FOOD_BEGINNING_OF_YEAR = "sumSpendingFoodBeginningOfYear"
    const val SUM_INCOME_YEAR = "sumIncomeYear"
    const val SUM_FOOD_JULIA_MONTH = "sumFoodJuliaMonth"
    const val SUM_FOOD_BERND_MONTH = "sumFoodBerndMonth"
    const val SUM_FOOD_PERSON_MONTH = "sumFoodPersonMonth"
}