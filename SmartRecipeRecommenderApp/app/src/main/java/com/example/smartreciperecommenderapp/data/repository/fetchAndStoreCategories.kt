package com.example.smartreciperecommenderapp.data.repository

import com.example.smartreciperecommenderapp.data.model.*
import com.example.smartreciperecommenderapp.ui.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun fetchAndStoreCategories(apiService: OpenFoodFactsService, categoriesViewModel: CategoriesViewModel) {
    CoroutineScope(Dispatchers.IO).launch {
        val apiResponse = apiService.getCategories()
        val categoryEntities = apiResponse.tags.map {
            CategoryEntity(id = it.id, name = it.name, productCount = it.products)
        }
        categoriesViewModel.saveCategories(categoryEntities)
    }
}
