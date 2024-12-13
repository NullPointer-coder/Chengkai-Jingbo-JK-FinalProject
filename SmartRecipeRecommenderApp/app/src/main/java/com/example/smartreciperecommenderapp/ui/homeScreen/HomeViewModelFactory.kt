package com.example.smartreciperecommenderapp.ui.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import com.example.smartreciperecommenderapp.data.repository.RecipeDetailRepository
import com.example.smartreciperecommenderapp.data.repository.RecipeRepository
import com.example.smartreciperecommenderapp.ui.api.FatSecretService
import com.example.smartreciperecommenderapp.utils.NetworkMonitor

class HomeViewModelFactory(
    private val ingredientRepository: IngredientRepository,
    private val fatSecretService: FatSecretService,
    private val recipeRepository: RecipeRepository,
    private val recipeDetailRepository: RecipeDetailRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(ingredientRepository, fatSecretService, recipeRepository, recipeDetailRepository, networkMonitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
