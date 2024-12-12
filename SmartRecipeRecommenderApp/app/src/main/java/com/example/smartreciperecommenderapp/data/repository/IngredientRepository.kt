package com.example.smartreciperecommenderapp.data.repository

import android.util.Log
import com.example.smartreciperecommenderapp.data.model.Ingredient
import com.example.smartreciperecommenderapp.data.model.IngredientDao
import com.example.smartreciperecommenderapp.data.model.IngredientEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class IngredientRepository(
    private val ingredientDao: IngredientDao,
    private val firebaseService: FirebaseIngredientService
) {
    /**
     * Save an ingredient to both Firebase and the local Room database.
     * Assigns a unique instanceId based on the current maximum instanceId in the local database.
     */
    suspend fun saveIngredient(ingredient: Ingredient) = withContext(Dispatchers.IO) {
        Log.d("IngredientRepository", "Saving ingredient: $ingredient")

        // Get the current maximum instanceId from the local database
        val currentMaxInstanceId = ingredientDao.getMaxInstanceId() ?: 0

        // Assign a unique instanceId to the new ingredient
        val newIngredient = ingredient.copy(instanceId = currentMaxInstanceId + 1)

        // Save to Firebase
        firebaseService.saveIngredient(newIngredient)

        // Save to the local Room database
        ingredientDao.insertIngredient(IngredientEntity.fromIngredient(newIngredient))

        Log.d("IngredientRepository", "Ingredient saved with instanceId: ${newIngredient.instanceId}")
    }

    /**
     * Synchronize ingredients from Firebase to the local Room database.
     * If the number of ingredients in Firebase is greater than in Room,
     * the Room database is updated to match Firebase.
     */
    suspend fun syncIngredients() = withContext(Dispatchers.IO) {
        val firebaseIngredients = firebaseService.getAllIngredients()
        val localIngredients = ingredientDao.getAllIngredients().map { it.toIngredient() }

        // If Firebase has more ingredients than the local database, update the local database
        if (firebaseIngredients.size > localIngredients.size) {
            val entities = firebaseIngredients.map { IngredientEntity.fromIngredient(it) }
            ingredientDao.deleteAll()
            ingredientDao.insertIngredients(entities)
        }
    }

    /**
     * Retrieve all ingredients from the local Room database.
     * Returns a list of Ingredient domain objects.
     */
    suspend fun getAllIngredientsFromRoom(): List<Ingredient> = withContext(Dispatchers.IO) {
        ingredientDao.getAllIngredients().map { it.toIngredient() }
    }

    /**
     * Delete an ingredient from both Firebase and the local Room database.
     */
    suspend fun deleteIngredient(ingredient: Ingredient) {
        // Delete from Firebase
        firebaseService.deleteIngredient(ingredient)

        // Delete from Room
        ingredientDao.deleteIngredientByInstanceId(ingredient.instanceId)
    }

    /**
     * Update the quantity of a specific ingredient by its instanceId in both Room and Firebase.
     */
    suspend fun updateIngredientQuantity(instanceId: Int, quantity: Double) = withContext(Dispatchers.IO) {
        // Update Room database
        ingredientDao.updateIngredientQuantity(instanceId, quantity)

        // Update Firebase
        firebaseService.updateIngredientQuantity(instanceId, quantity)
    }

    /**
     * Delete an ingredient locally by marking it as deleted and pending sync.
     * This does not sync with Firebase immediately.
     */
    suspend fun deleteIngredientLocally(ingredient: Ingredient) {
        ingredientDao.markAsDeleted(ingredient.instanceId, true)
        ingredientDao.markPendingSync(ingredient.instanceId, true)
    }


    /**
     * Update the quantity of an ingredient locally and mark it as pending sync.
     * This does not sync with Firebase immediately.
     */
    suspend fun updateIngredientQuantityLocally(instanceId: Int, quantity: Double) = withContext(Dispatchers.IO) {
        // Update local quantities
        ingredientDao.updateIngredientQuantity(instanceId, quantity)
        // Marked for synchronization
        ingredientDao.markPendingSync(instanceId, true)
    }

    /**
     * To expose a Flow of ingredients
     */
    fun getAllIngredientsFlow(): kotlinx.coroutines.flow.Flow<List<Ingredient>> {
        return ingredientDao.getAllIngredientsFlow().map { entities ->
            entities.map { it.toIngredient() }
        }
    }

}
