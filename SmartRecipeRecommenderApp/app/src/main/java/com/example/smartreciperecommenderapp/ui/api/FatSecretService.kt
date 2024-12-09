package com.example.smartreciperecommenderapp.ui.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

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
    // 根据需要解析calories和fat
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

// 根据官方文档构建响应数据类
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
     * 使用 FatSecret 食物搜索 API
     * 需要使用 OAuth2 token 在 Header 中添加 "Authorization: Bearer <access_token>"
     *
     * 示例查询参数：
     * method=foods.search
     * format=json
     * search_expression=name(要搜索的食材名)
     */
    @GET("rest/server.api")
    suspend fun searchFoods(
        @Header("Authorization") authorization: String, // "Bearer <token>"
        @Query("method") method: String = "foods.search",
        @Query("format") format: String = "json",
        @Query("search_expression") name: String
    ): FatSecretSearchResponse
}

interface FatSecretImageRecognitionService {
    @POST("rest/image-recognition/v1")
    suspend fun recognizeImage(
        @Header("Authorization") authorization: String,
        @Body request: ImageRecognitionRequest
    ): ImageRecognitionResponse
}