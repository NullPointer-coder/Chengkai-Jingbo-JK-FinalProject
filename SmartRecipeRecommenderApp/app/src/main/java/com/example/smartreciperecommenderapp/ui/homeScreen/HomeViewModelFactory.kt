package com.example.smartreciperecommenderapp.ui.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import com.example.smartreciperecommenderapp.ui.api.FatSecretService

class HomeViewModelFactory(
    private val ingredientRepository: IngredientRepository,
    private val fatSecretService: FatSecretService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(ingredientRepository, fatSecretService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
