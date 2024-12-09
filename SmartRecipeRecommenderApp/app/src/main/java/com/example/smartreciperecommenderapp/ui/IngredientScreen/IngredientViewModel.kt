package com.example.smartreciperecommenderapp.ui.IngredientScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.data.model.Ingredient
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IngredientViewModel(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {
    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients = _ingredients.asStateFlow()

    fun loadIngredients() {
        viewModelScope.launch {
            // 同步Firebase与Room
            ingredientRepository.syncIngredients()
            val localIngredients = ingredientRepository.getAllIngredientsFromRoom()
            _ingredients.value = localIngredients
        }
    }

    fun saveIngredient(ingredient: Ingredient, onSuccess: () -> Unit, onError: (String) -> Unit) {
        Log.d("IngredientViewModel", "Saving ingredient: $ingredient")
        viewModelScope.launch {
            try {
                ingredientRepository.saveIngredient(ingredient)
                loadIngredients() // 更新本地列表
                onSuccess()
            } catch (e: Exception) {
                Log.e("IngredientViewModel", "Error saving ingredient", e)
            }
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            try {
                // 调用仓库方法删除 Ingredient
                ingredientRepository.deleteIngredient(ingredient)
                // 删除后重新加载本地数据库
                loadIngredients()
            } catch (e: Exception) {
                Log.e("IngredientViewModel", "Error deleting ingredient: ${ingredient.id}", e)
            }
        }
    }


    fun updateIngredientQuantity(instanceId: Int, quantity: Double) {
        Log.d("IngredientViewModel", "Updating ingredient quantity: $quantity")
        viewModelScope.launch {
            try {
                ingredientRepository.updateIngredientQuantity(instanceId, quantity) // 更新保存逻辑
                loadIngredients() // 更新本地列表
            } catch (e: Exception) {
                Log.e("IngredientViewModel", "Error updating ingredient: ${e.message}", e)
            }
        }
    }

}
