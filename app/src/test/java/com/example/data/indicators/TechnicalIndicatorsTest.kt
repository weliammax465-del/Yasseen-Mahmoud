package com.example.data.indicators

import com.example.data.remote.DailyPrice
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.abs

class TechnicalIndicatorsTest {

    private val sampleData = listOf(
        DailyPrice(1, 10.0, 12.0, 9.0, 11.0, 100),
        DailyPrice(2, 11.0, 13.0, 10.0, 12.0, 150),
        DailyPrice(3, 12.0, 14.0, 11.0, 13.0, 200),
        DailyPrice(4, 13.0, 15.0, 12.0, 14.0, 100),
        DailyPrice(5, 14.0, 16.0, 13.0, 15.0, 120)
    )

    @Test
    fun testSMA() {
        val sma = TechnicalIndicators.calculateSMA(sampleData, 3)
        assertEquals(14.0, sma!!, 0.001) // (13+14+15)/3 = 14.0
    }

    @Test
    fun testEMA() {
        // Just a basic check that it's calculated
        val ema = TechnicalIndicators.calculateEMA(sampleData, 3)
        // initial SMA = (11+12+13)/3 = 12
        // next EMA = (14 - 12) * (2/4) + 12 = 1 + 12 = 13
        // next EMA = (15 - 13) * (2/4) + 13 = 1 + 13 = 14
        assertEquals(14.0, ema!!, 0.001)
    }

    @Test
    fun testOBV() {
        val obv = TechnicalIndicators.calculateOBV(sampleData)
        // Starts at 100
        // Day 2 close > Day 1 close: +150 = 250
        // Day 3 close > Day 2 close: +200 = 450
        // Day 4 close > Day 3 close: +100 = 550
        // Day 5 close > Day 4 close: +120 = 670
        assertEquals(670.0, obv, 0.001)
    }
}
