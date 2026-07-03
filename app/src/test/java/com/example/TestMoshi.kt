package com.example
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Tool
import com.example.data.remote.GoogleSearch
import org.junit.Test

class TestMoshi {
    @Test
    fun testMoshi() {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(Tool::class.java)
        println(adapter.toJson(Tool(googleSearch = GoogleSearch())))
    }
}
