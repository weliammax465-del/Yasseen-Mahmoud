package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RealTimeTicker(
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val volume: Long,
    val recommendation: String = "Neutral"
)

object RealTimeMarketService {
    private const val TAG = "RealTimeMarketService"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches real-time ticker data for a list of EGX symbols (e.g., "COMI.CA", "FWRY.CA").
     * Returns a list of parsed RealTimeTicker objects.
     */
    suspend fun getRealTimeQuotes(symbols: List<String>): Result<List<RealTimeTicker>> = withContext(Dispatchers.IO) {
        if (symbols.isEmpty()) return@withContext Result.success(emptyList())

        return@withContext try {
            // Convert "COMI.CA" to "EGX:COMI"
            val tvTickers = symbols.map { "EGX:${it.replace(".CA", "")}" }
            
            val jsonPayload = JSONObject().apply {
                put("symbols", JSONObject().apply {
                    put("tickers", org.json.JSONArray(tvTickers))
                })
                put("columns", org.json.JSONArray(listOf("close", "change", "volume", "Recommend.All")))
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://scanner.tradingview.com/egypt/scan")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Error: HTTP ${response.code}"))
            }

            val responseBody = response.body?.string() ?: ""
            val json = JSONObject(responseBody)
            val dataArray = json.optJSONArray("data") ?: org.json.JSONArray()

            val tickers = mutableListOf<RealTimeTicker>()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.optJSONObject(i) ?: continue
                val tvSymbol = item.optString("s") // "EGX:COMI"
                val originalSymbol = tvSymbol.replace("EGX:", "") + ".CA"
                
                val dArray = item.optJSONArray("d") ?: continue
                if (dArray.length() >= 4) {
                    val price = dArray.optDouble(0, 0.0)
                    val changePercent = dArray.optDouble(1, 0.0)
                    val volume = dArray.optLong(2, 0L)
                    val recVal = dArray.optDouble(3, 0.0) // -1 to 1
                    
                    val change = (price * changePercent) / 100.0 // Approximate absolute change
                    
                    val recommendation = when {
                        recVal >= 0.5 -> "Strong Buy"
                        recVal >= 0.1 -> "Buy"
                        recVal <= -0.5 -> "Strong Sell"
                        recVal <= -0.1 -> "Sell"
                        else -> "Hold"
                    }

                    tickers.add(RealTimeTicker(
                        symbol = originalSymbol,
                        name = originalSymbol.replace(".CA", ""),
                        price = price,
                        change = change,
                        changePercent = changePercent,
                        volume = volume,
                        recommendation = recommendation
                    ))
                }
            }

            Result.success(tickers)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch real-time quotes", e)
            Result.failure(e)
        }
    }
}
