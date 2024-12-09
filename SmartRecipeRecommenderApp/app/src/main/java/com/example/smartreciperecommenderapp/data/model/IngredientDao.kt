package com.example.smartreciperecommenderapp.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredient")
    suspend fun getAllIngredients(): List<IngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    @Query("DELETE FROM ingredient WHERE instanceId = :instanceId")
    suspend fun deleteIngredientByInstanceId(instanceId: Int)

    @Query("DELETE FROM ingredient")
    suspend fun deleteAll()

    @Query("SELECT MAX(instanceId) FROM ingredient")
    suspend fun getMaxInstanceId(): Int?

    @Query("UPDATE ingredient SET quantity = :quantity WHERE instanceId = :instanceId")
    suspend fun updateIngredientQuantity(instanceId: Int, quantity: Double)
}