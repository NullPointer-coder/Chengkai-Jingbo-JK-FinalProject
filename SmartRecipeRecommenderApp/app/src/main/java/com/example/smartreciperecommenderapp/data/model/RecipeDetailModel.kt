package com.example.smartreciperecommenderapp.data.model

data class RecipeDetailModel(
    val recipeId: Long = 0L,
    val name: String = "Unknown",
    val description: String = "No description",
    val servings: Double = 0.0,
    val prepTime: Int? = null,
    val cookTime: Int? = null,
    val gramsPerPortion: Double? = null,
    val rating: Int? = null,
    val categoriesJson: String? = null,
    val typesJson: String? = null,
    val servingSizesJson: String? = null,
    val ingredientsJson: String? = null,
    val directionsJson: String? = null,
    val imageUrl: String? = null
)
