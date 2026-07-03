package com.example.data.storage

import android.content.Context
import com.example.data.model.MarketSummary
import com.example.data.remote.MarketAnalysisResponse
import com.example.data.remote.RetrofitClient
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StorageManager {
    fun exportLatestReport(context: Context, response: MarketAnalysisResponse) {
        val jsonAdapter = RetrofitClient.moshiParser.adapter(MarketAnalysisResponse::class.java)
        val jsonString = jsonAdapter.toJson(response)
        
        // Save latest.json
        val latestFile = File(context.filesDir, "latest.json")
        latestFile.writeText(jsonString)
        
        // Save dated json
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateString = dateFormat.format(Date())
        val datedFile = File(context.filesDir, "report_${dateString}.json")
        datedFile.writeText(jsonString)
    }

    fun readLatestReport(context: Context): MarketAnalysisResponse? {
        val latestFile = File(context.filesDir, "latest.json")
        if (!latestFile.exists()) return null
        
        return try {
            val jsonString = latestFile.readText()
            val jsonAdapter = RetrofitClient.moshiParser.adapter(MarketAnalysisResponse::class.java)
            jsonAdapter.fromJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
