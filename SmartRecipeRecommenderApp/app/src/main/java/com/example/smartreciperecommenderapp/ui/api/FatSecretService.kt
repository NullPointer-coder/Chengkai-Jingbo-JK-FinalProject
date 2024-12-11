package com.example.smartreciperecommenderapp.ui.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


// Ingredients

/**
 * Data models for the foods.search response
 */

data class FatSecretSearchResponse(
    val foods: Foods
)

data class Foods(
    val food: List<FatSecretFood>
)

data class FatSecretFood(
    val food_name: String,
    val food_description: String,
    val food_id: String
)



// Recipes
data class FatSecretRecipesResponse(
    val recipes: Recipes // 主对象，包含 recipes 相关信息
)

data class Recipes(
    val max_results: Int,         // 最大返回结果数（字符串格式）
    val page_number: Int,         // 当前页数
    val total_results: Int,       // 符合条件的总结果数
    val recipe: List<Recipe>         // 食谱列表，包含多个 Recipe 对象
)

data class Recipe(
    val recipe_id: Long,           // 食谱的唯一 ID
    val recipe_name: String,         // 食谱名称
    val recipe_description: String,  // 食谱描述
    val recipe_image: String?,       // 食谱图片的 URL（可能为空）
    val recipe_nutrition: RecipeNutrition,  // 食谱的营养信息
    val recipe_ingredients: RecipeIngredients,  // 食谱的主要食材
    val recipe_types: RecipeTypes?  // 食谱类型（例如 "Main Dish", "Salad"）
)

data class RecipeNutrition(
    val calories: String,            // 卡路里含量
    val carbohydrate: String,        // 碳水化合物含量
    val fat: String,                 // 脂肪含量
    val protein: String              // 蛋白质含量
)

data class RecipeIngredients(
    val ingredient: List<String>     // 食材列表
)

data class RecipeTypes(
    val recipe_type: List<String>    // 食谱类型列表
)



// Recipe Details

data class FatSecretRecipeDetailsWrapper(
    val recipe: FatSecretRecipeDetailsResponse
)

data class FatSecretRecipeDetailsResponse(
    val recipe_id: Long,                 // Unique recipe identifier
    val recipe_name: String,             // Name of the recipe
    val recipe_url: String,              // URL of the recipe on FatSecret
    val recipe_description: String,      // Short description of the recipe
    val number_of_servings: Double,      // Number of servings the recipe is intended for
    val grams_per_portion: Double?,      // Number of grams per portion (optional)
    val preparation_time_min: Int?,      // Preparation time in minutes (optional)
    val cooking_time_min: Int?,          // Cooking time in minutes (optional)
    val rating: Int?,                    // Recipe rating out of 5 (optional)
    val recipe_types: RecipeTypesWrapper?,
    val recipe_categories: RecipeCategoriesWrapper?,
    val recipe_images: RecipeImagesWrapper?, // List of image URLs for the recipe
    val serving_sizes: ServingSizesWrapper?,    // Nutritional information for servings
    val ingredients: IngredientsWrapper,
    val directions: DirectionsWrapper
)

data class RecipeCategory(
    val recipe_category_name: String,    // Category name
    val recipe_category_url: String      // Category URL
)

data class RecipeTypesWrapper(
    val recipe_type: List<String>
)

data class RecipeCategoriesWrapper(
    val recipe_category: List<RecipeCategory>
)

data class RecipeImagesWrapper(
    val recipe_image: List<String>
)

data class ServingSizesWrapper(
    val Serving: List<Serving>
)

data class Serving(
    val serving_size: String,            // Description of the serving size
    val calories: Double,                // Energy content in kcal
    val carbohydrate: Double?,           // Carbohydrate content in grams (optional)
    val protein: Double?,                // Protein content in grams (optional)
    val fat: Double?,                    // Fat content in grams (optional)
    val saturated_fat: Double?,          // Saturated fat content in grams (optional)
    val polyunsaturated_fat: Double?,    // Polyunsaturated fat content in grams (optional)
    val monounsaturated_fat: Double?,    // Monounsaturated fat content in grams (optional)
    val trans_fat: Double?,              // Trans fat content in grams (optional)
    val cholesterol: Double?,            // Cholesterol content in mg (optional)
    val sodium: Double?,                 // Sodium content in mg (optional)
    val potassium: Double?,              // Potassium content in mg (optional)
    val fiber: Double?,                  // Fiber content in grams (optional)
    val sugar: Double?,                  // Sugar content in grams (optional)
    val vitamin_a: Double?,              // Vitamin A percentage (optional)
    val vitamin_c: Double?,              // Vitamin C percentage (optional)
    val calcium: Double?,                // Calcium percentage (optional)
    val iron: Double?                    // Iron percentage (optional)
)



data class IngredientsWrapper(
    val ingredient: List<Ingredient>
)


data class Ingredient(
    val food_id: Long,                   // Unique food identifier
    val food_name: String,               // Name of the food
    val serving_id: Long,                // Unique serving identifier
    val number_of_units: Double,         // Number of units in the serving size
    val measurement_description: String, // Description of the unit of measure
    val ingredient_url: String,          // URL of the ingredient on FatSecret
    val ingredient_description: String   // Fully formatted description of the ingredient
)

data class DirectionsWrapper(
    val direction: List<Direction>
)


data class Direction(
    val direction_number: Int,           // Order of the direction
    val direction_description: String    // Instruction for this step
)





/**
 * Retrofit service interface to communicate with FatSecret API.
 */

interface FatSecretService {

    /**
     * Searches for foods (foods.search)
     * Requires adding "Authorization: Bearer <access_token>" to the header.
     * For example:
     * method=foods.search
     * format=json
     * search_expression=ingredientName
     */
    @GET("rest/server.api")
    suspend fun searchFoods(
        @Header("Authorization") authorization: String, // e.g. "Bearer <token>"
        @Query("method") method: String = "foods.search",
        @Query("format") format: String = "json",
        @Query("search_expression") name: String
    ): FatSecretSearchResponse


    @GET("rest/server.api")
    suspend fun searchRecipes(
        @Header("Authorization") authorization: String, // "Bearer <token>"
        @Query("method") method: String = "recipes.search.v3",
        @Query("format") format: String = "json",
        @Query("search_expression") name: String,
        @Query("max_results") maxResults: Int = 10 // Limit to 10 results
    ): FatSecretRecipesResponse


    @GET("rest/recipe/v2")
    suspend fun searchRecipesDetails(
        @Header("Authorization") authorization: String, // "Bearer <token>"
        //@Query("method") method: String = "recipe.get.v2",
        @Query("recipe_id") id: Long,
        @Query("format") format: String = "json"
    ): FatSecretRecipeDetailsWrapper

}



