package at.co.netconsulting.balancesheet.currency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class CurrencyConversionService(private val apiKey: String) {

    private val client = OkHttpClient()

    suspend fun getExchangeRate(fromCurrency: String, toCurrency: String): Double? {
        if (fromCurrency == toCurrency) return 1.0
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://v6.exchangerate-api.com/v6/6a3d3e949ff3cfaba27f8f44/latest/$fromCurrency"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: return@withContext null
                val jsonObject = JSONObject(responseBody)
                val rates = jsonObject.getJSONObject("conversion_rates")
                if (rates.has(toCurrency)) rates.getDouble(toCurrency) else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Double? {
        // If currencies are the same, no conversion needed
        if (fromCurrency == toCurrency) {
            return amount
        }

        return withContext(Dispatchers.IO) {
            try {
                // Use a free currency conversion API
                // Note: You'll need to sign up for a free API key
                val url = "https://v6.exchangerate-api.com/v6/6a3d3e949ff3cfaba27f8f44/latest/$fromCurrency"

                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: return@withContext null

                val jsonObject = JSONObject(responseBody)
                val rates = jsonObject.getJSONObject("conversion_rates")

                if (rates.has(toCurrency)) {
                    val rate = rates.getDouble(toCurrency)
                    return@withContext amount * rate
                }

                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}