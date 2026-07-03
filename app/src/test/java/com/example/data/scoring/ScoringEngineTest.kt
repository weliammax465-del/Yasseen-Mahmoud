package com.example.data.scoring

import com.example.data.remote.DailyPrice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringEngineTest {

    private fun generateUptrendData(): List<DailyPrice> {
        val list = mutableListOf<DailyPrice>()
        var price = 10.0
        for (i in 1..250) {
            price += 0.1
            list.add(DailyPrice(i.toLong(), price - 0.5, price + 0.5, price - 1.0, price, 1000 + (i * 10).toLong()))
        }
        return list
    }

    @Test
    fun testScoringUptrend() {
        val data = generateUptrendData()
        val result = ScoringEngine.calculateScore("COMI.CA", data)
        
        // Uptrend should score very high
        // SMA20/50/200 should all be passed (30 pts)
        // MACD should be positive (20 pts)
        // Volume increasing (15 pts)
        // BB in upper half (15 pts)
        
        assertTrue(result.totalScore >= 60)
        assertTrue(result.isEligible)
        assertEquals("COMI.CA", result.symbol)
    }
}
