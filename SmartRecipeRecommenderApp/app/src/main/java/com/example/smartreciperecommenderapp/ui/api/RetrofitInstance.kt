package com.example.smartreciperecommenderapp.ui.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val OPENFOODFACTS_BASE_URL = "https://world.openfoodfacts.org/"
    private const val FATSECRET_AUTH_BASE_URL = "https://oauth.fatsecret.com/"
    private const val FATSECRET_API_BASE_URL = "https://platform.fatsecret.com/"

    // OpenFoodFacts Service
    val api: OpenFoodFactsService by lazy {
        Retrofit.Builder()
            .baseUrl(OPENFOODFACTS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsService::class.java)
    }

    // FatSecret Auth Service，用于获取Access Token
    val fatSecretAuthApi: FatSecretAuthService by lazy {
        Retrofit.Builder()
            .baseUrl(FATSECRET_AUTH_BASE_URL) // 使用FatSecret授权服务器的URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FatSecretAuthService::class.java)
    }

    // FatSecret API Service，用于使用Access Token访问Food相关API
    val fatSecretApi: FatSecretService by lazy {
        Retrofit.Builder()
            .baseUrl(FATSECRET_API_BASE_URL) // 使用FatSecret数据接口的URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FatSecretService::class.java)
    }

    val fatSecretImageRecognitionApi: FatSecretImageRecognitionService by lazy {
        Retrofit.Builder()
            .baseUrl(FATSECRET_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FatSecretImageRecognitionService::class.java)
    }

}
