package com.example.smartreciperecommenderapp.utils

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsLogger(private val analytics: FirebaseAnalytics) {
    // Log event for viewing a recipe
    fun logViewRecipe(recipeName: String) {
        val bundle = Bundle().apply {
            putString("recipe_name", recipeName)
        }
        analytics.logEvent("view_recipe", bundle)
        Log.d("AnalyticsLogger", "Logged view recipe event for recipe: $recipeName")
    }
}
