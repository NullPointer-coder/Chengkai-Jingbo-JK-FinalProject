package com.example.smartreciperecommenderapp.ui.IngredientScreen.camera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class QRScannerViewModel : ViewModel() {
    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> get() = _scanResult

    private val _error = MutableStateFlow<Exception?>(null)
    val error: StateFlow<Exception?> get() = _error

    fun onScanSuccess(result: String) {
        _scanResult.value = result
    }

    fun onScanError(exception: Exception) {
        _error.value = exception
    }
}
