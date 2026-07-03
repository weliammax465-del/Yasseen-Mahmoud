package com.example

import com.example.data.remote.StockDataFetcher
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FetchTest {
    @Test
    fun testFetch() = runBlocking {
        val result = StockDataFetcher.fetchAndValidate("COMI.CA")
        if (result.isSuccess) {
            val data = result.getOrNull()
            println("Success: ${data?.symbol} price: ${data?.currentPrice}")
        } else {
            println("Failed: ${result.exceptionOrNull()}")
        }
    }
}
