package com.example.smartreciperecommenderapp.data.repository

import android.util.Log
import com.example.smartreciperecommenderapp.data.model.Ingredient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.FirebaseDatabase

class FirebaseIngredientService {

    private val database = FirebaseDatabase.getInstance().reference
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Save an ingredient to Firebase Realtime Database.
     */
    suspend fun saveIngredient(ingredient: Ingredient) {
        currentUserId?.let { userId ->
            val ingredientRef = database.child("users").child(userId).child("ingredients").child(ingredient.instanceId.toString())

            try {
                Log.d("FirebaseIngredientService", "Saving ingredient: $ingredient")
                ingredientRef.setValue(ingredient).await()
                Log.d("FirebaseIngredientService", "Ingredient saved successfully.")
            } catch (e: Exception) {
                Log.e("FirebaseIngredientService", "Error saving ingredient: ${e.message}", e)
                throw e
            }
        }
    }

    /**
     * Fetch all ingredients associated with the current user from Firebase Realtime Database.
     *
     * @return A list of Ingredients. Returns an empty list if none found or an error occurs.
     */
    suspend fun getAllIngredients(): List<Ingredient> {
        return currentUserId?.let { userId ->
            try {
                val snapshot = database.child("users").child(userId).child("ingredients").get().await()
                val ingredientList = mutableListOf<Ingredient>()
                for (dataSnapshot in snapshot.children) {
                    val ingredient = dataSnapshot.getValue(Ingredient::class.java)
                    if (ingredient != null) {
                        ingredientList.add(ingredient)
                    }
                }
                Log.d("FirebaseIngredientService", "Fetched ingredients: $ingredientList")
                ingredientList
            } catch (e: Exception) {
                Log.e("FirebaseIngredientService", "Error fetching ingredients: ${e.message}", e)
                emptyList()
            }
        } ?: emptyList()
    }

    /**
     * Delete a specific ingredient from Firebase Realtime Database.
     *
     * @param ingredient The ingredient to be deleted.
     */
    suspend fun deleteIngredient(ingredient: Ingredient) {
        currentUserId?.let { userId ->
            try {
                val ingredientRef = database.child("users").child(userId).child("ingredients").child(ingredient.instanceId.toString())
                ingredientRef.removeValue().await()
                Log.d("FirebaseIngredientService", "Deleted ingredient with instanceId: ${ingredient.instanceId}")
            } catch (e: Exception) {
                Log.e("FirebaseIngredientService", "Error deleting ingredient: ${ingredient.instanceId}", e)
                throw e
            }
        } ?: Log.e("FirebaseIngredientService", "Error: User not authenticated")
    }

    /**
     * Update the quantity of a specific ingredient in Firebase Realtime Database.
     *
     * @param instanceId The unique instance ID of the ingredient.
     * @param quantity The new quantity value.
     */
    suspend fun updateIngredientQuantity(instanceId: Int, quantity: Double) {
        currentUserId?.let { userId ->
            val ingredientRef = database.child("users").child(userId).child("ingredients").child(instanceId.toString())
            ingredientRef.child("quantity").setValue(quantity).await()
        }
    }
}
