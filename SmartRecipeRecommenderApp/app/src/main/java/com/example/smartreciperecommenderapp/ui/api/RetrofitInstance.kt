package com.example.smartreciperecommenderapp.ui.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val OPENFOODFACTS_BASE_URL = "https://world.openfoodfacts.org/"
    private const val FATSECRET_AUTH_BASE_URL = "https://oauth.fatsecret.com/"
    private const val FATSECRET_API_BASE_URL = "https://platform.fatsecret.com/"

    val api: OpenFoodFactsService by lazy {
        Retrofit.Builder()
            .baseUrl(OPENFOODFACTS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsService::class.java)
    }

    val fatSecretAuthApi: FatSecretAuthService by lazy {
        Retrofit.Builder()
            .baseUrl(FATSECRET_AUTH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FatSecretAuthService::class.java)
    }

    val fatSecretApi: FatSecretService by lazy {
        Retrofit.Builder()
            .baseUrl(FATSECRET_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FatSecretService::class.java)
    }
}
