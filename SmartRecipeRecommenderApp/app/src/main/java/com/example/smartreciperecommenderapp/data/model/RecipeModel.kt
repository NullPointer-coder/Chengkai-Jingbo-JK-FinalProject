package com.example.smartreciperecommenderapp.data.model

/**
 * Represents a local data model for recipes at the data/model layer.
 *
 * This data class serves as a simplified and domain-oriented representation of the recipes fetched
 * from the FatSecret API. Converting the original network data (Recipe) into this RecipeModel helps
 * maintain a clear separation between the network layer and the UI. It also makes the data more
 * manageable and consistent throughout the application.
 *
 * Use this model within the Repository to transform network-layer recipes into RecipeModel objects,
 * which can then be provided to the ViewModel and UI layers for display and interaction.
 */
data class RecipeModel(
    val id: Long = 0L,                         // Unique identifier
    val name: String = "Unknown",             // Recipe name
    val description: String = "No description",// Recipe description
    val imageUrl: String? = null,             // Image URL (optional)
    val calories: String = "0",               // Calories (default 0)
    val carbohydrate: String = "0",           // Carbohydrate (default 0)
    val fat: String = "0",                    // Fat (default 0)
    val protein: String = "0",                // Protein (default 0)
    val ingredients: List<String> = emptyList(), // Ingredients list, empty by default
    val types: List<String>? = null,           // Recipe types (optional, null if not provided)
    val originIngredient: String? = null
)

