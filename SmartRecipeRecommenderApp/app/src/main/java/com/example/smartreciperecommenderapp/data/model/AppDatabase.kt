package com.example.smartreciperecommenderapp.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [IngredientEntity::class, RecipeEntity::class, RecipeDetailEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class, RecipeDetailConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeDetailDao(): RecipeDetailDao
}
