package com.example.data.scoring

import com.example.data.indicators.TechnicalIndicators
import com.example.data.remote.DailyPrice

data class ScoreReason(
    val indicatorName: String,
    val pointsAwarded: Int,
    val description: String
)

data class StockScoreResult(
    val symbol: String,
    val totalScore: Int,
    val reasons: List<ScoreReason>,
    val isEligible: Boolean,
    val support1: Double = 0.0,
    val support2: Double = 0.0,
    val resistance1: Double = 0.0,
    val resistance2: Double = 0.0,
    val riskLevel: String = "متوسط",
    val successProbability: Int = 0
)

object ScoringEngine {

    private const val MIN_SCORE_THRESHOLD = 60

    fun calculateScore(symbol: String, data: List<DailyPrice>): StockScoreResult {
        var totalScore = 0
        val reasons = mutableListOf<ScoreReason>()

        val currentPrice = data.last().close
        
        // 1. Trend Analysis using SMA (Max 30 points)
        val sma20 = TechnicalIndicators.calculateSMA(data, 20)
        val sma50 = TechnicalIndicators.calculateSMA(data, 50)
        val sma200 = TechnicalIndicators.calculateSMA(data, 200)

        if (sma20 != null && currentPrice > sma20) {
            totalScore += 10
            reasons.add(ScoreReason("SMA20", 10, "Current price ($currentPrice) is above 20-day SMA ($sma20). Short-term bullish."))
        }
        
        if (sma50 != null && currentPrice > sma50) {
            totalScore += 10
            reasons.add(ScoreReason("SMA50", 10, "Current price ($currentPrice) is above 50-day SMA ($sma50). Medium-term bullish."))
        }

        if (sma200 != null && currentPrice > sma200) {
            totalScore += 10
            reasons.add(ScoreReason("SMA200", 10, "Current price ($currentPrice) is above 200-day SMA ($sma200). Long-term bullish."))
        } else if (sma200 != null) {
             reasons.add(ScoreReason("SMA200", 0, "Current price ($currentPrice) is below 200-day SMA ($sma200). Long-term bearish."))
        }

        // 2. Momentum Analysis using RSI (Max 20 points)
        val rsi = TechnicalIndicators.calculateRSI(data, 14)
        if (rsi != null) {
            if (rsi in 40.0..60.0) {
                totalScore += 10
                reasons.add(ScoreReason("RSI", 10, "RSI is neutral/healthy (${String.format("%.2f", rsi)}). Room for growth."))
            } else if (rsi in 30.0..40.0) {
                totalScore += 20
                reasons.add(ScoreReason("RSI", 20, "RSI is approaching oversold (${String.format("%.2f", rsi)}). Potential bounce."))
            } else if (rsi < 30.0) {
                totalScore += 15
                reasons.add(ScoreReason("RSI", 15, "RSI is oversold (${String.format("%.2f", rsi)}). High chance of reversal but risky."))
            } else if (rsi > 70.0) {
                reasons.add(ScoreReason("RSI", 0, "RSI is overbought (${String.format("%.2f", rsi)}). Risk of pullback."))
            }
        }

        // 3. MACD Analysis (Max 20 points)
        val macd = TechnicalIndicators.calculateMACD(data)
        if (macd != null) {
            val (macdLine, signalLine, histogram) = macd
            if (macdLine > signalLine) {
                totalScore += 10
                reasons.add(ScoreReason("MACD", 10, "MACD line is above Signal line. Bullish momentum."))
                if (histogram > 0 && histogram > (macdLine * 0.01)) { // Histogram growing
                     totalScore += 10
                     reasons.add(ScoreReason("MACD Histogram", 10, "MACD Histogram is positive and growing."))
                }
            } else {
                reasons.add(ScoreReason("MACD", 0, "MACD line is below Signal line. Bearish momentum."))
            }
        }

        // 4. Volume Confirmation using OBV and recent volume (Max 15 points)
        val obv = TechnicalIndicators.calculateOBV(data)
        val avgVol20 = data.takeLast(20).map { it.volume.toDouble() }.average()
        val currentVol = data.last().volume
        
        if (currentVol > avgVol20 * 1.2 && currentPrice > data[data.size - 2].close) {
            totalScore += 15
            reasons.add(ScoreReason("Volume", 15, "Recent volume ($currentVol) is 20% higher than 20-day average, on an up day."))
        } else if (currentVol > avgVol20) {
            totalScore += 5
            reasons.add(ScoreReason("Volume", 5, "Recent volume is above average."))
        } else {
            reasons.add(ScoreReason("Volume", 0, "Volume does not confirm strong buying pressure."))
        }

        // 5. Volatility / Bollinger Bands (Max 15 points)
        val bb = TechnicalIndicators.calculateBollingerBands(data)
        if (bb != null) {
            val (upper, middle, lower) = bb
            if (currentPrice > middle && currentPrice < upper) {
                totalScore += 15
                reasons.add(ScoreReason("Bollinger Bands", 15, "Price is in the upper half of the bands, indicating an uptrend without being overextended."))
            } else if (currentPrice <= lower * 1.02) {
                 totalScore += 10
                 reasons.add(ScoreReason("Bollinger Bands", 10, "Price is near the lower band, possible mean reversion bounce."))
            } else if (currentPrice >= upper) {
                 reasons.add(ScoreReason("Bollinger Bands", 0, "Price is piercing upper band, potential exhaustion/pullback."))
            } else {
                 reasons.add(ScoreReason("Bollinger Bands", 0, "Price is in the lower half of the bands, indicating weakness."))
            }
        }

        // Final score capping
        if (totalScore > 100) totalScore = 100
        
        val isEligible = totalScore >= MIN_SCORE_THRESHOLD

        val pivots = TechnicalIndicators.calculatePivotPoints(data)
        val s1 = pivots?.s1 ?: 0.0
        val s2 = pivots?.s2 ?: 0.0
        val r1 = pivots?.r1 ?: 0.0
        val r2 = pivots?.r2 ?: 0.0

        val atr = TechnicalIndicators.calculateATR(data, 14) ?: 0.0
        val riskLevel = if (atr > currentPrice * 0.05) "عالي" else if (atr > currentPrice * 0.02) "متوسط" else "منخفض"

        return StockScoreResult(
            symbol = symbol,
            totalScore = totalScore,
            reasons = reasons,
            isEligible = isEligible,
            support1 = s1,
            support2 = s2,
            resistance1 = r1,
            resistance2 = r2,
            riskLevel = riskLevel,
            successProbability = totalScore
        )
    }
}
