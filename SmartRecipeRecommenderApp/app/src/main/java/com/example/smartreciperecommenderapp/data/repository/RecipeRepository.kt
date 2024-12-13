package com.example.smartreciperecommenderapp.data.repository

import com.example.smartreciperecommenderapp.data.model.RecipeDao
import com.example.smartreciperecommenderapp.data.model.RecipeEntity
import com.example.smartreciperecommenderapp.data.model.RecipeModel
import com.example.smartreciperecommenderapp.ui.api.Recipe

/**
 * The RecipeRepository acts as a mediator between the data sources (local database, network, etc.)
 * and the rest of the application. It handles data operations such as converting network models
 * to domain models and storing data locally.
 *
 * This repository provides a clean API for accessing data and ensures that the data is presented
 * in a suitable format for the ViewModel and UI layers.
 */
class RecipeRepository(
    private val recipeDao: RecipeDao
) {

    /**
     * Converts a network layer Recipe object into a domain-oriented RecipeModel object.
     *
     * @receiver A Recipe instance from the network layer.
     * @return A corresponding RecipeModel that is easier to use within the app.
     */
    private fun Recipe.toRecipeModel(originIngredient: String? = null): RecipeModel {
        return RecipeModel(
            id = this.recipe_id,
            name = this.recipe_name,
            description = this.recipe_description,
            imageUrl = this.recipe_image,
            calories = this.recipe_nutrition.calories,
            carbohydrate = this.recipe_nutrition.carbohydrate,
            fat = this.recipe_nutrition.fat,
            protein = this.recipe_nutrition.protein,
            ingredients = this.recipe_ingredients.ingredient,
            types = this.recipe_types?.recipe_type,
            originIngredient = originIngredient
        )
    }

    /**
     * Converts a RecipeModel object into a RecipeEntity object for local database storage.
     *
     * @receiver A RecipeModel representing a recipe in a domain-friendly format.
     * @return A RecipeEntity instance suitable for Room database insertion.
     */
    private fun RecipeModel.toEntity(): RecipeEntity {
        return RecipeEntity(
            id = id,
            name = name,
            description = description,
            imageUrl = imageUrl,
            calories = calories,
            carbohydrate = carbohydrate,
            fat = fat,
            protein = protein,
            ingredients = ingredients,
            types = types,
            originIngredient = originIngredient ?: "" // If nullable, handle default
        )
    }

    // Convert RecipeModel to domain Recipe if needed
    // Or directly store domain Recipe if you already have a mapping.
    // Assuming you have a method to map RecipeModel to Recipe and vice versa.
    private fun RecipeEntity.toModel(): RecipeModel {
        return RecipeModel(
            id = id,
            name = name,
            description = description,
            imageUrl = imageUrl,
            calories = calories,
            carbohydrate = carbohydrate,
            fat = fat,
            protein = protein,
            ingredients = ingredients,
            types = types,
            originIngredient = originIngredient
        )
    }

    /**
     * Stores a list of network-level Recipe objects into the local database.
     *
     * Steps:
     * 1. Convert each Recipe object to a RecipeModel for domain-level consistency.
     * 2. Convert each RecipeModel to a RecipeEntity for database storage.
     * 3. Insert all the resulting entities into the local database.
     *
     * @param recipes The list of Recipe objects retrieved from the network.
     */
    suspend fun storeRecipes(recipes: List<Recipe>, originIngredient: String) {
        val models = recipes.map { it.toRecipeModel(originIngredient) }
        val entities = models.map { it.toEntity() }
        recipeDao.insertAll(entities)
    }

    /**
     * Updates the imageUrl of a specific recipe in the local database.
     *
     * @param id The ID of the recipe to update.
     * @param imageUrl The new image URL, or null if none is available.
     */
    suspend fun updateRecipeImageUrl(id: Long, imageUrl: String?) {
        recipeDao.updateImageUrl(id, imageUrl)
    }

    /**
     * Retrieve recipes from the database filtered by ingredientName (originIngredient).
     */
    suspend fun getRecipesByIngredient(ingredientName: String): List<RecipeModel> {
        val entities = recipeDao.getRecipesByIngredient(ingredientName)
        return entities.map { it.toModel() }
    }

    /**
     * Get all Recipes from the database and convert to RecipeModel.
     */
    suspend fun getAllRecipesFromDB(): List<RecipeModel> {
        val entities = recipeDao.getAllRecipes()
        return entities.map { it.toModel() }
    }

    suspend fun deleteRecipesByIngredient(ingredientName: String) {
        recipeDao.deleteRecipesByIngredient(ingredientName)
    }

    suspend fun getAllOriginIngredients(): List<String> {
        return recipeDao.getAllOriginIngredients()
    }

    suspend fun getRecipeImageUrlById(recipeId: Long): String? {
        return recipeDao.getRecipeImageUrlById(recipeId)
    }
}
