package com.example

import com.example.data.remote.RealTimeMarketService
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TestRealTime {
    @Test
    fun testRealTime() = runBlocking {
        val result = RealTimeMarketService.getRealTimeQuotes(listOf("COMI.CA", "FWRY.CA", "TMGH.CA"))
        if (result.isSuccess) {
            println("Quotes: " + result.getOrNull())
        } else {
            println("Failed: " + result.exceptionOrNull())
        }
    }
}
