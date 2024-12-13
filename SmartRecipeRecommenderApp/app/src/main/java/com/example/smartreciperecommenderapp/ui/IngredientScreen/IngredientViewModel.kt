package com.example.smartreciperecommenderapp.ui.IngredientScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.data.model.Ingredient
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import com.example.smartreciperecommenderapp.utils.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for managing ingredient data.
 * It interacts with the [IngredientRepository] to load, save, delete, and update ingredients.
 */
class IngredientViewModel(
    private val ingredientRepository: IngredientRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients = _ingredients.asStateFlow()

    /**
     * Load ingredients from the repository, synchronizing data from Firebase to Room if needed.
     */
    fun loadIngredients() {
        viewModelScope.launch {
            if (networkMonitor.isConnected.first()) {
                // Online: sync with Firebase
                ingredientRepository.syncIngredients()
            } else {
                // Offline: no sync
                // Optionally, log or display a message indicating offline mode
                Log.d("IngredientViewModel", "No internet connection. Loading offline data from Room.")
            }

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
        viewModelScope.launch {
            if (!networkMonitor.isConnected.first()) {
                // Prompts that the user is offline and unable to add an ingredient.
                onError("There is currently no internet connection to synchronize the addition of ingredients.")
                return@launch
            }

            try {
                ingredientRepository.saveIngredient(ingredient)
                loadIngredients()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred while saving ingredient.")
            }
        }
    }

    /**
     * Delete a specific ingredient from both Firebase and the local database.
     *
     * @param ingredient The ingredient to delete.
     * @param onError A callback that will be called if an error occurs or if the operation cannot be done offline.
     */
    fun deleteIngredient(
        ingredient: Ingredient,
        onError: (String) -> Unit = {}
    ) {
        Log.d("IngredientViewModel", "Deleting ingredient: ${ingredient.instanceId}")
        viewModelScope.launch {
            if (!networkMonitor.isConnected.first()) {
                // Offline: Only remove locally and set pendingSync = true, or just show a message
                onError("No internet connection. Deleting ingredient locally and will sync later.")
                ingredientRepository.deleteIngredientLocally(ingredient)
                loadIngredients()
                return@launch
            }

            try {
                // Online: normal deletion
                ingredientRepository.deleteIngredient(ingredient)
                loadIngredients()
            } catch (e: Exception) {
                Log.e("IngredientViewModel", "Error deleting ingredient: ${ingredient.instanceId}", e)
                onError(e.message ?: "Unknown error occurred while deleting the ingredient.")
            }
        }
    }

    /**
     * Update the quantity of a specific ingredient by instanceId in both Firebase and local database.
     *
     * @param instanceId The unique instance identifier of the ingredient.
     * @param quantity The new quantity value.
     * @param onError A callback that will be called if an error occurs or if the operation cannot be done offline.
     */
    fun updateIngredientQuantity(
        instanceId: Int,
        quantity: Double,
        onError: (String) -> Unit = {}
    ) {
        Log.d("IngredientViewModel", "Updating ingredient quantity: $quantity")
        viewModelScope.launch {
            if (!networkMonitor.isConnected.first()) {
                // Offline: Only update locally and set pendingSync = true
                onError("No internet connection. Updating quantity locally and will sync later.")
                ingredientRepository.updateIngredientQuantityLocally(instanceId, quantity)
                loadIngredients()
                return@launch
            }

            try {
                // Online: normal update
                ingredientRepository.updateIngredientQuantity(instanceId, quantity)
                loadIngredients()
            } catch (e: Exception) {
                Log.e("IngredientViewModel", "Error updating ingredient: ${e.message}", e)
                onError(e.message ?: "Unknown error occurred while updating ingredient.")
            }
        }
    }
}
