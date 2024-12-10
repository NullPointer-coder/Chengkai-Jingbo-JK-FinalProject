package com.example.smartreciperecommenderapp.data.model

import java.util.Date

data class Ingredient(
    val id: Int = 0,                        // Unique identifier
    val instanceId: Int = 0,                // Unique identifier for this instance
    val name: String = "Unknown",           // Ingredient name
    val quantity: Double = 0.0,             // Quantity
    val unit: String = "Unknown",           // Unit of measurement
    val category: String = "General",       // Category
    val expiryDate: Date? = null,           // Expiration date (optional)
    val imageUrl: String? = null,           // Image URL (optional)
    val calories: Double? = null,           // Calories (optional)
    val fat: Double? = null                 // Fat content (optional)
)
