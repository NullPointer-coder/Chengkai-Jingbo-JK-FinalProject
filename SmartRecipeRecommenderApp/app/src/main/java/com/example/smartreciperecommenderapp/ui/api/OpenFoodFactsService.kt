package com.example.smartreciperecommenderapp.ui.api

import retrofit2.http.GET
import retrofit2.http.Path

data class ProductResponse(
    val status: Int,
    val product: ProductDetails?
)

data class ProductDetails(
    val product_name: String?,
    val brands: String?,
    val ingredients_text: String?,
    val categories: String?,
    val nutriments: Nutriments?,
    val image_url: String?
)

data class Nutriments(
    val energy: Float?,
    val sugars: Float?
)

data class CategoriesResponse(
    val tags: List<Category>
)

data class Category(
    val id: String,
    val name: String,
    val products: Int,
    val url: String
)

interface OpenFoodFactsService {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): ProductResponse

    @GET("categories.json")
    suspend fun getCategories(): CategoriesResponse
}
