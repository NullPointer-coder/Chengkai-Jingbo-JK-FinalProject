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

    // 保存食材到 Firebase Realtime Database
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

    // 从 Firebase Realtime Database 获取食材列表
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

    suspend fun deleteIngredient(ingredient: Ingredient) {
        currentUserId?.let { userId ->
            try {
                // 直接使用 `database.child()` 获取路径
                val ingredientRef = database.child("users").child(userId).child("ingredients").child(ingredient.instanceId.toString())
                ingredientRef.removeValue().await() // 删除该节点
                Log.d("FirebaseIngredientService", "Deleted ingredient with instanceId: ${ingredient.instanceId}")
            } catch (e: Exception) {
                Log.e("FirebaseIngredientService", "Error deleting ingredient: ${ingredient.instanceId}", e)
                throw e
            }
        } ?: Log.e("FirebaseIngredientService", "Error: User not authenticated")
    }

    suspend fun updateIngredientQuantity(instanceId: Int, quantity: Double) {
        currentUserId?.let { userId ->
            val ingredientRef = database.child("users").child(userId).child("ingredients").child(instanceId.toString())
            ingredientRef.child("quantity").setValue(quantity).await()
        }
    }


}

