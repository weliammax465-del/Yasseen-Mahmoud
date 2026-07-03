package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST API Request Models ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val tools: List<Tool>? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Tool(
    val googleSearch: GoogleSearch? = null
)

@JsonClass(generateAdapter = true)
class GoogleSearch

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val responseMimeType: String? = null,
    val responseSchema: ResponseSchema? = null
)

@JsonClass(generateAdapter = true)
data class ResponseSchema(
    val type: String,
    val description: String? = null,
    val properties: Map<String, SchemaProperty>? = null,
    val required: List<String>? = null,
    val items: ResponseSchema? = null
)

@JsonClass(generateAdapter = true)
data class SchemaProperty(
    val type: String,
    val description: String? = null,
    val items: ResponseSchema? = null
)

// --- Gemini REST API Response Models ---

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?,
    val groundingMetadata: GroundingMetadata? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class GroundingMetadata(
    val groundingChunks: List<GroundingChunk>?,
    val webSearchQueries: List<String>?
)

@JsonClass(generateAdapter = true)
data class GroundingChunk(
    val web: WebSource?
)

@JsonClass(generateAdapter = true)
data class WebSource(
    val uri: String?,
    val title: String?
)

// --- Moshi Parsing Model for the Structured Gemini Output ---

@JsonClass(generateAdapter = true)
data class MarketAnalysisResponse(
    val egx30Index: Double,
    val egx30Change: Double,
    val totalVolume: String,
    val marketTrend: String, // Bullish, Bearish, Neutral
    val marketSummary: String, // Arabic overview
    val sentimentScore: Int, // 0 to 100
    val sentimentLabel: String, // e.g. إيجابي، سلبي، محايد
    val stocks: List<AnalyzedStock>
)

@JsonClass(generateAdapter = true)
data class AnalyzedStock(
    val symbol: String,
    val nameAr: String,
    val nameEn: String,
    val price: Double,
    val changePercent: Double,
    val volume: String,
    val sector: String,
    val rsiValue: Double,
    val rsiStatus: String,
    val macdSignal: String,
    val maSignal: String,
    val support1: Double,
    val support2: Double,
    val resistance1: Double,
    val resistance2: Double,
    val recommendation: String, // Strong Buy, Buy, Hold, Sell
    val strategyDetails: String, // Arabic report
    val sources: String,
    val riskLevel: String? = "متوسط",
    val successProbability: Int? = 0
)

// --- API Service Interfaces ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val YAHOO_BASE_URL = "https://query1.finance.yahoo.com/"

    val yahooFinanceService: YahooFinanceApiService by lazy {
        Retrofit.Builder()
            .baseUrl(YAHOO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(YahooFinanceApiService::class.java)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val geminiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    val moshiParser: Moshi = moshi
}

// --- Helper for Telegram Alert Integration ---

object TelegramAlertSender {
    suspend fun sendTelegramMessage(token: String, chatId: String, messageHtml: String): Boolean =
        withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val url = "https://api.telegram.org/bot$token/sendMessage"

            val formBody = FormBody.Builder()
                .add("chat_id", chatId)
                .add("text", messageHtml)
                .add("parse_mode", "HTML")
                .build()

            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    response.isSuccessful
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
}
