package com.example.smartreciperecommenderapp.data.repository

import android.util.Log
import com.example.smartreciperecommenderapp.data.model.RecipeDetailDao
import com.example.smartreciperecommenderapp.data.model.RecipeDetailEntity
import com.example.smartreciperecommenderapp.data.model.RecipeDetailModel
import com.example.smartreciperecommenderapp.ui.api.FatSecretRecipeDetailsResponse
import com.google.gson.Gson

class RecipeDetailRepository(
    private val recipeDetailDao: RecipeDetailDao
){
    private val gson = Gson()

    private fun FatSecretRecipeDetailsResponse.toEntity(): RecipeDetailEntity {
        val categoriesJson = this.recipe_categories?.let { gson.toJson(it) }
        val typesJson = this.recipe_types?.let { gson.toJson(it) }
        val servingSizesJson = this.serving_sizes?.let { gson.toJson(it) }
        val ingredientsJson = this.ingredients.let { gson.toJson(it) }
        val directionsJson = this.directions.let { gson.toJson(it) }
        val mainImageUrl = this.recipe_images?.recipe_image?.firstOrNull()

        Log.d("RecipeDetailRepository", "serving sizes: ${this.serving_sizes}")
        return RecipeDetailEntity(
            recipeId = this.recipe_id,
            name = this.recipe_name,
            description = this.recipe_description,
            servings = this.number_of_servings,
            prepTime = this.preparation_time_min,
            cookTime = this.cooking_time_min,
            gramsPerPortion = this.grams_per_portion,
            rating = this.rating,
            categoriesJson = categoriesJson,
            typesJson = typesJson,
            servingSizesJson = servingSizesJson,
            ingredientsJson = ingredientsJson,
            directionsJson = directionsJson,
            imageUrl = mainImageUrl
        )
    }

    private fun RecipeDetailEntity.toModel(): RecipeDetailModel {
        return RecipeDetailModel(
            recipeId = this.recipeId,
            name = this.name,
            description = this.description,
            servings = this.servings,
            prepTime = this.prepTime,
            cookTime = this.cookTime,
            gramsPerPortion = this.gramsPerPortion,
            rating = this.rating,
            categoriesJson = this.categoriesJson,
            typesJson = this.typesJson,
            servingSizesJson = this.servingSizesJson,
            ingredientsJson = this.ingredientsJson,
            directionsJson = this.directionsJson,
            imageUrl = this.imageUrl
        )
    }


    suspend fun storeRecipeDetails(detail: FatSecretRecipeDetailsResponse) {
        val entity = detail.toEntity()
        recipeDetailDao.insertDetail(entity)
    }

    suspend fun getRecipeDetailsFromDB(recipeId: Long): RecipeDetailModel? {
        val entity = recipeDetailDao.getRecipeDetail(recipeId) ?: return null
        return entity.toModel()
    }


}