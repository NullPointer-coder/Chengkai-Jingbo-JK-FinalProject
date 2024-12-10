package com.example.smartreciperecommenderapp.ui.IngredientScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.data.model.Ingredient
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing ingredient data.
 * It interacts with the [IngredientRepository] to load, save, delete, and update ingredients.
 */
class IngredientViewModel(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients = _ingredients.asStateFlow()

    /**
     * Load ingredients from the repository, synchronizing data from Firebase to Room if needed.
     */
    fun loadIngredients() {
        viewModelScope.launch {
            // Synchronize ingredients from Firebase to local database if Firebase has more items
            ingredientRepository.syncIngredients()

            // After syncing, load the ingredients from the local Room database
            val localIngredients = ingredientRepository.getAllIngredientsFromRoom()
            _ingredients.value = localIngredients
        }
    }

    /**
     * Save a new or updated ingredient to both Firebase and local Room database.
     *
     * @param ingredient The ingredient to be saved.
     * @param onSuccess Called when the ingredient is successfully saved.
     * @param onError Called if an error occurs during save.
     */
    fun saveIngredient(ingredient: Ingredient, onSuccess: () -> Unit, onError: (String) -> Unit) {
        Log.d("IngredientViewModel", "Saving ingredient: $ingredient")
        viewModelScope.launch {
            try {
                ingredientRepository.saveIngredient(ingredient)
                loadIngredients()
                onSuccess()
            } catch (e: Exception) {
                Log.e("IngredientViewModel", "Error saving ingredient", e)
                onError(e.message ?: "Unknown error occurred while saving ingredient.")
            }
        }
    }

    /**
     * Delete a specific ingredient from both Firebase and the local database.
     *
     * @param ingredient The ingredient to delete.
     */
    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            try {
                ingredientRepository.deleteIngredient(ingredient)
                loadIngredients()
            } catch (e: Exception) {
                Log.e("IngredientViewModel", "Error deleting ingredient: ${ingredient.id}", e)
            }
        }
    }

    /**
     * Update the quantity of a specific ingredient by instanceId in both Firebase and local database.
     *
     * @param instanceId The unique instance identifier of the ingredient.
     * @param quantity The new quantity value.
     */
    fun updateIngredientQuantity(instanceId: Int, quantity: Double) {
        Log.d("IngredientViewModel", "Updating ingredient quantity: $quantity")
        viewModelScope.launch {
            try {
                ingredientRepository.updateIngredientQuantity(instanceId, quantity)
                loadIngredients()
            } catch (e: Exception) {
                Log.e("IngredientViewModel", "Error updating ingredient: ${e.message}", e)
            }
        }
    }
}
