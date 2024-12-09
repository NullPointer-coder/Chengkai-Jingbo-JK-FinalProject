
package com.example.smartreciperecommenderapp.ui.IngredientScreen.camera

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.data.model.Ingredient
import com.example.smartreciperecommenderapp.ui.api.FatSecretFood
import com.example.smartreciperecommenderapp.ui.api.GoogleImageSearchService
import com.example.smartreciperecommenderapp.ui.api.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException

class QRScannerViewModel : ViewModel() {
    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> get() = _scanResult

    private val _error = MutableStateFlow<Exception?>(null)
    val error: StateFlow<Exception?> get() = _error

    private val _productDetails = MutableStateFlow<String?>(null)
    val productDetails: StateFlow<String?> get() = _productDetails

    private val _productImage = MutableStateFlow<String?>(null)
    val productImage: StateFlow<String?> get() = _productImage

    private var hasScanned = false // 标记是否已经扫描过

    private val _ingredient = MutableStateFlow<Ingredient?>(null)
    val ingredient: StateFlow<Ingredient?> get() = _ingredient

    private val _searchedFoods = MutableStateFlow<List<FatSecretFood>>(emptyList())
    val searchedFoods: StateFlow<List<FatSecretFood>> get() = _searchedFoods

    private val googleImageSearchService = GoogleImageSearchService(
        apiKey = "AIzaSyD48oYqtdfpWNI_6Su4h7bL9B6408K2W4U",
        cseCx = "f617f7ad9a77d4d7e"
    )

    fun onScanSuccess(result: String) {
        if (!hasScanned) {
            hasScanned = true
            _scanResult.value = result
            Log.d("QRScannerViewModel", "Scanned barcode: $result")
            fetchProductInfo(result)
        }
    }

    fun resetScan() {
        hasScanned = false
        _error.value = null
        _scanResult.value = null
        _productDetails.value = null
        _productImage.value = null
        _ingredient.value = null
    }

    fun onScanError(exception: Exception) {
        _error.value = exception
        Log.e("QRScannerViewModel", "Scan error occurred", exception)
    }

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
                _productDetails.value = "HTTP Error: ${e.message()}"
                _productImage.value = null
            } catch (e: IOException) {
                Log.e("QRScannerViewModel", "Network Error, please check your connection.", e)
                _productDetails.value = "Network Error: Please check your connection."
                _productImage.value = null
            } catch (e: Exception) {
                Log.e("QRScannerViewModel", "Unexpected Error: ${e.message}", e)
                _productDetails.value = "Unexpected Error: ${e.message}"
                _productImage.value = null
            }
        }
    }

    fun fetchNutrientsByName(name: String) {
        viewModelScope.launch {
            try {
                // 1. 获取Access Token
                val tokenResponse = RetrofitInstance.fatSecretAuthApi.getAccessToken()
                val accessToken = tokenResponse.access_token

                // 2. 使用Access Token搜索食材信息
                val searchResponse = RetrofitInstance.fatSecretApi.searchFoods(
                    authorization = "Bearer $accessToken",
                    name = name
                )

                // 3. 处理搜索结果
                val foods = searchResponse.foods.food
                _searchedFoods.value = foods

                Log.d("QRScannerViewModel", "Fetched ${foods.size} foods for $name")


            } catch (e: Exception) {
                Log.e("QRScannerViewModel", "Error fetching nutrients by name: ${e.message}", e)
                // 根据需要处理错误信息，例如更新_stateFlow表示错误状态
            }
        }
    }

    fun updateSelectedFood(selectedFood: FatSecretFood) {
        val description = selectedFood.food_description

        // 使用正则从 food_description 中提取出 calories 和 fat
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

        val newId = selectedFood.food_id.toIntOrNull() ?: current.id

        viewModelScope.launch {
            val updatedIngredient = try {
                // 如果您有FatSecret food.get接口可用，可以在这里尝试获取图片
                // 假设food.get无法提供图片，那么直接用Google搜索

                // 使用Google搜索图片
                val googleImage = googleImageSearchService.fetchFirstImageForFood(selectedFood.food_name)
                Log.d("QRScannerViewModel", "Fetched image URL from Google: $googleImage")

                current.copy(
                    id = newId,
                    calories = caloriesValue,
                    fat = fatValue,
                    imageUrl = googleImage ?: current.imageUrl
                )
            } catch (e: Exception) {
                Log.e("QRScannerViewModel", "Error fetching image from Google: ${e.message}", e)
                current.copy(
                    id = newId,
                    calories = caloriesValue,
                    fat = fatValue
                )
            }

            _ingredient.value = updatedIngredient
        }
    }


    fun clearSearchedFoods() {
        _searchedFoods.value = emptyList()
        _error.value = null
        _scanResult.value = null
        _productDetails.value = null
        _productImage.value = null
        _ingredient.value = null
    }


}
