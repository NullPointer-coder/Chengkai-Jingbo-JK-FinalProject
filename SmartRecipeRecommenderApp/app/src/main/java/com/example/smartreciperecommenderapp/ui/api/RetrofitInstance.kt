package com.example.smartreciperecommenderapp.ui.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * This object provides instances of Retrofit-based services for various APIs:
 * - OpenFoodFacts API
 * - FatSecret authentication API
 * - FatSecret platform API
 *
 * These instances are lazily initialized and use Gson for JSON deserialization.
 */
object RetrofitInstance {
    private const val OPENFOODFACTS_BASE_URL = "https://world.openfoodfacts.org/"
    private const val FATSECRET_AUTH_BASE_URL = "https://oauth.fatsecret.com/"
    private const val FATSECRET_API_BASE_URL = "https://platform.fatsecret.com/"

    /**
     * Provides an instance of [OpenFoodFactsService] for fetching product details from the OpenFoodFacts API.
     */
    val api: OpenFoodFactsService by lazy {
        Retrofit.Builder()
            .baseUrl(OPENFOODFACTS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsService::class.java)
    }

    /**
     * Provides an instance of [FatSecretAuthService] for obtaining OAuth tokens from FatSecret.
     */
    val fatSecretAuthApi: FatSecretAuthService by lazy {
        Retrofit.Builder()
            .baseUrl(FATSECRET_AUTH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FatSecretAuthService::class.java)
    }

    /**
     * Provides an instance of [FatSecretService] for performing searches and retrieving data from the FatSecret platform API.
     */
    val fatSecretApi: FatSecretService by lazy {
        Retrofit.Builder()
            .baseUrl(FATSECRET_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FatSecretService::class.java)
    }
}
