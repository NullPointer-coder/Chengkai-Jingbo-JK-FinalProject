package com.example.smartreciperecommenderapp.ui.homeScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import com.example.smartreciperecommenderapp.ui.IngredientScreen.calculateRemainingDays
import com.example.smartreciperecommenderapp.ui.api.FatSecretFood
import com.example.smartreciperecommenderapp.ui.api.FatSecretRecipeDetailsResponse
import com.example.smartreciperecommenderapp.ui.api.FatSecretService
import com.example.smartreciperecommenderapp.ui.api.GoogleImageSearchService
import com.example.smartreciperecommenderapp.ui.api.Recipe
import com.example.smartreciperecommenderapp.ui.api.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val ingredientRepository: IngredientRepository,
    private val fatSecretService: FatSecretService
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList()) // Updated to use `Recipe` type
    val recipes = _recipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedRecipeDetails = MutableStateFlow<FatSecretRecipeDetailsResponse?>(null)
    val selectedRecipeDetails = _selectedRecipeDetails.asStateFlow()

    // Google Image Search service for fetching product images
    private val googleImageSearchService = GoogleImageSearchService(
        apiKey = "AIzaSyD48oYqtdfpWNI_6Su4h7bL9B6408K2W4U",
        cseCx = "f617f7ad9a77d4d7e"
    )

    fun loadRecipeDetails(recipeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Step 1: Retrieve the Access Token
                val tokenResponse = RetrofitInstance.fatSecretAuthApi.getAccessToken()
                val accessToken = tokenResponse.access_token

                // Step 2: Fetch detailed recipe information
                val response = fatSecretService.searchRecipesDetails(
                    authorization = "Bearer $accessToken",
                    id = recipeId
                )

                Log.d("HomeViewModel", "Fetched Recipe Details: $response")
                _selectedRecipeDetails.value = response.recipe
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading recipe details", e)
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tokenResponse = RetrofitInstance.fatSecretAuthApi.getAccessToken()
                val accessToken = tokenResponse.access_token

                val ingredients = ingredientRepository.getAllIngredientsFromRoom()
                    .filter { it.expiryDate?.let { date -> calculateRemainingDays(date) >= 0 } ?: true }

                Log.d("HomeViewModel", "Fetched non-expired ingredients: $ingredients")

                val ingredientNames = ingredients.joinToString(",") { it.name }

                if (ingredientNames.isNotEmpty()) {
                    val response = fatSecretService.searchRecipes(
                        authorization = "Bearer $accessToken",
                        name = ingredientNames
                    )

                    Log.d("HomeViewModel", "Fetched Recipe IDs: ${response.recipes.recipe.map { it.recipe_id }}")
                    Log.d("HomeViewModel", "Fetched Recipes: ${response.recipes.recipe}")

                    // Fetch fallback images for recipes that have no image
                    val updatedRecipes = response.recipes.recipe.map { recipe ->
                        if (recipe.recipe_image == null) {
                            try {
                                val googleImage = googleImageSearchService.fetchFirstImageForFood(recipe.recipe_name)
                                recipe.copy(recipe_image = googleImage)
                            } catch (e: Exception) {
                                Log.e("HomeViewModel", "Error fetching image for ${recipe.recipe_name}: ${e.message}", e)
                                // If fetching image fails, just return the recipe as is
                                recipe
                            }
                        } else {
                            recipe
                        }
                    }

                    _recipes.value = updatedRecipes
                } else {
                    Log.d("HomeViewModel", "No ingredients to fetch recipes for.")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading recipes", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}


