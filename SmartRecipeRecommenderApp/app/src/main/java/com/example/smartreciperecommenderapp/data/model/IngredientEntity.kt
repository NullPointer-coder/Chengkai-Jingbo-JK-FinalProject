package com.example.smartreciperecommenderapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "ingredient")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val instanceId: Int = 0,
    val id: Int,
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    val expiryDate: Long?,
    val imageUrl: String?,
    val calories: Double?,
    val fat: Double?
) {
    fun toIngredient(): Ingredient {
        return Ingredient(
            id = id,
            instanceId = instanceId,
            name = name,
            quantity = quantity,
            unit = unit,
            category = category,
            expiryDate = expiryDate?.let { Date(it) },
            imageUrl = imageUrl,
            calories = calories,
            fat = fat
        )
    }

    companion object {
        fun fromIngredient(ingredient: Ingredient): IngredientEntity {
            return IngredientEntity(
                instanceId = ingredient.instanceId,
                id = ingredient.id,
                name = ingredient.name,
                quantity = ingredient.quantity,
                unit = ingredient.unit,
                category = ingredient.category,
                expiryDate = ingredient.expiryDate?.time,
                imageUrl = ingredient.imageUrl,
                calories = ingredient.calories,
                fat = ingredient.fat
            )
        }
    }
}
