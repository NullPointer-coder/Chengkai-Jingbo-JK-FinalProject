package com.example.smartreciperecommenderapp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant


object NavRoutes {
    const val HOME = "home"
    const val INGREDIENTS = "ingredients"
    const val PROFILE = "profile"
    val icons = mapOf(
        HOME to Icons.Filled.Home,
        INGREDIENTS to Icons.Filled.Restaurant,
        PROFILE to Icons.Filled.Person
    )
}
