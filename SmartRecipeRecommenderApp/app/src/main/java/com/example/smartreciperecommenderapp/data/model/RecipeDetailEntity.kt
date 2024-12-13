package com.example.smartreciperecommenderapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_details")
data class RecipeDetailEntity(
    @PrimaryKey val recipeId: Long,
    val name: String,
    val description: String,
    val servings: Double,
    val prepTime: Int?,
    val cookTime: Int?,
    val gramsPerPortion: Double?,
    val rating: Int?,
    val categoriesJson: String?,
    val typesJson: String?,
    val servingSizesJson: String?,
    val ingredientsJson: String?,
    val directionsJson: String?,
    val imageUrl: String?
)

