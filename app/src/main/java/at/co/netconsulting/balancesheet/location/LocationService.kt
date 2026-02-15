package at.co.netconsulting.balancesheet.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.coroutines.resume

class LocationService(private val context: Context, private val apiKey: String) {

    private val client = OkHttpClient()

    suspend fun getCurrentLocation(): Pair<Double, Double>? {
        return withContext(Dispatchers.IO) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return@withContext null
                }

                suspendCancellableCoroutine { continuation ->
                    val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                    if (location != null) {
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun getCountryCurrencyFromLocation(latitude: Double, longitude: Double): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.opencagedata.com/geocode/v1/json?q=$latitude,$longitude&key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: return@withContext null

                val jsonObject = JSONObject(responseBody)
                val results = jsonObject.getJSONArray("results")

                if (results.length() > 0) {
                    val firstResult = results.getJSONObject(0)
                    val components = firstResult.getJSONObject("components")

                    val country = components.optString("country", "")
                    var currency = ""

                    // Check if currency code is directly available
                    if (components.has("currency")) {
                        val currencyObj = components.getJSONObject("currency")
                        currency = currencyObj.optString("iso_code", "")
                    } else {
                        // Get currency based on country code
                        val countryCode = components.optString("country_code", "").uppercase()
                        currency = getCurrencyForCountryCode(countryCode)
                    }

                    return@withContext Pair(country, currency)
                }

                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun getCurrencyForCountryCode(countryCode: String): String {
        // Mapping of country codes to currency codes (simplified)
        val countryCurrencyMap = mapOf(
            "US" to "USD",
            "GB" to "GBP",
            "EU" to "EUR", // Not a country code, but included for completeness
            "DE" to "EUR",
            "FR" to "EUR",
            "IT" to "EUR",
            "ES" to "EUR",
            "AT" to "EUR",
            "JP" to "JPY",
            "CN" to "CNY",
            "IN" to "INR",
            "CA" to "CAD",
            "AU" to "AUD",
            "CH" to "CHF",
            "SE" to "SEK",
            "NO" to "NOK",
            "DK" to "DKK",
            "PL" to "PLN",
            "CZ" to "CZK",
            "HU" to "HUF",
            "RU" to "RUB",
            "TR" to "TRY",
            "BR" to "BRL",
            "MX" to "MXN",
            "ZA" to "ZAR"
            // Add more countries as needed
        )

        return countryCurrencyMap[countryCode] ?: "USD" // Default to USD if not found
    }
}