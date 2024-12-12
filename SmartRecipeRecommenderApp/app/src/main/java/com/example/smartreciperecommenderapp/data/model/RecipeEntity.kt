package com.example.smartreciperecommenderapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val calories: String,
    val carbohydrate: String,
    val fat: String,
    val protein: String,
    val ingredients: List<String>,
    val types: List<String>?,
    val originIngredient: String
)
