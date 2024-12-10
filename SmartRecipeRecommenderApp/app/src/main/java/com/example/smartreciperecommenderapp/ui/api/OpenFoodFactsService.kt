package com.example.smartreciperecommenderapp.ui.api

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Response model for OpenFoodFacts API calls.
 *
 * @property status The status code for the request. Typically 1 for success, 0 for not found.
 * @property product The product details if found, or null otherwise.
 */
data class ProductResponse(
    val status: Int,
    val product: ProductDetails?
)

/**
 * Detailed information about a product returned by the OpenFoodFacts API.
 *
 * @property product_name The name of the product.
 * @property brands The brand(s) under which this product is sold.
 * @property ingredients_text A textual description of the ingredients.
 * @property categories A list of categories to which this product belongs.
 * @property nutriments Nutritional information of the product.
 * @property image_url A URL pointing to an image of the product.
 */
data class ProductDetails(
    val product_name: String?,
    val brands: String?,
    val ingredients_text: String?,
    val categories: String?,
    val nutriments: Nutriments?,
    val image_url: String?
)

/**
 * Nutritional values associated with the product.
 *
 * @property energy The energy content of the product (in kcal or kJ, depending on the dataset).
 * @property sugars The sugar content of the product (in grams).
 */
data class Nutriments(
    val energy: Float?,
    val sugars: Float?
)

/**
 * Service interface for interacting with the OpenFoodFacts API.
 */
interface OpenFoodFactsService {
    /**
     * Retrieve product details using its barcode.
     *
     * @param barcode The barcode (e.g., UPC, EAN) of the product.
     * @return A [ProductResponse] object containing product details if found, or a status indicating otherwise.
     */
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): ProductResponse
}
