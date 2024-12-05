package com.example.smartreciperecommenderapp.ui.IngredientScreen.camera

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.ui.api.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    fun onScanSuccess(result: String) {
        if (!hasScanned) { // 如果未扫描过，则继续
            hasScanned = true
            _scanResult.value = result
            fetchProductInfo(result)
        }
    }

    fun resetScan() {
        hasScanned = false // 重置扫描状态
        _scanResult.value = null
        _productDetails.value = null
        _productImage.value = null
    }

    fun onScanError(exception: Exception) {
        _error.value = exception // 更新错误状态
    }

    private fun fetchProductInfo(barcode: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getProduct(barcode)
                if (response.status == 1 && response.product != null) {
                    val product = response.product
                    _productDetails.value = """
                    Name: ${product.product_name ?: "N/A"}
                    Brand: ${product.brands ?: "N/A"}
                    Ingredients: ${product.ingredients_text ?: "N/A"}
                    Categories: ${product.categories ?: "N/A"}N
                    Energy: ${product.nutriments?.energy ?: "N/A"} kJ
                    Sugars: ${product.nutriments?.sugars ?: "N/A"} g
                """.trimIndent()
                    _productImage.value = product.image_url // 更新图片 URL
                } else {
                    _productDetails.value = "No product details found for barcode: $barcode"
                    _productImage.value = null // 重置图片 URL
                }
            } catch (@SuppressLint("NewApi") e: retrofit2.HttpException) {
                _productDetails.value = "HTTP Error: ${e.message()}"
                _productImage.value = null
            } catch (e: IOException) {
                _productDetails.value = "Network Error: Please check your connection."
                _productImage.value = null
            } catch (e: Exception) {
                _productDetails.value = "Unexpected Error: ${e.message}"
                _productImage.value = null
            }
        }
    }
}