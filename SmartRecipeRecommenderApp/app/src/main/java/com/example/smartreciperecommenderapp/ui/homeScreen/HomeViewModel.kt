package com.example.smartreciperecommenderapp.ui.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import com.example.smartreciperecommenderapp.ui.IngredientScreen.calculateRemainingDays
import com.example.smartreciperecommenderapp.ui.api.FatSecretFood
import com.example.smartreciperecommenderapp.ui.api.FatSecretService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val ingredientRepository: IngredientRepository,
    private val fatSecretService: FatSecretService
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<FatSecretFood>>(emptyList())
    val recipes = _recipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch non-expired ingredients
                val ingredients = ingredientRepository.getAllIngredientsFromRoom()
                    .filter { it.expiryDate?.let { date -> calculateRemainingDays(date) >= 0 } ?: true }

                val ingredientNames = ingredients.joinToString(",") { it.name }

                // Fetch recipes from FatSecret API
                if (ingredientNames.isNotEmpty()) {
                    val response = fatSecretService.searchFoods(
                        authorization = "Bearer YOUR_ACCESS_TOKEN",
                        name = ingredientNames
                    )
                    _recipes.value = response.foods.food
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

