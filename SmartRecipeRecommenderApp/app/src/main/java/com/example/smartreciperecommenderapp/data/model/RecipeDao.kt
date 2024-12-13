package com.example.smartreciperecommenderapp.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) for the Recipe table.
 * Provides methods for accessing and manipulating recipe data in the local Room database.
 */
@Dao
interface RecipeDao {
    /**
     * Inserts a list of recipes into the database.
     * If any conflict occurs (e.g., same ID), the existing entry will be replaced.
     *
     * @param recipes The list of RecipeEntity objects to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    /**
     * Retrieves all recipes stored in the local database.
     *
     * @return A list of all stored RecipeEntity objects.
     */
    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<RecipeEntity>

    /**
     * Updates the imageUrl of a specific recipe, identified by its ID.
     *
     * @param id The ID of the recipe to be updated.
     * @param imageUrl The new image URL to set. Can be null if no image is available.
     */
    @Query("UPDATE recipes SET imageUrl = :imageUrl WHERE id = :id")
    suspend fun updateImageUrl(id: Long, imageUrl: String?)

    /**
     * Get recipes by originIngredient
     *
     * @param ingredientName The ingredient name to filter recipes by.
     * @return A list of all stored RecipeEntity objects.
     */
    @Query("SELECT * FROM recipes WHERE originIngredient = :ingredientName")
    suspend fun getRecipesByIngredient(ingredientName: String): List<RecipeEntity>

    /**
     * Delete recipes by originIngredient
     *
     * @param ingredientName The ingredient name to filter recipes by.
     * @return A list of all stored RecipeEntity objects.
     */
    @Query("DELETE FROM recipes WHERE originIngredient = :ingredientName")
    suspend fun deleteRecipesByIngredient(ingredientName: String)

    /**
     * Get all originIngredients
     *
     * @return A list of all stored RecipeEntity objects.
     */
    @Query("SELECT DISTINCT originIngredient FROM recipes")
    suspend fun getAllOriginIngredients(): List<String>

    @Query("SELECT imageUrl FROM recipes WHERE id = :id")
    suspend fun getRecipeImageUrlById(id: Long): String?
}
