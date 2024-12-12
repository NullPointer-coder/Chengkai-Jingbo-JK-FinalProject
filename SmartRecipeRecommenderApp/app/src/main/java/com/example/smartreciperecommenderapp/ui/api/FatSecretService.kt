package com.example.smartreciperecommenderapp.ui.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// Data models for Foods Search Response

/**
 * Represents the response body for the "foods.search" API call.
 * It contains a list of food items that match the search criteria.
 */
data class FatSecretSearchResponse(
    val foods: Foods
)

/**
 * Represents the foods object, which includes a list of individual foods.
 */
data class Foods(
    val food: List<FatSecretFood>
)

/**
 * Represents a single food item returned from the "foods.search" API endpoint.
 */
data class FatSecretFood(
    val food_name: String,
    val food_description: String,
    val food_id: String
)


// Data models for Recipes Search Response (recipes.search.v3)

/**
 * Represents the response body for the "recipes.search.v3" API call.
 * It contains the recipes object which holds information about recipe results.
 */
data class FatSecretRecipesResponse(
    val recipes: Recipes
)

/**
 * Represents the main recipes object containing metadata and a list of recipe objects.
 */
data class Recipes(
    val max_results: Int,         // Maximum number of results returned
    val page_number: Int,         // Current page number
    val total_results: Int,       // Total number of results available
    val recipe: List<Recipe>      // List of recipes
)

/**
 * Represents a single recipe item in the search results.
 */
data class Recipe(
    val recipe_id: Long,                    // Unique ID of the recipe
    val recipe_name: String,                // Name of the recipe
    val recipe_description: String,         // Short description of the recipe
    val recipe_image: String?,              // URL of the recipe's image (may be null)
    val recipe_nutrition: RecipeNutrition,  // Nutritional information
    val recipe_ingredients: RecipeIngredients, // Main ingredients used in the recipe
    val recipe_types: RecipeTypes?          // Types of recipe (e.g., "Main Dish", "Salad")
)

/**
 * Represents the basic nutritional information for a recipe.
 */
data class RecipeNutrition(
    val calories: String,
    val carbohydrate: String,
    val fat: String,
    val protein: String
)

/**
 * Represents the ingredients used in the recipe.
 * Each item is represented as a string that describes an ingredient.
 */
data class RecipeIngredients(
    val ingredient: List<String>
)

/**
 * Represents a collection of recipe types.
 */
data class RecipeTypes(
    val recipe_type: List<String>
)


// Data models for Recipe Details (recipe.get.v2)

/**
 * Wrapper for a single recipe details response.
 */
data class FatSecretRecipeDetailsWrapper(
    val recipe: FatSecretRecipeDetailsResponse
)

/**
 * Represents the full detailed information of a recipe, including
 * preparation steps, ingredients, images, and nutritional details.
 */
data class FatSecretRecipeDetailsResponse(
    val recipe_id: Long,
    val recipe_name: String,
    val recipe_url: String,
    val recipe_description: String,
    val number_of_servings: Double,
    val grams_per_portion: Double?,
    val preparation_time_min: Int?,
    val cooking_time_min: Int?,
    val rating: Int?,
    val recipe_types: RecipeTypesWrapper?,
    val recipe_categories: RecipeCategoriesWrapper?,
    val recipe_images: RecipeImagesWrapper?,
    val serving_sizes: ServingSizesWrapper?,
    val ingredients: IngredientsWrapper,
    val directions: DirectionsWrapper
)

/**
 * Represents a category associated with a recipe.
 */
data class RecipeCategory(
    val recipe_category_name: String,
    val recipe_category_url: String
)

/**
 * Wrapper for a list of recipe types.
 */
data class RecipeTypesWrapper(
    val recipe_type: List<String>
)

/**
 * Wrapper for a list of recipe categories.
 */
data class RecipeCategoriesWrapper(
    val recipe_category: List<RecipeCategory>
)

/**
 * Wrapper for a list of recipe images.
 */
data class RecipeImagesWrapper(
    val recipe_image: List<String>
)

/**
 * Wrapper for serving sizes and their nutritional information.
 */
data class ServingSizesWrapper(
    @SerializedName("serving") val serving: Serving
)

/**
 * Represents detailed nutritional information for a particular serving size of the recipe.
 */
data class Serving(
    val serving_size: String,
    val calories: Double,
    val carbohydrate: Double?,
    val protein: Double?,
    val fat: Double?,
    val saturated_fat: Double?,
    val polyunsaturated_fat: Double?,
    val monounsaturated_fat: Double?,
    val trans_fat: Double?,
    val cholesterol: Double?,
    val sodium: Double?,
    val potassium: Double?,
    val fiber: Double?,
    val sugar: Double?,
    val vitamin_a: Double?,
    val vitamin_c: Double?,
    val calcium: Double?,
    val iron: Double?
)

/**
 * Wrapper for a list of ingredients in the detailed recipe view.
 */
data class IngredientsWrapper(
    val ingredient: List<Ingredient>
)

/**
 * Represents an individual ingredient used in the recipe.
 */
data class Ingredient(
    val food_id: Long,
    val food_name: String,
    val serving_id: Long,
    val number_of_units: Double,
    val measurement_description: String,
    val ingredient_url: String,
    val ingredient_description: String
)

/**
 * Wrapper for the directions (steps) of the recipe.
 */
data class DirectionsWrapper(
    val direction: List<Direction>
)

/**
 * Represents a single direction (step) in preparing the recipe.
 */
data class Direction(
    val direction_number: Int,
    val direction_description: String
)


/**
 * Retrofit service interface for communicating with the FatSecret API.
 * Each method corresponds to a specific FatSecret API endpoint.
 */
interface FatSecretService {

    /**
     * Searches for foods by a given search expression.
     * Add "Authorization: Bearer <access_token>" to the header.
     * @param authorization The Bearer token for authentication.
     * @param name The search expression (ingredient name).
     */
    @GET("rest/server.api")
    suspend fun searchFoods(
        @Header("Authorization") authorization: String,
        @Query("method") method: String = "foods.search",
        @Query("format") format: String = "json",
        @Query("search_expression") name: String
    ): FatSecretSearchResponse


    /**
     * Searches for recipes based on a given search expression.
     * Add "Authorization: Bearer <access_token>" to the header.
     * @param authorization The Bearer token for authentication.
     * @param name The search expression.
     * @param maxResults The maximum number of recipes to return.
     */
    @GET("rest/server.api")
    suspend fun searchRecipes(
        @Header("Authorization") authorization: String,
        @Query("method") method: String = "recipes.search.v3",
        @Query("format") format: String = "json",
        @Query("search_expression") name: String,
        @Query("max_results") maxResults: Int
    ): FatSecretRecipesResponse


    /**
     * Retrieves detailed information for a specific recipe by its ID.
     * Add "Authorization: Bearer <access_token>" to the header.
     * @param authorization The Bearer token for authentication.
     * @param id The unique ID of the recipe.
     */
    @GET("rest/recipe/v2")
    suspend fun searchRecipesDetails(
        @Header("Authorization") authorization: String,
        @Query("recipe_id") id: Long,
        @Query("format") format: String = "json"
    ): FatSecretRecipeDetailsWrapper
}
