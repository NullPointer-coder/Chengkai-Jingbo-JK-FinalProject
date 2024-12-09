package com.example.smartreciperecommenderapp.ui.IngredientScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository

class IngredientViewModelFactory(
    private val ingredientRepository: IngredientRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IngredientViewModel::class.java)) {
            return IngredientViewModel(ingredientRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

