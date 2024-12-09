package com.example.smartreciperecommenderapp.data.repository

import android.util.Log
import com.example.smartreciperecommenderapp.data.model.Ingredient
import com.example.smartreciperecommenderapp.data.model.IngredientDao
import com.example.smartreciperecommenderapp.data.model.IngredientEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IngredientRepository(
    private val ingredientDao: IngredientDao,
    private val firebaseService: FirebaseIngredientService
) {
    suspend fun saveIngredient(ingredient: Ingredient) = withContext(Dispatchers.IO) {
        Log.d("IngredientRepository", "Saving ingredient: $ingredient")

        // 获取当前最大的 instanceId
        val currentMaxInstanceId = ingredientDao.getMaxInstanceId() ?: 0

        // 为新的实例分配唯一的 instanceId
        val newIngredient = ingredient.copy(instanceId = currentMaxInstanceId + 1)

        // 保存到 Firebase
        firebaseService.saveIngredient(newIngredient)

        // 保存到本地 Room 数据库
        ingredientDao.insertIngredient(IngredientEntity.fromIngredient(newIngredient))

        Log.d("IngredientRepository", "Ingredient saved with instanceId: ${newIngredient.instanceId}")
    }


    suspend fun syncIngredients() = withContext(Dispatchers.IO) {
        val firebaseIngredients = firebaseService.getAllIngredients()
        val localIngredients = ingredientDao.getAllIngredients().map { it.toIngredient() }

        // 如果Firebase中的数量 > Room的数量，则更新Room
        if (firebaseIngredients.size > localIngredients.size) {
            // 更新Room
            val entities = firebaseIngredients.map { IngredientEntity.fromIngredient(it) }
            ingredientDao.deleteAll()
            ingredientDao.insertIngredients(entities)
        }
    }

    suspend fun getAllIngredientsFromRoom(): List<Ingredient> = withContext(Dispatchers.IO) {
        ingredientDao.getAllIngredients().map { it.toIngredient() }
    }

    suspend fun deleteIngredient(ingredient: Ingredient) {
        // 删除 Firebase 数据
        firebaseService.deleteIngredient(ingredient)

        // 删除 Room 数据
        ingredientDao.deleteIngredientByInstanceId(ingredient.instanceId)
    }

    suspend fun updateIngredientQuantity(instanceId: Int, quantity: Double) = withContext(Dispatchers.IO) {
        // 更新 Room 数据库
        ingredientDao.updateIngredientQuantity(instanceId, quantity)

        // 更新 Firebase 数据
        firebaseService.updateIngredientQuantity(instanceId, quantity)
    }

}
