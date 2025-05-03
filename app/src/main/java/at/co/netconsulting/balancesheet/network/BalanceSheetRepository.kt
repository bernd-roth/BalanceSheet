package at.co.netconsulting.balancesheet.network

import at.co.netconsulting.balancesheet.enums.Location
import at.co.netconsulting.balancesheet.enums.Spending
import at.co.netconsulting.balancesheet.data.IncomeExpense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit
import at.co.netconsulting.general.StaticFields
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.time.LocalDateTime
import java.time.ZonedDateTime

/**
 * Repository for handling all network operations related to the balance sheet data.
 * Uses OkHttp for network requests.
 */
class BalanceSheetRepository(private val baseUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Get the total income for the current month.
     */
    suspend fun getTotalIncome(): Double = withContext(Dispatchers.IO) {
        val url = "$baseUrl${StaticFields.REST_URL_GET_SUM_INCOME}"
        val responseJson = makeGetRequest(url)
        parseValueFromResponse(responseJson)
    }

    /**
     * Get the total expenses for the current month.
     */
    suspend fun getTotalExpense(): Double = withContext(Dispatchers.IO) {
        val url = "$baseUrl${StaticFields.REST_URL_GET_SUM_EXPENSE}"
        val responseJson = makeGetRequest(url)
        parseValueFromResponse(responseJson)
    }

    /**
     * Get the total savings for the current month.
     */
    suspend fun getTotalSavings(): Double = withContext(Dispatchers.IO) {
        val url = "$baseUrl${StaticFields.REST_URL_GET_SUM_SAVINGS}"
        val responseJson = makeGetRequest(url)
        parseValueFromResponse(responseJson)
    }

    /**
     * Get the total food expenses for the current month.
     */
    suspend fun getTotalFood(): Double = withContext(Dispatchers.IO) {
        val url = "$baseUrl${StaticFields.REST_URL_GET_SUM_FOOD}"
        val responseJson = makeGetRequest(url)
        parseValueFromResponse(responseJson)
    }

    /**
     * Get the average food expenses per day for the current month.
     */
    suspend fun getAverageFoodPerDay(): Double = withContext(Dispatchers.IO) {
        val url = "$baseUrl${StaticFields.REST_URL_GET_AVERAGE_FOOD_DAY_OF_MONTH}"
        val responseJson = makeGetRequest(url)
        parseValueFromResponse(responseJson)
    }

    /**
     * Get the reserved food expenses per day until the end of the month.
     */
    suspend fun getReservedFoodPerDay(): Double = withContext(Dispatchers.IO) {
        val url = "$baseUrl${StaticFields.REST_URL_GET_SUM_RESERVED_PER_DAY_UNTIL_END_OF_MONTH}"
        val responseJson = makeGetRequest(url)
        parseValueFromResponse(responseJson)
    }

    /**
     * Get the total food expenses for the current year.
     */
    suspend fun getTotalYearFood(): Double = withContext(Dispatchers.IO) {
        val url = "$baseUrl${StaticFields.REST_URL_GET_SUM_SPENDING_FOOD_BEGINNING_OF_YEAR}"
        val responseJson = makeGetRequest(url)
        parseValueFromResponse(responseJson)
    }

    /**
     * Get the total income for the current year.
     */
    suspend fun getTotalYearIncome(): Double = withContext(Dispatchers.IO) {
        val url = "$baseUrl${StaticFields.REST_URL_GET_SUM_INCOME_YEAR}"
        val responseJson = makeGetRequest(url)
        parseValueFromResponse(responseJson)
    }

    /**
     * Get the food summary for a specific person.
     */
    suspend fun getPersonFoodSummary(person: String, reserve: String): Double = withContext(Dispatchers.IO) {
        val url = "$baseUrl${StaticFields.REST_URL_GET_SUM_FOOD_SPEND_PER_PERSON}'$person'&reserve=$reserve"
        val responseJson = makeGetRequest(url)
        parseValueFromResponse(responseJson)
    }

    /**
     * Get all entries for the current month.
     */
    suspend fun getAllEntries(): List<IncomeExpense> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl${StaticFields.REST_URL_GET_ALL}"
            println("DEBUG: Making request to URL: $url")

            val responseJson = makeGetRequest(url)
            println("DEBUG: Raw API response: $responseJson")

            // Check if response is empty or null
            if (responseJson.isNullOrBlank()) {
                println("DEBUG: Response is empty or null")
                return@withContext emptyList()
            }

            // Try to parse as JSON to see if it's valid
            try {
                val testJson = JSONObject(responseJson)
                println("DEBUG: Valid JSON object received")
            } catch (e: Exception) {
                println("DEBUG: Invalid JSON: ${e.message}")
            }

            return@withContext parseEntriesFromResponse(responseJson)
        } catch (e: Exception) {
            println("DEBUG: Exception in getAllEntries: ${e.message}")
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    /**
     * Add a new entry to the database.
     */
    suspend fun addEntry(entry: IncomeExpense, transactionId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formattedDate = entry.orderdate.format(formatter)

            val url = "$baseUrl${StaticFields.REST_URL_ADD}"

            val formBody = FormBody.Builder()
                .add("transaction_id", transactionId)
                .add("orderdate", formattedDate)
                .add("who", entry.who)
                .add("position", entry.position.toString())
                .add("income", entry.income.toString())
                .add("expense", entry.expense.toString())
                .add("location", entry.location.toString())
                .add("comment", entry.comment)
                .build()

            val response = makePostRequest(url, formBody)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Update an existing entry in the database.
     */
    suspend fun updateEntry(entry: IncomeExpense): Boolean = withContext(Dispatchers.IO) {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formattedDate = entry.orderdate.format(formatter)

            val url = "$baseUrl${StaticFields.REST_URL_PUT}${entry.id}"

            val formBody = FormBody.Builder()
                .add("id", entry.id)
                .add("orderdate", formattedDate)
                .add("who", entry.who)
                .add("position", entry.position.toString())
                .add("income", entry.income.toString())
                .add("expense", entry.expense.toString())
                .add("location", entry.location.toString())
                .add("comment", entry.comment)
                .build()

            val response = makePutRequest(url, formBody)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Generate a unique transaction ID.
     */
    fun generateTransactionId(): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        return "$timestamp-$uuid"
    }

    /**
     * Make a GET request to the specified URL.
     */
    private fun makeGetRequest(url: String): String {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    /**
     * Make a POST request to the specified URL with the given form data.
     */
    private fun makePostRequest(url: String, formBody: RequestBody): String {
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        val response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    /**
     * Make a PUT request to the specified URL with the given form data.
     */
    private fun makePutRequest(url: String, formBody: RequestBody): String {
        val request = Request.Builder()
            .url(url)
            .put(formBody)
            .build()

        val response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    /**
     * Parse a numeric value from the response JSON.
     */
    private fun parseValueFromResponse(responseJson: String): Double {
        try {
            val jsonObject = JSONObject(responseJson)
            val dataObject = jsonObject.getJSONObject("incomeexpense")
            val records = dataObject.getJSONArray("Total income")
            val value = records.getString(0)
            return if (value == "null") 0.0 else value.toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            return 0.0
        }
    }

    /**
     * Parse entries from the response JSON.
     */
    private fun parseEntriesFromResponse(responseJson: String): List<IncomeExpense> {
        val entries = mutableListOf<IncomeExpense>()
        try {
            val jsonObject = JSONObject(responseJson)
            println("DEBUG: JSON object keys: ${JSONObject(responseJson).keys().asSequence().toList()}")

            // Try different ways to access the data
            if (jsonObject.has("incomeexpense")) {
                val incomeExpenseValue = jsonObject.get("incomeexpense")

                when (incomeExpenseValue) {
                    is JSONArray -> {
                        // Direct array of entries
                        for (i in 0 until incomeExpenseValue.length()) {
                            val item = incomeExpenseValue.getJSONObject(i)
                            val entry = parseEntryFromJson(item)
                            entries.add(entry)
                        }
                    }
                    is JSONObject -> {
                        // Check for various object structures
                        if (incomeExpenseValue.has("data") && incomeExpenseValue.get("data") is JSONArray) {
                            // Object with data array
                            val dataArray = incomeExpenseValue.getJSONArray("data")
                            for (i in 0 until dataArray.length()) {
                                val item = dataArray.getJSONObject(i)
                                val entry = parseEntryFromJson(item)
                                entries.add(entry)
                            }
                        } else {
                            // May be a single object entry
                            try {
                                val entry = parseEntryFromJson(incomeExpenseValue)
                                entries.add(entry)
                            } catch (e: Exception) {
                                println("Not a single entry object: ${e.message}")
                            }
                        }
                    }
                    else -> {
                        println("DEBUG: incomeexpense is neither JSONArray nor JSONObject: ${incomeExpenseValue.javaClass.name}")
                    }
                }
            } else {
                // Check if the response itself is an array
                try {
                    val jsonArray = JSONArray(responseJson)
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val entry = parseEntryFromJson(item)
                        entries.add(entry)
                    }
                } catch (e: Exception) {
                    println("DEBUG: Response is not a direct JSONArray: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("DEBUG: Exception in parseEntriesFromResponse: ${e.message}")
            e.printStackTrace()
        }

        println("DEBUG: Parsed entries count: ${entries.size}")
        return entries
    }

    /**
     * Parse a single entry from a JSON object.
     */
    private fun parseEntryFromJson(item: JSONObject): IncomeExpense {
        try {
            // Debug: Print all keys in the JSON object
            println("DEBUG: All JSON keys: ${item.keys().asSequence().toList()}")

            // Get the date - handle different formats
            val orderDateStr = item.optString("orderdate", "")
            println("DEBUG: Raw date string: $orderDateStr")

            // Try to parse the date from the format in your response
            val orderDate = if (orderDateStr.contains(",")) {
                // Format: "Sun, 13 Apr 2025 00:00:00 GMT"
                try {
                    val dateParts = orderDateStr.split(",")[1].trim().split(" ")
                    val day = dateParts[0].toInt()
                    val month = when (dateParts[1].toLowerCase()) {
                        "jan" -> 1
                        "feb" -> 2
                        "mar" -> 3
                        "apr" -> 4
                        "may" -> 5
                        "jun" -> 6
                        "jul" -> 7
                        "aug" -> 8
                        "sep" -> 9
                        "oct" -> 10
                        "nov" -> 11
                        "dec" -> 12
                        else -> 1
                    }
                    val year = dateParts[2].toInt()
                    LocalDate.of(year, month, day)
                } catch (e: Exception) {
                    println("DEBUG: Error parsing date (format 1): ${e.message}")
                    LocalDate.now() // Fallback to current date
                }
            } else {
                // Try the original format (yyyy-MM-dd)
                try {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    LocalDate.parse(orderDateStr, formatter)
                } catch (e: Exception) {
                    println("DEBUG: Error parsing date (format 2): ${e.message}")
                    LocalDate.now() // Fallback to current date
                }
            }

            println("DEBUG: Parsed date: $orderDate")

            // Parse the created_at timestamp with timezone information
            val createdAtStr = item.optString("created_at", "")
            println("DEBUG: Raw created_at string: '$createdAtStr'")

            val createdAt = if (createdAtStr.isNotEmpty() && createdAtStr != "null") {
                try {
                    // Handle different timestamp formats with better error handling
                    when {
                        // ISO format with T separator and timezone: "2025-05-03T10:11:27.740+02:00"
                        createdAtStr.contains("T") && (createdAtStr.contains("+") || createdAtStr.contains("Z")) -> {
                            println("DEBUG: Parsing ISO format with timezone")
                            ZonedDateTime.parse(createdAtStr).toLocalDateTime()
                        }

                        // ISO format with T separator: "2025-05-03T10:11:27.740"
                        createdAtStr.contains("T") -> {
                            println("DEBUG: Parsing ISO format without timezone")
                            LocalDateTime.parse(createdAtStr)
                        }

                        // Standard PostgreSQL timestamp with milliseconds: "2025-05-03 10:11:27.740"
                        createdAtStr.contains(".") -> {
                            println("DEBUG: Parsing PostgreSQL format with milliseconds")
                            try {
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                                LocalDateTime.parse(createdAtStr, formatter)
                            } catch (e: Exception) {
                                println("DEBUG: Failed with milliseconds format, trying with variable precision")
                                // Some databases use variable precision milliseconds, try with a more flexible approach
                                val pattern = "yyyy-MM-dd HH:mm:ss[.SSSSSSSSS]"
                                val formatter = DateTimeFormatter.ofPattern(pattern)
                                LocalDateTime.parse(createdAtStr, formatter)
                            }
                        }

                        // Standard PostgreSQL timestamp without milliseconds: "2025-05-03 10:11:27"
                        else -> {
                            println("DEBUG: Parsing PostgreSQL format without milliseconds")
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            LocalDateTime.parse(createdAtStr, formatter)
                        }
                    }
                } catch (e: Exception) {
                    println("DEBUG: Error parsing created_at: ${e.message}")
                    println("DEBUG: Timestamp format that failed: $createdAtStr")
                    e.printStackTrace()
                    null
                }
            } else {
                println("DEBUG: created_at field is empty, null, or not found")
                null
            }

            if (createdAt != null) {
                println("DEBUG: Successfully parsed created_at: $createdAt")
            } else {
                println("DEBUG: Could not parse created_at timestamp")
            }

            // Rest of the parsing logic
            return IncomeExpense(
                id = item.optString("id", ""),
                orderdate = orderDate,
                who = item.optString("who", ""),
                position = try {
                    Spending.valueOf(item.optString("position", "Expense"))
                } catch (e: Exception) {
                    Spending.Expense
                },
                income = item.optString("income", "0").toDoubleOrNull() ?: 0.0,
                expense = item.optString("expense", "0").toDoubleOrNull() ?: 0.0,
                location = try {
                    Location.valueOf(item.optString("location", "Hollgasse_1_1"))
                } catch (e: Exception) {
                    Location.Hollgasse_1_1
                },
                comment = item.optString("comment", ""),
                createdAt = createdAt
            )
        } catch (e: Exception) {
            println("DEBUG: Error in parseEntryFromJson: ${e.message}")
            e.printStackTrace()

            // Return a dummy entry for debugging
            return IncomeExpense(
                id = "error",
                orderdate = LocalDate.now(),
                who = "Error",
                position = Spending.Expense,
                income = 0.0,
                expense = 0.0,
                location = Location.Hollgasse_1_1,
                comment = "Error parsing: ${e.message}",
                createdAt = null
            )
        }
    }
}