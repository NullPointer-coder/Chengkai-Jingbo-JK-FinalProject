package com.example.smartreciperecommenderapp.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: RecipeDetailEntity)

    @Query("SELECT * FROM recipe_details WHERE recipeId = :recipeId LIMIT 1")
    suspend fun getRecipeDetail(recipeId: Long): RecipeDetailEntity?
}
