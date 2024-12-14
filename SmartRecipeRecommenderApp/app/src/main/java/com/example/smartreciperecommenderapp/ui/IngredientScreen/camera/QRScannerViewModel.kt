package com.example.smartreciperecommenderapp.ui.IngredientScreen.camera

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.data.model.Ingredient
import com.example.smartreciperecommenderapp.ui.api.FatSecretFood
import com.example.smartreciperecommenderapp.ui.api.GoogleImageSearchService
import com.example.smartreciperecommenderapp.ui.api.GoogleSearchConfig
import com.example.smartreciperecommenderapp.ui.api.RetrofitInstance
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel responsible for handling QR scan results, fetching product details from APIs,
 * and updating ingredient data based on the scanned barcode or user searches.
 */
class QRScannerViewModel : ViewModel() {
    // States for scan result, errors, product details, images, and ingredients
    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> get() = _scanResult

    private val _error = MutableStateFlow<Exception?>(null)
    val error: StateFlow<Exception?> get() = _error

    private val _productDetails = MutableStateFlow<String?>(null)
    val productDetails: StateFlow<String?> get() = _productDetails

    private val _productImage = MutableStateFlow<String?>(null)
    val productImage: StateFlow<String?> get() = _productImage

    // Track whether a scan has already occurred to prevent repeated fetches
    private var hasScanned = false

    private val _ingredient = MutableStateFlow<Ingredient?>(null)
    val ingredient: StateFlow<Ingredient?> get() = _ingredient

    private val _searchedFoods = MutableStateFlow<List<FatSecretFood>>(emptyList())
    val searchedFoods: StateFlow<List<FatSecretFood>> get() = _searchedFoods

    // Google Image Search service for fetching product images
    private val googleImageSearchService = GoogleImageSearchService(
        apiKey = GoogleSearchConfig().apiKey,
        cseCx = GoogleSearchConfig().cseCx,
    )

    /**
     * Handle a successful scan.
     * If not scanned before, set the scan result and fetch product info.
     */
    fun onScanSuccess(result: String) {
        if (!hasScanned) {
            hasScanned = true
            _scanResult.value = result
            Log.d("QRScannerViewModel", "Scanned barcode: $result")
            fetchProductInfo(result)
        }
    }

    /**
     * Reset the scanning state to allow another scan.
     */
    fun resetScan() {
        hasScanned = false
        _error.value = null
        _scanResult.value = null
        _productDetails.value = null
        _productImage.value = null
        _ingredient.value = null
    }

    /**
     * Handle scanning errors.
     */
    fun onScanError(exception: Exception) {
        _error.value = exception
        Log.e("QRScannerViewModel", "Scan error occurred", exception)
    }

    /**
     * Fetch product information from the OpenFoodFacts API based on the scanned barcode.
     */
    private fun fetchProductInfo(barcode: String) {
        viewModelScope.launch {
            Log.d("QRScannerViewModel", "Fetching product info for barcode: $barcode")
            try {
                val openFoodFactsResponse = RetrofitInstance.api.getProduct(barcode)
                Log.d("QRScannerViewModel", "Response received: status=${openFoodFactsResponse.status}, product=${openFoodFactsResponse.product}")

                if (openFoodFactsResponse.status == 1 && openFoodFactsResponse.product != null) {
                    val product = openFoodFactsResponse.product
                    val fetchedIngredient = Ingredient(
                        id = product.product_name.hashCode(),
                        name = product.product_name ?: "Unknown",
                        quantity = 1.0,
                        category = product.categories ?: "General",
                        imageUrl = product.image_url
                    )
                    _ingredient.value = fetchedIngredient
                    Log.d("QRScannerViewModel", "Ingredient fetched successfully: ${fetchedIngredient.name}")
                } else {
                    _ingredient.value = null
                    Log.d("QRScannerViewModel", "No product found or status != 1")
                }
            } catch (@SuppressLint("NewApi") e: retrofit2.HttpException) {
                Log.e("QRScannerViewModel", "HTTP Error: ${e.message()}", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                _productDetails.value = "HTTP Error: ${e.message()}"
                _productImage.value = null
            } catch (e: IOException) {
                Log.e("QRScannerViewModel", "Network Error, please check your connection.", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                _productDetails.value = "Network Error: Please check your connection."
                _productImage.value = null
            } catch (e: Exception) {
                Log.e("QRScannerViewModel", "Unexpected Error: ${e.message}", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                _productDetails.value = "Unexpected Error: ${e.message}"
                _productImage.value = null
            }
        }
    }

    /**
     * Fetch nutrient information by searching for foods using the FatSecret API.
     */
    fun fetchNutrientsByName(name: String) {
        viewModelScope.launch {
            try {
                // 1. Retrieve the Access Token
                val tokenResponse = RetrofitInstance.fatSecretAuthApi.getAccessToken()
                val accessToken = tokenResponse.access_token

                // 2. Use the Access Token to search for food items
                val searchResponse = RetrofitInstance.fatSecretApi.searchFoods(
                    authorization = "Bearer $accessToken",
                    name = name
                )

                // 3. Process the search results
                val foodsList = searchResponse.foods?.food ?: emptyList()
                _searchedFoods.value = foodsList

                Log.d("QRScannerViewModel", "Fetched ${foodsList.size} foods for $name")
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("QRScannerViewModel", "Error fetching nutrients by name: ${e.message}", e)
                // Handle errors as needed
                _searchedFoods.value = emptyList()
            }
        }
    }

    /**
     * Update the currently selected ingredient with additional details from a chosen FatSecretFood item.
     * Attempts to parse calories and fat from the food description and fetch an image using Google search.
     */
    fun updateSelectedFood(selectedFood: FatSecretFood) {
        val description = selectedFood.food_description

        // Extract calories and fat from the food description using regex
        val regex = Regex("""Calories:\s*(\d+(?:\.\d+)?)kcal\s*\|\s*Fat:\s*(\d+(?:\.\d+)?)g""")
        val matchResult = regex.find(description)
        val matches = matchResult?.destructured
        val (calStr, fatStr) = if (matches != null) {
            matches.component1() to matches.component2()
        } else {
            null to null
        }

        val caloriesValue = calStr?.toDoubleOrNull()
        val fatValue = fatStr?.toDoubleOrNull()

        val current = _ingredient.value ?: Ingredient(
            id = selectedFood.food_name.hashCode(),
            name = selectedFood.food_name,
            quantity = 1.0,
            category = "General"
        )
        Log.d("QRScannerViewModel", "Current ingredient image URL: ${current.imageUrl}")

        val newId = selectedFood.food_id.toIntOrNull() ?: current.id

        viewModelScope.launch {
            val updatedIngredient = try {
                when {
                    // If current ingredient already has an image URL, use it
                    current.imageUrl != null -> {
                        current.copy(
                            id = newId,
                            calories = caloriesValue,
                            fat = fatValue,
                            imageUrl = current.imageUrl
                        )
                    }
                    else -> {
                        // If no current imageUrl and no valid food_url, fallback to Google search
                        val googleImage = googleImageSearchService.fetchFirstImageForFood(selectedFood.food_name)
                        Log.d("QRScannerViewModel", "Fetched image URL from Google: $googleImage")

                        current.copy(
                            id = newId,
                            calories = caloriesValue,
                            fat = fatValue,
                            imageUrl = googleImage
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("QRScannerViewModel", "Error fetching image from Google: ${e.message}", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                current.copy(
                    id = newId,
                    calories = caloriesValue,
                    fat = fatValue
                )
            }

            _ingredient.value = updatedIngredient
        }
    }

    /**
     * Clear the searched foods and reset related states.
     */
    fun clearSearchedFoods() {
        _searchedFoods.value = emptyList()
    }
}
