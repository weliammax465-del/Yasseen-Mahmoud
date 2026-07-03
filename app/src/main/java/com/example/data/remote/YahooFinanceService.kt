package com.example.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class YahooChartResponse(
    val chart: YahooChartResult?
)

@JsonClass(generateAdapter = true)
data class YahooChartResult(
    val result: List<YahooChartData>?,
    val error: YahooError?
)

@JsonClass(generateAdapter = true)
data class YahooError(
    val code: String?,
    val description: String?
)

@JsonClass(generateAdapter = true)
data class YahooChartData(
    val meta: YahooChartMeta?,
    val timestamp: List<Long>?,
    val indicators: YahooIndicators?
)

@JsonClass(generateAdapter = true)
data class YahooChartMeta(
    val currency: String?,
    val symbol: String?,
    val regularMarketPrice: Double?,
    val chartPreviousClose: Double?
)

@JsonClass(generateAdapter = true)
data class YahooIndicators(
    val quote: List<YahooQuote>?
)

@JsonClass(generateAdapter = true)
data class YahooQuote(
    val open: List<Double?>?,
    val close: List<Double?>?,
    val high: List<Double?>?,
    val low: List<Double?>?,
    val volume: List<Long?>?
)

interface YahooFinanceApiService {
    @GET("v8/finance/chart/{ticker}")
    suspend fun getChart(
        @Path("ticker") ticker: String,
        @Query("range") range: String = "200d", // 200 days for 200 SMA/EMA
        @Query("interval") interval: String = "1d"
    ): YahooChartResponse

    @GET("v7/finance/quote")
    suspend fun getQuotes(
        @Query("symbols") symbols: String
    ): YahooQuoteResponse
}

@JsonClass(generateAdapter = true)
data class YahooQuoteResponse(
    val quoteResponse: YahooQuoteResult?
)

@JsonClass(generateAdapter = true)
data class YahooQuoteResult(
    val result: List<YahooRealTimeQuote>?,
    val error: YahooError?
)

@JsonClass(generateAdapter = true)
data class YahooRealTimeQuote(
    val symbol: String,
    val regularMarketPrice: Double?,
    val regularMarketChange: Double?,
    val regularMarketChangePercent: Double?,
    val regularMarketVolume: Long?,
    val shortName: String?
)
