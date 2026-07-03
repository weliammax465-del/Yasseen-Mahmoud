package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks")
data class Stock(
    @PrimaryKey val symbol: String,
    val nameAr: String,
    val nameEn: String,
    val price: Double,
    val changePercent: Double,
    val volume: String,
    val sector: String,
    val isFavorite: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_analyses")
data class StockAnalysis(
    @PrimaryKey val symbol: String,
    val rsiValue: Double,
    val rsiStatus: String,      // Oversold, Neutral, Overbought
    val macdSignal: String,      // Golden Cross, Dead Cross, Neutral
    val maSignal: String,        // Bullish, Bearish, Neutral
    val support1: Double,
    val support2: Double,
    val resistance1: Double,
    val resistance2: Double,
    val recommendation: String,  // Strong Buy, Buy, Hold, Sell, Strong Sell
    val strategyDetails: String, // Detailed analysis text in Arabic
    val sources: String,         // Comma-separated reliable sources
    val riskLevel: String = "متوسط",
    val successProbability: Int = 0,
    val analysisDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "alert_settings")
data class AlertSettings(
    @PrimaryKey val id: Int = 1,
    val telegramBotToken: String = "",
    val telegramChatId: String = "",
    val emailAddress: String = "",
    val isTelegramEnabled: Boolean = false,
    val isEmailEnabled: Boolean = false,
    val autoAlertOnStrongBuy: Boolean = true
)

@Entity(tableName = "market_summary")
data class MarketSummary(
    @PrimaryKey val id: Int = 1,
    val egx30Index: Double,
    val egx30Change: Double,
    val totalVolume: String,
    val overallTrend: String, // Bearish, Neutral, Bullish
    val generalReportAr: String, // Arabic overview of the market
    val sentimentScore: Int = 50, // 0 to 100, where > 50 is bullish
    val sentimentSummary: String = "", // e.g. "إيجابي"
    val timestamp: Long = System.currentTimeMillis()
)
