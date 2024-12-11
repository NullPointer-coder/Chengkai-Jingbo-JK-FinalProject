package com.example.smartreciperecommenderapp.ui.api

import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A service that uses the Google Custom Search Engine (CSE) API to fetch the first image result
 * for a given food name. This can be used to provide a relevant image for an ingredient or dish.
 *
 * @property apiKey Your Google CSE API key.
 * @property cseCx Your Custom Search Engine ID.
 */
class GoogleImageSearchService(
    private val apiKey: String,
    private val cseCx: String
) {
    // Configure an HTTP client with JSON (Gson) support for parsing responses.
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            gson()
        }
    }

    /**
     * Fetch the first image URL corresponding to the given food name using Google Custom Search.
     *
     * @param foodName The name of the food for which to search an image.
     * @return The URL of the first image result, or null if no result is found or an error occurs.
     */
    suspend fun fetchFirstImageForFood(foodName: String): String? = withContext(Dispatchers.IO) {
        val cseUrl = "https://customsearch.googleapis.com/customsearch/v1"
        try {
            // Make a GET request to the Google Custom Search API.
            val response: HttpResponse = httpClient.get(cseUrl) {
                parameter("key", apiKey)
                parameter("cx", cseCx)
                parameter("searchType", "image")
                parameter("q", foodName)
            }

            // Extract the response body as text.
            val bodyString = response.bodyAsText()

            // Parse the JSON response to find the first image link.
            val json = JsonParser.parseString(bodyString).asJsonObject
            val items = json.getAsJsonArray("items")
            if (items != null && items.size() > 0) {
                val firstItem = items[0].asJsonObject
                firstItem.get("link")?.asString
            } else {
                null
            }
        } catch (e: Exception) {
            // If any exception occurs (e.g., network error), print the stack trace and return null.
            e.printStackTrace()
            null
        }
    }
}