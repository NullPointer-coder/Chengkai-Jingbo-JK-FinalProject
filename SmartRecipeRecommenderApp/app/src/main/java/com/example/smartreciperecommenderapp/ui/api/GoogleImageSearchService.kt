package com.example.smartreciperecommenderapp.ui.api

import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleImageSearchService(
    private val apiKey: String,
    private val cseCx: String
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            gson()
        }
    }

    suspend fun fetchFirstImageForFood(foodName: String): String? = withContext(Dispatchers.IO) {
        val cseUrl = "https://customsearch.googleapis.com/customsearch/v1"
        try {
            val response: HttpResponse = httpClient.get(cseUrl) {
                parameter("key", apiKey)
                parameter("cx", cseCx)
                parameter("searchType", "image")
                parameter("q", foodName)
            }
            val bodyString = response.bodyAsText()
            val json = JsonParser.parseString(bodyString).asJsonObject
            val items = json.getAsJsonArray("items")
            if (items != null && items.size() > 0) {
                val firstItem = items[0].asJsonObject
                val imageUrl = firstItem.get("link")?.asString
                imageUrl
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
