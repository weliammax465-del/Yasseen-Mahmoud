package com.example.data.remote

import android.util.Log
import java.time.Instant
import java.time.temporal.ChronoUnit

data class DailyPrice(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

data class ValidatedStockData(
    val symbol: String,
    val currentPrice: Double,
    val history: List<DailyPrice>
)

object StockDataFetcher {
    private const val TAG = "StockDataFetcher"

    suspend fun fetchAndValidate(symbol: String): Result<ValidatedStockData> {
        return try {
            val response = RetrofitClient.yahooFinanceService.getChart(ticker = symbol)
            
            val result = response.chart?.result?.firstOrNull()
                ?: return Result.failure(Exception("NO_DATA_FROM_YAHOO: No result array for $symbol"))

            val timestamps = result.timestamp
                ?: return Result.failure(Exception("NO_TIMESTAMPS: Data missing for $symbol"))
            
            val quote = result.indicators?.quote?.firstOrNull()
                ?: return Result.failure(Exception("NO_QUOTE: Quote data missing for $symbol"))

            // Validate arrays length match
            val size = timestamps.size
            if (quote.open?.size != size || quote.close?.size != size || quote.volume?.size != size) {
                return Result.failure(Exception("MISMATCHED_ARRAYS: Data arrays size mismatch for $symbol"))
            }

            val dailyPrices = mutableListOf<DailyPrice>()
            for (i in 0 until size) {
                val ts = timestamps[i]
                val o = quote.open[i]
                val h = quote.high?.get(i) ?: o
                val l = quote.low?.get(i) ?: o
                val c = quote.close[i]
                val v = quote.volume[i]

                // PHASE 0 & 1: Validation
                // Reject if price is 0 or null
                if (c == null || c == 0.0 || o == null || o == 0.0 || v == null) {
                    Log.w(TAG, "Skipping invalid data point for $symbol at $ts (close=$c, open=$o)")
                    continue
                }

                dailyPrices.add(DailyPrice(ts, o, h!!, l!!, c, v))
            }

            if (dailyPrices.isEmpty()) {
                return Result.failure(Exception("NO_VALID_DATA: All data points were invalid for $symbol"))
            }

            // Reject if less than 50 days of data (cannot calculate 50 SMA reliably, 200 SMA will just be NA)
            if (dailyPrices.size < 50) {
                 return Result.failure(Exception("INSUFFICIENT_DATA: Only ${dailyPrices.size} days available for $symbol. Min 50 required."))
            }

            val latestTs = dailyPrices.last().timestamp
            val nowTs = Instant.now().epochSecond
            
            // If data is older than 3 days (allowing for weekends), mark as stale
            val daysDiff = ChronoUnit.DAYS.between(Instant.ofEpochSecond(latestTs), Instant.now())
            if (daysDiff > 3) {
                 return Result.failure(Exception("STALE_DATA: Latest data for $symbol is $daysDiff days old."))
            }

            // Optional Check: Sudden drop/spike of 20% compared to previous day
            for (i in 1 until dailyPrices.size) {
                val prevClose = dailyPrices[i-1].close
                val currentClose = dailyPrices[i].close
                val change = Math.abs(currentClose - prevClose) / prevClose
                if (change > 0.25) { // 25% to be safe, EGX allows up to 20% in some cases
                    Log.w(TAG, "Unusual price change of ${change*100}% for $symbol on ${dailyPrices[i].timestamp}")
                    // Depending on strictness, we could throw here, but we will just log it for now
                    // as splits/dividends might cause this in unadjusted data. Yahoo provides adjusted close but we used normal close.
                }
            }

            val currentPrice = result.meta?.regularMarketPrice ?: dailyPrices.last().close

            Result.success(
                ValidatedStockData(
                    symbol = symbol,
                    currentPrice = currentPrice,
                    history = dailyPrices
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching data for $symbol: ${e.message}")
            Result.failure(e)
        }
    }
}
