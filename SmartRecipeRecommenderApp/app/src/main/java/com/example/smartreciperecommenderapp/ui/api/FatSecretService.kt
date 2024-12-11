package com.example.smartreciperecommenderapp.ui.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * Data models for the foods.search response
 */
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
)

/**
 * Retrofit service interface to communicate with FatSecret API.
 */
interface FatSecretService {

    /**
     * Searches for foods (foods.search)
     * Requires adding "Authorization: Bearer <access_token>" to the header.
     * For example:
     * method=foods.search
     * format=json
     * search_expression=ingredientName
     */
    @GET("rest/server.api")
    suspend fun searchFoods(
        @Header("Authorization") authorization: String, // e.g. "Bearer <token>"
        @Query("method") method: String = "foods.search",
        @Query("format") format: String = "json",
        @Query("search_expression") name: String
    ): FatSecretSearchResponse
}
