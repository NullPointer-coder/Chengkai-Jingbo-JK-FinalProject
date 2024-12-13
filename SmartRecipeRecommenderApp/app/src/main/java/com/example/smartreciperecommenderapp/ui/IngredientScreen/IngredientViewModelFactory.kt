package com.example.smartreciperecommenderapp.ui.IngredientScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import com.example.smartreciperecommenderapp.utils.NetworkMonitor

/**
 * A factory class that provides instances of [IngredientViewModel].
 * This factory ensures that the ViewModel is created with the required [IngredientRepository]
 * and a [NetworkMonitor].
 */
class IngredientViewModelFactory(
    private val ingredientRepository: IngredientRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class is IngredientViewModel
        if (modelClass.isAssignableFrom(IngredientViewModel::class.java)) {
            // Return a new instance of IngredientViewModel with the repository and network monitor
            return IngredientViewModel(ingredientRepository, networkMonitor) as T
        }
        // If the ViewModel class does not match, throw an exception
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
