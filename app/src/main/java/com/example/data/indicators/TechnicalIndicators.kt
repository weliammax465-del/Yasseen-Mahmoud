package com.example.data.indicators

import com.example.data.remote.DailyPrice
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

data class PivotPoints(val p: Double, val s1: Double, val s2: Double, val r1: Double, val r2: Double)

object TechnicalIndicators {

    fun calculatePivotPoints(data: List<DailyPrice>): PivotPoints? {
        if (data.size < 2) return null
        val prevDay = data[data.size - 2]
        val p = (prevDay.high + prevDay.low + prevDay.close) / 3.0
        val r1 = (p * 2) - prevDay.low
        val s1 = (p * 2) - prevDay.high
        val r2 = p + (prevDay.high - prevDay.low)
        val s2 = p - (prevDay.high - prevDay.low)
        return PivotPoints(p, s1, s2, r1, r2)
    }

    fun calculateSMA(data: List<DailyPrice>, period: Int): Double? {
        if (data.size < period) return null
        val sum = data.takeLast(period).sumOf { it.close }
        return sum / period
    }

    fun calculateEMA(data: List<DailyPrice>, period: Int): Double? {
        if (data.size < period) return null
        val k = 2.0 / (period + 1)
        var ema = data.subList(0, period).sumOf { it.close } / period // Initial SMA
        for (i in period until data.size) {
            ema = (data[i].close - ema) * k + ema
        }
        return ema
    }

    fun calculateRSI(data: List<DailyPrice>, period: Int = 14): Double? {
        if (data.size <= period) return null
        var gains = 0.0
        var losses = 0.0

        for (i in 1..period) {
            val change = data[i].close - data[i - 1].close
            if (change > 0) gains += change else losses -= change
        }

        var avgGain = gains / period
        var avgLoss = losses / period

        for (i in period + 1 until data.size) {
            val change = data[i].close - data[i - 1].close
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) -change else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    fun calculateMACD(data: List<DailyPrice>, fastPeriod: Int = 12, slowPeriod: Int = 26, signalPeriod: Int = 9): Triple<Double, Double, Double>? {
        if (data.size < slowPeriod + signalPeriod) return null
        
        val macdLine = mutableListOf<Double>()
        for (i in slowPeriod..data.size) {
            val subData = data.subList(0, i)
            val emaFast = calculateEMA(subData, fastPeriod)
            val emaSlow = calculateEMA(subData, slowPeriod)
            if (emaFast != null && emaSlow != null) {
                macdLine.add(emaFast - emaSlow)
            }
        }
        
        if (macdLine.size < signalPeriod) return null
        
        // Calculate Signal Line (EMA of MACD line)
        val k = 2.0 / (signalPeriod + 1)
        var signalLine = macdLine.subList(0, signalPeriod).average()
        for (i in signalPeriod until macdLine.size) {
            signalLine = (macdLine[i] - signalLine) * k + signalLine
        }
        
        val currentMacd = macdLine.last()
        val histogram = currentMacd - signalLine
        
        return Triple(currentMacd, signalLine, histogram)
    }

    fun calculateBollingerBands(data: List<DailyPrice>, period: Int = 20, multiplier: Double = 2.0): Triple<Double, Double, Double>? {
        if (data.size < period) return null
        val subData = data.takeLast(period)
        val sma = subData.sumOf { it.close } / period
        
        var sumSquaredDiff = 0.0
        for (price in subData) {
            sumSquaredDiff += Math.pow(price.close - sma, 2.0)
        }
        val stdDev = sqrt(sumSquaredDiff / period)
        
        val upperBand = sma + (multiplier * stdDev)
        val lowerBand = sma - (multiplier * stdDev)
        
        return Triple(upperBand, sma, lowerBand)
    }

    fun calculateATR(data: List<DailyPrice>, period: Int = 14): Double? {
        if (data.size <= period) return null
        val trList = mutableListOf<Double>()
        for (i in 1 until data.size) {
            val high = data[i].high
            val low = data[i].low
            val prevClose = data[i-1].close
            val tr = max(high - low, max(abs(high - prevClose), abs(low - prevClose)))
            trList.add(tr)
        }
        
        var atr = trList.take(period).average()
        for (i in period until trList.size) {
            atr = (atr * (period - 1) + trList[i]) / period
        }
        return atr
    }

    fun calculateOBV(data: List<DailyPrice>): Double {
        if (data.isEmpty()) return 0.0
        var obv = data[0].volume.toDouble()
        for (i in 1 until data.size) {
            if (data[i].close > data[i-1].close) {
                obv += data[i].volume
            } else if (data[i].close < data[i-1].close) {
                obv -= data[i].volume
            }
        }
        return obv
    }
}
