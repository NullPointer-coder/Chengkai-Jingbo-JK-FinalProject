package com.example.smartreciperecommenderapp.ui.api

import retrofit2.http.*

data class FatSecretSearchResponse(
    val foods: Foods
)

data class Foods(
    val food: List<FatSecretFood>
)

data class FatSecretFood(
    val food_name: String,
    val food_description: String,
    val food_id: String
    // Parse calories and fat as needed.
)

data class ImageRecognitionRequest(
    val image_b64: String,
    val region: String? = null,
    val language: String? = null,
    val include_food_data: Boolean = true,
    val eaten_foods: List<EatenFood>? = null
)

data class EatenFood(
    val food_id: Long,
    val food_name: String,
    val brand: String? = null,
    val serving_description: String? = null,
    val serving_size: Double? = null
)

// Build response data classes according to the official documentation
data class ImageRecognitionResponse(
    val food_response: List<FoodResponseItem>
)

data class FoodResponseItem(
    val food_id: Long,
    val food_entry_name: String,
    val brand_name: String?,
)

interface FatSecretService {
    /**
     * Use the FatSecret food search API.
     * Requires an OAuth2 token. Add "Authorization: Bearer <access_token>" in the header.
     *
     * Example query parameters:
     * method=foods.search
     * format=json
     * search_expression=name (the ingredient to search for)
     */
    @GET("rest/server.api")
    suspend fun searchFoods(
        @Header("Authorization") authorization: String, // "Bearer <token>"
        @Query("method") method: String = "foods.search",
        @Query("format") format: String = "json",
        @Query("search_expression") name: String
    ): FatSecretSearchResponse
}
