package com.example.smartreciperecommenderapp.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.smartreciperecommenderapp.ui.api.*

class RecipeDetailConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromCategoriesWrapper(wrapper: RecipeCategoriesWrapper?): String? {
        return wrapper?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toCategoriesWrapper(json: String?): RecipeCategoriesWrapper? {
        return json?.let {
            gson.fromJson(it, RecipeCategoriesWrapper::class.java)
        }
    }

    @TypeConverter
    fun fromTypesWrapper(wrapper: RecipeTypesWrapper?): String? {
        return wrapper?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toTypesWrapper(json: String?): RecipeTypesWrapper? {
        return json?.let {
            gson.fromJson(it, RecipeTypesWrapper::class.java)
        }
    }

    @TypeConverter
    fun fromServingSizesWrapper(wrapper: ServingSizesWrapper?): String? {
        return wrapper?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toServingSizesWrapper(json: String?): ServingSizesWrapper? {
        return json?.let {
            gson.fromJson(it, ServingSizesWrapper::class.java)
        }
    }

    @TypeConverter
    fun fromIngredientsWrapper(wrapper: IngredientsWrapper?): String? {
        return wrapper?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toIngredientsWrapper(json: String?): IngredientsWrapper? {
        return json?.let {
            gson.fromJson(it, IngredientsWrapper::class.java)
        }
    }

    @TypeConverter
    fun fromDirectionsWrapper(wrapper: DirectionsWrapper?): String? {
        return wrapper?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDirectionsWrapper(json: String?): DirectionsWrapper? {
        return json?.let {
            gson.fromJson(it, DirectionsWrapper::class.java)
        }
    }
}
